package com.ruoyi.framework.web.service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/** 按账号限制重复认证，不按共享出口IP限制机房学生。 */
@Service
public class LoginAttemptGuard {
    @Autowired private StringRedisTemplate redis;
    private static final DefaultRedisScript<Long> ACQUIRE = new DefaultRedisScript<>(
        "if redis.call('EXISTS',KEYS[1])==1 or redis.call('EXISTS',KEYS[2])==1 then return 0 end; " +
        "redis.call('SET',KEYS[1],ARGV[1],'EX',60); redis.call('SET',KEYS[2],'1','EX',3); return 1", Long.class);
    private static final DefaultRedisScript<Long> RELEASE = new DefaultRedisScript<>(
        "if redis.call('GET',KEYS[1])==ARGV[1] then return redis.call('DEL',KEYS[1]) end; return 0", Long.class);

    public <T> T execute(String username, Supplier<T> action) {
        String normalized = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        String key = "login:pending:" + DigestUtils.md5DigestAsHex(normalized.getBytes(StandardCharsets.UTF_8));
        String owner = UUID.randomUUID().toString();
        if (!Long.valueOf(1).equals(redis.execute(ACQUIRE, Arrays.asList(key, key + ":cooldown"), owner)))
            throw new ServiceException("该账号正在登录或操作过快，请等待3秒后重试", 601);
        try { return action.get(); }
        finally { redis.execute(RELEASE, Collections.singletonList(key), owner); }
    }
}
