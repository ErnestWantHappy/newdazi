package com.ruoyi.business.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.ruoyi.business.service.StudentPresenceService;

/** 客户端只允许心跳，服务端用 TTL 自动判离线。 */
@Component
public class StudentPresenceWebSocketHandler extends TextWebSocketHandler {
    @Autowired private StudentPresenceService service;
    @Override public void afterConnectionEstablished(WebSocketSession session) { touch(session); }
    @Override protected void handleTextMessage(WebSocketSession session, TextMessage message) { touch(session); }
    private void touch(WebSocketSession session) { Object id = session.getAttributes().get("studentId"); Object device = session.getAttributes().get("deviceId"); if (id instanceof Long && device != null) service.heartbeat((Long)id, String.valueOf(device), String.valueOf(session.getAttributes().get("connectionIp"))); }
}
