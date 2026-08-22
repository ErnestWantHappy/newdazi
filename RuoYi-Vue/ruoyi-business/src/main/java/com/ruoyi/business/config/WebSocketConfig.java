package com.ruoyi.business.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * 课堂 WebSocket 复用系统登录态，避免匿名客户端加入任意学校或班级。
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer
{
    private final ClassroomWebSocketHandler handler;
    private final ClassroomWebSocketHandshakeInterceptor interceptor;
    private final IotWebSocketHandler iotHandler;
    private final IotWebSocketHandshakeInterceptor iotInterceptor;
    private final String[] allowedOrigins;

    public WebSocketConfig(ClassroomWebSocketHandler handler,
                           ClassroomWebSocketHandshakeInterceptor interceptor,
                           IotWebSocketHandler iotHandler, IotWebSocketHandshakeInterceptor iotInterceptor,
                           @Value("${guide-sheet.websocket.allowed-origins}") String allowedOrigins)
    {
        this.handler = handler;
        this.interceptor = interceptor;
        this.iotHandler = iotHandler;
        this.iotInterceptor = iotInterceptor;
        // 精确白名单由部署环境维护，避免代理部署时退化为允许任意来源。
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
    {
        registry.addHandler(handler, "/ws/classroom/*/*/*")
                .addInterceptors(interceptor)
                .setAllowedOrigins(allowedOrigins);
        registry.addHandler(iotHandler, "/ws/iot/*")
                .addInterceptors(iotInterceptor)
                .setAllowedOrigins(allowedOrigins);
    }

    /**
     * 容器级文本缓冲上限 64KB：限制单帧消息大小，防止学生端伪造超大 payload 挤占内存。
     * 业务层已有控制消息角色校验，此处补齐传输层兜底。
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer()
    {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(64 * 1024);
        container.setMaxBinaryMessageBufferSize(64 * 1024);
        return container;
    }
}
