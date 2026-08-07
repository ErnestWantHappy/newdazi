package com.ruoyi.business.domain.dto;

import java.io.Serializable;

/**
 * 临时上传凭证只保存在 Redis，用于把文件绑定到上传学生和具体题目。
 */
public class PracticalUploadTicket implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String token;
    private Long studentId;
    private Long lessonId;
    private Long questionId;
    private String originalFileName;
    private String resourcePath;
    private String fileExtension;
    private String fileKind;
    private String mimeType;
    private Long fileSize;
    private String sha256;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    public String getResourcePath() { return resourcePath; }
    public void setResourcePath(String resourcePath) { this.resourcePath = resourcePath; }
    public String getFileExtension() { return fileExtension; }
    public void setFileExtension(String fileExtension) { this.fileExtension = fileExtension; }
    public String getFileKind() { return fileKind; }
    public void setFileKind(String fileKind) { this.fileKind = fileKind; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }
}
