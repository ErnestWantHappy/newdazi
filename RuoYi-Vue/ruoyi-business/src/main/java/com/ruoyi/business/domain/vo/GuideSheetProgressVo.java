package com.ruoyi.business.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class GuideSheetProgressVo
{
    private Long id;
    private Long bindingId;
    private Long answerId;
    private Long studentId;
    private String studentName;
    private String studentUserName;
    private String studentNo;
    private Long deptId;
    private String entryYear;
    private String classCode;
    private Integer currentPage;
    private String isSubmitted;
    private String answerStatus;
    private Integer autoScore;
    private Integer manualAdjustment;
    private Integer totalScore;
    private String gradingStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastHeartbeat;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    private String progressDetail;
    private String answerJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBindingId() { return bindingId; }
    public void setBindingId(Long bindingId) { this.bindingId = bindingId; }
    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentUserName() { return studentUserName; }
    public void setStudentUserName(String studentUserName) { this.studentUserName = studentUserName; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
    public Integer getCurrentPage() { return currentPage; }
    public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }
    public String getIsSubmitted() { return isSubmitted; }
    public void setIsSubmitted(String isSubmitted) { this.isSubmitted = isSubmitted; }
    public String getAnswerStatus() { return answerStatus; }
    public void setAnswerStatus(String answerStatus) { this.answerStatus = answerStatus; }
    public Integer getAutoScore() { return autoScore; }
    public void setAutoScore(Integer autoScore) { this.autoScore = autoScore; }
    public Integer getManualAdjustment() { return manualAdjustment; }
    public void setManualAdjustment(Integer manualAdjustment) { this.manualAdjustment = manualAdjustment; }
    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
    public String getGradingStatus() { return gradingStatus; }
    public void setGradingStatus(String gradingStatus) { this.gradingStatus = gradingStatus; }
    public Date getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(Date lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
    public String getProgressDetail() { return progressDetail; }
    public void setProgressDetail(String progressDetail) { this.progressDetail = progressDetail; }
    public String getAnswerJson() { return answerJson; }
    public void setAnswerJson(String answerJson) { this.answerJson = answerJson; }
}
