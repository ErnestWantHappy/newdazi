package com.ruoyi.business.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.framework.config.FastJson2JsonRedisSerializer;

/**
 * 判题限流计数器的真实 Redis 回归验证。
 *
 * 背景：原来的释放写法是「if (GET != null) DECR」两条命令，键在两条命令之间到期时
 * DECR 会把它重建成值为 -1、TTL=-1 的永久键；而预留只在返回值 == 1 时设 TTL，
 * 负值再也回不到 1，班级并发上限被永久放宽。本用例锁死修复后的三条不变量：
 * 键不存在时绝不重建、每次递增都带 TTL、归零即删除。
 *
 * 只操作独立前缀 bugaudit:junit: 下的键，结束时逐个删除，不碰任何业务键。
 * 本机没有 Redis 时整类跳过（assumeTrue），不会让 mvn test 失败。
 */
class RedisCounterAtomicityTest {
    private static LettuceConnectionFactory factory;
    private static RedisTemplate<Object, Object> template;
    private static RedisCache cache;
    private static String prefix;
    private static final Set<Object> TRACKED = ConcurrentHashMap.newKeySet();

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void connect() {
        String host = System.getProperty("redis.test.host", "127.0.0.1");
        int port = Integer.getInteger("redis.test.port", 6379);
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(host, port);
        // 口令只从环境变量取，不写进源码、命令行和日志
        String password = System.getenv("REDIS_TEST_PASSWORD");
        if (password != null && !password.isEmpty()) configuration.setPassword(password);
        factory = new LettuceConnectionFactory(configuration);
        String pong = null;
        try {
            factory.afterPropertiesSet();
            RedisConnection connection = factory.getConnection();
            try {
                pong = connection.ping();
            } finally {
                connection.close();
            }
        } catch (RuntimeException e) {
            assumeTrue(false, "本机 Redis 不可用，跳过真实并发验证：" + e.getClass().getSimpleName());
        }
        assumeTrue("PONG".equalsIgnoreCase(pong), "本机 Redis 不可用，跳过真实并发验证");

        template = new RedisTemplate<Object, Object>();
        template.setConnectionFactory(factory);
        // 与 RedisConfig 完全一致的序列化方式，否则 Lua 里的数字比较会因 JSON 引号而失真
        FastJson2JsonRedisSerializer<Object> serializer = new FastJson2JsonRedisSerializer<Object>(Object.class);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        cache = new RedisCache();
        cache.redisTemplate = template;
        prefix = "bugaudit:junit:" + UUID.randomUUID().toString().substring(0, 8) + ":";
    }

    @AfterAll
    static void cleanUp() {
        if (template != null && !TRACKED.isEmpty()) {
            template.delete(TRACKED);
            for (Object key : TRACKED) {
                assertFalse(Boolean.TRUE.equals(template.hasKey(key)), "测试键未清理：" + key);
            }
        }
        if (factory != null) factory.destroy();
    }

    private static String key(String name) {
        String full = prefix + name;
        TRACKED.add(full);
        return full;
    }

    @Test
    void 键不存在时归还额度绝不重建永久键() {
        String counter = key("missing");

        assertNull(cache.decrementCounterOrDelete(counter), "键不存在时必须返回 null");
        assertFalse(Boolean.TRUE.equals(template.hasKey(counter)), "DECR 不得把已过期的键重建成 -1");
    }

    @Test
    void 每次递增都续期而不是只在第一次设TTL() {
        String counter = key("ttl");

        assertEquals(1L, cache.incrementCounterWithTtl(counter, 60, TimeUnit.SECONDS));
        assertEquals(2L, cache.incrementCounterWithTtl(counter, 60, TimeUnit.SECONDS));
        assertEquals(3L, cache.incrementCounterWithTtl(counter, 60, TimeUnit.SECONDS));

        Long ttl = template.getExpire(counter, TimeUnit.SECONDS);
        assertTrue(ttl != null && ttl > 0 && ttl <= 60, "第三次递增后仍必须有 TTL，实际=" + ttl);
    }

    @Test
    void 线上已漂移的负值键在下一次预留时自愈() {
        String counter = key("drift");
        // 手工造出线上那种「值为负、TTL=-1」的永久键
        template.opsForValue().set(counter, Integer.valueOf(-16));
        assertEquals(-1L, template.getExpire(counter, TimeUnit.SECONDS).longValue());

        assertEquals(1L, cache.incrementCounterWithTtl(counter, 60, TimeUnit.SECONDS), "负值必须就地重置为 1");

        Long ttl = template.getExpire(counter, TimeUnit.SECONDS);
        assertTrue(ttl != null && ttl > 0, "自愈后必须重新带上 TTL，实际=" + ttl);
    }

    @Test
    void 计数归零立即删除键() {
        String counter = key("zero");
        cache.incrementCounterWithTtl(counter, 60, TimeUnit.SECONDS);
        cache.incrementCounterWithTtl(counter, 60, TimeUnit.SECONDS);

        assertEquals(1L, cache.decrementCounterOrDelete(counter).longValue());
        assertTrue(Boolean.TRUE.equals(template.hasKey(counter)), "还剩 1 个占用时不能删除");
        assertEquals(0L, cache.decrementCounterOrDelete(counter).longValue());
        assertFalse(Boolean.TRUE.equals(template.hasKey(counter)), "归零后必须删除，避免留下空计数键");
    }

    @Test
    void 认主释放和认主续期都不会动别人的锁() {
        String lock = key("lock");
        assertTrue(Boolean.TRUE.equals(cache.setCacheObjectIfAbsent(lock, "token-A", 60, TimeUnit.SECONDS)));

        assertFalse(cache.deleteObjectIfValueMatches(lock, "token-B"), "不能删掉别人的锁");
        assertFalse(cache.expireIfValueMatches(lock, "token-B", 5, TimeUnit.SECONDS), "不能给别人的锁续期");
        assertTrue(Boolean.TRUE.equals(template.hasKey(lock)));

        assertTrue(cache.expireIfValueMatches(lock, "token-A", 5, TimeUnit.SECONDS));
        Long ttl = template.getExpire(lock, TimeUnit.SECONDS);
        assertTrue(ttl != null && ttl > 0 && ttl <= 5, "认主续期必须生效，实际=" + ttl);

        assertTrue(cache.deleteObjectIfValueMatches(lock, "token-A"));
        assertFalse(Boolean.TRUE.equals(template.hasKey(lock)));
    }

    @Test
    void 并发预留不会突破班级并发上限() throws InterruptedException {
        final String counter = key("limit");
        final int limit = 8;
        final AtomicLong alive = new AtomicLong();
        final AtomicLong peak = new AtomicLong();

        runConcurrently(16, 50, new Runnable() {
            @Override
            public void run() {
                long value = cache.incrementCounterWithTtl(counter, 60, TimeUnit.SECONDS);
                if (value > limit) {
                    // 超额请求必须立刻归还自己刚加的那一次，不能把额度越滚越大
                    cache.decrementCounterOrDelete(counter);
                    return;
                }
                peak.accumulateAndGet(alive.incrementAndGet(), Math::max);
                try {
                    Thread.sleep(1L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                alive.decrementAndGet();
                cache.decrementCounterOrDelete(counter);
            }
        });

        assertTrue(peak.get() <= limit, "同时在跑的人数峰值=" + peak.get() + "，超过上限 " + limit);
        assertEquals(0L, alive.get());
        assertFalse(Boolean.TRUE.equals(template.hasKey(counter)), "全部归还后计数键必须消失");
    }

    @Test
    void 极短TTL下反复预留归还也不留下负值永久键() throws InterruptedException {
        final String counter = key("race");

        // 3 毫秒 TTL 把「键在两条命令之间到期」的窗口放大，正是线上漂移的成因
        runConcurrently(16, 200, new Runnable() {
            @Override
            public void run() {
                cache.incrementCounterWithTtl(counter, 3L, TimeUnit.MILLISECONDS);
                cache.decrementCounterOrDelete(counter);
            }
        });

        if (Boolean.TRUE.equals(template.hasKey(counter))) {
            Object value = template.opsForValue().get(counter);
            Long ttl = template.getExpire(counter, TimeUnit.SECONDS);
            long numeric = value instanceof Number ? ((Number) value).longValue() : Long.parseLong(String.valueOf(value));
            assertTrue(numeric > 0, "残留计数不得为负，实际=" + numeric);
            assertTrue(ttl != null && ttl != -1L, "残留计数不得永不过期，实际 TTL=" + ttl);
        }
    }

    /** 所有线程在同一起跑线出发，任何一个异常都必须让用例失败，不能被静默吞掉。 */
    private static void runConcurrently(int threads, int rounds, final Runnable task) throws InterruptedException {
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        final List<Throwable> errors = Collections.synchronizedList(new ArrayList<Throwable>());
        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        start.await();
                        for (int round = 0; round < rounds; round++) task.run();
                    } catch (Throwable t) {
                        errors.add(t);
                    } finally {
                        done.countDown();
                    }
                }
            }, "redis-counter-audit-" + i);
            thread.setDaemon(true);
            thread.start();
        }
        start.countDown();
        assertTrue(done.await(120L, TimeUnit.SECONDS), "并发任务未在 120 秒内结束");
        if (!errors.isEmpty()) {
            throw new AssertionError("并发任务出现异常：" + errors.get(0), errors.get(0));
        }
    }
}