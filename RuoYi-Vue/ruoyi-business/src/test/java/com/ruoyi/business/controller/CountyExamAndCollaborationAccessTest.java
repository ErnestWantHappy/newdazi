package com.ruoyi.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 协作健康检查会暴露内网主机与环境变量名，不能向学生开放；区域抽测上传归属按整场考试保留。
 */
class CountyExamAndCollaborationAccessTest {
    @Test
    void collaborationHealthIsRestrictedToTeacherAndAdmin() throws Exception {
        PreAuthorize allowed = CollaborationController.class.getMethod("health").getAnnotation(PreAuthorize.class);
        assertNotNull(allowed);
        assertEquals("@ss.hasAnyRoles('admin,teacher')", allowed.value());
    }

    @Test
    void countyExamUploadOwnershipCoversWholeExam() {
        assertEquals(Integer.valueOf(24), CountyExamController.UPLOAD_OWNER_TTL_HOURS);
        assertTrue(CountyExamController.UPLOAD_OWNER_TTL_HOURS > 1L);
    }
}
