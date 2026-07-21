package com.ruoyi.business.config;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClassroomWebSocketHandlerTest
{
    @Test
    void transportErrorRemovesServerSideSession() throws Exception
    {
        ClassroomWebSocketHandler handler = new ClassroomWebSocketHandler();
        WebSocketSession failed = session("failed", true);
        WebSocketSession teacher = session("teacher", true);
        handler.afterConnectionEstablished(failed);
        handler.afterConnectionEstablished(teacher);

        handler.handleTransportError(failed, new IllegalStateException("closed"));
        handler.handleTextMessage(teacher,
                new TextMessage("{\"type\":\"message\",\"content\":\"hello\"}"));

        verify(failed, never()).sendMessage(org.mockito.ArgumentMatchers.any());
    }

    private WebSocketSession session(String id, boolean teacher)
    {
        WebSocketSession session = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("roomKey", "10_2025_1_3");
        attributes.put("userId", 8L);
        attributes.put("teacher", teacher);
        when(session.getId()).thenReturn(id);
        when(session.getAttributes()).thenReturn(attributes);
        when(session.isOpen()).thenReturn(true);
        return session;
    }
}
