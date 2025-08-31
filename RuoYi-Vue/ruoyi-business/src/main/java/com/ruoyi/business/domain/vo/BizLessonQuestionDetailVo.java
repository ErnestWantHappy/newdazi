package com.ruoyi.business.domain.vo;

import com.ruoyi.business.domain.BizLessonQuestion;

/**
 * 课程题目关联详情 - 视图对象
 * (比 BizLessonQuestion 多了题干和题型)
 */
public class BizLessonQuestionDetailVo extends BizLessonQuestion {
    private String questionContent;
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
}
