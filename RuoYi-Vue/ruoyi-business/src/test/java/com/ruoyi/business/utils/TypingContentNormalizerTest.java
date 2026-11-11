package com.ruoyi.business.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** 打字题弯单引号归一化：英文撇场景必须修复，中文引号场景必须原样保留。 */
class TypingContentNormalizerTest
{
    @Test
    void curlyApostropheBetweenLettersIsNormalized()
    {
        // 130 线上 991848/990812 的实际病灶：Word 复制带入的 U+2019。
        assertEquals("It's orange.", TypingContentNormalizer.normalize("It’s orange."));
        assertEquals("I'can speak English", TypingContentNormalizer.normalize("I’can speak English"));
        assertEquals("don't and can't", TypingContentNormalizer.normalize("don’t and can’t"));
    }

    @Test
    void chineseQuotesArePreserved()
    {
        // 中文课文里的弯引号是合法中文标点；符号练习题的练习对象就是弯引号本身，都不能动。
        String chinese = "他说“人工智能‘AI’很好”";
        assertEquals(chinese, TypingContentNormalizer.normalize(chinese));
        String symbolDrill = "：“”；‘’ zxcvbn";
        assertEquals(symbolDrill, TypingContentNormalizer.normalize(symbolDrill));
    }

    @Test
    void straightApostropheAndEdgeCasesStayUnchanged()
    {
        assertEquals("It's orange.", TypingContentNormalizer.normalize("It's orange."));
        // 撇后不是字母（复数所有格 teachers'）保守不动，宁可留待人工处理也不错改中文语境。
        assertEquals("the teachers’ books", TypingContentNormalizer.normalize("the teachers’ books"));
        assertEquals("‘quoted’", TypingContentNormalizer.normalize("‘quoted’"));
    }

    @Test
    void nullAndEmptyAreSafe()
    {
        assertNull(TypingContentNormalizer.normalize(null));
        assertEquals("", TypingContentNormalizer.normalize(""));
    }
}
