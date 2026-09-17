package com.ruoyi.business.domain.dto;

/** 教师开启或关闭课程在线协作。 */
public class CollaborationSettingsRequest
{
    private Boolean enabled;
    private Long questionId;
    private Long materialId;

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
}

