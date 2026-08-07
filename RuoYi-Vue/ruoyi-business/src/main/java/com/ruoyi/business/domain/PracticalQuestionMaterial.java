package com.ruoyi.business.domain;

import java.util.Date;

/**
 * 操作题的起始文件、补充资源或教师参考材料。
 */
public class PracticalQuestionMaterial
{
    private Long materialId;
    private Long questionId;
    private String materialType;
    private Integer fileOrder;
    private String originalFileName;
    private String resourcePath;
    private String fileExtension;
    private String mimeType;
    private Long fileSize;
    private String sha256;
    private String createBy;
    private Date createTime;

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public Integer getFileOrder() { return fileOrder; }
    public void setFileOrder(Integer fileOrder) { this.fileOrder = fileOrder; }
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
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
