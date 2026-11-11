package com.ruoyi.business.judge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class JudgeOutcomeTest {
    @Test
    void weightDecidesScoreInsteadOfCaseCount() {
        // 两个测试点各占 50%，只通过一个得一半分
        assertEquals(50, JudgeOutcome.weightedScore(100D, 50D, 100D));
        // 权重不等时按权重算，而不是按“通过个数/总数”
        assertEquals(90, JudgeOutcome.weightedScore(100D, 90D, 100D));
        assertEquals(1, JudgeOutcome.weightedScore(2D, 50D, 100D));
    }

    @Test
    void scoreNeverExceedsFullScoreOrDropsBelowZero() {
        assertEquals(100, JudgeOutcome.weightedScore(100D, 120D, 100D));
        assertEquals(0, JudgeOutcome.weightedScore(100D, -10D, 100D));
        assertEquals(0, JudgeOutcome.weightedScore(100D, 0D, 0D));
    }

    @Test
    void sameWeightsAlwaysProduceSameScoreAcrossEntries() {
        // 课程链路满分是题目分值，刷题链路满分是 100，比例必须一致
        assertEquals(6, JudgeOutcome.weightedScore(20D, 30D, 100D));
        assertEquals(30, JudgeOutcome.weightedScore(100D, 30D, 100D));
    }

    @Test
    void summaryReportsPartialAndFirstRealFailure() {
        assertEquals("ACCEPTED", JudgeOutcome.summary(Arrays.asList("ACCEPTED", "ACCEPTED")));
        assertEquals("PARTIAL", JudgeOutcome.summary(Arrays.asList("ACCEPTED", "WRONG_ANSWER")));
        assertEquals("WRONG_ANSWER", JudgeOutcome.summary(Arrays.asList("WRONG_ANSWER", "WRONG_ANSWER")));
        // 全未通过时优先给出语法错误等真实原因，而不是笼统的答案错误
        assertEquals("SYNTAX_ERROR", JudgeOutcome.summary(Arrays.asList("WRONG_ANSWER", "SYNTAX_ERROR")));
        assertEquals("SERVICE_ERROR", JudgeOutcome.summary(Collections.<String>emptyList()));
    }

    @Test
    void missingInputIsNeverTreatedAsAccepted() {
        Judge0Result result = new Judge0Result();
        result.setStatusId(3);
        result.setStdout(JudgeSourceWrapper.PROMPT_START + "年龄" + JudgeSourceWrapper.PROMPT_END);
        result.setStderr(JudgeSourceWrapper.NEED_INPUT);
        // 输入没给够时程序提前结束，不能因为“跑完了”就判通过
        assertEquals("RUNTIME_ERROR", JudgeOutcome.caseStatus(result, ""));
        assertEquals("RUNTIME_ERROR", JudgeOutcome.runStatus(result));
    }

    @Test
    void promptIsIgnoredWhenComparingWithExpectedOutput() {
        Judge0Result result = new Judge0Result();
        result.setStatusId(3);
        result.setStdout(JudgeSourceWrapper.PROMPT_START + "请输入年龄" + JudgeSourceWrapper.PROMPT_END + "免票\n");
        assertEquals("ACCEPTED", JudgeOutcome.caseStatus(result, "免票"));
        // 存量题目把提示语也写进期望输出，同样要能通过
        assertEquals("ACCEPTED", JudgeOutcome.caseStatus(result, "请输入年龄免票"));
    }
}
