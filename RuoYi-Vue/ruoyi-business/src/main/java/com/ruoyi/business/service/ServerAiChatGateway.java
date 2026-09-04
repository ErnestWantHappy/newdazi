package com.ruoyi.business.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.config.GuideSheetProperties;
import org.springframework.stereotype.Service;

/**
 * 统一封装兼容 OpenAI Chat Completions 协议的服务端调用。
 */
@Service
public class ServerAiChatGateway implements AiChatGateway
{
    private static final int MAX_PROMPT_LENGTH = 20000;
    private static final int CONNECT_TIMEOUT_MILLIS = 10000;

    private final GuideSheetProperties properties;
    private final ObjectMapper objectMapper;

    public ServerAiChatGateway(GuideSheetProperties properties, ObjectMapper objectMapper)
    {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isConfigured()
    {
        return properties.getAi().isConfigured();
    }

    @Override
    public String chat(String prompt, int maxTokens, int readTimeoutMillis)
    {
        if (!isConfigured() || prompt == null || prompt.trim().isEmpty()
                || prompt.length() > MAX_PROMPT_LENGTH)
        {
            throw unavailable();
        }

        GuideSheetProperties.Ai config = properties.getAi();
        AiProviderConfig provider = AiProviderConfig.fromCode(config.getProvider());
        String apiUrl = provider.getApiUrl(config.getBaseUrl());
        String model = provider.getModel(config.getModel());
        if (apiUrl == null || apiUrl.trim().isEmpty() || model == null || model.trim().isEmpty())
        {
            throw unavailable();
        }

        HttpURLConnection connection = null;
        try
        {
            connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            connection.setDoOutput(true);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
            connection.setReadTimeout(Math.max(1000, readTimeoutMillis));

            byte[] requestBody = createRequestBody(model, prompt, maxTokens)
                    .getBytes(StandardCharsets.UTF_8);
            try (OutputStream output = connection.getOutputStream())
            {
                output.write(requestBody);
            }

            int statusCode = connection.getResponseCode();
            if (statusCode < 200 || statusCode >= 300)
            {
                closeQuietly(connection.getErrorStream());
                throw unavailable();
            }
            String responseBody = readBody(connection.getInputStream());
            JsonNode response = objectMapper.readTree(responseBody);
            String content = response.path("choices").path(0).path("message").path("content").asText("");
            if (content.trim().isEmpty())
            {
                throw unavailable();
            }
            return content.trim();
        }
        catch (RuntimeException e)
        {
            throw unavailable();
        }
        catch (Exception e)
        {
            throw unavailable();
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }
    }

    private String createRequestBody(String model, String prompt, int maxTokens) throws Exception
    {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        Map<String, String> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        body.put("messages", Collections.singletonList(message));
        body.put("temperature", 0.1D);
        body.put("max_tokens", Math.max(100, Math.min(maxTokens, 2000)));
        return objectMapper.writeValueAsString(body);
    }

    private String readBody(InputStream input) throws Exception
    {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8)))
        {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                body.append(line);
            }
            return body.toString();
        }
    }

    private void closeQuietly(InputStream input)
    {
        if (input == null)
        {
            return;
        }
        try
        {
            input.close();
        }
        catch (Exception ignored)
        {
            // 错误响应内容不参与业务，关闭失败无需覆盖原始降级结果。
        }
    }

    private IllegalStateException unavailable()
    {
        return new IllegalStateException("AI 服务暂不可用");
    }
}
