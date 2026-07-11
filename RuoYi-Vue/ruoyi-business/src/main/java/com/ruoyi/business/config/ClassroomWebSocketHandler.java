package com.ruoyi.business.config;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ServerEndpoint("/ws/classroom/{deptId}/{classCode}")
public class ClassroomWebSocketHandler
{
    private static final Logger log = LoggerFactory.getLogger(ClassroomWebSocketHandler.class);

    private static final Map<String, Map<String, Session>> classroomMap = new ConcurrentHashMap<>();
    private static final Map<String, Long> lastHeartbeatMap = new ConcurrentHashMap<>();
    private static final long HEARTBEAT_TIMEOUT = 5 * 60 * 1000;

    private String roomKey;
    private String teacherKey;

    static {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000);
                    long now = System.currentTimeMillis();
                    Iterator<Map.Entry<String, Long>> it = lastHeartbeatMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, Long> entry = it.next();
                        if (now - entry.getValue() > HEARTBEAT_TIMEOUT) {
                            String sessionId = entry.getKey();
                            it.remove();
                            for (Map<String, Session> room : classroomMap.values()) {
                                Session s = room.remove(sessionId);
                                if (s != null && s.isOpen()) {
                                    try { s.close(); } catch (Exception ignored) {}
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "ws-heartbeat-cleaner");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("deptId") String deptId,
                        @PathParam("classCode") String classCode)
    {
        roomKey = deptId + "_" + classCode;
        teacherKey = roomKey + "_teacher";
        classroomMap.computeIfAbsent(roomKey, k -> new ConcurrentHashMap<>())
                .put(session.getId(), session);
        lastHeartbeatMap.put(session.getId(), System.currentTimeMillis());
        log.info("WebSocket连接建立: room={}, sessionId={}", roomKey, session.getId());
    }

    @OnClose
    public void onClose(Session session)
    {
        if (roomKey != null)
        {
            Map<String, Session> room = classroomMap.get(roomKey);
            if (room != null)
            {
                room.remove(session.getId());
                if (room.isEmpty())
                {
                    classroomMap.remove(roomKey);
                }
            }
        }
        lastHeartbeatMap.remove(session.getId());
        log.info("WebSocket连接关闭: room={}, sessionId={}", roomKey, session.getId());
    }

    @OnError
    public void onError(Session session, Throwable error)
    {
        lastHeartbeatMap.remove(session.getId());
        log.error("WebSocket错误: room={}, sessionId={}", roomKey, session.getId(), error);
    }

    @OnMessage
    public void onMessage(String message, Session session)
    {
        try
        {
            lastHeartbeatMap.put(session.getId(), System.currentTimeMillis());
            JSONObject msg = JSON.parseObject(message);
            String type = msg.getString("type");
            if (roomKey == null) return;

            if ("teacher_register".equals(type))
            {
                classroomMap.computeIfAbsent(teacherKey, k -> new ConcurrentHashMap<>())
                        .put(session.getId(), session);
                log.info("教师注册: room={}, sessionId={}", teacherKey, session.getId());
                return;
            }

            if ("heartbeat".equals(type))
            {
                return;
            }

            Map<String, Session> teacherSessions = classroomMap.get(teacherKey);
            if (teacherSessions != null)
            {
                msg.put("fromSession", session.getId());
                msg.put("roomKey", roomKey);
                String broadcastMsg = msg.toJSONString();
                for (Session ts : teacherSessions.values())
                {
                    if (ts.isOpen())
                    {
                        ts.getBasicRemote().sendText(broadcastMsg);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("WebSocket消息处理异常: room={}", roomKey, e);
        }
    }

    public void broadcastToRoom(String deptId, String classCode, String message)
    {
        String key = deptId + "_" + classCode;
        Map<String, Session> room = classroomMap.get(key);
        if (room != null)
        {
            for (Session session : room.values())
            {
                try
                {
                    if (session.isOpen())
                    {
                        session.getBasicRemote().sendText(message);
                    }
                }
                catch (IOException e)
                {
                    log.error("广播消息失败: sessionId={}", session.getId(), e);
                }
            }
        }
    }

    public void broadcastPageChange(String deptId, String classCode, int page)
    {
        JSONObject msg = new JSONObject();
        msg.put("type", "page_change");
        msg.put("page", page);
        msg.put("timestamp", System.currentTimeMillis());
        broadcastToRoom(deptId, classCode, msg.toJSONString());
    }

    public void broadcastRefresh(String deptId, String classCode)
    {
        JSONObject msg = new JSONObject();
        msg.put("type", "refresh");
        msg.put("timestamp", System.currentTimeMillis());
        broadcastToRoom(deptId, classCode, msg.toJSONString());
    }

    public void broadcastMessage(String deptId, String classCode, String content)
    {
        JSONObject msg = new JSONObject();
        msg.put("type", "message");
        msg.put("content", content);
        msg.put("timestamp", System.currentTimeMillis());
        broadcastToRoom(deptId, classCode, msg.toJSONString());
    }
}