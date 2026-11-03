package com.ruoyi.business.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizPracticalGradingDeadline;
import com.ruoyi.business.domain.vo.PracticalGradingStatusVo;
import com.ruoyi.business.mapper.PracticalGradingDeadlineMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.domain.SysConfig;

/**
 * 操作题批改期限的唯一领域入口。
 */
@Service
public class PracticalGradingDeadlineService
{
    public static final String DEADLINE_DAYS_KEY = "business.practicalGrading.deadlineDays";
    private static final long DUE_SOON_MILLIS = 72L * 60L * 60L * 1000L;
    private static final long DASHBOARD_CACHE_TTL_MS = 45_000L;
    private static final int DASHBOARD_CACHE_MAX_SIZE = 5000;

    private final Map<String, CachedStatus> dashboardCache = new ConcurrentHashMap<>();

    @Autowired
    private PracticalGradingDeadlineMapper deadlineMapper;

    @Autowired
    private ISysConfigService configService;

    public PracticalGradingStatusVo getStatus(Long lessonId, Long deptId, String entryYear,
                                              String classCode, boolean ensureTrigger)
    {
        return getStatus(lessonId, deptId, entryYear, classCode, ensureTrigger, "COMPENSATION");
    }

    public void checkAndCreateDeadline(Long lessonId, Long deptId, String entryYear, String classCode)
    {
        getStatus(lessonId, deptId, entryYear, classCode, true, "REALTIME");
    }

    /**
     * 教师首页红点每 10 秒轮询全部课程 × 班级，相同班级在短窗口内重复计算；
     * 只对 dashboard 读路径加 45 秒缓存，批改锁定判断仍走无缓存 getStatus。
     */
    public PracticalGradingStatusVo getStatusForDashboard(Long lessonId, Long deptId,
                                                          String entryYear, String classCode)
    {
        String key = dashCacheKey(lessonId, deptId, entryYear, classCode);
        long now = System.currentTimeMillis();
        CachedStatus cached = dashboardCache.get(key);
        if (cached != null && cached.expiresAt > now)
        {
            return cached.status;
        }
        PracticalGradingStatusVo status = getStatus(lessonId, deptId, entryYear, classCode, true);
        if (dashboardCache.size() >= DASHBOARD_CACHE_MAX_SIZE)
        {
            dashboardCache.clear();
        }
        dashboardCache.put(key, new CachedStatus(status, now + DASHBOARD_CACHE_TTL_MS));
        return status;
    }

    private String dashCacheKey(Long lessonId, Long deptId, String entryYear, String classCode)
    {
        String year = entryYear == null ? "" : entryYear.trim();
        return lessonId + ":" + deptId + ":" + year + ":" + normalizeClassCode(classCode);
    }

    private void evictDashboardCache(Long lessonId, Long deptId, String entryYear, String classCode)
    {
        dashboardCache.remove(dashCacheKey(lessonId, deptId, entryYear, classCode));
    }

    private static final class CachedStatus
    {
        private final PracticalGradingStatusVo status;
        private final long expiresAt;

        private CachedStatus(PracticalGradingStatusVo status, long expiresAt)
        {
            this.status = status;
            this.expiresAt = expiresAt;
        }
    }

    public void assertCanGrade(Long answerId)
    {
        Map<String, Object> key = deadlineMapper.selectClassKeyByAnswerId(answerId);
        if (key == null || key.isEmpty())
        {
            throw new ServiceException("答题记录所属课程班级不完整");
        }
        PracticalGradingStatusVo status = getStatus(
                longValue(key, "lessonId"),
                longValue(key, "deptId"),
                stringValue(key, "entryYear"),
                stringValue(key, "classCode"),
                true);
        if (!status.isCanGrade())
        {
            throw new ServiceException("已逾期，操作题批改已锁定；如需继续批改，请联系教研员调整截止时间");
        }
    }

    public int getDeadlineDays()
    {
        return readDeadlineDays();
    }

    public void updateDeadlineDays(int days, String operator)
    {
        if (days < 1 || days > 365)
        {
            throw new ServiceException("批改期限必须在1至365天之间");
        }
        SysConfig query = new SysConfig();
        query.setConfigKey(DEADLINE_DAYS_KEY);
        List<SysConfig> rows = configService.selectConfigList(query);
        if (rows == null || rows.isEmpty())
        {
            throw new ServiceException("操作题批改期限配置不存在，请先执行增量SQL");
        }
        SysConfig config = rows.get(0);
        config.setConfigValue(String.valueOf(days));
        config.setUpdateBy(operator);
        configService.updateConfig(config);
    }

    @Transactional(rollbackFor = Exception.class)
    public PracticalGradingStatusVo adjustDeadline(Long deadlineId, Date newDeadlineTime,
                                                   String reason, Long operatorId, String operatorName)
    {
        if (deadlineId == null || newDeadlineTime == null || reason == null || reason.trim().isEmpty())
        {
            throw new ServiceException("期限、调整时间和原因不能为空");
        }
        if (reason.trim().length() > 500)
        {
            throw new ServiceException("调整原因不能超过500个字符");
        }
        BizPracticalGradingDeadline deadline = deadlineMapper.selectDeadlineById(deadlineId);
        if (deadline == null)
        {
            throw new ServiceException("批改期限不存在");
        }
        Date now = new Date();
        Date oldDeadlineTime = deadline.getCurrentDeadlineTime();
        if (!newDeadlineTime.after(oldDeadlineTime))
        {
            throw new ServiceException("新截止时间必须晚于当前有效截止时间");
        }
        String actionType = now.after(oldDeadlineTime) ? "REOPEN" : "EXTEND";
        if ("REOPEN".equals(actionType) && !newDeadlineTime.after(now))
        {
            throw new ServiceException("重新开放后的截止时间必须晚于当前时间");
        }
        int rows = deadlineMapper.updateCurrentDeadline(
                deadlineId, oldDeadlineTime, newDeadlineTime, actionType, operatorName);
        if (rows != 1)
        {
            throw new ServiceException("截止时间已被其他操作修改，请刷新后重试");
        }
        deadlineMapper.insertDeadlineAudit(deadline, actionType, oldDeadlineTime,
                newDeadlineTime, reason.trim(), operatorId, operatorName);
        evictDashboardCache(deadline.getLessonId(), deadline.getDeptId(),
                deadline.getEntryYear(), deadline.getClassCode());
        return getStatus(deadline.getLessonId(), deadline.getDeptId(), deadline.getEntryYear(),
                deadline.getClassCode(), false);
    }

    public List<Map<String, Object>> getAdjustmentHistory(Long deadlineId)
    {
        if (deadlineMapper.selectDeadlineById(deadlineId) == null)
        {
            throw new ServiceException("批改期限不存在");
        }
        return deadlineMapper.selectDeadlineAudits(deadlineId);
    }

    public Map<String, Object> reconcileTriggers()
    {
        int scanned = 0;
        int created = 0;
        long afterScopeId = 0L;
        final int pageSize = 200;
        while (true)
        {
            List<Map<String, Object>> rows = deadlineMapper.selectUntriggeredPracticalClasses(afterScopeId, pageSize);
            if (rows == null || rows.isEmpty())
            {
                break;
            }
            for (Map<String, Object> row : rows)
            {
                scanned++;
                Long lessonId = longValue(row, "lessonId");
                Long deptId = longValue(row, "deptId");
                String entryYear = stringValue(row, "entryYear");
                String classCode = stringValue(row, "classCode");
                BizPracticalGradingDeadline before = deadlineMapper.selectDeadline(
                        lessonId, deptId, entryYear, classCode);
                checkAndCreateDeadline(lessonId, deptId, entryYear, classCode);
                BizPracticalGradingDeadline after = deadlineMapper.selectDeadline(
                        lessonId, deptId, entryYear, classCode);
                if (before == null && after != null)
                {
                    created++;
                }
                Long scopeId = longValue(row, "scopeId");
                if (scopeId != null)
                {
                    afterScopeId = Math.max(afterScopeId, scopeId);
                }
            }
            if (rows.size() < pageSize)
            {
                break;
            }
        }
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("scanned", scanned);
        result.put("created", created);
        return result;
    }

    private PracticalGradingStatusVo getStatus(Long lessonId, Long deptId, String entryYear,
                                                String classCode, boolean ensureTrigger, String source)
    {
        validateKey(lessonId, deptId, entryYear, classCode);
        Map<String, Object> metrics = deadlineMapper.selectClassMetrics(
                lessonId, deptId, entryYear.trim(), normalizeClassCode(classCode));
        boolean hasPractical = boolValue(metrics, "hasPractical");
        int total = intValue(metrics, "totalStudentCount");
        int answered = intValue(metrics, "answeredStudentCount");
        int submitted = intValue(metrics, "practicalSubmittedStudentCount");

        BizPracticalGradingDeadline deadline = deadlineMapper.selectDeadline(
                lessonId, deptId, entryYear.trim(), normalizeClassCode(classCode));
        // 打字、理论等可能分课时先做，只有正式操作作品达到半班才启动批改倒计时。
        if (ensureTrigger && deadline == null && hasPractical && total > 0 && submitted * 2 >= total)
        {
            Date thresholdTime = dateValue(metrics, "thresholdTime");
            if (thresholdTime == null)
            {
                thresholdTime = new Date();
            }
            createDeadline(lessonId, deptId, entryYear.trim(), normalizeClassCode(classCode),
                    submitted, total, thresholdTime, source);
            deadline = deadlineMapper.selectDeadline(
                    lessonId, deptId, entryYear.trim(), normalizeClassCode(classCode));
        }
        return buildStatus(lessonId, deptId, entryYear.trim(), normalizeClassCode(classCode),
                metrics, deadline, hasPractical, answered, total);
    }

    private void createDeadline(Long lessonId, Long deptId, String entryYear, String classCode,
                                int answered, int total, Date triggerTime, String source)
    {
        int days = readDeadlineDays();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(triggerTime);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        Date dueTime = calendar.getTime();

        BizPracticalGradingDeadline deadline = new BizPracticalGradingDeadline();
        deadline.setLessonId(lessonId);
        deadline.setDeptId(deptId);
        deadline.setEntryYear(entryYear);
        deadline.setClassCode(classCode);
        deadline.setTriggerTime(triggerTime);
        deadline.setTriggerAnsweredCount(answered);
        deadline.setTriggerStudentCount(total);
        deadline.setDeadlineDays(days);
        deadline.setOriginalDeadlineTime(dueTime);
        deadline.setCurrentDeadlineTime(dueTime);
        deadline.setInitializationSource(source);
        deadline.setCreateBy("system");
        deadlineMapper.insertDeadlineIgnore(deadline);
        evictDashboardCache(lessonId, deptId, entryYear, classCode);
    }

    private PracticalGradingStatusVo buildStatus(Long lessonId, Long deptId, String entryYear,
                                                  String classCode, Map<String, Object> metrics,
                                                  BizPracticalGradingDeadline deadline,
                                                  boolean hasPractical, int answered, int total)
    {
        Date now = new Date();
        int due = intValue(metrics, "dueCount");
        int graded = intValue(metrics, "gradedCount");
        PracticalGradingStatusVo status = new PracticalGradingStatusVo();
        status.setLessonId(lessonId);
        status.setDeptId(deptId);
        status.setEntryYear(entryYear);
        status.setClassCode(classCode);
        status.setHasPractical(hasPractical);
        status.setAnsweredStudentCount(answered);
        int submitted = intValue(metrics, "practicalSubmittedStudentCount");
        status.setPracticalSubmittedStudentCount(submitted);
        status.setTotalStudentCount(total);
        status.setRemainingStudentsToTrigger(Math.max(0, (total + 1) / 2 - submitted));
        status.setDueCount(due);
        status.setGradedCount(graded);
        status.setUngradedCount(Math.max(0, due - graded));
        status.setServerNow(now);
        status.setCanGrade(true);

        if (!hasPractical)
        {
            status.setStatusCode("NO_PRACTICAL");
        }
        else if (deadline == null)
        {
            status.setStatusCode("NOT_TRIGGERED");
        }
        else
        {
            status.setDeadlineId(deadline.getDeadlineId());
            status.setTriggerTime(deadline.getTriggerTime());
            status.setCurrentDeadlineTime(deadline.getCurrentDeadlineTime());
            status.setLastAdjustmentType(deadline.getLastAdjustmentType());
            long remaining = deadline.getCurrentDeadlineTime().getTime() - now.getTime();
            boolean completed = due > 0 && due == graded;
            if (completed)
            {
                // 已全部批完是教师最关心的最终业务结论；逾期后仍禁止改分，但不把完成状态改写成逾期。
                status.setStatusCode("COMPLETED");
                status.setCanGrade(remaining > 0);
            }
            else if (remaining <= 0)
            {
                status.setStatusCode("OVERDUE");
                status.setCanGrade(false);
            }
            else if ("REOPEN".equals(deadline.getLastAdjustmentType()))
            {
                status.setStatusCode("REOPENED");
            }
            else if (remaining <= DUE_SOON_MILLIS)
            {
                status.setStatusCode("DUE_SOON");
            }
            else
            {
                status.setStatusCode("GRADING");
            }
        }
        return status;
    }

    private int readDeadlineDays()
    {
        try
        {
            int days = Integer.parseInt(configService.selectConfigByKey(DEADLINE_DAYS_KEY));
            return days >= 1 && days <= 365 ? days : 21;
        }
        catch (Exception ignored)
        {
            return 21;
        }
    }

    private void validateKey(Long lessonId, Long deptId, String entryYear, String classCode)
    {
        if (lessonId == null || deptId == null || entryYear == null || classCode == null)
        {
            throw new ServiceException("课程班级期限参数不完整");
        }
    }

    private String normalizeClassCode(String value) { return value.replace("班", "").trim(); }
    private static int intValue(Map<String, Object> map, String key) {
        Object value = findValue(map, key);
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }
    private static Long longValue(Map<String, Object> map, String key) {
        Object value = findValue(map, key);
        return value instanceof Number ? ((Number) value).longValue() : null;
    }
    private static boolean boolValue(Map<String, Object> map, String key) {
        Object value = findValue(map, key);
        return value instanceof Boolean ? (Boolean) value
                : value instanceof Number && ((Number) value).intValue() != 0;
    }
    private static Date dateValue(Map<String, Object> map, String key) {
        Object value = findValue(map, key);
        return value instanceof Date ? (Date) value : null;
    }
    private static String stringValue(Map<String, Object> map, String key) {
        Object value = findValue(map, key);
        return value == null ? null : String.valueOf(value);
    }
    private static Object findValue(Map<String, Object> map, String key) {
        if (map == null) return null;
        if (map.containsKey(key)) return map.get(key);
        String lower = key.toLowerCase(java.util.Locale.ROOT);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().toLowerCase(java.util.Locale.ROOT).equals(lower)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
