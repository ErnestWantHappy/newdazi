package com.ruoyi.business.util;

import java.time.LocalDate;
import java.time.MonthDay;

/**
 * 学年与年级换算工具。
 *
 * <p>平台统一在每年 7 月 20 日（含当天）切换到新学年，所有依赖日期的年级计算
 * 都应通过本工具完成，避免课程、成绩与区域抽测采用不同切换口径。</p>
 */
public final class AcademicYearUtils
{
    public static final String PRIMARY_SCHOOL = "1";
    public static final String JUNIOR_HIGH_SCHOOL = "2";
    public static final String SENIOR_HIGH_SCHOOL = "3";

    private static final MonthDay ACADEMIC_YEAR_CUTOFF = MonthDay.of(7, 20);

    private AcademicYearUtils()
    {
    }

    /**
     * 返回指定日期所属学年的起始年份。
     */
    public static int resolveAcademicStartYear(LocalDate date)
    {
        requireDate(date);
        MonthDay currentDay = MonthDay.from(date);
        return currentDay.compareTo(ACADEMIC_YEAR_CUTOFF) >= 0
                ? date.getYear() : date.getYear() - 1;
    }

    /**
     * 根据入学年份和学段计算绝对年级：小学为 1-6，初中为 7-9，高中为 10-12。
     */
    public static int resolveAbsoluteGrade(String entryYear, String schoolType, LocalDate date)
    {
        if (entryYear == null || !entryYear.trim().matches("\\d{4}"))
        {
            throw new IllegalArgumentException("入学年份必须是四位数字");
        }
        return resolveAbsoluteGrade(Integer.parseInt(entryYear.trim()), schoolType, date);
    }

    /**
     * 根据入学年份和学段计算绝对年级：小学为 1-6，初中为 7-9，高中为 10-12。
     */
    public static int resolveAbsoluteGrade(int entryYear, String schoolType, LocalDate date)
    {
        requireDate(date);
        SchoolSection section = requireSchoolSection(schoolType);
        int gradeInSection = resolveAcademicStartYear(date) - entryYear + 1;
        if (gradeInSection < 1 || gradeInSection > section.durationYears)
        {
            throw new IllegalArgumentException("入学年份与当前学年、学段不匹配");
        }
        return section.gradeOffset + gradeInSection;
    }

    /**
     * 根据绝对年级和指定日期反推出入学年份。
     */
    public static String resolveEntryYear(int absoluteGrade, LocalDate date)
    {
        requireDate(date);
        int gradeInSection = gradeInSection(absoluteGrade);
        return String.valueOf(resolveAcademicStartYear(date) - gradeInSection + 1);
    }

    /**
     * 把绝对年级换算为学段内年级序号，例如七年级和高一都返回 1。
     */
    public static int gradeInSection(int absoluteGrade)
    {
        if (absoluteGrade >= 1 && absoluteGrade <= 6)
        {
            return absoluteGrade;
        }
        if (absoluteGrade >= 7 && absoluteGrade <= 9)
        {
            return absoluteGrade - 6;
        }
        if (absoluteGrade >= 10 && absoluteGrade <= 12)
        {
            return absoluteGrade - 9;
        }
        throw new IllegalArgumentException("绝对年级必须在 1-12 之间");
    }

    private static void requireDate(LocalDate date)
    {
        if (date == null)
        {
            throw new IllegalArgumentException("日期不能为空");
        }
    }

    private static SchoolSection requireSchoolSection(String schoolType)
    {
        if (PRIMARY_SCHOOL.equals(schoolType))
        {
            return new SchoolSection(0, 6);
        }
        if (JUNIOR_HIGH_SCHOOL.equals(schoolType))
        {
            return new SchoolSection(6, 3);
        }
        if (SENIOR_HIGH_SCHOOL.equals(schoolType))
        {
            return new SchoolSection(9, 3);
        }
        throw new IllegalArgumentException("学段类型必须是 1、2 或 3");
    }

    private static final class SchoolSection
    {
        private final int gradeOffset;
        private final int durationYears;

        private SchoolSection(int gradeOffset, int durationYears)
        {
            this.gradeOffset = gradeOffset;
            this.durationYears = durationYears;
        }
    }
}
