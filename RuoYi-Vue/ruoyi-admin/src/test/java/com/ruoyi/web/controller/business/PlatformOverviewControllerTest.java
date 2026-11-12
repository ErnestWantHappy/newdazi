package com.ruoyi.web.controller.business;

import com.ruoyi.common.core.domain.AjaxResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.PreDestroy;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** 不启动 Spring 或数据库，用受控查询验证快照发布和并发刷新边界。 */
@Timeout(20)
class PlatformOverviewControllerTest
{
    private static final String SCHOOL_COUNT_SQL =
            "select count(1) from sys_dept where del_flag = '0' and parent_id <> 0";
    private static final int CALLERS = 6;

    private PlatformOverviewController controller;
    private JdbcTemplate jdbc;
    private ExecutorService requests;
    private ExecutorService refreshPool;
    private final List<CountDownLatch> releases = new ArrayList<>();
    private final AtomicLong databaseValue = new AtomicLong(17L);

    @BeforeEach
    void setUp()
    {
        controller = new PlatformOverviewController();
        jdbc = mock(JdbcTemplate.class);
        ReflectionTestUtils.setField(controller, "jdbcTemplate", jdbc);
        refreshPool = (ExecutorService) ReflectionTestUtils.getField(controller, "refreshPool");
        requests = Executors.newFixedThreadPool(CALLERS);
        when(jdbc.queryForObject(anyString(), eq(Long.class)))
                .thenAnswer(invocation -> databaseValue.get());
        when(jdbc.queryForList(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", "11月");
            row.put("value", databaseValue.get());
            row.put("paperCount", databaseValue.get());
            row.put("avgScore", 86.5);
            if (sql.contains("active_student_count"))
            {
                row.put("dept_name", "测试学校");
                row.put("active_student_count", databaseValue.get());
            }
            return Collections.singletonList(row);
        });
    }

    @AfterEach
    void tearDown() throws Exception
    {
        // 断言失败也先放开查询，保证测试自身不会遗留后台任务。
        releases.forEach(CountDownLatch::countDown);
        controller.destroy();
        requests.shutdownNow();
        assertTrue(requests.awaitTermination(5, TimeUnit.SECONDS), "请求测试线程未退出");
        assertTrue(refreshPool.awaitTermination(5, TimeUnit.SECONDS), "刷新线程未退出");
    }

    @Test
    void warmupAndConcurrentColdRequestsShareOneBuild() throws Exception
    {
        assertConcurrentColdBuild(true);
    }

    @Test
    void concurrentColdRequestsWithoutWarmupShareOneBuild() throws Exception
    {
        assertConcurrentColdBuild(false);
    }

    private void assertConcurrentColdBuild(boolean warmup) throws Exception
    {
        QueryGate gate = blockFirstCount();
        if (warmup)
        {
            controller.warmUpSnapshot();
            assertTrue(gate.entered.await(5, TimeUnit.SECONDS));
            controller.warmUpSnapshot();
        }
        CountDownLatch start = new CountDownLatch(1);
        ConcurrentLinkedQueue<Thread> callers = new ConcurrentLinkedQueue<>();
        List<Future<AjaxResult>> responses = new ArrayList<>();
        for (int i = 0; i < CALLERS; i++)
        {
            responses.add(requests.submit(() -> {
                callers.add(Thread.currentThread());
                assertTrue(start.await(5, TimeUnit.SECONDS));
                return controller.summary();
            }));
        }
        start.countDown();
        assertTrue(gate.entered.await(5, TimeUnit.SECONDS));
        // 等每位调用者真正进入冷等待，再放行数据库，避免顺序命中缓存冒充并发测试。
        awaitCondition(() -> callers.size() == CALLERS && callers.stream().allMatch(thread ->
                thread.getState() == Thread.State.TIMED_WAITING
                        && hasFrame(thread, "summary")), "冷请求没有全部等待共享任务");
        responses.forEach(response -> assertFalse(response.isDone()));
        verify(jdbc, times(1)).queryForObject(SCHOOL_COUNT_SQL, Long.class);
        assertNull(ReflectionTestUtils.getField(controller, "summaryCache"));
        gate.release.countDown();
        Long generatedAt = null;
        for (Future<AjaxResult> response : responses)
        {
            Map<String, Object> data = successfulData(response.get(5, TimeUnit.SECONDS));
            assertEquals(17L, schoolCount(data));
            assertEquals(false, data.get("stale"));
            assertEquals(false, data.get("refreshing"));
            assertEquals(false, data.get("refreshFailed"));
            assertEquals(0L, data.get("retryAfterMs"));
            if (generatedAt == null)
            {
                generatedAt = (Long) data.get("generatedAt");
            }
            assertEquals(generatedAt, data.get("generatedAt"));
            assertEquals(17L, rows(data, "answerTrend").get(0).get("value"));
            assertEquals(86.5, rows(data, "scoreTrend").get(0).get("value"));
        }
        controller.warmUpSnapshot();
        successfulData(controller.summary());
        drainRefreshPool();
        assertBuildQueries(1);
    }

    @Test
    void staleRequestReturnsOldSnapshotWhileRefreshIsBlockedThenPublishesNewData() throws Exception
    {
        Object old = seedStaleSnapshot(9L);
        long oldTime = (Long) ReflectionTestUtils.getField(old, "generatedAt");
        QueryGate gate = blockFirstCount();
        Map<String, Object> stale = successfulData(requests.submit(controller::summary).get(2, TimeUnit.SECONDS));
        assertTrue(gate.entered.await(5, TimeUnit.SECONDS));
        assertEquals(1L, gate.release.getCount(), "返回旧值时刷新查询仍应阻塞");
        assertEquals(9L, schoolCount(stale));
        assertEquals(oldTime, stale.get("generatedAt"));
        assertEquals(true, stale.get("stale"));
        assertEquals(true, stale.get("refreshing"));
        assertEquals(false, stale.get("refreshFailed"));
        assertSame(old, ReflectionTestUtils.getField(controller, "summaryCache"));
        for (int i = 0; i < CALLERS; i++)
        {
            assertEquals(9L, schoolCount(successfulData(controller.summary())));
        }
        gate.release.countDown();
        drainRefreshPool();
        Map<String, Object> fresh = successfulData(controller.summary());
        assertEquals(17L, schoolCount(fresh));
        assertTrue((Long) fresh.get("generatedAt") > oldTime);
        assertEquals(false, fresh.get("stale"));
        assertEquals(false, fresh.get("refreshing"));
        assertNotSame(old, ReflectionTestUtils.getField(controller, "summaryCache"));
        // 响应元数据只能加在副本上，旧响应也不能随后台发布被改写。
        assertEquals(9L, schoolCount(stale));
        assertFalse(snapshotData(old).containsKey("stale"));
        assertBuildQueries(1);
    }

    @Test
    void countFailurePreservesOldSnapshotAndBacksOff() throws Exception
    {
        assertStaleFailure(true);
    }

    @Test
    void lateQueryListFailureDiscardsPartialBuildAndBacksOff() throws Exception
    {
        assertStaleFailure(false);
    }

    private void assertStaleFailure(boolean countFailure) throws Exception
    {
        Object old = seedStaleSnapshot(9L);
        DataAccessResourceFailureException failure = failQuery(countFailure);
        long beforeFailure = System.currentTimeMillis();
        controller.summary();
        assertRefreshFailed(failure);
        assertSame(old, ReflectionTestUtils.getField(controller, "summaryCache"));
        long retryAt = (Long) ReflectionTestUtils.getField(controller, "nextRetryAt");
        assertTrue(retryAt >= beforeFailure + 30_000L, "失败退避应为 30 秒");
        assertTrue(retryAt > System.currentTimeMillis());
        assertTrue(retryAt <= System.currentTimeMillis() + 30_000L);
        int queries = mockingDetails(jdbc).getInvocations().size();
        for (int i = 0; i < CALLERS; i++)
        {
            Map<String, Object> data = successfulData(controller.summary());
            assertEquals(9L, schoolCount(data));
            assertEquals(ReflectionTestUtils.getField(old, "generatedAt"), data.get("generatedAt"));
            assertEquals(true, data.get("stale"));
            assertEquals(false, data.get("refreshing"));
            assertEquals(true, data.get("refreshFailed"));
            assertTrue((Long) data.get("retryAfterMs") > 0L);
            assertTrue((Long) data.get("retryAfterMs") <= 30_000L);
            controller.warmUpSnapshot();
        }
        drainRefreshPool();
        assertEquals(queries, mockingDetails(jdbc).getInvocations().size(), "退避期间不能再次查询");
        assertEquals(retryAt, ReflectionTestUtils.getField(controller, "nextRetryAt"), "读请求不能延长退避");
    }

    @Test
    void coldCountFailureReturnsErrorInsteadOfZeroSnapshot() throws Exception
    {
        assertColdFailure(true);
    }

    @Test
    void coldQueryListFailureReturnsErrorInsteadOfPartialSnapshot() throws Exception
    {
        assertColdFailure(false);
    }

    private void assertColdFailure(boolean countFailure) throws Exception
    {
        DataAccessResourceFailureException failure = failQuery(countFailure);
        assertUnavailable(controller.summary());
        assertRefreshFailed(failure);
        assertNull(ReflectionTestUtils.getField(controller, "summaryCache"));
        int queries = mockingDetails(jdbc).getInvocations().size();
        assertUnavailable(controller.summary());
        controller.warmUpSnapshot();
        drainRefreshPool();
        assertEquals(queries, mockingDetails(jdbc).getInvocations().size());
    }

    @Test
    void staleSnapshotRecoversAfterBackoffAndClearsFailureState() throws Exception
    {
        seedStaleSnapshot(9L);
        DataAccessResourceFailureException failure = failQuery(true);
        controller.summary();
        assertRefreshFailed(failure);
        recoverCountQuery();
        QueryGate recovery = blockFirstCount();
        ReflectionTestUtils.setField(controller, "nextRetryAt", System.currentTimeMillis() - 1L);
        Map<String, Object> retrying = successfulData(
                requests.submit(controller::summary).get(2, TimeUnit.SECONDS));
        assertTrue(recovery.entered.await(5, TimeUnit.SECONDS));
        assertEquals(1L, recovery.release.getCount());
        assertEquals(9L, schoolCount(retrying));
        assertEquals(true, retrying.get("stale"));
        assertEquals(true, retrying.get("refreshing"));
        assertEquals(false, retrying.get("refreshFailed"), "新一轮刷新不能沿用上次失败标志");
        assertEquals(0L, retrying.get("retryAfterMs"));
        controller.warmUpSnapshot();
        recovery.release.countDown();
        drainRefreshPool();
        assertRecovered(controller.summary());
        verify(jdbc, times(2)).queryForObject(SCHOOL_COUNT_SQL, Long.class);
        verify(jdbc, times(5)).queryForList(anyString());
    }

    @Test
    void coldFailureCanRecoverAfterBackoff() throws Exception
    {
        DataAccessResourceFailureException failure = failQuery(true);
        assertUnavailable(controller.summary());
        assertRefreshFailed(failure);
        recoverCountQuery();
        ReflectionTestUtils.setField(controller, "nextRetryAt", System.currentTimeMillis() - 1L);
        assertRecovered(controller.summary());
        drainRefreshPool();
        verify(jdbc, times(2)).queryForObject(SCHOOL_COUNT_SQL, Long.class);
    }

    @Test
    void requestThatSawStaleDataRechecksFreshnessAfterAcquiringLock() throws Exception
    {
        seedStaleSnapshot(9L);
        Object lock = ReflectionTestUtils.getField(controller, "summaryLock");
        AtomicReference<Thread> caller = new AtomicReference<>();
        Future<AjaxResult> response;
        Object published;
        synchronized (lock)
        {
            response = requests.submit(() -> {
                caller.set(Thread.currentThread());
                return controller.summary();
            });
            awaitCondition(() -> caller.get() != null
                    && caller.get().getState() == Thread.State.BLOCKED
                    && hasFrame(caller.get(), "triggerAsyncRefresh"), "请求未在刷新锁上等待");
            // 模拟另一个刷新刚在锁内完成发布；等待者必须重新检查，而非沿用锁外旧判断。
            published = newSnapshot(snapshotData(ReflectionTestUtils.getField(controller, "summaryCache")),
                    System.currentTimeMillis());
            ReflectionTestUtils.setField(controller, "summaryCache", published);
            ReflectionTestUtils.setField(controller, "refreshFuture", CompletableFuture.completedFuture(published));
        }
        Map<String, Object> data = successfulData(response.get(5, TimeUnit.SECONDS));
        drainRefreshPool();
        assertEquals(false, data.get("stale"));
        assertEquals(ReflectionTestUtils.getField(published, "generatedAt"), data.get("generatedAt"));
        assertSame(published, ReflectionTestUtils.getField(controller, "summaryCache"));
        verifyNoInteractions(jdbc);
    }

    @Test
    void destroyIsLifecycleHookAndStopsIdleInstancePool() throws Exception
    {
        assertTrue(PlatformOverviewController.class.getMethod("destroy").isAnnotationPresent(PreDestroy.class));
        PlatformOverviewController other = new PlatformOverviewController();
        ExecutorService otherPool = (ExecutorService) ReflectionTestUtils.getField(other, "refreshPool");
        try
        {
            assertNotSame(refreshPool, otherPool, "刷新池必须按控制器实例隔离");
            controller.summary();
            drainRefreshPool();
            Thread worker = refreshPool.submit(Thread::currentThread).get(5, TimeUnit.SECONDS);
            assertTrue(worker.isAlive());
            assertTrue(worker.isDaemon());
            assertEquals("platform-overview-refresh", worker.getName());
            controller.destroy();
            controller.destroy();
            assertTrue(refreshPool.isShutdown());
            assertTrue(refreshPool.awaitTermination(5, TimeUnit.SECONDS));
            worker.join(1000);
            assertFalse(worker.isAlive(), "销毁后实际工作线程必须退出");
            assertFalse(otherPool.isShutdown(), "销毁一个实例不能关闭其他实例的线程池");
        }
        finally
        {
            other.destroy();
            assertTrue(otherPool.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void destroyInterruptsInflightBuildAndReleasesColdWaiter() throws Exception
    {
        QueryGate gate = blockFirstCount();
        Future<AjaxResult> response = requests.submit(controller::summary);
        assertTrue(gate.entered.await(5, TimeUnit.SECONDS));
        CompletableFuture<?> pending = refreshFuture();
        controller.destroy();
        assertUnavailable(response.get(2, TimeUnit.SECONDS));
        assertTrue(gate.interrupted.await(5, TimeUnit.SECONDS), "运行中的查询没有收到中断");
        assertTrue(refreshPool.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(pending.isCompletedExceptionally());
        assertNull(ReflectionTestUtils.getField(controller, "summaryCache"));
        assertEquals(1L, gate.release.getCount(), "销毁不能依赖查询自行放行");
    }

    private QueryGate blockFirstCount()
    {
        QueryGate gate = new QueryGate();
        releases.add(gate.release);
        doAnswer(invocation -> {
            gate.entered.countDown();
            try
            {
                if (!gate.release.await(10, TimeUnit.SECONDS))
                {
                    throw new AssertionError("测试未放行数据库查询");
                }
            }
            catch (InterruptedException e)
            {
                gate.interrupted.countDown();
                Thread.currentThread().interrupt();
                throw new DataAccessResourceFailureException("受控查询已中断", e);
            }
            return databaseValue.get();
        }).when(jdbc).queryForObject(SCHOOL_COUNT_SQL, Long.class);
        return gate;
    }

    private DataAccessResourceFailureException failQuery(boolean countFailure)
    {
        DataAccessResourceFailureException failure = new DataAccessResourceFailureException("受控数据库故障");
        if (countFailure)
        {
            // 第一项统计失败必须终止整次构建，不能以 0 继续拼装。
            doThrow(failure).when(jdbc).queryForObject(SCHOOL_COUNT_SQL, Long.class);
        }
        else
        {
            // 最后一条列表查询才失败，验证此前已拼装的统计和趋势不会部分发布。
            doThrow(failure).when(jdbc).queryForList(contains("active_student_count"));
        }
        return failure;
    }

    private void recoverCountQuery()
    {
        databaseValue.set(29L);
        doAnswer(invocation -> databaseValue.get()).when(jdbc).queryForObject(SCHOOL_COUNT_SQL, Long.class);
    }

    private void assertRefreshFailed(Throwable failure) throws Exception
    {
        CompletableFuture<?> pending = refreshFuture();
        ExecutionException exception = assertThrows(ExecutionException.class,
                () -> pending.get(5, TimeUnit.SECONDS));
        assertSame(failure, exception.getCause());
        drainRefreshPool();
        assertEquals(true, ReflectionTestUtils.getField(controller, "refreshFailed"));
    }

    private void assertRecovered(AjaxResult result)
    {
        Map<String, Object> data = successfulData(result);
        assertEquals(29L, schoolCount(data));
        assertEquals(false, data.get("stale"));
        assertEquals(false, data.get("refreshFailed"));
        assertEquals(false, data.get("refreshing"));
        assertEquals(0L, data.get("retryAfterMs"));
        assertEquals(0L, ReflectionTestUtils.getField(controller, "nextRetryAt"));
        assertEquals(false, ReflectionTestUtils.getField(controller, "refreshFailed"));
    }

    private Object seedStaleSnapshot(long value) throws Exception
    {
        Map<String, Object> metric = new LinkedHashMap<>();
        metric.put("key", "schoolCount");
        metric.put("value", value);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("metrics", Collections.singletonList(metric));
        Object snapshot = newSnapshot(data, System.currentTimeMillis() - 300_001L);
        ReflectionTestUtils.setField(controller, "summaryCache", snapshot);
        return snapshot;
    }

    private Object newSnapshot(Map<String, Object> data, long generatedAt) throws Exception
    {
        Class<?> type = Class.forName(PlatformOverviewController.class.getName() + "$Snapshot");
        Constructor<?> constructor = type.getDeclaredConstructor(Map.class, long.class);
        constructor.setAccessible(true);
        return constructor.newInstance(data, generatedAt);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> snapshotData(Object snapshot)
    {
        return (Map<String, Object>) ReflectionTestUtils.getField(snapshot, "data");
    }

    private CompletableFuture<?> refreshFuture()
    {
        return (CompletableFuture<?>) ReflectionTestUtils.getField(controller, "refreshFuture");
    }

    private void drainRefreshPool() throws Exception
    {
        // 同一个单线程池中的屏障保证刷新任务（含失败处理）已结束，不靠固定 sleep。
        refreshPool.submit(() -> { }).get(5, TimeUnit.SECONDS);
    }

    private void assertBuildQueries(int builds)
    {
        verify(jdbc, times(builds)).queryForObject(SCHOOL_COUNT_SQL, Long.class);
        verify(jdbc, times(10 * builds)).queryForObject(anyString(), eq(Long.class));
        verify(jdbc, times(5 * builds)).queryForList(anyString());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> successfulData(AjaxResult result)
    {
        assertTrue(result.isSuccess(), () -> "预期成功快照，实际响应：" + result);
        assertTrue(result.get(AjaxResult.DATA_TAG) instanceof Map);
        return (Map<String, Object>) result.get(AjaxResult.DATA_TAG);
    }

    private void assertUnavailable(AjaxResult result)
    {
        assertTrue(result.isError(), () -> "数据库故障不能返回成功零值快照：" + result);
        assertFalse(result.containsKey(AjaxResult.DATA_TAG));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> rows(Map<String, Object> data, String key)
    {
        return (List<Map<String, Object>>) data.get(key);
    }

    private long schoolCount(Map<String, Object> data)
    {
        return ((Number) rows(data, "metrics").stream()
                .filter(metric -> "schoolCount".equals(metric.get("key")))
                .findFirst().orElseThrow(() -> new AssertionError("缺少学校统计"))
                .get("value")).longValue();
    }

    private boolean hasFrame(Thread thread, String method)
    {
        return Arrays.stream(thread.getStackTrace()).anyMatch(frame ->
                PlatformOverviewController.class.getName().equals(frame.getClassName())
                        && method.equals(frame.getMethodName()));
    }

    private void awaitCondition(BooleanSupplier condition, String message)
    {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline)
        {
            Thread.yield();
        }
        assertTrue(condition.getAsBoolean(), message);
    }

    private static final class QueryGate
    {
        private final CountDownLatch entered = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);
        private final CountDownLatch interrupted = new CountDownLatch(1);
    }
}
