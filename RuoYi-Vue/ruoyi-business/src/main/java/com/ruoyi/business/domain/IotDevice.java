package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;

/** 物联网实验设备；凭据只保存哈希。 */
public class IotDevice
{
    private Long deviceId;
    private Long groupId;
    private Long experimentId;
    private String deviceCode;
    private String deviceName;
    private String topic;
    private String brokerUsername;
    private String credentialHash;
    private Date credentialExpiresAt;
    private String status;
    private Date lastSeenAt;
    private String createBy;

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long value) { deviceId = value; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long value) { groupId = value; }
    public Long getExperimentId() { return experimentId; }
    public void setExperimentId(Long value) { experimentId = value; }
    public String getDeviceCode() { return deviceCode; }
    public void setDeviceCode(String value) { deviceCode = value; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String value) { deviceName = value; }
    public String getTopic() { return topic; }
    public void setTopic(String value) { topic = value; }
    public String getBrokerUsername() { return brokerUsername; }
    public void setBrokerUsername(String value) { brokerUsername = value; }
    @JsonIgnore
    public String getCredentialHash() { return credentialHash; }
    public void setCredentialHash(String value) { credentialHash = value; }
    public Date getCredentialExpiresAt() { return credentialExpiresAt; }
    public void setCredentialExpiresAt(Date value) { credentialExpiresAt = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public Date getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Date value) { lastSeenAt = value; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String value) { createBy = value; }
}
