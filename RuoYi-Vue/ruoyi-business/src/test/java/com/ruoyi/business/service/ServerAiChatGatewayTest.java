package com.ruoyi.business.service;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.config.GuideSheetProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerAiChatGatewayTest
{
    private HttpServer server;

    @AfterEach
    void tearDown()
    {
        if (server != null)
        {
            server.stop(0);
        }
    }

    @Test
    void readsOnlyAssistantContentFromCompatibleApi() throws Exception
    {
        startServer(200, "{\"choices\":[{\"message\":{\"content\":\"生成的学习目标\"}}]}");
        ServerAiChatGateway gateway = new ServerAiChatGateway(properties(), new ObjectMapper());

        assertEquals("生成的学习目标", gateway.chat("生成目标", 300, 3000));
    }

    @Test
    void providerErrorsCollapseToGenericUnavailableMessage() throws Exception
    {
        startServer(500, "{\"error\":\"secret-key and provider details\"}");
        ServerAiChatGateway gateway = new ServerAiChatGateway(properties(), new ObjectMapper());

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> gateway.chat("生成目标", 300, 3000));

        assertEquals("AI 服务暂不可用", error.getMessage());
    }

    private void startServer(int status, String body) throws Exception
    {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat", exchange -> {
            byte[] response = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, response.length);
            try (OutputStream output = exchange.getResponseBody())
            {
                output.write(response);
            }
        });
        server.start();
    }

    private GuideSheetProperties properties()
    {
        GuideSheetProperties properties = new GuideSheetProperties();
        properties.getAi().setProvider("custom");
        properties.getAi().setApiKey("server-only-key");
        properties.getAi().setModel("test-model");
        properties.getAi().setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/chat");
        return properties;
    }
}
