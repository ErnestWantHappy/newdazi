package com.ruoyi.business.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 纸质扫描功能开关与上传约束。
 * AI Key/model/baseUrl 一律复用教师个人AI配置（TeacherAiConfig），本类不保存任何密钥。
 */
@Component
@ConfigurationProperties(prefix = "paper-scan")
public class PaperScanProperties
{
    /** 总开关：关闭时扫描接口直接拒绝 */
    private boolean enabled = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
