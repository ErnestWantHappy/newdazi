package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.domain.dto.TeacherAiConfigRequest;
import com.ruoyi.business.mapper.PracticalAiGradingMapper;
import com.ruoyi.common.exception.ServiceException;

class TeacherAiConfigServiceTest
{
    private final PracticalAiGradingMapper mapper = mock(PracticalAiGradingMapper.class);
    private final PracticalAiCipherService cipher = mock(PracticalAiCipherService.class);
    private final TeacherAiConfigService service = new TeacherAiConfigService();

    @BeforeEach void setup()
    {
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ReflectionTestUtils.setField(service, "cipherService", cipher);
    }

    @Test void switchingModelPreservesEncryptedKey()
    {
        TeacherAiConfig existing = new TeacherAiConfig();
        existing.setApiKeyCiphertext("existing-ciphertext");
        existing.setApiKeyHint("****test");
        when(mapper.selectConfigForUpdate(104L)).thenReturn(existing);
        TeacherAiConfigRequest request = new TeacherAiConfigRequest();
        request.setApiKey("  "); request.setModelName("qwen3.8-max-0902");
        service.save(104L, request);
        assertEquals("existing-ciphertext", existing.getApiKeyCiphertext());
        assertEquals("****test", existing.getApiKeyHint());
        assertEquals("qwen3.8-max-0902", existing.getModelName());
        verify(mapper).upsertConfig(existing);
        verifyNoInteractions(cipher);
    }

    @Test void firstConfigurationRequiresKey()
    {
        assertThrows(ServiceException.class, () -> service.save(104L, new TeacherAiConfigRequest()));
        verify(mapper, never()).upsertConfig(any());
    }

    @Test void newKeyIsEncryptedAndMissingModelDefaultsToMax()
    {
        TeacherAiConfigRequest request = new TeacherAiConfigRequest();
        request.setApiKey("synthetic-test-key");
        when(cipher.encrypt("synthetic-test-key")).thenReturn("new-ciphertext");
        service.save(104L, request);
        verify(mapper).upsertConfig(argThat(c -> "qwen3.8-max".equals(c.getModelName())
                && "new-ciphertext".equals(c.getApiKeyCiphertext())));
    }

    @Test void invalidModelCannotWriteConfig()
    {
        TeacherAiConfigRequest request = new TeacherAiConfigRequest();
        request.setModelName("unsupported");
        assertThrows(ServiceException.class, () -> service.save(104L, request));
        verify(mapper, never()).upsertConfig(any());
    }
}
