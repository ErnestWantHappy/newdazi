package com.ruoyi.business.utils;

/**
 * 打字题内容保存前归一化。
 * 老师从 Word/WPS/PDF 复制课文时，英文撇（It's）常被自动替换成中文弯单引号（U+2018/U+2019），
 * 两者屏幕显示几乎一样，但学生键盘只能敲出英文撇，逐字比对必然判错。
 * 仅替换前后都是英文字母的弯单引号（It's、I'can）；中文课文引号和“中文标点练习题”（内容本身就是‘’）不受影响。
 */
public final class TypingContentNormalizer {
    private static final String CURLY_SINGLE_BETWEEN_LETTERS = "(?<=[A-Za-z])[‘’](?=[A-Za-z])";
    private static final String ENGLISH_APOSTROPHE = "'";

    private TypingContentNormalizer() {}

    public static String normalize(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        return content.replaceAll(CURLY_SINGLE_BETWEEN_LETTERS, ENGLISH_APOSTROPHE);
    }
}
