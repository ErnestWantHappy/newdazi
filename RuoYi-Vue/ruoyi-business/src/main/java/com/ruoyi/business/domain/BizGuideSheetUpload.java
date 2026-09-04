package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizGuideSheetUpload extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long uploadId;
    private Long answerId;
    private Long bindingId;
    private Long sourceSheetId;
    private Long studentId;
    private String questionName;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private String teacherMachineIp;
    private String storedPath;
    private String accessUrl;
    private String clientUploadId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadTime;

    public Long getUploadId() { return uploadId; }
    public void setUploadId(Long uploadId) { this.uploadId = uploadId; }
    @JsonIgnore
    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }
    @JsonIgnore
    public Long getBindingId() { return bindingId; }
    public void setBindingId(Long bindingId) { this.bindingId = bindingId; }
    @JsonIgnore
    public Long getSourceSheetId() { return sourceSheetId; }
    public void setSourceSheetId(Long sourceSheetId) { this.sourceSheetId = sourceSheetId; }
    @JsonIgnore
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getQuestionName() { return questionName; }
    public void setQuestionName(String questionName) { this.questionName = questionName; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    @JsonIgnore
    public String getTeacherMachineIp() { return teacherMachineIp; }
    public void setTeacherMachineIp(String teacherMachineIp) { this.teacherMachineIp = teacherMachineIp; }
    @JsonIgnore
    public String getStoredPath() { return storedPath; }
    public void setStoredPath(String storedPath) { this.storedPath = storedPath; }
    @JsonIgnore
    public String getAccessUrl() { return accessUrl; }
    public void setAccessUrl(String accessUrl) { this.accessUrl = accessUrl; }
    @JsonIgnore
    public String getClientUploadId() { return clientUploadId; }
    public void setClientUploadId(String clientUploadId) { this.clientUploadId = clientUploadId; }
    public Date getUploadTime() { return uploadTime; }
    public void setUploadTime(Date uploadTime) { this.uploadTime = uploadTime; }
}
