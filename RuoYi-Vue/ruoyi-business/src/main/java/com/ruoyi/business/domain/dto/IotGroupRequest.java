package com.ruoyi.business.domain.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 创建物联网小组请求。 */
public class IotGroupRequest
{
    @NotNull
    private Long experimentId;
    @NotBlank
    @Size(max = 16)
    private String entryYear;
    @NotBlank
    @Size(max = 32)
    private String classCode;
    @NotBlank
    @Size(max = 32)
    private String groupCode;
    @NotBlank
    @Size(max = 64)
    private String groupName;

    public Long getExperimentId() { return experimentId; }
    public void setExperimentId(Long value) { experimentId = value; }
    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String value) { entryYear = value; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String value) { classCode = value; }
    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String value) { groupCode = value; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String value) { groupName = value; }
}
