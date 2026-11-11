package com.ruoyi.common.core.redis;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

/**
 * spring redis 工具类
 *
 * @author ruoyi
 **/
@SuppressWarnings(value = { "unchecked", "rawtypes" })
@Component
public class RedisCache
{
    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value)
    {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     * @param timeout 时间
     * @param timeUnit 时间颗粒度
     */
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit)
    {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 仅在键不存在时写入带有效期的值，用于业务防重锁。
     */
    public <T> Boolean setCacheObjectIfAbsent(final String key, final T value, final long timeout,
            final TimeUnit timeUnit)
    {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit);
    }

    /**
     * 只有锁值仍属于当前调用方时才删除，避免超时后误删其他请求的新锁。
     */
    public boolean deleteObjectIfValueMatches(final String key, final Object expectedValue)
    {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then "
                + "return redis.call('del', KEYS[1]) else return 0 end";
        org.springframework.data.redis.core.script.DefaultRedisScript<Long> redisScript =
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(script, Long.class);
        Long deleted = (Long) redisTemplate.execute(redisScript,
                java.util.Collections.singletonList(key), expectedValue);
        return deleted != null && deleted > 0;
    }

    /**
     * 只有锁值仍属于当前调用方时才续期。
     *
     * 长任务（例如一次运行要串行跑多个测试点）必须边跑边续期，否则锁会在任务途中过期，
     * 同一名学生就能再占一份额度。续期同样要认主：锁已过期并被下一次请求重新持有时，
     * 旧请求不能把别人的锁继续延长下去。
     */
    public boolean expireIfValueMatches(final String key, final Object expectedValue, final long timeout,
            final TimeUnit unit)
    {
        long millis = unit.toMillis(timeout);
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then "
                + "return redis.call('pexpire', KEYS[1], " + millis + ") else return 0 end";
        org.springframework.data.redis.core.script.DefaultRedisScript<Long> redisScript =
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(script, Long.class);
        Long renewed = (Long) redisTemplate.execute(redisScript,
                java.util.Collections.singletonList(key), expectedValue);
        return renewed != null && renewed > 0;
    }

    /**
     * 原子递增限流计数器，并每次都滑动续期。
     *
     * 为什么不能沿用「INCR 后只在返回值==1 时 EXPIRE」：计数一旦因竞态漂移成负数，
     * 就再也不会等于 1，TTL 永不重设，键变成 TTL=-1 的永久键，限流额度被永久放宽。
     * 每次续期后，只要还有活跃请求键就不会提前消失；归零由 decrementCounterOrDelete 删除。
     *
     * 递增后仍 <=0 说明是历史漂移留下的负值（正确计数不会低于 0），就地重置为 1，
     * 让线上已漂移的键在下一次预留时自愈，不必手工清理 Redis。
     *
     * TTL 直接写进脚本文本而不走 ARGV：RedisTemplate 的值序列化器是 FastJson2，
     * 会把 "60000" 序列化成带引号的 JSON 字符串，PEXPIRE 无法解析。这里是 long 字面量，无注入风险。
     */
    public long incrementCounterWithTtl(final String key, final long timeout, final TimeUnit unit)
    {
        long millis = unit.toMillis(timeout);
        String script = "local v = redis.call('INCR', KEYS[1]) "
                + "if v <= 0 then redis.call('SET', KEYS[1], 1) v = 1 end "
                + "redis.call('PEXPIRE', KEYS[1], " + millis + ") return v";
        org.springframework.data.redis.core.script.DefaultRedisScript<Long> redisScript =
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(script, Long.class);
        Long value = (Long) redisTemplate.execute(redisScript,
                java.util.Collections.singletonList(key));
        return value == null ? 0L : value;
    }

    /**
     * 原子归还限流计数器：键存在才递减，减到 0 或以下立即删除。
     *
     * 为什么不能沿用「if (GET != null) DECR」：那是两条命令，键在 GET 与 DECR 之间到期时，
     * DECR 会把键重建成值为 -1 且不带 TTL 的新键，从此永不过期。
     * 键不存在时直接返回 null，绝不重建。
     */
    public Long decrementCounterOrDelete(final String key)
    {
        String script = "if redis.call('EXISTS', KEYS[1]) == 0 then return nil end "
                + "local v = redis.call('DECR', KEYS[1]) "
                + "if v <= 0 then redis.call('DEL', KEYS[1]) end return v";
        org.springframework.data.redis.core.script.DefaultRedisScript<Long> redisScript =
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(script, Long.class);
        return (Long) redisTemplate.execute(redisScript,
                java.util.Collections.singletonList(key));
    }

    /**
     * 设置有效时间
     *
     * @param key Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout)
    {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间
     *
     * @param key Redis键
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit)
    {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取有效时间
     *
     * @param key Redis键
     * @return 有效时间
     */
    public long getExpire(final String key)
    {
        return redisTemplate.getExpire(key);
    }

    /**
     * 判断 key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public Boolean hasKey(String key)
    {
        return redisTemplate.hasKey(key);
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(final String key)
    {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public boolean deleteObject(final String key)
    {
        return redisTemplate.delete(key);
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return
     */
    public boolean deleteObject(final Collection collection)
    {
        return redisTemplate.delete(collection) > 0;
    }

    /**
     * 缓存List数据
     *
     * @param key 缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long setCacheList(final String key, final List<T> dataList)
    {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(final String key)
    {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缓存Set
     *
     * @param key 缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet)
    {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext())
        {
            setOperation.add(it.next());
        }
        return setOperation;
    }

    /**
     * 获得缓存的set
     *
     * @param key
     * @return
     */
    public <T> Set<T> getCacheSet(final String key)
    {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap)
    {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(final String key)
    {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value)
    {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey)
    {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys)
    {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 删除Hash中的某条数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @return 是否成功
     */
    public boolean deleteCacheMapValue(final String key, final String hKey)
    {
        return redisTemplate.opsForHash().delete(key, hKey) > 0;
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern)
    {
        return redisTemplate.keys(pattern);
    }

    /**
     * 使用 SCAN 分批枚举键，避免课堂等高频业务使用 KEYS 阻塞 Redis 单线程。
     */
    public Collection<String> scanKeys(final String pattern, final long count)
    {
        return (Collection<String>) redisTemplate.execute((RedisCallback<Collection<String>>) connection -> {
            List<String> result = new ArrayList<>();
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions()
                    .match(pattern).count(Math.max(1, count)).build());
            try
            {
                while (cursor.hasNext())
                {
                    result.add(new String(cursor.next(), java.nio.charset.StandardCharsets.UTF_8));
                }
            }
            finally
            {
                cursor.close();
            }
            return result;
        });
    }
}
