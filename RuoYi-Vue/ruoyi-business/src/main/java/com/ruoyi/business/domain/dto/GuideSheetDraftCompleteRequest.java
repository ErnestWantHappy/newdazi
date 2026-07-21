package com.ruoyi.business.domain.dto;

/**
 * 完成电子导学单创作草稿的请求。
 */
public class GuideSheetDraftCompleteRequest
{
    private Long revision;

    public Long getRevision() { return revision; }
    public void setRevision(Long revision) { this.revision = revision; }
}
