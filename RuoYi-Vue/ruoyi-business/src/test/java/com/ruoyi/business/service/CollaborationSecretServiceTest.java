package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.exception.ServiceException;

class CollaborationSecretServiceTest
{
    private CollaborationSecretService service;

    @BeforeEach
    void setUp()
    {
        service = new CollaborationSecretService();
        ReflectionTestUtils.setField(service, "keySecret", "cryptpad-test-secret-at-least-32-characters");
    }

    @Test
    void encryptAndDecryptRoundTrip()
    {
        String plain = service.generateKey();
        String encrypted = service.encrypt(plain);
        assertNotEquals(plain, encrypted);
        assertEquals(plain, service.decrypt(encrypted));
    }

    @Test
    void tamperedCiphertextIsRejected()
    {
        String encrypted = service.encrypt("key");
        assertThrows(ServiceException.class, () -> service.decrypt(encrypted + "x"));
    }
}
