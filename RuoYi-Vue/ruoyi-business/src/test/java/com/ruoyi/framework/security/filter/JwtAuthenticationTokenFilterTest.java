package com.ruoyi.framework.security.filter;

import javax.servlet.http.Cookie;

import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.framework.web.service.TokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationTokenFilterTest
{
    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void studentFileGetMayUseSameOriginTokenCookie() throws Exception
    {
        TokenService tokenService = mock(TokenService.class);
        LoginUser loginUser = mock(LoginUser.class);
        when(tokenService.getLoginUser("student-token")).thenReturn(loginUser);
        JwtAuthenticationTokenFilter filter = filter(tokenService);
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET", "/business/guide-sheet/student/uploads/7/upload_12345678");
        request.setCookies(new Cookie("Admin-Token", "student-token"));

        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());

        verify(tokenService).verifyToken(loginUser);
        assertSame(loginUser, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void protectedCommonPreviewMayUseSameOriginTokenCookie() throws Exception
    {
        TokenService tokenService = mock(TokenService.class);
        LoginUser loginUser = mock(LoginUser.class);
        when(tokenService.getLoginUser("file-token")).thenReturn(loginUser);
        JwtAuthenticationTokenFilter filter = filter(tokenService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/common/resource/view");
        request.setCookies(new Cookie("Admin-Token", "file-token"));

        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());

        verify(tokenService).verifyToken(loginUser);
        assertSame(loginUser, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void unrelatedAndWriteRequestsNeverUseCookieFallback() throws Exception
    {
        TokenService tokenService = mock(TokenService.class);
        JwtAuthenticationTokenFilter filter = filter(tokenService);
        MockHttpServletRequest unrelated = new MockHttpServletRequest("GET", "/business/guide-sheet/list");
        unrelated.setCookies(new Cookie("Admin-Token", "student-token"));

        filter.doFilterInternal(unrelated, new MockHttpServletResponse(), new MockFilterChain());

        verify(tokenService, never()).getLoginUser("student-token");
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        MockHttpServletRequest write = new MockHttpServletRequest(
                "POST", "/business/guide-sheet/student/uploads/7/upload_12345678");
        write.setCookies(new Cookie("Admin-Token", "student-token"));
        filter.doFilterInternal(write, new MockHttpServletResponse(), new MockFilterChain());

        verify(tokenService, never()).getLoginUser("student-token");
    }

    private JwtAuthenticationTokenFilter filter(TokenService tokenService)
    {
        JwtAuthenticationTokenFilter filter = new JwtAuthenticationTokenFilter();
        ReflectionTestUtils.setField(filter, "tokenService", tokenService);
        return filter;
    }
}
