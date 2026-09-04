package com.ruoyi.business.service;

import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作题预览失败重试服务。
 * <p>
 * 定时小时级重试 + LibreOffice 重建后立即捞回（缩短 stuck 窗口）。
 * 仅负责重投转换任务；不直接操作 Office 进程池。
 */
@Service
public class PracticalPreviewRetryService {

    private static final Logger log = LoggerFactory.getLogger(PracticalPreviewRetryService.class);
    /** failed 记录默认至少间隔 1 小时再自动重试 */
    private static final long AUTO_RETRY_INTERVAL_MILLIS = 60L * 60L * 1000L;
    /** 定时任务捞取 pending/converting 卡住记录的窗口（小时级任务仍用 10 分钟） */
    private static final long STUCK_PREVIEW_TIMEOUT_MILLIS = 10L * 60L * 1000L;
    /**
     * Office 重建后立即捞回时使用的卡住窗口。
     * 明显短于定时窗口，目标是 1～2 分钟内消化半死期间遗留任务。
     */
    private static final long RECOVER_STUCK_PREVIEW_TIMEOUT_MILLIS = 2L * 60L * 1000L;
    /** Office 重建后 failed 也可立即重试，不再等 1 小时 */
    private static final long RECOVER_FAILED_RETRY_INTERVAL_MILLIS = 0L;
    private static final int MAX_AUTO_RETRY_COUNT = 3;

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    @Autowired
    private AsyncConversionService asyncConversionService;

    /**
     * 自动重试达到时间窗口的失败记录（Quartz 小时级）
     */
    public Map<String, Object> retryExpiredFailedPreviews() {
        return doRetry(AUTO_RETRY_INTERVAL_MILLIS, STUCK_PREVIEW_TIMEOUT_MILLIS, "scheduler");
    }

    /**
     * LibreOffice 自愈重建成功后立即捞回。
     * 仅应在 Office 池已恢复可用时调用，避免半死池上空转。
     */
    public Map<String, Object> retryAfterOfficeRecovered() {
        return doRetry(RECOVER_FAILED_RETRY_INTERVAL_MILLIS, RECOVER_STUCK_PREVIEW_TIMEOUT_MILLIS, "office-recover");
    }

    private Map<String, Object> doRetry(long failedIntervalMillis, long stuckTimeoutMillis, String triggerSource) {
        Date retryBefore = new Date(System.currentTimeMillis() - failedIntervalMillis);
        Date stuckBefore = new Date(System.currentTimeMillis() - stuckTimeoutMillis);
        List<BizStudentAnswer> failedAnswers = studentAnswerMapper.selectRecoverablePracticalAnswersForRetry(
                retryBefore, stuckBefore, MAX_AUTO_RETRY_COUNT
        );
        int triggeredCount = 0;

        for (BizStudentAnswer answer : failedAnswers) {
            if (asyncConversionService.claimRetryAndExecute(answer, false, triggerSource)) {
                triggeredCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("matchedCount", failedAnswers.size());
        result.put("triggeredCount", triggeredCount);
        result.put("skippedCount", Math.max(failedAnswers.size() - triggeredCount, 0));

        log.info("【操作题自动重试】source={} 匹配 {} 条，触发 {} 条，跳过 {} 条",
                triggerSource, failedAnswers.size(), triggeredCount,
                Math.max(failedAnswers.size() - triggeredCount, 0));
        return result;
    }

    /**
     * 教师按当前班级当前操作题手动重转失败记录
     */
    public Map<String, Object> retryFailedPreviewsByQuestionAndClass(Long lessonId, Long questionId,
                                                                     String classCode, String entryYear, Long deptId) {
        Date stuckBefore = new Date(System.currentTimeMillis() - STUCK_PREVIEW_TIMEOUT_MILLIS);
        List<BizStudentAnswer> failedAnswers = studentAnswerMapper.selectRecoverablePracticalAnswersForManualRetry(
                lessonId, questionId, classCode, entryYear, deptId, stuckBefore
        );
        int triggeredCount = 0;

        for (BizStudentAnswer answer : failedAnswers) {
            if (asyncConversionService.claimRetryAndExecute(answer, true, "teacher-manual")) {
                triggeredCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("matchedCount", failedAnswers.size());
        result.put("triggeredCount", triggeredCount);
        result.put("skippedCount", Math.max(failedAnswers.size() - triggeredCount, 0));

        log.info("【操作题手动重转】lessonId={}, questionId={}, classCode={}, entryYear={}, 匹配 {} 条，触发 {} 条，跳过 {} 条",
                lessonId, questionId, classCode, entryYear,
                failedAnswers.size(), triggeredCount, Math.max(failedAnswers.size() - triggeredCount, 0));
        return result;
    }
}
