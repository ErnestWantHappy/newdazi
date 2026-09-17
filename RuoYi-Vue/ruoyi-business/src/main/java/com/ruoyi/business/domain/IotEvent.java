package com.ruoyi.business.domain;

import java.util.Date;

/** 物联网诊断和异常事件。 */
public class IotEvent
{
    private Long eventId;
    private Long experimentId;
    private Long groupId;
    private Long deviceId;
    private String eventType;
    private String diagnosticStage;
    private String detail;
    private String payloadDigest;
    private Date occurredAt;

    public Long getEventId() { return eventId; }
    public void setEventId(Long value) { eventId = value; }
    public Long getExperimentId() { return experimentId; }
    public void setExperimentId(Long value) { experimentId = value; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long value) { groupId = value; }
    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long value) { deviceId = value; }
    public String getEventType() { return eventType; }
    public void setEventType(String value) { eventType = value; }
    public String getDiagnosticStage() { return diagnosticStage; }
    public void setDiagnosticStage(String value) { diagnosticStage = value; }
    public String getDetail() { return detail; }
    public void setDetail(String value) { detail = value; }
    public String getPayloadDigest() { return payloadDigest; }
    public void setPayloadDigest(String value) { payloadDigest = value; }
    public Date getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Date value) { occurredAt = value; }
}
