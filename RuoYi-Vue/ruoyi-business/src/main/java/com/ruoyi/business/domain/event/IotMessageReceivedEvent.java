package com.ruoyi.business.domain.event;

import java.util.Date;

/**
 * 平台收到一条设备上行消息后发布的领域事件。
 * 用途：把「接收与落库」和「AI 判定与下行下发」解耦，避免接收器与下行服务相互注入形成循环依赖。
 */
public class IotMessageReceivedEvent
{
    private final Long experimentId;
    private final Long groupId;
    private final Long deviceId;
    private final String topic;
    private final String payloadType;
    private final String payloadText;
    private final Date receivedAt;

    public IotMessageReceivedEvent(Long experimentId, Long groupId, Long deviceId,
            String topic, String payloadType, String payloadText, Date receivedAt)
    {
        this.experimentId = experimentId;
        this.groupId = groupId;
        this.deviceId = deviceId;
        this.topic = topic;
        this.payloadType = payloadType;
        this.payloadText = payloadText;
        this.receivedAt = receivedAt;
    }

    public Long getExperimentId() { return experimentId; }
    public Long getGroupId() { return groupId; }
    public Long getDeviceId() { return deviceId; }
    public String getTopic() { return topic; }
    public String getPayloadType() { return payloadType; }
    public String getPayloadText() { return payloadText; }
    public Date getReceivedAt() { return receivedAt; }
}
