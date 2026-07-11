package com.ruoyi.business.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 课程-题目关联对象 biz_lesson_question
 * 
 * @author ruoyi
 * @date 2025-08-18
 */
public class BizLessonQuestion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 课程ID */
    @Excel(name = "课程ID")
    private Long lessonId;

    /** 题目ID */
    @Excel(name = "题目ID")
    private Long questionId;

    /** 该题目在本课程中的分值 */
    @Excel(name = "该题目在本课程中的分值")
    private Long questionScore;

    /** 题目在课程中的排序 */
    @Excel(name = "题目在课程中的排序")
    private Long orderNum;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setLessonId(Long lessonId) 
    {
        this.lessonId = lessonId;
    }

    public Long getLessonId() 
    {
        return lessonId;
    }

    public void setQuestionId(Long questionId) 
    {
        this.questionId = questionId;
    }

    public Long getQuestionId() 
    {
        return questionId;
    }

    public void setQuestionScore(Long questionScore) 
    {
        this.questionScore = questionScore;
    }

    public Long getQuestionScore() 
    {
        return questionScore;
    }

    public void setOrderNum(Long orderNum) 
    {
        this.orderNum = orderNum;
    }

    public Long getOrderNum() 
    {
        return orderNum;
    }
    /** 题干内容 (非数据库字段) */
    private String questionContent;

    /** 题目类型 (非数据库字段) */
    private String questionType;

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("lessonId", getLessonId())
            .append("questionId", getQuestionId())
            .append("questionScore", getQuestionScore())
            .append("orderNum", getOrderNum())
            .append("questionContent", getQuestionContent())
            .append("questionType", getQuestionType())
            .toString();
    }
}
