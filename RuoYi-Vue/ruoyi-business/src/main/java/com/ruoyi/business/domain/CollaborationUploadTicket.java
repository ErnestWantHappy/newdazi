package com.ruoyi.business.domain;

import java.util.Date;

/** WPS 三阶段保存使用的一次性上传票据。 */
public class CollaborationUploadTicket
{
    private Long ticketId;
    private String ticketToken;
    private Long roomId;
    private Integer expectedVersion;
    private String expectedFileName;
    private Long expectedFileSize;
    private String expectedDigestType;
    private String expectedDigest;
    private Boolean manualSave;
    private String tempFilePath;
    private String status;
    private Long uploadedFileSize;
    private String uploadedSha256;
    private Long requesterUserId;
    private Date expiresTime;
    private Date completedTime;
    private String errorMessage;
    private Date createTime;
    private Date updateTime;

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public String getTicketToken() { return ticketToken; }
    public void setTicketToken(String ticketToken) { this.ticketToken = ticketToken; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public Integer getExpectedVersion() { return expectedVersion; }
    public void setExpectedVersion(Integer expectedVersion) { this.expectedVersion = expectedVersion; }
    public String getExpectedFileName() { return expectedFileName; }
    public void setExpectedFileName(String expectedFileName) { this.expectedFileName = expectedFileName; }
    public Long getExpectedFileSize() { return expectedFileSize; }
    public void setExpectedFileSize(Long expectedFileSize) { this.expectedFileSize = expectedFileSize; }
    public String getExpectedDigestType() { return expectedDigestType; }
    public void setExpectedDigestType(String expectedDigestType) { this.expectedDigestType = expectedDigestType; }
    public String getExpectedDigest() { return expectedDigest; }
    public void setExpectedDigest(String expectedDigest) { this.expectedDigest = expectedDigest; }
    public Boolean getManualSave() { return manualSave; }
    public void setManualSave(Boolean manualSave) { this.manualSave = manualSave; }
    public String getTempFilePath() { return tempFilePath; }
    public void setTempFilePath(String tempFilePath) { this.tempFilePath = tempFilePath; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getUploadedFileSize() { return uploadedFileSize; }
    public void setUploadedFileSize(Long uploadedFileSize) { this.uploadedFileSize = uploadedFileSize; }
    public String getUploadedSha256() { return uploadedSha256; }
    public void setUploadedSha256(String uploadedSha256) { this.uploadedSha256 = uploadedSha256; }
    public Long getRequesterUserId() { return requesterUserId; }
    public void setRequesterUserId(Long requesterUserId) { this.requesterUserId = requesterUserId; }
    public Date getExpiresTime() { return expiresTime; }
    public void setExpiresTime(Date expiresTime) { this.expiresTime = expiresTime; }
    public Date getCompletedTime() { return completedTime; }
    public void setCompletedTime(Date completedTime) { this.completedTime = completedTime; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}

