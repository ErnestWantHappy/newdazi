package com.ruoyi.business.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class ExemptionApplicationControllerTest
{
    @Test
    void teacherEndpointsRequireTeacherRoleAndApplyPermission() throws Exception
    {
        assertAuthorization("preview", "hasRole('teacher')", "business:exemption:apply");
        assertAuthorization("submit", "hasRole('teacher')", "business:exemption:apply");
        assertAuthorization("myApplications", "hasRole('teacher')", "business:exemption:apply");
    }

    @Test
    void reviewAndStandardEndpointsRequireResearchRoleAndIndependentPermissions() throws Exception
    {
        assertAuthorization("reviewApplications", "hasRole('researcher')",
                "business:exemption:review");
        assertAuthorization("review", "hasRole('researcher')",
                "business:exemption:review");
        assertAuthorization("standards", "hasRole('researcher')",
                "business:exemption:standard");
        assertAuthorization("saveStandard", "hasRole('researcher')",
                "business:exemption:standard");
    }

    private void assertAuthorization(String methodName, String... expected) throws Exception
    {
        Method method = Arrays.stream(ExemptionApplicationController.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow(NoSuchMethodException::new);
        String expression = method.getAnnotation(PreAuthorize.class).value();
        for (String fragment : expected)
        {
            assertTrue(expression.contains(fragment));
        }
    }
}
