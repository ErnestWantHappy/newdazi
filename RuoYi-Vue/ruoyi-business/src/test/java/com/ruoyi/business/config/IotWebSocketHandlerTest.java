package com.ruoyi.business.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

class IotWebSocketHandlerTest
{
    @Test
    void ignoresSessionsWithoutValidatedExperimentAttribute()
    {
        IotWebSocketHandler handler = new IotWebSocketHandler();
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(Collections.emptyMap());
        when(session.getId()).thenReturn("missing-experiment");

        assertDoesNotThrow(() -> handler.afterConnectionEstablished(session));
        assertDoesNotThrow(() -> handler.afterConnectionClosed(session, null));
        assertDoesNotThrow(() -> handler.handleTransportError(session, new IllegalStateException("test")));
        assertDoesNotThrow(() -> handler.publishRefresh(null));
    }
}
