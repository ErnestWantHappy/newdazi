package com.ruoyi.business.config;

import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.framework.web.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * 使用 Admin-Token Cookie 校验课堂连接的学校、班级和角色。
 */
@Component
public class ClassroomWebSocketHandshakeInterceptor implements HandshakeInterceptor
{
    private final TokenService tokenService;
    private final BizStudentMapper studentMapper;

    public ClassroomWebSocketHandshakeInterceptor(TokenService tokenService, BizStudentMapper studentMapper)
    {
        this.tokenService = tokenService;
        this.studentMapper = studentMapper;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes)
    {
        if (!(request instanceof ServletServerHttpRequest)) return reject(response);
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        String token = readToken(servletRequest);
        LoginUser loginUser = tokenService.getLoginUser(token);
        if (loginUser == null || loginUser.getUser() == null) return reject(response);

        String[] segments = request.getURI().getPath().split("/");
        if (segments.length < 6) return reject(response);
        String deptId = segments[segments.length - 3];
        String entryYear = segments[segments.length - 2];
        String classCode = segments[segments.length - 1];
        if (!deptId.equals(String.valueOf(loginUser.getDeptId()))) return reject(response);

        boolean teacher = loginUser.getUser().isAdmin() || loginUser.getUser().getRoles().stream()
                .map(SysRole::getRoleKey)
                .anyMatch(role -> "teacher".equals(role) || "researcher".equals(role));
        if (!teacher)
        {
            BizStudent student = studentMapper.selectBizStudentByUserId(loginUser.getUserId());
            if (student == null || !entryYear.equals(student.getEntryYear())
                    || !classCode.equals(student.getClassCode())) return reject(response);
        }

        attributes.put("roomKey", deptId + "_" + entryYear + "_" + classCode);
        attributes.put("userId", loginUser.getUserId());
        attributes.put("teacher", teacher);
        return true;
    }

    private boolean reject(ServerHttpResponse response)
    {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception)
    {
    }

    private String readToken(HttpServletRequest request)
    {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies)
        {
            if ("Admin-Token".equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}
