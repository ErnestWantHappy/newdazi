package com.ruoyi.business.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** WPS WebOffice 凭据只允许从外置配置注入。 */
@Component
@ConfigurationProperties(prefix = "collaboration.wps")
public class WpsWebOfficeProperties
{
    private String appId;
    private String appSecret;
    private String publicBaseUrl;
    private String sdkUrl;
    private String endpoint = "https://o.wpsgo.com";
    private String tokenSecret;
    private long tokenMinutes = 120L;
    private long testMaxFileBytes = 5L * 1024L * 1024L;

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
    public String getPublicBaseUrl() { return publicBaseUrl; }
    public void setPublicBaseUrl(String publicBaseUrl) { this.publicBaseUrl = publicBaseUrl; }
    public String getSdkUrl() { return sdkUrl; }
    public void setSdkUrl(String sdkUrl) { this.sdkUrl = sdkUrl; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getTokenSecret() { return tokenSecret; }
    public void setTokenSecret(String tokenSecret) { this.tokenSecret = tokenSecret; }
    public long getTokenMinutes() { return tokenMinutes; }
    public void setTokenMinutes(long tokenMinutes) { this.tokenMinutes = tokenMinutes; }
    public long getTestMaxFileBytes() { return testMaxFileBytes; }
    public void setTestMaxFileBytes(long testMaxFileBytes) { this.testMaxFileBytes = testMaxFileBytes; }
}

