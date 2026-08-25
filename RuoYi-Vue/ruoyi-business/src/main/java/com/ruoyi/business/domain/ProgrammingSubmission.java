package com.ruoyi.business.domain;

import java.util.Date;
import java.util.List;

public class ProgrammingSubmission {
    private Long submissionId; private String submissionKey; private Long studentId; private Long lessonId; private Long questionId;
    private String sourceCode; private String customInput; private String submissionKind; private String statusCode; private String statusMessage; private Integer score;
    private Integer passedCaseCount; private Integer totalCaseCount; private String judge0Token; private Integer judge0StatusId;
    private Double timeSeconds; private Integer memoryKb; private String errorSummary; private String requestIp;
    private Date submittedAt; private Date judgedAt; private Date cancelledAt; private List<ProgrammingSubmissionCase> cases;
    public Long getSubmissionId() { return submissionId; } public void setSubmissionId(Long v) { submissionId = v; }
    public String getSubmissionKey() { return submissionKey; } public void setSubmissionKey(String v) { submissionKey = v; }
    public Long getStudentId() { return studentId; } public void setStudentId(Long v) { studentId = v; }
    public Long getLessonId() { return lessonId; } public void setLessonId(Long v) { lessonId = v; }
    public Long getQuestionId() { return questionId; } public void setQuestionId(Long v) { questionId = v; }
    public String getSourceCode() { return sourceCode; } public void setSourceCode(String v) { sourceCode = v; }
    public String getCustomInput() { return customInput; } public void setCustomInput(String v) { customInput = v; }
    public String getSubmissionKind() { return submissionKind; } public void setSubmissionKind(String v) { submissionKind = v; }
    public String getStatusCode() { return statusCode; } public void setStatusCode(String v) { statusCode = v; }
    public String getStatusMessage() { return statusMessage; } public void setStatusMessage(String v) { statusMessage = v; }
    public Integer getScore() { return score; } public void setScore(Integer v) { score = v; }
    public Integer getPassedCaseCount() { return passedCaseCount; } public void setPassedCaseCount(Integer v) { passedCaseCount = v; }
    public Integer getTotalCaseCount() { return totalCaseCount; } public void setTotalCaseCount(Integer v) { totalCaseCount = v; }
    public String getJudge0Token() { return judge0Token; } public void setJudge0Token(String v) { judge0Token = v; }
    public Integer getJudge0StatusId() { return judge0StatusId; } public void setJudge0StatusId(Integer v) { judge0StatusId = v; }
    public Double getTimeSeconds() { return timeSeconds; } public void setTimeSeconds(Double v) { timeSeconds = v; }
    public Integer getMemoryKb() { return memoryKb; } public void setMemoryKb(Integer v) { memoryKb = v; }
    public String getErrorSummary() { return errorSummary; } public void setErrorSummary(String v) { errorSummary = v; }
    public String getRequestIp() { return requestIp; } public void setRequestIp(String v) { requestIp = v; }
    public Date getSubmittedAt() { return submittedAt; } public void setSubmittedAt(Date v) { submittedAt = v; }
    public Date getJudgedAt() { return judgedAt; } public void setJudgedAt(Date v) { judgedAt = v; }
    public Date getCancelledAt() { return cancelledAt; } public void setCancelledAt(Date v) { cancelledAt = v; }
    public List<ProgrammingSubmissionCase> getCases() { return cases; } public void setCases(List<ProgrammingSubmissionCase> v) { cases = v; }
}
