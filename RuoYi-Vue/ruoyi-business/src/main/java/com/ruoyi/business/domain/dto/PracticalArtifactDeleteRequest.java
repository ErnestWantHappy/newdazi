package com.ruoyi.business.domain.dto;

/**
 * 删除当前逻辑作品，历史版本继续保留。
 */
public class PracticalArtifactDeleteRequest
{
    private Long lessonId;
    private Long questionId;
    private Long expectedVersionId;

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Long getExpectedVersionId() { return expectedVersionId; }
    public void setExpectedVersionId(Long expectedVersionId) { this.expectedVersionId = expectedVersionId; }
}
