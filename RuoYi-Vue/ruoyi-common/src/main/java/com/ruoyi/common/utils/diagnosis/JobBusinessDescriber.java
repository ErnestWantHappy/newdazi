package com.ruoyi.common.utils.diagnosis;

import java.util.LinkedHashMap;
import java.util.Map;
import com.ruoyi.common.utils.StringUtils;

/**
 * 后台定时任务业务说明
 */
public final class JobBusinessDescriber
{
    private JobBusinessDescriber()
    {
    }

    public static JobDescription describe(String jobName, String invokeTarget, String cronExpression)
    {
        String target = invokeTarget == null ? "" : invokeTarget;
        if (target.startsWith("ryTask."))
        {
            return new JobDescription(
                    StringUtils.isNotEmpty(jobName) ? jobName : "RuoYi 演示任务",
                    "框架自带示例，与业务无关，可忽略",
                    "演示任务",
                    "framework_demo");
        }
        if (target.contains("practicalPreviewRetryTask.retryFailedStudentAnswerPreviews"))
        {
            return new JobDescription("操作题预览重试",
                    "自动重试 DOC/DOCX 转 PDF 失败的学生作品",
                    "每小时",
                    "platform");
        }
        if (target.contains("libreOfficeMaintenanceTask.cleanupAndRestart"))
        {
            return new JobDescription("LibreOffice维护清理",
                    "清理残留 soffice 进程并重启文档转换服务池",
                    "每天 00:00",
                    "platform");
        }
        if (target.contains("sysPerfEventCleanupTask.cleanupExpiredPerfEvents"))
        {
            return new JobDescription("性能事件清理",
                    "删除 7 天前的诊断事件记录",
                    "每天凌晨 2 点",
                    "platform");
        }
        return new JobDescription(
                StringUtils.isNotEmpty(jobName) ? jobName : "后台任务",
                "系统定时任务：" + target,
                humanizeCron(cronExpression),
                "platform");
    }

    public static void enrichJobRow(Map<String, Object> row)
    {
        JobDescription desc = describe(
                stringValue(row.get("job_name")),
                stringValue(row.get("invoke_target")),
                stringValue(row.get("cron_expression")));
        row.put("displayName", desc.getDisplayName());
        row.put("purpose", desc.getPurpose());
        row.put("scheduleDesc", desc.getScheduleDesc());
        row.put("taskCategory", desc.getTaskCategory());
    }

    private static String humanizeCron(String cron)
    {
        if (StringUtils.isEmpty(cron))
        {
            return "未配置";
        }
        if ("0 0 * * * ?".equals(cron))
        {
            return "每小时";
        }
        if ("0 0 2 * * ?".equals(cron))
        {
            return "每天凌晨 2 点";
        }
        if ("0 0 0 * * ?".equals(cron))
        {
            return "每天 00:00";
        }
        if (cron.startsWith("0/"))
        {
            return "高频演示调度";
        }
        return cron;
    }

    private static String stringValue(Object value)
    {
        return value == null ? "" : String.valueOf(value);
    }

    public static class JobDescription
    {
        private final String displayName;
        private final String purpose;
        private final String scheduleDesc;
        private final String taskCategory;

        public JobDescription(String displayName, String purpose, String scheduleDesc, String taskCategory)
        {
            this.displayName = displayName;
            this.purpose = purpose;
            this.scheduleDesc = scheduleDesc;
            this.taskCategory = taskCategory;
        }

        public String getDisplayName()
        {
            return displayName;
        }

        public String getPurpose()
        {
            return purpose;
        }

        public String getScheduleDesc()
        {
            return scheduleDesc;
        }

        public String getTaskCategory()
        {
            return taskCategory;
        }
    }
}
