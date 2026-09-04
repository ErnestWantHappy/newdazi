package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.exception.ServiceException;

class PracticalAiCipherServiceTest
{
    @Test
    void shouldEncryptWithRandomIvAndDecrypt()
    {
        PracticalAiCipherService service = service("this-is-a-local-test-master-key-only");
        String first = service.encrypt("teacher-api-key-test-1234");
        String second = service.encrypt("teacher-api-key-test-1234");
        assertNotEquals(first, second);
        assertEquals("teacher-api-key-test-1234", service.decrypt(first));
    }

    @Test
    void shouldRejectMissingOrWrongMasterKey()
    {
        PracticalAiCipherService missing = service("");
        assertThrows(ServiceException.class, () -> missing.encrypt("teacher-api-key-test-1234"));
        String ciphertext = service("master-key-number-one-for-tests").encrypt("teacher-api-key-test-1234");
        assertThrows(ServiceException.class, () -> service("different-master-key-for-tests").decrypt(ciphertext));
    }

    private PracticalAiCipherService service(String key)
    {
        PracticalAiCipherService service = new PracticalAiCipherService();
        ReflectionTestUtils.setField(service, "masterKey", key);
        return service;
    }
}
