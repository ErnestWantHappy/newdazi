package com.ruoyi.business.judge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutputComparatorTest {
    @Test
    void acceptsDifferentLineEndingsAndTrailingWhitespace() {
        assertTrue(OutputComparator.matches("第一行\n第二行", "第一行  \r\n第二行\t\r\n\r\n"));
    }

    @Test
    void keepsInternalBlankLinesSignificant() {
        assertFalse(OutputComparator.matches("第一行\n\n第二行", "第一行\n第二行"));
    }

    @Test
    void keepsCaseNumberFormatAndWordSpacingSignificant() {
        assertFalse(OutputComparator.matches("Python", "python"));
        assertFalse(OutputComparator.matches("1.0", "1"));
        assertFalse(OutputComparator.matches("a b", "a  b"));
    }

    @Test
    void treatsNullAndWhitespaceOnlyOutputAsEmpty() {
        assertTrue(OutputComparator.matches(null, " \t\r\n"));
    }
}
