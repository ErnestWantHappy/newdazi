package com.ruoyi.business.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ruoyi.business.domain.query.TeachingSupervisionQuery;
import com.ruoyi.business.mapper.TeachingSupervisionMapper;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.redis.RedisCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/** 监管汇总缓存只验证读路径，不改变监管统计业务口径。 */
@ExtendWith(MockitoExtension.class)
class TeachingSupervisionCacheTest
{
    @Mock private TeachingSupervisionMapper mapper;
    @Mock private RedisCache redisCache;
    @InjectMocks private TeachingSupervisionController controller;

    @AfterEach
    void clearRequest()
    {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void cacheHitDoesNotExecuteHeavyAggregation()
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("pageNum", "1");
        request.setParameter("pageSize", "10");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        TableDataInfo cached = new TableDataInfo();
        cached.setTotal(19L);
        when(redisCache.getCacheObject(anyString())).thenReturn(cached);

        TableDataInfo result = controller.schools(new TeachingSupervisionQuery());

        assertSame(cached, result);
        verify(mapper, never()).countSchoolSummaries(org.mockito.ArgumentMatchers.any());
        verify(mapper, never()).selectSchoolSummaries(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void allHeavySummaryViewsReuseCachedResult()
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("pageNum", "1");
        request.setParameter("pageSize", "10");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        TableDataInfo cached = new TableDataInfo();
        when(redisCache.getCacheObject(anyString())).thenReturn(cached);

        assertEquals(0L, controller.teachers(new TeachingSupervisionQuery()).getTotal());
        assertEquals(0L, controller.courses(new TeachingSupervisionQuery()).getTotal());
        assertEquals(0L, controller.timeline(new TeachingSupervisionQuery()).getTotal());

        verify(mapper, never()).selectTeacherSummaries(org.mockito.ArgumentMatchers.any());
        verify(mapper, never()).selectCourseSummaries(org.mockito.ArgumentMatchers.any());
        verify(mapper, never()).selectTimelineSummaries(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void cachedFullResultIsPagedInMemory()
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("pageNum", "2");
        request.setParameter("pageSize", "2");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        Map<String, Object> row1 = Collections.singletonMap("teacherId", 1L);
        Map<String, Object> row2 = Collections.singletonMap("teacherId", 2L);
        Map<String, Object> row3 = Collections.singletonMap("teacherId", 3L);
        TableDataInfo cached = new TableDataInfo(Arrays.asList(row1, row2, row3), 3L);
        cached.setCode(200);
        when(redisCache.getCacheObject(anyString())).thenReturn(cached);

        TableDataInfo result = controller.teachers(new TeachingSupervisionQuery());

        assertEquals(3L, result.getTotal());
        assertEquals(Collections.singletonList(row3), result.getRows());
        verify(mapper, never()).selectTeacherSummaries(org.mockito.ArgumentMatchers.any());
    }
}
