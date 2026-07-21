package com.ruoyi.business.service;

/**
 * 服务端 AI 对话网关，调用方不得把供应商配置或异常明细返回给浏览器。
 */
public interface AiChatGateway
{
    boolean isConfigured();

    String chat(String prompt, int maxTokens, int readTimeoutMillis);
}
