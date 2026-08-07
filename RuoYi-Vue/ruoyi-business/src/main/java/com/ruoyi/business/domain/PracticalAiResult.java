package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;

/** AI 对某个不可变提交版本给出的建议；不等同于正式成绩。 */
public class PracticalAiResult
{
    private Long resultId;
    private Long jobId;
    private Long answerId;
    private Long practicalVersionId;
    private Long rubricSnapshotId;
    private String resultStatus;
    private Integer suggestedScore;
    private String scoringDetailsJson;
    private String evidenceJson;
    private BigDecimal confidence;
    private String providerRequestId;
    private Integer promptTokens;
    private Integer completionTokens;
    private String errorMessage;
    private Date createTime;
    private Date finishTime;

    public Long getResultId() { return resultId; } public void setResultId(Long v) { resultId=v; }
    public Long getJobId() { return jobId; } public void setJobId(Long v) { jobId=v; }
    public Long getAnswerId() { return answerId; } public void setAnswerId(Long v) { answerId=v; }
    public Long getPracticalVersionId() { return practicalVersionId; } public void setPracticalVersionId(Long v) { practicalVersionId=v; }
    public Long getRubricSnapshotId() { return rubricSnapshotId; } public void setRubricSnapshotId(Long v) { rubricSnapshotId=v; }
    public String getResultStatus() { return resultStatus; } public void setResultStatus(String v) { resultStatus=v; }
    public Integer getSuggestedScore() { return suggestedScore; } public void setSuggestedScore(Integer v) { suggestedScore=v; }
    public String getScoringDetailsJson() { return scoringDetailsJson; } public void setScoringDetailsJson(String v) { scoringDetailsJson=v; }
    public String getEvidenceJson() { return evidenceJson; } public void setEvidenceJson(String v) { evidenceJson=v; }
    public BigDecimal getConfidence() { return confidence; } public void setConfidence(BigDecimal v) { confidence=v; }
    public String getProviderRequestId() { return providerRequestId; } public void setProviderRequestId(String v) { providerRequestId=v; }
    public Integer getPromptTokens() { return promptTokens; } public void setPromptTokens(Integer v) { promptTokens=v; }
    public Integer getCompletionTokens() { return completionTokens; } public void setCompletionTokens(Integer v) { completionTokens=v; }
    public String getErrorMessage() { return errorMessage; } public void setErrorMessage(String v) { errorMessage=v; }
    public Date getCreateTime() { return createTime; } public void setCreateTime(Date v) { createTime=v; }
    public Date getFinishTime() { return finishTime; } public void setFinishTime(Date v) { finishTime=v; }
}
