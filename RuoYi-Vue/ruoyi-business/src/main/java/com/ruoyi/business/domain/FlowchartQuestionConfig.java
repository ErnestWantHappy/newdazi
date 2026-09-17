package com.ruoyi.business.domain;

import java.util.Date;

/** 画程题目配置，保存教师基础图、标准答案和结构检查规则。 */
public class FlowchartQuestionConfig {
    private Long questionId;
    private String schemaVersion;
    private String starterJson;
    private String answerJson;
    private String permissionsJson;
    private String rulesJson;
    private Integer configRevision;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(String schemaVersion) { this.schemaVersion = schemaVersion; }
    public String getStarterJson() { return starterJson; }
    public void setStarterJson(String starterJson) { this.starterJson = starterJson; }
    public String getAnswerJson() { return answerJson; }
    public void setAnswerJson(String answerJson) { this.answerJson = answerJson; }
    public String getPermissionsJson() { return permissionsJson; }
    public void setPermissionsJson(String permissionsJson) { this.permissionsJson = permissionsJson; }
    public String getRulesJson() { return rulesJson; }
    public void setRulesJson(String rulesJson) { this.rulesJson = rulesJson; }
    public Integer getConfigRevision() { return configRevision; }
    public void setConfigRevision(Integer configRevision) { this.configRevision = configRevision; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}

