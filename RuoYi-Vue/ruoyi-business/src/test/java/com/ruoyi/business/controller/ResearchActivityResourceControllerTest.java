package com.ruoyi.business.controller;

import com.ruoyi.business.service.ResearchActivityService;
import com.ruoyi.common.core.domain.AjaxResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResearchActivityResourceControllerTest
{
    @Mock
    private ResearchActivityService service;

    @InjectMocks
    private ResearchActivityResourceController controller;

    @Test
    void uploadImageReturnsAuthenticatedPreviewUrl()
    {
        MockMultipartFile file = new MockMultipartFile("file", "课堂照片.png", "image/png", new byte[]{1});
        String storedPath = "/profile/upload/research-activity/images/2026/07/23/example.png";
        when(service.uploadImage(file)).thenReturn(storedPath);

        AjaxResult result = controller.uploadImage(file);

        assertEquals(storedPath, result.get("fileName"));
        assertEquals("/common/resource/view?resource=" + storedPath, result.get("url"));
    }
}
