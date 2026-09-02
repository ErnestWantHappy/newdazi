package com.ruoyi.business.domain;

import java.util.Date;

/** 课程使用画程题目时冻结的出题和评分口径。 */
public class FlowchartLessonSnapshot {
    private Long snapshotId;
    private Long lessonId;
    private Long questionId;
    private Integer sourceRevision;
    private String schemaVersion;
    private String starterJson;
    private String answerJson;
    private String permissionsJson;
    private String rulesJson;
    private Date createTime;

    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Integer getSourceRevision() { return sourceRevision; }
    public void setSourceRevision(Integer sourceRevision) { this.sourceRevision = sourceRevision; }
    public String getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(String schemaVersion) { this.schemaVersion = schemaVersion; }
    public String getStarterJson() { return starterJson; }
    public void setStarterJson(String starterJson) { this.starterJson = starterJson; }
    public String getAnswerJson() { return answerJson; }
    public void setAnswerJson(String answerJson) { this.answerJson = answerJson; }
    public String getPermissionsJson() { return permissionsJson; }
    public void setPermissionsJson(String permissionsJson) { this.permissionsJson = permissionsJson; }
    public String getRulesJson() { return rulesJson; }
    public void setRulesJson(String rulesJson) { this.rulesJson = rulesJson; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}

