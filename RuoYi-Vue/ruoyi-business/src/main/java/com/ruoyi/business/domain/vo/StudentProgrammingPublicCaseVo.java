package com.ruoyi.business.domain.vo;

/** 学生可见的公开样例，不包含权重和任何隐藏测试数据。 */
public class StudentProgrammingPublicCaseVo {
    private Long testCaseId;
    private String caseName;
    private String inputText;
    private String expectedOutput;

    public Long getTestCaseId() { return testCaseId; }
    public void setTestCaseId(Long v) { testCaseId = v; }
    public String getCaseName() { return caseName; }
    public void setCaseName(String v) { caseName = v; }
    public String getInputText() { return inputText; }
    public void setInputText(String v) { inputText = v; }
    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String v) { expectedOutput = v; }
}
