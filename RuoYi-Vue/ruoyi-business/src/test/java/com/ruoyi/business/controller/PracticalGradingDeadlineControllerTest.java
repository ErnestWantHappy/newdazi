package com.ruoyi.business.controller;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PracticalGradingDeadlineControllerTest
{
    @Test
    void managementMethodsRequireRoleTogetherWithPermission()
    {
        for (Method method : PracticalGradingDeadlineController.class.getDeclaredMethods())
        {
            PreAuthorize authorize = method.getAnnotation(PreAuthorize.class);
            if (authorize == null)
            {
                continue;
            }
            String expression = authorize.value();
            assertTrue(expression.contains("hasRole('researcher')"), method.getName());
            assertTrue(expression.contains("hasRole('admin')"), method.getName());
            assertTrue(expression.contains("hasPermi("), method.getName());
        }
    }
}
