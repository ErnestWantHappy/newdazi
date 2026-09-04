package com.ruoyi.business.service;

import java.math.BigDecimal;

/** 厂商响应经本地解析后的结构化建议。 */
public class PracticalAiGradingOutput
{
    private Integer suggestedScore;
    private String scoringDetailsJson;
    private String evidenceJson;
    private BigDecimal confidence;
    private String requestId;
    private Integer promptTokens;
    private Integer completionTokens;
    public Integer getSuggestedScore() { return suggestedScore; } public void setSuggestedScore(Integer v) { suggestedScore=v; }
    public String getScoringDetailsJson() { return scoringDetailsJson; } public void setScoringDetailsJson(String v) { scoringDetailsJson=v; }
    public String getEvidenceJson() { return evidenceJson; } public void setEvidenceJson(String v) { evidenceJson=v; }
    public BigDecimal getConfidence() { return confidence; } public void setConfidence(BigDecimal v) { confidence=v; }
    public String getRequestId() { return requestId; } public void setRequestId(String v) { requestId=v; }
    public Integer getPromptTokens() { return promptTokens; } public void setPromptTokens(Integer v) { promptTokens=v; }
    public Integer getCompletionTokens() { return completionTokens; } public void setCompletionTokens(Integer v) { completionTokens=v; }
}
