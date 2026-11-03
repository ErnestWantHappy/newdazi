package com.ruoyi.business.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.*;
import com.ruoyi.business.domain.dto.PracticalUploadTicket;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.mapper.*;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import java.util.Collections;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PracticalStudentResubmitTest {
    PracticalArtifactService service = new PracticalArtifactService();
    PracticalArtifactMapper artifacts = mock(PracticalArtifactMapper.class);
    BizStudentAnswerMapper answers = mock(BizStudentAnswerMapper.class);
    BizStudentMapper students = mock(BizStudentMapper.class);
    BizLessonAssignmentMapper assignments = mock(BizLessonAssignmentMapper.class);
    BizScoringDetailMapper details = mock(BizScoringDetailMapper.class);
    ClassroomTaskStateService states = mock(ClassroomTaskStateService.class);
    RedisCache cache = mock(RedisCache.class);
    BizLessonQuestionDetailVo question = new BizLessonQuestionDetailVo();
    BizStudentAnswer old = new BizStudentAnswer();
    BizStudent student = new BizStudent();

    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service, "artifactMapper", artifacts);
        ReflectionTestUtils.setField(service, "studentAnswerMapper", answers);
        ReflectionTestUtils.setField(service, "studentMapper", students);
        ReflectionTestUtils.setField(service, "lessonAssignmentMapper", assignments);
        ReflectionTestUtils.setField(service, "scoringDetailMapper", details);
        ReflectionTestUtils.setField(service, "taskStateService", states);
        ReflectionTestUtils.setField(service, "redisCache", cache);
        ReflectionTestUtils.setField(service, "filePolicyService", new PracticalFilePolicyService());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        PracticalRubricSnapshotService rubrics = mock(PracticalRubricSnapshotService.class);
        ReflectionTestUtils.setField(service, "rubricSnapshotService", rubrics);
        PracticalRubricSnapshot rubric = new PracticalRubricSnapshot(); rubric.setSnapshotId(20L);
        when(rubrics.ensureLatest(anyLong(), any(), anyLong())).thenReturn(rubric);
        student.setStudentId(1L); student.setEntryYear("2026"); student.setClassCode("1"); student.setDeptId(8L);
        when(students.selectBizStudentByStudentId(1L)).thenReturn(student);
        BizLessonAssignment assignment = new BizLessonAssignment(); assignment.setLessonId(2L);
        when(assignments.selectCurrentAssignmentForUpdate("2026", "1", 8L)).thenReturn(assignment);
        question.setQuestionId(3L); question.setQuestionType("practical"); question.setPracticalMode("FILE");
        question.setPracticalAllowedExtensions("pdf");
        PracticalUploadTicket ticket = new PracticalUploadTicket(); ticket.setStudentId(1L);
        ticket.setLessonId(2L); ticket.setQuestionId(3L); ticket.setFileExtension("pdf");
        ticket.setFileKind("PDF"); ticket.setResourcePath("/new.pdf"); ticket.setToken("new");
        when(cache.getCacheObject("student:practical-artifact-ticket:new")).thenReturn(ticket);
        PracticalArtifact artifact = new PracticalArtifact(); artifact.setArtifactId(4L);
        artifact.setCurrentVersionId(5L); artifact.setLatestVersionNo(1); artifact.setLockVersion(0);
        when(artifacts.selectByContextForUpdate("LESSON", 2L, 1L, 3L)).thenReturn(artifact);
        old.setAnswerId(6L); old.setScore(68); old.setStudentAnswer("/old.pdf");
        when(answers.selectLatestByStudentLessonQuestion(1L, 2L, 3L)).thenReturn(old);
        when(answers.selectByIdForUpdate(6L)).thenReturn(old);
        when(artifacts.supersedeVersion(eq(5L), any(), anyString(), any())).thenReturn(1);
        doAnswer(call -> { ((PracticalSubmissionVersion) call.getArgument(0)).setVersionId(7L); return 1; })
                .when(artifacts).insertVersion(any());
        when(artifacts.updateCurrentVersion(eq(4L), eq(7L), eq(2), eq(0), any())).thenReturn(1);
        TransactionSynchronizationManager.initSynchronization();
    }
    @AfterEach void cleanup() { TransactionSynchronizationManager.clearSynchronization(); }
    void submit(Long version) {
        service.submitLessonArtifact(1L, 9L, question, 2L, version, Collections.singletonList("new"));
    }
    @Test void scoredStudentCanReplaceAndOldGradeIsArchivedBeforeNewAnswer() {
        submit(5L);
        org.mockito.InOrder order = inOrder(artifacts, answers, details, states);
        order.verify(artifacts).supersedeVersion(eq(5L), eq(68), anyString(), any());
        order.verify(answers).upsertAnswer(argThat(answer -> answer.getScore() == null
                && "/new.pdf".equals(answer.getStudentAnswer()) && Long.valueOf(7).equals(answer.getPracticalVersionId())));
        order.verify(details).deleteBizScoringDetailByAnswerId(6L);
        order.verify(states).markInTransaction(student, 8L, 2L, 3L, "SUBMITTED");
        assertEquals(68, old.getScore());
        verify(cache, never()).deleteObject(anyString());
    }
    @Test void zeroGradeCanAlsoBeReplaced() {
        old.setScore(0); submit(5L);
        verify(artifacts).supersedeVersion(eq(5L), eq(0), anyString(), any());
        verify(answers).upsertAnswer(argThat(answer -> answer.getScore() == null));
    }
    @Test void expiredUploadCannotTouchOldWorkOrGrade() {
        when(cache.getCacheObject("student:practical-artifact-ticket:new")).thenReturn(null);
        assertThrows(ServiceException.class, () -> submit(5L));
        verify(answers, never()).upsertAnswer(any());
        verify(artifacts, never()).supersedeVersion(any(), any(), anyString(), any());
        assertEquals(68, old.getScore());
    }
    @Test void staleVersionCannotClearTheGrade() {
        assertThrows(ServiceException.class, () -> submit(99L));
        verify(answers, never()).upsertAnswer(any());
        verifyNoInteractions(details, states);
    }
    @Test void oldLessonCannotBeResubmitted() {
        when(assignments.selectCurrentAssignmentForUpdate("2026", "1", 8L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> submit(5L));
        verifyNoInteractions(artifacts, answers, details, states);
    }
    @Test void missingHistoricalVersionCannotSilentlyLoseOldGrade() {
        PracticalArtifact artifact = new PracticalArtifact(); artifact.setArtifactId(4L);
        when(artifacts.selectByContextForUpdate("LESSON", 2L, 1L, 3L)).thenReturn(artifact);
        assertThrows(ServiceException.class, () -> submit(null));
        verify(answers, never()).upsertAnswer(any());
        verify(artifacts, never()).insertVersion(any());
        assertEquals(68, old.getScore());
    }
    @Test void stateFailureEscapesTransactionalBoundaryForRollback() throws Exception {
        doThrow(new ServiceException("状态保存失败")).when(states).markInTransaction(student, 8L, 2L, 3L, "SUBMITTED");
        assertThrows(ServiceException.class, () -> submit(5L));
        Transactional tx = PracticalArtifactService.class.getMethod("submitLessonArtifact", Long.class,
                Long.class, BizLessonQuestionDetailVo.class, Long.class, Long.class, java.util.List.class)
                .getAnnotation(Transactional.class);
        assertTrue(java.util.Arrays.asList(tx.rollbackFor()).contains(Exception.class));
    }
}
