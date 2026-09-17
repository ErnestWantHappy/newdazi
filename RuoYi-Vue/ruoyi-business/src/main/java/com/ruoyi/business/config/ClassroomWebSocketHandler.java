package com.ruoyi.business.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 课堂消息只在经过握手鉴权的同校同班连接之间转发。
 */
@Component
public class ClassroomWebSocketHandler extends TextWebSocketHandler
{
    private static final Logger log = LoggerFactory.getLogger(ClassroomWebSocketHandler.class);
    private final Map<String, Map<String, WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session)
    {
        String roomKey = String.valueOf(session.getAttributes().get("roomKey"));
        rooms.computeIfAbsent(roomKey, key -> new ConcurrentHashMap<>())
                .put(session.getId(), session);
        log.info("课堂连接建立 room={} userId={}", roomKey, session.getAttributes().get("userId"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception
    {
        JSONObject payload;
        try
        {
            payload = JSON.parseObject(message.getPayload());
        }
        catch (Exception e)
        {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"消息格式无效\"}"));
            return;
        }

        String type = payload.getString("type");
        if ("heartbeat".equals(type)) return;
        boolean teacher = Boolean.TRUE.equals(session.getAttributes().get("teacher"));
        if (!teacher)
        {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"学生连接只允许心跳消息\"}"));
            return;
        }

        payload.put("fromUserId", session.getAttributes().get("userId"));
        broadcast(String.valueOf(session.getAttributes().get("roomKey")), payload.toJSONString());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
    {
        removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception)
    {
        removeSession(session);
        if (session.isOpen())
        {
            try
            {
                session.close(CloseStatus.SERVER_ERROR);
            }
            catch (Exception e)
            {
                log.debug("课堂异常连接关闭失败 sessionId={}", session.getId());
            }
        }
    }

    private void removeSession(WebSocketSession session)
    {
        String roomKey = String.valueOf(session.getAttributes().get("roomKey"));
        rooms.computeIfPresent(roomKey, (key, room) -> {
            room.remove(session.getId());
            return room.isEmpty() ? null : room;
        });
    }

    private void broadcast(String roomKey, String payload)
    {
        Map<String, WebSocketSession> room = rooms.get(roomKey);
        if (room == null) return;
        for (WebSocketSession target : room.values())
        {
            if (!target.isOpen())
            {
                removeSession(target);
                continue;
            }
            try
            {
                target.sendMessage(new TextMessage(payload));
            }
            catch (Exception e)
            {
                log.warn("课堂消息发送失败 sessionId={}", target.getId());
                removeSession(target);
            }
        }
    }

    /**
     * 向指定学校、年级、班级和课程的课堂连接广播状态变更消息
     */
    public void broadcastToClassroom(Long deptId, String entryYear, String classCode, Long lessonId, String payload)
    {
        if (deptId == null || entryYear == null || classCode == null || lessonId == null) return;
        String targetRoomKey = ClassroomRoomKey.of(deptId, entryYear, classCode, lessonId);
        if (targetRoomKey == null) return;
        broadcast(targetRoomKey, payload);
    }
}
