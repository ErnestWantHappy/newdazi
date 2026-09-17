package com.ruoyi.business.service;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 教师首页缓存统一入口。
 *
 * 课程保存或删除会影响同校多位任课教师，因此失效时按学校清理，不能只删操作者自己的键。
 */
@Service
public class TeacherDashboardCacheService
{
    private static final String CACHE_PREFIX = "business:teacher-dashboard:v1:";
    private static final int CACHE_SECONDS = 30;

    @Autowired
    private RedisCache redisCache;

    public AjaxResult get(Long userId, Long deptId)
    {
        return redisCache.getCacheObject(buildKey(userId, deptId));
    }

    public void put(Long userId, Long deptId, AjaxResult value)
    {
        redisCache.setCacheObject(buildKey(userId, deptId), value, CACHE_SECONDS, TimeUnit.SECONDS);
    }

    public void evictDepartment(Long deptId)
    {
        if (deptId == null)
        {
            return;
        }
        Collection<String> keys = redisCache.keys(CACHE_PREFIX + "*:" + deptId);
        if (keys != null && !keys.isEmpty())
        {
            redisCache.deleteObject(keys);
        }
    }

    String buildKey(Long userId, Long deptId)
    {
        return CACHE_PREFIX + userId + ":" + deptId;
    }
}
