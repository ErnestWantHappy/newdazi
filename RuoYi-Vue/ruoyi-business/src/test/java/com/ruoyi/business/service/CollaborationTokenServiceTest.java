package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.config.WpsWebOfficeProperties;
import com.ruoyi.common.exception.ServiceException;

class CollaborationTokenServiceTest
{
    private WpsWebOfficeProperties properties;
    private CollaborationTokenService service;

    @BeforeEach
    void setUp()
    {
        properties = new WpsWebOfficeProperties();
        properties.setTokenSecret("test-token-secret-at-least-32-characters");
        properties.setTokenMinutes(10);
        service = new CollaborationTokenService();
        ReflectionTestUtils.setField(service, "properties", properties);
    }

    @Test
    void tokenCarriesUserRoomAndScope()
    {
        CollaborationTokenService.Claims claims = service.verify(service.issue(12L, 34L, "STUDENT"));
        assertEquals(12L, claims.getUserId());
        assertEquals(34L, claims.getRoomId());
        assertEquals("STUDENT", claims.getScope());
    }

    @Test
    void tamperedTokenIsRejected()
    {
        String token = service.issue(12L, 34L, "STUDENT");
        assertThrows(ServiceException.class, () -> service.verify(token + "x"));
    }

    @Test
    void expiredTokenIsRejected()
    {
        properties.setTokenMinutes(-1);
        assertThrows(ServiceException.class, () -> service.verify(service.issue(12L, 34L, "STUDENT")));
    }
}

