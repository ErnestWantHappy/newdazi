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
        Map<Long,List<Map<String,Object>>> devicesByStudent = new LinkedHashMap<>();
        Collection<String> presenceKeys = redisCache.scanKeys("classroom:presence:*", 200L);
        if (presenceKeys == null) presenceKeys = Collections.emptyList();
        for (String key : presenceKeys) {
            Long studentId = studentIdFromKey(key);
            if (studentId == null || !studentIds.contains(studentId)) continue;
            Object value = redisCache.getCacheObject(key);
            if (!(value instanceof Map)) continue;
            Map<String,Object> device = new LinkedHashMap<>();
            for (Map.Entry<?,?> entry : ((Map<?,?>) value).entrySet()) {
                device.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            devicesByStudent.computeIfAbsent(studentId, ignored -> new ArrayList<>()).add(device);
        }
        for (Long studentId : studentIds) {
            if (studentId == null) continue;
            List<Map<String,Object>> devices = devicesByStudent.getOrDefault(studentId, new ArrayList<>());
            Map<String,Object> state = new LinkedHashMap<>(); state.put("online", !devices.isEmpty()); state.put("onlineDeviceCount", devices.size());
            if (!devices.isEmpty()) { Collections.sort(devices, (a,b) -> Long.compare(number(b.get("lastSeenAt")), number(a.get("lastSeenAt")))); state.put("connectionIp", devices.get(0).get("connectionIp")); state.put("lastSeenAt", devices.get(0).get("lastSeenAt")); }
            state.put("devices", devices); result.put(studentId, state);
        }
        return result;
    }

    private static long number(Object value) { return value instanceof Number ? ((Number)value).longValue() : 0L; }
    private static Long studentIdFromKey(String key) {
        if (key == null || !key.startsWith("classroom:presence:")) return null;
        int begin = "classroom:presence:".length();
        int end = key.indexOf(':', begin);
        if (end <= begin) return null;
        try { return Long.valueOf(key.substring(begin, end)); } catch (NumberFormatException ignored) { return null; }
    }
    private static String key(Long studentId, String deviceId) { return "classroom:presence:" + studentId + ":" + deviceId; }
}
