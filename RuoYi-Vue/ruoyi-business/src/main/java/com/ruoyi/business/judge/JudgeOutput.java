package com.ruoyi.business.judge;

import java.util.ArrayList;
import java.util.List;

/**
 * 把判题输出拆成「正式输出」和「input 提示语」两部分。
 *
 * 背景：input("请输入您的年龄") 会把提示语写进标准输出，而且不带换行，
 * 于是 print("免票") 会紧跟在提示语后面。判题端如果直接拿原始输出和期望输出比，
 * 带提示语的题目必然判错；如果用正则删除提示语，又会误删学生自己打印的同样文字。
 * 因此只信任 {@link JudgeSourceWrapper} 写入的标记，标记之外的内容一律按真实输出处理。
 */
public final class JudgeOutput {
    private final String raw;
    private final String plainText;
    private final String visibleText;

    private JudgeOutput(String raw, String plainText, String visibleText) {
        this.raw = raw == null ? "" : raw;
        // 统一换行：判题比较本身忽略 CRLF，但教师差异页和学生终端会直接显示，残留 \r 会造成错位。
        this.plainText = normalizeNewlines(plainText);
        this.visibleText = normalizeNewlines(visibleText);
    }

    private static String normalizeNewlines(String value) {
        return value == null ? "" : value.replace("\r\n", "\n").replace('\r', '\n');
    }

    public static JudgeOutput of(String raw) {
        if (raw == null || raw.isEmpty()
                || (!raw.contains(JudgeSourceWrapper.PROMPT_START)
                && !raw.contains(JudgeSourceWrapper.PROMPT_END))) {
            String value = raw == null ? "" : raw;
            return new JudgeOutput(value, value, value);
        }
        StringBuilder plain = new StringBuilder();
        StringBuilder visible = new StringBuilder();
        int cursor = 0;
        while (cursor < raw.length()) {
            int start = raw.indexOf(JudgeSourceWrapper.PROMPT_START, cursor);
            if (start < 0) {
                plain.append(raw, cursor, raw.length());
                visible.append(raw, cursor, raw.length());
                break;
            }
            plain.append(raw, cursor, start);
            visible.append(raw, cursor, start);
            int end = raw.indexOf(JudgeSourceWrapper.PROMPT_END, start + JudgeSourceWrapper.PROMPT_START.length());
            if (end < 0) {
                // 标记不配对时按普通文本处理，宁可严格比较也不误删内容。
                plain.append(raw, start, raw.length());
                visible.append(raw, start, raw.length());
                break;
            }
            visible.append(raw, start + JudgeSourceWrapper.PROMPT_START.length(), end);
            cursor = end + JudgeSourceWrapper.PROMPT_END.length();
        }
        return new JudgeOutput(raw, plain.toString(), visible.toString());
    }

    /** 只含程序真正 print/输出的内容，不含 input 提示语。 */
    public String plain() {
        return plainText;
    }

    /** 去掉内部标记、保留提示语文字的版本，用于兼容历史题目的期望输出。 */
    public String visible() {
        return visibleText;
    }

    public String raw() {
        return raw;
    }

    /** 程序是否停在“还差输入”的状态，而不是真的出错。 */
    public static boolean needsMoreInput(String stderr) {
        return stderr != null && stderr.contains(JudgeSourceWrapper.NEED_INPUT);
    }

    /** 去掉内部标记后的错误文本，避免把控制字符展示给师生。 */
    public static String cleanError(String stderr) {
        if (stderr == null) return "";
        return stderr.replace(JudgeSourceWrapper.NEED_INPUT, "").trim();
    }

    /**
     * 兼容两种教师写法：期望输出只写结果，或期望输出连同提示语一起写。
     * 两种任一命中即通过，避免存量题目因规则变化失效。
     */
    public boolean matches(String expected) {
        return OutputComparator.matches(expected, plainText)
                || OutputComparator.matches(expected, visibleText);
    }

    /**
     * 生成和真实交互终端一致的文本：提示语 + 用户输入 + 换行 + 程序输出。
     * 教师和学生看到的将与学生本机运行完全一致，不再出现“请输入您的年龄免票”。
     */
    public String terminalText(String stdin) {
        List<String> inputs = splitLines(stdin);
        // 没有套壳标记时无法判断输入被读了几次（例如 input() 不带提示语，或代码根本没调用 input），
        // 此时原样返回程序输出，绝不追加任何说明文字，避免把提示当成程序输出参与判题或展示。
        if (!raw.contains(JudgeSourceWrapper.PROMPT_START)) {
            return normalizeNewlines(raw);
        }
        StringBuilder out = new StringBuilder();
        int cursor = 0;
        int inputIndex = 0;
        while (cursor < raw.length()) {
            int start = raw.indexOf(JudgeSourceWrapper.PROMPT_START, cursor);
            if (start < 0) {
                out.append(raw, cursor, raw.length());
                break;
            }
            out.append(raw, cursor, start);
            int end = raw.indexOf(JudgeSourceWrapper.PROMPT_END, start + JudgeSourceWrapper.PROMPT_START.length());
            if (end < 0) {
                out.append(raw, start, raw.length());
                break;
            }
            out.append(raw, start + JudgeSourceWrapper.PROMPT_START.length(), end);
            if (inputIndex < inputs.size()) {
                out.append(inputs.get(inputIndex));
                inputIndex++;
                // 有输入才换行；还在等输入时不换行，让输入框紧跟在提示语后面，和 Thonny 一致。
                out.append('\n');
            }
            cursor = end + JudgeSourceWrapper.PROMPT_END.length();
        }
        // 多填的输入不再追加说明文字：回放文本只负责展示真实的“提示语 + 输入 + 输出”。
        // 统一换行：个别判题机在 Windows 上返回 CRLF，残留在行尾的 \r 会让终端显示错位。
        return normalizeNewlines(out.toString());
    }

    private static List<String> splitLines(String stdin) {
        List<String> rows = new ArrayList<String>();
        if (stdin == null) return rows;
        String normalized = stdin.replace("\r\n", "\n").replace('\r', '\n');
        int start = 0;
        for (int i = 0; i < normalized.length(); i++) {
            if (normalized.charAt(i) == '\n') {
                rows.add(normalized.substring(start, i));
                start = i + 1;
            }
        }
        // 末尾换行不产生一个空输入，避免多发一次 input() 导致 EOF。
        if (start < normalized.length()) rows.add(normalized.substring(start));
        return rows;
    }
}
