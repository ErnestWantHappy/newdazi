package com.ruoyi.business.service;

import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.domain.dto.PracticalInsightV3.Input;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PracticalInsightProviderSnapshotTest
{
    @Test void resumedReportUsesFrozenProviderAndModel()
    {
        TeacherAiConfigService configs = mock(TeacherAiConfigService.class);
        PracticalInsightAiService service = new PracticalInsightAiService();
        ReflectionTestUtils.setField(service, "configs", configs);
        Input input = new Input();
        input.teacherId = 104L;
        input.provider = "DEEPSEEK";
        input.model = "deepseek-flash";
        TeacherAiConfig frozen = new TeacherAiConfig();
        when(configs.configForJob(104L, "DEEPSEEK", "deepseek-flash")).thenReturn(frozen);

        assertSame(frozen, service.configForInput(input));
        verify(configs).configForJob(104L, "DEEPSEEK", "deepseek-flash");
        verifyNoMoreInteractions(configs);
    }

    @Test void oldReportWithoutProviderKeepsQwen()
    {
        TeacherAiConfigService configs = mock(TeacherAiConfigService.class);
        PracticalInsightAiService service = new PracticalInsightAiService();
        ReflectionTestUtils.setField(service, "configs", configs);
        Input input = new Input();
        input.teacherId = 104L;
        input.model = "qwen3.8-max";
        service.configForInput(input);
        verify(configs).configForJob(104L, "QWEN", "qwen3.8-max");
        verifyNoMoreInteractions(configs);
    }
}
