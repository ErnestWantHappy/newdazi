package com.ruoyi.business.domain.vo;

import java.util.Date;
import java.util.List;

/** 学生提交历史专用响应，隔离 Judge0 令牌、请求 IP 和平台内部诊断。 */
public class StudentProgrammingSubmissionVo {
    private Long submissionId;
    private String sourceCode;
    private String submissionKind;
    private String statusCode;
    private String statusMessage;
    private Integer score;
    private Integer passedCaseCount;
    private Integer totalCaseCount;
    private Double timeSeconds;
    private Integer memoryKb;
    private Date submittedAt;
    private Date judgedAt;
    private Date cancelledAt;
    private List<StudentProgrammingSubmissionCaseVo> cases;

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long v) { submissionId = v; }
    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String v) { sourceCode = v; }
    public String getSubmissionKind() { return submissionKind; }
    public void setSubmissionKind(String v) { submissionKind = v; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String v) { statusCode = v; }
    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String v) { statusMessage = v; }
    public Integer getScore() { return score; }
    public void setScore(Integer v) { score = v; }
    public Integer getPassedCaseCount() { return passedCaseCount; }
    public void setPassedCaseCount(Integer v) { passedCaseCount = v; }
    public Integer getTotalCaseCount() { return totalCaseCount; }
    public void setTotalCaseCount(Integer v) { totalCaseCount = v; }
    public Double getTimeSeconds() { return timeSeconds; }
    public void setTimeSeconds(Double v) { timeSeconds = v; }
    public Integer getMemoryKb() { return memoryKb; }
    public void setMemoryKb(Integer v) { memoryKb = v; }
    public Date getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Date v) { submittedAt = v; }
    public Date getJudgedAt() { return judgedAt; }
    public void setJudgedAt(Date v) { judgedAt = v; }
    public Date getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Date v) { cancelledAt = v; }
    public List<StudentProgrammingSubmissionCaseVo> getCases() { return cases; }
    public void setCases(List<StudentProgrammingSubmissionCaseVo> v) { cases = v; }
}
