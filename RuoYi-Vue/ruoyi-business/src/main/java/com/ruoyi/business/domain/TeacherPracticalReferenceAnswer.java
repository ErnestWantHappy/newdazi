package com.ruoyi.business.domain;

import java.util.Date;

/** 教师在某门课程、某道操作题下当前使用的 AI 参考答案。 */
public class TeacherPracticalReferenceAnswer
{
    private Long referenceId;
    private Long teacherUserId;
    private Long deptId;
    private Long lessonId;
    private Long questionId;
    private String originalFileName;
    private String resourcePath;
    private String fileExtension;
    private String mimeType;
    private Long fileSize;
    private String sha256;
    private Date createTime;
    private Date updateTime;

    public Long getReferenceId() { return referenceId; } public void setReferenceId(Long v) { referenceId=v; }
    public Long getTeacherUserId() { return teacherUserId; } public void setTeacherUserId(Long v) { teacherUserId=v; }
    public Long getDeptId() { return deptId; } public void setDeptId(Long v) { deptId=v; }
    public Long getLessonId() { return lessonId; } public void setLessonId(Long v) { lessonId=v; }
    public Long getQuestionId() { return questionId; } public void setQuestionId(Long v) { questionId=v; }
    public String getOriginalFileName() { return originalFileName; } public void setOriginalFileName(String v) { originalFileName=v; }
    public String getResourcePath() { return resourcePath; } public void setResourcePath(String v) { resourcePath=v; }
    public String getFileExtension() { return fileExtension; } public void setFileExtension(String v) { fileExtension=v; }
    public String getMimeType() { return mimeType; } public void setMimeType(String v) { mimeType=v; }
    public Long getFileSize() { return fileSize; } public void setFileSize(Long v) { fileSize=v; }
    public String getSha256() { return sha256; } public void setSha256(String v) { sha256=v; }
    public Date getCreateTime() { return createTime; } public void setCreateTime(Date v) { createTime=v; }
    public Date getUpdateTime() { return updateTime; } public void setUpdateTime(Date v) { updateTime=v; }
}
