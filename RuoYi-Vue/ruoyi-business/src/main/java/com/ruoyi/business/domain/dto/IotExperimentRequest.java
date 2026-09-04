package com.ruoyi.business.domain.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 创建物联网实验请求。 */
public class IotExperimentRequest
{
    @NotNull
    private Long lessonId;
    @NotBlank
    @Size(max = 64)
    private String activityCode;
    @NotBlank
    @Size(max = 128)
    private String title;
    @Size(max = 1000)
    private String description;

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long value) { lessonId = value; }
    public String getActivityCode() { return activityCode; }
    public void setActivityCode(String value) { activityCode = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
}
