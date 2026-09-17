package com.ruoyi.business.controller;

import com.ruoyi.business.config.GuideSheetProperties;
import com.ruoyi.business.service.AiGradingService;
import com.ruoyi.common.core.domain.AjaxResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuideSheetControllerSecurityTest
{
    @Mock
    private AiGradingService aiGradingService;

    @Mock
    private GuideSheetProperties guideSheetProperties;

    @InjectMocks
    private GuideSheetController controller;

    @Test
    void capabilitiesRequiresGuideSheetListPermission() throws Exception
    {
        Method method = GuideSheetController.class.getMethod("getCapabilities");
        PreAuthorize authorization = method.getAnnotation(PreAuthorize.class);

        assertEquals("@ss.hasPermi('business:guideSheet:list')", authorization.value());
    }

    @Test
    void capabilitiesDoesNotExposeTeacherHelperPort()
    {
        GuideSheetProperties.TeacherHelper teacherHelper = new GuideSheetProperties.TeacherHelper();
        teacherHelper.setEnabled(true);
        teacherHelper.setPort(5000);
        when(aiGradingService.isConfigured()).thenReturn(true);
        when(guideSheetProperties.getTeacherHelper()).thenReturn(teacherHelper);

        AjaxResult result = controller.getCapabilities();

        assertEquals(Boolean.TRUE, result.get("aiConfigured"));
        assertEquals(Boolean.TRUE, result.get("teacherHelperEnabled"));
        assertFalse(result.containsKey("teacherHelperPort"));
    }
}
