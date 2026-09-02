package com.ruoyi.business.util;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 学生导入字段规范。班号只表示班序，不包含年级信息。
 */
public final class StudentImportRules
{
    private StudentImportRules()
    {
    }

    public static String normalizeClassCode(String rawClassCode)
    {
        String classCode = StringUtils.trimToEmpty(rawClassCode);
        if (!classCode.matches("\\d{1,2}"))
        {
            throw new ServiceException(classCodeMessage());
        }
        int classNumber = Integer.parseInt(classCode);
        if (classNumber < 1 || classNumber > 99)
        {
            throw new ServiceException(classCodeMessage());
        }
        return String.valueOf(classNumber);
    }

    public static String normalizeStudentNo(String rawStudentNo)
    {
        String studentNo = StringUtils.trimToEmpty(rawStudentNo);
        if (!studentNo.matches("\\d{1,2}"))
        {
            throw new ServiceException("学号只能填写 01～99，不要填写完整登录账号");
        }
        int studentNumber = Integer.parseInt(studentNo);
        if (studentNumber < 1 || studentNumber > 99)
        {
            throw new ServiceException("学号只能填写 01～99，不要填写完整登录账号");
        }
        return String.valueOf(studentNumber);
    }

    public static String normalizeEntryYear(String rawEntryYear)
    {
        String entryYear = StringUtils.trimToEmpty(rawEntryYear);
        if (!entryYear.matches("20\\d{2}"))
        {
            throw new ServiceException("入学年份必须填写 4 位年份，例如 2025");
        }
        return entryYear;
    }

    private static String classCodeMessage()
    {
        return "班级编号只能填写 01～99；只填班号，不要写 601、602 等带年级的三位数";
    }
}
