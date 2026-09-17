package com.ruoyi.business.config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import com.ruoyi.business.service.IotMqttReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 应用启动时按外置开关连接 MQTT，停止时释放客户端。 */
@Component
public class IotMqttLifecycle
{
    @Autowired private IotMqttReceiver receiver;

    @PostConstruct
    public void start() { receiver.startIfEnabled(); }

    @PreDestroy
    public void stop() { receiver.stop(); }
}
