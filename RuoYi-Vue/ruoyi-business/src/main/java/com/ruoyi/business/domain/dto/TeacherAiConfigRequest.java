package com.ruoyi.business.domain.dto;

/** 教师保存模型配置时提交的最小字段。 */
public class TeacherAiConfigRequest
{
    private String apiKey;
    private String modelName;
    public String getApiKey() { return apiKey; }
    public void setApiKey(String value) { apiKey = value; }
    public String getModelName() { return modelName; }
    public void setModelName(String value) { modelName = value; }
}
