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

    /** Python 由解释器执行，部分 Judge0 会把语法错误归入 Runtime Error，需要结合错误文本纠正。 */
    public static String toPlatformStatus(Judge0Result result) {
        String status = toPlatformStatus(result == null ? null : result.getStatusId());
        if (!"RUNTIME_ERROR".equals(status) || result == null) return status;
        String details = safe(result.getCompileOutput()) + "\n" + safe(result.getStderr()) + "\n" + safe(result.getMessage());
        if (details.contains("SyntaxError") || details.contains("IndentationError") || details.contains("TabError")) {
            return "SYNTAX_ERROR";
        }
        return status;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
