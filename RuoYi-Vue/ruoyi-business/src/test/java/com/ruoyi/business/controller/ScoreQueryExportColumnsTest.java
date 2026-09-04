package com.ruoyi.business.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ScoreQueryExportColumnsTest
{
    @SuppressWarnings("unchecked")
    @Test
    void performanceOnlySelectionDoesNotImplicitlyIncludeLessonDetails()
    {
        ScoreQueryController controller = new ScoreQueryController();

        List<String> columns = ReflectionTestUtils.invokeMethod(
                controller, "parseExportColumns",
                "userName,className,studentNo,studentName,totalPerformance");

        assertTrue(columns.contains("totalPerformance"));
        assertFalse(columns.contains("lessonDetails"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void missingColumnSelectionKeepsLegacyFullExport()
    {
        ScoreQueryController controller = new ScoreQueryController();

        List<String> columns = ReflectionTestUtils.invokeMethod(
                controller, "parseExportColumns", (Object) null);

        assertTrue(columns.contains("lessonDetails"));
        assertTrue(columns.contains("totalPerformance"));
    }
}
