package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PythonPracticeRecommendationTest {
    @SuppressWarnings("unchecked")
    @Test
    void countyDepartmentWithoutSchoolTypeKeepsOriginalClassLabels() {
        List<Map<String, Object>> classes = new ArrayList<Map<String, Object>>();
        classes.add(classRow("2024", "2"));
        classes.add(classRow("2025", "7"));

        List<Map<String, Object>> decorated = ReflectionTestUtils.invokeMethod(
            new PythonPracticeService(), "decorateCurrentClasses", classes, null);

        assertEquals(2, decorated.size());
        assertEquals("2025级7班", decorated.get(0).get("class_label"));
        assertEquals("2024级2班", decorated.get(1).get("class_label"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void recommendationBalancesDifficultyAndNeverDuplicatesQuestions() {
        List<Map<String, Object>> candidates = new ArrayList<Map<String, Object>>();
        add(candidates, "SIMPLE", 8, 1L);
        add(candidates, "MEDIUM", 7, 101L);
        add(candidates, "HARD", 3, 201L);

        List<Map<String, Object>> selected = ReflectionTestUtils.invokeMethod(
            PythonPracticeService.class, "selectRecommended", candidates, 12);

        assertEquals(12, selected.size());
        assertEquals(6, count(selected, "SIMPLE"));
        assertEquals(5, count(selected, "MEDIUM"));
        assertEquals(1, count(selected, "HARD"));
        Set<Object> ids = new HashSet<Object>();
        for (Map<String, Object> item : selected) ids.add(item.get("question_id"));
        assertEquals(12, ids.size());
    }

    private static void add(List<Map<String, Object>> rows, String difficulty, int count, long start) {
        for (int index = 0; index < count; index++) {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("question_id", start + index);
            row.put("difficulty", difficulty);
            row.put("knowledge_points", "知识点" + (index % 6));
            rows.add(row);
        }
    }

    private static Map<String, Object> classRow(String entryYear, String classCode) {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("entry_year", entryYear);
        row.put("class_code", classCode);
        return row;
    }

    private static int count(List<Map<String, Object>> rows, String difficulty) {
        int result = 0;
        for (Map<String, Object> row : rows) if (difficulty.equals(row.get("difficulty"))) result++;
        return result;
    }
}
