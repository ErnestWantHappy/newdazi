package com.ruoyi.business.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.config.CryptPadProperties;

class CryptPadAdapterTest
{
    @Test
    void missingExternalConfigurationIsNotReady()
    {
        CryptPadAdapter adapter = new CryptPadAdapter();
        CryptPadProperties properties = new CryptPadProperties();
        ReflectionTestUtils.setField(adapter, "properties", properties);
        ReflectionTestUtils.setField(adapter, "secretService", new com.ruoyi.business.service.CollaborationSecretService());
        assertFalse(adapter.ready());
        assertEquals("CRYPTPAD", adapter.health().get("provider"));
    }

    @Test
    void validExternalConfigurationIsReady()
    {
        CryptPadAdapter adapter = new CryptPadAdapter();
        CryptPadProperties properties = new CryptPadProperties();
        properties.setBaseUrl("https://office.xsedu.net.cn");
        properties.setApiUrl("https://office.xsedu.net.cn/cryptpad-api.js");
        properties.setKeySecret("cryptpad-test-secret-at-least-32-characters");
        properties.setRemoteEmbedding(true);
        ReflectionTestUtils.setField(adapter, "properties", properties);
        ReflectionTestUtils.setField(adapter, "secretService", new com.ruoyi.business.service.CollaborationSecretService());
        assertTrue(adapter.ready());
    }
}
