package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.ProgrammingQuestionConfig;
import com.ruoyi.business.domain.ProgrammingTestCase;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.judge.Judge0Client;
import com.ruoyi.business.judge.Judge0Properties;
import com.ruoyi.business.judge.Judge0Request;
import com.ruoyi.business.judge.Judge0Result;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.ProgrammingJudgeMapper;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;

/**
 * 判题限流修复后的行为契约：锁要认主、TTL 要覆盖判题耗时、失败要退额度、槽位要还得回去。
 * 全部用 mock 断言调用契约，真实 Redis 的原子性由 RedisCounterAtomicityTest 覆盖。
 */
class ProgrammingRunLockContractTest {
    private static final String RUN_STUDENT_KEY = "judge0:run:student:30";
    private static final String RUN_CLASS_KEY = "judge0:run:class:9:2025:1";

    private ProgrammingSubmissionService service;
    private ProgrammingJudgeMapper programmingMapper;
    private BizLessonAssignmentMapper assignments;
    private BizStudentMapper studentMapper;
    private Judge0Client client;
    private RedisCache redisCache;
    private final BizStudent student = new BizStudent();

    @BeforeEach
    void setUp() {
        service = new ProgrammingSubmissionService();
        programmingMapper = mock(ProgrammingJudgeMapper.class);
        assignments = mock(BizLessonAssignmentMapper.class);
        studentMapper = mock(BizStudentMapper.class);
        client = mock(Judge0Client.class);
        BizLessonMapper lessons = mock(BizLessonMapper.class);
        BizLessonQuestionMapper lessonQuestions = mock(BizLessonQuestionMapper.class);
        redisCache = mock(RedisCache.class);
        when(redisCache.setCacheObjectIfAbsent(anyString(), any(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(redisCache.incrementCounterWithTtl(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(1L);

        Judge0Properties properties = new Judge0Properties();
        ReflectionTestUtils.setField(properties, "mode", "http");
        ReflectionTestUtils.setField(properties, "maxPolls", 2);
        ReflectionTestUtils.setField(properties, "pollIntervalMs", 100);
        ReflectionTestUtils.setField(properties, "classConcurrency", 8);
        ReflectionTestUtils.setField(properties, "studentSubmitsPerMinute", 10);
        ReflectionTestUtils.setField(properties, "maxSourceBytes", 65536);

        student.setStudentId(30L);
        student.setDeptId(9L);
        student.setEntryYear("2025");
        student.setClassCode("1");
        BizLessonAssignment current = new BizLessonAssignment();
        current.setLessonId(10L);
        current.setPracticalOpen(1);
        when(assignments.selectCurrentLessonByClass("2025", "1", 9L)).thenReturn(10L);
        when(assignments.selectCurrentAssignment("2025", "1", 9L)).thenReturn(current);
        BizLesson lesson = new BizLesson();
        lesson.setLessonId(10L);
        lesson.setDeptId(9L);
        when(lessons.selectBizLessonByLessonId(10L)).thenReturn(lesson);
        BizLessonQuestionDetailVo question = new BizLessonQuestionDetailVo();
        question.setQuestionId(20L);
        question.setQuestionType("practical");
        question.setPracticalMode("PYTHON");
        when(lessonQuestions.selectDetailsByLessonId(10L)).thenReturn(Collections.singletonList(question));

        ProgrammingQuestionConfig config = new ProgrammingQuestionConfig();
        config.setEnabled("1");
        config.setTimeLimitSeconds(3D);
        config.setMemoryLimitKb(131072);
        config.setMaxProcesses(1);
        config.setMaxFileSizeKb(64);
        config.setMaxOutputKb(32);
        when(programmingMapper.selectConfig(20L)).thenReturn(config);
        when(programmingMapper.selectPublicTestCases(20L)).thenReturn(Arrays.asList(publicCase(5L, "示例1")));

        ReflectionTestUtils.setField(service, "programmingMapper", programmingMapper);
        ReflectionTestUtils.setField(service, "lessonAssignmentMapper", assignments);
        ReflectionTestUtils.setField(service, "studentMapper", studentMapper);
        ReflectionTestUtils.setField(service, "lessonMapper", lessons);
        ReflectionTestUtils.setField(service, "lessonQuestionMapper", lessonQuestions);
        ReflectionTestUtils.setField(service, "redisCache", redisCache);
        ReflectionTestUtils.setField(service, "httpClient", client);
        ReflectionTestUtils.setField(service, "mockClient", client);
        ReflectionTestUtils.setField(service, "properties", properties);
    }

    private static ProgrammingTestCase publicCase(Long id, String name) {
        ProgrammingTestCase testCase = new ProgrammingTestCase();
        testCase.setTestCaseId(id);
        testCase.setCaseName(name);
        testCase.setInputText("8");
        testCase.setExpectedOutput("请买票");
        testCase.setIsPublic("1");
        return testCase;
    }

    private void givenJudgeOutput(String stdout) {
        Judge0Result started = new Judge0Result();
        started.setToken("t1");
        Judge0Result done = new Judge0Result();
        done.setStatusId(3);
        done.setStdout(stdout);
        when(client.submit(any(Judge0Request.class))).thenReturn(started);
        when(client.poll(anyString())).thenReturn(done);
    }

    @Test
    void 交互运行和示例运行共用同一把学生锁() {
        givenJudgeOutput("请买票\n");

        service.runPublicCases(student, 9L, 10L, 20L, "print(1)");
        service.runPublicCases(student, 9L, 10L, 20L, "print(input())", "8");

        ArgumentCaptor<String> keys = ArgumentCaptor.forClass(String.class);
        verify(redisCache, times(2)).setCacheObjectIfAbsent(keys.capture(), any(), anyLong(), any(TimeUnit.class));
        // 分成两把锁会让学生同时跑两条判题链，绕过“上一次运行还没结束”的串行约束
        assertEquals(Arrays.asList(RUN_STUDENT_KEY, RUN_STUDENT_KEY), keys.getAllValues());
    }

    @Test
    void 学生锁有效期必须覆盖最坏轮询耗时() {
        givenJudgeOutput("请买票\n");

        service.runPublicCases(student, 9L, 10L, 20L, "print(1)");

        ArgumentCaptor<Long> ttl = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TimeUnit> unit = ArgumentCaptor.forClass(TimeUnit.class);
        verify(redisCache).setCacheObjectIfAbsent(eq(RUN_STUDENT_KEY), any(), ttl.capture(), unit.capture());
        long seconds = unit.getValue().toSeconds(ttl.getValue());
        // 默认 24 次 × 500ms 已经 12 秒，服务器把间隔调大后接近 108 秒，10 秒/60 秒都会中途过期
        assertTrue(seconds >= 120L, "学生锁 TTL 只有 " + seconds + " 秒，覆盖不了判题耗时");
    }

    @Test
    void 运行结束只释放自己持有的锁令牌() {
        givenJudgeOutput("请买票\n");

        service.runPublicCases(student, 9L, 10L, 20L, "print(1)");

        ArgumentCaptor<Object> token = ArgumentCaptor.forClass(Object.class);
        verify(redisCache).setCacheObjectIfAbsent(eq(RUN_STUDENT_KEY), token.capture(), anyLong(), any(TimeUnit.class));
        assertNotNull(token.getValue());
        verify(redisCache).deleteObjectIfValueMatches(RUN_STUDENT_KEY, token.getValue());
        // 无条件删除会在锁超时后误删下一次运行的新锁
        verify(redisCache, never()).deleteObject(RUN_STUDENT_KEY);
        verify(redisCache).decrementCounterOrDelete(RUN_CLASS_KEY);
    }

    @Test
    void 判题服务异常也归还班级并发额度() {
        when(client.submit(any(Judge0Request.class))).thenThrow(new ServiceException("Judge0 无响应"));

        assertThrows(ServiceException.class, () -> service.runPublicCases(student, 9L, 10L, 20L, "print(1)"));

        verify(redisCache).decrementCounterOrDelete(RUN_CLASS_KEY);
        verify(redisCache).deleteObjectIfValueMatches(eq(RUN_STUDENT_KEY), any());
    }

    @Test
    void 班级超限时退还自己刚加的槽位并释放自己的锁() {
        when(redisCache.incrementCounterWithTtl(eq(RUN_CLASS_KEY), anyLong(), any(TimeUnit.class))).thenReturn(9L);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.runPublicCases(student, 9L, 10L, 20L, "print(1)"));

        assertEquals("本班同时运行的人数较多，请稍后再试", error.getMessage());
        verify(redisCache).decrementCounterOrDelete(RUN_CLASS_KEY);
        ArgumentCaptor<Object> token = ArgumentCaptor.forClass(Object.class);
        verify(redisCache).setCacheObjectIfAbsent(eq(RUN_STUDENT_KEY), token.capture(), anyLong(), any(TimeUnit.class));
        verify(redisCache).deleteObjectIfValueMatches(RUN_STUDENT_KEY, token.getValue());
        verify(client, never()).submit(any(Judge0Request.class));
    }

    @Test
    void 多个示例串行运行时逐个续期学生锁() {
        givenJudgeOutput("请买票\n");
        when(programmingMapper.selectPublicTestCases(20L))
                .thenReturn(Arrays.asList(publicCase(5L, "示例1"), publicCase(6L, "示例2")));

        service.runPublicCases(student, 9L, 10L, 20L, "print(1)");

        ArgumentCaptor<Object> token = ArgumentCaptor.forClass(Object.class);
        verify(redisCache).setCacheObjectIfAbsent(eq(RUN_STUDENT_KEY), token.capture(), anyLong(), any(TimeUnit.class));
        // 总耗时可能远超单个 TTL，必须边跑边续期，而不是把 TTL 放大到让学生被锁死几分钟
        verify(redisCache, times(2)).expireIfValueMatches(eq(RUN_STUDENT_KEY), eq(token.getValue()), anyLong(), any(TimeUnit.class));
        verify(redisCache, times(2)).expire(RUN_CLASS_KEY, 180L, TimeUnit.SECONDS);
    }

    @Test
    void 准入校验不再使用无事务的行锁() {
        givenJudgeOutput("请买票\n");

        service.runPublicCases(student, 9L, 10L, 20L, "print(1)");

        verify(assignments).selectCurrentAssignment("2025", "1", 9L);
        // 调用方没有事务，FOR UPDATE 出了语句就释放，锁不住任何东西
        verify(assignments, never()).selectCurrentAssignmentForUpdate(anyString(), anyString(), anyLong());
    }

    @Test
    void 判题入队被拒时退还分钟额度并标记服务异常() {
        com.ruoyi.business.domain.ProgrammingSubmission stored = new com.ruoyi.business.domain.ProgrammingSubmission();
        stored.setSubmissionId(11L); stored.setStudentId(30L); stored.setLessonId(10L); stored.setQuestionId(20L);
        stored.setStatusCode("WAITING");
        when(programmingMapper.selectSubmissionById(11L)).thenReturn(stored);
        org.mockito.Mockito.doAnswer(invocation -> {
            ((com.ruoyi.business.domain.ProgrammingSubmission) invocation.getArgument(0)).setSubmissionId(11L);
            return 1;
        }).when(programmingMapper).insertSubmission(any());
        ProgrammingSubmissionWorker worker = mock(ProgrammingSubmissionWorker.class);
        doThrow(new java.util.concurrent.RejectedExecutionException("full")).when(worker).judge(11L);

        com.ruoyi.business.domain.ProgrammingSubmission submission = service.submit(student, 9L, 10L, 20L,
                "print(1)", null, "abcdefgh12345678", "SUBMIT", "127.0.0.1", worker);

        assertEquals("SERVICE_ERROR", stored.getStatusCode());
        // 控制器据此决定不把课堂任务标成已提交，返回对象不能仍是 WAITING。
        assertEquals("SERVICE_ERROR", submission.getStatusCode());
        ArgumentCaptor<String> keys = ArgumentCaptor.forClass(String.class);
        verify(redisCache, atLeastOnce()).decrementCounterOrDelete(keys.capture());
        assertTrue(keys.getAllValues().stream().anyMatch(key -> key.startsWith("judge0:rate:30:")),
                "判题未开始必须退还分钟额度：" + keys.getAllValues());
        verify(redisCache).decrementCounterOrDelete("judge0:active:class:9:2025:1");
        verify(programmingMapper).updateSubmissionResult(stored);
    }

    @Test
    void 提交未落库时退还本次分钟额度() {
        doThrow(new RuntimeException("数据库不可用")).when(programmingMapper).insertSubmission(any());

        assertThrows(RuntimeException.class, () -> service.submit(student, 9L, 10L, 20L,
                "print(1)", null, "abcdefgh12345678", "SUBMIT", "127.0.0.1", null));

        ArgumentCaptor<String> keys = ArgumentCaptor.forClass(String.class);
        verify(redisCache, atLeastOnce()).decrementCounterOrDelete(keys.capture());
        List<String> refunded = new ArrayList<String>();
        for (String key : keys.getAllValues()) if (key.startsWith("judge0:rate:30:")) refunded.add(key);
        // 失败重试不该白白吃掉每分钟 10 次的成功配额
        assertEquals(1, refunded.size(), "应退还一次分钟额度，实际=" + keys.getAllValues());
        verify(redisCache).decrementCounterOrDelete("judge0:active:class:9:2025:1");
        verify(redisCache).deleteObject("judge0:active:student:30:10:20");
    }

    @Test
    void 学生档案查不到时仍按提交当时的班级桶归还槽位() {
        doReturn("judge0:active:class:139:2026:1").when(redisCache).getCacheObject("judge0:slot:submission:11");
        when(studentMapper.selectBizStudentByStudentId(12L)).thenReturn(null);

        ReflectionTestUtils.invokeMethod(service, "releaseSlot", 11L, 12L, 1L, 13L);

        // 学生被软删后档案查询返回 null，原来整段跳过，班级额度被永久占死
        verify(redisCache).deleteObject("judge0:active:student:12:1:13");
        verify(redisCache).decrementCounterOrDelete("judge0:active:class:139:2026:1");
        verify(studentMapper, never()).selectBizStudentByStudentId(anyLong());
    }

    @Test
    void 判题结束归还槽位但不退分钟额度() {
        doReturn("judge0:active:class:139:2026:1").when(redisCache).getCacheObject("judge0:slot:submission:11");

        ReflectionTestUtils.invokeMethod(service, "releaseSlot", 11L, 12L, 1L, 13L);

        ArgumentCaptor<String> keys = ArgumentCaptor.forClass(String.class);
        verify(redisCache, atLeastOnce()).decrementCounterOrDelete(keys.capture());
        for (String key : keys.getAllValues()) {
            assertTrue(!key.startsWith("judge0:rate:"), "判题成功不应退还分钟额度：" + key);
        }
        verify(redisCache).deleteObject("judge0:slot:submission:11");
    }

    @Test
    void 同一提交重复到达只归还一次班级槽位() {
        doReturn("judge0:active:class:139:2026:1").when(redisCache).getCacheObject("judge0:slot:submission:11");
        when(redisCache.setCacheObjectIfAbsent(eq("judge0:released:submission:11"), any(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true, false);

        ReflectionTestUtils.invokeMethod(service, "releaseSlot", 11L, 12L, 1L, 13L);
        ReflectionTestUtils.invokeMethod(service, "releaseSlot", 11L, 12L, 1L, 13L);

        // 取消和判题线程可能先后到达，第二次必须直接返回，否则班级额度被减成负数
        verify(redisCache, times(1)).decrementCounterOrDelete("judge0:active:class:139:2026:1");
    }

    @Test
    void 服务端运行结果仍只回放公开示例() {
        givenJudgeOutput("请买票\n");

        Map<String, Object> data = service.runPublicCases(student, 9L, 10L, 20L, "print(1)");

        assertEquals("SERVER", data.get("mode"));
        verify(programmingMapper, never()).insertSubmission(any());
    }
}