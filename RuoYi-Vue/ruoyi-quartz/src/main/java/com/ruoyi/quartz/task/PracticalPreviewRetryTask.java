package com.ruoyi.quartz.task;

import com.ruoyi.business.service.PracticalPreviewRetryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 操作题预览失败重试定时任务
 */
@Component("practicalPreviewRetryTask")
public class PracticalPreviewRetryTask {

    @Autowired
    private PracticalPreviewRetryService practicalPreviewRetryService;

    public String retryFailedStudentAnswerPreviews() {
        Map<String, Object> result = practicalPreviewRetryService.retryExpiredFailedPreviews();
        return String.format("操作题自动重试完成：匹配 %s 条，触发 %s 条，跳过 %s 条",
                result.get("matchedCount"),
                result.get("triggeredCount"),
                result.get("skippedCount"));
    }
}
