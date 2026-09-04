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

    /** 历史届按学段最高年级展示，避免毕业生查询被当作参数错误。 */
    public static int resolveDisplayGrade(int entryYear, String schoolType, LocalDate date)
    {
        requireDate(date);
        SchoolSection section = requireSchoolSection(schoolType);
        int gradeInSection = resolveAcademicStartYear(date) - entryYear + 1;
        if (gradeInSection < 1)
        {
            throw new IllegalArgumentException("入学年份不能晚于当前学年");
        }
        return section.gradeOffset + Math.min(gradeInSection, section.durationYears);
    }

    /** 页面统一使用的年级名称；已经毕业的历史届显示“已毕业”。 */
    public static String resolveGradeName(String entryYear, String schoolType, LocalDate date)
    {
        if (entryYear == null || entryYear.trim().isEmpty())
        {
            return "未知年级";
        }
        final int parsed;
        try
        {
            parsed = Integer.parseInt(entryYear.trim());
        }
        catch (NumberFormatException ex)
        {
            return "未知年级";
        }
        SchoolSection section = requireSchoolSection(schoolType);
        int gradeInSection = resolveAcademicStartYear(date) - parsed + 1;
        if (gradeInSection < 1)
        {
            return "未知年级";
        }
        if (gradeInSection > section.durationYears)
        {
            return "已毕业";
        }
        return gradeName(section.gradeOffset + gradeInSection);
    }

    /**
     * 生成当前教学班简称，例如初中 2024 级 1 班在 2026 学年显示为“901班”。
     * 班号统一补足两位，避免“9年级1班”和“901班”两套口径并存。
     */
    public static String resolveClassLabel(String entryYear, String classCode, String schoolType, LocalDate date)
    {
        int absoluteGrade = resolveAbsoluteGrade(entryYear, schoolType, date);
        if (classCode == null || !classCode.trim().matches("\\d+"))
        {
            throw new IllegalArgumentException("班级编号必须是数字");
        }
        int parsedClassCode = Integer.parseInt(classCode.trim());
        if (parsedClassCode < 1 || parsedClassCode > 99)
        {
            throw new IllegalArgumentException("班级编号必须在 1-99 之间");
        }
        return String.format("%d%02d班", absoluteGrade, parsedClassCode);
    }

    private static String gradeName(int absoluteGrade)
    {
        switch (absoluteGrade)
        {
            case 1: return "一年级";
            case 2: return "二年级";
            case 3: return "三年级";
            case 4: return "四年级";
            case 5: return "五年级";
            case 6: return "六年级";
            case 7: return "七年级";
            case 8: return "八年级";
            case 9: return "九年级";
            case 10: return "高一";
            case 11: return "高二";
            case 12: return "高三";
            default: return "未知年级";
        }
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
