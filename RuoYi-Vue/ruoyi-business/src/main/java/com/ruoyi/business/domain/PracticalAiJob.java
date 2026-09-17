package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;

/** 一个班级、一道操作题的一次批量 AI 建议任务。 */
public class PracticalAiJob
{
    private Long jobId;
    private Long teacherUserId;
    private Long deptId;
    private Long lessonId;
    private Long questionId;
    private String entryYear;
    private String classCode;
    private String providerCode;
    private String modelName;
    private BigDecimal inputPricePerThousand;
    private BigDecimal outputPricePerThousand;
    private String priceStatus;
    private String priceNote;
    private String promptVersion;
    private String scopeMode;
    private String referenceAnswerJson;
    private String starterMaterialsJson;
    private String preparationStatus;
    private String comparisonPagesJson;
    private Long currentResultId;
    private Date heartbeatTime;
    private String jobStatus;
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
    private Integer skippedCount;
    private String errorMessage;
    private Date createTime;
    private Date startTime;
    private Date finishTime;

    public Long getJobId() { return jobId; } public void setJobId(Long v) { jobId=v; }
    public Long getTeacherUserId() { return teacherUserId; } public void setTeacherUserId(Long v) { teacherUserId=v; }
    public Long getDeptId() { return deptId; } public void setDeptId(Long v) { deptId=v; }
    public Long getLessonId() { return lessonId; } public void setLessonId(Long v) { lessonId=v; }
    public Long getQuestionId() { return questionId; } public void setQuestionId(Long v) { questionId=v; }
    public String getEntryYear() { return entryYear; } public void setEntryYear(String v) { entryYear=v; }
    public String getClassCode() { return classCode; } public void setClassCode(String v) { classCode=v; }
    public String getProviderCode() { return providerCode; } public void setProviderCode(String v) { providerCode=v; }
    public String getModelName() { return modelName; } public void setModelName(String v) { modelName=v; }
    public BigDecimal getInputPricePerThousand() { return inputPricePerThousand; } public void setInputPricePerThousand(BigDecimal v) { inputPricePerThousand=v; }
    public BigDecimal getOutputPricePerThousand() { return outputPricePerThousand; } public void setOutputPricePerThousand(BigDecimal v) { outputPricePerThousand=v; }
    public String getPriceStatus() { return priceStatus; } public void setPriceStatus(String v) { priceStatus=v; }
    public String getPriceNote() { return priceNote; } public void setPriceNote(String v) { priceNote=v; }
    public String getPromptVersion() { return promptVersion; } public void setPromptVersion(String v) { promptVersion=v; }
    public String getScopeMode() { return scopeMode; } public void setScopeMode(String v) { scopeMode=v; }
    public String getReferenceAnswerJson() { return referenceAnswerJson; } public void setReferenceAnswerJson(String v) { referenceAnswerJson=v; }
    public String getStarterMaterialsJson() { return starterMaterialsJson; } public void setStarterMaterialsJson(String v) { starterMaterialsJson=v; }
    public String getPreparationStatus() { return preparationStatus; } public void setPreparationStatus(String v) { preparationStatus=v; }
    public String getComparisonPagesJson() { return comparisonPagesJson; } public void setComparisonPagesJson(String v) { comparisonPagesJson=v; }
    public Long getCurrentResultId() { return currentResultId; } public void setCurrentResultId(Long v) { currentResultId=v; }
    public Date getHeartbeatTime() { return heartbeatTime; } public void setHeartbeatTime(Date v) { heartbeatTime=v; }
    public String getJobStatus() { return jobStatus; } public void setJobStatus(String v) { jobStatus=v; }
    public Integer getTotalCount() { return totalCount; } public void setTotalCount(Integer v) { totalCount=v; }
    public Integer getSuccessCount() { return successCount; } public void setSuccessCount(Integer v) { successCount=v; }
    public Integer getFailedCount() { return failedCount; } public void setFailedCount(Integer v) { failedCount=v; }
    public Integer getSkippedCount() { return skippedCount; } public void setSkippedCount(Integer v) { skippedCount=v; }
    public String getErrorMessage() { return errorMessage; } public void setErrorMessage(String v) { errorMessage=v; }
    public Date getCreateTime() { return createTime; } public void setCreateTime(Date v) { createTime=v; }
    public Date getStartTime() { return startTime; } public void setStartTime(Date v) { startTime=v; }
    public Date getFinishTime() { return finishTime; } public void setFinishTime(Date v) { finishTime=v; }
}
