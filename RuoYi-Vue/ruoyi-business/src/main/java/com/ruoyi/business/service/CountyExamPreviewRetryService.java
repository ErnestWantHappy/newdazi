package com.ruoyi.business.service;

import com.ruoyi.business.domain.CountyExamAnswer;
import com.ruoyi.business.mapper.CountyExamAnswerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 区域抽测操作题预览失败重试服务。
 * <p>
 * 与日常操作题一致：小时级定时重试 + Office 重建后立即捞回。
 */
@Service
public class CountyExamPreviewRetryService {

    private static final Logger log = LoggerFactory.getLogger(CountyExamPreviewRetryService.class);
    private static final long AUTO_RETRY_INTERVAL_MILLIS = 60L * 60L * 1000L;
    private static final long STUCK_PREVIEW_TIMEOUT_MILLIS = 10L * 60L * 1000L;
    private static final long RECOVER_STUCK_PREVIEW_TIMEOUT_MILLIS = 2L * 60L * 1000L;
    private static final long RECOVER_FAILED_RETRY_INTERVAL_MILLIS = 0L;
    private static final int MAX_AUTO_RETRY_COUNT = 3;

    @Autowired
    private CountyExamAnswerMapper countyExamAnswerMapper;

    @Autowired
    private AsyncConversionService asyncConversionService;

    /**
     * 自动重试区域抽测中失败或卡住的 Word 预览（Quartz 小时级）。
     */
    public Map<String, Object> retryExpiredFailedPreviews() {
        return doRetry(AUTO_RETRY_INTERVAL_MILLIS, STUCK_PREVIEW_TIMEOUT_MILLIS, "county-scheduler");
    }

    /**
     * LibreOffice 自愈重建成功后立即捞回区域抽测卡住/失败预览。
     */
    public Map<String, Object> retryAfterOfficeRecovered() {
        return doRetry(RECOVER_FAILED_RETRY_INTERVAL_MILLIS, RECOVER_STUCK_PREVIEW_TIMEOUT_MILLIS, "county-office-recover");
    }

    private Map<String, Object> doRetry(long failedIntervalMillis, long stuckTimeoutMillis, String triggerSource) {
        Date retryBefore = new Date(System.currentTimeMillis() - failedIntervalMillis);
        Date stuckBefore = new Date(System.currentTimeMillis() - stuckTimeoutMillis);
        List<CountyExamAnswer> failedAnswers = countyExamAnswerMapper.selectRecoverablePreviewsForRetry(
                retryBefore, stuckBefore, MAX_AUTO_RETRY_COUNT);
        int triggeredCount = 0;

        for (CountyExamAnswer answer : failedAnswers) {
            if (asyncConversionService.claimCountyRetryAndExecute(answer, false, triggerSource)) {
                triggeredCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("matchedCount", failedAnswers.size());
        result.put("triggeredCount", triggeredCount);
        result.put("skippedCount", Math.max(failedAnswers.size() - triggeredCount, 0));

        log.info("【区域抽测预览自动重试】source={} 匹配 {} 条，触发 {} 条，跳过 {} 条",
                triggerSource, failedAnswers.size(), triggeredCount,
                Math.max(failedAnswers.size() - triggeredCount, 0));
        return result;
    }
}
