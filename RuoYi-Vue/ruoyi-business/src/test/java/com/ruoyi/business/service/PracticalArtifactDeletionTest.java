package com.ruoyi.business.service;

import com.ruoyi.business.domain.*;
import com.ruoyi.business.mapper.*;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PracticalArtifactDeletionTest {
    @Mock BizStudentMapper studentMapper;
    @Mock BizLessonAssignmentMapper lessonAssignmentMapper;
    @Mock PracticalArtifactMapper artifactMapper;
    @Mock BizStudentAnswerMapper studentAnswerMapper;
    @Mock BizScoringDetailMapper scoringDetailMapper;
    @Mock ClassroomTaskStateService taskStateService;
    @org.mockito.Spy com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    @InjectMocks PracticalArtifactService service;
    BizStudent student;
    BizStudentAnswer answer;
    @BeforeEach void setup() {
        student = new BizStudent(); student.setStudentId(7L); student.setDeptId(3L);
        student.setEntryYear("2025"); student.setClassCode("1");
        when(studentMapper.selectBizStudentByStudentId(7L)).thenReturn(student);
        BizLessonAssignment assignment = new BizLessonAssignment(); assignment.setLessonId(9L);
        when(lessonAssignmentMapper.selectCurrentAssignmentForUpdate("2025", "1", 3L)).thenReturn(assignment);
        PracticalArtifact artifact = new PracticalArtifact(); artifact.setArtifactId(11L);
        artifact.setCurrentVersionId(12L); artifact.setLatestVersionNo(1); artifact.setLockVersion(0);
        when(artifactMapper.selectByContextForUpdate(anyString(), eq(9L), eq(7L), eq(10L))).thenReturn(artifact);
        answer = new BizStudentAnswer(); answer.setAnswerId(13L);
    }
    private void answer() {
        when(studentAnswerMapper.selectLatestByStudentLessonQuestion(7L, 9L, 10L)).thenReturn(answer);
        when(studentAnswerMapper.selectByIdForUpdate(13L)).thenReturn(answer);
    }
    @Test void deletionClearsAnswerAndWithdrawsOnlyThisQuestion() {
        answer();
        when(artifactMapper.updateCurrentVersion(eq(11L), isNull(), eq(1), eq(0), any())).thenReturn(1);
        service.deleteCurrentLessonArtifact(7L, 9L, 10L, 12L);
        verify(studentAnswerMapper).clearPracticalAnswer(13L, 11L);
        verify(taskStateService).withdrawSubmissionInTransaction(student, 9L, 10L);
    }
    @Test void zeroGradeStillRejectsDeletion() {
        answer(); answer.setScore(0);
        assertThrows(ServiceException.class, () -> service.deleteCurrentLessonArtifact(7L, 9L, 10L, 12L));
        verify(studentAnswerMapper, never()).clearPracticalAnswer(anyLong(), anyLong());
        verifyNoInteractions(taskStateService);
    }
    @Test void staleVersionCannotDeleteOrWithdraw() {
        assertThrows(ServiceException.class, () -> service.deleteCurrentLessonArtifact(7L, 9L, 10L, 99L));
        verifyNoInteractions(studentAnswerMapper, taskStateService);
    }
    @Test void stateFailurePropagatesToRollbackOuterTransaction() {
        answer();
        when(artifactMapper.updateCurrentVersion(eq(11L), isNull(), eq(1), eq(0), any())).thenReturn(1);
        doThrow(new ServiceException("状态写入失败")).when(taskStateService).withdrawSubmissionInTransaction(student, 9L, 10L);
        assertThrows(ServiceException.class, () -> service.deleteCurrentLessonArtifact(7L, 9L, 10L, 12L));
    }
}
