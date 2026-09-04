package com.ruoyi.business.domain;

import java.util.Date;

/** 测试点的期望输出始终只在后端数据层出现。 */
public class ProgrammingTestCase {
    private Long testCaseId; private Long questionId; private String caseName; private String inputText;
    private String expectedOutput; private String isPublic = "0"; private Double scoreWeight = 1D; private Integer orderNum;
    private String createBy; private Date createTime; private String updateBy; private Date updateTime;
    public Long getTestCaseId() { return testCaseId; } public void setTestCaseId(Long v) { testCaseId = v; }
    public Long getQuestionId() { return questionId; } public void setQuestionId(Long v) { questionId = v; }
    public String getCaseName() { return caseName; } public void setCaseName(String v) { caseName = v; }
    public String getInputText() { return inputText; } public void setInputText(String v) { inputText = v; }
    public String getExpectedOutput() { return expectedOutput; } public void setExpectedOutput(String v) { expectedOutput = v; }
    public String getIsPublic() { return isPublic; } public void setIsPublic(String v) { isPublic = v; }
    public Double getScoreWeight() { return scoreWeight; } public void setScoreWeight(Double v) { scoreWeight = v; }
    public Integer getOrderNum() { return orderNum; } public void setOrderNum(Integer v) { orderNum = v; }
    public String getCreateBy() { return createBy; } public void setCreateBy(String v) { createBy = v; }
    public Date getCreateTime() { return createTime; } public void setCreateTime(Date v) { createTime = v; }
    public String getUpdateBy() { return updateBy; } public void setUpdateBy(String v) { updateBy = v; }
    public Date getUpdateTime() { return updateTime; } public void setUpdateTime(Date v) { updateTime = v; }
}
