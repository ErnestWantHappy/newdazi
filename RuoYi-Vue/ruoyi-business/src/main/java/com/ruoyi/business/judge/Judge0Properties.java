package com.ruoyi.business.judge;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 全部运行参数可由服务器环境变量覆盖，源码不保存 Judge0 地址之外的凭据。 */
@Component
public class Judge0Properties {
    @Value("${judge0.mode:disabled}") private String mode;
    @Value("${judge0.base-url:}") private String baseUrl;
    @Value("${judge0.auth-header:X-Judge0-Token}") private String authHeader;
    @Value("${judge0.auth-token:}") private String authToken;
    @Value("${judge0.python-language-id:71}") private Integer pythonLanguageId;
    @Value("${judge0.connect-timeout-ms:2000}") private Integer connectTimeoutMs;
    @Value("${judge0.read-timeout-ms:4000}") private Integer readTimeoutMs;
    @Value("${judge0.poll-interval-ms:500}") private Integer pollIntervalMs;
    @Value("${judge0.max-polls:24}") private Integer maxPolls;
    @Value("${judge0.student-submits-per-minute:10}") private Integer studentSubmitsPerMinute;
    @Value("${judge0.class-concurrency:8}") private Integer classConcurrency;
    @Value("${judge0.max-source-bytes:65536}") private Integer maxSourceBytes;
    @Value("${judge0.recovery-timeout-seconds:180}") private Integer recoveryTimeoutSeconds;
    public String getMode() { return mode; } public String getBaseUrl() { return baseUrl; } public String getAuthHeader() { return authHeader; } public String getAuthToken() { return authToken; } public Integer getPythonLanguageId() { return pythonLanguageId; }
    public Integer getConnectTimeoutMs() { return connectTimeoutMs; } public Integer getReadTimeoutMs() { return readTimeoutMs; } public Integer getPollIntervalMs() { return pollIntervalMs; }
    public Integer getMaxPolls() { return maxPolls; } public Integer getStudentSubmitsPerMinute() { return studentSubmitsPerMinute; } public Integer getClassConcurrency() { return classConcurrency; } public Integer getMaxSourceBytes() { return maxSourceBytes; } public Integer getRecoveryTimeoutSeconds() { return recoveryTimeoutSeconds; }
}
