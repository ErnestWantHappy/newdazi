package com.ruoyi.business.service;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ruoyi.business.domain.IotDevice;
import com.ruoyi.business.mapper.IotMapper;
import com.ruoyi.business.config.IotMqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 到期设备统一失效，避免短期凭据在 SIoT 中无限有效。 */
@Component
public class IotCredentialExpiryJob
{
    private static final Logger log = LoggerFactory.getLogger(IotCredentialExpiryJob.class);
    @Autowired private IotMapper mapper;
    @Autowired private IotSiotCredentialAdapter adapter;
    @Autowired private IotMqttProperties properties;

    @Scheduled(fixedDelayString = "${IOT_CREDENTIAL_EXPIRY_INTERVAL_MS:60000}")
    public void expire()
    {
        // IoT 表和 SIoT 凭据只在功能正式启用后存在；关闭时必须跳过定时查询，避免未迁移环境持续报错。
        if (!properties.isEnabled()) return;
        List<IotDevice> devices = mapper.selectExpiredDevices(new Date());
        for (IotDevice device : devices)
        {
            mapper.updateDeviceStatus(device.getDeviceId(), "1");
            try { adapter.revoke(device.getBrokerUsername()); }
            catch (RuntimeException e) { log.warn("物联网设备凭据失效同步失败 deviceId={}", device.getDeviceId()); }
        }
    }
}
