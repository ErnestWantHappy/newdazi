package com.ruoyi.business.domain.dto;

import java.util.List;

/**
 * 区域抽测评卷教师分配请求。
 */
public class CountyExamGraderAllocateRequest {
    private List<Long> graderIds;
    private List<Assignment> assignments;

    public List<Long> getGraderIds() {
        return graderIds;
    }

    public void setGraderIds(List<Long> graderIds) {
        this.graderIds = graderIds;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }

    /**
     * 一条按题评卷配置。
     */
    public static class Assignment {
        private Long questionId;
        private Long graderId;
        private Integer targetCount;

        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public Long getGraderId() {
            return graderId;
        }

        public void setGraderId(Long graderId) {
            this.graderId = graderId;
        }

        public Integer getTargetCount() {
            return targetCount;
        }

        public void setTargetCount(Integer targetCount) {
            this.targetCount = targetCount;
        }
    }
}
