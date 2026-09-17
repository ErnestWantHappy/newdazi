package com.ruoyi.business.domain.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 保存电子导学单创作草稿的请求。
 */
public class GuideSheetDraftSaveRequest
{
    private String draftKey;
    private Long sheetId;
    private Long revision;
    private JsonNode content;

    public String getDraftKey() { return draftKey; }
    public void setDraftKey(String draftKey) { this.draftKey = draftKey; }
    public Long getSheetId() { return sheetId; }
    public void setSheetId(Long sheetId) { this.sheetId = sheetId; }
    public Long getRevision() { return revision; }
    public void setRevision(Long revision) { this.revision = revision; }
    public JsonNode getContent() { return content; }
    public void setContent(JsonNode content) { this.content = content; }
}
