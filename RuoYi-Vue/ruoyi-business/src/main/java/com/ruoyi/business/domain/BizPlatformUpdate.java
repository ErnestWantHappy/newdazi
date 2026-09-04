package com.ruoyi.business.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

/**
 * 面向平台用户展示的发布记录；不复用系统操作日志，避免将技术细节暴露给教师。
 */
public class BizPlatformUpdate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long updateId;
    private String versionNo;
    private String title;
    private String content;
    private Date publishedAt;
    /** DRAFT 草稿、PUBLISHED 已发布、WITHDRAWN 已撤回。 */
    private String status;

    public Long getUpdateId() { return updateId; }
    public void setUpdateId(Long updateId) { this.updateId = updateId; }
    public String getVersionNo() { return versionNo; }
    public void setVersionNo(String versionNo) { this.versionNo = versionNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Date getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Date publishedAt) { this.publishedAt = publishedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
