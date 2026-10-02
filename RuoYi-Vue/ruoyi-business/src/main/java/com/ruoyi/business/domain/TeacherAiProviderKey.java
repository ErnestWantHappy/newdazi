package com.ruoyi.business.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

/** 教师按厂商保存的凭据；密文只供服务端使用。 */
public class TeacherAiProviderKey
{
    private Long teacherUserId;
    private String providerCode;
    @JsonIgnore private String apiKeyCiphertext;
    private String apiKeyHint;
    private String modelName;

    public Long getTeacherUserId() { return teacherUserId; }
    public void setTeacherUserId(Long value) { teacherUserId = value; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String value) { providerCode = value; }
    public String getApiKeyCiphertext() { return apiKeyCiphertext; }
    public void setApiKeyCiphertext(String value) { apiKeyCiphertext = value; }
    public String getApiKeyHint() { return apiKeyHint; }
    public void setApiKeyHint(String value) { apiKeyHint = value; }
    public String getModelName() { return modelName; }
    public void setModelName(String value) { modelName = value; }
}
