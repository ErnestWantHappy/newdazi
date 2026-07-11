package com.ruoyi.common.utils.sql;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.ruoyi.common.utils.StringUtils;

/**
 * 将 SQL / Mapper 语句翻译为中文业务说明
 */
public final class SqlBusinessDescriber
{
    private static final Pattern FROM_PATTERN = Pattern.compile("(?i)\\bfrom\\s+([a-z0-9_]+)");
    private static final Pattern JOIN_PATTERN = Pattern.compile("(?i)\\bjoin\\s+([a-z0-9_]+)");
    private static final Pattern INTO_PATTERN = Pattern.compile("(?i)\\binto\\s+([a-z0-9_]+)");
    private static final Pattern UPDATE_PATTERN = Pattern.compile("(?i)\\bupdate\\s+([a-z0-9_]+)");

    private static final Map<String, String> TABLE_LABELS = new LinkedHashMap<>();

    static
    {
        TABLE_LABELS.put("biz_student_answer", "学生答题");
        TABLE_LABELS.put("biz_lesson", "课程");
        TABLE_LABELS.put("biz_question", "题库");
        TABLE_LABELS.put("biz_lesson_question", "课程题目");
        TABLE_LABELS.put("biz_lesson_assignment", "课程指派");
        TABLE_LABELS.put("biz_student", "学生档案");
        TABLE_LABELS.put("biz_classroom_performance", "课堂表现");
        TABLE_LABELS.put("biz_county_exam", "区域抽测");
        TABLE_LABELS.put("biz_county_exam_answer", "区域抽测答卷");
        TABLE_LABELS.put("biz_county_exam_student", "区域抽测学生");
        TABLE_LABELS.put("biz_teacher_class", "教师班级");
        TABLE_LABELS.put("sys_user", "系统用户");
        TABLE_LABELS.put("sys_dept", "学校部门");
        TABLE_LABELS.put("sys_oper_log", "操作日志");
        TABLE_LABELS.put("sys_job", "定时任务");
    }

    private static final Map<String, String> MAPPER_KEYWORDS = new LinkedHashMap<>();

    static
    {
        MAPPER_KEYWORDS.put("BizStudentAnswer", "学生答题");
        MAPPER_KEYWORDS.put("BizLesson", "课程");
        MAPPER_KEYWORDS.put("BizQuestion", "题库");
        MAPPER_KEYWORDS.put("BizClassroomPerformance", "课堂表现");
        MAPPER_KEYWORDS.put("CountyExam", "区域抽测");
        MAPPER_KEYWORDS.put("StudentProfile", "学生画像");
        MAPPER_KEYWORDS.put("SchoolScore", "学校成绩");
        MAPPER_KEYWORDS.put("ScoreQuery", "成绩查询");
        MAPPER_KEYWORDS.put("SysOperLog", "操作日志");
        MAPPER_KEYWORDS.put("SysUser", "用户管理");
    }

    private SqlBusinessDescriber()
    {
    }

    public static SqlDescription describe(String mapperId, String sqlText)
    {
        String domain = resolveDomain(mapperId, sqlText);
        String action = resolveAction(mapperId, sqlText);
        String title = domain + " · " + action;
        String description = "涉及" + domain + "的" + action + "，主表 " + resolvePrimaryTable(sqlText);
        return new SqlDescription(title, description);
    }

    private static String resolveDomain(String mapperId, String sqlText)
    {
        if (StringUtils.isNotEmpty(mapperId))
        {
            for (Map.Entry<String, String> entry : MAPPER_KEYWORDS.entrySet())
            {
                if (mapperId.contains(entry.getKey()))
                {
                    return entry.getValue();
                }
            }
            int dot = mapperId.lastIndexOf('.');
            if (dot >= 0 && dot < mapperId.length() - 1)
            {
                String mapperName = mapperId.substring(dot + 1);
                return humanizeToken(mapperName.replace("Mapper", ""));
            }
        }
        return resolvePrimaryTable(sqlText);
    }

    private static String resolveAction(String mapperId, String sqlText)
    {
        String sql = sqlText == null ? "" : sqlText.trim().toUpperCase(Locale.ROOT);
        if (sql.startsWith("SELECT"))
        {
            if (sql.contains(" COUNT(") || sql.startsWith("SELECT COUNT"))
            {
                return "统计查询";
            }
            return "列表查询";
        }
        if (sql.startsWith("INSERT"))
        {
            return "新增写入";
        }
        if (sql.startsWith("UPDATE"))
        {
            return "更新写入";
        }
        if (sql.startsWith("DELETE"))
        {
            return "删除操作";
        }
        if (StringUtils.isNotEmpty(mapperId))
        {
            String method = mapperId.contains(".") ? mapperId.substring(mapperId.lastIndexOf('.') + 1) : mapperId;
            if (method.startsWith("select") || method.startsWith("list") || method.startsWith("get"))
            {
                return "列表查询";
            }
            if (method.startsWith("insert") || method.startsWith("add"))
            {
                return "新增写入";
            }
            if (method.startsWith("update"))
            {
                return "更新写入";
            }
            if (method.startsWith("delete"))
            {
                return "删除操作";
            }
            return humanizeToken(method);
        }
        return "数据访问";
    }

    private static String resolvePrimaryTable(String sqlText)
    {
        if (StringUtils.isEmpty(sqlText))
        {
            return "未知表";
        }
        Matcher fromMatcher = FROM_PATTERN.matcher(sqlText);
        if (fromMatcher.find())
        {
            return labelTable(fromMatcher.group(1));
        }
        Matcher joinMatcher = JOIN_PATTERN.matcher(sqlText);
        if (joinMatcher.find())
        {
            return labelTable(joinMatcher.group(1));
        }
        Matcher intoMatcher = INTO_PATTERN.matcher(sqlText);
        if (intoMatcher.find())
        {
            return labelTable(intoMatcher.group(1));
        }
        Matcher updateMatcher = UPDATE_PATTERN.matcher(sqlText);
        if (updateMatcher.find())
        {
            return labelTable(updateMatcher.group(1));
        }
        return "未知表";
    }

    private static String labelTable(String tableName)
    {
        return TABLE_LABELS.getOrDefault(tableName.toLowerCase(Locale.ROOT), tableName);
    }

    private static String humanizeToken(String token)
    {
        if (StringUtils.isEmpty(token))
        {
            return "数据访问";
        }
        return token.replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    public static class SqlDescription
    {
        private final String title;
        private final String description;

        public SqlDescription(String title, String description)
        {
            this.title = title;
            this.description = description;
        }

        public String getTitle()
        {
            return title;
        }

        public String getDescription()
        {
            return description;
        }
    }
}
