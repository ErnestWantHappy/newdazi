package com.ruoyi.business.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import com.ruoyi.business.domain.IotExperiment;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.IotMapper;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.framework.web.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/** 握手阶段只允许本校且有课程/班级范围的教师订阅实验刷新通知。 */
@Component
public class IotWebSocketHandshakeInterceptor implements HandshakeInterceptor
{
    @Autowired private TokenService tokenService;
    @Autowired private IotMapper mapper;
    @Autowired private BizLessonMapper lessonMapper;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler,
                                   Map<String, Object> attributes)
    {
        if (!(request instanceof ServletServerHttpRequest)) return reject(response);
        HttpServletRequest servlet = ((ServletServerHttpRequest) request).getServletRequest();
        LoginUser user = tokenService.getLoginUser(readToken(servlet));
        if (user == null || user.getUser() == null) return reject(response);
        Long experimentId;
        try { experimentId = Long.valueOf(request.getURI().getPath().replaceFirst(".*/", "")); }
        catch (NumberFormatException ex) { return reject(response); }
        IotExperiment experiment = mapper.selectExperimentById(experimentId);
        if (experiment == null || experiment.getDeptId() == null || !experiment.getDeptId().equals(user.getDeptId())) return reject(response);
        boolean manager = user.getUser().isAdmin()
                || (lessonMapper.selectBizLessonByLessonId(experiment.getLessonId()) != null
                    && user.getUserId().equals(lessonMapper.selectBizLessonByLessonId(experiment.getLessonId()).getCreatorId()));
        List<SysRole> roles = user.getUser().getRoles() == null ? Collections.emptyList() : user.getUser().getRoles();
        boolean teacherLike = roles.stream().map(SysRole::getRoleKey)
                .anyMatch(role -> "teacher".equals(role) || "researcher".equals(role));
        if (!manager && (!teacherLike || mapper.countTeacherGroupScope(experimentId, user.getUserId(), user.getDeptId()) == 0)) return reject(response);
        attributes.put("experimentId", experimentId);
        return true;
    }

    @Override public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler, Exception exception) { }

    private boolean reject(ServerHttpResponse response) { response.setStatusCode(HttpStatus.FORBIDDEN); return false; }

    private String readToken(HttpServletRequest request)
    {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) if ("Admin-Token".equals(cookie.getName())) return cookie.getValue();
        return null;
    }
}
