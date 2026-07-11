package com.ruoyi.business.domain.dto;

import java.util.List;

/**
 * 区域抽测匿名评卷请求。
 */
public class CountyExamGradeRequest {
    private Long answerId;
    private Integer score;
    private List<ScoringDetailRequest> scoringDetails;

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public List<ScoringDetailRequest> getScoringDetails() {
        return scoringDetails;
    }

    public void setScoringDetails(List<ScoringDetailRequest> scoringDetails) {
        this.scoringDetails = scoringDetails;
    }

    public static class ScoringDetailRequest {
        private Long itemId;
        private Integer score;

        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }
    }
}
