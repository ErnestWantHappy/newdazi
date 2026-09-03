package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.ruoyi.business.domain.BizQuestion;
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
        config.setStarterJson("{\"nodes\":[{\"id\":\"starter\"}]}");
        config.setAnswerJson("{\"nodes\":[{\"id\":\"answer\"}]}");
        config.setRulesJson("[{\"ruleId\":\"hidden\"}]");
        config.setSchemaVersion("1.0");
        when(questionMapper.selectBizQuestionByQuestionId(1L)).thenReturn(question);
        when(flowchartMapper.selectQuestionConfig(1L)).thenReturn(config);

        Map<String, Object> preview = service.teacherPreview(1L, 20L, false);

        assertEquals(1L, preview.get("questionId"));
        assertEquals(config.getStarterJson(), preview.get("starterJson"));
        assertFalse(preview.containsKey("answerJson"));
        assertFalse(preview.containsKey("rulesJson"));
    }

    @Test
    void privateQuestionPreviewShouldRejectAnotherTeacher() {
        FlowchartMapper flowchartMapper = mock(FlowchartMapper.class);
        BizQuestionMapper questionMapper = mock(BizQuestionMapper.class);
        FlowchartService service = newService(flowchartMapper, questionMapper);
        when(questionMapper.selectBizQuestionByQuestionId(1L)).thenReturn(flowchartQuestion("N", 10L));

        assertThrows(ServiceException.class, () -> service.teacherPreview(1L, 20L, false));
    }

    private FlowchartService newService(FlowchartMapper flowchartMapper, BizQuestionMapper questionMapper) {
        return new FlowchartService(flowchartMapper, questionMapper, mock(BizStudentMapper.class),
                mock(BizLessonAssignmentMapper.class), mock(BizLessonQuestionMapper.class),
                mock(BizStudentAnswerMapper.class), mock(GuideSheetAccessService.class),
                mock(FlowchartDocumentService.class), mock(FlowchartStructureCheckService.class));
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
