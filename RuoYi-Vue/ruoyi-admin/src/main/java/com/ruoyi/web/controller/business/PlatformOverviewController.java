package com.ruoyi.web.controller.business;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final String LATEST_ANSWER_SUBQUERY =
            "select max(answer_id) as answer_id from biz_student_answer group by student_id, lesson_id, question_id";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("metrics", buildMetrics());
        data.put("systemHealth", buildSystemHealth());
        data.put("questionDistribution", queryList(
                "select question_type as name, count(1) as value from biz_question group by question_type order by value desc"));
        data.put("answerTrend", queryList(
                "select date_format(last_submit, '%m月') as name, count(1) as value "
                        + "from ( "
                        + "  select a.lesson_id, a.student_id, max(a.submit_time) as last_submit "
                        + "  from biz_student_answer a "
                        + "  inner join (" + LATEST_ANSWER_SUBQUERY + ") latest on latest.answer_id = a.answer_id "
                        + "  where a.submit_time is not null and a.submit_time >= date_sub(curdate(), interval 6 month) "
                        + "  group by a.lesson_id, a.student_id, date_format(a.submit_time, '%Y-%m') "
                        + ") papers "
                        + "group by date_format(last_submit, '%Y-%m'), date_format(last_submit, '%m月') "
                        + "order by date_format(last_submit, '%Y-%m')"));
        data.put("scoreTrend", queryList(
                "select date_format(last_submit, '%m月') as name, round(avg(paper_score), 1) as value "
                        + "from ( "
                        + "  select a.lesson_id, a.student_id, sum(a.score) as paper_score, max(a.submit_time) as last_submit "
                        + "  from biz_student_answer a "
                        + "  inner join (" + LATEST_ANSWER_SUBQUERY + ") latest on latest.answer_id = a.answer_id "
                        + "  where a.submit_time is not null and a.score is not null "
                        + "    and a.submit_time >= date_sub(curdate(), interval 6 month) "
                        + "  group by a.lesson_id, a.student_id, date_format(a.submit_time, '%Y-%m') "
                        + ") papers "
                        + "group by date_format(last_submit, '%Y-%m'), date_format(last_submit, '%m月') "
                        + "order by date_format(last_submit, '%Y-%m')"));
        data.put("countyExamStatus", queryList(
                "select status as name, count(1) as value from biz_county_exam where del_flag = '0' group by status"));
        data.put("recentCountyExams", queryList(
                "select exam_id, exam_name, exam_grade, status, duration_minutes, create_time, open_time, close_time "
                        + "from biz_county_exam where del_flag = '0' order by create_time desc, exam_id desc limit 6"));
        data.put("topSchools", queryList(
                "select d.dept_name, count(distinct papers.student_id) as active_student_count, round(avg(papers.paper_score), 1) as avg_score "
                        + "from ( "
                        + "  select a.student_id, u.dept_id, sum(a.score) as paper_score "
                        + "  from biz_student_answer a "
                        + "  inner join (" + LATEST_ANSWER_SUBQUERY + ") latest on latest.answer_id = a.answer_id "
                        + "  inner join biz_student s on a.student_id = s.student_id "
                        + "  inner join sys_user u on s.user_id = u.user_id "
                        + "  where a.submit_time >= date_sub(curdate(), interval 90 day) and a.score is not null "
                        + "  group by a.lesson_id, a.student_id, u.dept_id "
                        + ") papers "
                        + "inner join sys_dept d on papers.dept_id = d.dept_id "
                        + "group by d.dept_id, d.dept_name order by active_student_count desc, avg_score desc limit 8"));
        data.put("intro", buildIntro());
        return AjaxResult.success(data);
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
        try
        {
            Long value = jdbcTemplate.queryForObject(sql, Long.class);
            return value == null ? 0L : value;
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
}
