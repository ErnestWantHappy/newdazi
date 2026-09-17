package com.ruoyi.business.config;

/**
 * 课堂房间键的唯一构造入口，避免握手、广播使用不同的班号格式而丢失推送。
 */
public final class ClassroomRoomKey
{
    private ClassroomRoomKey()
    {
    }

    public static String of(Long deptId, String entryYear, String classCode, Long lessonId)
    {
        if (deptId == null || lessonId == null) return null;
        String normalizedYear = entryYear == null ? null : entryYear.trim();
        String normalizedClassCode = normalizeClassCode(classCode);
        if (normalizedYear == null || normalizedYear.isEmpty()
                || normalizedClassCode == null || normalizedClassCode.isEmpty()) return null;
        return deptId + "_" + normalizedYear + "_" + normalizedClassCode + "_" + lessonId;
    }

    public static String normalizeClassCode(String classCode)
    {
        if (classCode == null) return null;
        String normalized = classCode.trim();
        if (normalized.endsWith("班")) normalized = normalized.substring(0, normalized.length() - 1);
        if (normalized.matches("\\d{1,2}"))
        {
            return String.valueOf(Integer.parseInt(normalized));
        }
        return normalized;
    }
}
