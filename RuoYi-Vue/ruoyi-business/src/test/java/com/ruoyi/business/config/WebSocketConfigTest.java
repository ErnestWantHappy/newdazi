package com.ruoyi.business.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
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
    private StudentPresenceWebSocketHandler presenceHandler;

    @Mock
    private StudentPresenceHandshakeInterceptor presenceInterceptor;

    @Mock
    private WebSocketHandlerRegistry registry;

    @Mock
    private WebSocketHandlerRegistration registration;

    @Test
    void shouldRegisterOnlyConfiguredOrigins()
    {
        when(registry.addHandler(handler, "/ws/classroom/*/*/*", "/ws/classroom/*/*/*/*")).thenReturn(registration);
        when(registration.addInterceptors(interceptor)).thenReturn(registration);
        WebSocketHandlerRegistration iotRegistration = org.mockito.Mockito.mock(WebSocketHandlerRegistration.class);
        when(registry.addHandler(iotHandler, "/ws/iot/*")).thenReturn(iotRegistration);
        when(iotRegistration.addInterceptors(iotInterceptor)).thenReturn(iotRegistration);
        WebSocketHandlerRegistration presenceRegistration = org.mockito.Mockito.mock(WebSocketHandlerRegistration.class);
        when(registry.addHandler(presenceHandler, "/ws/presence/*")).thenReturn(presenceRegistration);
        when(presenceRegistration.addInterceptors(presenceInterceptor)).thenReturn(presenceRegistration);

        WebSocketConfig config = new WebSocketConfig(handler, interceptor, iotHandler, iotInterceptor, presenceHandler, presenceInterceptor,
                "http://localhost, http://127.0.0.1:80");

        config.registerWebSocketHandlers(registry);

        verify(registration).setAllowedOrigins("http://localhost", "http://127.0.0.1:80");
        verify(iotRegistration).setAllowedOrigins("http://localhost", "http://127.0.0.1:80");
        verify(presenceRegistration).setAllowedOrigins("http://localhost", "http://127.0.0.1:80");
    }

    @Test
    void shouldUseNginxForwardedAddressOnlyForLoopbackConnection()
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Real-IP", "10.52.12.34");

        org.junit.jupiter.api.Assertions.assertEquals("10.52.12.34", StudentPresenceHandshakeInterceptor.connectionIp(request));

        request.setRemoteAddr("10.52.12.35");
        org.junit.jupiter.api.Assertions.assertEquals("10.52.12.35", StudentPresenceHandshakeInterceptor.connectionIp(request));
    }
}
