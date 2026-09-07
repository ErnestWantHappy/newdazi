package com.ruoyi.framework.web.service;
import java.util.*;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.domain.entity.SysUser;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenReuseTest {
    private final Map<String,Object> cache=new HashMap<>();
    private TokenService service;
    @BeforeEach void setup() {
        RedisCache redis=mock(RedisCache.class);service=spy(new TokenService());
        ReflectionTestUtils.setField(service,"redisCache",redis);ReflectionTestUtils.setField(service,"secret","abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGH");
        ReflectionTestUtils.setField(service,"expireTime",120);
        when(redis.getCacheObject(anyString())).thenAnswer(i -> cache.get(i.getArgument(0)));
        doAnswer(i -> {cache.put(i.getArgument(0),i.getArgument(1));return null;}).when(redis).setCacheObject(anyString(),any(),anyInt(),any());
        doNothing().when(service).setUserAgent(any());
        client("0123456789abcdef0123456789abcdef");
    }
    @AfterEach void clearRequest() { RequestContextHolder.resetRequestAttributes(); }
    @Test void successfulRepeatReusesTokenAndRefreshesUserObject() {
        LoginUser first=user(1);String token=service.createLoginToken(first);
        LoginUser second=user(1);second.getUser().setNickName("最新姓名");
        assertEquals(token,service.createLoginToken(second));
        assertEquals(1,cache.keySet().stream().filter(k->k.startsWith("login_tokens:")).count());
        assertSame(second,cache.get("login_tokens:"+first.getToken()));
    }
    @Test void sameSharedBrowserDifferentAccountsAndDifferentBrowsersStayIndependent() {
        String first=service.createLoginToken(user(1));assertNotEquals(first,service.createLoginToken(user(2)));
        client("abcdef0123456789abcdef0123456789");assertNotEquals(first,service.createLoginToken(user(1)));
    }
    @Test void revokedSessionIsNeverReusedAndSchoolRotationAlwaysCreatesNewToken() {
        LoginUser first=user(1);String token=service.createLoginToken(first);cache.remove("login_tokens:"+first.getToken());
        assertNotEquals(token,service.createLoginToken(user(1)));
        assertNotEquals(service.createToken(user(1)),service.createToken(user(1)));
    }
    @Test void missingClientKeepsLegacyLoginWorking() {
        client(null);assertNotEquals(service.createLoginToken(user(1)),service.createLoginToken(user(1)));
    }
    private void client(String id) { MockHttpServletRequest request=new MockHttpServletRequest();if(id!=null)request.addHeader("X-Login-Client",id);RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request)); }
    private LoginUser user(long id) { LoginUser user=new LoginUser();user.setUserId(id);SysUser detail=new SysUser();detail.setUserName("student"+id);user.setUser(detail);return user; }
}
