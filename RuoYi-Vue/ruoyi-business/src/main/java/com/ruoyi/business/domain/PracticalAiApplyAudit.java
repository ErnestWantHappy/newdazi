package com.ruoyi.business.domain;

import java.util.Date;

/** AI 建议写入正式成绩的逐份审计快照。 */
public class PracticalAiApplyAudit
{
    private Long auditId;
    private Long jobId;
    private Long resultId;
    private Long answerId;
    private Long practicalVersionId;
    private String applyMode;
    private Integer oldScore;
    private Integer newScore;
    private String oldScoringDetailsJson;
    private String newScoringDetailsJson;
    private Long operatorUserId;
    private Date createTime;

    public Long getAuditId() { return auditId; } public void setAuditId(Long v) { auditId = v; }
    public Long getJobId() { return jobId; } public void setJobId(Long v) { jobId = v; }
    public Long getResultId() { return resultId; } public void setResultId(Long v) { resultId = v; }
    public Long getAnswerId() { return answerId; } public void setAnswerId(Long v) { answerId = v; }
    public Long getPracticalVersionId() { return practicalVersionId; } public void setPracticalVersionId(Long v) { practicalVersionId = v; }
    public String getApplyMode() { return applyMode; } public void setApplyMode(String v) { applyMode = v; }
    public Integer getOldScore() { return oldScore; } public void setOldScore(Integer v) { oldScore = v; }
    public Integer getNewScore() { return newScore; } public void setNewScore(Integer v) { newScore = v; }
    public String getOldScoringDetailsJson() { return oldScoringDetailsJson; } public void setOldScoringDetailsJson(String v) { oldScoringDetailsJson = v; }
    public String getNewScoringDetailsJson() { return newScoringDetailsJson; } public void setNewScoringDetailsJson(String v) { newScoringDetailsJson = v; }
    public Long getOperatorUserId() { return operatorUserId; } public void setOperatorUserId(Long v) { operatorUserId = v; }
    public Date getCreateTime() { return createTime; } public void setCreateTime(Date v) { createTime = v; }
}
