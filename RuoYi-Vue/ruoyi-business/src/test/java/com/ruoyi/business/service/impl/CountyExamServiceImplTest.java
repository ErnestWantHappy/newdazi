package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizScoringItem;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.CountyExam;
import com.ruoyi.business.domain.CountyExamClass;
import com.ruoyi.business.domain.CountyExamGrader;
import com.ruoyi.business.domain.CountyExamQuestion;
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
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
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

    @Mock
    private RedisCache redisCache;

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
        String randomPath = "/profile/upload/county-exam/7/101/random/2026/07/11/abcdef.docx";

        assertTrue(service.isStudentWorkPath(ownPath, 7L, 20L, 101L));
        assertFalse(service.isStudentWorkPath(ownPath, 7L, 21L, 101L));
        when(redisCache.getCacheObject("student:county-exam-upload-owner:" + randomPath)).thenReturn(20L);
        assertTrue(service.isStudentWorkPath(randomPath, 7L, 20L, 101L));
        assertFalse(service.isStudentWorkPath(randomPath, 7L, 21L, 101L));
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

    @Test
    void shouldCalculateRandomPaperScoreFromActualDrawCount()
    {
        CountyExam exam = exam("0");
        exam.setShuffleMode(2);
        exam.setRandomChoiceCount(2);
        exam.setRandomJudgmentCount(1);

        int total = CountyExamServiceImpl.calculateEffectiveTotalScore(exam, Arrays.asList(
                scoredQuestion(1L, "typing", 20),
                scoredQuestion(2L, "practical", 55),
                scoredQuestion(3L, "choice", 10),
                scoredQuestion(4L, "choice", 10),
                scoredQuestion(5L, "choice", 10),
                scoredQuestion(6L, "judgment", 5),
                scoredQuestion(7L, "judgment", 5)));

        assertEquals(100, total);
    }

    @Test
    void saveQuestionsShouldPersistEffectiveRandomPaperTotal()
    {
        loginResearcher(90L);
        CountyExam exam = exam("0");
        exam.setShuffleMode(2);
        exam.setRandomChoiceCount(2);
        exam.setRandomJudgmentCount(1);
        when(countyExamMapper.selectCountyExamByIdForUpdate(7L)).thenReturn(exam);
        when(questionMapper.batchInsert(any())).thenReturn(7);
        when(questionMapper.selectDetailsByExamId(7L)).thenReturn(Arrays.asList(
                scoredQuestion(1L, "typing", 20),
                scoredQuestion(2L, "practical", 55),
                scoredQuestion(3L, "choice", 10),
                scoredQuestion(4L, "choice", 10),
                scoredQuestion(5L, "choice", 10),
                scoredQuestion(6L, "judgment", 5),
                scoredQuestion(7L, "judgment", 5)));
        List<CountyExamQuestion> payload = Arrays.asList(
                paperQuestion(1L, 20), paperQuestion(2L, 55),
                paperQuestion(3L, 10), paperQuestion(4L, 10), paperQuestion(5L, 10),
                paperQuestion(6L, 5), paperQuestion(7L, 5));

        assertEquals(7, service.saveQuestions(7L, payload));

        ArgumentCaptor<CountyExam> captor = ArgumentCaptor.forClass(CountyExam.class);
        verify(countyExamMapper).updateCountyExam(captor.capture());
        assertEquals(100, captor.getValue().getTotalScore());
    }

    @Test
    void shouldRejectDifferentScoresInsideRandomQuestionType()
    {
        CountyExam exam = exam("0");
        exam.setShuffleMode(2);
        exam.setRandomChoiceCount(1);

        ServiceException error = assertThrows(ServiceException.class,
                () -> CountyExamServiceImpl.calculateEffectiveTotalScore(exam, Arrays.asList(
                        scoredQuestion(1L, "choice", 10),
                        scoredQuestion(2L, "choice", 20))));

        assertTrue(error.getMessage().contains("分值必须一致"));
    }

    @Test
    void shouldSwitchAcademicYearOnJulyTwentieth()
    {
        Calendar before = Calendar.getInstance();
        before.set(2026, Calendar.JULY, 19);
        Calendar onCutoff = Calendar.getInstance();
        onCutoff.set(2026, Calendar.JULY, 20);

        assertEquals(2025, CountyExamServiceImpl.resolveAcademicStartYear(before));
        assertEquals(2026, CountyExamServiceImpl.resolveAcademicStartYear(onCutoff));
    }

    @Test
    void shouldIgnoreProtectedStateFieldsWhenCreatingExam()
    {
        loginResearcher(90L);
        CountyExam input = exam("3");
        input.setGradingEnabled("1");
        input.setTotalScore(999);
        input.setOpenTime(new Date());
        input.setCloseTime(new Date());
        input.setPublishTime(new Date());
        when(countyExamMapper.insertCountyExam(any())).thenReturn(1);

        assertEquals(1, service.insertCountyExam(input));

        assertEquals("0", input.getStatus());
        assertEquals("0", input.getGradingEnabled());
        assertEquals(0, input.getTotalScore());
        assertEquals(null, input.getOpenTime());
        assertEquals(null, input.getCloseTime());
        assertEquals(null, input.getPublishTime());
    }

    @Test
    void shouldUpdateOnlyEditableDraftFields()
    {
        loginResearcher(90L);
        CountyExam saved = exam("0");
        saved.setExamId(7L);
        when(countyExamMapper.selectCountyExamById(7L)).thenReturn(saved);
        when(countyExamMapper.updateDraftFields(any())).thenReturn(1);
        CountyExam request = new CountyExam();
        request.setExamId(7L);
        request.setStatus("3");
        request.setGradingEnabled("1");
        request.setTotalScore(999);
        request.setShuffleMode(2);
        request.setRandomChoiceCount(2);
        request.setRandomJudgmentCount(1);

        service.updateCountyExam(request);

        ArgumentCaptor<CountyExam> captor = ArgumentCaptor.forClass(CountyExam.class);
        verify(countyExamMapper).updateDraftFields(captor.capture());
        CountyExam update = captor.getValue();
        assertEquals(null, update.getStatus());
        assertEquals(null, update.getGradingEnabled());
        assertEquals(null, update.getTotalScore());
        assertEquals(2, update.getRandomChoiceCount());
        assertEquals(1, update.getRandomJudgmentCount());
    }

    @Test
    void shouldRejectGradingAfterExamWasPublishedUnderLock()
    {
        login(50L, 10L, "teacher");
        com.ruoyi.business.domain.CountyExamAnswer answer =
                new com.ruoyi.business.domain.CountyExamAnswer();
        answer.setAnswerId(12L);
        answer.setExamId(7L);
        answer.setStudentId(20L);
        answer.setQuestionId(101L);
        answer.setGraderId(50L);
        when(answerMapper.selectById(12L)).thenReturn(answer);
        when(countyExamMapper.selectCountyExamByIdForUpdate(7L)).thenReturn(exam("3"));
        CountyExamGradeRequest request = new CountyExamGradeRequest();
        request.setAnswerId(12L);
        request.setScore(10);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.gradeAnswer(request));

        assertTrue(error.getMessage().contains("未开放评卷"));
        verify(answerMapper, never()).updateGrade(any(), any(), any());
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

    private void loginResearcher(Long userId)
    {
        SysRole role = new SysRole();
        role.setRoleKey("researcher");
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setUserName("researcher");
        user.setRoles(Collections.singletonList(role));
        LoginUser loginUser = new LoginUser(userId, 100L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    private void login(Long userId, Long deptId, String username)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setDeptId(deptId);
        user.setUserName(username);
        user.setRoles(Collections.emptyList());
        LoginUser loginUser = new LoginUser(userId, deptId, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    private CountyExam exam(String status)
    {
        CountyExam exam = new CountyExam();
        exam.setExamId(7L);
        exam.setExamName("区域抽测");
        exam.setSchoolType("1");
        exam.setExamGrade(6);
        exam.setStatus(status);
        exam.setGradingEnabled("0");
        exam.setShuffleMode(0);
        exam.setRandomChoiceCount(0);
        exam.setRandomJudgmentCount(0);
        exam.setDurationMinutes(40);
        return exam;
    }

    private BizLessonQuestionDetailVo scoredQuestion(Long questionId, String type, int score)
    {
        BizLessonQuestionDetailVo question = question(questionId, type);
        question.setQuestionScore((long) score);
        return question;
    }

    private CountyExamQuestion paperQuestion(Long questionId, int score)
    {
        CountyExamQuestion question = new CountyExamQuestion();
        question.setQuestionId(questionId);
        question.setQuestionScore(score);
        return question;
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
