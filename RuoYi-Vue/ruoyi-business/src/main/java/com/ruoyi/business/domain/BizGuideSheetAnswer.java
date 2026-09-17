package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizGuideSheetAnswer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long answerId;
    private Long bindingId;
    private Long studentId;
    private Long lessonId;
    private Long sourceSheetId;
    private String answerJson;
    private Integer currentPage;
    private String status;
    private Integer autoScore;
    private Integer manualAdjustment;
    private Integer totalScore;
    private String gradingStatus;
    private String gradingDetail;
    private Long draftRevision;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }
    public Long getBindingId() { return bindingId; }
    public void setBindingId(Long bindingId) { this.bindingId = bindingId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getSourceSheetId() { return sourceSheetId; }
    public void setSourceSheetId(Long sourceSheetId) { this.sourceSheetId = sourceSheetId; }
    public String getAnswerJson() { return answerJson; }
    public void setAnswerJson(String answerJson) { this.answerJson = answerJson; }
    public Integer getCurrentPage() { return currentPage; }
    public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAutoScore() { return autoScore; }
    public void setAutoScore(Integer autoScore) { this.autoScore = autoScore; }
    public Integer getManualAdjustment() { return manualAdjustment; }
    public void setManualAdjustment(Integer manualAdjustment) { this.manualAdjustment = manualAdjustment; }
    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
    public String getGradingStatus() { return gradingStatus; }
    public void setGradingStatus(String gradingStatus) { this.gradingStatus = gradingStatus; }
    public String getGradingDetail() { return gradingDetail; }
    public void setGradingDetail(String gradingDetail) { this.gradingDetail = gradingDetail; }
    public Long getDraftRevision() { return draftRevision; }
    public void setDraftRevision(Long draftRevision) { this.draftRevision = draftRevision; }
    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
}
