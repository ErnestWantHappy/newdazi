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

    private static int count(List<Map<String, Object>> rows, String difficulty) {
        int result = 0;
        for (Map<String, Object> row : rows) if (difficulty.equals(row.get("difficulty"))) result++;
        return result;
    }
}
