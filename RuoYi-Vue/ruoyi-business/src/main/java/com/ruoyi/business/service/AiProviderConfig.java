package com.ruoyi.business.service;

/**
 * AI 评分供应商配置
 * 支持 DeepSeek / 豆包(字节) / 千问(阿里) / 智谱清言 / 自定义
 *
 * @author ruoyi
 */
public enum AiProviderConfig {

    DEEPSEEK("deepseek", "https://api.deepseek.com/v1/chat/completions", "deepseek-chat"),
    DOUBAO("doubao", "https://ark.cn-beijing.volces.com/api/v3/chat/completions", "doubao-pro-32k"),
    QWEN("qwen", "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions", "qwen-plus"),
    ZHIPU("zhipu", "https://open.bigmodel.cn/api/paas/v4/chat/completions", "glm-4-flash"),
    CUSTOM("custom", null, null);

    private final String code;
    private final String defaultUrl;
    private final String defaultModel;

    AiProviderConfig(String code, String defaultUrl, String defaultModel) {
        this.code = code;
        this.defaultUrl = defaultUrl;
        this.defaultModel = defaultModel;
    }

    public String getCode() { return code; }
    public String getDefaultUrl() { return defaultUrl; }
    public String getDefaultModel() { return defaultModel; }

    /**
     * 根据 code 获取供应商，默认返回 DEEPSEEK
     */
    public static AiProviderConfig fromCode(String code) {
        if (code == null || code.isEmpty()) return DEEPSEEK;
        for (AiProviderConfig p : values()) {
            if (p.code.equalsIgnoreCase(code)) return p;
        }
        return DEEPSEEK;
    }

    /**
     * 获取实际 API URL（优先使用自定义 URL）
     */
    public String getApiUrl(String customUrl) {
        if (this == CUSTOM && customUrl != null && !customUrl.isEmpty()) {
            return customUrl;
        }
        return defaultUrl != null ? defaultUrl : customUrl;
    }

    /**
     * 获取实际模型名（优先使用自定义模型）
     */
    public String getModel(String customModel) {
        if (customModel != null && !customModel.isEmpty()) {
            return customModel;
        }
        return defaultModel;
    }
}