package com.ruoyi.business.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 教师端电子导学单创作草稿。
 */
public class BizGuideSheetDraft extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long draftId;
    private Long ownerId;
    private String clientDraftKey;
    private Long sheetId;
    private String contentJson;
    private Long revision;
    private String draftStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completedTime;

    public Long getDraftId() { return draftId; }
    public void setDraftId(Long draftId) { this.draftId = draftId; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getClientDraftKey() { return clientDraftKey; }
    public void setClientDraftKey(String clientDraftKey) { this.clientDraftKey = clientDraftKey; }
    public Long getSheetId() { return sheetId; }
    public void setSheetId(Long sheetId) { this.sheetId = sheetId; }
    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }
    public Long getRevision() { return revision; }
    public void setRevision(Long revision) { this.revision = revision; }
    public String getDraftStatus() { return draftStatus; }
    public void setDraftStatus(String draftStatus) { this.draftStatus = draftStatus; }
    public Date getCompletedTime() { return completedTime; }
    public void setCompletedTime(Date completedTime) { this.completedTime = completedTime; }
}
