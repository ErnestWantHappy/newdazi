package com.ruoyi.business.service;

import java.util.List;
import java.util.Map;
import java.util.Collections;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.BizScoringItem;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.StudentLessonQuestionVo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuideSheetStudentViewServiceTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GuideSheetStudentViewService service = new GuideSheetStudentViewService();

    @Test
    void studentFormRemovesExecutableAndScoringConfiguration() throws Exception
    {
        String source = "{\"widgetList\":[{\"type\":\"input\",\"id\":\"q1\","
                + "\"options\":{\"name\":\"q1\",\"label\":\"问题\",\"onChange\":\"steal()\","
                + "\"remote\":true,\"dataSource\":\"https://evil.test\","
                + "\"scoring\":{\"answer\":\"秘密\",\"score\":10}},"
                + "\"scoring\":{\"answer\":\"秘密\"}}],"
                + "\"formConfig\":{\"labelWidth\":100,\"cssCode\":\"body{}\","
                + "\"functions\":\"function steal(){}\",\"onFormMounted\":\"steal()\"},"
                + "\"_scoringConfig\":{\"q1\":{\"answer\":\"秘密\"}},"
                + "\"_aiProvider\":\"custom\",\"prompt\":\"internal\"}";

        String sanitized = service.sanitizeFormJson(source);

        assertFalse(sanitized.contains("秘密"));
        assertFalse(sanitized.contains("steal"));
        assertFalse(sanitized.contains("evil.test"));
        assertFalse(sanitized.contains("_aiProvider"));
        assertFalse(sanitized.contains("prompt"));
        Map<String, Object> root = objectMapper.readValue(sanitized,
                new TypeReference<Map<String, Object>>() { });
        assertTrue(root.containsKey("widgetList"));
        assertTrue(root.containsKey("formConfig"));
    }

    @Test
    void htmlWidgetKeepsTeachingMarkupButRemovesActiveContent() throws Exception
    {
        String source = "{\"widgetList\":[{\"type\":\"html-text\",\"id\":\"intro\","
                + "\"options\":{\"name\":\"intro\",\"htmlContent\":"
                + "\"<p onclick='steal()'>学习<strong>目标</strong><script>steal()</script>"
                + "<a href='javascript:steal()'>链接</a></p>\"}}],\"formConfig\":{}}";

        String sanitized = service.sanitizeFormJson(source);

        assertTrue(sanitized.contains("学习"));
        assertTrue(sanitized.contains("strong"));
        assertFalse(sanitized.contains("onclick"));
        assertFalse(sanitized.contains("script"));
        assertFalse(sanitized.contains("javascript:"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void unknownComponentIsRenderedAsSafePlaceholder() throws Exception
    {
        String source = "{\"widgetList\":[{\"type\":\"sfc-widget\",\"id\":\"custom1\","
                + "\"options\":{\"name\":\"custom1\",\"label\":\"实验组件\","
                + "\"code\":\"fetch('/secret')\"}}],\"formConfig\":{}}";

        Map<String, Object> root = objectMapper.readValue(service.sanitizeFormJson(source),
                new TypeReference<Map<String, Object>>() { });
        Map<String, Object> widget = (Map<String, Object>) ((List<?>) root.get("widgetList")).get(0);

        assertEquals("static-text", widget.get("type"));
        assertFalse(objectMapper.writeValueAsString(widget).contains("fetch"));
    }

    @Test
    void studentGradingDetailUsesPublicFieldWhitelist() throws Exception
    {
        String source = "[{\"fieldTitle\":\"问题\",\"score\":8,\"maxScore\":10,"
                + "\"desc\":\"基本正确\",\"aiComment\":\"继续努力\",\"tabIndex\":0,"
                + "\"manualGraded\":true,"
                + "\"fieldKey\":\"q1\",\"matchType\":\"auto\",\"correctAnswer\":\"A\","
                + "\"referenceAnswer\":\"秘密\",\"prompt\":\"internal\"}]";

        String sanitized = service.sanitizeGradingDetail(source);

        assertTrue(sanitized.contains("基本正确"));
        assertFalse(sanitized.contains("继续努力"));
        assertFalse(sanitized.contains("aiComment"));
        assertFalse(sanitized.contains("fieldKey"));
        assertFalse(sanitized.contains("matchType"));
        assertFalse(sanitized.contains("manualGraded"));
        assertFalse(sanitized.contains("correctAnswer"));
        assertFalse(sanitized.contains("秘密"));
    }

    @Test
    void dailyLessonQuestionDoesNotExposeAnswerOrScoringItems() throws Exception
    {
        BizLessonQuestionDetailVo source = new BizLessonQuestionDetailVo();
        source.setQuestionId(12L);
        source.setQuestionContent("请选择正确选项");
        source.setQuestionType("choice");
        source.setOptionA("A");
        source.setOptionB("B");
        source.setAnswer("A");
        source.setAnalysis("内部解析");
        source.setScoringItems(Collections.singletonList(new BizScoringItem()));

        List<StudentLessonQuestionVo> result = service.toStudentLessonQuestions(
                Collections.singletonList(source));
        String json = objectMapper.writeValueAsString(result);

        assertTrue(json.contains("请选择正确选项"));
        assertFalse(json.contains("\"answer\""));
        assertFalse(json.contains("scoringItems"));
        assertFalse(json.contains("内部解析"));
    }
}
