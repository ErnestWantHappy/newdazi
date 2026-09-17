package com.ruoyi.business.domain;

import java.util.Date;

/** 普通课程操作题在课程保存时固化的评分依据。 */
public class PracticalRubricSnapshot
{
    private Long snapshotId;
    private Long lessonId;
    private Long questionId;
    private Integer snapshotVersion;
    private String questionContent;
    private Integer questionScore;
    private String scoringItemsJson;
    private String referenceMaterialsJson;
    private Long createdByUserId;
    private Date createTime;

    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Integer getSnapshotVersion() { return snapshotVersion; }
    public void setSnapshotVersion(Integer snapshotVersion) { this.snapshotVersion = snapshotVersion; }
    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
    public Integer getQuestionScore() { return questionScore; }
    public void setQuestionScore(Integer questionScore) { this.questionScore = questionScore; }
    public String getScoringItemsJson() { return scoringItemsJson; }
    public void setScoringItemsJson(String scoringItemsJson) { this.scoringItemsJson = scoringItemsJson; }
    public String getReferenceMaterialsJson() { return referenceMaterialsJson; }
    public void setReferenceMaterialsJson(String referenceMaterialsJson) { this.referenceMaterialsJson = referenceMaterialsJson; }
    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
