package com.ruoyi.business.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class GuideSheetProgressVo
{
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String classCode;
    private Integer currentPage;
    private String isSubmitted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastHeartbeat;

    /** 进度详情JSON（填写中：{filled:N,total:N}；已提交：评分详情） */
    private String progressDetail;

    /** 学生答案JSON（用于提取自评数据） */
    private String answerJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public String getIsSubmitted() {
        return isSubmitted;
    }

    public void setIsSubmitted(String isSubmitted) {
        this.isSubmitted = isSubmitted;
    }

    public Date getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Date lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public String getProgressDetail() { return progressDetail; }
    public void setProgressDetail(String progressDetail) { this.progressDetail = progressDetail; }

    public String getAnswerJson() { return answerJson; }
    public void setAnswerJson(String answerJson) { this.answerJson = answerJson; }
}
