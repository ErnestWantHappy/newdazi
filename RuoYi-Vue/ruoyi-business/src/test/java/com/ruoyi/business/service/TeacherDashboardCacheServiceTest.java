package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ruoyi.common.core.redis.RedisCache;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeacherDashboardCacheServiceTest
{
    @Mock private RedisCache redisCache;
    @InjectMocks private TeacherDashboardCacheService service;

    @Test
    void buildsTeacherAndDepartmentScopedKey()
    {
        assertEquals("business:teacher-dashboard:v1:8:10", service.buildKey(8L, 10L));
    }

    @Test
    void evictsAllTeacherDashboardEntriesInDepartment()
    {
        Collection<String> keys = Arrays.asList(
                "business:teacher-dashboard:v1:8:10",
                "business:teacher-dashboard:v1:9:10");
        when(redisCache.keys("business:teacher-dashboard:v1:*:10")).thenReturn(keys);

        service.evictDepartment(10L);

        verify(redisCache).deleteObject(keys);
    }
}
