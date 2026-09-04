package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 系统性能事件 sys_perf_event
 */
public class SysPerfEvent
{
    private Long eventId;

    private String eventType;

    private String severity;

    /** 动态诊断分类，不落库，用于区分业务提示、性能关注和系统故障。 */
    private String category;

    /** 动态处置建议，不落库。 */
    private String advice;

    private String title;

    private String description;

    private String sourceName;

    private String sourceUrl;

    private String sqlText;

    private String sqlHash;

    private Long durationMs;

    private String errorMsg;

    private String operName;

    private String deptName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date occurTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getEventId()
    {
        return eventId;
    }

    public void setEventId(Long eventId)
    {
        this.eventId = eventId;
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public String getSeverity()
    {
        return severity;
    }

    public void setSeverity(String severity)
    {
        this.severity = severity;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getAdvice()
    {
        return advice;
    }

    public void setAdvice(String advice)
    {
        this.advice = advice;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public void setSourceName(String sourceName)
    {
        this.sourceName = sourceName;
    }

    public String getSourceUrl()
    {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl)
    {
        this.sourceUrl = sourceUrl;
    }

    public String getSqlText()
    {
        return sqlText;
    }

    public void setSqlText(String sqlText)
    {
        this.sqlText = sqlText;
    }

    public String getSqlHash()
    {
        return sqlHash;
    }

    public void setSqlHash(String sqlHash)
    {
        this.sqlHash = sqlHash;
    }

    public Long getDurationMs()
    {
        return durationMs;
    }

    public void setDurationMs(Long durationMs)
    {
        this.durationMs = durationMs;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public String getOperName()
    {
        return operName;
    }

    public void setOperName(String operName)
    {
        this.operName = operName;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public Date getOccurTime()
    {
        return occurTime;
    }

    public void setOccurTime(Date occurTime)
    {
        this.occurTime = occurTime;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
