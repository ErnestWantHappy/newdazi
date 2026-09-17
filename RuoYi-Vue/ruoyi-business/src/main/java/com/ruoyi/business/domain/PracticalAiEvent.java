package com.ruoyi.business.domain;

import java.util.Date;

/** 教师可见的 AI 任务安全事件；不保存密钥、提示词、模型原文或异常堆栈。 */
public class PracticalAiEvent
{
    private Long eventId;
    private Long jobId;
    private Long resultId;
    private String eventLevel;
    private String eventStage;
    private String eventMessage;
    private Date createTime;

    public Long getEventId() { return eventId; } public void setEventId(Long v) { eventId=v; }
    public Long getJobId() { return jobId; } public void setJobId(Long v) { jobId=v; }
    public Long getResultId() { return resultId; } public void setResultId(Long v) { resultId=v; }
    public String getEventLevel() { return eventLevel; } public void setEventLevel(String v) { eventLevel=v; }
    public String getEventStage() { return eventStage; } public void setEventStage(String v) { eventStage=v; }
    public String getEventMessage() { return eventMessage; } public void setEventMessage(String v) { eventMessage=v; }
    public Date getCreateTime() { return createTime; } public void setCreateTime(Date v) { createTime=v; }
}
