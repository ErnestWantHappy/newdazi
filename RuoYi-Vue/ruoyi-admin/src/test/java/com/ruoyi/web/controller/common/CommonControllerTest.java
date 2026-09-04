package com.ruoyi.web.controller.common;

import com.ruoyi.common.config.RuoYiConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonControllerTest
{
    private final CommonController controller = new CommonController();
    private final RuoYiConfig config = new RuoYiConfig();
    private String originalProfile;

    @BeforeEach
    void setUp()
    {
        originalProfile = RuoYiConfig.getProfile();
        config.setProfile("D:/ruoyi-profile");
    }

    @AfterEach
    void tearDown()
    {
        config.setProfile(originalProfile);
    }

    @Test
    void privateGuideSheetResourcesCannotBypassCommonResourceEndpoints()
    {
        assertTrue(controller.isPrivateGuideSheetResource("/profile/upload/guide-sheet/answer/a.pdf"));
        assertTrue(controller.isPrivateGuideSheetResource("//profile//upload/./guide-sheet/a.pdf"));
        assertTrue(controller.isPrivateGuideSheetResource("x/profile/upload/guide-sheet/a.pdf"));
        assertTrue(controller.isPrivateGuideSheetResource("\\PROFILE\\UPLOAD\\GUIDE-SHEET\\a.pdf"));
        assertTrue(controller.isPrivateGuideSheetResource("%2Fprofile%2Fupload%2Fguide-sheet%2Fa.pdf"));
        assertTrue(controller.isPrivateGuideSheetResource("%252Fprofile%252Fupload%252Fguide-sheet%252Fa.pdf"));
    }

    @Test
    void ordinaryProfileResourcesRemainAvailable()
    {
        assertFalse(controller.isPrivateGuideSheetResource("/profile/upload/2026/07/material.pdf"));
        assertFalse(controller.isPrivateGuideSheetResource("/profile/avatar/teacher.png"));
        assertFalse(controller.isPrivateGuideSheetResource("/download/report.xlsx"));
    }

    @Test
    void researchActivityWebpUsesScopedPreviewAllowance()
    {
        assertTrue(controller.isAllowedPreviewResource(
                "/profile/upload/research-activity/images/2026/07/23/example.webp"));
        assertFalse(controller.isAllowedPreviewResource(
                "/profile/upload/research-activity/images/../../secret.webp"));
        assertFalse(controller.isAllowedPreviewResource("/profile/upload/other/example.webp"));
    }
}
