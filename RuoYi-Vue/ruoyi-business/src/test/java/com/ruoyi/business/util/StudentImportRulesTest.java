package com.ruoyi.business.util;

import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentImportRulesTest
{
    @Test
    void acceptsOneOrTwoDigitClassAndNormalizesLeadingZero()
    {
        assertEquals("1", StudentImportRules.normalizeClassCode("01"));
        assertEquals("10", StudentImportRules.normalizeClassCode("10"));
    }

    @Test
    void rejectsGradePrefixedThreeDigitClassCode()
    {
        ServiceException exception = assertThrows(ServiceException.class,
                () -> StudentImportRules.normalizeClassCode("601"));
        assertTrue(exception.getMessage().contains("不要写 601、602"));
    }

    @Test
    void validatesStudentNumberAndEntryYear()
    {
        assertEquals("1", StudentImportRules.normalizeStudentNo("01"));
        assertEquals("2025", StudentImportRules.normalizeEntryYear("2025"));
        assertThrows(ServiceException.class, () -> StudentImportRules.normalizeStudentNo("100"));
        assertThrows(ServiceException.class, () -> StudentImportRules.normalizeEntryYear("25"));
    }
}

