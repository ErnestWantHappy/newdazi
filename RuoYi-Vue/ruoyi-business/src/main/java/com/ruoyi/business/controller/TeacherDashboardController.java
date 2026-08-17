package com.ruoyi.business.controller;

import com.ruoyi.business.domain.vo.GradeGroupVo;
import com.ruoyi.business.domain.vo.PracticalGradingStatusVo;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.service.IBizLessonService;
import com.ruoyi.business.service.PracticalGradingDeadlineService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;

/**
 * 教师端首页仪表盘
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/teacher")
@PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('admin')")
public class TeacherDashboardController extends BaseController
{
    private static final String DASHBOARD_CACHE_PREFIX = "business:teacher-dashboard:v1:";
    private static final int DASHBOARD_CACHE_SECONDS = 30;

    @Autowired
    private IBizLessonService lessonService;

    @Autowired
    private PracticalGradingDeadlineService deadlineService;

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    @Autowired
    private RedisCache redisCache;

    private final Map<String, Object> dashboardCacheLocks = new ConcurrentHashMap<>();

    /**
     * 获取教师首页的完整数据
     */
    @GetMapping("/dashboard-data")
    public AjaxResult getDashboardData()
    {
        String cacheKey = DASHBOARD_CACHE_PREFIX + SecurityUtils.getUserId() + ":" + SecurityUtils.getDeptId();
        AjaxResult cached = redisCache.getCacheObject(cacheKey);
        if (cached != null)
        {
            return cached;
        }
        Object cacheLock = dashboardCacheLocks.computeIfAbsent(cacheKey, key -> new Object());
        try
        {
            // 教师首页包含多次班级、课程和指派聚合，同一教师瞬时并发只回源一次。
            synchronized (cacheLock)
            {
                cached = redisCache.getCacheObject(cacheKey);
                if (cached != null)
                {
                    return cached;
                }
                List<GradeGroupVo> dashboardData = lessonService.getTeacherDashboardData();
                AjaxResult result = AjaxResult.success(dashboardData);
                redisCache.setCacheObject(cacheKey, result, DASHBOARD_CACHE_SECONDS, TimeUnit.SECONDS);
                return result;
            }
        }
        finally
        {
            dashboardCacheLocks.remove(cacheKey, cacheLock);
        }
    }

    /**
     * 课程卡片先显示，操作题红点随后异步补充，避免逐班期限计算阻塞首页首屏。
     */
    @PostMapping("/dashboard-practical-status")
    public AjaxResult getDashboardPracticalStatus(@RequestBody(required = false) List<Long> lessonIds)
    {
        List<Long> normalizedIds = lessonIds == null ? new ArrayList<>() : lessonIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        if (normalizedIds.size() > 100)
        {
            throw new ServiceException("一次最多查询100门课程的批改状态");
        }
        Long deptId = SecurityUtils.getDeptId();
        Long userId = SecurityUtils.getUserId();
        Map<Long, List<PracticalGradingStatusVo>> statusByLesson = new LinkedHashMap<>();
        for (Long lessonId : normalizedIds)
        {
            List<PracticalGradingStatusVo> classStatuses = new ArrayList<>();
            // 查询本身与教师管班、学校和真实指派/答题事实相交，未知或越界课程不会返回班级状态。
            List<Map<String, Object>> classRows = studentAnswerMapper.selectClassStatusByLesson(
                    lessonId, userId, deptId);
            for (Map<String, Object> classRow : classRows)
            {
                String entryYear = String.valueOf(classRow.get("entryYear"));
                String classCode = String.valueOf(classRow.get("classCode"));
                classStatuses.add(deadlineService.getStatus(
                        lessonId, deptId, entryYear, classCode, true));
            }
            statusByLesson.put(lessonId, classStatuses);
        }
        return AjaxResult.success(statusByLesson);
    }

}
