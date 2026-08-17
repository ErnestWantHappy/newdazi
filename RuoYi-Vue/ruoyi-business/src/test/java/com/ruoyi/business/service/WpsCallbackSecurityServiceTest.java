package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.config.WpsWebOfficeProperties;
import com.ruoyi.common.exception.ServiceException;

class WpsCallbackSecurityServiceTest
{
    private WpsWebOfficeProperties properties;
    private CollaborationTokenService tokenService;
    private WpsCallbackSecurityService securityService;

    @BeforeEach
    void setUp()
    {
        properties = new WpsWebOfficeProperties();
        properties.setAppId("test-app-id");
        properties.setAppSecret("test-app-secret");
        properties.setTokenSecret("test-token-secret-at-least-32-characters");
        tokenService = new CollaborationTokenService();
        ReflectionTestUtils.setField(tokenService, "properties", properties);
        securityService = new WpsCallbackSecurityService();
        ReflectionTestUtils.setField(securityService, "properties", properties);
        ReflectionTestUtils.setField(securityService, "tokenService", tokenService);
        ReflectionTestUtils.setField(securityService, "enabled", true);
    }

    @Test
    void verifiesOfficialGetSignatureRule()
    {
        String uri = "/weboffice/callback/v3/3rd/files/c123";
        MockHttpServletRequest request = signedRequest("GET", uri, null, "");
        CollaborationTokenService.Claims claims = securityService.verify(request, new byte[0]);
        assertEquals(9L, claims.getUserId());
        assertEquals(7L, claims.getRoomId());
    }

    @Test
    void verifiesJsonBodyMd5WithoutReformatting()
    {
        byte[] body = "{\"name\":\"课堂.xlsx\",\"size\":123}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = signedRequest("POST",
                "/weboffice/callback/v3/3rd/files/c123/upload/address", body, "application/json");
        assertEquals(7L, securityService.verify(request, body).getRoomId());
    }

    @Test
    void rejectsInvalidSignature()
    {
        MockHttpServletRequest request = signedRequest("GET",
                "/weboffice/callback/v3/3rd/files/c123", null, "");
        request.removeHeader("Authorization");
        request.addHeader("Authorization", "WPS-2:test-app-id:bad");
        assertThrows(ServiceException.class, () -> securityService.verify(request, new byte[0]));
    }

    @Test
    void rejectsCallbacksWhenProviderIsDisabled()
    {
        ReflectionTestUtils.setField(securityService, "enabled", false);
        MockHttpServletRequest request = signedRequest("GET",
                "/weboffice/callback/v3/3rd/files/c123", null, "");
        ServiceException error = assertThrows(ServiceException.class,
                () -> securityService.verify(request, new byte[0]));
        assertEquals("在线协作服务已停用", error.getMessage());
    }

    private MockHttpServletRequest signedRequest(String method, String uri, byte[] body, String contentType)
    {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        String date = ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        String contentMd5 = body != null && body.length > 0
                ? DigestUtils.md5Hex(body)
                : DigestUtils.md5Hex(uri.getBytes(StandardCharsets.UTF_8));
        String sha1 = DigestUtils.sha1Hex(properties.getAppSecret() + contentMd5 + contentType + date);
        request.addHeader("Date", date);
        request.addHeader("X-App-Id", properties.getAppId());
        request.addHeader("X-Weboffice-Token", tokenService.issue(9L, 7L, "STUDENT"));
        request.addHeader("Authorization", "WPS-2:" + properties.getAppId() + ":" + sha1);
        if (!contentType.isEmpty()) request.setContentType(contentType);
        return request;
    }
}
