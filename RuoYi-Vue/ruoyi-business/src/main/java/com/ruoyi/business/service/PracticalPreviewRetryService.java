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
 * 操作题预览失败重试服务
 */
@Service
public class PracticalPreviewRetryService {

    private static final Logger log = LoggerFactory.getLogger(PracticalPreviewRetryService.class);
    private static final long AUTO_RETRY_INTERVAL_MILLIS = 60L * 60L * 1000L;
    private static final long STUCK_PREVIEW_TIMEOUT_MILLIS = 10L * 60L * 1000L;
    private static final int MAX_AUTO_RETRY_COUNT = 3;

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    @Autowired
    private AsyncConversionService asyncConversionService;

    /**
     * 自动重试达到时间窗口的失败记录
     */
    public Map<String, Object> retryExpiredFailedPreviews() {
        Date retryBefore = new Date(System.currentTimeMillis() - AUTO_RETRY_INTERVAL_MILLIS);
        Date stuckBefore = new Date(System.currentTimeMillis() - STUCK_PREVIEW_TIMEOUT_MILLIS);
        List<BizStudentAnswer> failedAnswers = studentAnswerMapper.selectRecoverablePracticalAnswersForRetry(
                retryBefore, stuckBefore, MAX_AUTO_RETRY_COUNT
        );
        int triggeredCount = 0;

        for (BizStudentAnswer answer : failedAnswers) {
            if (asyncConversionService.claimRetryAndExecute(answer, false, "scheduler")) {
                triggeredCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("matchedCount", failedAnswers.size());
        result.put("triggeredCount", triggeredCount);
        result.put("skippedCount", Math.max(failedAnswers.size() - triggeredCount, 0));

        log.info("【操作题自动重试】匹配 {} 条，触发 {} 条，跳过 {} 条",
                failedAnswers.size(), triggeredCount, Math.max(failedAnswers.size() - triggeredCount, 0));
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
