package com.ruoyi.business.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 导学单评分引擎单测（对齐 DigitalGuide：AI 配置来自 formJson，非服务端密钥）。
 */
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
    void shouldDowngradeAiGradingToManualWhenApiKeyMissing()
    {
        AiGradingService aiService = new AiGradingService();
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
            assertTrue(result.gradingDetail.contains("AI评分失败")
                    || result.gradingDetail.contains("未配置API Key"));
        }
        finally
        {
            aiService.shutdown();
        }
    }
}
