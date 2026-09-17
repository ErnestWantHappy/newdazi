package com.ruoyi.framework.web.service;
import java.util.function.Supplier;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class LoginAttemptGuardTest {
    @Test void blockedAttemptNeverAuthenticatesOrCreatesToken() {
        StringRedisTemplate redis=mock(StringRedisTemplate.class); LoginAttemptGuard guard=new LoginAttemptGuard();ReflectionTestUtils.setField(guard,"redis",redis);
        when(redis.execute(any(RedisScript.class),anyList(),any())).thenReturn(0L);
        Supplier<String> authenticate=mock(Supplier.class);
        assertThrows(ServiceException.class,()->guard.execute("student",authenticate));verifyNoInteractions(authenticate);
    }
    @Test void failedAuthenticationReleasesOnlyItsOwnLease() {
        StringRedisTemplate redis=mock(StringRedisTemplate.class); LoginAttemptGuard guard=new LoginAttemptGuard();ReflectionTestUtils.setField(guard,"redis",redis);
        when(redis.execute(any(RedisScript.class),anyList(),any())).thenReturn(1L);
        assertThrows(IllegalStateException.class,()->guard.execute("student",()->{throw new IllegalStateException();}));
        verify(redis,times(2)).execute(any(RedisScript.class),anyList(),any());
    }
}
