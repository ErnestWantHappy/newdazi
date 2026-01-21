package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 县考学生成绩总表 biz_county_exam_student
 * 
 * @author ruoyi
 */
public class CountyExamStudent {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 县考ID */
    private Long examId;

    /** 学生ID */
    private Long studentId;

    /** 所属学校快照 */
    private Long deptId;

    /** 班级信息快照 */
    private String classInfo;

    /** 总分 */
    private BigDecimal totalScore;

    /** 理论题得分 */
    private BigDecimal theoryScore;

    /** 技术题得分 */
    private BigDecimal techScore;

    /** 状态(0未交/1已交) */
    private String status;

    /** 提交时间 */
    private Date submitTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(String classInfo) {
        this.classInfo = classInfo;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public BigDecimal getTheoryScore() {
        return theoryScore;
    }

    public void setTheoryScore(BigDecimal theoryScore) {
        this.theoryScore = theoryScore;
    }

    public BigDecimal getTechScore() {
        return techScore;
    }

    public void setTechScore(BigDecimal techScore) {
        this.techScore = techScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }
}
