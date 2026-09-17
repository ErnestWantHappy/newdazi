package com.ruoyi.business.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class IotMqttPropertiesTest
{
    @Test
    void shouldEnableSharedDeviceCredentialOnlyWhenBothValuesExist()
    {
        IotMqttProperties properties = new IotMqttProperties();
        properties.setDeviceUsername("shared-device");
        assertFalse(properties.useSharedDeviceCredential());

        properties.setDevicePassword("not-logged-test-value");
        assertTrue(properties.useSharedDeviceCredential());
    }
}
