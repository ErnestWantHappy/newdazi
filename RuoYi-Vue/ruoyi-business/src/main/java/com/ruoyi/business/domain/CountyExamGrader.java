package com.ruoyi.business.domain;

/**
 * 评卷任务分配表 biz_county_exam_grader
 * 
 * @author ruoyi
 */
public class CountyExamGrader {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 县考ID */
    private Long examId;

    /** 教师ID */
    private Long graderId;

    /** 分配数量 */
    private Integer targetCount;

    /** 已评数量 */
    private Integer gradedCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
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

    public Integer getGradedCount() {
        return gradedCount;
    }

    public void setGradedCount(Integer gradedCount) {
        this.gradedCount = gradedCount;
    }
}
