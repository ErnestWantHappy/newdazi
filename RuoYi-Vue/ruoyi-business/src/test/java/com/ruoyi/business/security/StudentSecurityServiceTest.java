package com.ruoyi.business.security;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.framework.web.service.PermissionService;

@ExtendWith(MockitoExtension.class)
class StudentSecurityServiceTest
{
    @Mock
    private PermissionService permissionService;

    @Mock
    private BizStudentMapper bizStudentMapper;

    @InjectMocks
    private StudentSecurityService service;

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void studentProfileWithoutRoleIsStillRecognized()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(88L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
        when(permissionService.hasRole("student")).thenReturn(false);
        when(bizStudentMapper.selectBizStudentByUserId(88L)).thenReturn(new BizStudent());

        assertTrue(service.isStudent());
    }
}
