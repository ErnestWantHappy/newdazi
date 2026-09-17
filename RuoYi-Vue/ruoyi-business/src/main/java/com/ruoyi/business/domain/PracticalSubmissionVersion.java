package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 操作题不可变提交版本。
 */
public class PracticalSubmissionVersion
{
    private Long versionId;
    private Long artifactId;
    private Long rubricSnapshotId;
    private Integer versionNo;
    private Long sourceAnswerId;
    private String versionStatus;
    private String scoreStatus;
    private Integer scoreSnapshot;
    private String scoringDetailsJson;
    private Long submittedByUserId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date invalidatedTime;
    private Date createTime;

    public Long getVersionId() { return versionId; }
    public void setVersionId(Long versionId) { this.versionId = versionId; }
    public Long getArtifactId() { return artifactId; }
    public void setArtifactId(Long artifactId) { this.artifactId = artifactId; }
    public Long getRubricSnapshotId() { return rubricSnapshotId; }
    public void setRubricSnapshotId(Long rubricSnapshotId) { this.rubricSnapshotId = rubricSnapshotId; }
    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    public Long getSourceAnswerId() { return sourceAnswerId; }
    public void setSourceAnswerId(Long sourceAnswerId) { this.sourceAnswerId = sourceAnswerId; }
    public String getVersionStatus() { return versionStatus; }
    public void setVersionStatus(String versionStatus) { this.versionStatus = versionStatus; }
    public String getScoreStatus() { return scoreStatus; }
    public void setScoreStatus(String scoreStatus) { this.scoreStatus = scoreStatus; }
    public Integer getScoreSnapshot() { return scoreSnapshot; }
    public void setScoreSnapshot(Integer scoreSnapshot) { this.scoreSnapshot = scoreSnapshot; }
    public String getScoringDetailsJson() { return scoringDetailsJson; }
    public void setScoringDetailsJson(String scoringDetailsJson) { this.scoringDetailsJson = scoringDetailsJson; }
    public Long getSubmittedByUserId() { return submittedByUserId; }
    public void setSubmittedByUserId(Long submittedByUserId) { this.submittedByUserId = submittedByUserId; }
    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
    public Date getInvalidatedTime() { return invalidatedTime; }
    public void setInvalidatedTime(Date invalidatedTime) { this.invalidatedTime = invalidatedTime; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
