package com.ruoyi.business.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AcademicYearUtilsTest
{
    private static final LocalDate BEFORE_CUTOFF = LocalDate.of(2026, 7, 19);
    private static final LocalDate ON_CUTOFF = LocalDate.of(2026, 7, 20);
    private static final LocalDate AFTER_CUTOFF = LocalDate.of(2026, 7, 21);

    @Test
    void shouldSwitchAcademicYearOnJulyTwentieth()
    {
        assertEquals(2025, AcademicYearUtils.resolveAcademicStartYear(BEFORE_CUTOFF));
        assertEquals(2026, AcademicYearUtils.resolveAcademicStartYear(ON_CUTOFF));
        assertEquals(2026, AcademicYearUtils.resolveAcademicStartYear(AFTER_CUTOFF));
        assertEquals(2025, AcademicYearUtils.resolveAcademicStartYear(LocalDate.of(2026, 1, 1)));
    }

    @Test
    void shouldResolveAbsoluteGradeForEverySchoolSection()
    {
        assertEquals(8, AcademicYearUtils.resolveAbsoluteGrade(
                "2024", AcademicYearUtils.JUNIOR_HIGH_SCHOOL, BEFORE_CUTOFF));
        assertEquals(9, AcademicYearUtils.resolveAbsoluteGrade(
                "2024", AcademicYearUtils.JUNIOR_HIGH_SCHOOL, ON_CUTOFF));
        assertEquals(8, AcademicYearUtils.resolveAbsoluteGrade(
                2025, AcademicYearUtils.JUNIOR_HIGH_SCHOOL, AFTER_CUTOFF));
        assertEquals(2, AcademicYearUtils.resolveAbsoluteGrade(
                "2025", AcademicYearUtils.PRIMARY_SCHOOL, AFTER_CUTOFF));
        assertEquals(12, AcademicYearUtils.resolveAbsoluteGrade(
                "2024", AcademicYearUtils.SENIOR_HIGH_SCHOOL, AFTER_CUTOFF));
    }

    @Test
    void shouldResolveEntryYearFromAbsoluteGrade()
    {
        assertEquals("2024", AcademicYearUtils.resolveEntryYear(9, ON_CUTOFF));
        assertEquals("2025", AcademicYearUtils.resolveEntryYear(8, AFTER_CUTOFF));
        assertEquals("2021", AcademicYearUtils.resolveEntryYear(6, AFTER_CUTOFF));
        assertEquals("2024", AcademicYearUtils.resolveEntryYear(12, AFTER_CUTOFF));
    }

    @Test
    void shouldResolveCurrentClassLabelForEverySchoolSection()
    {
        assertEquals("901班", AcademicYearUtils.resolveClassLabel(
                "2024", "1", AcademicYearUtils.JUNIOR_HIGH_SCHOOL, AFTER_CUTOFF));
        assertEquals("803班", AcademicYearUtils.resolveClassLabel(
                "2025", "3", AcademicYearUtils.JUNIOR_HIGH_SCHOOL, AFTER_CUTOFF));
        assertEquals("301班", AcademicYearUtils.resolveClassLabel(
                "2024", "1", AcademicYearUtils.PRIMARY_SCHOOL, AFTER_CUTOFF));
        assertEquals("1201班", AcademicYearUtils.resolveClassLabel(
                "2024", "1", AcademicYearUtils.SENIOR_HIGH_SCHOOL, AFTER_CUTOFF));
    }

    @Test
    void shouldResolveGradePositionInsideSchoolSection()
    {
        assertEquals(1, AcademicYearUtils.gradeInSection(1));
        assertEquals(6, AcademicYearUtils.gradeInSection(6));
        assertEquals(1, AcademicYearUtils.gradeInSection(7));
        assertEquals(3, AcademicYearUtils.gradeInSection(9));
        assertEquals(1, AcademicYearUtils.gradeInSection(10));
        assertEquals(3, AcademicYearUtils.gradeInSection(12));
    }

    @Test
    void shouldRoundTripEveryAbsoluteGrade()
    {
        for (int grade = 1; grade <= 12; grade++)
        {
            String schoolType = grade <= 6
                    ? AcademicYearUtils.PRIMARY_SCHOOL
                    : grade <= 9 ? AcademicYearUtils.JUNIOR_HIGH_SCHOOL
                    : AcademicYearUtils.SENIOR_HIGH_SCHOOL;
            String entryYear = AcademicYearUtils.resolveEntryYear(grade, AFTER_CUTOFF);
            assertEquals(grade, AcademicYearUtils.resolveAbsoluteGrade(entryYear, schoolType, AFTER_CUTOFF));
        }
    }

    @Test
    void shouldRejectInvalidArguments()
    {
        assertThrows(IllegalArgumentException.class,
                () -> AcademicYearUtils.resolveAcademicStartYear(null));
        assertThrows(IllegalArgumentException.class,
                () -> AcademicYearUtils.resolveAbsoluteGrade(null, AcademicYearUtils.JUNIOR_HIGH_SCHOOL, AFTER_CUTOFF));
        assertThrows(IllegalArgumentException.class,
                () -> AcademicYearUtils.resolveAbsoluteGrade("20A4", AcademicYearUtils.JUNIOR_HIGH_SCHOOL, AFTER_CUTOFF));
        assertThrows(IllegalArgumentException.class,
                () -> AcademicYearUtils.resolveAbsoluteGrade("2024", "4", AFTER_CUTOFF));
        assertThrows(IllegalArgumentException.class,
                () -> AcademicYearUtils.resolveAbsoluteGrade("2026", AcademicYearUtils.JUNIOR_HIGH_SCHOOL, BEFORE_CUTOFF));
        assertThrows(IllegalArgumentException.class,
                () -> AcademicYearUtils.resolveAbsoluteGrade("2020", AcademicYearUtils.JUNIOR_HIGH_SCHOOL, AFTER_CUTOFF));
        assertThrows(IllegalArgumentException.class,
                () -> AcademicYearUtils.resolveEntryYear(9, null));
        assertThrows(IllegalArgumentException.class,
                () -> AcademicYearUtils.gradeInSection(0));
        assertThrows(IllegalArgumentException.class,
                () -> AcademicYearUtils.gradeInSection(13));
        assertThrows(IllegalArgumentException.class,
                () -> AcademicYearUtils.resolveClassLabel("2024", "A", AcademicYearUtils.JUNIOR_HIGH_SCHOOL, AFTER_CUTOFF));
    }

    @Test
    void shouldKeepHistoricalGradeQueriesAvailable()
    {
        assertEquals(9, AcademicYearUtils.resolveDisplayGrade(
                2020, AcademicYearUtils.JUNIOR_HIGH_SCHOOL, AFTER_CUTOFF));
        assertEquals("已毕业", AcademicYearUtils.resolveGradeName(
                "2020", AcademicYearUtils.JUNIOR_HIGH_SCHOOL, AFTER_CUTOFF));
        assertEquals("九年级", AcademicYearUtils.resolveGradeName(
                "2024", AcademicYearUtils.JUNIOR_HIGH_SCHOOL, AFTER_CUTOFF));
    }
}
