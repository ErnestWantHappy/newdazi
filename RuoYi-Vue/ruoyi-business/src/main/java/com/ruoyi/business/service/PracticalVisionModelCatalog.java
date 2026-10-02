package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.common.exception.ServiceException;

/** 操作题视觉批改的固定模型清单；服务端决定地址，教师不能输入任意 URL。 */
public final class PracticalVisionModelCatalog
{
    public static final String QWEN_ENDPOINT = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    public static final String DEEPSEEK_ENDPOINT = "https://api.deepseek.com/chat/completions";
    public static final String DOUBAO_ENDPOINT = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
    private static final Map<String, Map<String, String>> MODELS;

    static
    {
        Map<String, Map<String, String>> providers = new LinkedHashMap<>();
        Map<String, String> qwen = new LinkedHashMap<>();
        for (String model : new String[] {"qwen3.8-max", "qwen3.8-max-0902", "qwen3.6-flash", "qwen3.7-plus"})
            qwen.put(model, QWEN_ENDPOINT);
        providers.put("QWEN", Collections.unmodifiableMap(qwen));
        providers.put("DEEPSEEK", Collections.singletonMap("deepseek-flash", DEEPSEEK_ENDPOINT));
        providers.put("DOUBAO", Collections.singletonMap("doubao-seed-2.1-pro", DOUBAO_ENDPOINT));
        MODELS = Collections.unmodifiableMap(providers);
    }

    private PracticalVisionModelCatalog() { }

    public static void assertProvider(String providerCode)
    {
        if (!MODELS.containsKey(providerCode)) throw new ServiceException("暂不支持该模型厂商");
    }

    public static String endpoint(String providerCode, String modelName)
    {
        Map<String, String> models = MODELS.get(providerCode);
        String endpoint = models == null ? null : models.get(modelName);
        if (endpoint == null) throw new ServiceException("暂不支持该厂商或视觉模型");
        return endpoint;
    }

    public static List<Map<String, String>> options()
    {
        List<Map<String, String>> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> provider : MODELS.entrySet())
            for (String model : provider.getValue().keySet())
            {
                Map<String, String> option = new LinkedHashMap<>();
                option.put("providerCode", provider.getKey());
                option.put("modelName", model);
                result.add(option);
            }
        return result;
    }
}
