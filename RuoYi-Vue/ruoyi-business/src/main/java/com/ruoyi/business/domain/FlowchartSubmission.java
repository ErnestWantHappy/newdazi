package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;

/** 学生画程不可变正式提交版本。 */
public class FlowchartSubmission {
    private Long submissionId;
    private Long studentId;
    private Long lessonId;
    private Long questionId;
    private Integer versionNo;
    private Integer draftRevision;
    private String schemaVersion;
    private String documentJson;
    private String rulesSnapshotJson;
    private String checkResultJson;
    private BigDecimal suggestedScore;
    private Long answerId;
    private Date submitTime;

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    public Integer getDraftRevision() { return draftRevision; }
    public void setDraftRevision(Integer draftRevision) { this.draftRevision = draftRevision; }
    public String getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(String schemaVersion) { this.schemaVersion = schemaVersion; }
    public String getDocumentJson() { return documentJson; }
    public void setDocumentJson(String documentJson) { this.documentJson = documentJson; }
    public String getRulesSnapshotJson() { return rulesSnapshotJson; }
    public void setRulesSnapshotJson(String rulesSnapshotJson) { this.rulesSnapshotJson = rulesSnapshotJson; }
    public String getCheckResultJson() { return checkResultJson; }
    public void setCheckResultJson(String checkResultJson) { this.checkResultJson = checkResultJson; }
    public BigDecimal getSuggestedScore() { return suggestedScore; }
    public void setSuggestedScore(BigDecimal suggestedScore) { this.suggestedScore = suggestedScore; }
    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }
    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
}

