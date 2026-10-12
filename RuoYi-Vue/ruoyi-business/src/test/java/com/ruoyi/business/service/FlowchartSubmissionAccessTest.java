package com.ruoyi.business.service;

import com.ruoyi.business.domain.*;
import com.ruoyi.business.mapper.*;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlowchartSubmissionAccessTest {
    FlowchartMapper graphs = mock(FlowchartMapper.class);
    BizStudentAnswerMapper answers = mock(BizStudentAnswerMapper.class);
    ClassroomTaskStateService states = mock(ClassroomTaskStateService.class);
    BizStudentAnswer answer = new BizStudentAnswer();
    FlowchartDraft draft = new FlowchartDraft();
    FlowchartService service;

    @BeforeEach void setup() {
        BizStudentMapper students = mock(BizStudentMapper.class);
        BizLessonAssignmentMapper assignments = mock(BizLessonAssignmentMapper.class);
        BizQuestionMapper questions = mock(BizQuestionMapper.class);
        BizLessonQuestionMapper lessonQuestions = mock(BizLessonQuestionMapper.class);
        BizStudent student = new BizStudent(); student.setStudentId(1L); student.setDeptId(2L);
        student.setEntryYear("2025"); student.setClassCode("1");
        when(students.selectBizStudentByUserId(9L)).thenReturn(student);
        BizLessonAssignment assignment = new BizLessonAssignment(); assignment.setLessonId(3L); assignment.setPracticalOpen(1);
        when(assignments.selectCurrentAssignmentForUpdate("2025", "1", 2L)).thenReturn(assignment);
        when(assignments.selectBizLessonAssignmentList(any())).thenReturn(Collections.singletonList(assignment));
        when(lessonQuestions.selectQuestionIdsByLessonId(3L)).thenReturn(Collections.singletonList(4L));
        BizQuestion question = new BizQuestion(); question.setQuestionType("practical"); question.setPracticalMode("FLOWCHART");
        when(questions.selectBizQuestionByQuestionId(4L)).thenReturn(question);
        answer.setAnswerId(5L); when(answers.selectLatestByStudentLessonQuestion(1L,3L,4L)).thenReturn(answer);
        when(answers.selectByIdForUpdate(5L)).thenReturn(answer);
        draft.setRevision(7); draft.setDocumentJson("old-draft");
        when(graphs.selectDraft(1L,3L,4L)).thenReturn(draft);
        FlowchartSubmission submitted = new FlowchartSubmission(); submitted.setVersionNo(2);
        when(graphs.selectLatestSubmission(1L,3L,4L)).thenReturn(submitted);
        when(graphs.selectLessonSnapshot(3L,4L)).thenReturn(new FlowchartLessonSnapshot());
        service = new FlowchartService(graphs,questions,students,assignments,lessonQuestions,answers,
                mock(GuideSheetAccessService.class),mock(FlowchartDocumentService.class),
                mock(FlowchartStructureCheckService.class),states);
    }
    @Test void submittedUngradedWorkspaceStaysEditableWithoutResettingTheDraft() {
        Map<String,Object> workspace = service.studentWorkspace(9L,2L,3L,4L);
        assertEquals(false,workspace.get("readOnly"));
        assertEquals("old-draft", ((FlowchartDraft)workspace.get("draft")).getDocumentJson());
        verify(graphs).alignDraftBase(1L,3L,4L,2);
        verify(graphs,never()).replaceDraftFromSubmission(any());
    }
    @Test void zeroGradeMakesWorkspaceReadOnlyAndRejectsAllWrites() {
        answer.setScore(0);
        assertEquals(true,service.studentWorkspace(9L,2L,3L,4L).get("readOnly"));
        assertThrows(ServiceException.class, () -> service.saveDraft(9L,2L,3L,4L,7,"new"));
        assertThrows(ServiceException.class, () -> service.submit(9L,2L,3L,4L,7));
        assertThrows(ServiceException.class, () -> service.reopen(9L,2L,3L,4L));
        verify(graphs,never()).insertSubmission(any());
    }
    @Test void returnedGradeIsEditableAndPreservesOldScore() {
        answer.setScore(10); when(states.isReturnedForUpdate(1L,3L,4L)).thenReturn(true);
        assertEquals(false,service.studentWorkspace(9L,2L,3L,4L).get("readOnly"));
        assertEquals(10,answer.getScore());
    }
}
