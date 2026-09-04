package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.ClassGroupingMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;

class ClassGroupingServiceTest
{
    private ClassGroupingService service;
    private ClassGroupingMapper mapper;
    private BizLessonMapper lessonMapper;

    @BeforeEach
    void setUp()
    {
        service = new ClassGroupingService();
        mapper = mock(ClassGroupingMapper.class);
        lessonMapper = mock(BizLessonMapper.class);
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ReflectionTestUtils.setField(service, "lessonMapper", lessonMapper);
        StudentPresenceService presenceService = mock(StudentPresenceService.class);
        ReflectionTestUtils.setField(service, "presenceService", presenceService);
        when(mapper.countManagedClass(200L, 9L, "2025", "1")).thenReturn(1);
        when(mapper.selectManagedClassDeptIds(200L, "2025", "1")).thenReturn(Collections.singletonList(9L));
        when(presenceService.summary(anyList())).thenReturn(Collections.<Long, Map<String, Object>>emptyMap());

        SysUser user = new SysUser();
        user.setUserId(200L);
        user.setDeptId(9L);
        user.setUserName("teacher200");
        LoginUser loginUser = new LoginUser(200L, 9L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectUnknownLayoutStudentBeforeDeletingExistingLayout()
    {
        when(mapper.selectClassStudents(9L, "2025", "1")).thenReturn(Collections.singletonList(student(101L)));
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("columnsCount", 6);
        request.put("items", Collections.singletonList(layoutItem(999L)));

        assertThrows(ServiceException.class, () -> service.saveLayout(200L, 9L, "2025", "1", request));

        verify(mapper, never()).deleteLayoutItems(org.mockito.ArgumentMatchers.any(Long.class));
        verify(mapper, never()).insertLayout(anyMap());
        verify(mapper, never()).updateLayout(anyMap());
    }

    @Test
    void shouldRequireLessonAssignmentBeforeFreezingSnapshot()
    {
        BizLesson lesson = new BizLesson();
        lesson.setLessonId(300L);
        lesson.setDeptId(9L);
        lesson.setCreatorId(200L);
        when(lessonMapper.selectBizLessonByLessonId(300L)).thenReturn(lesson);
        when(mapper.countLessonAssignment(300L, 9L, "2025", "1")).thenReturn(0);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.generateSnapshot(200L, 300L, "2025", "1", 400L));

        assertEquals("课程未指派给该班级，不能冻结分组快照", error.getMessage());
        verify(mapper, never()).insertSnapshot(anyMap());
    }

    @Test
    void shouldCreateNextVersionForSameSchemeName()
    {
        when(mapper.selectClassStudents(9L, "2025", "1")).thenReturn(Collections.singletonList(student(101L)));
        when(mapper.selectNextSchemeVersion(9L, "2025", "1", "默认分组")).thenReturn(2);
        when(mapper.insertScheme(anyMap())).thenAnswer(invocation -> {
            invocation.<Map<String, Object>>getArgument(0).put("schemeId", 500L);
            return 1;
        });
        when(mapper.insertGroup(anyMap())).thenAnswer(invocation -> {
            invocation.<Map<String, Object>>getArgument(0).put("groupId", 600L);
            return 1;
        });
        Map<String, Object> stored = new LinkedHashMap<String, Object>();
        stored.put("schemeId", 500L);
        when(mapper.selectScheme(500L)).thenReturn(stored);
        when(mapper.selectGroups(500L)).thenReturn(new ArrayList<Map<String, Object>>());
        when(mapper.selectMembers(500L)).thenReturn(new ArrayList<Map<String, Object>>());

        Map<String, Object> group = new HashMap<String, Object>();
        group.put("studentIds", Collections.singletonList(101L));
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("schemeName", "默认分组");
        request.put("groups", Collections.singletonList(group));

        service.saveScheme(200L, 9L, "2025", "1", request);

        verify(mapper).selectNextSchemeVersion(9L, "2025", "1", "默认分组");
        ArgumentCaptor<Map<String, Object>> schemeCaptor = mapCaptor();
        verify(mapper).insertScheme(schemeCaptor.capture());
        Map<String, Object> scheme = schemeCaptor.getValue();
        assertEquals(9L, scheme.get("deptId"));
        assertEquals("2025", scheme.get("entryYear"));
        assertEquals("1", scheme.get("classCode"));
        assertEquals("默认分组", scheme.get("schemeName"));
        assertEquals(2, scheme.get("schemeVersion"));
        assertEquals(200L, scheme.get("creatorUserId"));
    }

    @Test
    void shouldUseManagedClassDepartmentInsteadOfTeachersCurrentDepartment()
    {
        when(mapper.selectManagedClassDeptIds(200L, "2025", "1")).thenReturn(Collections.singletonList(169L));
        when(mapper.selectDesktopStudents(169L, "2025", "1", null))
                .thenReturn(Collections.singletonList(student(101L)));

        Map<String, Object> desktop = service.desktop(200L, 9L, "2025", "1");

        assertEquals(1, ((List<?>) desktop.get("students")).size());
        verify(mapper).selectDesktopStudents(169L, "2025", "1", null);
        verify(mapper, never()).selectDesktopStudents(9L, "2025", "1", null);
    }

    @Test
    void shouldRejectAmbiguousCrossSchoolClass()
    {
        when(mapper.selectManagedClassDeptIds(200L, "2025", "1")).thenReturn(java.util.Arrays.asList(9L, 169L));

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.desktop(200L, 9L, "2025", "1"));

        assertEquals("该年级班号在多个学校存在，请从具体课程或班级入口进入", error.getMessage());
        verify(mapper, never()).selectDesktopStudents(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    private static Map<String, Object> student(Long studentId)
    {
        Map<String, Object> student = new HashMap<String, Object>();
        student.put("studentId", studentId);
        return student;
    }

    private static Map<String, Object> layoutItem(Long studentId)
    {
        Map<String, Object> item = new HashMap<String, Object>();
        item.put("studentId", studentId);
        return item;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static ArgumentCaptor<Map<String, Object>> mapCaptor()
    {
        return (ArgumentCaptor) ArgumentCaptor.forClass(Map.class);
    }

}
