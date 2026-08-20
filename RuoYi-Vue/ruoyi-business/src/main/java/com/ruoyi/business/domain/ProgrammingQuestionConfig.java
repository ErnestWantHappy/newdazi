package com.ruoyi.business.domain;

import java.util.Date;
import java.util.List;

/** Python 题目独有的执行限制；题目基础信息仍在 biz_question。 */
public class ProgrammingQuestionConfig {
    private Long questionId;
    private String languageCode = "python";
    private String starterCode;
    private String inputDescription;
    private String outputDescription;
    private String sampleExplanation;
    private String constraintsText;
    private String notesText;
    private Double timeLimitSeconds = 2D;
    private Integer memoryLimitKb = 131072;
    private Integer maxProcesses = 8;
    private Integer maxFileSizeKb = 1024;
    private Integer maxOutputKb = 64;
    private String enabled = "1";
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;
    private List<ProgrammingTestCase> testCases;
    public Long getQuestionId() { return questionId; } public void setQuestionId(Long v) { questionId = v; }
    public String getLanguageCode() { return languageCode; } public void setLanguageCode(String v) { languageCode = v; }
    public String getStarterCode() { return starterCode; } public void setStarterCode(String v) { starterCode = v; }
    public String getInputDescription() { return inputDescription; } public void setInputDescription(String v) { inputDescription = v; }
    public String getOutputDescription() { return outputDescription; } public void setOutputDescription(String v) { outputDescription = v; }
    public String getSampleExplanation() { return sampleExplanation; } public void setSampleExplanation(String v) { sampleExplanation = v; }
    public String getConstraintsText() { return constraintsText; } public void setConstraintsText(String v) { constraintsText = v; }
    public String getNotesText() { return notesText; } public void setNotesText(String v) { notesText = v; }
    public Double getTimeLimitSeconds() { return timeLimitSeconds; } public void setTimeLimitSeconds(Double v) { timeLimitSeconds = v; }
    public Integer getMemoryLimitKb() { return memoryLimitKb; } public void setMemoryLimitKb(Integer v) { memoryLimitKb = v; }
    public Integer getMaxProcesses() { return maxProcesses; } public void setMaxProcesses(Integer v) { maxProcesses = v; }
    public Integer getMaxFileSizeKb() { return maxFileSizeKb; } public void setMaxFileSizeKb(Integer v) { maxFileSizeKb = v; }
    public Integer getMaxOutputKb() { return maxOutputKb; } public void setMaxOutputKb(Integer v) { maxOutputKb = v; }
    public String getEnabled() { return enabled; } public void setEnabled(String v) { enabled = v; }
    public String getCreateBy() { return createBy; } public void setCreateBy(String v) { createBy = v; }
    public Date getCreateTime() { return createTime; } public void setCreateTime(Date v) { createTime = v; }
    public String getUpdateBy() { return updateBy; } public void setUpdateBy(String v) { updateBy = v; }
    public Date getUpdateTime() { return updateTime; } public void setUpdateTime(Date v) { updateTime = v; }
    public List<ProgrammingTestCase> getTestCases() { return testCases; } public void setTestCases(List<ProgrammingTestCase> v) { testCases = v; }
}
