package com.ruoyi.business.domain;

/**
 * 县考题目关联表 biz_county_exam_question
 * 
 * @author ruoyi
 */
public class CountyExamQuestion {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 县考ID */
    private Long examId;

    /** 题目ID */
    private Long questionId;

    /** 分值 */
    private Integer questionScore;

    /** 排序 */
    private Integer orderNum;

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

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Integer getQuestionScore() {
        return questionScore;
    }

    public void setQuestionScore(Integer questionScore) {
        this.questionScore = questionScore;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }
}
