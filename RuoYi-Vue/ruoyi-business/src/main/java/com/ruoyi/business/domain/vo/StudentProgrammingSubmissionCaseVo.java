package com.ruoyi.business.domain.vo;

/** 公开样例的一次运行结果；错误内容只保留 Judge0 面向代码作者的诊断。 */
public class StudentProgrammingSubmissionCaseVo {
    private Long testCaseId;
    private String caseName;
    private String inputText;
    private String expectedOutput;
    private String actualOutput;
    private String statusCode;
    private Double timeSeconds;
    private Integer memoryKb;
    private String errorMessage;

    public Long getTestCaseId() { return testCaseId; }
    public void setTestCaseId(Long v) { testCaseId = v; }
    public String getCaseName() { return caseName; }
    public void setCaseName(String v) { caseName = v; }
    public String getInputText() { return inputText; }
    public void setInputText(String v) { inputText = v; }
    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String v) { expectedOutput = v; }
    public String getActualOutput() { return actualOutput; }
    public void setActualOutput(String v) { actualOutput = v; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String v) { statusCode = v; }
    public Double getTimeSeconds() { return timeSeconds; }
    public void setTimeSeconds(Double v) { timeSeconds = v; }
    public Integer getMemoryKb() { return memoryKb; }
    public void setMemoryKb(Integer v) { memoryKb = v; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String v) { errorMessage = v; }
}
