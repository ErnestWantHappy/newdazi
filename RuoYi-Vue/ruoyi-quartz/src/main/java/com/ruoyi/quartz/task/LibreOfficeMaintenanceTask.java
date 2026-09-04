package com.ruoyi.quartz.task;

import com.ruoyi.business.service.CountyExamPreviewRetryService;
import com.ruoyi.business.service.PracticalArtifactService;
import com.ruoyi.business.service.PracticalPreviewRetryService;
import com.ruoyi.business.utils.FileConversionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * LibreOffice 转换服务维护与分钟级自愈任务。
 * <p>
 * - cleanupAndRestart：日级全量清理（兜底）
 * - healthCheckAndRecover：分钟级健康巡检；不健康时 cleanup+重启，成功后联动捞回卡住预览
 */
@Component("libreOfficeMaintenanceTask")
public class LibreOfficeMaintenanceTask {

    private static final Logger log = LoggerFactory.getLogger(LibreOfficeMaintenanceTask.class);

    @Autowired
    private PracticalPreviewRetryService practicalPreviewRetryService;

    @Autowired
    private CountyExamPreviewRetryService countyExamPreviewRetryService;

    @Autowired
    private PracticalArtifactService practicalArtifactService;

    /**
     * 日级全量维护：无条件清理残留并重启服务池。
     */
    public String cleanupAndRestart() {
        return FileConversionUtils.cleanupAndRestartForMaintenance();
    }

    /**
     * 分钟级健康巡检：仅在不健康且冷却结束后重建；重建成功后立即捞回 pending/卡住 converting/可重试 failed。
     */
    public String healthCheckAndRecover() {
        Map<String, Object> health = FileConversionUtils.healthCheckAndRecover();
        boolean recovered = Boolean.TRUE.equals(health.get("recovered"));
        boolean healthy = Boolean.TRUE.equals(health.get("healthy"));
        boolean skipped = Boolean.TRUE.equals(health.get("skipped"));
        String message = String.valueOf(health.getOrDefault("message", "LibreOffice 健康检查完成"));

        if (!recovered) {
            // 健康或冷却中：不联动重试，避免 LO 仍半死时空转
            return message;
        }

        // 重建后立刻捞回：缩短 stuck 窗口，且此时 LO 已可用
        Map<String, Object> daily = practicalPreviewRetryService.retryAfterOfficeRecovered();
        Map<String, Object> attachments = practicalArtifactService.retryAttachmentsAfterOfficeRecovered();
        Map<String, Object> county = countyExamPreviewRetryService.retryAfterOfficeRecovered();
        String summary = String.format(
                "%s；联动捞回 旧答卷[匹配%s/触发%s] 多附件[匹配%s/触发%s] 区域抽测[匹配%s/触发%s]",
                message,
                daily.get("matchedCount"), daily.get("triggeredCount"),
                attachments.get("matchedCount"), attachments.get("triggeredCount"),
                county.get("matchedCount"), county.get("triggeredCount"));
        log.info("【LibreOffice自愈】{}", summary);
        // 防止 unused 告警语义不清；healthy 仅用于日志上下文
        if (!healthy) {
            log.debug("【LibreOffice自愈】重建后探测仍标记不健康，请关注下轮巡检");
        }
        if (skipped) {
            log.debug("【LibreOffice自愈】本轮曾处于冷却（不应与 recovered 同时为 true）");
        }
        return summary;
    }
}
