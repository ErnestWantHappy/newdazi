package com.ruoyi.business.domain;

import java.util.Date;

/** 已接收的 MQTT 消息。 */
public class IotMessage
{
    private Long messageId;
    private Long experimentId;
    private Long groupId;
    private Long deviceId;
    private String groupCode;
    private String deviceCode;
    private String topic;
    private String payloadType;
    private String payloadText;
    private java.math.BigDecimal payloadNumber;
    private String sourceIp;
    private Integer qos;
    private Boolean retained;
    private Date receivedAt;

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long value) { messageId = value; }
    public Long getExperimentId() { return experimentId; }
    public void setExperimentId(Long value) { experimentId = value; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long value) { groupId = value; }
    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long value) { deviceId = value; }
    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String value) { groupCode = value; }
    public String getDeviceCode() { return deviceCode; }
    public void setDeviceCode(String value) { deviceCode = value; }
    public String getTopic() { return topic; }
    public void setTopic(String value) { topic = value; }
    public String getPayloadType() { return payloadType; }
    public void setPayloadType(String value) { payloadType = value; }
    public String getPayloadText() { return payloadText; }
    public void setPayloadText(String value) { payloadText = value; }
    public java.math.BigDecimal getPayloadNumber() { return payloadNumber; }
    public void setPayloadNumber(java.math.BigDecimal value) { payloadNumber = value; }
    public String getSourceIp() { return sourceIp; }
    public void setSourceIp(String value) { sourceIp = value; }
    public Integer getQos() { return qos; }
    public void setQos(Integer value) { qos = value; }
    public Boolean getRetained() { return retained; }
    public void setRetained(Boolean value) { retained = value; }
    public Date getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Date value) { receivedAt = value; }
}
