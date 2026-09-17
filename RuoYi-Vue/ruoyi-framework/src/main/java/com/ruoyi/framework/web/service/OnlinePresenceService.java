package com.ruoyi.framework.web.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

/** 只保存近期活动摘要，监控查询不反序列化完整权限和登录会话。 */
@Service
public class OnlinePresenceService {
    public static final long WINDOW_MS = 5 * 60 * 1000L;
    private static final String INDEX = "monitor:active:v1";
    private static final String PREFIX = "monitor:active:user:";
    private static final Logger log = LoggerFactory.getLogger(OnlinePresenceService.class);
    private final Map<Long, Long> recorded = new ConcurrentHashMap<>();
    @Autowired private StringRedisTemplate redis;

    private static final DefaultRedisScript<Long> TOUCH = new DefaultRedisScript<>(
        "redis.call('SET',KEYS[2],ARGV[3],'PX',ARGV[4]); " +
        "redis.call('ZADD',KEYS[1],ARGV[2],ARGV[1]); " +
        "redis.call('ZREMRANGEBYSCORE',KEYS[1],'-inf',ARGV[5]); return 1", Long.class);
    private static final DefaultRedisScript<Long> REMOVE = new DefaultRedisScript<>(
        "local v=redis.call('GET',KEYS[2]); if v and cjson.decode(v).tokenId==ARGV[2] then " +
        "redis.call('DEL',KEYS[2]); redis.call('ZREM',KEYS[1],ARGV[1]); return 1 end; return 0", Long.class);

    public void touch(LoginUser user) {
        if (user == null || user.getUserId() == null || user.getUser() == null) return;
        long now = System.currentTimeMillis();
        Long previous = recorded.get(user.getUserId());
        if (previous != null && now - previous < 30000) return;
        // 认证请求只需每账号30秒更新一次；本机节流表也定期淘汰，避免长期增长。
        if (recorded.size() > 20000) recorded.entrySet().removeIf(e -> now - e.getValue() > WINDOW_MS);
        if (previous == null ? recorded.putIfAbsent(user.getUserId(), now) != null
                : !recorded.replace(user.getUserId(), previous, now)) return;
        try {
            JSONObject row = new JSONObject();
            row.put("userId", user.getUserId()); row.put("userName", user.getUsername());
            row.put("nickName", user.getUser().getNickName()); row.put("deptId", user.getDeptId());
            row.put("tokenId", user.getToken()); row.put("lastSeenAt", now);
            row.put("ipaddr", user.getIpaddr()); row.put("browser", user.getBrowser()); row.put("os", user.getOs());
            redis.execute(TOUCH, Arrays.asList(INDEX, PREFIX + user.getUserId()),
                    user.getUserId().toString(), Long.toString(now), row.toJSONString(),
                    Long.toString(WINDOW_MS), Long.toString(now - WINDOW_MS));
        } catch (RuntimeException e) {
            recorded.remove(user.getUserId(), now);
            // 监控故障不能把正常业务请求变为登录失败。
            log.warn("在线活动摘要更新失败：{}", e.getClass().getSimpleName());
        }
    }

    public List<JSONObject> activeUsers() {
        long cutoff = System.currentTimeMillis() - WINDOW_MS;
        Set<String> ids = redis.opsForZSet().reverseRangeByScore(INDEX, cutoff + 1, Double.POSITIVE_INFINITY);
        List<JSONObject> result = new ArrayList<>();
        if (ids == null || ids.isEmpty()) return result;
        List<String> keys = new ArrayList<>();
        for (String id : ids) keys.add(PREFIX + id);
        // 批量读取的只是每人一个轻量摘要，不读取login_tokens，也不执行KEYS。
        List<String> values = redis.opsForValue().multiGet(keys);
        if (values != null) for (String value : values) {
            if (value == null) continue;
            JSONObject row = JSON.parseObject(value);
            if (row.getLongValue("lastSeenAt") > cutoff) result.add(row);
        }
        result.sort((a, b) -> Long.compare(b.getLongValue("lastSeenAt"), a.getLongValue("lastSeenAt")));
        return result;
    }

    public long count() { return activeUsers().size(); }

    public void forget(LoginUser user) {
        if (user == null || user.getUserId() == null) return;
        recorded.remove(user.getUserId());
        redis.execute(REMOVE, Arrays.asList(INDEX, PREFIX + user.getUserId()),
                user.getUserId().toString(), user.getToken());
    }
}
