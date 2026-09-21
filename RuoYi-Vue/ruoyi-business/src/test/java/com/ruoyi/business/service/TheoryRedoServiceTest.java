package com.ruoyi.business.service;

import java.util.*;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.business.domain.*;
import com.ruoyi.business.domain.dto.TheoryRedoRequest;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.mapper.*;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TheoryRedoServiceTest
{
    @Mock TheoryRedoMapper mapper;
    @Mock BizStudentMapper studentMapper;
    @Mock BizLessonMapper lessonMapper;
    @Mock BizLessonAssignmentMapper assignmentMapper;
    @Mock BizLessonQuestionMapper questionMapper;
    @Mock BizStudentAnswerMapper answerMapper;
    @Mock BizScoreAdjustmentMapper adjustmentMapper;
    @Mock GuideSheetAccessService accessService;
    @Mock ClassroomTaskStateWriter stateWriter;
    @InjectMocks TheoryRedoService service;
    BizStudent student;
    BizLessonAssignment assignment;
    TheoryRedoRequest request;
    BizStudentAnswer theory;

    @BeforeEach void setup()
    {
        LoginUser login = new LoginUser(200L, 9L, new SysUser(), Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login, null, Collections.emptyList()));
        student = new BizStudent(); student.setStudentId(30L); student.setDeptId(9L); student.setEntryYear("2025"); student.setClassCode("2");
        when(mapper.lockStudent(30L)).thenReturn(30L);
        when(studentMapper.selectBizStudentByStudentId(30L)).thenReturn(student);
        when(studentMapper.countManagedScoreStudent(200L, 9L, 30L)).thenReturn(1);
        BizLesson lesson = new BizLesson(); lesson.setDeptId(9L); lesson.setLessonId(5L);
        when(lessonMapper.selectBizLessonByLessonId(5L)).thenReturn(lesson);
        assignment = new BizLessonAssignment(); assignment.setLessonId(5L); assignment.setTheoryOpen(1);
        when(mapper.lockCurrentAssignment(9L,"2025","2")).thenReturn(assignment);
        BizLessonQuestionDetailVo choice = new BizLessonQuestionDetailVo(); choice.setQuestionId(7L); choice.setQuestionType("choice");
        BizLessonQuestionDetailVo typing = new BizLessonQuestionDetailVo(); typing.setQuestionId(8L); typing.setQuestionType("typing");
        when(questionMapper.selectDetailsByLessonId(5L)).thenReturn(Arrays.asList(choice, typing));
        theory = new BizStudentAnswer(); theory.setQuestionId(7L); theory.setStudentAnswer("A"); theory.setScore(80);
        BizStudentAnswer typingAnswer = new BizStudentAnswer(); typingAnswer.setQuestionId(8L);
        when(answerMapper.selectByStudentAndLesson(30L,5L)).thenReturn(Arrays.asList(theory,typingAnswer));
        when(mapper.archive(eq(30L),eq(5L),eq(9L),eq("2025"),eq("2"),eq(200L),eq(0L),anyString())).thenReturn(1);
        request = new TheoryRedoRequest(); request.setLessonId(5L); request.setEntryYear("2025"); request.setClassCode("2");
        request.setStudentRevisions(Collections.singletonMap(30L,0L));
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    @Test void typingResetArchivesAndDeletesOnlyTyping()
    {
        when(mapper.archiveTyping(anyLong(),anyLong(),anyLong(),anyString(),anyString(),anyLong(),anyLong(),anyString())).thenReturn(1);
        assertEquals(1, service.resetTyping(request));
        verify(answerMapper).deleteByStudentLessonAndQuestions(30L,5L,Collections.singletonList(8L));
        verify(stateWriter).markInTransaction(student,9L,5L,8L,"RETURNED");
        verify(mapper,never()).archive(anyLong(),anyLong(),anyLong(),anyString(),anyString(),anyLong(),anyLong(),anyString());
    }

    @Test void typingRoundRejectsOldTeacherAndStudentRequests()
    {
        when(mapper.typingRevision(30L,5L)).thenReturn(4L);
        assertThrows(ServiceException.class, () -> service.resetTyping(request));
        assertThrows(ServiceException.class, () -> service.guardTypingSubmission(30L,5L,0L));
        assertThrows(ServiceException.class, () -> service.guardTypingSubmission(30L,5L,null));
        assertEquals(student, service.guardTypingSubmission(30L,5L,4L));
    }

    @Test void typingPracticeIsAllowedWithExistingAnswerAndClosedTheoryGate()
    {
        assignment.setTheoryOpen(0);
        assertEquals(student, service.guardTypingSubmission(30L,5L,0L));
    }

    @Test void archivesBeforeDeletingOnlyTheoryAndReturnsState()
    {
        assertEquals(1, service.reset(request));
        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        InOrder ordered = inOrder(mapper,answerMapper,stateWriter);
        ordered.verify(mapper).archive(eq(30L),eq(5L),eq(9L),eq("2025"),eq("2"),eq(200L),eq(0L),json.capture());
        ordered.verify(answerMapper).deleteByStudentLessonAndQuestions(30L,5L,Collections.singletonList(7L));
        ordered.verify(stateWriter).markInTransaction(student,9L,5L,7L,"RETURNED");
        assertEquals(80, JSON.parseObject(json.getValue()).getJSONArray("answers").getJSONObject(0).getIntValue("score"));
        assertEquals(1, JSON.parseObject(json.getValue()).getJSONArray("answers").size());
    }
    @Test void rejectsOtherClassBeforeArchive()
    {
        student.setClassCode("3");
        assertThrows(ServiceException.class, () -> service.reset(request));
        verifyNoInteractions(answerMapper, stateWriter);
    }
    @Test void rejectsUnmanagedStudent()
    {
        when(studentMapper.countManagedScoreStudent(200L,9L,30L)).thenReturn(0);
        assertThrows(ServiceException.class, () -> service.reset(request));
        verifyNoInteractions(answerMapper);
    }
    @Test void rejectsOtherSchool()
    {
        student.setDeptId(10L);
        assertThrows(ServiceException.class, () -> service.reset(request));
        verifyNoInteractions(answerMapper);
    }
    @Test void rejectsHistoricalLesson()
    {
        assignment.setLessonId(6L);
        assertThrows(ServiceException.class, () -> service.reset(request));
        verifyNoInteractions(answerMapper);
    }
    @Test void rejectsRepeatedOrStaleTeacherRequest()
    {
        when(mapper.revision(30L,5L)).thenReturn(4L);
        assertThrows(ServiceException.class, () -> service.reset(request));
        verifyNoInteractions(answerMapper);
    }
    @Test void skipsUnsubmittedWithoutCreatingRound()
    {
        when(answerMapper.selectByStudentAndLesson(30L,5L)).thenReturn(Collections.emptyList());
        assertEquals(0,service.reset(request));
        verify(mapper,never()).archive(anyLong(),anyLong(),anyLong(),anyString(),anyString(),anyLong(),anyLong(),anyString());
        verifyNoInteractions(stateWriter);
    }
    @Test void failedArchivePreventsDelete()
    {
        when(mapper.archive(anyLong(),anyLong(),anyLong(),anyString(),anyString(),anyLong(),anyLong(),anyString())).thenReturn(0);
        assertThrows(ServiceException.class, () -> service.reset(request));
        verify(answerMapper,never()).deleteByStudentLessonAndQuestions(anyLong(),anyLong(),anyList());
    }
    @Test void refusesActiveManualScore()
    {
        BizScoreAdjustment adjustment = new BizScoreAdjustment(); adjustment.setActionType("ADJUST"); adjustment.setAdjustedHomeworkScore(90);
        when(adjustmentMapper.selectLatestByStudentAndLesson(30L,5L,9L)).thenReturn(adjustment);
        assertThrows(ServiceException.class, () -> service.reset(request));
        verifyNoInteractions(answerMapper);
    }
    @Test void oldPageAndMissingRevisionCannotSubmitAfterReset()
    {
        when(mapper.revision(30L,5L)).thenReturn(4L);
        assertThrows(ServiceException.class, () -> service.guardSubmission(30L,5L,0L));
        assertThrows(ServiceException.class, () -> service.guardSubmission(30L,5L,null));
        verifyNoInteractions(answerMapper);
    }
    @Test void closedTheoryCannotSubmit()
    {
        assignment.setTheoryOpen(0);
        assertThrows(ServiceException.class, () -> service.guardSubmission(30L,5L,0L));
    }
    @Test void newRevisionCanSubmitButExistingTheoryBlocksPartialReplay()
    {
        when(mapper.revision(30L,5L)).thenReturn(4L);
        assertThrows(ServiceException.class, () -> service.guardSubmission(30L,5L,4L));
        when(answerMapper.selectByStudentAndLesson(30L,5L)).thenReturn(Collections.emptyList());
        assertSame(student,service.guardSubmission(30L,5L,4L));
    }
}
