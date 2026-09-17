package com.ruoyi.business.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/** 已登录管理页面的分享状态，不返回令牌哈希。 */
public class ResearchPublicShareVo
{
    private Boolean enabled;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;
    private String shareUrl;

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
    public String getShareUrl() { return shareUrl; }
    public void setShareUrl(String shareUrl) { this.shareUrl = shareUrl; }
}
