package com.ruoyi.business.domain.vo;

import java.util.Date;

/** 临时设备凭据，只在生成成功时返回明文 secret。 */
public class IotCredentialVo
{
    private Long deviceId;
    private String username;
    private String secret;
    private Date expiresAt;
    private String brokerMode;
    private String brokerUrl;
    private String topic;

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long value) { deviceId = value; }
    public String getUsername() { return username; }
    public void setUsername(String value) { username = value; }
    public String getSecret() { return secret; }
    public void setSecret(String value) { secret = value; }
    public Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Date value) { expiresAt = value; }
    public String getBrokerMode() { return brokerMode; }
    public void setBrokerMode(String value) { brokerMode = value; }
    public String getBrokerUrl() { return brokerUrl; }
    public void setBrokerUrl(String value) { brokerUrl = value; }
    public String getTopic() { return topic; }
    public void setTopic(String value) { topic = value; }
}
