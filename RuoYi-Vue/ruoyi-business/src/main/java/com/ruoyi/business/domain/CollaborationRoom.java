package com.ruoyi.business.domain;

import java.util.Date;

/** 每个课程班级独享一份在线协作文档。 */
public class CollaborationRoom
{
    private Long roomId;
    private String provider;
    private String providerSessionKey;
    private String publicFileId;
    private Long lessonId;
    private Long questionId;
    private Long sourceMaterialId;
    private Long deptId;
    private String entryYear;
    private String classCode;
    private String roomTitle;
    private String status;
    private Integer currentVersion;
    private String currentFileName;
    private String currentFilePath;
    private String currentFileExtension;
    private String currentMimeType;
    private Long currentFileSize;
    private String currentSha256;
    private Long creatorUserId;
    private Long modifierUserId;
    private Date lastOpenTime;
    private Date lastSaveTime;
    private String lastCallbackType;
    private String lastCallbackStatus;
    private String lastWpsRequestId;
    private String lastErrorMessage;
    private Date createTime;
    private Date updateTime;

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getProviderSessionKey() { return providerSessionKey; }
    public void setProviderSessionKey(String providerSessionKey) { this.providerSessionKey = providerSessionKey; }
    public String getPublicFileId() { return publicFileId; }
    public void setPublicFileId(String publicFileId) { this.publicFileId = publicFileId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Long getSourceMaterialId() { return sourceMaterialId; }
    public void setSourceMaterialId(Long sourceMaterialId) { this.sourceMaterialId = sourceMaterialId; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
    public String getRoomTitle() { return roomTitle; }
    public void setRoomTitle(String roomTitle) { this.roomTitle = roomTitle; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(Integer currentVersion) { this.currentVersion = currentVersion; }
    public String getCurrentFileName() { return currentFileName; }
    public void setCurrentFileName(String currentFileName) { this.currentFileName = currentFileName; }
    public String getCurrentFilePath() { return currentFilePath; }
    public void setCurrentFilePath(String currentFilePath) { this.currentFilePath = currentFilePath; }
    public String getCurrentFileExtension() { return currentFileExtension; }
    public void setCurrentFileExtension(String currentFileExtension) { this.currentFileExtension = currentFileExtension; }
    public String getCurrentMimeType() { return currentMimeType; }
    public void setCurrentMimeType(String currentMimeType) { this.currentMimeType = currentMimeType; }
    public Long getCurrentFileSize() { return currentFileSize; }
    public void setCurrentFileSize(Long currentFileSize) { this.currentFileSize = currentFileSize; }
    public String getCurrentSha256() { return currentSha256; }
    public void setCurrentSha256(String currentSha256) { this.currentSha256 = currentSha256; }
    public Long getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(Long creatorUserId) { this.creatorUserId = creatorUserId; }
    public Long getModifierUserId() { return modifierUserId; }
    public void setModifierUserId(Long modifierUserId) { this.modifierUserId = modifierUserId; }
    public Date getLastOpenTime() { return lastOpenTime; }
    public void setLastOpenTime(Date lastOpenTime) { this.lastOpenTime = lastOpenTime; }
    public Date getLastSaveTime() { return lastSaveTime; }
    public void setLastSaveTime(Date lastSaveTime) { this.lastSaveTime = lastSaveTime; }
    public String getLastCallbackType() { return lastCallbackType; }
    public void setLastCallbackType(String lastCallbackType) { this.lastCallbackType = lastCallbackType; }
    public String getLastCallbackStatus() { return lastCallbackStatus; }
    public void setLastCallbackStatus(String lastCallbackStatus) { this.lastCallbackStatus = lastCallbackStatus; }
    public String getLastWpsRequestId() { return lastWpsRequestId; }
    public void setLastWpsRequestId(String lastWpsRequestId) { this.lastWpsRequestId = lastWpsRequestId; }
    public String getLastErrorMessage() { return lastErrorMessage; }
    public void setLastErrorMessage(String lastErrorMessage) { this.lastErrorMessage = lastErrorMessage; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
