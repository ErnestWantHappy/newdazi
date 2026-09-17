package com.ruoyi.business.controller;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 缺考行只做标记展示：分母与排名不受影响。 */
class ScoreQueryControllerTest
{
    @Test
    void missingRowIsMarkedAndExcluded()
    {
        Map<String, Object> row = ScoreQueryController.missingScoreRow(9492L, 34L, "3-人工智能", 1);
        assertTrue(Boolean.TRUE.equals(row.get(ScoreQueryController.SCORE_FLAG_MISSING)));
        assertFalse(Boolean.TRUE.equals(row.get("isAbsent")));
        assertNull(row.get("finalScore"));
        assertEquals(0, row.get("totalScore"));
        assertTrue(ScoreQueryController.isExcludedFromAverage(row));
    }

    @Test
    void absenceStillExcludedAndNormalRowKept()
    {
        Map<String, Object> absent = new HashMap<String, Object>();
        absent.put("isAbsent", true);
        assertTrue(ScoreQueryController.isExcludedFromAverage(absent));

        Map<String, Object> normal = new HashMap<String, Object>();
        normal.put("isAbsent", false);
        normal.put("finalScore", 80);
        assertFalse(ScoreQueryController.isExcludedFromAverage(normal));

        assertTrue(ScoreQueryController.isExcludedFromAverage(null));
    }

    @Test
    void missingOnlyWhenClassHasRecords()
    {
        java.util.Set<Long> known = new java.util.HashSet<Long>(java.util.Arrays.asList(1L, 4L));
        java.util.Set<Long> recorded = new java.util.HashSet<Long>(java.util.Arrays.asList(1L, 3L, 4L, 10L));
        // 本人缺的 3、10 且全班有人上：标缺考；本人有的 1：不标；全班无记录的 2：不标。
        assertTrue(ScoreQueryController.isMissingLesson(3L, known, recorded));
        assertTrue(ScoreQueryController.isMissingLesson(10L, known, recorded));
        assertFalse(ScoreQueryController.isMissingLesson(1L, known, recorded));
        assertFalse(ScoreQueryController.isMissingLesson(2L, known, recorded));
        assertFalse(ScoreQueryController.isMissingLesson(null, known, recorded));
    }
}
