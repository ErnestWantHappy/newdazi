package com.ruoyi.business.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;

import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.CountyExamClass;
import com.ruoyi.business.domain.CountyExamStudent;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.BizTeacherClassMapper;
import com.ruoyi.business.mapper.CountyExamClassMapper;
import com.ruoyi.business.mapper.CountyExamStudentMapper;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.framework.web.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassroomWebSocketSecurityTest
{
    @Mock private TokenService tokenService;
    @Mock private BizStudentMapper studentMapper;
    @Mock private BizTeacherClassMapper teacherClassMapper;
    @Mock private BizLessonAssignmentMapper assignmentMapper;
    @Mock private BizLessonMapper lessonMapper;
    @Mock private CountyExamClassMapper countyExamClassMapper;
    @Mock private CountyExamStudentMapper countyExamStudentMapper;
    @Mock private ServerHttpResponse response;
    @Mock private WebSocketHandler handler;

    private ClassroomWebSocketHandshakeInterceptor interceptor;

    @BeforeEach
    void setUp()
    {
        interceptor = new ClassroomWebSocketHandshakeInterceptor(tokenService, studentMapper,
                teacherClassMapper, assignmentMapper, lessonMapper,
                countyExamClassMapper, countyExamStudentMapper);
    }

    @Test
    void teacherNeedsExactClassAndCurrentCourseScope()
    {
        LoginUser teacher = loginUser(8L, 10L, "teacher");
        when(tokenService.getLoginUser("token")).thenReturn(teacher);
        when(assignmentMapper.selectCurrentLessonByClass("2025", "1", 10L)).thenReturn(3L);
        BizLesson lesson = new BizLesson();
        lesson.setLessonId(3L);
        lesson.setDeptId(10L);
        lesson.setCreatorId(99L);
        when(lessonMapper.selectBizLessonByLessonId(3L)).thenReturn(lesson);
        when(teacherClassMapper.checkTeacherClassExists(any())).thenReturn(0, 1);

        assertFalse(handshake(new HashMap<>()));
        Map<String, Object> attributes = new HashMap<>();
        assertTrue(handshake(attributes));
        assertTrue(String.valueOf(attributes.get("roomKey")).endsWith("_3"));
    }

    @Test
    void unfinishedCountyExamBlocksStudentWebSocket()
    {
        LoginUser loginUser = loginUser(20L, 10L, "student");
        when(tokenService.getLoginUser("token")).thenReturn(loginUser);
        BizStudent student = new BizStudent();
        student.setStudentId(9L);
        student.setDeptId(10L);
        student.setEntryYear("2025");
        student.setClassCode("1");
        when(studentMapper.selectBizStudentByUserId(20L)).thenReturn(student);
        CountyExamClass examClass = new CountyExamClass();
        examClass.setExamId(6L);
        when(countyExamClassMapper.selectActiveByStudentInfo(10L, "2025", "1"))
                .thenReturn(Collections.singletonList(examClass));
        CountyExamStudent attempt = new CountyExamStudent();
        attempt.setStatus("0");
        when(countyExamStudentMapper.selectByExamAndStudent(6L, 9L)).thenReturn(attempt);

        assertFalse(handshake(new HashMap<>()));
    }

    @Test
    void teacherCanSubscribeExplicitHistoricalLessonOnlyWithClassEvidence()
    {
        LoginUser teacher = loginUser(8L, 10L, "teacher");
        when(tokenService.getLoginUser("token")).thenReturn(teacher);
        when(assignmentMapper.selectCurrentLessonByClass("2025", "1", 10L)).thenReturn(99L);
        BizLesson lesson = new BizLesson();
        lesson.setLessonId(3L);
        lesson.setDeptId(10L);
        lesson.setCreatorId(8L);
        when(lessonMapper.selectBizLessonByLessonId(3L)).thenReturn(lesson);
        BizLessonAssignment historical = new BizLessonAssignment();
        historical.setDeptId(10L);
        historical.setEntryYear("2025");
        historical.setClassCode("1班");
        when(assignmentMapper.selectAssignmentsByLessonId(3L))
                .thenReturn(Collections.singletonList(historical));

        Map<String, Object> attributes = new HashMap<>();
        assertTrue(handshake("/ws/classroom/10/2025/1/3", attributes));
        assertTrue(String.valueOf(attributes.get("roomKey")).endsWith("_3"));
    }

    private boolean handshake(Map<String, Object> attributes)
    {
        return handshake("/ws/classroom/10/2025/1", attributes);
    }

    private boolean handshake(String uri, Map<String, Object> attributes)
    {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI(uri);
        servletRequest.setCookies(new Cookie("Admin-Token", "token"));
        return interceptor.beforeHandshake(new ServletServerHttpRequest(servletRequest),
                response, handler, attributes);
    }

    private LoginUser loginUser(Long userId, Long deptId, String roleKey)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setDeptId(deptId);
        user.setUserName("u" + userId);
        SysRole role = new SysRole();
        role.setRoleKey(roleKey);
        user.setRoles(new ArrayList<>(Collections.singletonList(role)));
        return new LoginUser(userId, deptId, user, Collections.emptySet());
    }
}
