package com.ruoyi.business.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import com.ruoyi.business.domain.vo.ResearchResourceVo;
import static org.junit.jupiter.api.Assertions.*;

class ResearchActivityVoSerializationTest
{
    @Test
    void resourceResponseNeverExposesPrivateStoredPath() throws Exception
    {
        ResearchResourceVo resource = new ResearchResourceVo();
        resource.setResourceId(1L);
        resource.setStoredPath("9/30/private.zip");
        resource.setOriginalFileName("课件.zip");

        String json = new ObjectMapper().writeValueAsString(resource);
        assertFalse(json.contains("storedPath"));
        assertFalse(json.contains("private.zip"));
        assertTrue(json.contains("课件.zip"));
    }
}
