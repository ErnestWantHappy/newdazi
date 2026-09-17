package com.ruoyi.business.domain;

import java.util.Date;

/** 学生画程自动保存草稿。 */
public class FlowchartDraft {
    private Long draftId;
    private Long studentId;
    private Long lessonId;
    private Long questionId;
    private String schemaVersion;
    private String documentJson;
    private Integer revision;
    private Integer baseSubmissionVersion;
    private Date createTime;
    private Date updateTime;

    public Long getDraftId() { return draftId; }
    public void setDraftId(Long draftId) { this.draftId = draftId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(String schemaVersion) { this.schemaVersion = schemaVersion; }
    public String getDocumentJson() { return documentJson; }
    public void setDocumentJson(String documentJson) { this.documentJson = documentJson; }
    public Integer getRevision() { return revision; }
    public void setRevision(Integer revision) { this.revision = revision; }
    public Integer getBaseSubmissionVersion() { return baseSubmissionVersion; }
    public void setBaseSubmissionVersion(Integer baseSubmissionVersion) { this.baseSubmissionVersion = baseSubmissionVersion; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}

