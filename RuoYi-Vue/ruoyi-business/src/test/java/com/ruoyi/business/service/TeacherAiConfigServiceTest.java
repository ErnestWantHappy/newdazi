package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.domain.TeacherAiProviderKey;
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
        TeacherAiProviderKey saved = new TeacherAiProviderKey();
        saved.setApiKeyCiphertext("existing-ciphertext");
        saved.setApiKeyHint("****test");
        when(mapper.selectProviderKey(104L, "QWEN")).thenReturn(saved);
        TeacherAiConfigRequest request = new TeacherAiConfigRequest();
        request.setApiKey("  "); request.setModelName("qwen3.8-max-0902");
        service.save(104L, request);
        assertEquals("existing-ciphertext", existing.getApiKeyCiphertext());
        assertEquals("****test", existing.getApiKeyHint());
        assertEquals("qwen3.8-max-0902", existing.getModelName());
        verify(mapper).upsertConfig(existing);
        verify(mapper).upsertProviderKey(saved);
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
        verify(mapper).upsertProviderKey(argThat(key -> "QWEN".equals(key.getProviderCode())
                && "new-ciphertext".equals(key.getApiKeyCiphertext())));
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

    @Test void switchingProviderReusesItsOwnKeyAndUsesFixedEndpoint()
    {
        TeacherAiConfig existing = new TeacherAiConfig();
        existing.setProviderCode("QWEN");
        existing.setApiKeyCiphertext("qwen-ciphertext");
        when(mapper.selectConfigForUpdate(104L)).thenReturn(existing);
        TeacherAiConfigRequest request = new TeacherAiConfigRequest();
        request.setProviderCode("DEEPSEEK");
        request.setModelName("deepseek-flash");
        assertThrows(ServiceException.class, () -> service.save(104L, request));
        TeacherAiProviderKey saved = new TeacherAiProviderKey();
        saved.setApiKeyCiphertext("deepseek-ciphertext");
        saved.setApiKeyHint("****deep");
        when(mapper.selectProviderKey(104L, "DEEPSEEK")).thenReturn(saved);
        service.save(104L, request);
        assertEquals(PracticalVisionModelCatalog.DEEPSEEK_ENDPOINT, existing.getEndpointUrl());
        assertEquals("deepseek-ciphertext", existing.getApiKeyCiphertext());
        assertEquals("DEEPSEEK", existing.getProviderCode());
        verifyNoInteractions(cipher);
    }

    @Test void jobKeepsItsProviderAfterActiveSelectionChanges()
    {
        TeacherAiProviderKey saved = new TeacherAiProviderKey();
        saved.setApiKeyCiphertext("old-provider-key");
        when(mapper.selectProviderKey(104L, "QWEN")).thenReturn(saved);
        TeacherAiConfig jobConfig = service.configForJob(104L, "QWEN", "qwen3.8-max");
        assertEquals("old-provider-key", jobConfig.getApiKeyCiphertext());
        assertEquals("QWEN", jobConfig.getProviderCode());
        assertEquals("qwen3.8-max", jobConfig.getModelName());
    }

    @Test void deletingProviderBlocksItsOldJobs()
    {
        assertThrows(ServiceException.class, () -> service.configForJob(104L, "DEEPSEEK", "deepseek-flash"));
        service.deleteProvider(104L, "DEEPSEEK");
        verify(mapper).deleteProviderKey(104L, "DEEPSEEK");
    }
}
