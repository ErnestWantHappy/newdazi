package com.ruoyi.business.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;

import java.util.Date;

/**
 * 导学单答案导出 VO
 *
 * @author ruoyi
 */
public class GuideSheetExportVo
{
    /** 学生姓名 */
    @Excel(name = "学生姓名")
    private String studentName;

    /** 学号 */
    @Excel(name = "学号")
    private String studentNo;

    /** 班级编号 */
    @Excel(name = "班级")
    private String classCode;

    /** 状态：未开始/填写中/已提交 */
    @Excel(name = "状态")
    private String status;

    /** 总分 */
    @Excel(name = "总分")
    private Integer totalScore;

    /** 评分状态 */
    @Excel(name = "评分状态")
    private String gradingStatus;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "提交时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public String getGradingStatus() { return gradingStatus; }
    public void setGradingStatus(String gradingStatus) { this.gradingStatus = gradingStatus; }

    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
}
