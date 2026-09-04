package com.ruoyi.business.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.alibaba.fastjson2.JSON;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/** 物联网页面只接收刷新通知，实际数据仍由带权限的 REST 接口读取。 */
@Component
public class IotWebSocketHandler extends TextWebSocketHandler
{
    private final Map<Long, Map<String, WebSocketSession>> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session)
    {
        if (session == null || session.getAttributes() == null) return;
        Long experimentId = (Long) session.getAttributes().get("experimentId");
        if (experimentId == null || session.getId() == null) return;
        sessions.computeIfAbsent(experimentId, key -> new ConcurrentHashMap<>()).put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
    {
        remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception)
    {
        remove(session);
    }

    public void publishRefresh(Long experimentId)
    {
        if (experimentId == null) return;
        Map<String, WebSocketSession> targets = sessions.get(experimentId);
        if (targets == null) return;
        String payload = JSON.toJSONString(java.util.Collections.singletonMap("type", "iot_refresh"));
        for (WebSocketSession session : targets.values())
        {
            try
            {
                if (session.isOpen()) session.sendMessage(new TextMessage(payload));
                else remove(session);
            }
            catch (Exception ignored) { remove(session); }
        }
    }

    private void remove(WebSocketSession session)
    {
        if (session == null || session.getAttributes() == null || session.getId() == null) return;
        Long experimentId = (Long) session.getAttributes().get("experimentId");
        if (experimentId == null) return;
        Map<String, WebSocketSession> targets = sessions.get(experimentId);
        if (targets == null) return;
        targets.remove(session.getId());
        if (targets.isEmpty()) sessions.remove(experimentId, targets);
    }
}
