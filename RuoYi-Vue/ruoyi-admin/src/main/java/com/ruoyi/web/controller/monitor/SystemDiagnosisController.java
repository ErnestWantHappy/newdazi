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

    /**
     * 单个探针超时预算：collectServer 内含固定 1 秒 OSHI tick 差分采样，
     * 预算必须明显大于采样窗口，否则冷启动/慢查询时会频繁降级为空数据假象。
     */
    private static final int PROBE_TIMEOUT_SECONDS = 4;

    /**
     * 日志表行数超过该阈值时，topInterfaces 聚合改走轻量 sys_perf_event，避免大表全窗口 Group By。
     */
    private static final long OPER_LOG_FALLBACK_ROWS = 1_000_000L;

    /** 探针专用守护线程池：与业务线程池隔离，池内任务彼此独立、互不阻塞。 */
    private static final java.util.concurrent.ExecutorService PROBE_POOL =
            java.util.concurrent.Executors.newFixedThreadPool(8, r -> {
                Thread t = new Thread(r, "diagnosis-probe");
                t.setDaemon(true);
                return t;
            });

    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @GetMapping("/summary")
    public AjaxResult summary(@RequestParam(value = "hours", defaultValue = "24") int hours)
    {
        int diagnosisHours = normalizeHours(hours);
        Map<String, Object> data = new LinkedHashMap<>();

        // 所有探针并行发射，主线程按各自 2 秒预算收割，任一慢任务只影响自己那一块数据。
        java.util.concurrent.Future<Server> serverF =
                java.util.concurrent.CompletableFuture.supplyAsync(this::collectServer, PROBE_POOL);
        java.util.concurrent.Future<Map<String, Object>> cacheF =
                java.util.concurrent.CompletableFuture.supplyAsync(this::collectCache, PROBE_POOL);
        java.util.concurrent.Future<List<Map<String, Object>>> slowSqlF =
                java.util.concurrent.CompletableFuture.supplyAsync(this::collectSlowSql, PROBE_POOL);
        java.util.concurrent.Future<List<Map<String, Object>>> errorsF =
                java.util.concurrent.CompletableFuture.supplyAsync(() -> queryRecentErrors(diagnosisHours), PROBE_POOL);
        java.util.concurrent.Future<List<Map<String, Object>>> slowOperationsF =
                java.util.concurrent.CompletableFuture.supplyAsync(() -> querySlowOperations(diagnosisHours), PROBE_POOL);
        java.util.concurrent.Future<List<Map<String, Object>>> topInterfacesF =
                java.util.concurrent.CompletableFuture.supplyAsync(() -> queryTopInterfaces(diagnosisHours), PROBE_POOL);
        java.util.concurrent.Future<List<Map<String, Object>>> jobsF =
                java.util.concurrent.CompletableFuture.supplyAsync(this::collectJobs, PROBE_POOL);
        java.util.concurrent.Future<Map<String, Object>> conversionF =
                java.util.concurrent.CompletableFuture.supplyAsync(this::collectConversion, PROBE_POOL);
        java.util.concurrent.Future<Map<String, Object>> threadPoolF =
                java.util.concurrent.CompletableFuture.supplyAsync(this::collectThreadPool, PROBE_POOL);
        java.util.concurrent.Future<Long> onlineCountF =
                java.util.concurrent.CompletableFuture.supplyAsync(this::countOnlineUsers, PROBE_POOL);
        java.util.concurrent.Future<List<Map<String, Object>>> dataSourcesF =
                java.util.concurrent.CompletableFuture.supplyAsync(this::collectDataSources, PROBE_POOL);
        java.util.concurrent.Future<List<SysPerfEvent>> perfEventsF =
                java.util.concurrent.CompletableFuture.supplyAsync(() -> perfEventService.selectRecentEvents(diagnosisHours, null), PROBE_POOL);

        // 记录主机探针是否降级：降级时前端必须显示“采集超时”而不是把全零当真实空闲
        boolean serverDegraded = false;
        Server server;
        try
        {
            server = serverF.get(PROBE_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
            // 空对象特征：cpuNum 从未赋值为正数，说明 collectServer 内部异常返回了默认实例
            serverDegraded = server.getCpu() == null || server.getCpu().getCpuNum() <= 0;
        }
        catch (Exception e)
        {
            server = new Server();
            serverDegraded = true;
        }
        Map<String, Object> cache = await(cacheF, degradedCache());
        List<Map<String, Object>> slowSql = await(slowSqlF, new ArrayList<>());
        List<Map<String, Object>> errors = await(errorsF, new ArrayList<>());
        List<Map<String, Object>> slowOperations = await(slowOperationsF, new ArrayList<>());
        List<Map<String, Object>> jobs = await(jobsF, new ArrayList<>());
        Map<String, Object> conversion = await(conversionF, new LinkedHashMap<>());
        List<SysPerfEvent> perfEvents = await(perfEventsF, new ArrayList<>());

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
        data.put("serverDegraded", serverDegraded);
        data.put("nodeVersion", resolveNodeVersion());
        data.put("cache", cache);
        data.put("threadPool", await(threadPoolF, singleEntryMap("available", false)));
        data.put("conversion", conversion);
        data.put("onlineCount", await(onlineCountF, 0L));
        data.put("dataSources", await(dataSourcesF, new ArrayList<>()));
        data.put("slowSql", slowSql);
        data.put("recentErrors", errors);
        data.put("slowOperations", slowOperations);
        data.put("topInterfaces", await(topInterfacesF, new ArrayList<>()));
        data.put("jobs", jobs);
        data.put("adviceSummary", adviceSummary);
        data.put("health", buildHealth(server, cache, slowSql, errors, slowOperations, conversion, diagnosisHours));
        data.put("report", buildReport(data, adviceSummary, perfEvents));
        // 双保险：递归替换 NaN/Infinity，保证任何探针的异常数值都不会炸掉 JSON 序列化
        sanitizeNonFiniteNumbers(data);
        return AjaxResult.success(data);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void sanitizeNonFiniteNumbers(Object node)
    {
        if (node instanceof Map)
        {
            ((Map) node).forEach((k, v) -> {
                if (v instanceof Double && (((Double) v).isNaN() || ((Double) v).isInfinite()))
                {
                    ((Map) node).put(k, 0D);
                }
                else if (v instanceof Float && (((Float) v).isNaN() || ((Float) v).isInfinite()))
                {
                    ((Map) node).put(k, 0F);
                }
                else
                {
                    sanitizeNonFiniteNumbers(v);
                }
            });
        }
        else if (node instanceof List)
        {
            for (Object item : (List) node)
            {
                sanitizeNonFiniteNumbers(item);
            }
        }
    }

    /** Node.js 版本缓存：仅首次调用探测一次，失败/缺失返回 null，不影响接口。 */
    private static volatile String NODE_VERSION_CACHE;

    /**
     * 探测本机 Node.js 版本（硬件信息面板展示用）。
     * 只执行一次 `node -v`，任何异常都静默降级为 null，绝不拖慢诊断接口。
     */
    private static String resolveNodeVersion()
    {
        if (NODE_VERSION_CACHE != null)
        {
            return NODE_VERSION_CACHE.isEmpty() ? null : NODE_VERSION_CACHE;
        }
        synchronized (SystemDiagnosisController.class)
        {
            if (NODE_VERSION_CACHE != null)
            {
                return NODE_VERSION_CACHE.isEmpty() ? null : NODE_VERSION_CACHE;
            }
            try
            {
                Process p = new ProcessBuilder("node", "-v").redirectErrorStream(true).start();
                byte[] out = readStreamWithLimit(p.getInputStream(), 64);
                boolean finished = p.waitFor(3, java.util.concurrent.TimeUnit.SECONDS);
                if (!finished)
                {
                    p.destroyForcibly();
                }
                String v = finished ? new String(out, java.nio.charset.StandardCharsets.UTF_8).trim() : "";
                NODE_VERSION_CACHE = v.matches("^v[0-9.]+.*") ? v : "";
            }
            catch (Exception e)
            {
                NODE_VERSION_CACHE = "";
            }
            return NODE_VERSION_CACHE.isEmpty() ? null : NODE_VERSION_CACHE;
        }
    }

    /** 读取进程输出，最多 limit 字节，防止异常输出灌爆内存。 */
    private static byte[] readStreamWithLimit(java.io.InputStream in, int limit) throws java.io.IOException
    {
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        byte[] chunk = new byte[256];
        int n;
        while ((n = in.read(chunk)) != -1 && buf.size() < limit)
        {
            buf.write(chunk, 0, n);
        }
        in.close();
        return buf.toByteArray();
    }

    private <T> T await(java.util.concurrent.Future<T> future, T fallback)
    {
        try
        {
            return future.get(PROBE_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            return fallback;
        }
    }

    private Map<String, Object> degradedCache()
    {
        Map<String, Object> cache = new LinkedHashMap<>();
        cache.put("available", false);
        cache.put("error", "缓存探针超时或异常");
        cache.put("dbSize", 0);
        return cache;
    }

    private Map<String, Object> singleEntryMap(String key, Object value)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
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
        // oper_time 已建索引：范围扫描天然按时间序输出，无全表 Filesort
        return queryList(
                "select oper_id, title, oper_name, dept_name, oper_url, request_method, oper_time, cost_time, error_msg "
                        + "from sys_oper_log where status = 1 and oper_time >= date_sub(now(), interval "
                        + hours + " hour) order by oper_id desc limit 10");
    }

    private List<Map<String, Object>> querySlowOperations(int hours)
    {
        // 主键倒查限窗：内层走 oper_time 索引范围 + oper_id 排序（无 Filesort），最多扫 2000 行；
        // 外层只对这 2000 行按耗时排序，代价可控。
        return queryList(
                "select oper_id, title, oper_name, dept_name, oper_url, request_method, oper_time, cost_time, status, error_msg "
                        + "from (select oper_id, title, oper_name, dept_name, oper_url, request_method, oper_time, cost_time, status, error_msg "
                        + "from sys_oper_log where cost_time >= 1000 and oper_time >= date_sub(now(), interval " + hours
                        + " hour) order by oper_id desc limit 2000) recent "
                        + "order by cost_time desc, oper_time desc limit 10");
    }

    private List<Map<String, Object>> queryTopInterfaces(int hours)
    {
        // 日志表过大时直接从轻量 sys_perf_event 聚合，避免大表全窗口 Group By
        if (operLogRows() > OPER_LOG_FALLBACK_ROWS)
        {
            return queryList(
                    "select source_url as oper_url, '' as title, count(1) as request_count, round(avg(duration_ms), 0) as avg_cost, max(duration_ms) as max_cost "
                            + "from sys_perf_event where occur_time >= date_sub(now(), interval " + hours
                            + " hour) group by source_url order by request_count desc limit 10");
        }
        // 常规路径：主键倒查最近 10000 条为聚合上限，杜绝全表扫描
        return queryList(
                "select oper_url, title, count(1) as request_count, round(avg(cost_time), 0) as avg_cost, max(cost_time) as max_cost "
                        + "from (select oper_url, title, cost_time from sys_oper_log where oper_time >= date_sub(now(), interval "
                        + hours + " hour) order by oper_id desc limit 10000) recent "
                        + "group by oper_url, title order by request_count desc, max_cost desc limit 10");
    }

    /** information_schema 行数估算，毫秒级返回；仅用于是否切换 perf_event 聚合的阈值判断。 */
    private long operLogRows()
    {
        try
        {
            Long rows = jdbcTemplate.queryForObject(
                    "select table_rows from information_schema.tables where table_schema = database() and table_name = 'sys_oper_log'",
                    Long.class);
            return rows == null ? 0L : rows;
        }
        catch (Exception e)
        {
            return 0L;
        }
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
        long actionableErrors = countActionable(errors);
        if (actionableErrors > 0)
        {
            risks.add("近 " + diagnosisHours + " 小时内有 " + actionableErrors + " 条需关注异常，请查看错误信息与处置建议。");
        }
        long actionableSlowOperations = countActionable(slowOperations);
        if (actionableSlowOperations > 0)
        {
            risks.add("近 " + diagnosisHours + " 小时内有 " + actionableSlowOperations + " 个需关注慢接口，可能影响并发体验。");
        }
        if (!slowSql.isEmpty() && actionableErrors == 0 && actionableSlowOperations == 0)
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

    private long countActionable(List<Map<String, Object>> rows)
    {
        if (rows == null)
        {
            return 0L;
        }
        return rows.stream()
                .filter(row -> !"info".equals(String.valueOf(row.get("severity"))))
                .count();
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
