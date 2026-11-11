package com.ruoyi.business.judge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JudgeOutputTest {
    private static final String P = JudgeSourceWrapper.PROMPT_START;
    private static final String E = JudgeSourceWrapper.PROMPT_END;

    @Test
    void keepsPlainOutputForProgramsWithoutInput() {
        JudgeOutput output = JudgeOutput.of("免票\n");
        assertEquals("免票\n", output.plain());
        assertEquals("免票\n", output.visible());
        assertEquals("免票\n", output.terminalText("3"));
    }

    @Test
    void promptIsRemovedFromPlainButKeptInVisible() {
        JudgeOutput output = JudgeOutput.of(P + "请输入您的年龄" + E + "免票\n");
        assertEquals("免票\n", output.plain());
        assertEquals("请输入您的年龄免票\n", output.visible());
        // 终端回放：提示语后面跟用户输入并换行，再接程序输出
        assertEquals("请输入您的年龄3\n免票\n", output.terminalText("3"));
    }

    @Test
    void multipleInputsReplayInOrder() {
        JudgeOutput output = JudgeOutput.of(P + "第一个：" + E + P + "第二个：" + E + "合计 8\n");
        assertEquals("合计 8\n", output.plain());
        assertEquals("第一个：3\n第二个：5\n合计 8\n", output.terminalText("3\n5\n"));
    }

    @Test
    void programPrintingSameTextAsPromptIsKept() {
        // 学生自己打印“请输入您的年龄”时不带标记，必须保留在正式输出里
        JudgeOutput output = JudgeOutput.of(P + "请输入您的年龄" + E + "请输入您的年龄\n");
        assertEquals("请输入您的年龄\n", output.plain());
        assertTrue(OutputComparator.matches("请输入您的年龄", output.plain()));
    }

    @Test
    void unpairedMarkerIsTreatedAsNormalText() {
        JudgeOutput output = JudgeOutput.of("开始" + P + "没有结束标记");
        assertTrue(output.plain().contains("没有结束标记"));
        // 标记不配对时保留原文，不做任何删除，宁可判得严也不误删学生输出
        assertTrue(output.plain().contains(P));
    }

    @Test
    void inputWithoutPromptStillReplaysTypedValue() {
        JudgeOutput output = JudgeOutput.of(P + E + "你好\n");
        assertEquals("你好\n", output.plain());
        assertEquals("张三\n你好\n", output.terminalText("张三"));
    }

    @Test
    void waitingForInputKeepsCursorRightAfterPrompt() {
        JudgeOutput output = JudgeOutput.of(P + "请输入您的年龄" + E);
        // 还没有输入时不要凭空补一个换行，输入框应紧跟在提示语后面
        assertEquals("请输入您的年龄", output.terminalText(""));
    }

    @Test
    void moreInputsThanConsumedDoesNotPolluteOutput() {
        JudgeOutput output = JudgeOutput.of(P + "年龄：" + E + "免票\n");
        // 多填的输入不能拼进输出，否则会被当成程序打印的内容
        assertEquals("年龄：3\n免票\n", output.terminalText("3\n多余"));
    }

    @Test
    void matchesAcceptsBothTeacherStyles() {
        JudgeOutput output = JudgeOutput.of(P + "请输入您的年龄" + E + "免票\n");
        assertTrue(output.matches("免票"));
        assertTrue(output.matches("请输入您的年龄免票"));
        assertFalse(output.matches("半票"));
    }

    @Test
    void needInputMarkerIsRecognisedAndHidden() {
        String stderr = JudgeSourceWrapper.NEED_INPUT;
        assertTrue(JudgeOutput.needsMoreInput(stderr));
        assertEquals("", JudgeOutput.cleanError(stderr));
        assertFalse(JudgeOutput.needsMoreInput(null));
        assertFalse(JudgeOutput.needsMoreInput("SyntaxError: invalid syntax"));
    }

    @Test
    void crlfInputIsSplitCorrectly() {
        JudgeOutput output = JudgeOutput.of(P + "年龄：" + E + P + "姓名：" + E + "完成\n");
        assertEquals("年龄：3\n姓名：小明\n完成\n", output.terminalText("3\r\n小明\r\n"));
    }

    @Test
    void trailingNewlineInStdinDoesNotCreateExtraInput() {
        JudgeOutput output = JudgeOutput.of(P + "年龄：" + E + "免票\n");
        assertEquals("年龄：3\n免票\n", output.terminalText("3\n"));
    }

    @Test
    void emptyEnterReplaysAsOneEmptyInputLine() {
        JudgeOutput output = JudgeOutput.of(P + "请输入：" + E + "完成\n");
        assertEquals("请输入：\n完成\n", output.terminalText("\n"));
    }

    @Test
    void windowsLineEndingsDoNotLeaveStrayCarriageReturns() {
        // 判题机在 Windows 上返回 CRLF 时，终端不能残留 \r（否则行首会被覆盖、显示错位）
        JudgeOutput prompted = JudgeOutput.of(P + "年龄：" + E + "免票\r\n");
        assertEquals("年龄：3\n免票\n", prompted.terminalText("3"));
        assertEquals("免票\n", prompted.plain());
        // 没有提示语的程序同样要统一换行
        assertEquals("第一行\n第二行\n", JudgeOutput.of("第一行\r\n第二行\r\n").terminalText(null));
    }

    @Test
    void differenceHintExplainsPromptJoinedCase() {
        // 存量题目把提示语写进期望输出时，给出可照做的建议而不是技术术语
        assertNull(JudgeOutcome.differenceHint("请输入您的年龄免票", P + "请输入您的年龄" + E + "免票\n"));
        // 期望输出真的不一致时，说明期望是什么、实际是什么
        String hint = JudgeOutcome.differenceHint("半票", P + "请输入您的年龄" + E + "免票\n");
        assertTrue(hint.contains("半票"), hint);
        assertTrue(hint.contains("免票"), hint);
        assertFalse(hint.contains("stdout"), hint);
    }
}
