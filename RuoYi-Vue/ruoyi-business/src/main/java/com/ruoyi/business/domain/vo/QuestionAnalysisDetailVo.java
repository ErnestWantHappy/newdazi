package com.ruoyi.business.domain.vo;

import java.util.Map;

/**
 * 题目分析详情视图对象
 */
public class QuestionAnalysisDetailVo {
    private Long questionId;
    private String questionContent;
    private String questionType; // choice, judgment
    private String answer; // 正确答案
    private Integer studentCount; // 答题人数
    private Integer correctCount; // 正确人数
    private Double accuracy; // 正确率 (%)
    private Map<String, Integer> answerDistribution; // 答案分布 {A: 10, B: 2, ...}
    private Map<String, String> optionContents; // 选项内容 {A: "content", B: "content"}

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

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

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Map<String, Integer> getAnswerDistribution() {
        return answerDistribution;
    }

    public void setAnswerDistribution(Map<String, Integer> answerDistribution) {
        this.answerDistribution = answerDistribution;
    }

    public Map<String, String> getOptionContents() {
        return optionContents;
    }

    public void setOptionContents(Map<String, String> optionContents) {
        this.optionContents = optionContents;
    }
}
