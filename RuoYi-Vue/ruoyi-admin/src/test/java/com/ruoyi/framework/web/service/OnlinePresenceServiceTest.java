package com.ruoyi.framework.web.service;
import java.util.*;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OnlinePresenceServiceTest {
    @Test void missingAndExpiredSummariesAreExcludedAndResultsSorted() {
        StringRedisTemplate redis=mock(StringRedisTemplate.class);
        ZSetOperations<String,String> sorted=mock(ZSetOperations.class);
        ValueOperations<String,String> values=mock(ValueOperations.class);
        when(redis.opsForZSet()).thenReturn(sorted); when(redis.opsForValue()).thenReturn(values);
        when(sorted.reverseRangeByScore(anyString(),anyDouble(),anyDouble())).thenReturn(new LinkedHashSet<>(Arrays.asList("1","2","3","4")));
        long now=System.currentTimeMillis();
        when(values.multiGet(anyCollection())).thenReturn(Arrays.asList(summary(1,now-1000),null,summary(3,now-400000),summary(4,now)));
        OnlinePresenceService service=new OnlinePresenceService(); ReflectionTestUtils.setField(service,"redis",redis);
        List<JSONObject> rows=service.activeUsers(); assertEquals(2,rows.size()); assertEquals(4,rows.get(0).getIntValue("userId"));
        verify(redis,never()).keys(anyString());
    }
    @Test void sameAccountMultipleTokensAreThrottledButOtherAccountsAreIndependent() {
        StringRedisTemplate redis=mock(StringRedisTemplate.class);
        OnlinePresenceService service=new OnlinePresenceService(); ReflectionTestUtils.setField(service,"redis",redis);
        service.touch(user(1,"a")); service.touch(user(1,"b")); service.touch(user(2,"c"));
        verify(redis,times(2)).execute(any(RedisScript.class),anyList(),any(),any(),any(),any(),any());
    }
    @Test void monitoringFailureDoesNotBreakAuthenticatedRequest() {
        StringRedisTemplate redis=mock(StringRedisTemplate.class);
        when(redis.execute(any(RedisScript.class),anyList(),any(),any(),any(),any(),any())).thenThrow(new IllegalStateException());
        OnlinePresenceService service=new OnlinePresenceService(); ReflectionTestUtils.setField(service,"redis",redis);
        assertDoesNotThrow(() -> service.touch(user(1,"a")));
    }
    private String summary(long id,long time) { JSONObject row=new JSONObject();row.put("userId",id);row.put("lastSeenAt",time);return row.toJSONString(); }
    private LoginUser user(long id,String token) { LoginUser user=new LoginUser();user.setUserId(id);user.setToken(token);SysUser detail=new SysUser();detail.setUserName("s"+id);user.setUser(detail);return user; }
}
