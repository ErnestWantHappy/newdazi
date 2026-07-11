package com.ruoyi.business.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 课堂 WebSocket 复用系统登录态，避免匿名客户端加入任意学校或班级。
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer
{
    private final ClassroomWebSocketHandler handler;
    private final ClassroomWebSocketHandshakeInterceptor interceptor;

    public WebSocketConfig(ClassroomWebSocketHandler handler,
                           ClassroomWebSocketHandshakeInterceptor interceptor)
    {
        this.handler = handler;
        this.interceptor = interceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
    {
        registry.addHandler(handler, "/ws/classroom/*/*")
                .addInterceptors(interceptor);
    }
}
