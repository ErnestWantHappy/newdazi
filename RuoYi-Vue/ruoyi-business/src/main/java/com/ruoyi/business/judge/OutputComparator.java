package com.ruoyi.business.judge;

import java.util.ArrayList;
import java.util.List;

/**
 * OJ 输出比较规则：兼容不同系统换行和无意义的行尾空白，但保留真正影响答案的内容差异。
 */
public final class OutputComparator {
    private OutputComparator() {
    }

    public static boolean matches(String expected, String actual) {
        return normalize(expected).equals(normalize(actual));
    }

    public static String normalize(String value) {
        String normalized = value == null ? "" : value.replace("\r\n", "\n").replace('\r', '\n');
        String[] rawLines = normalized.split("\n", -1);
        List<String> lines = new ArrayList<String>(rawLines.length);
        for (String line : rawLines) {
            int end = line.length();
            while (end > 0) {
                char current = line.charAt(end - 1);
                if (current != ' ' && current != '\t') break;
                end--;
            }
            lines.add(line.substring(0, end));
        }
        int last = lines.size();
        while (last > 0 && lines.get(last - 1).isEmpty()) last--;
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < last; index++) {
            if (index > 0) result.append('\n');
            result.append(lines.get(index));
        }
        return result.toString();
    }
}
