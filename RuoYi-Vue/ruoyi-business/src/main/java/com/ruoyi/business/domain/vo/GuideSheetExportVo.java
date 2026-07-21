package com.ruoyi.business.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;

/**
 * 导学单成绩导出行。
 */
public class GuideSheetExportVo
{
    private Long bindingId;

    @Excel(name = "学生姓名")
    private String studentName;

    @Excel(name = "账号")
    private String studentUserName;

    @Excel(name = "学号")
    private String studentNo;

    @Excel(name = "入学年份")
    private String entryYear;

    @Excel(name = "班级")
    private String classCode;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "自动评分")
    private Integer autoScore;

    @Excel(name = "人工调整分")
    private Integer manualAdjustment;

    @Excel(name = "最终得分")
    private Integer totalScore;

    @Excel(name = "评分状态")
    private String gradingStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "提交时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    public Long getBindingId() { return bindingId; }
    public void setBindingId(Long bindingId) { this.bindingId = bindingId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentUserName() { return studentUserName; }
    public void setStudentUserName(String studentUserName) { this.studentUserName = studentUserName; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
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
    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
}
