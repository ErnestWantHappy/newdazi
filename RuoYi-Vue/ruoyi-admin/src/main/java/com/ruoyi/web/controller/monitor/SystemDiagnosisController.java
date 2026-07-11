package com.ruoyi.web.controller.monitor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import com.alibaba.druid.stat.DruidStatManagerFacade;
import com.ruoyi.business.utils.FileConversionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.diagnosis.DiagnosisAdvisor;
import com.ruoyi.common.utils.diagnosis.DiagnosisAdvice;
import com.ruoyi.common.utils.diagnosis.JobBusinessDescriber;
import com.ruoyi.common.utils.sql.SqlBusinessDescriber;
import com.ruoyi.framework.web.domain.Server;
import com.ruoyi.system.domain.SysPerfEvent;
import com.ruoyi.system.service.ISysPerfEventService;

/**
 * 系统诊断中心
 */
@RestController
@RequestMapping("/monitor/diagnosis")
public class SystemDiagnosisController
{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired(required = false)
    @Qualifier("threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired(required = false)
    @Qualifier("conversionExecutor")
    private ThreadPoolTaskExecutor conversionExecutor;

    @Autowired
    private ISysPerfEventService perfEventService;

    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @GetMapping("/summary")
    public AjaxResult summary(@RequestParam(value = "hours", defaultValue = "24") int hours)
    {
        int diagnosisHours = normalizeHours(hours);
        Map<String, Object> data = new LinkedHashMap<>();
        Server server = collectServer();
        Map<String, Object> cache = collectCache();
        List<Map<String, Object>> slowSql = collectSlowSql();
        List<Map<String, Object>> errors = queryRecentErrors(diagnosisHours);
        List<Map<String, Object>> slowOperations = querySlowOperations(diagnosisHours);
        List<Map<String, Object>> jobs = collectJobs();
        Map<String, Object> conversion = collectConversion();

        enrichErrors(errors);
        enrichSlowOperations(slowOperations);
        enrichSlowSql(slowSql);
        jobs.forEach(JobBusinessDescriber::enrichJobRow);

        double memUsage = server.getMem() == null ? 0D : server.getMem().getUsage();
        double jvmUsage = server.getJvm() == null ? 0D : server.getJvm().getUsage();
        boolean redisAvailable = (Boolean) cache.getOrDefault("available", false);
        List<Map<String, Object>> adviceSummary = DiagnosisAdvisor.buildAdviceSummary(
                diagnosisHours, memUsage, jvmUsage, redisAvailable, errors, slowOperations, slowSql);

        data.put("diagnosisHours", diagnosisHours);
        data.put("server", server);
        data.put("cache", cache);
        data.put("threadPool", collectThreadPool());
        data.put("conversion", conversion);
        data.put("onlineCount", countOnlineUsers());
        data.put("dataSources", collectDataSources());
        data.put("slowSql", slowSql);
        data.put("recentErrors", errors);
        data.put("slowOperations", slowOperations);
        data.put("topInterfaces", queryTopInterfaces(diagnosisHours));
        data.put("jobs", jobs);
        data.put("adviceSummary", adviceSummary);
        data.put("health", buildHealth(server, cache, slowSql, errors, slowOperations, conversion, diagnosisHours));
        data.put("report", buildReport(data, adviceSummary, perfEventService.selectRecentEvents(diagnosisHours, null)));
        return AjaxResult.success(data);
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @GetMapping("/events")
    public AjaxResult events(@RequestParam(value = "hours", defaultValue = "24") int hours,
            @RequestParam(value = "type", required = false) String type)
    {
        List<SysPerfEvent> events = perfEventService.selectRecentEvents(hours, type);
        return AjaxResult.success(events);
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @PostMapping("/libre-office/cleanup")
    public AjaxResult cleanupLibreOffice()
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", FileConversionUtils.cleanupAndRestartForMaintenance());
        result.put("libreOffice", FileConversionUtils.getHealthSnapshot());
        return AjaxResult.success(result);
    }

    private int normalizeHours(int hours)
    {
        return Math.min(Math.max(hours, 1), 168);
    }

    private List<Map<String, Object>> queryRecentErrors(int hours)
    {
        return queryList(
                "select oper_id, title, oper_name, dept_name, oper_url, request_method, oper_time, cost_time, error_msg "
                        + "from sys_oper_log where status = 1 and oper_time >= date_sub(now(), interval "
                        + hours + " hour) order by oper_time desc limit 10");
    }

    private List<Map<String, Object>> querySlowOperations(int hours)
    {
        return queryList(
                "select oper_id, title, oper_name, dept_name, oper_url, request_method, oper_time, cost_time, status, error_msg "
                        + "from sys_oper_log where cost_time >= 1000 and oper_time >= date_sub(now(), interval "
                        + hours + " hour) order by cost_time desc, oper_time desc limit 10");
    }

    private List<Map<String, Object>> queryTopInterfaces(int hours)
    {
        return queryList(
                "select oper_url, title, count(1) as request_count, round(avg(cost_time), 0) as avg_cost, max(cost_time) as max_cost "
                        + "from sys_oper_log where oper_time >= date_sub(now(), interval "
                        + hours + " hour) group by oper_url, title order by request_count desc, max_cost desc limit 10");
    }

    private List<Map<String, Object>> collectJobs()
    {
        return queryList(
                "select job_id, job_name, job_group, invoke_target, cron_expression, status "
                        + "from sys_job order by case when invoke_target like 'ryTask.%' then 1 else 0 end, job_id desc limit 12");
    }

    private void enrichErrors(List<Map<String, Object>> rows)
    {
        for (Map<String, Object> row : rows)
        {
            DiagnosisAdvisor.enrichRow(row, DiagnosisAdvisor.adviseForError(row));
        }
    }

    private void enrichSlowOperations(List<Map<String, Object>> rows)
    {
        for (Map<String, Object> row : rows)
        {
            DiagnosisAdvisor.enrichRow(row, DiagnosisAdvisor.adviseForSlowApi(row));
        }
    }

    private void enrichSlowSql(List<Map<String, Object>> rows)
    {
        for (Map<String, Object> row : rows)
        {
            DiagnosisAdvice advice = DiagnosisAdvisor.adviseForSlowSql(row);
            row.put("category", advice.getCategory());
            if (!"critical".equals(row.get("severity")))
            {
                row.put("severity", advice.getSeverity());
            }
            row.put("advice", advice.getAdvice());
        }
    }

    private Server collectServer()
    {
        try
        {
            Server server = new Server();
            server.copyTo();
            return server;
        }
        catch (Exception e)
        {
            return new Server();
        }
    }

    private Map<String, Object> collectCache()
    {
        Map<String, Object> cache = new LinkedHashMap<>();
        try
        {
            Properties info = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.info());
            Object dbSize = redisTemplate.execute((RedisCallback<Object>) connection -> connection.dbSize());
            cache.put("available", true);
            cache.put("info", info);
            cache.put("dbSize", dbSize);
        }
        catch (Exception e)
        {
            cache.put("available", false);
            cache.put("error", e.getMessage());
            cache.put("dbSize", 0);
        }
        return cache;
    }

    private Map<String, Object> collectThreadPool()
    {
        Map<String, Object> data = new LinkedHashMap<>();
        if (threadPoolTaskExecutor == null)
        {
            data.put("available", false);
            return data;
        }
        ThreadPoolExecutor executor = threadPoolTaskExecutor.getThreadPoolExecutor();
        data.put("available", true);
        data.put("activeCount", executor.getActiveCount());
        data.put("poolSize", executor.getPoolSize());
        data.put("corePoolSize", executor.getCorePoolSize());
        data.put("maximumPoolSize", executor.getMaximumPoolSize());
        data.put("queueSize", executor.getQueue().size());
        data.put("queueRemainingCapacity", executor.getQueue().remainingCapacity());
        data.put("completedTaskCount", executor.getCompletedTaskCount());
        return data;
    }

    private Map<String, Object> collectConversion()
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("libreOffice", FileConversionUtils.getHealthSnapshot());
        if (conversionExecutor == null)
        {
            data.put("available", false);
            return data;
        }
        ThreadPoolExecutor executor = conversionExecutor.getThreadPoolExecutor();
        int queueSize = executor.getQueue().size();
        int remainingCapacity = executor.getQueue().remainingCapacity();
        data.put("available", true);
        data.put("activeCount", executor.getActiveCount());
        data.put("poolSize", executor.getPoolSize());
        data.put("corePoolSize", executor.getCorePoolSize());
        data.put("maximumPoolSize", executor.getMaximumPoolSize());
        data.put("queueSize", queueSize);
        data.put("queueRemainingCapacity", remainingCapacity);
        data.put("queueCapacity", queueSize + remainingCapacity);
        data.put("completedTaskCount", executor.getCompletedTaskCount());
        data.put("taskCount", executor.getTaskCount());
        data.put("dailyWaitingCount", countBySql(
                "select count(1) from biz_student_answer where preview_status in ('pending','converting')"));
        data.put("dailyFailedCount", countBySql(
                "select count(1) from biz_student_answer where preview_status = 'failed'"));
        data.put("countyWaitingCount", countBySql(
                "select count(1) from biz_county_exam_answer where preview_status in ('pending','converting')"));
        data.put("countyFailedCount", countBySql(
                "select count(1) from biz_county_exam_answer where preview_status = 'failed'"));
        return data;
    }

    private List<Map<String, Object>> collectSlowSql()
    {
        try
        {
            List<Map<String, Object>> sqlList = DruidStatManagerFacade.getInstance().getSqlStatDataList((Integer) null);
            return sqlList.stream()
                    .sorted(Comparator.comparingLong(this::sqlSortValue).reversed())
                    .limit(10)
                    .map(this::normalizeSqlStat)
                    .collect(Collectors.toList());
        }
        catch (Exception e)
        {
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> collectDataSources()
    {
        try
        {
            return DruidStatManagerFacade.getInstance().getDataSourceStatDataList();
        }
        catch (Exception e)
        {
            return new ArrayList<>();
        }
    }

    private Map<String, Object> normalizeSqlStat(Map<String, Object> source)
    {
        String sql = truncate(String.valueOf(source.get("SQL")), 1200);
        SqlBusinessDescriber.SqlDescription desc = SqlBusinessDescriber.describe(null, sql);
        long executeCount = number(source.get("ExecuteCount"));
        long totalTime = number(source.get("TotalTime"));
        long maxTimespan = number(source.get("MaxTimespan"));
        long runningCount = number(source.get("RunningCount"));
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("sql", sql);
        item.put("title", desc.getTitle());
        item.put("description", desc.getDescription());
        item.put("executeCount", executeCount);
        item.put("totalTime", totalTime);
        item.put("maxTimespan", maxTimespan);
        item.put("runningCount", runningCount);
        item.put("errorCount", number(source.get("ErrorCount")));
        item.put("lastTime", source.get("LastTime"));
        item.put("avgTime", executeCount > 0 ? Math.round((double) totalTime / executeCount) : 0);
        item.put("severity", maxTimespan >= 3000 || runningCount > 0 ? "critical" : "warning");
        return item;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildHealth(Server server, Map<String, Object> cache, List<Map<String, Object>> slowSql,
            List<Map<String, Object>> errors, List<Map<String, Object>> slowOperations, Map<String, Object> conversion,
            int diagnosisHours)
    {
        List<String> risks = new ArrayList<>();
        if (server.getJvm() != null && server.getJvm().getUsage() >= 80)
        {
            risks.add("JVM 内存使用率超过 80%，优先排查大文件预览、导出或批量查询。");
        }
        if (server.getMem() != null && server.getMem().getUsage() >= 85)
        {
            risks.add("服务器内存使用率超过 85%，可能存在并发压力或进程占用。");
        }
        if (!(Boolean) cache.getOrDefault("available", false))
        {
            risks.add("Redis 缓存不可用，登录令牌、验证码、防重提交和限流可能受影响。");
        }
        if (!errors.isEmpty())
        {
            risks.add("近 " + diagnosisHours + " 小时内有 " + errors.size() + " 条异常操作，请查看错误信息与处置建议。");
        }
        if (!slowOperations.isEmpty())
        {
            risks.add("近 " + diagnosisHours + " 小时内有 " + slowOperations.size() + " 个慢接口，可能影响并发体验。");
        }
        if (!slowSql.isEmpty() && errors.isEmpty() && slowOperations.isEmpty())
        {
            risks.add("Druid 已记录 SQL 耗时，建议结合慢接口和操作日志定位用户行为。");
        }
        Map<String, Object> libreOffice = conversion == null ? null : (Map<String, Object>) conversion.get("libreOffice");
        if (libreOffice != null)
        {
            if (Boolean.TRUE.equals(libreOffice.get("excessiveProcesses")))
            {
                risks.add("LibreOffice 进程数超过阈值，建议执行维护清理任务并观察转换队列。");
            }
            if (!Boolean.TRUE.equals(libreOffice.get("serviceAvailable")))
            {
                risks.add("LibreOffice 服务池当前不可用，操作题预览转换可能排队或无法生成。");
            }
        }
        if (conversion != null && number(conversion.get("queueSize")) > 0)
        {
            risks.add("文档转换队列存在等待任务，集中上传时请关注 conversion 线程池消费速度。");
        }
        if (conversion != null && number(conversion.get("dailyFailedCount")) + number(conversion.get("countyFailedCount")) > 0)
        {
            risks.add("存在文档预览转换失败记录，学生端会保持温和提示，后台需结合日志排查 LibreOffice。");
        }
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("level", risks.isEmpty() ? "stable" : "warning");
        health.put("title", risks.isEmpty() ? "当前未发现明显风险" : "发现需要关注的运行信号");
        health.put("risks", risks);
        health.put("scopeLabel", formatScopeLabel(diagnosisHours));
        return health;
    }

    @SuppressWarnings("unchecked")
    private String buildReport(Map<String, Object> data, List<Map<String, Object>> adviceSummary, List<SysPerfEvent> perfEvents)
    {
        int hours = data.get("diagnosisHours") instanceof Number
                ? ((Number) data.get("diagnosisHours")).intValue()
                : 24;
        StringBuilder builder = new StringBuilder();
        builder.append("【系统诊断报告】统计范围：").append(formatScopeLabel(hours)).append("\n\n");

        Map<String, Object> health = (Map<String, Object>) data.get("health");
        Server server = (Server) data.get("server");
        List<Map<String, Object>> errors = (List<Map<String, Object>>) data.get("recentErrors");
        List<Map<String, Object>> slowOperations = (List<Map<String, Object>>) data.get("slowOperations");
        Map<String, Object> conversion = (Map<String, Object>) data.get("conversion");
        Map<String, Object> libreOffice = conversion == null ? null : (Map<String, Object>) conversion.get("libreOffice");

        builder.append("【当前结论】\n");
        builder.append("- 资源：");
        if (server.getMem() != null && server.getMem().getUsage() >= 85)
        {
            builder.append("服务器内存 ").append(Math.round(server.getMem().getUsage())).append("%，需关注\n");
        }
        else if (server.getJvm() != null && server.getJvm().getUsage() >= 80)
        {
            builder.append("JVM 内存 ").append(Math.round(server.getJvm().getUsage())).append("%，需关注\n");
        }
        else
        {
            builder.append("资源压力暂无明显异常\n");
        }
        builder.append("- 业务：近 ").append(hours).append(" 小时");
        builder.append(errors == null || errors.isEmpty() ? "无新增异常\n" : "有 " + errors.size() + " 条异常记录\n");
        builder.append("- 性能：近 ").append(hours).append(" 小时");
        builder.append(slowOperations == null || slowOperations.isEmpty() ? "无慢接口\n" : "有 " + slowOperations.size() + " 个慢接口\n");
        if (conversion != null)
        {
            builder.append("- 转换：活跃线程 ").append(conversion.getOrDefault("activeCount", 0))
                    .append("，队列 ").append(conversion.getOrDefault("queueSize", 0))
                    .append("，LibreOffice 进程 ")
                    .append(libreOffice == null ? 0 : libreOffice.getOrDefault("processCount", 0))
                    .append("\n");
        }

        builder.append("\n【优先处理】\n");
        if (adviceSummary == null || adviceSummary.isEmpty())
        {
            builder.append("- 暂无需紧急处理的事项\n");
        }
        else
        {
            int index = 1;
            for (Map<String, Object> item : adviceSummary)
            {
                builder.append(index++).append(". [")
                        .append(categoryLabel(String.valueOf(item.get("category")))).append("] ")
                        .append(item.get("label")).append(" → ")
                        .append(item.get("advice")).append("\n");
            }
        }

        appendDetailRows(builder, "明细摘录-最近错误", errors, "oper_time", "title", "advice");
        appendDetailRows(builder, "明细摘录-慢接口", slowOperations, "oper_time", "title", "advice");
        appendDetailRows(builder, "明细摘录-慢 SQL", (List<Map<String, Object>>) data.get("slowSql"), "lastTime", "title", "advice");

        builder.append("\n【后台任务巡检】\n");
        List<Map<String, Object>> jobs = (List<Map<String, Object>>) data.get("jobs");
        if (jobs == null || jobs.isEmpty())
        {
            builder.append("- 暂无后台任务\n");
        }
        else
        {
            jobs.stream().limit(6).forEach(job -> {
                String category = String.valueOf(job.get("taskCategory"));
                if ("framework_demo".equals(category))
                {
                    builder.append("- ").append(job.get("displayName")).append("：演示任务，可忽略\n");
                }
                else
                {
                    builder.append("- ").append(job.get("displayName")).append("：")
                            .append("0".equals(String.valueOf(job.get("status"))) ? "运行中" : "已暂停")
                            .append("，").append(job.get("scheduleDesc"))
                            .append("，").append(job.get("purpose")).append("\n");
                }
            });
        }

        builder.append("\n【性能事件时间线】\n");
        if (perfEvents == null || perfEvents.isEmpty())
        {
            builder.append("- 近 ").append(hours).append(" 小时内暂无持久化性能事件\n");
        }
        else
        {
            perfEvents.stream().limit(5).forEach(event -> builder.append("- ")
                    .append(event.getOccurTime()).append(" | ")
                    .append(event.getEventType()).append(" | ")
                    .append(event.getTitle()).append(" | ")
                    .append(event.getDurationMs()).append(" ms\n"));
        }

        builder.append("\n健康状态：").append(health.get("title")).append("\n");
        builder.append("在线用户：").append(data.get("onlineCount")).append("\n");
        return builder.toString();
    }

    private void appendDetailRows(StringBuilder builder, String title, List<Map<String, Object>> rows,
            String timeKey, String labelKey, String adviceKey)
    {
        builder.append("\n【").append(title).append("】\n");
        if (rows == null || rows.isEmpty())
        {
            builder.append("- 暂无记录\n");
            return;
        }
        rows.stream().limit(3).forEach(row -> builder.append("- ")
                .append(value(row, timeKey)).append(" | ")
                .append(value(row, labelKey)).append(" | ")
                .append(truncate(String.valueOf(value(row, adviceKey)), 120)).append("\n"));
    }

    private String formatScopeLabel(int hours)
    {
        return hours >= 168 ? "近 7 天" : ("近 " + hours + " 小时");
    }

    private String categoryLabel(String category)
    {
        switch (category)
        {
            case "resource":
                return "资源";
            case "business":
                return "业务";
            case "performance":
                return "性能";
            default:
                return "系统";
        }
    }

    private Object value(Map<String, Object> row, String key)
    {
        Object value = row.get(key);
        if (value == null)
        {
            value = row.get(toCamelCase(key));
        }
        return value == null ? "" : value;
    }

    private String toCamelCase(String value)
    {
        StringBuilder builder = new StringBuilder();
        boolean upper = false;
        for (char c : value.toCharArray())
        {
            if (c == '_')
            {
                upper = true;
            }
            else if (upper)
            {
                builder.append(Character.toUpperCase(c));
                upper = false;
            }
            else
            {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private Long countOnlineUsers()
    {
        try
        {
            return (long) redisTemplate.keys(CacheConstants.LOGIN_TOKEN_KEY + "*").size();
        }
        catch (Exception e)
        {
            return 0L;
        }
    }

    private List<Map<String, Object>> queryList(String sql)
    {
        try
        {
            return jdbcTemplate.queryForList(sql);
        }
        catch (Exception e)
        {
            return new ArrayList<>();
        }
    }

    private long countBySql(String sql)
    {
        try
        {
            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return count == null ? 0L : count;
        }
        catch (Exception e)
        {
            return 0L;
        }
    }

    private long sqlSortValue(Map<String, Object> source)
    {
        long maxTimespan = number(source.get("MaxTimespan"));
        long totalTime = number(source.get("TotalTime"));
        return Math.max(maxTimespan, totalTime);
    }

    private long number(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        try
        {
            return Long.parseLong(String.valueOf(value));
        }
        catch (Exception e)
        {
            return 0L;
        }
    }

    private String truncate(String value, int maxLength)
    {
        if (value == null)
        {
            return "";
        }
        return value.length() > maxLength ? value.substring(0, maxLength) + "..." : value;
    }
}
