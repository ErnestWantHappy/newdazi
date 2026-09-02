package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizTeacherClass;
import com.ruoyi.business.domain.vo.StudentImportResult;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.service.AnswerDeletionGuardService;
import com.ruoyi.business.service.IBizTeacherClassService;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import com.ruoyi.system.service.ISysUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BizStudentImportGovernanceTest
{
    @Mock
    private BizStudentMapper bizStudentMapper;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SysUserRoleMapper userRoleMapper;
    @Mock
    private SysDeptMapper deptMapper;
    @Mock
    private ISysUserService userService;
    @Mock
    private IBizTeacherClassService teacherClassService;
    @Mock
    private RedisCache redisCache;
    @Mock
    private AnswerDeletionGuardService answerDeletionGuardService;

    @InjectMocks
    private BizStudentServiceImpl service;

    @BeforeEach
    void prepareTeacherAndSchool()
    {
        SysUser teacher = new SysUser();
        teacher.setUserId(8L);
        teacher.setDeptId(10L);
        teacher.setUserName("teacher");
        LoginUser loginUser = new LoginUser(8L, 10L, teacher, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));

        SysDept school = new SysDept();
        school.setDeptId(10L);
        school.setSchoolCode("71");
        when(deptMapper.selectDeptById(10L)).thenReturn(school);
    }

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void importsStudentsInBatchesAndReusesOnePasswordHash()
    {
        allowImportLock();
        when(teacherClassService.selectBizTeacherClassList(org.mockito.ArgumentMatchers.any(BizTeacherClass.class)))
                .thenReturn(Collections.emptyList());
        List<BizStudent> students = Arrays.asList(student("张三", "1"), student("李四", "2"));
        List<SysUser> persistedUsers = Arrays.asList(user(101L, "2025710101"), user(102L, "2025710102"));
        when(userMapper.selectActiveUsersByUserNames(anyList()))
                .thenReturn(Collections.emptyList(), persistedUsers);
        when(userMapper.batchInsertUsers(anyList())).thenReturn(2);
        when(userRoleMapper.batchUserRole(anyList())).thenReturn(2);
        when(bizStudentMapper.batchInsertBizStudents(anyList())).thenReturn(2);

        StudentImportResult result = service.importStudent(students, "teacher");

        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        ArgumentCaptor<List> usersCaptor = ArgumentCaptor.forClass(List.class);
        verify(userMapper).batchInsertUsers(usersCaptor.capture());
        List<SysUser> insertedUsers = usersCaptor.getValue();
        assertEquals(2, insertedUsers.size());
        assertFalse(insertedUsers.get(0).getPassword().isEmpty());
        assertEquals(insertedUsers.get(0).getPassword(), insertedUsers.get(1).getPassword());
        verify(userMapper, never()).checkUserNameUnique(anyString());
        verify(teacherClassService).insertBizTeacherClass(org.mockito.ArgumentMatchers.any(BizTeacherClass.class));
        verify(redisCache).deleteObjectIfValueMatches(anyString(), anyString());
    }

    @Test
    void rejectsDuplicateRowsInsideExcelBeforeWritingThemTwice()
    {
        allowImportLock();
        when(teacherClassService.selectBizTeacherClassList(org.mockito.ArgumentMatchers.any(BizTeacherClass.class)))
                .thenReturn(Collections.emptyList());
        List<BizStudent> students = Arrays.asList(student("张三", "1"), student("重复账号", "1"));
        when(userMapper.selectActiveUsersByUserNames(anyList()))
                .thenReturn(Collections.emptyList(), Collections.singletonList(user(101L, "2025710101")));
        when(userMapper.batchInsertUsers(anyList())).thenReturn(1);
        when(userRoleMapper.batchUserRole(anyList())).thenReturn(1);
        when(bizStudentMapper.batchInsertBizStudents(anyList())).thenReturn(1);

        StudentImportResult result = service.importStudent(students, "teacher");

        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        verify(userMapper).batchInsertUsers(anyList());
    }

    @Test
    void propagatesDatabaseFailureSoTransactionCanRollback()
    {
        allowImportLock();
        when(userMapper.selectActiveUsersByUserNames(anyList())).thenReturn(Collections.emptyList());
        when(userMapper.batchInsertUsers(anyList())).thenThrow(new ServiceException("数据库写入失败"));

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.importStudent(Collections.singletonList(student("张三", "1")), "teacher"));

        assertEquals("数据库写入失败", error.getMessage());
        verify(bizStudentMapper, never()).batchInsertBizStudents(anyList());
        verify(redisCache).deleteObjectIfValueMatches(anyString(), anyString());
    }

    @Test
    void rejectsConcurrentImportForSameSchool()
    {
        when(redisCache.setCacheObjectIfAbsent(anyString(), anyString(), anyLong(), eq(TimeUnit.MINUTES)))
                .thenReturn(false);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.importStudent(Collections.singletonList(student("张三", "1")), "teacher"));

        assertEquals("本校已有学生导入正在处理，请等待完成后再试", error.getMessage());
        verify(userMapper, never()).selectActiveUsersByUserNames(anyList());
    }

    private BizStudent student(String name, String studentNo)
    {
        BizStudent student = new BizStudent();
        student.setStudentName(name);
        student.setEntryYear("2025");
        student.setClassCode("1");
        student.setStudentNo(studentNo);
        return student;
    }

    private void allowImportLock()
    {
        when(redisCache.setCacheObjectIfAbsent(anyString(), anyString(), anyLong(), eq(TimeUnit.MINUTES)))
                .thenReturn(true);
    }

    private SysUser user(Long userId, String userName)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setUserName(userName);
        return user;
    }
}
