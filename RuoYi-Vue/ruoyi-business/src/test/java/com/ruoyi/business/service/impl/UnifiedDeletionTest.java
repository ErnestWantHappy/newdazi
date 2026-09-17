package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.*;
import com.ruoyi.business.service.AnswerDeletionGuardService;
import com.ruoyi.business.service.IBizTeacherClassService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class UnifiedDeletionTest {
    @Mock BizLessonMapper bizLessonMapper;
    @Mock BizLessonQuestionMapper lessonQuestionMapper;
    @Mock BizLessonAssignmentMapper lessonAssignmentMapper;
    @Mock PracticalGradingDeadlineMapper practicalGradingDeadlineMapper;
    @Mock LessonClassScopeMapper lessonClassScopeMapper;
    @Mock IotMapper iotMapper;
    @Mock BizStudentMapper bizStudentMapper;
    @Mock SysUserMapper userMapper;
    @Mock SysUserRoleMapper userRoleMapper;
    @Mock IBizTeacherClassService teacherClassService;
    @Mock AnswerDeletionGuardService answerDeletionGuardService;
    @Mock PurgeMapper purgeMapper;
    @Mock PurgeFileService purgeFileService;
    @Mock RedisCache redisCache;
    @InjectMocks BizLessonServiceImpl lessons;
    @InjectMocks BizStudentServiceImpl students;

    @BeforeEach void login() {
        SysUser user = new SysUser();
        user.setUserId(8L); user.setDeptId(10L); user.setUserName("teacher");
        LoginUser login = new LoginUser(8L, 10L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login, null, Collections.emptyList()));
        TransactionSynchronizationManager.initSynchronization();
    }
    @AfterEach void cleanup() {
        TransactionSynchronizationManager.clearSynchronization();
        SecurityContextHolder.clearContext();
    }
    private BizLesson lesson(long id, long owner) {
        BizLesson lesson = new BizLesson();
        lesson.setLessonId(id); lesson.setCreatorId(owner); lesson.setDeptId(10L);
        when(bizLessonMapper.selectBizLessonByLessonId(id)).thenReturn(lesson);
        return lesson;
    }
    private BizStudent student() {
        BizStudent student = new BizStudent();
        student.setStudentId(7L); student.setUserId(70L); student.setDeptId(10L);
        student.setEntryYear("2025"); student.setClassCode("1");
        when(bizStudentMapper.selectBizStudentsByIds(anyList(), eq(8L), eq(10L)))
            .thenReturn(Collections.singletonList(student));
        return student;
    }
    @Test void normalLessonDeleteCleansScoresAndWaitsForCommitBeforeFiles() {
        lesson(7L, 8L);
        when(bizLessonMapper.deleteBizLessonByLessonIds(any())).thenReturn(1);
        assertEquals(1, lessons.deleteBizLessonByLessonId(7L));
        verify(purgeMapper).deleteAnswersByLessons(Collections.singletonList(7L));
        verify(purgeMapper).deleteGuideAnswersByLessons(Collections.singletonList(7L));
        verifyNoInteractions(answerDeletionGuardService, purgeFileService);
        TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCommit());
        verify(purgeFileService).deleteUnreferenced(anyList());
    }
    @Test void mixedOwnershipBatchDoesNotStartDeleting() {
        lesson(7L, 8L); lesson(9L, 99L);
        assertThrows(ServiceException.class, () -> lessons.deleteBizLessonByLessonIds(new Long[]{7L, 9L}));
        verifyNoInteractions(purgeMapper, purgeFileService);
    }
    @Test void repeatedLessonIdIsDeletedOnce() {
        lesson(7L, 8L);
        when(bizLessonMapper.deleteBizLessonByLessonIds(any())).thenReturn(1);
        assertEquals(1, lessons.deleteBizLessonByLessonIds(new Long[]{7L, 7L}));
        verify(purgeMapper).deleteAnswersByLessons(Collections.singletonList(7L));
    }
    @Test void databaseFailureDoesNotScheduleFileDeletion() {
        lesson(7L, 8L);
        doThrow(new IllegalStateException("模拟数据库失败")).when(purgeMapper).deleteAnswersByLessons(anyList());
        assertThrows(IllegalStateException.class, () -> lessons.deleteBizLessonByLessonId(7L));
        assertTrue(TransactionSynchronizationManager.getSynchronizations().isEmpty());
        verifyNoInteractions(purgeFileService);
        verify(bizLessonMapper, never()).deleteBizLessonByLessonIds(any());
    }
    @Test void fileCleanupFailureDoesNotTurnCommittedDeletionIntoFailure() {
        lesson(7L, 8L);
        when(bizLessonMapper.deleteBizLessonByLessonIds(any())).thenReturn(1);
        lessons.deleteBizLessonByLessonId(7L);
        doThrow(new IllegalStateException("模拟文件收尾失败")).when(purgeFileService).deleteUnreferenced(anyList());
        assertDoesNotThrow(() -> TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCommit()));
    }
    @Test void normalStudentDeleteCleansAnswersAndUser() {
        student();
        when(bizStudentMapper.deleteBizStudentByStudentIds(any())).thenReturn(1);
        assertEquals(1, students.deleteBizStudentByStudentId(7L));
        verify(purgeMapper).deleteAnswersByStudents(Collections.singletonList(7L));
        verify(purgeMapper).deleteCountyAnswersByStudents(Collections.singletonList(7L));
        verify(userMapper).deleteUserById(70L);
        verifyNoInteractions(answerDeletionGuardService, purgeFileService);
    }
    @Test void studentOutsideManagedClassesDoesNotStartDeleting() {
        assertThrows(ServiceException.class, () -> students.deleteBizStudentByStudentIds(new Long[]{7L}));
        verifyNoInteractions(purgeMapper, userMapper, purgeFileService);
    }
    @Test void wholeClassDeletionUsesTeacherScopeAndSameCleanup() {
        BizStudent student = student();
        when(bizStudentMapper.selectBizStudentList(any())).thenReturn(Collections.singletonList(student));
        when(bizStudentMapper.deleteBizStudentByStudentIds(any())).thenReturn(1);
        assertEquals(1, students.deleteBizStudentByClass("2025", "1", 10L));
        ArgumentCaptor<BizStudent> query = ArgumentCaptor.forClass(BizStudent.class);
        verify(bizStudentMapper).selectBizStudentList(query.capture());
        assertEquals(Long.valueOf(8L), query.getValue().getTeacherUserId());
        assertEquals(Long.valueOf(10L), query.getValue().getDeptId());
        verify(purgeMapper).deleteAnswersByStudents(Collections.singletonList(7L));
    }
    @Test void wholeClassCrossSchoolIsRejectedBeforeQuery() {
        assertThrows(ServiceException.class, () -> students.deleteBizStudentByClass("2025", "1", 99L));
        verifyNoInteractions(bizStudentMapper, purgeMapper);
    }
    @Test void studentRowCountMismatchDoesNotScheduleFileCleanup() {
        student();
        assertThrows(ServiceException.class, () -> students.deleteBizStudentByStudentIds(new Long[]{7L}));
        assertTrue(TransactionSynchronizationManager.getSynchronizations().isEmpty());
        verifyNoInteractions(purgeFileService);
    }
}
