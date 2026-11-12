package com.ruoyi.web.controller.business;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 教研员平台概览
 */
@RestController
@RequestMapping("/business/platformOverview")
public class PlatformOverviewController
{
    /**
     * 近 6 个月作答趋势。
     * biz_student_answer 已有唯一键 uk_student_lesson_question(student_id, lesson_id, question_id)，
     * 每个「学生 + 课程 + 题目」只有一行，原先的
     *   inner join (select max(answer_id) ... group by student_id, lesson_id, question_id)
     * 只是把这张表再整体分组物化一遍，实测单条 7.1 秒；去掉后同样的数据 0.6 秒。
     * 人次与均分原本各查一次同样重的派生集，这里合并为一次扫描：paper_count 口径不含分数，
     * avg_score 依赖 sum(score) 的 NULL 传播跳过无分试卷，与原先两条 SQL 结果一致。
     */
    private static final String ANSWER_TREND_SQL =
            "select date_format(papers.last_submit, '%m月') as name, count(1) as paperCount, "
                    + "round(avg(papers.paper_score), 1) as avgScore "
                    + "from ( "
                    + "  select a.lesson_id, a.student_id, max(a.submit_time) as last_submit, sum(a.score) as paper_score "
                    + "  from biz_student_answer a "
                    + "  where a.submit_time is not null and a.submit_time >= date_sub(curdate(), interval 6 month) "
                    + "  group by a.lesson_id, a.student_id, date_format(a.submit_time, '%Y-%m') "
                    + ") papers "
                    + "group by date_format(papers.last_submit, '%Y-%m'), date_format(papers.last_submit, '%m月') "
                    + "order by date_format(papers.last_submit, '%Y-%m')";

    /** 近 90 天学校活跃；同样去掉多余的 max(answer_id) 派生表。 */
    private static final String TOP_SCHOOLS_SQL =
            "select d.dept_name, count(distinct papers.student_id) as active_student_count, "
                    + "round(avg(papers.paper_score), 1) as avg_score "
                    + "from ( "
                    + "  select a.student_id, u.dept_id, sum(a.score) as paper_score "
                    + "  from biz_student_answer a "
                    + "  inner join biz_student s on a.student_id = s.student_id "
                    + "  inner join sys_user u on s.user_id = u.user_id "
                    + "  where a.submit_time >= date_sub(curdate(), interval 90 day) and a.score is not null "
                    + "  group by a.lesson_id, a.student_id, u.dept_id "
                    + ") papers "
                    + "inner join sys_dept d on papers.dept_id = d.dept_id "
                    + "group by d.dept_id, d.dept_name order by active_student_count desc, avg_score desc limit 8";

    /**
     * 概览由二十余条聚合拼装，130 实测全量构建约 9~10 秒（趋势/学校两条聚合各 4.4 秒），
     * 平台统计不需要分钟级实时，快照 TTL 放宽到 5 分钟，把重聚合压出用户请求路径：
     * 过期后立即返回旧快照，由后台单线程刷新；冷启动无旧值时最多等待15秒共享构建结果。
     */
    private static final long SUMMARY_CACHE_MS = 300_000L;

    private static final Logger log = LoggerFactory.getLogger(PlatformOverviewController.class);

    private static final long FAILURE_RETRY_MS = 30_000L;
    private static final long COLD_WAIT_SECONDS = 15L;
    private final Object summaryLock = new Object();
    private volatile Snapshot summaryCache;
    private volatile long nextRetryAt;
    private volatile boolean refreshFailed;
    private CompletableFuture<Snapshot> refreshFuture;

    /** 预热与请求共用一个任务；线程池随控制器销毁，避免上下文重建遗留线程。 */
    private final ExecutorService refreshPool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "platform-overview-refresh");
        t.setDaemon(true);
        return t;
    });

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 应用就绪后异步预热一次，避免重启后第一位教研员同步等待全量聚合。 */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpSnapshot()
    {
        triggerAsyncRefresh();
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        Snapshot cached = summaryCache;
        if (isFresh(cached))
        {
            return snapshotResult(cached);
        }
        CompletableFuture<Snapshot> pending = triggerAsyncRefresh();
        cached = summaryCache;
        if (cached != null)
        {
            return snapshotResult(cached);
        }
        if (pending == null)
        {
            return AjaxResult.error("平台概览暂不可用，请稍后重试");
        }
        try
        {
            return snapshotResult(pending.get(COLD_WAIT_SECONDS, TimeUnit.SECONDS));
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return AjaxResult.error("平台概览读取已中断，请稍后重试");
        }
        catch (Exception e)
        {
            // 等待超时不取消共享任务，后续请求仍复用该任务。
            return AjaxResult.error("平台概览正在准备或暂不可用，请稍后重试");
        }
    }

    private boolean isFresh(Snapshot cached)
    {
        return cached != null && System.currentTimeMillis() - cached.generatedAt < SUMMARY_CACHE_MS;
    }

    private AjaxResult snapshotResult(Snapshot snapshot)
    {
        Map<String, Object> data = new LinkedHashMap<>(snapshot.data);
        synchronized (summaryLock)
        {
            data.put("generatedAt", snapshot.generatedAt);
            data.put("stale", !isFresh(snapshot));
            data.put("refreshing", refreshFuture != null && !refreshFuture.isDone());
            data.put("refreshFailed", refreshFailed);
            data.put("retryAfterMs", Math.max(0L, nextRetryAt - System.currentTimeMillis()));
        }
        return AjaxResult.success(data);
    }

    private CompletableFuture<Snapshot> triggerAsyncRefresh()
    {
        synchronized (summaryLock)
        {
            // 锁内重查新鲜度，避免持有旧快照的请求重复触发刚完成的刷新。
            if (isFresh(summaryCache))
            {
                return CompletableFuture.completedFuture(summaryCache);
            }
            if (refreshFuture != null && !refreshFuture.isDone())
            {
                return refreshFuture;
            }
            if (System.currentTimeMillis() < nextRetryAt)
            {
                return null;
            }
            CompletableFuture<Snapshot> pending = new CompletableFuture<>();
            refreshFuture = pending;
            refreshFailed = false;
            try
            {
                refreshPool.execute(() -> {
                    try
                    {
                        Snapshot snapshot = new Snapshot(buildSummary(), System.currentTimeMillis());
                        synchronized (summaryLock)
                        {
                            summaryCache = snapshot;
                            nextRetryAt = 0L;
                            refreshFailed = false;
                            pending.complete(snapshot);
                        }
                    }
                    catch (Throwable e)
                    {
                        failRefresh(pending, e);
                        if (e instanceof Error)
                        {
                            throw (Error) e;
                        }
                    }
                });
            }
            catch (RuntimeException e)
            {
                failRefresh(pending, e);
            }
            return pending;
        }
    }

    private void failRefresh(CompletableFuture<Snapshot> pending, Throwable error)
    {
        synchronized (summaryLock)
        {
            nextRetryAt = System.currentTimeMillis() + FAILURE_RETRY_MS;
            refreshFailed = true;
            pending.completeExceptionally(error);
        }
        log.warn("平台概览快照刷新失败，保留旧快照并延迟重试: {}", error.getMessage());
    }

    @PreDestroy
    public void destroy()
    {
        refreshPool.shutdownNow();
        synchronized (summaryLock)
        {
            if (refreshFuture != null && !refreshFuture.isDone())
            {
                failRefresh(refreshFuture, new IllegalStateException("平台概览服务已停止"));
            }
        }
    }

    private static final class Snapshot
    {
        private final Map<String, Object> data;
        private final long generatedAt;

        private Snapshot(Map<String, Object> data, long generatedAt)
        {
            this.data = data;
            this.generatedAt = generatedAt;
        }
    }

    private Map<String, Object> buildSummary()
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("metrics", buildMetrics());
        data.put("systemHealth", buildSystemHealth());
        data.put("questionDistribution", queryList(
                "select question_type as name, count(1) as value from biz_question group by question_type order by value desc"));
        List<Map<String, Object>> trend = queryList(ANSWER_TREND_SQL);
        data.put("answerTrend", pickTrend(trend, "paperCount"));
        data.put("scoreTrend", pickTrend(trend, "avgScore"));
        data.put("countyExamStatus", queryList(
                "select status as name, count(1) as value from biz_county_exam where del_flag = '0' group by status"));
        data.put("recentCountyExams", queryList(
                "select exam_id, exam_name, exam_grade, status, duration_minutes, create_time, open_time, close_time "
                        + "from biz_county_exam where del_flag = '0' order by create_time desc, exam_id desc limit 6"));
        data.put("topSchools", queryList(TOP_SCHOOLS_SQL));
        data.put("intro", buildIntro());
        return data;
    }

    /** 把一次趋势查询拆成前端原有的 name/value 结构，两张图各自只取需要的列。 */
    private List<Map<String, Object>> pickTrend(List<Map<String, Object>> rows, String valueKey)
    {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> row : rows)
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", row.get("name"));
            item.put("value", row.get(valueKey));
            list.add(item);
        }
        return list;
    }

    private List<Map<String, Object>> buildMetrics()
    {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(metric("schoolCount", "覆盖学校", count("select count(1) from sys_dept where del_flag = '0' and parent_id <> 0"), "所", "teal"));
        list.add(metric("teacherCount", "教师账号", count(
                "select count(distinct u.user_id) from sys_user u inner join sys_user_role ur on u.user_id = ur.user_id "
                        + "inner join sys_role r on ur.role_id = r.role_id where u.del_flag = '0' and r.role_key = 'teacher'"),
                "名", "gold"));
        list.add(metric("studentCount", "学生档案", count(
                "select count(1) from biz_student s inner join sys_user u on s.user_id = u.user_id where u.del_flag = '0'"),
                "人", "blue"));
        list.add(metric("questionCount", "题库题目", count("select count(1) from biz_question"), "题", "coral"));
        list.add(metric("lessonCount", "课程资源", count("select count(1) from biz_lesson"), "节", "green"));
        list.add(metric("countyExamCount", "区域抽测", count("select count(1) from biz_county_exam where del_flag = '0'"), "场", "red"));
        list.add(metric("todayAnswerCount", "今日作答", count(
                "select count(1) from biz_student_answer where submit_time >= curdate()"), "次", "cyan"));
        list.add(metric("todayOperateCount", "今日操作日志", count(
                "select count(1) from sys_oper_log where oper_time >= curdate()"), "条", "slate"));
        return list;
    }

    private Map<String, Object> buildSystemHealth()
    {
        Long errorCount = count("select count(1) from sys_oper_log where status = 1 and oper_time >= date_sub(now(), interval 24 hour)");
        Long slowCount = count("select count(1) from sys_oper_log where cost_time >= 3000 and oper_time >= date_sub(now(), interval 24 hour)");
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("level", errorCount > 0 ? "warning" : "stable");
        health.put("title", errorCount > 0 ? "近 24 小时存在异常操作" : "核心服务运行平稳");
        health.put("errorCount", errorCount);
        health.put("slowCount", slowCount);
        health.put("message", slowCount > 0 ? "存在耗时超过 3 秒的接口，建议进入系统诊断中心查看。" : "暂无明显慢接口信号。");
        return health;
    }

    private List<Map<String, Object>> buildIntro()
    {
        List<Map<String, Object>> intro = new ArrayList<>();
        intro.add(introItem("统一测评", "支持选择、判断、打字、操作题的完整测评闭环。"));
        intro.add(introItem("区域抽测", "教研员可跨校组织区域抽测、匿名评卷和学校汇总分析。"));
        intro.add(introItem("智能诊断", "系统诊断中心聚合慢 SQL、错误日志、并发和缓存健康。"));
        intro.add(introItem("课题支撑", "以真实数据沉淀教学过程、测评结果和平台运行证据。"));
        return intro;
    }

    private Map<String, Object> metric(String key, String label, Long value, String unit, String tone)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", key);
        item.put("label", label);
        item.put("value", value);
        item.put("unit", unit);
        item.put("tone", tone);
        return item;
    }

    private Map<String, Object> introItem(String title, String content)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", title);
        item.put("content", content);
        return item;
    }

    private Long count(String sql)
    {
        // 读取失败必须终止整份快照，不能把数据库故障伪装成真实零值。
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }

    private List<Map<String, Object>> queryList(String sql)
    {
        return jdbcTemplate.queryForList(sql);
    }
}
