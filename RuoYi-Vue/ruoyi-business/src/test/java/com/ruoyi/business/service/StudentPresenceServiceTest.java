package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.ruoyi.common.core.redis.RedisCache;

class StudentPresenceServiceTest
{
    @Test
    void summaryScansPresenceKeysOnceAndGroupsDevicesByStudent()
    {
        RedisCache redis = mock(RedisCache.class);
        StudentPresenceService service = new StudentPresenceService();
        org.springframework.test.util.ReflectionTestUtils.setField(service, "redisCache", redis);
        when(redis.scanKeys("classroom:presence:*", 200)).thenReturn(Arrays.asList(
                "classroom:presence:10:device-a", "classroom:presence:10:device-b",
                "classroom:presence:99:other"));
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("deviceId", "device-a"); first.put("connectionIp", "10.0.0.1"); first.put("lastSeenAt", 100L);
        Map<String, Object> second = new LinkedHashMap<>();
        second.put("deviceId", "device-b"); second.put("connectionIp", "10.0.0.2"); second.put("lastSeenAt", 200L);
        when(redis.getCacheObject("classroom:presence:10:device-a")).thenReturn(first);
        when(redis.getCacheObject("classroom:presence:10:device-b")).thenReturn(second);

        Map<Long, Map<String, Object>> result = service.summary(Arrays.asList(10L, 11L));

        assertEquals(2, result.get(10L).get("onlineDeviceCount"));
        assertEquals("10.0.0.2", result.get(10L).get("connectionIp"));
        assertEquals(false, result.get(11L).get("online"));
        verify(redis).scanKeys("classroom:presence:*", 200);
    }
}
