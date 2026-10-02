package com.ruoyi.business.domain;

import java.util.Date;

/**
 * 学生数据分析实验提交记录实体
 */
public class DataLabSubmission {
    private Long submissionId;
    private Long revision;
    private Integer maxScore;
    public Long getRevision() { return revision; }
    public void setRevision(Long revision) { this.revision = revision; }
    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }
    private Long studentId;
    private Long questionId;
    private String sourceType;
    private Long sourceId;
    private String sourceCode;
    private String chartImageUrl;
    private String outputText;
    private Integer score;
    private String scoreComment;
    private Long scoredBy;
    private Date scoredAt;
    private Date submittedAt;

    /** 关联展示字段 */
    private String studentName;
    private String studentNumber;

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public String getChartImageUrl() { return chartImageUrl; }
    public void setChartImageUrl(String chartImageUrl) { this.chartImageUrl = chartImageUrl; }

    public String getOutputText() { return outputText; }
    public void setOutputText(String outputText) { this.outputText = outputText; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getScoreComment() { return scoreComment; }
    public void setScoreComment(String scoreComment) { this.scoreComment = scoreComment; }

    public Long getScoredBy() { return scoredBy; }
    public void setScoredBy(Long scoredBy) { this.scoredBy = scoredBy; }

    public Date getScoredAt() { return scoredAt; }
    public void setScoredAt(Date scoredAt) { this.scoredAt = scoredAt; }

    public Date getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Date submittedAt) { this.submittedAt = submittedAt; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
}
