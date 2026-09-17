package com.ruoyi.business.domain.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 物联网班级自动分组请求
 */
public class IotClassGroupingRequest
{
    @NotNull(message = "实验ID不能为空")
    private Long experimentId;

    @NotBlank(message = "届别不能为空")
    private String entryYear;

    @NotBlank(message = "班级编号不能为空")
    private String classCode;

    @NotNull(message = "每组人数不能为空")
    @Min(value = 1, message = "每组人数必须为正整数")
    private Integer groupSize = 4;

    /** 是否强制重新分组（覆盖已有快照） */
    private Boolean force = false;

    public Long getExperimentId() { return experimentId; }
    public void setExperimentId(Long experimentId) { this.experimentId = experimentId; }

    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public Integer getGroupSize() { return groupSize; }
    public void setGroupSize(Integer groupSize) { this.groupSize = groupSize; }

    public Boolean getForce() { return force; }
    public void setForce(Boolean force) { this.force = force; }
}
