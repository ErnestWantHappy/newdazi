package com.ruoyi.business.config;

import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.framework.web.service.TokenService;

/** Presence 仅允许已登录的学生建立，IP 始终取服务器实际看到的连接地址。 */
@Component
public class StudentPresenceHandshakeInterceptor implements HandshakeInterceptor {
    private final TokenService tokenService; private final BizStudentMapper studentMapper;
    public StudentPresenceHandshakeInterceptor(TokenService tokenService, BizStudentMapper studentMapper) { this.tokenService = tokenService; this.studentMapper = studentMapper; }
    @Override public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler, Map<String,Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest)) return reject(response);
        HttpServletRequest servlet = ((ServletServerHttpRequest)request).getServletRequest(); LoginUser user = tokenService.getLoginUser(token(servlet));
        if (user == null || user.getUserId() == null) return reject(response); BizStudent student = studentMapper.selectBizStudentByUserId(user.getUserId()); if (student == null) return reject(response);
        String path = request.getURI().getPath(); String deviceId = path.substring(path.lastIndexOf('/') + 1);
        if (!deviceId.matches("[A-Za-z0-9_-]{8,80}")) return reject(response);
        attributes.put("studentId", student.getStudentId()); attributes.put("deviceId", deviceId); attributes.put("connectionIp", servlet.getRemoteAddr()); return true;
    }
    @Override public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler, Exception exception) { }
    private boolean reject(ServerHttpResponse response) { response.setStatusCode(HttpStatus.FORBIDDEN); return false; }
    private String token(HttpServletRequest request) { Cookie[] cookies = request.getCookies(); if (cookies == null) return null; for (Cookie cookie : cookies) if ("Admin-Token".equals(cookie.getName())) return cookie.getValue(); return null; }
}
