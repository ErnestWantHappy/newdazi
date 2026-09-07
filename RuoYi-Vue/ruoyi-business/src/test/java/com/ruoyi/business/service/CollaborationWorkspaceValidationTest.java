package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.ruoyi.common.exception.ServiceException;

/**
 * 协作工作台分组校验：组数等于文档数、不重不漏、拒绝外班成员。
 * 纯静态规则测试，不依赖数据库。
 */
class CollaborationWorkspaceValidationTest
{
    @Test
    void shouldAcceptEvenSplitAcrossDocuments()
    {
        List<Map<String, Object>> students = students(1L, 2L, 3L, 4L);
        List<Long> materials = Arrays.asList(11L, 22L);
        List<Map<String, Object>> groups = groups(group(11L, 1L, 2L), group(11L, 3L, 4L));
        assertDoesNotThrow(() -> CollaborationWorkspaceService.validateGroups(students, materials, groups));
    }

    @Test
    void shouldAllowDifferentGroupsSharingSameTemplate()
    {
        List<Map<String, Object>> students = students(1L, 2L, 3L);
        List<Long> materials = Arrays.asList(11L, 22L);
        List<Map<String, Object>> groups = groups(group(11L, 1L, 2L), group(11L, 3L));
        assertDoesNotThrow(() -> CollaborationWorkspaceService.validateGroups(students, materials, groups));
    }

    @Test
    void shouldRejectDuplicateStudentAcrossGroups()
    {
        List<Map<String, Object>> students = students(1L, 2L, 3L);
        List<Long> materials = Arrays.asList(11L, 22L);
        List<Map<String, Object>> groups = groups(group(11L, 1L, 2L), group(22L, 2L, 3L));
        assertThrows(ServiceException.class,
                () -> CollaborationWorkspaceService.validateGroups(students, materials, groups));
    }

    @Test
    void shouldRejectMissingStudent()
    {
        List<Map<String, Object>> students = students(1L, 2L, 3L);
        List<Long> materials = Arrays.asList(11L, 22L);
        // 只覆盖 1、2 号，遗漏 3 号学生
        List<Map<String, Object>> groups = groups(group(11L, 1L), group(22L, 2L));
        assertThrows(ServiceException.class,
                () -> CollaborationWorkspaceService.validateGroups(students, materials, groups));
    }

    @Test
    void shouldRejectOutsideStudent()
    {
        List<Map<String, Object>> students = students(1L, 2L);
        List<Long> materials = Arrays.asList(11L);
        List<Map<String, Object>> groups = groups(group(11L, 1L, 999L));
        assertThrows(ServiceException.class,
                () -> CollaborationWorkspaceService.validateGroups(students, materials, groups));
    }

    @Test
    void shouldRejectEmptyGroupAndGroupCountMismatch()
    {
        List<Map<String, Object>> students = students(1L, 2L);
        List<Long> materials = Arrays.asList(11L);
        assertThrows(ServiceException.class, () -> CollaborationWorkspaceService
                .validateGroups(students, materials, groups(group(11L))));
        assertThrows(ServiceException.class, () -> CollaborationWorkspaceService
                .validateGroups(students, materials, groups(group(11L, 1L), group(11L, 2L))));
    }

    @Test
    void shouldRejectUnknownMaterial()
    {
        List<Map<String, Object>> students = students(1L, 2L);
        List<Long> materials = Arrays.asList(11L);
        assertThrows(ServiceException.class, () -> CollaborationWorkspaceService
                .validateGroups(students, materials, groups(group(99L, 1L, 2L))));
    }

    private static List<Map<String, Object>> students(Long... ids)
    {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Long id : ids)
        {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("studentId", id);
            result.add(row);
        }
        return result;
    }

    private static Map<String, Object> group(Long materialId, Long... studentIds)
    {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("materialId", materialId);
        row.put("studentIds", new ArrayList<Long>(Arrays.asList(studentIds)));
        return row;
    }

    private static List<Map<String, Object>> groups(Map<String, Object>... items)
    {
        return new ArrayList<Map<String, Object>>(Arrays.asList(items));
    }
}
