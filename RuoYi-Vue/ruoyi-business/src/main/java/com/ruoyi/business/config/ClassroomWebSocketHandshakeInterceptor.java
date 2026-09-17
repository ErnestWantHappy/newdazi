package com.ruoyi.business.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizTeacherClass;
import com.ruoyi.business.domain.CountyExamClass;
import com.ruoyi.business.domain.CountyExamStudent;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.BizTeacherClassMapper;
import com.ruoyi.business.mapper.CountyExamClassMapper;
import com.ruoyi.business.mapper.CountyExamStudentMapper;
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
    private final BizTeacherClassMapper teacherClassMapper;
    private final BizLessonAssignmentMapper assignmentMapper;
    private final BizLessonMapper lessonMapper;
    private final CountyExamClassMapper countyExamClassMapper;
    private final CountyExamStudentMapper countyExamStudentMapper;

    public ClassroomWebSocketHandshakeInterceptor(TokenService tokenService, BizStudentMapper studentMapper,
                                                   BizTeacherClassMapper teacherClassMapper,
                                                   BizLessonAssignmentMapper assignmentMapper,
                                                   BizLessonMapper lessonMapper,
                                                   CountyExamClassMapper countyExamClassMapper,
                                                   CountyExamStudentMapper countyExamStudentMapper)
    {
        this.tokenService = tokenService;
        this.studentMapper = studentMapper;
        this.teacherClassMapper = teacherClassMapper;
        this.assignmentMapper = assignmentMapper;
        this.lessonMapper = lessonMapper;
        this.countyExamClassMapper = countyExamClassMapper;
        this.countyExamStudentMapper = countyExamStudentMapper;
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
        boolean explicitLesson = segments.length >= 7;
        String deptId = segments[segments.length - (explicitLesson ? 4 : 3)];
        String entryYear = segments[segments.length - (explicitLesson ? 3 : 2)];
        String classCode = normalizeClassCode(segments[segments.length - (explicitLesson ? 2 : 1)]);
        if (!deptId.equals(String.valueOf(loginUser.getDeptId()))) return reject(response);
        Long numericDeptId;
        try
        {
            numericDeptId = Long.valueOf(deptId);
        }
        catch (NumberFormatException e)
        {
            return reject(response);
        }

        List<SysRole> roles = loginUser.getUser().getRoles() == null
                ? Collections.emptyList() : loginUser.getUser().getRoles();
        boolean teacher = loginUser.getUser().isAdmin() || roles.stream()
                .map(SysRole::getRoleKey)
                .anyMatch(role -> "teacher".equals(role) || "researcher".equals(role));
        if (!teacher)
        {
            BizStudent student = studentMapper.selectBizStudentByUserId(loginUser.getUserId());
            if (student == null || student.getDeptId() == null || !numericDeptId.equals(student.getDeptId())
                    || !entryYear.equals(student.getEntryYear())
                    || !classCode.equals(normalizeClassCode(student.getClassCode()))
                    || hasUnfinishedCountyExam(student, numericDeptId)) return reject(response);
        }

        Long currentLessonId = assignmentMapper.selectCurrentLessonByClass(entryYear, classCode, numericDeptId);
        Long lessonId = currentLessonId;
        if (explicitLesson)
        {
            try
            {
                lessonId = Long.valueOf(segments[segments.length - 1]);
            }
            catch (NumberFormatException e)
            {
                return reject(response);
            }
            // 学生只能进入自己班级的当前课程；教师可订阅有真实班级关系的历史课程。
            if (!teacher && !lessonId.equals(currentLessonId)) return reject(response);
        }
        BizLesson lesson = lessonId == null ? null : lessonMapper.selectBizLessonByLessonId(lessonId);
        if (lesson == null || lesson.getDeptId() == null || !numericDeptId.equals(lesson.getDeptId()))
        {
            return reject(response);
        }
        if (teacher && !hasTeacherCourseScope(loginUser, lesson, numericDeptId, entryYear, classCode))
        {
            return reject(response);
        }
        if (teacher && explicitLesson && !isLessonRelatedToClass(lessonId, numericDeptId, entryYear, classCode))
        {
            return reject(response);
        }

        attributes.put("roomKey", ClassroomRoomKey.of(numericDeptId, entryYear, classCode, lessonId));
        attributes.put("userId", loginUser.getUserId());
        attributes.put("teacher", teacher);
        attributes.put("lessonId", lessonId);
        return true;
    }

    private boolean isLessonRelatedToClass(Long lessonId, Long deptId, String entryYear, String classCode)
    {
        List<com.ruoyi.business.domain.BizLessonAssignment> assignments = assignmentMapper.selectAssignmentsByLessonId(lessonId);
        return (assignments != null && assignments.stream().anyMatch(assignment ->
                deptId.equals(assignment.getDeptId())
                        && entryYear.equals(assignment.getEntryYear())
                        && classCode.equals(normalizeClassCode(assignment.getClassCode()))))
                || assignmentMapper.countHistoricalAssignment(
                        lessonId, entryYear, classCode, deptId) > 0;
    }

    private boolean hasTeacherCourseScope(LoginUser loginUser, BizLesson lesson, Long deptId,
                                          String entryYear, String classCode)
    {
        if (loginUser.getUser().isAdmin()) return true;
        boolean creator = loginUser.getUserId().equals(lesson.getCreatorId())
                || (lesson.getCreatorId() == null
                && loginUser.getUsername().equals(lesson.getCreateBy()));
        if (creator) return true;
        BizTeacherClass teacherClass = new BizTeacherClass();
        teacherClass.setUserId(loginUser.getUserId());
        teacherClass.setDeptId(deptId);
        teacherClass.setEntryYear(entryYear);
        teacherClass.setClassCode(classCode);
        return teacherClassMapper.checkTeacherClassExists(teacherClass) > 0;
    }

    private boolean hasUnfinishedCountyExam(BizStudent student, Long deptId)
    {
        List<CountyExamClass> activeClasses = countyExamClassMapper.selectActiveByStudentInfo(
                deptId, student.getEntryYear(), normalizeClassCode(student.getClassCode()));
        if (activeClasses == null) return false;
        for (CountyExamClass activeClass : activeClasses)
        {
            if (activeClass == null || activeClass.getExamId() == null) return true;
            CountyExamStudent attempt = countyExamStudentMapper.selectByExamAndStudent(
                    activeClass.getExamId(), student.getStudentId());
            if (attempt == null || !"1".equals(attempt.getStatus())) return true;
        }
        return false;
    }

    private String normalizeClassCode(String classCode)
    {
        String normalized = ClassroomRoomKey.normalizeClassCode(classCode);
        return normalized == null ? "" : normalized;
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
