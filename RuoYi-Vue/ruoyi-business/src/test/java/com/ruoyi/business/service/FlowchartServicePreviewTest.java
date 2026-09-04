package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.FlowchartQuestionConfig;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.FlowchartMapper;
import com.ruoyi.common.exception.ServiceException;

class FlowchartServicePreviewTest {
    @Test
    void publicQuestionPreviewShouldOnlyReturnStarterDiagram() {
        FlowchartMapper flowchartMapper = mock(FlowchartMapper.class);
        BizQuestionMapper questionMapper = mock(BizQuestionMapper.class);
        FlowchartService service = newService(flowchartMapper, questionMapper);
        BizQuestion question = flowchartQuestion("Y", 10L);
        FlowchartQuestionConfig config = new FlowchartQuestionConfig();
        config.setStarterJson(singleNodeJson("starter"));
        config.setAnswerJson(singleNodeJson("answer"));
        config.setRulesJson("[{\"ruleId\":\"hidden\"}]");
        config.setSchemaVersion("1.0");
        when(questionMapper.selectBizQuestionByQuestionId(1L)).thenReturn(question);
        when(flowchartMapper.selectQuestionConfig(1L)).thenReturn(config);

        Map<String, Object> preview = service.teacherPreview(1L, 20L, false);

        assertEquals(1L, preview.get("questionId"));
        assertEquals(config.getStarterJson(), preview.get("starterJson"));
        assertEquals(true, preview.get("configReady"));
        assertFalse(preview.containsKey("answerJson"));
        assertFalse(preview.containsKey("rulesJson"));
    }

    @Test
    void previewShouldReportIncompleteConfigWithoutLeakingAnswer() {
        FlowchartMapper flowchartMapper = mock(FlowchartMapper.class);
        BizQuestionMapper questionMapper = mock(BizQuestionMapper.class);
        FlowchartService service = newService(flowchartMapper, questionMapper);
        FlowchartQuestionConfig config = new FlowchartQuestionConfig();
        config.setStarterJson(singleNodeJson("starter"));
        config.setAnswerJson("{\"nodes\":[]}");
        when(questionMapper.selectBizQuestionByQuestionId(1L)).thenReturn(flowchartQuestion("Y", 10L));
        when(flowchartMapper.selectQuestionConfig(1L)).thenReturn(config);

        Map<String, Object> preview = service.teacherPreview(1L, 20L, false);

        assertEquals(false, preview.get("configReady"));
        assertFalse(preview.containsKey("answerJson"));
    }

    @Test
    void privateQuestionPreviewShouldRejectAnotherTeacher() {
        FlowchartMapper flowchartMapper = mock(FlowchartMapper.class);
        BizQuestionMapper questionMapper = mock(BizQuestionMapper.class);
        FlowchartService service = newService(flowchartMapper, questionMapper);
        when(questionMapper.selectBizQuestionByQuestionId(1L)).thenReturn(flowchartQuestion("N", 10L));

        assertThrows(ServiceException.class, () -> service.teacherPreview(1L, 20L, false));
    }

    @Test
    void gradingSubmissionShouldUseCrossSchoolAwareLessonClassAccess() {
        FlowchartMapper flowchartMapper = mock(FlowchartMapper.class);
        BizStudentMapper studentMapper = mock(BizStudentMapper.class);
        GuideSheetAccessService accessService = mock(GuideSheetAccessService.class);
        BizStudent student = new BizStudent();
        student.setStudentId(30L);
        student.setEntryYear("2025");
        student.setClassCode("1");
        when(studentMapper.selectBizStudentByStudentId(30L)).thenReturn(student);
        when(accessService.requireViewableLessonClassDept(372L, "2025", "1")).thenReturn(169L);
        FlowchartService service = new FlowchartService(flowchartMapper, mock(BizQuestionMapper.class), studentMapper,
                mock(BizLessonAssignmentMapper.class), mock(BizLessonQuestionMapper.class),
                mock(BizStudentAnswerMapper.class), accessService, mock(FlowchartDocumentService.class),
                mock(FlowchartStructureCheckService.class));

        assertThrows(ServiceException.class, () -> service.gradingSubmission(372L, 2012L, 30L, 1));

        verify(accessService).requireViewableLessonClassDept(372L, "2025", "1");
        verify(accessService, never()).assertCanViewLessonClass(372L, "2025", "1");
    }

    private FlowchartService newService(FlowchartMapper flowchartMapper, BizQuestionMapper questionMapper) {
        return new FlowchartService(flowchartMapper, questionMapper, mock(BizStudentMapper.class),
                mock(BizLessonAssignmentMapper.class), mock(BizLessonQuestionMapper.class),
                mock(BizStudentAnswerMapper.class), mock(GuideSheetAccessService.class),
                new FlowchartDocumentService(new ObjectMapper()), mock(FlowchartStructureCheckService.class));
    }

    private String singleNodeJson(String id) {
        return "{\"nodes\":[{\"id\":\"" + id
                + "\",\"type\":\"process\",\"x\":100,\"y\":100,\"text\":\"\"}],\"edges\":[]}";
    }

    private BizQuestion flowchartQuestion(String isPublic, Long creatorId) {
        BizQuestion question = new BizQuestion();
        question.setQuestionType("practical");
        question.setPracticalMode("FLOWCHART");
        question.setIsPublic(isPublic);
        question.setCreatorId(creatorId);
        return question;
    }
}
