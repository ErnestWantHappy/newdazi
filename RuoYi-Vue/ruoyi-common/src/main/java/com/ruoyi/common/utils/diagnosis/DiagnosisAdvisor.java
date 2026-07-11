package com.ruoyi.common.utils.diagnosis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.ruoyi.common.utils.StringUtils;

/**
 * 为诊断记录生成中文处置建议
 */
public final class DiagnosisAdvisor
{
    private DiagnosisAdvisor()
    {
    }

    public static DiagnosisAdvice adviseForError(Map<String, Object> row)
    {
        String errorMsg = text(row, "error_msg", "errorMsg");
        String operUrl = text(row, "oper_url", "operUrl");
        if (containsAny(errorMsg, "账号已存在", "已存在", "重复"))
        {
            return new DiagnosisAdvice("business", "info",
                    "入学年份+班级+学号组合重复；检查 Excel 是否重复行或与现有学生撞号");
        }
        if (StringUtils.isNotEmpty(operUrl) && operUrl.contains("/business/question/importData"))
        {
            return new DiagnosisAdvice("business", "info",
                    "题库导入失败多为 Excel 格式或题型字段问题；按模板逐列核对");
        }
        if (StringUtils.isNotEmpty(operUrl) && operUrl.contains("/business/student"))
        {
            return new DiagnosisAdvice("business", "info",
                    "学生管理操作未通过业务校验；核对导入文件字段和已有学生档案");
        }
        return new DiagnosisAdvice("system", "warning",
                "系统异常；复制本条记录给技术人员或 AI 继续排查");
    }

    public static DiagnosisAdvice adviseForSlowApi(Map<String, Object> row)
    {
        String operUrl = text(row, "oper_url", "operUrl");
        long costTime = number(row, "cost_time", "costTime");
        if (StringUtils.isNotEmpty(operUrl) && operUrl.contains("/business/student/importData") && costTime > 10000)
        {
            return new DiagnosisAdvice("performance", costTime >= 30000 ? "critical" : "warning",
                    "大批量学生导入会长时间占用接口；建议分批导入、避开上课高峰，导入期间避免多人同时操作");
        }
        if (StringUtils.isNotEmpty(operUrl) && operUrl.contains("/export"))
        {
            return new DiagnosisAdvice("performance", "warning",
                    "导出操作耗时较长；建议缩小筛选范围，避开多人同时导出");
        }
        if (costTime >= 3000)
        {
            return new DiagnosisAdvice("performance", "critical",
                    "接口响应超过 3 秒，可能影响并发体验；结合发生时间查看是否多人同时使用");
        }
        return new DiagnosisAdvice("performance", "warning",
                "接口响应偏慢；如频繁出现，建议联系技术人员排查");
    }

    public static DiagnosisAdvice adviseForSlowSql(Map<String, Object> row)
    {
        String sql = text(row, "sql");
        String title = text(row, "title");
        long maxTimespan = number(row, "maxTimespan");
        long runningCount = number(row, "runningCount");
        String combined = (title + " " + sql).toLowerCase();
        if (containsAny(combined, "answer_trend", "paper_score", "last_submit", "平台概览", "platformoverview"))
        {
            return new DiagnosisAdvice("performance", "info",
                    "平台概览统计查询较重；避免多人频繁刷新首页，必要时错峰查看");
        }
        if (maxTimespan >= 3000 || runningCount > 0)
        {
            return new DiagnosisAdvice("performance", "critical",
                    "SQL 可能正在拖慢系统；结合最近执行时间查看是否多人同时使用");
        }
        return new DiagnosisAdvice("performance", "warning",
                "数据库查询偏慢；如持续出现，建议结合慢接口和操作日志定位触发行为");
    }

    public static DiagnosisAdvice adviseForResource(String type, double usage)
    {
        if ("mem".equals(type) && usage >= 85)
        {
            return new DiagnosisAdvice("resource", "critical",
                    "服务器内存偏高；检查 LibreOffice 残留进程、大文件导出，必要时重启后端服务");
        }
        if ("jvm".equals(type) && usage >= 80)
        {
            return new DiagnosisAdvice("resource", "warning",
                    "JVM 内存偏高；优先排查大文件预览、导出或批量查询");
        }
        if ("redis".equals(type))
        {
            return new DiagnosisAdvice("resource", "critical",
                    "Redis 不可用；登录令牌、验证码、防重提交和限流可能受影响，需尽快恢复");
        }
        return new DiagnosisAdvice("resource", "info", "资源指标正常");
    }

    public static void enrichRow(Map<String, Object> row, DiagnosisAdvice advice)
    {
        row.put("category", advice.getCategory());
        row.put("severity", advice.getSeverity());
        row.put("advice", advice.getAdvice());
    }

    public static List<Map<String, Object>> buildAdviceSummary(int hours, double memUsage, double jvmUsage, boolean redisAvailable,
            List<Map<String, Object>> errors, List<Map<String, Object>> slowOperations, List<Map<String, Object>> slowSql)
    {
        List<Map<String, Object>> items = new ArrayList<>();
        if (memUsage >= 85)
        {
            items.add(summaryItem("resource", "critical", "服务器内存",
                    "内存使用率 " + Math.round(memUsage) + "%",
                    adviseForResource("mem", memUsage).getAdvice()));
        }
        if (jvmUsage >= 80)
        {
            items.add(summaryItem("resource", "warning", "JVM 内存",
                    "JVM 使用率 " + Math.round(jvmUsage) + "%",
                    adviseForResource("jvm", jvmUsage).getAdvice()));
        }
        if (!redisAvailable)
        {
            items.add(summaryItem("resource", "critical", "Redis 缓存", "缓存不可用",
                    adviseForResource("redis", 0).getAdvice()));
        }
        if (errors != null)
        {
            for (Map<String, Object> row : errors.stream().limit(2).collect(Collectors.toList()))
            {
                items.add(summaryItem(text(row, "category"), text(row, "severity"), "业务异常",
                        text(row, "title") + " " + text(row, "oper_url", "operUrl"),
                        text(row, "advice")));
            }
        }
        if (slowOperations != null)
        {
            for (Map<String, Object> row : slowOperations.stream().limit(2).collect(Collectors.toList()))
            {
                items.add(summaryItem(text(row, "category"), text(row, "severity"), "慢接口",
                        text(row, "title") + " " + number(row, "cost_time", "costTime") + " ms",
                        text(row, "advice")));
            }
        }
        if (slowSql != null)
        {
            for (Map<String, Object> row : slowSql.stream().limit(1).collect(Collectors.toList()))
            {
                items.add(summaryItem(text(row, "category"), text(row, "severity"), "慢 SQL",
                        text(row, "title") + " " + number(row, "maxTimespan") + " ms",
                        text(row, "advice")));
            }
        }
        if (items.isEmpty())
        {
            items.add(summaryItem("system", "info", "运行状态",
                    "近 " + hours + " 小时内未发现需紧急处理的业务异常或慢接口",
                    "继续关注资源压力；后台任务保持运行即可"));
        }
        items.sort((a, b) -> Integer.compare(
                severityRank(String.valueOf(b.get("severity"))),
                severityRank(String.valueOf(a.get("severity")))));
        if (items.size() > 3)
        {
            return new ArrayList<>(items.subList(0, 3));
        }
        return items;
    }

    private static Map<String, Object> summaryItem(String category, String severity, String label, String detail, String advice)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("category", category);
        item.put("severity", severity);
        item.put("label", label);
        item.put("detail", detail);
        item.put("advice", advice);
        return item;
    }

    private static int severityRank(String severity)
    {
        if ("critical".equals(severity))
        {
            return 3;
        }
        if ("warning".equals(severity))
        {
            return 2;
        }
        if ("info".equals(severity))
        {
            return 1;
        }
        return 0;
    }

    private static String text(Map<String, Object> row, String... keys)
    {
        if (row == null)
        {
            return "";
        }
        for (String key : keys)
        {
            Object value = row.get(key);
            if (value != null && StringUtils.isNotEmpty(String.valueOf(value)))
            {
                return String.valueOf(value);
            }
        }
        return "";
    }

    private static long number(Map<String, Object> row, String... keys)
    {
        for (String key : keys)
        {
            Object value = row.get(key);
            if (value instanceof Number)
            {
                return ((Number) value).longValue();
            }
            if (value != null)
            {
                try
                {
                    return Long.parseLong(String.valueOf(value));
                }
                catch (Exception ignored)
                {
                }
            }
        }
        return 0L;
    }

    private static boolean containsAny(String text, String... keywords)
    {
        if (StringUtils.isEmpty(text))
        {
            return false;
        }
        for (String keyword : keywords)
        {
            if (text.contains(keyword))
            {
                return true;
            }
        }
        return false;
    }
}
