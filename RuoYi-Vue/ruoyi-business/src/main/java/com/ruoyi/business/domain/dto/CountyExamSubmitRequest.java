package com.ruoyi.business.domain.dto;

import java.util.Map;

/**
 * 区域抽测答题提交请求。
 */
public class CountyExamSubmitRequest {
    private Long examId;
    private Map<Long, String> answers;
    private Map<Long, Integer> answerTimes;
    private Map<Long, TypingStatItem> typingStats;

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public Map<Long, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Long, String> answers) {
        this.answers = answers;
    }

    public Map<Long, Integer> getAnswerTimes() {
        return answerTimes;
    }

    public void setAnswerTimes(Map<Long, Integer> answerTimes) {
        this.answerTimes = answerTimes;
    }

    public Map<Long, TypingStatItem> getTypingStats() {
        return typingStats;
    }

    public void setTypingStats(Map<Long, TypingStatItem> typingStats) {
        this.typingStats = typingStats;
    }

    public static class TypingStatItem {
        private Integer typingSpeed;
        private Double accuracyRate;
        private Double completionRate;

        public Integer getTypingSpeed() {
            return typingSpeed;
        }

        public void setTypingSpeed(Integer typingSpeed) {
            this.typingSpeed = typingSpeed;
        }

        public Double getAccuracyRate() {
            return accuracyRate;
        }

        public void setAccuracyRate(Double accuracyRate) {
            this.accuracyRate = accuracyRate;
        }

        public Double getCompletionRate() {
            return completionRate;
        }

        public void setCompletionRate(Double completionRate) {
            this.completionRate = completionRate;
        }
    }
}
