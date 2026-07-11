package com.ruoyi.business.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 导学单外部能力统一由服务端配置，避免密钥和环境地址进入业务数据。
 */
@Component
@ConfigurationProperties(prefix = "guide-sheet")
public class GuideSheetProperties
{
    private final Ai ai = new Ai();
    private final TeacherHelper teacherHelper = new TeacherHelper();

    public Ai getAi()
    {
        return ai;
    }

    public TeacherHelper getTeacherHelper()
    {
        return teacherHelper;
    }

    public static class Ai
    {
        private String provider = "deepseek";
        private String apiKey = "";
        private String model = "";
        private String baseUrl = "";

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public boolean isConfigured() { return apiKey != null && !apiKey.trim().isEmpty(); }
    }

    public static class TeacherHelper
    {
        private boolean enabled;
        private int port = 5000;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
    }
}
