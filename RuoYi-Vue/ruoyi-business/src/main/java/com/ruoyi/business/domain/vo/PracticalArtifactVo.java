package com.ruoyi.business.domain.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.domain.PracticalAttachment;

/**
 * 教师和学生共同使用的逻辑作品只读视图。
 */
public class PracticalArtifactVo
{
    private Long artifactId;
    private Long versionId;
    private Integer versionNo;
    private String versionStatus;
    private Integer scoreSnapshot;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;
    private List<PracticalAttachment> attachments = new ArrayList<PracticalAttachment>();

    public Long getArtifactId() { return artifactId; }
    public void setArtifactId(Long artifactId) { this.artifactId = artifactId; }
    public Long getVersionId() { return versionId; }
    public void setVersionId(Long versionId) { this.versionId = versionId; }
    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    public String getVersionStatus() { return versionStatus; }
    public void setVersionStatus(String versionStatus) { this.versionStatus = versionStatus; }
    public Integer getScoreSnapshot() { return scoreSnapshot; }
    public void setScoreSnapshot(Integer scoreSnapshot) { this.scoreSnapshot = scoreSnapshot; }
    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
    public List<PracticalAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<PracticalAttachment> attachments) { this.attachments = attachments; }
}
