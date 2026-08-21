package com.ruoyi.business.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** MQTT 接收器与 EMQX 管理配置；凭据只允许通过部署环境注入。 */
@Component
@ConfigurationProperties(prefix = "iot.mqtt")
public class IotMqttProperties
{
    private boolean enabled;
    private String brokerUrl = "tcp://10.52.1.129:1883";
    private String username;
    private String password;
    private String subscription = "county/#";
    private String clientId = "dazi-platform-iot";
    private int maxPayloadBytes = 16000;
    private int maxMessagesPerMinute = 120;
    /** 全局上限必须低于数据库和 WebSocket 能承受的峰值，防止多 Topic 叠加绕过单 Topic 限流。 */
    private int maxMessagesPerMinuteGlobal = 5000;
    private int maxTopicLength = 256;

    // EMQX v5 管理 API 配置
    private String emqxApiUrl = "http://127.0.0.1:18083/api/v5";
    private String emqxApiToken;
    private String emqxApiKey;
    private String emqxApiSecret;

    // AES-256-GCM 口令加密密钥必须由部署环境注入，禁止使用源码默认密钥。
    private String passcodeSecret;

    // 历史 SIoT 回退配置
    private boolean siotCredentialSyncEnabled;
    private String siotDbPath;
    private int credentialTtlMinutes = 120;
    private String deviceUsername;
    private String devicePassword;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getBrokerUrl() { return brokerUrl; }
    public void setBrokerUrl(String brokerUrl) { this.brokerUrl = brokerUrl; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSubscription() { return subscription; }
    public void setSubscription(String subscription) { this.subscription = subscription; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public int getMaxPayloadBytes() { return maxPayloadBytes; }
    public void setMaxPayloadBytes(int maxPayloadBytes) { this.maxPayloadBytes = maxPayloadBytes; }

    public int getMaxMessagesPerMinute() { return maxMessagesPerMinute; }
    public void setMaxMessagesPerMinute(int maxMessagesPerMinute) { this.maxMessagesPerMinute = maxMessagesPerMinute; }

    public int getMaxMessagesPerMinuteGlobal() { return maxMessagesPerMinuteGlobal; }
    public void setMaxMessagesPerMinuteGlobal(int value) { maxMessagesPerMinuteGlobal = value; }

    public int getMaxTopicLength() { return maxTopicLength; }
    public void setMaxTopicLength(int value) { maxTopicLength = value; }

    public String getEmqxApiUrl() { return emqxApiUrl; }
    public void setEmqxApiUrl(String emqxApiUrl) { this.emqxApiUrl = emqxApiUrl; }

    public String getEmqxApiToken() { return emqxApiToken; }
    public void setEmqxApiToken(String emqxApiToken) { this.emqxApiToken = emqxApiToken; }

    public String getEmqxApiKey() { return emqxApiKey; }
    public void setEmqxApiKey(String emqxApiKey) { this.emqxApiKey = emqxApiKey; }

    public String getEmqxApiSecret() { return emqxApiSecret; }
    public void setEmqxApiSecret(String emqxApiSecret) { this.emqxApiSecret = emqxApiSecret; }

    public String getPasscodeSecret() { return passcodeSecret; }
    public void setPasscodeSecret(String passcodeSecret) { this.passcodeSecret = passcodeSecret; }

    public boolean isSiotCredentialSyncEnabled() { return siotCredentialSyncEnabled; }
    public void setSiotCredentialSyncEnabled(boolean value) { siotCredentialSyncEnabled = value; }

    public String getSiotDbPath() { return siotDbPath; }
    public void setSiotDbPath(String value) { siotDbPath = value; }

    public int getCredentialTtlMinutes() { return credentialTtlMinutes; }
    public void setCredentialTtlMinutes(int value) { credentialTtlMinutes = value; }

    public String getDeviceUsername() { return deviceUsername; }
    public void setDeviceUsername(String value) { deviceUsername = value; }

    public String getDevicePassword() { return devicePassword; }
    public void setDevicePassword(String value) { devicePassword = value; }

    /** 共享设备账号仅由部署环境注入，前端不会持有平台订阅账号。 */
    public boolean useSharedDeviceCredential()
    {
        return !isBlank(deviceUsername) && !isBlank(devicePassword);
    }

    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
}
