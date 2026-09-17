package com.ruoyi.business.domain.dto;

import java.util.List;

/**
 * 学生提交一个完整逻辑作品。
 */
public class PracticalArtifactSubmitRequest
{
    private Long lessonId;
    private Long questionId;
    private Long expectedVersionId;
    private List<String> uploadTokens;

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Long getExpectedVersionId() { return expectedVersionId; }
    public void setExpectedVersionId(Long expectedVersionId) { this.expectedVersionId = expectedVersionId; }
    public List<String> getUploadTokens() { return uploadTokens; }
    public void setUploadTokens(List<String> uploadTokens) { this.uploadTokens = uploadTokens; }
}
