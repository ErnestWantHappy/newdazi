package com.ruoyi.business.service;

import com.ruoyi.business.config.GuideSheetProperties;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GuideSheetGradingServiceTest
{
    @Test
    void shouldGradeExactAndContainsAnswers()
    {
        GuideSheetGradingService service = new GuideSheetGradingService();
        String formJson = "{\"widgetList\":["
                + "{\"name\":\"q1\",\"type\":\"input\",\"options\":{\"label\":\"题一\"},"
                + "\"scoring\":{\"score\":60,\"type\":\"exact\",\"answer\":\"A\"}},"
                + "{\"name\":\"q2\",\"type\":\"input\",\"options\":{\"label\":\"题二\"},"
                + "\"scoring\":{\"score\":40,\"type\":\"contains\",\"answer\":\"关键字\"}}]}";
        String answerJson = "{\"q1\":\"A\",\"q2\":\"包含关键字的回答\"}";

        GuideSheetGradingService.GradingResult result = service.grade(formJson, answerJson, 1L, 1L);

        assertEquals(100, result.totalScore);
        assertEquals(GuideSheetGradingService.STATUS_AUTO, result.gradingStatus);
    }

    @Test
    void shouldGradeVFormFieldsNamedInsideOptions()
    {
        GuideSheetGradingService service = new GuideSheetGradingService();
        String formJson = "{\"widgetList\":[{\"id\":\"bg-home-tab\",\"type\":\"tab\","
                + "\"options\":{\"name\":\"HomeTab\"},\"tabs\":[{\"id\":\"bg-home-pane\","
                + "\"type\":\"tab-pane\",\"options\":{\"name\":\"tab1\"},\"widgetList\":["
                + "{\"id\":\"bg-pre-class-check-1\",\"type\":\"input\","
                + "\"options\":{\"name\":\"bg_preClassCheck_1\",\"label\":\"课前检测\"},"
                + "\"scoring\":{\"score\":40,\"type\":\"exact\",\"answer\":\"循环\"}},"
                + "{\"id\":\"bg-multiple-choice-2\",\"type\":\"checkbox\","
                + "\"options\":{\"name\":\"bg_multipleChoice_2\",\"label\":\"多选题\"},"
                + "\"scoring\":{\"score\":60,\"type\":\"exact\",\"answer\":[\"A\",\"B\"]}}]}]}]}";
        String answerJson = "{\"bg_preClassCheck_1\":\"循环\","
                + "\"bg_multipleChoice_2\":[\"A\",\"B\"]}";

        GuideSheetGradingService.GradingResult result = service.gradePage(
                formJson, answerJson, 9399L, 2L, 0);

        assertEquals(100, result.totalScore);
        assertEquals(100, result.pageScore);
        assertEquals(100, result.pageMaxScore);
        assertEquals(GuideSheetGradingService.STATUS_AUTO, result.gradingStatus);
        assertTrue(result.gradingDetail.contains("\"fieldKey\":\"bg_preClassCheck_1\""));
        assertTrue(result.gradingDetail.contains("\"fieldKey\":\"bg_multipleChoice_2\""));
        assertTrue(result.gradingDetail.contains("\"tabIndex\":0"));
        assertFalse(result.gradingDetail.contains("未作答"));
    }

    @Test
    void widgetScoringWinsWhenRootSnapshotIsStale()
    {
        GuideSheetGradingService service = new GuideSheetGradingService();
        String formJson = "{\"widgetList\":[{\"name\":\"q1\",\"type\":\"radio\","
                + "\"options\":{\"label\":\"单选题\"},"
                + "\"scoring\":{\"score\":20,\"type\":\"exact\",\"answer\":\"B\","
                + "\"explanation\":\"新解析\"}}],"
                + "\"_scoringConfig\":{\"单选题\":{\"score\":5,\"type\":\"exact\","
                + "\"answer\":\"A\",\"explanation\":\"旧解析\"}}}";

        GuideSheetGradingService.GradingResult result = service.grade(
                formJson, "{\"q1\":\"B\"}", 1L, 1L);

        assertEquals(20, result.totalScore);
        assertEquals(GuideSheetGradingService.STATUS_AUTO, result.gradingStatus);
    }

    @Test
    void legacyRootSnapshotStillGradesWhenWidgetHasNoScoring()
    {
        GuideSheetGradingService service = new GuideSheetGradingService();
        String formJson = "{\"widgetList\":[{\"name\":\"legacyQuestion\",\"type\":\"radio\","
                + "\"options\":{\"label\":\"旧模板题\"},\"scoring\":{}}],"
                + "\"_scoringConfig\":{\"旧模板题\":{\"score\":12,\"type\":\"exact\","
                + "\"answer\":\"A\"}}}";

        GuideSheetGradingService.GradingResult result = service.grade(
                formJson, "{\"legacyQuestion\":\"A\"}", 1L, 1L);

        assertEquals(12, result.totalScore);
        assertEquals(GuideSheetGradingService.STATUS_AUTO, result.gradingStatus);
    }

    @Test
    void checkboxWrongSelectionsReducePartialCredit()
    {
        GuideSheetGradingService service = new GuideSheetGradingService();
        String formJson = "{\"widgetList\":[{\"name\":\"q1\",\"type\":\"checkbox\","
                + "\"options\":{\"label\":\"多选题\"},"
                + "\"scoring\":{\"score\":10,\"type\":\"exact\",\"answer\":\"A,B\"}}]}";

        GuideSheetGradingService.GradingResult partial = service.grade(
                formJson, "{\"q1\":[\"A\"]}", 1L, 1L);
        GuideSheetGradingService.GradingResult selectAll = service.grade(
                formJson, "{\"q1\":[\"A\",\"B\",\"C\",\"D\"]}", 1L, 1L);

        assertEquals(5, partial.totalScore);
        assertEquals(0, selectAll.totalScore);
    }

    @Test
    void beginnerTextTaskWaitsForManualReviewInsteadOfAutoFailing()
    {
        GuideSheetGradingService service = new GuideSheetGradingService();
        String formJson = "{\"widgetList\":[{\"name\":\"q1\",\"type\":\"textarea\","
                + "\"options\":{\"label\":\"说说你的理解\"},"
                + "\"scoring\":{\"score\":10,\"type\":\"manual\",\"answer\":\"\"}}]}";

        GuideSheetGradingService.GradingResult result = service.grade(
                formJson, "{\"q1\":\"我理解了循环结构\"}", 1L, 1L);

        assertEquals(0, result.totalScore);
        assertEquals(GuideSheetGradingService.STATUS_MANUAL, result.gradingStatus);
        assertTrue(result.gradingDetail.contains("待人工批改"));
    }

    @Test
    void legacyBeginnerTextSnapshotWithEmptyExactAnswerWaitsForManualReview()
    {
        GuideSheetGradingService service = new GuideSheetGradingService();
        String formJson = "{\"widgetList\":[{\"id\":\"home-tab\",\"type\":\"tab\","
                + "\"tabs\":[{\"id\":\"task-pane\",\"type\":\"tab-pane\",\"widgetList\":["
                + "{\"id\":\"pre-check\",\"type\":\"textarea\",\"options\":{"
                + "\"name\":\"preCheck\",\"label\":\"课前检测\","
                + "\"beginnerModuleType\":\"preClassCheck\"},"
                + "\"scoring\":{\"score\":10,\"type\":\"exact\",\"answer\":\"\"}},"
                + "{\"id\":\"multiple-choice\",\"type\":\"checkbox\",\"options\":{"
                + "\"name\":\"multipleChoice\",\"label\":\"多选题\"},"
                + "\"scoring\":{\"score\":10,\"type\":\"exact\",\"answer\":\"A,B\"}}]}]}]}";

        GuideSheetGradingService.GradingResult result = service.grade(
                formJson, "{\"preCheck\":\"学生回答\",\"multipleChoice\":[\"A\",\"B\"]}",
                9399L, 2L);

        assertEquals(10, result.totalScore);
        assertEquals(GuideSheetGradingService.STATUS_PARTIAL, result.gradingStatus);
        assertTrue(result.gradingDetail.contains("\"fieldKey\":\"preCheck\""));
        assertTrue(result.gradingDetail.contains("待人工批改"));
        assertTrue(result.gradingDetail.contains("\"fieldKey\":\"multipleChoice\""));
        assertTrue(result.gradingDetail.contains("\"score\":10"));
    }

    @Test
    void shouldRejectAiRequestWhenServerSecretIsMissing()
    {
        GuideSheetProperties properties = new GuideSheetProperties();
        AiGradingService service = new AiGradingService(properties);
        try
        {
            assertFalse(service.isConfigured());
            assertThrows(IllegalStateException.class, () -> service.grade("评分", 10));
        }
        finally
        {
            service.shutdown();
        }
    }

    @Test
    void shouldDowngradeAiGradingToManualWhenServerSecretIsMissing()
    {
        GuideSheetProperties properties = new GuideSheetProperties();
        AiGradingService aiService = new AiGradingService(properties);
        GuideSheetGradingService service = new GuideSheetGradingService();
        ReflectionTestUtils.setField(service, "aiGradingService", aiService);
        String formJson = "{\"widgetList\":[{\"name\":\"q1\",\"type\":\"textarea\","
                + "\"options\":{\"label\":\"简答题\"},"
                + "\"scoring\":{\"score\":100,\"type\":\"ai\",\"answer\":\"参考答案\"}}]}";

        try
        {
            GuideSheetGradingService.GradingResult result = service.grade(
                    formJson, "{\"q1\":\"学生答案\"}", 1L, 1L);

            assertEquals(0, result.totalScore);
            assertEquals(GuideSheetGradingService.STATUS_MANUAL, result.gradingStatus);
            assertTrue(result.gradingDetail.contains("已转为人工处理"));
        }
        finally
        {
            aiService.shutdown();
        }
    }

    @Test
    void aiGradingReusesServerChatGateway() throws Exception
    {
        GuideSheetProperties properties = new GuideSheetProperties();
        AiChatGateway gateway = mock(AiChatGateway.class);
        when(gateway.isConfigured()).thenReturn(true);
        when(gateway.chat(anyString(), anyInt(), anyInt()))
                .thenReturn("{\"score\":8,\"comment\":\"思路正确\"}");
        AiGradingService service = new AiGradingService(properties, gateway);
        try
        {
            AiGradingService.AiGradeResult result = service.grade("评分", 10);
            assertEquals(8, result.score);
            assertEquals("思路正确", result.comment);
        }
        finally
        {
            service.shutdown();
        }
    }
}
