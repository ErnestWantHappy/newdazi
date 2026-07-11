package com.ruoyi.business.service;

import com.ruoyi.business.config.GuideSheetProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
