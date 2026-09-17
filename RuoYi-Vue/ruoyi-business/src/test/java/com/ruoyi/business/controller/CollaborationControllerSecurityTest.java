package com.ruoyi.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class CollaborationControllerSecurityTest
{
    @Test
    void studentEntryUsesBusinessFactsInsteadOfMissingSystemRole() throws Exception
    {
        Method current = CollaborationController.class.getMethod("currentStudentRooms");
        Method session = CollaborationController.class.getMethod("session", Long.class);

        assertNull(current.getAnnotation(PreAuthorize.class));
        assertNull(session.getAnnotation(PreAuthorize.class));
    }

    @Test
    void teacherConfigurationStillRequiresTeacherOrAdmin() throws Exception
    {
        Method settings = CollaborationController.class.getMethod("settings", Long.class);
        Method save = CollaborationController.class.getMethod("saveSettings", Long.class,
                com.ruoyi.business.domain.dto.CollaborationSettingsRequest.class);

        assertEquals("@ss.hasAnyRoles('admin,teacher')", settings.getAnnotation(PreAuthorize.class).value());
        assertEquals("@ss.hasAnyRoles('admin,teacher')", save.getAnnotation(PreAuthorize.class).value());
    }
}
