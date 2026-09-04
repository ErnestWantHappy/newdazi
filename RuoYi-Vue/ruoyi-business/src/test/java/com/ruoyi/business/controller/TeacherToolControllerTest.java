package com.ruoyi.business.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class TeacherToolControllerTest
{
    @Test
    void catalogRequiresListPermissionAndAllowedRoles() throws Exception
    {
        assertAuthorization("catalog", "business:teacherTool:list", "hasRole('teacher')",
                "hasRole('researcher')", "hasRole('admin')");
    }

    @Test
    void managementRequiresManagePermissionAndResearcherOrAdminRole() throws Exception
    {
        for (String method : Arrays.asList("categories", "createCategory", "updateCategory",
                "updateCategoryStatus", "tools", "tool", "createTool", "updateTool",
                "updateToolStatus", "deleteTool", "restoreTool"))
        {
            assertAuthorization(method, "business:teacherTool:manage", "hasRole('researcher')", "hasRole('admin')");
        }
    }

    private void assertAuthorization(String methodName, String... expected) throws Exception
    {
        Method method = Arrays.stream(TeacherToolController.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst().orElseThrow(NoSuchMethodException::new);
        String expression = method.getAnnotation(PreAuthorize.class).value();
        for (String fragment : expected)
        {
            assertTrue(expression.contains(fragment));
        }
    }
}
