package com.ruoyi.business.controller;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ruoyi.business.domain.query.TeachingSupervisionQuery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeachingSupervisionControllerTest
{
    private static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");

    private final TeachingSupervisionController controller = new TeachingSupervisionController();

    @Test
    void defaultAcademicYearSwitchesOnJulyTwentieth()
    {
        TeachingSupervisionQuery before = new TeachingSupervisionQuery();
        controller.normalizePeriod(before, LocalDate.of(2026, 7, 19));
        assertEquals("2025", before.getAcademicYear());
        assertEquals(atStartOfDay(2025, 7, 20), before.getStartTime());
        assertEquals(atStartOfDay(2026, 7, 20), before.getEndTime());

        TeachingSupervisionQuery after = new TeachingSupervisionQuery();
        controller.normalizePeriod(after, LocalDate.of(2026, 7, 20));
        assertEquals("2026", after.getAcademicYear());
        assertEquals(atStartOfDay(2026, 7, 20), after.getStartTime());
        assertEquals(atStartOfDay(2027, 7, 20), after.getEndTime());
    }

    @Test
    void semesterRangesStayInsidePlatformAcademicYear()
    {
        TeachingSupervisionQuery first = query("2026", "1");
        controller.normalizePeriod(first, LocalDate.of(2026, 8, 1));
        assertEquals(atStartOfDay(2026, 7, 20), first.getStartTime());
        assertEquals(atStartOfDay(2027, 2, 1), first.getEndTime());

        TeachingSupervisionQuery second = query("2026", "2");
        controller.normalizePeriod(second, LocalDate.of(2026, 8, 1));
        assertEquals(atStartOfDay(2027, 2, 1), second.getStartTime());
        assertEquals(atStartOfDay(2027, 7, 20), second.getEndTime());
    }

    @Test
    void explicitUsageDateRangeIsInclusiveByNaturalDay()
    {
        TeachingSupervisionQuery query = query("2026", "1");
        query.setUsageStartDate("2026-08-01");
        query.setUsageEndDate("2026-08-31");

        controller.normalizePeriod(query, LocalDate.of(2026, 8, 1));

        assertTrue(query.getUsageDateFiltered());
        assertEquals(atStartOfDay(2026, 8, 1), query.getActivityStartTime());
        assertEquals(atStartOfDay(2026, 9, 1), query.getActivityEndTime());
    }

    @Test
    void usageDateRangeCannotEscapeSelectedSemester()
    {
        TeachingSupervisionQuery query = query("2026", "1");
        query.setUsageStartDate("2026-07-19");
        query.setUsageEndDate("2026-08-01");

        assertThrows(com.ruoyi.common.exception.ServiceException.class,
                () -> controller.normalizePeriod(query, LocalDate.of(2026, 8, 1)));
    }

    @Test
    void exportMethodsRequireBothRoleAndPermission() throws Exception
    {
        assertRoleAndPermission("exportSchools");
        assertRoleAndPermission("exportCourses");
        assertRoleAndPermission("exportStudents");
    }

    private void assertRoleAndPermission(String methodName) throws Exception
    {
        Method method = java.util.Arrays.stream(TeachingSupervisionController.class.getDeclaredMethods())
                .filter(item -> item.getName().equals(methodName))
                .findFirst()
                .orElseThrow(NoSuchMethodException::new);
        String expression = method.getAnnotation(PreAuthorize.class).value();
        assertTrue(expression.contains("hasRole('researcher')"));
        assertTrue(expression.contains("hasRole('admin')"));
        assertTrue(expression.contains("business:teachingSupervision:export"));
    }

    private static TeachingSupervisionQuery query(String academicYear, String semester)
    {
        TeachingSupervisionQuery query = new TeachingSupervisionQuery();
        query.setAcademicYear(academicYear);
        query.setSemester(semester);
        return query;
    }

    private static Date atStartOfDay(int year, int month, int day)
    {
        return Date.from(LocalDate.of(year, month, day).atStartOfDay(BEIJING_ZONE).toInstant());
    }
}
