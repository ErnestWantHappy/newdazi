package com.ruoyi.business.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import com.ruoyi.business.config.IotMqttProperties;
import com.ruoyi.business.config.IotWebSocketHandler;
import com.ruoyi.business.domain.IotDevice;
import com.ruoyi.business.domain.IotGroup;
import com.ruoyi.business.mapper.IotMapper;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IotMqttReceiverTest
{
    @Test
    void shouldArchiveJsonMessageAndNotifyOnlyExperimentForDeviceTopic()
    {
        IotMapper mapper = mock(IotMapper.class);
        IotWebSocketHandler socket = mock(IotWebSocketHandler.class);
        IotDevice device = device();
        when(mapper.selectDeviceByTopic("county/10/20/2024-01/light/group01/data")).thenReturn(device);
        IotMqttReceiver receiver = receiver(mapper, socket, 10);
        MqttMessage message = new MqttMessage("{\"light\":42}".getBytes(StandardCharsets.UTF_8));
        message.setQos(1);

        receiver.receive("county/10/20/2024-01/light/group01/data", message);

        ArgumentCaptor<com.ruoyi.business.domain.IotMessage> messageCaptor = ArgumentCaptor.forClass(com.ruoyi.business.domain.IotMessage.class);
        verify(mapper).insertMessage(messageCaptor.capture());
        assertEquals("JSON", messageCaptor.getValue().getPayloadType());
        assertEquals(Long.valueOf(9L), messageCaptor.getValue().getExperimentId());
        verify(mapper).touchDevice(eq(3L), any(Date.class));
        verify(mapper).touchGroup(eq(5L), any(Date.class));
        verify(socket).publishRefresh(9L);
    }

    @Test
    void shouldArchiveMessageForGroupTopicDirectly()
    {
        IotMapper mapper = mock(IotMapper.class);
        IotWebSocketHandler socket = mock(IotWebSocketHandler.class);
        IotGroup group = new IotGroup();
        group.setGroupId(12L);
        group.setExperimentId(9L);
        when(mapper.selectGroupByTopic("county/169/270/2024-01/exp01/group01/data")).thenReturn(group);

        IotMqttReceiver receiver = receiver(mapper, socket, 10);
        MqttMessage message = new MqttMessage("23.5".getBytes(StandardCharsets.UTF_8));

        receiver.receive("county/169/270/2024-01/exp01/group01/data", message);

        ArgumentCaptor<com.ruoyi.business.domain.IotMessage> messageCaptor = ArgumentCaptor.forClass(com.ruoyi.business.domain.IotMessage.class);
        verify(mapper).insertMessage(messageCaptor.capture());
        assertEquals("NUMBER", messageCaptor.getValue().getPayloadType());
        assertEquals(Long.valueOf(9L), messageCaptor.getValue().getExperimentId());
        assertEquals(Long.valueOf(12L), messageCaptor.getValue().getGroupId());
        verify(mapper).touchGroup(eq(12L), any(Date.class));
        verify(socket).publishRefresh(9L);
    }

    @Test
    void shouldRejectUnknownTopicBeforeSavingPayload()
    {
        IotMapper mapper = mock(IotMapper.class);
        IotMqttReceiver receiver = receiver(mapper, mock(IotWebSocketHandler.class), 10);

        receiver.receive("county/unknown", new MqttMessage("9".getBytes(StandardCharsets.UTF_8)));

        verify(mapper, never()).insertMessage(any());
        verify(mapper).insertEvent(any());
    }

    @Test
    void shouldRateLimitTopic()
    {
        IotMapper mapper = mock(IotMapper.class);
        IotMqttReceiver receiver = receiver(mapper, mock(IotWebSocketHandler.class), 1);
        String topic = "county/10/20/2024-01/light/group01/data";
        when(mapper.selectDeviceByTopic(topic)).thenReturn(device());

        receiver.receive(topic, new MqttMessage("7".getBytes(StandardCharsets.UTF_8)));
        receiver.receive(topic, new MqttMessage("8".getBytes(StandardCharsets.UTF_8)));

        verify(mapper).insertMessage(any());
        verify(mapper, org.mockito.Mockito.times(2)).insertEvent(any());
    }

    private IotMqttReceiver receiver(IotMapper mapper, IotWebSocketHandler socket, int rate)
    {
        IotMqttProperties properties = new IotMqttProperties();
        properties.setMaxMessagesPerMinute(rate);
        IotMqttReceiver receiver = new IotMqttReceiver();
        ReflectionTestUtils.setField(receiver, "properties", properties);
        ReflectionTestUtils.setField(receiver, "mapper", mapper);
        ReflectionTestUtils.setField(receiver, "websocketHandler", socket);
        return receiver;
    }

    private IotDevice device()
    {
        IotDevice device = new IotDevice();
        device.setDeviceId(3L);
        device.setGroupId(5L);
        device.setExperimentId(9L);
        return device;
    }
}
