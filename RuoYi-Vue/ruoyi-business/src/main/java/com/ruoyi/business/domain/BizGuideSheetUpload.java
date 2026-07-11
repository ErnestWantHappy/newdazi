package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class BizGuideSheetUpload extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long uploadId;
    private Long answerId;
    private Long sheetId;
    private Long studentId;
    private String questionName;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private String teacherMachineIp;
    private String storedPath;
    private String accessUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadTime;

    public Long getUploadId() {
        return uploadId;
    }

    public void setUploadId(Long uploadId) {
        this.uploadId = uploadId;
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public Long getSheetId() {
        return sheetId;
    }

    public void setSheetId(Long sheetId) {
        this.sheetId = sheetId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getQuestionName() {
        return questionName;
    }

    public void setQuestionName(String questionName) {
        this.questionName = questionName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getTeacherMachineIp() {
        return teacherMachineIp;
    }

    public void setTeacherMachineIp(String teacherMachineIp) {
        this.teacherMachineIp = teacherMachineIp;
    }

    public String getStoredPath() {
        return storedPath;
    }

    public void setStoredPath(String storedPath) {
        this.storedPath = storedPath;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("uploadId", getUploadId())
                .append("sheetId", getSheetId())
                .append("studentId", getStudentId())
                .append("questionName", getQuestionName())
                .append("fileName", getFileName())
                .append("fileSize", getFileSize())
                .append("mimeType", getMimeType())
                .append("teacherMachineIp", getTeacherMachineIp())
                .append("accessUrl", getAccessUrl())
                .append("uploadTime", getUploadTime())
                .toString();
    }
}
