package com.ruoyi.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class PracticalAttachmentControllerSecurityTest
{
    @Test
    void attachmentEndpointsUseStudentProfileFallback()
    {
        PreAuthorize authorization = PracticalAttachmentController.class.getAnnotation(PreAuthorize.class);

        // 历史学生账号可能缺少 sys_role，不能用严格角色判断把本人作品预览和下载挡在外面。
        assertEquals("@studentSs.isStudent() or @ss.hasRole('teacher') or @ss.hasRole('admin')",
                authorization.value());
    }
}
