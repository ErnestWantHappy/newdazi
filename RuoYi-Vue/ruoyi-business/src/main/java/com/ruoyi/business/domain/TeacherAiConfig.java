package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;

/** 教师个人的视觉模型配置；密文字段永不序列化到前端。 */
public class TeacherAiConfig
{
    private Long teacherUserId;
    private String providerCode;
    private String modelName;
    private String endpointUrl;
    @JsonIgnore private String apiKeyCiphertext;
    private String apiKeyHint;
    private Boolean enabled;
    private Date createTime;
    private Date updateTime;

    public Long getTeacherUserId() { return teacherUserId; }
    public void setTeacherUserId(Long value) { teacherUserId = value; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String value) { providerCode = value; }
    public String getModelName() { return modelName; }
    public void setModelName(String value) { modelName = value; }
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String value) { endpointUrl = value; }
    public String getApiKeyCiphertext() { return apiKeyCiphertext; }
    public void setApiKeyCiphertext(String value) { apiKeyCiphertext = value; }
    public String getApiKeyHint() { return apiKeyHint; }
    public void setApiKeyHint(String value) { apiKeyHint = value; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean value) { enabled = value; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date value) { createTime = value; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date value) { updateTime = value; }
}
