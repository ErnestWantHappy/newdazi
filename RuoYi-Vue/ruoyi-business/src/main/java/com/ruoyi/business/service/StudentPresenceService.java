package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.redis.RedisCache;

/** 学生终端在线事实只保留 Redis TTL，不写签到或考勤记录。 */
@Service
public class StudentPresenceService {
    private static final int TTL_SECONDS = 60;
    @Autowired private RedisCache redisCache;

    public void heartbeat(Long studentId, String deviceId, String connectionIp) {
        if (studentId == null || deviceId == null || deviceId.length() > 80) return;
        Map<String,Object> value = new LinkedHashMap<>(); value.put("deviceId", deviceId); value.put("connectionIp", connectionIp); value.put("lastSeenAt", System.currentTimeMillis());
        redisCache.setCacheObject(key(studentId, deviceId), value, TTL_SECONDS, TimeUnit.SECONDS);
    }

    public Map<Long,Map<String,Object>> summary(List<Long> studentIds) {
        Map<Long,Map<String,Object>> result = new LinkedHashMap<>();
        if (studentIds == null || studentIds.isEmpty()) return result;
        for (Long studentId : studentIds) {
            if (studentId == null) continue;
            Collection<String> keys = redisCache.keys("classroom:presence:" + studentId + ":*");
            List<Map<String,Object>> devices = new ArrayList<>();
            if (keys != null) for (String key : keys) { Object value = redisCache.getCacheObject(key); if (value instanceof Map) { Map<?,?> raw = (Map<?,?>) value; Map<String,Object> device = new LinkedHashMap<>(); for (Map.Entry<?,?> entry : raw.entrySet()) device.put(String.valueOf(entry.getKey()), entry.getValue()); devices.add(device); } }
            Map<String,Object> state = new LinkedHashMap<>(); state.put("online", !devices.isEmpty()); state.put("onlineDeviceCount", devices.size());
            if (!devices.isEmpty()) { Collections.sort(devices, (a,b) -> Long.compare(number(b.get("lastSeenAt")), number(a.get("lastSeenAt")))); state.put("connectionIp", devices.get(0).get("connectionIp")); state.put("lastSeenAt", devices.get(0).get("lastSeenAt")); }
            state.put("devices", devices); result.put(studentId, state);
        }
        return result;
    }

    private static long number(Object value) { return value instanceof Number ? ((Number)value).longValue() : 0L; }
    private static String key(Long studentId, String deviceId) { return "classroom:presence:" + studentId + ":" + deviceId; }
}
