package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExemptionApplicationServiceTest
{
    private final ExemptionApplicationService service = new ExemptionApplicationService();

    @Test
    void twelveOfFifteenIsExactlyQualified()
    {
        Map<String, Object> result = summarize(singleClass(10),
                courses("1", 12, 10, 5, 0, 0), 15);
        Map<String, Object> classMetric = firstClass(result);

        assertEquals(new BigDecimal("80.00"), classMetric.get("usageRate"));
        assertTrue((Boolean) classMetric.get("usageQualified"));
        assertTrue((Boolean) result.get("allClassesQualified"));
    }

    @Test
    void elevenOfFifteenIsNotQualified()
    {
        Map<String, Object> result = summarize(singleClass(10),
                courses("1", 11, 10, 5, 0, 0), 15);
        Map<String, Object> classMetric = firstClass(result);

        assertEquals(new BigDecimal("73.33"), classMetric.get("usageRate"));
        assertFalse((Boolean) classMetric.get("usageQualified"));
    }

    @Test
    void classesAreNeverMergedIntoOneDenominator()
    {
        List<Map<String, Object>> classes = new ArrayList<>();
        List<Map<String, Object>> courses = new ArrayList<>();
        for (int classNo = 1; classNo <= 6; classNo++)
        {
            classes.add(classRow(String.valueOf(classNo), 10));
            int used = classNo == 6 ? 11 : 12;
            courses.addAll(courses(String.valueOf(classNo), used, 10, 5, 0, 0));
        }

        Map<String, Object> result = summarize(classes, courses, 15);

        assertEquals(6, result.get("classCount"));
        assertFalse((Boolean) result.get("allClassesQualified"));
        assertEquals(new BigDecimal("73.33"),
                castClasses(result).get(5).get("usageRate"));
    }

    @Test
    void exactHalfParticipationCountsButBelowHalfDoesNot()
    {
        List<Map<String, Object>> courses = new ArrayList<>();
        courses.add(course("1", 1L, 10, 5, 0, 0));
        courses.add(course("1", 2L, 10, 4, 0, 0));

        Map<String, Object> result = summarize(singleClass(10), courses, 15);
        List<Map<String, Object>> metrics = courseMetrics(result);

        assertTrue((Boolean) metrics.get(0).get("countedAsUsed"));
        assertFalse((Boolean) metrics.get(1).get("countedAsUsed"));
        assertEquals(1, firstClass(result).get("usedLessonCount"));
    }

    @Test
    void zeroStudentDenominatorNeverCountsAsUsed()
    {
        Map<String, Object> result = summarize(singleClass(0),
                courses("1", 1, 0, 0, 0, 0), 15);
        Map<String, Object> course = courseMetrics(result).get(0);

        assertNull(course.get("participationRate"));
        assertFalse((Boolean) course.get("countedAsUsed"));
        assertEquals(0, firstClass(result).get("usedLessonCount"));
    }

    @Test
    void zeroScoreSubmissionBelongsToGradedCountAndEightyPercentQualifies()
    {
        Map<String, Object> result = summarize(singleClass(10),
                courses("1", 1, 10, 5, 5, 4), 15);

        assertEquals(5, result.get("practicalDueCount"));
        assertEquals(4, result.get("practicalGradedCount"));
        assertEquals(new BigDecimal("80.00"), result.get("practicalRate"));
        assertTrue((Boolean) result.get("practicalQualified"));
    }

    @Test
    void noPracticalSubmissionKeepsRateAndQualificationUnknown()
    {
        Map<String, Object> result = summarize(singleClass(10),
                courses("1", 1, 10, 5, 0, 0), 15);

        assertNull(result.get("practicalRate"));
        assertNull(result.get("practicalQualified"));
        assertNull(firstClass(result).get("practicalRate"));
    }

    private Map<String, Object> summarize(List<Map<String, Object>> classes,
                                          List<Map<String, Object>> courses,
                                          int required)
    {
        return service.summarizeMetrics(classes, courses, required);
    }

    private static List<Map<String, Object>> singleClass(int validStudents)
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(classRow("1", validStudents));
        return rows;
    }

    private static Map<String, Object> classRow(String classCode, int validStudents)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("deptId", 100L);
        row.put("entryYear", "2025");
        row.put("classCode", classCode);
        row.put("validStudentCount", validStudents);
        return row;
    }

    private static List<Map<String, Object>> courses(String classCode, int count,
                                                     int validStudents, int participants,
                                                     int due, int graded)
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 1; i <= count; i++)
        {
            rows.add(course(classCode, (long) i, validStudents, participants, due, graded));
        }
        return rows;
    }

    private static Map<String, Object> course(String classCode, Long lessonId,
                                              int validStudents, int participants,
                                              int due, int graded)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("lessonId", lessonId);
        row.put("lessonTitle", "课程" + lessonId);
        row.put("deptId", 100L);
        row.put("entryYear", "2025");
        row.put("classCode", classCode);
        row.put("validStudentCount", validStudents);
        row.put("participantCount", participants);
        row.put("practicalDueCount", due);
        row.put("practicalGradedCount", graded);
        return row;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castClasses(Map<String, Object> result)
    {
        return (List<Map<String, Object>>) result.get("classes");
    }

    private static Map<String, Object> firstClass(Map<String, Object> result)
    {
        return castClasses(result).get(0);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> courseMetrics(Map<String, Object> result)
    {
        return (List<Map<String, Object>>) firstClass(result).get("courses");
    }
}
