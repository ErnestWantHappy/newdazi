package com.ruoyi.business.domain;

import java.util.Date;

/**
 * 一个学生在一个课程或抽测操作题下的逻辑作品。
 */
public class PracticalArtifact
{
    private Long artifactId;
    private String contextType;
    private Long contextId;
    private Long studentId;
    private Long questionId;
    private Long currentVersionId;
    private Integer latestVersionNo;
    private Integer lockVersion;
    private Date createTime;
    private Date updateTime;

    public Long getArtifactId() { return artifactId; }
    public void setArtifactId(Long artifactId) { this.artifactId = artifactId; }
    public String getContextType() { return contextType; }
    public void setContextType(String contextType) { this.contextType = contextType; }
    public Long getContextId() { return contextId; }
    public void setContextId(Long contextId) { this.contextId = contextId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Long getCurrentVersionId() { return currentVersionId; }
    public void setCurrentVersionId(Long currentVersionId) { this.currentVersionId = currentVersionId; }
    public Integer getLatestVersionNo() { return latestVersionNo; }
    public void setLatestVersionNo(Integer latestVersionNo) { this.latestVersionNo = latestVersionNo; }
    public Integer getLockVersion() { return lockVersion; }
    public void setLockVersion(Integer lockVersion) { this.lockVersion = lockVersion; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
