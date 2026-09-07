package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.ClassGroupingMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;

/**
 * 固定组预览：自动生成只返回分组不落库，默认四人连续切分。
 */
class ClassGroupingPreviewTest
{
    private ClassGroupingService service;
    private ClassGroupingMapper mapper;

    @BeforeEach
    void setUp()
    {
        service = new ClassGroupingService();
        mapper = mock(ClassGroupingMapper.class);
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ReflectionTestUtils.setField(service, "lessonMapper", mock(BizLessonMapper.class));
        when(mapper.selectManagedClassDeptIds(200L, "2025", "1")).thenReturn(Collections.singletonList(9L));

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
    @SuppressWarnings("unchecked")
    void shouldPreviewFourPerGroupWithoutSaving()
    {
        when(mapper.selectClassStudents(9L, "2025", "1")).thenReturn(students(10));
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("membersPerGroup", 4);
        request.put("mode", "RANGE");

        Map<String, Object> result = service.previewScheme(200L, 9L, "2025", "1", request);
        List<Map<String, Object>> groups = (List<Map<String, Object>>) result.get("groups");
        assertEquals(3, groups.size());
        assertEquals(4, ((List<Long>) groups.get(0).get("studentIds")).size());
        assertEquals(4, ((List<Long>) groups.get(1).get("studentIds")).size());
        assertEquals(2, ((List<Long>) groups.get(2).get("studentIds")).size());
        // 预览不落库：取消后无任何方案写入
        verify(mapper, never()).insertScheme(anyMap());
        verify(mapper, never()).insertGroup(anyMap());
    }

    @Test
    void shouldRejectPreviewForEmptyClass()
    {
        when(mapper.selectClassStudents(9L, "2025", "1")).thenReturn(Collections.<Map<String, Object>>emptyList());
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("membersPerGroup", 4);
        request.put("mode", "RANGE");
        assertThrows(ServiceException.class, () -> service.previewScheme(200L, 9L, "2025", "1", request));
        verify(mapper, never()).insertScheme(anyMap());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldKeepRangeOrderContinuous()
    {
        when(mapper.selectClassStudents(9L, "2025", "1")).thenReturn(students(8));
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("membersPerGroup", 4);
        request.put("mode", "RANGE");
        Map<String, Object> result = service.previewScheme(200L, 9L, "2025", "1", request);
        List<Map<String, Object>> groups = (List<Map<String, Object>>) result.get("groups");
        assertEquals(2, groups.size());
        List<Long> first = (List<Long>) groups.get(0).get("studentIds");
        assertTrue(first.contains(101L) && first.contains(104L));
    }

    private static List<Map<String, Object>> students(int count)
    {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < count; i++)
        {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("studentId", 101L + i);
            result.add(row);
        }
        return result;
    }
}
