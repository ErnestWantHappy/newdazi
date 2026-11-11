package com.ruoyi.business.judge;

import java.util.List;

/**
 * 判题结论的唯一来源：状态判定、通过数量和加权分数都在这里算。
 *
 * 之前课程操作题和 Python 刷题各写一套逻辑，出现同一道题两条链路分数不同的情况
 * （刷题链路直接按通过点数取百分比，完全忽略测试点权重）。现在两边都调用这里。
 */
public final class JudgeOutcome {
    private JudgeOutcome() {
    }

    /** 单测试点状态：只有判题服务本身接受、且输出匹配，才算通过。 */
    public static String caseStatus(Judge0Result result, String expectedOutput) {
        // 正式判题时输入由题目固定给出，程序还要输入说明题目漏配了输入，不能当成功。
        if (JudgeOutput.needsMoreInput(result == null ? null : result.getStderr())) return "RUNTIME_ERROR";
        String status = Judge0StatusMapper.toPlatformStatus(result);
        if (!"ACCEPTED".equals(status) && !"WRONG_ANSWER".equals(status)) return status;
        if (result == null) return status;
        return JudgeOutput.of(result.getStdout()).matches(expectedOutput) ? "ACCEPTED" : "WRONG_ANSWER";
    }

    /** 自定义运行没有期望输出，能跑完即视为运行成功。 */
    public static String runStatus(Judge0Result result) {
        // 学生已经提交了输入却仍停在等待输入，说明输入没填够，按运行错误提示更贴近真实原因。
        if (JudgeOutput.needsMoreInput(result == null ? null : result.getStderr())) return "RUNTIME_ERROR";
        String status = Judge0StatusMapper.toPlatformStatus(result);
        return "ACCEPTED".equals(status) ? "COMPLETED" : status;
    }

    /**
     * 按权重合计得分。满分由题目在课程/题单中的分值决定，
     * 权重只用于分配分数，这样同一道题在任何入口得分一致。
     */
    public static int weightedScore(double fullScore, double passedWeight, double totalWeight) {
        if (totalWeight <= 0D) return 0;
        int score = (int) Math.round(fullScore * passedWeight / totalWeight);
        return Math.max(0, Math.min((int) Math.round(fullScore), score));
    }

    /** 多测试点总状态：全部通过→通过，部分通过→部分通过，全未通过取第一个非答案错误的失败原因。 */
    public static String summary(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) return "SERVICE_ERROR";
        int passed = 0;
        for (String status : statuses) if ("ACCEPTED".equals(status)) passed++;
        if (passed == statuses.size()) return "ACCEPTED";
        if (passed > 0) return "PARTIAL";
        for (String status : statuses) if (!"WRONG_ANSWER".equals(status)) return status;
        return "WRONG_ANSWER";
    }

    public static String statusMessage(String status) {
        if ("COMPLETED".equals(status)) return "运行完成";
        if ("ACCEPTED".equals(status)) return "通过";
        if ("PARTIAL".equals(status)) return "部分通过";
        if ("WRONG_ANSWER".equals(status)) return "答案错误";
        if ("SYNTAX_ERROR".equals(status)) return "语法错误";
        if ("RUNTIME_ERROR".equals(status)) return "运行错误";
        if ("TIME_LIMIT".equals(status)) return "运行超时";
        if ("MEMORY_LIMIT".equals(status)) return "内存超限";
        if ("SERVICE_BUSY".equals(status)) return "判题队列繁忙";
        return "判题服务异常，代码和提交已保留";
    }

    /**
     * 验证失败时给教师看的差异说明。人话描述，不出现 stdout/测试点编号等技术词。
     */
    public static String differenceHint(String expected, String actualRaw) {
        JudgeOutput output = JudgeOutput.of(actualRaw);
        String actual = output.plain();
        if (OutputComparator.matches(expected, actual)) return null;
        String visible = output.visible();
        if (!visible.equals(actual) && OutputComparator.matches(expected, visible)) {
            // 教师把“提示语+结果”一起写成期望输出时，判题本身是能通过的，不算差异。
            return null;
        }
        return "期望输出是“" + brief(expected) + "”，程序实际输出是“" + brief(actual) + "”。";
    }

    private static String brief(String value) {
        if (value == null) return "（空）";
        String text = value.replace("\r\n", "\n").replace('\r', '\n').trim();
        if (text.isEmpty()) return "（空）";
        return text.length() > 60 ? text.substring(0, 60) + "…" : text;
    }
}
