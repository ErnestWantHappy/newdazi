package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizScoringItem;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.CountyExam;
import com.ruoyi.business.domain.CountyExamClass;
import com.ruoyi.business.domain.CountyExamGrader;
import com.ruoyi.business.domain.CountyExamStudent;
import com.ruoyi.business.domain.dto.CountyExamGradeRequest;
import com.ruoyi.business.domain.dto.CountyExamSubmitRequest;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.CountyExamScoringItemVo;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.CountyExamAnswerMapper;
import com.ruoyi.business.mapper.CountyExamClassMapper;
import com.ruoyi.business.mapper.CountyExamMapper;
import com.ruoyi.business.mapper.CountyExamPaperQuestionMapper;
import com.ruoyi.business.mapper.CountyExamQuestionMapper;
import com.ruoyi.business.mapper.CountyExamStudentMapper;
import com.ruoyi.business.service.ICountyExamService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CountyExamServiceImplTest
{
    @Mock
    private CountyExamMapper countyExamMapper;

    @Mock
    private CountyExamQuestionMapper questionMapper;

    @Mock
    private CountyExamClassMapper classMapper;

    @Mock
    private CountyExamStudentMapper studentMapper;

    @Mock
    private CountyExamPaperQuestionMapper paperQuestionMapper;

    @Mock
    private CountyExamAnswerMapper answerMapper;

    @Mock
    private BizStudentMapper bizStudentMapper;

    @InjectMocks
    private CountyExamServiceImpl service;

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldKeepTypingPercentagesInsideDatabaseRange()
    {
        assertEquals(100D, CountyExamServiceImpl.normalizePercentage(100D));
        assertEquals(100D, CountyExamServiceImpl.normalizePercentage(125.5D));
        assertEquals(0D, CountyExamServiceImpl.normalizePercentage(-1D));
    }

    @Test
    void shouldRequireEveryPracticalQuestionToHaveAGrader()
    {
        CountyExamGrader grader = grader(101L, 900L);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.validatePracticalGraderCoverage(
                        7L, Arrays.asList(question(101L, "practical"), question(102L, "practical")),
                        Collections.singletonList(grader)));

        assertTrue(error.getMessage().contains("102"));
    }

    @Test
    void shouldAllowSameTeacherToGradeMultiplePracticalQuestions()
    {
        assertDoesNotThrow(() -> service.validatePracticalGraderCoverage(
                7L,
                Arrays.asList(question(101L, "practical"), question(102L, "practical")),
                Arrays.asList(grader(101L, 900L), grader(102L, 900L))));
    }

    @Test
    void shouldValidateAssignedPracticalUploadAndReturnStudentId()
    {
        prepareStudentContext("0");
        when(paperQuestionMapper.countByExamAndStudent(7L, 20L)).thenReturn(1);
        when(paperQuestionMapper.countPracticalByExamStudentQuestion(7L, 20L, 101L)).thenReturn(1);

        assertEquals(20L, service.validateStudentWorkUpload(7L, 101L));
    }

    @Test
    void shouldRejectUploadAfterFinalSubmission()
    {
        prepareStudentContext("1");

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.validateStudentWorkUpload(7L, 101L));

        assertTrue(error.getMessage().contains("最终提交"));
    }

    @Test
    void shouldRejectUploadOutsideStudentsPracticalPaper()
    {
        prepareStudentContext("0");
        when(paperQuestionMapper.countByExamAndStudent(7L, 20L)).thenReturn(1);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.validateStudentWorkUpload(7L, 999L));

        assertTrue(error.getMessage().contains("本人区域抽测试卷"));
    }

    @Test
    void shouldOnlyAcceptWorkPathFromCurrentStudentQuestionScope()
    {
        String ownPath = "/profile/upload/county-exam/7/20/101/2026/07/11/abcdef.docx";

        assertTrue(service.isStudentWorkPath(ownPath, 7L, 20L, 101L));
        assertFalse(service.isStudentWorkPath(ownPath, 7L, 21L, 101L));
        assertFalse(service.isStudentWorkPath(
                "/profile/upload/county-exam/7/20/101/../../other.docx", 7L, 20L, 101L));
    }

    @Test
    void shouldRejectDraftAfterLockedAttemptWasSubmitted()
    {
        prepareStudentContext("1");
        CountyExamSubmitRequest request = new CountyExamSubmitRequest();
        request.setExamId(7L);
        request.setAnswers(Collections.singletonMap(101L, "A"));

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.saveStudentDraft(request));

        assertTrue(error.getMessage().contains("最终提交"));
        verify(studentMapper).selectByExamAndStudentForUpdate(7L, 20L);
        verifyNoInteractions(answerMapper);
    }

    @Test
    void shouldRollbackTransactionWhenFinalSubmitCasFails()
    {
        prepareStudentContext("0");
        when(paperQuestionMapper.countByExamAndStudent(7L, 20L)).thenReturn(1);
        BizLessonQuestionDetailVo question = question(101L, "choice");
        question.setQuestionScore(10L);
        question.setAnswer("A");
        when(paperQuestionMapper.selectDetailsByExamAndStudent(7L, 20L))
                .thenReturn(Collections.singletonList(question));
        when(answerMapper.selectByExamAndStudent(7L, 20L)).thenReturn(Collections.emptyList());
        when(studentMapper.markSubmittedIfOpen(eq(30L), any(Date.class), eq("0"))).thenReturn(0);

        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        ProxyFactory proxyFactory = new ProxyFactory(service);
        proxyFactory.addAdvice(new TransactionInterceptor(
                transactionManager, new AnnotationTransactionAttributeSource()));
        ICountyExamService transactionalService = (ICountyExamService) proxyFactory.getProxy();

        CountyExamSubmitRequest request = new CountyExamSubmitRequest();
        request.setExamId(7L);
        request.setAnswers(Collections.singletonMap(101L, "A"));

        ServiceException error = assertThrows(ServiceException.class,
                () -> transactionalService.submitStudentExam(request));

        assertTrue(error.getMessage().contains("提交状态已变化"));
        verify(answerMapper).insert(any());
        verify(transactionManager).rollback(transactionStatus);
        verify(transactionManager, never()).commit(transactionStatus);
    }

    @Test
    void shouldScaleCountyScoringWeightsToQuestionScore()
    {
        List<CountyExamScoringItemVo> items = CountyExamServiceImpl.buildCountyScoringItems(
                Arrays.asList(scoringItem(1L, 40), scoringItem(2L, 60)), 80);

        assertEquals(32, items.get(0).getMaxScore());
        assertEquals(48, items.get(1).getMaxScore());
        assertEquals(80, items.stream().mapToInt(CountyExamScoringItemVo::getMaxScore).sum());
    }

    @Test
    void shouldKeepScaledItemTotalExactAtRoundingBoundary()
    {
        List<CountyExamScoringItemVo> items = CountyExamServiceImpl.buildCountyScoringItems(
                Arrays.asList(scoringItem(1L, 50), scoringItem(2L, 50)), 1);

        assertEquals(1, items.stream().mapToInt(CountyExamScoringItemVo::getMaxScore).sum());
    }

    @Test
    void shouldRejectScoringDetailAboveScaledMaximum()
    {
        CountyExamScoringItemVo item = new CountyExamScoringItemVo();
        item.setItemId(1L);
        item.setMaxScore(80);
        CountyExamGradeRequest.ScoringDetailRequest detail = new CountyExamGradeRequest.ScoringDetailRequest();
        detail.setItemId(1L);
        detail.setScore(100);
        CountyExamGradeRequest request = new CountyExamGradeRequest();
        request.setScore(80);
        request.setScoringDetails(Collections.singletonList(detail));

        assertThrows(ServiceException.class,
                () -> CountyExamServiceImpl.validateCountyScoringDetails(
                        request, Collections.singletonList(item)));
    }

    private void prepareStudentContext(String status)
    {
        SysUser user = new SysUser();
        user.setUserName("student");
        LoginUser loginUser = new LoginUser(10L, 104L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));

        BizStudent student = new BizStudent();
        student.setStudentId(20L);
        student.setEntryYear("2025");
        student.setClassCode("1");
        when(bizStudentMapper.selectBizStudentByUserId(10L)).thenReturn(student);

        CountyExam exam = new CountyExam();
        exam.setExamId(7L);
        exam.setStatus("1");
        exam.setDurationMinutes(40);
        when(countyExamMapper.selectCountyExamById(7L)).thenReturn(exam);

        CountyExamClass examClass = new CountyExamClass();
        examClass.setExamId(7L);
        examClass.setDeptId(104L);
        examClass.setEntryYear("2025");
        examClass.setClassCode("1");
        when(classMapper.selectActiveByStudentInfo(104L, "2025", "1"))
                .thenReturn(Collections.singletonList(examClass));

        CountyExamStudent examStudent = new CountyExamStudent();
        examStudent.setId(30L);
        examStudent.setExamId(7L);
        examStudent.setStudentId(20L);
        examStudent.setStatus(status);
        examStudent.setStartTime(new Date());
        examStudent.setDeadlineTime(new Date(System.currentTimeMillis() + 60000L));
        when(studentMapper.selectByExamAndStudent(7L, 20L)).thenReturn(examStudent);
        when(studentMapper.selectByExamAndStudentForUpdate(7L, 20L)).thenReturn(examStudent);
    }

    private BizLessonQuestionDetailVo question(Long questionId, String type)
    {
        BizLessonQuestionDetailVo question = new BizLessonQuestionDetailVo();
        question.setQuestionId(questionId);
        question.setQuestionType(type);
        return question;
    }

    private CountyExamGrader grader(Long questionId, Long graderId)
    {
        CountyExamGrader grader = new CountyExamGrader();
        grader.setQuestionId(questionId);
        grader.setGraderId(graderId);
        return grader;
    }

    private BizScoringItem scoringItem(Long itemId, int weightPercent)
    {
        BizScoringItem item = new BizScoringItem();
        item.setItemId(itemId);
        item.setQuestionId(101L);
        item.setItemName("评分项" + itemId);
        item.setItemScore(weightPercent);
        item.setOrderNum(itemId.intValue());
        return item;
    }
}
