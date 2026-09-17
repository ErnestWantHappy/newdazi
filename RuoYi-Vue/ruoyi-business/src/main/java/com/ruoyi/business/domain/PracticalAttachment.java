package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作题提交版本中的一个附件。
 */
public class PracticalAttachment
{
    private Long attachmentId;
    private Long versionId;
    private Integer fileOrder;
    private String fileKind;
    private String originalFileName;
    private String resourcePath;
    private String fileExtension;
    private String mimeType;
    private Long fileSize;
    private String sha256;
    private String securityStatus;
    private String previewStatus;
    private String previewPath;
    private Integer previewRetryCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date previewLastRetryTime;
    private String previewErrorMessage;
    private String normalizedStatus;
    @JsonIgnore
    private String normalizedPagesJson;
    private List<String> normalizedPages = new ArrayList<String>();
    private String rendererVersion;
    private Integer normalizedRetryCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date normalizedLastRetryTime;
    private String normalizedErrorMessage;
    private Date createTime;
    private Date updateTime;

    public Long getAttachmentId() { return attachmentId; }
    public void setAttachmentId(Long attachmentId) { this.attachmentId = attachmentId; }
    public Long getVersionId() { return versionId; }
    public void setVersionId(Long versionId) { this.versionId = versionId; }
    public Integer getFileOrder() { return fileOrder; }
    public void setFileOrder(Integer fileOrder) { this.fileOrder = fileOrder; }
    public String getFileKind() { return fileKind; }
    public void setFileKind(String fileKind) { this.fileKind = fileKind; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    public String getResourcePath() { return resourcePath; }
    public void setResourcePath(String resourcePath) { this.resourcePath = resourcePath; }
    public String getFileExtension() { return fileExtension; }
    public void setFileExtension(String fileExtension) { this.fileExtension = fileExtension; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }
    public String getSecurityStatus() { return securityStatus; }
    public void setSecurityStatus(String securityStatus) { this.securityStatus = securityStatus; }
    public String getPreviewStatus() { return previewStatus; }
    public void setPreviewStatus(String previewStatus) { this.previewStatus = previewStatus; }
    public String getPreviewPath() { return previewPath; }
    public void setPreviewPath(String previewPath) { this.previewPath = previewPath; }
    public Integer getPreviewRetryCount() { return previewRetryCount; }
    public void setPreviewRetryCount(Integer previewRetryCount) { this.previewRetryCount = previewRetryCount; }
    public Date getPreviewLastRetryTime() { return previewLastRetryTime; }
    public void setPreviewLastRetryTime(Date previewLastRetryTime) { this.previewLastRetryTime = previewLastRetryTime; }
    public String getPreviewErrorMessage() { return previewErrorMessage; }
    public void setPreviewErrorMessage(String previewErrorMessage) { this.previewErrorMessage = previewErrorMessage; }
    public String getNormalizedStatus() { return normalizedStatus; }
    public void setNormalizedStatus(String normalizedStatus) { this.normalizedStatus = normalizedStatus; }
    public String getNormalizedPagesJson() { return normalizedPagesJson; }
    public void setNormalizedPagesJson(String normalizedPagesJson) { this.normalizedPagesJson = normalizedPagesJson; }
    public List<String> getNormalizedPages() { return normalizedPages; }
    public void setNormalizedPages(List<String> normalizedPages) { this.normalizedPages = normalizedPages; }
    public String getRendererVersion() { return rendererVersion; }
    public void setRendererVersion(String rendererVersion) { this.rendererVersion = rendererVersion; }
    public Integer getNormalizedRetryCount() { return normalizedRetryCount; }
    public void setNormalizedRetryCount(Integer normalizedRetryCount) { this.normalizedRetryCount = normalizedRetryCount; }
    public Date getNormalizedLastRetryTime() { return normalizedLastRetryTime; }
    public void setNormalizedLastRetryTime(Date normalizedLastRetryTime) { this.normalizedLastRetryTime = normalizedLastRetryTime; }
    public String getNormalizedErrorMessage() { return normalizedErrorMessage; }
    public void setNormalizedErrorMessage(String normalizedErrorMessage) { this.normalizedErrorMessage = normalizedErrorMessage; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
