package com.ruoyi.business.judge;

/** Judge0 原始状态统一转换为平台对学生展示和成绩处理的终态。 */
public final class Judge0StatusMapper {
    private Judge0StatusMapper() {
    }

    public static String toPlatformStatus(Integer statusId) {
        if (statusId == null) return "SERVICE_ERROR";
        switch (statusId) {
            case 3: return "ACCEPTED";
            case 4: return "WRONG_ANSWER";
            case 5: return "TIME_LIMIT";
            case 6: return "SYNTAX_ERROR";
            case 12: return "MEMORY_LIMIT";
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 14: return "RUNTIME_ERROR";
            default: return "SERVICE_ERROR";
        }
    }
}
