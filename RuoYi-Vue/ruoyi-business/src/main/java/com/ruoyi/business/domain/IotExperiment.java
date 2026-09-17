package com.ruoyi.business.domain;

import java.util.Date;

/** 物联网课堂实验。 */
public class IotExperiment
{
    private Long experimentId;
    private Long lessonId;
    private Long deptId;
    private String activityCode;
    private String title;
    private String description;
    private String topicPrefix;
    private String status;
    private String createBy;
    private Date createTime;
    private Date updateTime;

    public Long getExperimentId() { return experimentId; }
    public void setExperimentId(Long value) { experimentId = value; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long value) { lessonId = value; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long value) { deptId = value; }
    public String getActivityCode() { return activityCode; }
    public void setActivityCode(String value) { activityCode = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public String getTopicPrefix() { return topicPrefix; }
    public void setTopicPrefix(String value) { topicPrefix = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String value) { createBy = value; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date value) { createTime = value; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date value) { updateTime = value; }
}
