package com.ruoyi.business.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.AiModelPrice;
import com.ruoyi.business.domain.PracticalAiJob;
import com.ruoyi.business.domain.PracticalAiResult;
import com.ruoyi.business.domain.dto.AiModelPriceRequest;
import com.ruoyi.business.mapper.AiModelPriceMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/** 模型参考价、单次成本估算和任务用量汇总。 */
@Service
public class AiModelPricingService
{
    private static final String PROVIDER_QWEN = "QWEN";
    private static final long FALLBACK_PROMPT_TOKENS = 4564L;
    private static final long FALLBACK_COMPLETION_TOKENS = 450L;
    @Autowired private AiModelPriceMapper mapper;

    public List<AiModelPrice> list()
    {
        List<AiModelPrice> prices = mapper.selectAll();
        prices = prices == null ? new ArrayList<AiModelPrice>() : new ArrayList<AiModelPrice>(prices);
        for (String model : new String[] {"qwen3.8-max", "qwen3.8-max-0902"})
        {
            if (prices.stream().noneMatch(p -> PROVIDER_QWEN.equals(p.getProviderCode()) && model.equals(p.getModelName())))
                prices.add(referenceMaxPrice(model));
        }
        for (Map<String, String> option : PracticalVisionModelCatalog.options())
        {
            String provider = option.get("providerCode"), model = option.get("modelName");
            if (!PROVIDER_QWEN.equals(provider) && prices.stream().noneMatch(p ->
                    provider.equals(p.getProviderCode()) && model.equals(p.getModelName())))
                prices.add(unpricedModel(provider, model));
        }
        return prices;
    }

    public AiModelPrice require(String providerCode, String modelName)
    {
        AiModelPrice price = mapper.selectByModel(providerCode, modelName);
        if (price == null)
        {
            if (PROVIDER_QWEN.equals(providerCode) && ("qwen3.8-max".equals(modelName)
                    || "qwen3.8-max-0902".equals(modelName))) return referenceMaxPrice(modelName);
            PracticalVisionModelCatalog.endpoint(providerCode, modelName);
            return unpricedModel(providerCode, modelName);
        }
        return price;
    }

    public AiModelPrice update(String modelName, AiModelPriceRequest request, String username)
    {
        return update(PROVIDER_QWEN, modelName, request, username);
    }

    public AiModelPrice update(String providerCode, String modelName, AiModelPriceRequest request, String username)
    {
        PracticalVisionModelCatalog.endpoint(providerCode, modelName);
        AiModelPrice current = require(providerCode, modelName);
        if (request == null || invalidPrice(request.getInputPricePerThousand())
                || invalidPrice(request.getOutputPricePerThousand()))
        {
            throw new ServiceException("模型输入价和输出价必须是0到100之间的数字");
        }
        String status = StringUtils.trim(request.getPriceStatus());
        if (!"REFERENCE".equals(status) && !"TO_CONFIRM".equals(status) && !"CONFIRMED".equals(status))
        {
            throw new ServiceException("价格状态无效");
        }
        current.setInputPricePerThousand(request.getInputPricePerThousand().setScale(6, RoundingMode.HALF_UP));
        current.setOutputPricePerThousand(request.getOutputPricePerThousand().setScale(6, RoundingMode.HALF_UP));
        current.setPriceStatus(status);
        current.setPriceNote(truncate(StringUtils.trim(request.getPriceNote()), 255));
        current.setUpdateBy(username);
        mapper.upsert(current);
        return require(providerCode, modelName);
    }

    public Map<String, Object> describe(String providerCode, String modelName)
    {
        AiModelPrice price = require(providerCode, modelName);
        Map<String, Object> average = mapper.selectAverageUsageByModel(providerCode, modelName);
        long sampleCount = number(average, "sampleCount", 0L);
        long promptTokens = sampleCount > 0 ? number(average, "avgPromptTokens", FALLBACK_PROMPT_TOKENS)
                : FALLBACK_PROMPT_TOKENS;
        long completionTokens = sampleCount > 0
                ? number(average, "avgCompletionTokens", FALLBACK_COMPLETION_TOKENS)
                : FALLBACK_COMPLETION_TOKENS;
        Map<String, Object> result = priceMap(price);
        result.put("estimatedPromptTokensPerGrading", promptTokens);
        result.put("estimatedCompletionTokensPerGrading", completionTokens);
        result.put("estimatedCostPerGradingYuan", cost(promptTokens, completionTokens,
                price.getInputPricePerThousand(), price.getOutputPricePerThousand()));
        result.put("sampleCount", sampleCount);
        result.put("usageEstimateSource", sampleCount > 0 ? "MODEL_HISTORY" : "REFERENCE_BASELINE");
        return result;
    }

    public Map<String, Object> usage(PracticalAiJob job, List<PracticalAiResult> results)
    {
        long promptTokens = 0L;
        long completionTokens = 0L;
        if (results != null)
        {
            for (PracticalAiResult item : results)
            {
                if (item.getPromptTokens() != null) promptTokens += item.getPromptTokens();
                if (item.getCompletionTokens() != null) completionTokens += item.getCompletionTokens();
            }
        }
        BigDecimal inputPrice = job.getInputPricePerThousand();
        BigDecimal outputPrice = job.getOutputPricePerThousand();
        String source = "JOB_SNAPSHOT";
        AiModelPrice current = null;
        if (inputPrice == null || outputPrice == null)
        {
            current = require(job.getProviderCode(), job.getModelName());
            inputPrice = current.getInputPricePerThousand();
            outputPrice = current.getOutputPricePerThousand();
            source = "CURRENT_REFERENCE";
        }
        Map<String, Object> usage = new LinkedHashMap<String, Object>();
        usage.put("promptTokens", promptTokens);
        usage.put("completionTokens", completionTokens);
        usage.put("totalTokens", promptTokens + completionTokens);
        usage.put("inputPricePerThousand", inputPrice);
        usage.put("outputPricePerThousand", outputPrice);
        usage.put("estimatedCostYuan", cost(promptTokens, completionTokens, inputPrice, outputPrice));
        usage.put("priceSource", source);
        usage.put("priceStatus", job.getPriceStatus() != null ? job.getPriceStatus()
                : current == null ? null : current.getPriceStatus());
        usage.put("priceNote", job.getPriceNote() != null ? job.getPriceNote()
                : current == null ? null : current.getPriceNote());
        usage.put("disclaimer", "费用估算仅供参考，实际以所选模型厂商账单为准");
        return usage;
    }

    public Map<String, Object> priceMap(AiModelPrice price)
    {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("providerCode", price.getProviderCode());
        result.put("modelName", price.getModelName());
        result.put("displayName", price.getDisplayName());
        result.put("inputPricePerThousand", price.getInputPricePerThousand());
        result.put("outputPricePerThousand", price.getOutputPricePerThousand());
        result.put("priceStatus", price.getPriceStatus());
        result.put("priceNote", price.getPriceNote());
        result.put("updateTime", price.getUpdateTime());
        return result;
    }

    BigDecimal cost(long promptTokens, long completionTokens, BigDecimal inputPrice, BigDecimal outputPrice)
    {
        if (inputPrice == null || outputPrice == null) return null;
        return inputPrice.multiply(BigDecimal.valueOf(promptTokens))
                .add(outputPrice.multiply(BigDecimal.valueOf(completionTokens)))
                .divide(BigDecimal.valueOf(1000L), 6, RoundingMode.HALF_UP);
    }

    private AiModelPrice referenceMaxPrice(String model)
    {
        // 官网按百万 token 报价，内部沿用千 token 单位；管理员已保存的价格优先。
        AiModelPrice price = new AiModelPrice();
        price.setProviderCode(PROVIDER_QWEN); price.setModelName(model);
        price.setDisplayName("qwen3.8-max".equals(model) ? "Qwen3.8-Max" : "Qwen3.8-Max-0902");
        price.setInputPricePerThousand(new BigDecimal("0.012000"));
        price.setOutputPricePerThousand(new BigDecimal("0.036000"));
        price.setPriceStatus("REFERENCE");
        price.setPriceNote("2026-09-17百炼官网北京地域参考价：0<Token≤1M，输入12元/百万、输出36元/百万；未扣缓存、Batch或免费额度；纸质识别和评分分别计费，实际以账单为准");
        return price;
    }

    private AiModelPrice unpricedModel(String provider, String model)
    {
        AiModelPrice price = new AiModelPrice();
        price.setProviderCode(provider);
        price.setModelName(model);
        price.setDisplayName(model);
        price.setPriceStatus("TO_CONFIRM");
        price.setPriceNote("单价和免费额度须在所选厂商账户中核实；尚未记录参考价，不按零元估算");
        return price;
    }

    private boolean invalidPrice(BigDecimal value)
    {
        return value == null || value.compareTo(BigDecimal.ZERO) < 0
                || value.compareTo(BigDecimal.valueOf(100L)) > 0;
    }

    private long number(Map<String, Object> values, String key, long fallback)
    {
        Object value = values == null ? null : values.get(key);
        return value instanceof Number ? ((Number) value).longValue() : fallback;
    }

    private String truncate(String value, int max)
    {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
