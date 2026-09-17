package com.ruoyi.business.controller;

import java.lang.reflect.Method;

import com.ruoyi.business.domain.dto.GuideSheetAiGenerateRequest;
import com.ruoyi.business.domain.dto.GuideSheetBeginnerAssembleRequest;
import com.ruoyi.business.domain.dto.GuideSheetDraftSaveRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuideSheetDraftControllerTest
{
    @Test
    void writeEndpointsRequireGuideSheetAuthoringPermission() throws Exception
    {
        assertAuthoringPermission(method("save", GuideSheetDraftSaveRequest.class));
        assertAuthoringPermission(method("assemble", GuideSheetBeginnerAssembleRequest.class));
        assertAuthoringPermission(method("generateContent", GuideSheetAiGenerateRequest.class));
    }

    @Test
    void endpointPathsMatchBeginnerFrontendContract() throws Exception
    {
        PutMapping save = method("save", GuideSheetDraftSaveRequest.class).getAnnotation(PutMapping.class);
        PostMapping assemble = method("assemble", GuideSheetBeginnerAssembleRequest.class)
                .getAnnotation(PostMapping.class);
        PostMapping ai = method("generateContent", GuideSheetAiGenerateRequest.class)
                .getAnnotation(PostMapping.class);

        assertArrayEquals(new String[] { "/draft" }, save.value());
        assertArrayEquals(new String[] { "/beginner/assemble" }, assemble.value());
        assertArrayEquals(new String[] { "/ai/generate" }, ai.value());
    }

    private Method method(String name, Class<?> argument) throws Exception
    {
        return GuideSheetDraftController.class.getMethod(name, argument);
    }

    private void assertAuthoringPermission(Method method)
    {
        PreAuthorize permission = method.getAnnotation(PreAuthorize.class);
        assertTrue(permission.value().contains("business:guideSheet:add"));
        assertTrue(permission.value().contains("business:guideSheet:edit"));
    }
}
