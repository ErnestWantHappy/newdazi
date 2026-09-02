package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.business.config.IotMqttProperties;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/** EMQX v5 管理接口契约测试，防止精确 ACL 请求格式再次退化。 */
class IotEmqxAdapterTest
{
    private HttpServer server;
    private IotEmqxAdapter adapter;
    private final AtomicReference<String> aclBody = new AtomicReference<>();

    @BeforeEach
    void setUp() throws Exception
    {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v5/authorization/sources", exchange -> respond(exchange, 200,
                "[{\"type\":\"built_in_database\",\"enable\":true}]"));
        server.createContext("/api/v5/authorization/sources/built_in_database/rules/users/class_139_2020_01", exchange ->
        {
            aclBody.set(readBody(exchange));
            respond(exchange, 204, "");
        });
        server.createContext("/api/v5/clients", exchange ->
        {
            if ("GET".equals(exchange.getRequestMethod()))
                respond(exchange, 200, "{\"data\":[{\"clientid\":\"old-client\"}]}");
            else respond(exchange, 405, "");
        });
        server.createContext("/api/v5/clients/old-client", exchange -> respond(exchange, 204, ""));
        server.start();

        IotMqttProperties properties = new IotMqttProperties();
        properties.setEmqxApiUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/api/v5");
        properties.setEmqxApiToken("test-token");
        adapter = new IotEmqxAdapter();
        ReflectionTestUtils.setField(adapter, "properties", properties);
    }

    @AfterEach
    void tearDown()
    {
        if (server != null) server.stop(0);
    }

    @Test
    void shouldDetectBuiltInAuthorizationAndSendUsernameWithExactRule()
    {
        assertTrue(adapter.isBuiltInAuthorizationReady());
        assertTrue(adapter.syncClassAcl("class_139_2020_01", "county/139/252/2020-01/#"));

        JSONObject body = JSON.parseObject(aclBody.get());
        assertEquals("class_139_2020_01", body.getString("username"));
        assertEquals("county/139/252/2020-01/#", body.getJSONArray("rules").getJSONObject(0).getString("topic"));
        assertEquals("publish", body.getJSONArray("rules").getJSONObject(0).getString("action"));
    }

    @Test
    void shouldDisconnectEveryClientUsingRotatedClassAccount()
    {
        assertTrue(adapter.disconnectClientsByUsername("class_139_2020_01"));
    }

    private String readBody(HttpExchange exchange) throws IOException
    {
        try (InputStream input = exchange.getRequestBody(); ByteArrayOutputStream output = new ByteArrayOutputStream())
        {
            byte[] buffer = new byte[1024];
            int count;
            while ((count = input.read(buffer)) >= 0) output.write(buffer, 0, count);
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException
    {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody())
        {
            output.write(bytes);
        }
    }
}

