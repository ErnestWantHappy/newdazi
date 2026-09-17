package com.ruoyi.business.domain.dto;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;

/** 课程资源中的一条结构化云盘链接。 */
public class ResearchResourceLinkRequest
{
    @NotBlank(message = "请输入资源名称")
    @Size(max = 255, message = "资源名称不能超过255个字符")
    private String resourceName;
    @NotBlank(message = "请输入资源链接")
    @Size(max = 1000, message = "资源链接不能超过1000个字符")
    private String linkUrl;
    @Size(max = 64, message = "提取码不能超过64个字符")
    private String extractCode;
    private Boolean permanent = Boolean.TRUE;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;
    @Size(max = 500, message = "资源说明不能超过500个字符")
    private String description;

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public String getExtractCode() { return extractCode; }
    public void setExtractCode(String extractCode) { this.extractCode = extractCode; }
    public Boolean getPermanent() { return permanent; }
    public void setPermanent(Boolean permanent) { this.permanent = permanent; }
    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
