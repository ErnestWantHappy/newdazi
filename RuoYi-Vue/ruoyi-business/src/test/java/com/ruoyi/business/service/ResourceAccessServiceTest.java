package com.ruoyi.business.service;

import java.util.Collections;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.CountyExamAnswer;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.BizTeacherClassMapper;
import com.ruoyi.business.mapper.CountyExamAnswerMapper;
import com.ruoyi.business.mapper.ResourceAccessMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceAccessServiceTest
{
    @Mock private ResourceAccessMapper resourceAccessMapper;
    @Mock private BizStudentAnswerMapper studentAnswerMapper;
    @Mock private CountyExamAnswerMapper countyAnswerMapper;
    @Mock private BizQuestionMapper questionMapper;
    @Mock private BizStudentMapper studentMapper;
    @Mock private BizTeacherClassMapper teacherClassMapper;
    @Mock private BizLessonMapper lessonMapper;

    @InjectMocks
    private ResourceAccessService service;

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void anonymousResourceReadIsRejected()
    {
        assertThrows(ServiceException.class,
                () -> service.assertCanRead("/profile/upload/2026/07/a.docx"));
    }

    @Test
    void studentMayReadOwnAnswerButNotClassmateAnswer()
    {
        login(20L, 10L);
        String path = "/profile/upload/student-answer/a.docx";
        BizStudentAnswer answer = answer(30L, 5L);
        when(resourceAccessMapper.selectStudentAnswerIdByResource(path)).thenReturn(8L);
        when(studentAnswerMapper.selectById(8L)).thenReturn(answer);
        when(studentMapper.selectBizStudentByUserId(20L)).thenReturn(student(30L, 10L, "2025", "1"));

        assertEquals(path, service.assertCanRead(path));

        when(studentMapper.selectBizStudentByUserId(20L)).thenReturn(student(31L, 10L, "2025", "1"));
        when(studentMapper.selectBizStudentByStudentId(30L)).thenReturn(student(30L, 10L, "2025", "1"));
        BizLesson lesson = new BizLesson();
        lesson.setLessonId(5L);
        lesson.setDeptId(10L);
        when(lessonMapper.selectBizLessonByLessonId(5L)).thenReturn(lesson);

        assertThrows(ServiceException.class, () -> service.assertCanRead(path));
    }

    @Test
    void teacherMayReadOnlyManagedClassAnswer()
    {
        login(40L, 10L);
        String path = "/profile/upload/student-answer/b.docx";
        when(resourceAccessMapper.selectStudentAnswerIdByResource(path)).thenReturn(9L);
        when(studentAnswerMapper.selectById(9L)).thenReturn(answer(30L, 5L));
        when(studentMapper.selectBizStudentByStudentId(30L)).thenReturn(student(30L, 10L, "2025", "2"));
        BizLesson lesson = new BizLesson();
        lesson.setLessonId(5L);
        lesson.setDeptId(10L);
        when(lessonMapper.selectBizLessonByLessonId(5L)).thenReturn(lesson);
        when(teacherClassMapper.checkTeacherClassExists(any())).thenReturn(1);

        assertEquals(path, service.assertCanRead(path));

        when(teacherClassMapper.checkTeacherClassExists(any())).thenReturn(0);
        assertThrows(ServiceException.class, () -> service.assertCanRead(path));
    }

    @Test
    void countyAnswerIsVisibleToAssignedGraderOnly()
    {
        login(50L, 10L);
        String path = "/profile/upload/county-exam/random.docx";
        CountyExamAnswer answer = new CountyExamAnswer();
        answer.setAnswerId(12L);
        answer.setStudentId(30L);
        answer.setGraderId(50L);
        when(resourceAccessMapper.selectCountyAnswerIdByResource(path)).thenReturn(12L);
        when(countyAnswerMapper.selectById(12L)).thenReturn(answer);
        when(resourceAccessMapper.countCountyAnswerForActiveGrader(12L, 50L)).thenReturn(1);

        assertEquals(path, service.assertCanRead(path));

        when(resourceAccessMapper.countCountyAnswerForActiveGrader(12L, 50L)).thenReturn(0);
        assertThrows(ServiceException.class, () -> service.assertCanRead(path));

        answer.setGraderId(51L);
        assertThrows(ServiceException.class, () -> service.assertCanRead(path));
    }

    @Test
    void privateQuestionMaterialRequiresCurrentStudentLesson()
    {
        login(20L, 10L);
        String path = "/profile/upload/question/private.docx";
        BizQuestion question = new BizQuestion();
        question.setQuestionId(7L);
        question.setIsPublic("N");
        question.setCreatorId(40L);
        when(resourceAccessMapper.selectQuestionIdByResource(path)).thenReturn(7L);
        when(questionMapper.selectBizQuestionByQuestionId(7L)).thenReturn(question);
        when(studentMapper.selectBizStudentByUserId(20L)).thenReturn(student(30L, 10L, "2025", "1"));
        when(resourceAccessMapper.countCurrentLessonQuestionForStudent(30L, 7L)).thenReturn(1);

        assertEquals(path, service.assertCanRead(path));

        when(resourceAccessMapper.countCurrentLessonQuestionForStudent(30L, 7L)).thenReturn(0);
        assertThrows(ServiceException.class, () -> service.assertCanRead(path));
    }

    @Test
    void guideSheetFilesCannotBypassDedicatedEndpoint()
    {
        login(40L, 10L);
        assertThrows(ServiceException.class,
                () -> service.assertCanRead("/profile/upload/guide-sheet/7/work.docx"));
    }

    private void login(Long userId, Long deptId)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setDeptId(deptId);
        LoginUser loginUser = new LoginUser(userId, deptId, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    private BizStudentAnswer answer(Long studentId, Long lessonId)
    {
        BizStudentAnswer answer = new BizStudentAnswer();
        answer.setStudentId(studentId);
        answer.setLessonId(lessonId);
        return answer;
    }

    private BizStudent student(Long studentId, Long deptId, String entryYear, String classCode)
    {
        BizStudent student = new BizStudent();
        student.setStudentId(studentId);
        student.setDeptId(deptId);
        student.setEntryYear(entryYear);
        student.setClassCode(classCode);
        return student;
    }
}
