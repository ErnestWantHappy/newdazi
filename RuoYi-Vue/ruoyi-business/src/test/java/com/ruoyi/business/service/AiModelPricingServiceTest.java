package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.AiModelPrice;
import com.ruoyi.business.domain.PracticalAiJob;
import com.ruoyi.business.domain.PracticalAiResult;
import com.ruoyi.business.mapper.AiModelPriceMapper;

@ExtendWith(MockitoExtension.class)
class AiModelPricingServiceTest
{
    @Mock private AiModelPriceMapper mapper;
    private AiModelPricingService service;

    @BeforeEach
    void setUp()
    {
        service = new AiModelPricingService();
        ReflectionTestUtils.setField(service, "mapper", mapper);
    }

    @Test
    void shouldCalculateTheoreticalCostInYuan()
    {
        assertEquals(new BigDecimal("0.017742"), service.cost(4564L, 450L,
                new BigDecimal("0.003"), new BigDecimal("0.009")));
    }

    @Test
    void shouldUseJobPriceSnapshotForUsageSummary()
    {
        PracticalAiJob job = new PracticalAiJob();
        job.setProviderCode("QWEN"); job.setModelName("qwen3.7-plus");
        job.setInputPricePerThousand(new BigDecimal("0.003"));
        job.setOutputPricePerThousand(new BigDecimal("0.009"));
        job.setPriceStatus("TO_CONFIRM");
        PracticalAiResult first = new PracticalAiResult();
        first.setPromptTokens(100); first.setCompletionTokens(20);
        PracticalAiResult second = new PracticalAiResult();
        second.setPromptTokens(200); second.setCompletionTokens(30);

        Map<String, Object> usage = service.usage(job, Arrays.asList(first, second));

        assertEquals(300L, usage.get("promptTokens"));
        assertEquals(50L, usage.get("completionTokens"));
        assertEquals(350L, usage.get("totalTokens"));
        assertEquals(new BigDecimal("0.001350"), usage.get("estimatedCostYuan"));
        assertEquals("JOB_SNAPSHOT", usage.get("priceSource"));
    }

    @Test
    void shouldUseHistoricalAverageWhenSamplesExist()
    {
        AiModelPrice price = price();
        when(mapper.selectByModel("QWEN", "qwen3.7-plus")).thenReturn(price);
        Map<String, Object> average = new HashMap<String, Object>();
        average.put("avgPromptTokens", 5000L);
        average.put("avgCompletionTokens", 500L);
        average.put("sampleCount", 171L);
        when(mapper.selectAverageUsageByModel("QWEN", "qwen3.7-plus")).thenReturn(average);

        Map<String, Object> result = service.describe("QWEN", "qwen3.7-plus");

        assertEquals(171L, result.get("sampleCount"));
        assertEquals("MODEL_HISTORY", result.get("usageEstimateSource"));
        assertEquals(new BigDecimal("0.019500"), result.get("estimatedCostPerGradingYuan"));
    }

    private AiModelPrice price()
    {
        AiModelPrice price = new AiModelPrice();
        price.setProviderCode("QWEN"); price.setModelName("qwen3.7-plus");
        price.setDisplayName("Qwen3.7-Plus");
        price.setInputPricePerThousand(new BigDecimal("0.003"));
        price.setOutputPricePerThousand(new BigDecimal("0.009"));
        price.setPriceStatus("TO_CONFIRM");
        return price;
    }
}
