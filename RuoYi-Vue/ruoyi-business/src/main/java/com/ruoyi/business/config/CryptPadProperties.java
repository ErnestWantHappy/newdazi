package com.ruoyi.business.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** CryptPad 集成配置，密钥和地址只能从外置环境注入。 */
@Component
@ConfigurationProperties(prefix = "collaboration.cryptpad")
public class CryptPadProperties
{
    private String baseUrl;
    private String apiUrl;
    private String keySecret;
    private String provider = "CRYPTPAD";
    private int autosaveSeconds = 15;
    private long maxFileBytes = 52428800L;
    private boolean remoteEmbedding = true;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    public String getKeySecret() { return keySecret; }
    public void setKeySecret(String keySecret) { this.keySecret = keySecret; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public int getAutosaveSeconds() { return autosaveSeconds; }
    public void setAutosaveSeconds(int autosaveSeconds) { this.autosaveSeconds = autosaveSeconds; }
    public long getMaxFileBytes() { return maxFileBytes; }
    public void setMaxFileBytes(long maxFileBytes) { this.maxFileBytes = maxFileBytes; }
    public boolean isRemoteEmbedding() { return remoteEmbedding; }
    public void setRemoteEmbedding(boolean remoteEmbedding) { this.remoteEmbedding = remoteEmbedding; }
}
