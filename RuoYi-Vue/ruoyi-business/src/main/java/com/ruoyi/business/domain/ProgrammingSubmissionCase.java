package com.ruoyi.business.domain;

public class ProgrammingSubmissionCase {
    private Long submissionCaseId; private Long submissionId; private Long testCaseId; private String isPublic;
    private String statusCode; private Integer judge0StatusId; private Double timeSeconds; private Integer memoryKb; private String outputText; private String errorSummary;
    public Long getSubmissionCaseId() { return submissionCaseId; } public void setSubmissionCaseId(Long v) { submissionCaseId = v; }
    public Long getSubmissionId() { return submissionId; } public void setSubmissionId(Long v) { submissionId = v; }
    public Long getTestCaseId() { return testCaseId; } public void setTestCaseId(Long v) { testCaseId = v; }
    public String getIsPublic() { return isPublic; } public void setIsPublic(String v) { isPublic = v; }
    public String getStatusCode() { return statusCode; } public void setStatusCode(String v) { statusCode = v; }
    public Integer getJudge0StatusId() { return judge0StatusId; } public void setJudge0StatusId(Integer v) { judge0StatusId = v; }
    public Double getTimeSeconds() { return timeSeconds; } public void setTimeSeconds(Double v) { timeSeconds = v; }
    public Integer getMemoryKb() { return memoryKb; } public void setMemoryKb(Integer v) { memoryKb = v; }
    public String getOutputText() { return outputText; } public void setOutputText(String v) { outputText = v; }
    public String getErrorSummary() { return errorSummary; } public void setErrorSummary(String v) { errorSummary = v; }
}
