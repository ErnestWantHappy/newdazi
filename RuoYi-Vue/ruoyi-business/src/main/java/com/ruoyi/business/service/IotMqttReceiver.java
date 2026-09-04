package com.ruoyi.business.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.business.config.IotMqttProperties;
import com.ruoyi.business.domain.IotDevice;
import com.ruoyi.business.domain.IotEvent;
import com.ruoyi.business.domain.IotGroup;
import com.ruoyi.business.domain.IotMessage;
import com.ruoyi.business.mapper.IotMapper;
import com.ruoyi.business.config.IotWebSocketHandler;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 平台服务端 MQTT 接收器。
 * 支持标准 EMQX 与 SIoT，按服务端 Topic 自动映射班级小组与实验数据，并记录分层诊断。
 */
@Service
public class IotMqttReceiver
{
    private static final Logger log = LoggerFactory.getLogger(IotMqttReceiver.class);
    private final AtomicReference<MqttClient> client = new AtomicReference<>();
    private final Map<String, AtomicInteger> rate = new ConcurrentHashMap<>();

    @Autowired private IotMqttProperties properties;
    @Autowired private IotMapper mapper;
    @Autowired private IotWebSocketHandler websocketHandler;

    /**
     * Broker 不可达时不能阻塞 Spring 主线程；连接失败仍由接收器记录诊断事件。
     */
    @Async("threadPoolTaskExecutor")
    public void startIfEnabled()
    {
        if (!properties.isEnabled())
        {
            log.info("物联网 MQTT 接收器未开启");
            return;
        }
        try
        {
            MqttClient mqtt = new MqttClient(properties.getBrokerUrl(), properties.getClientId());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(false);
            options.setConnectionTimeout(10);
            if (properties.getUsername() != null && !properties.getUsername().trim().isEmpty())
            {
                options.setUserName(properties.getUsername());
                options.setPassword(properties.getPassword() == null ? new char[0] : properties.getPassword().toCharArray());
            }
            mqtt.setCallback(new Callback());
            mqtt.connect(options);
            mqtt.subscribe(properties.getSubscription(), new Listener());
            client.set(mqtt);
            log.info("物联网 MQTT 接收器已连接 broker={} subscription={}", properties.getBrokerUrl(), properties.getSubscription());
        }
        catch (Exception e)
        {
            record(null, null, null, "BROKER_CONNECT_FAILED", "MQTT认证", "平台连接 Broker 失败");
            log.error("物联网 MQTT 接收器连接失败 broker={}", properties.getBrokerUrl(), e);
        }
    }

    public void stop()
    {
        MqttClient mqtt = client.getAndSet(null);
        if (mqtt == null) return;
        try { mqtt.disconnect(); mqtt.close(); } catch (MqttException e) { log.warn("物联网 MQTT 接收器关闭失败"); }
    }

    public boolean isConnected()
    {
        MqttClient mqtt = client.get();
        return mqtt != null && mqtt.isConnected();
    }

    private final class Callback implements MqttCallbackExtended
    {
        @Override public void connectComplete(boolean reconnect, String serverURI)
        {
            if (!reconnect) return;
            // paho 自动重连不会恢复订阅；会话未过期时重复订阅是幂等的，
            // 会话过期（broker 侧 expiry_interval 2 小时）后不重订阅将静默收不到任何数据。
            try
            {
                MqttClient mqtt = client.get();
                if (mqtt != null && mqtt.isConnected())
                {
                    mqtt.subscribe(properties.getSubscription(), new Listener());
                    record(null, null, null, "BROKER_RECONNECTED", "MQTT认证", "重连成功，已恢复订阅 " + properties.getSubscription());
                    log.info("物联网 MQTT 重连成功并恢复订阅 broker={} subscription={}", serverURI, properties.getSubscription());
                }
            }
            catch (Exception e)
            {
                record(null, null, null, "RESUBSCRIBE_FAILED", "MQTT认证", "重连后恢复订阅失败: " + abbreviate(String.valueOf(e), 200));
                log.error("物联网 MQTT 重连后恢复订阅失败", e);
            }
        }
        @Override public void connectionLost(Throwable cause)
        {
            String reason = cause == null ? "未知原因" : String.valueOf(cause);
            record(null, null, null, "BROKER_CONNECTION_LOST", "网络未到达", "平台与 Broker 连接中断: " + abbreviate(reason, 200));
            log.warn("物联网 MQTT 连接中断，将由客户端自动重连 cause={}", reason);
        }
        @Override public void messageArrived(String topic, MqttMessage message) { }
        @Override public void deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken token) { }
    }

    private final class Listener implements IMqttMessageListener
    {
        @Override public void messageArrived(String topic, MqttMessage mqttMessage)
        {
            receive(topic, mqttMessage);
        }
    }

    /** 接收并存档单条消息，便于模拟器和真实 Broker 共用同一条业务链。 */
    public void receive(String topic, MqttMessage mqttMessage)
    {
        if (topic == null || mqttMessage == null)
        {
            record(null, null, null, "INVALID_MESSAGE", "消息格式", "消息为空");
            return;
        }
        try
        {
            receiveInternal(topic, mqttMessage);
        }
        catch (Exception e)
        {
            // paho 回调线程中抛出任何异常都会导致客户端断连并重连，
            // 单条消息的处理失败必须就地隔离，只记诊断事件，绝不向上传播。
            log.error("物联网消息处理失败 topic={}", topic, e);
            try
            {
                record(null, null, null, "MESSAGE_PROCESS_FAILED", "平台接收",
                        "消息处理失败: " + e.getClass().getSimpleName() + " " + abbreviate(String.valueOf(e.getMessage()), 200));
            }
            catch (Exception ignored) { }
        }
    }

    private void receiveInternal(String topic, MqttMessage mqttMessage)
    {
        if (!isValidTopic(topic, properties.getMaxTopicLength()))
        {
            record(null, null, null, "INVALID_TOPIC", "Topic", "Topic 格式不符合平台约束");
            return;
        }
        byte[] bytes = mqttMessage.getPayload();
        if (bytes.length == 0 || bytes.length > properties.getMaxPayloadBytes())
        {
            record(null, null, null, "PAYLOAD_TOO_LARGE", "消息格式", "消息大小不符合限制");
            return;
        }
        if (!allowRate(topic))
        {
            record(null, null, null, "RATE_LIMITED", "平台接收", "消息超过接收频率限制");
            return;
        }

        Long experimentId = null;
        Long groupId = null;
        Long deviceId = 0L;

        IotDevice device = mapper.selectDeviceByTopic(topic);
        if (device != null)
        {
            experimentId = device.getExperimentId();
            groupId = device.getGroupId();
            deviceId = device.getDeviceId();
        }
        else
        {
            IotGroup group = mapper.selectGroupByTopic(topic);
            if (group != null)
            {
                experimentId = group.getExperimentId();
                groupId = group.getGroupId();
            }
        }

        if (experimentId == null || groupId == null)
        {
            record(null, null, null, "TOPIC_NOT_MAPPED", "Topic", "Topic 未映射到启用小组或设备: " + topic);
            return;
        }

        String payload = new String(bytes, StandardCharsets.UTF_8);
        String payloadType = "TEXT";
        BigDecimal number = null;
        try
        {
            if (payload.trim().startsWith("{"))
            {
                JSONObject object = JSON.parseObject(payload);
                object.toJSONString();
                payloadType = "JSON";
            }
            else
            {
                number = new BigDecimal(payload.trim());
                payloadType = "NUMBER";
            }
        }
        catch (Exception ignored) { }

        IotMessage stored = new IotMessage();
        stored.setExperimentId(experimentId);
        stored.setGroupId(groupId);
        stored.setDeviceId(deviceId);
        stored.setTopic(topic);
        stored.setPayloadType(payloadType);
        stored.setPayloadText(payload);
        stored.setPayloadNumber(number);
        stored.setQos(mqttMessage.getQos());
        stored.setRetained(mqttMessage.isRetained());
        stored.setReceivedAt(new Date());

        mapper.insertMessage(stored);
        if (deviceId != null && deviceId > 0)
        {
            mapper.touchDevice(deviceId, stored.getReceivedAt());
        }
        mapper.touchGroup(groupId, stored.getReceivedAt());

        record(experimentId, groupId, deviceId, "MESSAGE_RECEIVED", "平台接收", "消息已存档");
        websocketHandler.publishRefresh(experimentId);
    }

    private boolean allowRate(String topic)
    {
        long bucket = System.currentTimeMillis() / 60000L;
        String globalKey = bucket + ":__global__";
        AtomicInteger globalCount = rate.computeIfAbsent(globalKey, ignored -> new AtomicInteger());
        if (globalCount.incrementAndGet() > Math.max(1, properties.getMaxMessagesPerMinuteGlobal())) return false;
        String key = bucket + ":" + topic;
        AtomicInteger count = rate.computeIfAbsent(key, ignored -> new AtomicInteger());
        if (count.incrementAndGet() > Math.max(1, properties.getMaxMessagesPerMinute())) return false;
        if (rate.size() > 2048) rate.entrySet().removeIf(entry -> !entry.getKey().startsWith(bucket + ":"));
        return true;
    }

    /** MQTT Broker 不会把通配符作为真实发布 Topic 传给订阅者，平台也拒绝控制字符和过长 Topic。 */
    static boolean isValidTopic(String topic, int maxLength)
    {
        if (topic == null || topic.isEmpty() || topic.length() > Math.max(1, maxLength)) return false;
        if (topic.indexOf('#') >= 0 || topic.indexOf('+') >= 0) return false;
        for (int i = 0; i < topic.length(); i++)
        {
            if (Character.isISOControl(topic.charAt(i))) return false;
        }
        return true;
    }

    private void record(Long experimentId, Long groupId, Long deviceId, String type, String stage, String detail)
    {
        IotEvent event = new IotEvent();
        event.setExperimentId(experimentId);
        event.setGroupId(groupId);
        event.setDeviceId(deviceId);
        event.setEventType(type);
        event.setDiagnosticStage(stage);
        event.setDetail(detail);
        event.setOccurredAt(new Date());
        mapper.insertEvent(event);
    }

    /** 截断诊断文本，防止超长异常信息撑破事件明细列。 */
    private static String abbreviate(String value, int max)
    {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }

    public String digest(String value)
    {
        try
        {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) result.append(String.format("%02x", b));
            return result.toString();
        }
        catch (Exception e) { return null; }
    }
}
