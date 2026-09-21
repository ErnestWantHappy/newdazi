package com.ruoyi.framework.web.service;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.redis.RedisCache;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TokenServiceFailureTest {
    @Test
    void invalidJwtIsAnonymousButCacheFailurePropagates() {
        TokenService service = new TokenService();
        RedisCache cache = mock(RedisCache.class);
        String secret = "test-only-session-secret-for-regression";
        ReflectionTestUtils.setField(service, "secret", secret);
        ReflectionTestUtils.setField(service, "redisCache", cache);
        assertNull(service.getLoginUser("invalid-jwt"));
        verifyNoInteractions(cache);
        String token = Jwts.builder().claim(Constants.LOGIN_USER_KEY, "test-session")
                .signWith(SignatureAlgorithm.HS512, secret).compact();
        when(cache.getCacheObject(anyString())).thenThrow(new RedisConnectionFailureException("unavailable"));
        assertThrows(RedisConnectionFailureException.class, () -> service.getLoginUser(token));
    }
}
