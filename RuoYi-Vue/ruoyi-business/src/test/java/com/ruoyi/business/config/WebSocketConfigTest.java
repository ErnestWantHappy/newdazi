package com.ruoyi.business.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest
{
    @Mock
    private ClassroomWebSocketHandler handler;

    @Mock
    private ClassroomWebSocketHandshakeInterceptor interceptor;

    @Mock
    private IotWebSocketHandler iotHandler;

    @Mock
    private IotWebSocketHandshakeInterceptor iotInterceptor;

    @Mock
    private WebSocketHandlerRegistry registry;

    @Mock
    private WebSocketHandlerRegistration registration;

    @Test
    void shouldRegisterOnlyConfiguredOrigins()
    {
        when(registry.addHandler(handler, "/ws/classroom/*/*/*")).thenReturn(registration);
        when(registration.addInterceptors(interceptor)).thenReturn(registration);
        WebSocketHandlerRegistration iotRegistration = org.mockito.Mockito.mock(WebSocketHandlerRegistration.class);
        when(registry.addHandler(iotHandler, "/ws/iot/*")).thenReturn(iotRegistration);
        when(iotRegistration.addInterceptors(iotInterceptor)).thenReturn(iotRegistration);

        WebSocketConfig config = new WebSocketConfig(handler, interceptor, iotHandler, iotInterceptor,
                "http://localhost, http://127.0.0.1:80");

        config.registerWebSocketHandlers(registry);

        verify(registration).setAllowedOrigins("http://localhost", "http://127.0.0.1:80");
        verify(iotRegistration).setAllowedOrigins("http://localhost", "http://127.0.0.1:80");
    }
}
