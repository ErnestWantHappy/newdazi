package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.config.WpsWebOfficeProperties;
import com.ruoyi.common.config.RuoYiConfig;

class CollaborationRoomServiceHealthTest
{
    @TempDir
    Path tempDir;

    private CollaborationRoomService service;
    private WpsWebOfficeProperties properties;

    @BeforeEach
    void setUp()
    {
        new RuoYiConfig().setProfile(tempDir.toString());
        properties = new WpsWebOfficeProperties();
        properties.setAppId("test-app-id");
        properties.setAppSecret("test-app-secret");
        properties.setTokenSecret("test-token-secret-at-least-32-characters");
        properties.setSdkUrl("/weboffice/sdk/web-office-sdk-v1.1.27.umd.js");
        service = new CollaborationRoomService();
        ReflectionTestUtils.setField(service, "enabled", true);
        ReflectionTestUtils.setField(service, "properties", properties);
    }

    @Test
    void privateCallbackAddressIsRejectedWithActionableProblem()
    {
        properties.setPublicBaseUrl("http://10.52.1.123");

        Map<String, Object> health = service.health();
        @SuppressWarnings("unchecked")
        List<String> problems = (List<String>) health.get("problems");

        assertFalse((Boolean) health.get("ready"));
        assertFalse((Boolean) health.get("publicBaseUrlLooksPublic"));
        assertTrue(problems.stream().anyMatch(problem -> problem.contains("内网地址")
                && problem.contains("公网 DNS") && problem.contains("NAT")));
    }
}
