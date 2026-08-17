package com.ruoyi.business.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import com.ruoyi.business.service.IBizLessonService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class TeacherDashboardCacheTest
{
    @Mock private IBizLessonService lessonService;
    @Mock private RedisCache redisCache;
    @InjectMocks private TeacherDashboardController controller;

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cacheHitDoesNotRecalculateDashboard()
    {
        SysUser user = new SysUser();
        user.setUserId(8L);
        user.setDeptId(10L);
        LoginUser loginUser = new LoginUser(8L, 10L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
        AjaxResult cached = AjaxResult.success(Collections.emptyList());
        when(redisCache.getCacheObject(anyString())).thenReturn(cached);

        AjaxResult result = controller.getDashboardData();

        assertSame(cached, result);
        verify(lessonService, never()).getTeacherDashboardData();
    }
}
