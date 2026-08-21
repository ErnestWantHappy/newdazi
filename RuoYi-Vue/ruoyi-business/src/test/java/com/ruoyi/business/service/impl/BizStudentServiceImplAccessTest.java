package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BizStudentServiceImplAccessTest
{
    @Mock
    private BizStudentMapper studentMapper;

    @InjectMocks
    private BizStudentServiceImpl service;

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void administratorWithoutSchoolFilterKeepsQueryUnrestricted()
    {
        authenticate(1L, 139L, "admin");
        BizStudent query = new BizStudent();
        when(studentMapper.selectBizStudentList(any(BizStudent.class))).thenReturn(Collections.emptyList());

        service.selectBizStudentList(query);

        assertNull(query.getDeptId());
        assertNull(query.getTeacherUserId());
        verify(studentMapper).selectBizStudentList(query);
    }

    @Test
    void teacherQueryIsBoundToCurrentSchoolAndTeacher()
    {
        authenticate(104L, 139L, "teacher");
        BizStudent query = new BizStudent();
        when(studentMapper.selectBizStudentList(any(BizStudent.class))).thenReturn(Collections.emptyList());

        service.selectBizStudentList(query);

        assertEquals(139L, query.getDeptId());
        assertEquals(104L, query.getTeacherUserId());
        verify(studentMapper).selectBizStudentList(query);
    }

    private void authenticate(Long userId, Long deptId, String userName)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setDeptId(deptId);
        user.setUserName(userName);
        LoginUser loginUser = new LoginUser(userId, deptId, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }
}
