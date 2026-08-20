package com.ruoyi.business.domain.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 轮换班级课堂口令请求
 */
public class IotRotatePasscodeRequest
{
    @NotNull(message = "实验ID不能为空")
    private Long experimentId;

    @NotBlank(message = "届别不能为空")
    private String entryYear;

    @NotBlank(message = "班级编号不能为空")
    private String classCode;

    public Long getExperimentId() { return experimentId; }
    public void setExperimentId(Long experimentId) { this.experimentId = experimentId; }

    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
}
