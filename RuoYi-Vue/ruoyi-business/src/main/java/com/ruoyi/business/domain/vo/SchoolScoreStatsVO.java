package com.ruoyi.business.domain.vo;

import java.math.BigDecimal;

/**
 * 学校成绩统计 VO
 */
public class SchoolScoreStatsVO {
    /** 学校ID */
    private Long deptId;

    /** 学校名称 */
    private String deptName;

    /** 学部类型 (1=小学, 2=初中, 3=高中) */
    private String schoolType;

    /** 活跃学生数 (有提交记录的学生) */
    private Integer activeStudentCount;

    /** 班级数 */
    private Integer classCount;

    /** 平均打字速度 (字/分) */
    private Integer avgTypingSpeed;

    /** 平均总分 */
    private BigDecimal avgTotalScore;

    /** 及格率 (>=60分) % */
    private BigDecimal passRate;

    /** 优秀率 (>=90分) % */
    private BigDecimal excellentRate;

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getSchoolType() {
        return schoolType;
    }

    public void setSchoolType(String schoolType) {
        this.schoolType = schoolType;
    }

    public Integer getActiveStudentCount() {
        return activeStudentCount;
    }

    public void setActiveStudentCount(Integer activeStudentCount) {
        this.activeStudentCount = activeStudentCount;
    }

    public Integer getClassCount() {
        return classCount;
    }

    public void setClassCount(Integer classCount) {
        this.classCount = classCount;
    }

    public Integer getAvgTypingSpeed() {
        return avgTypingSpeed;
    }

    public void setAvgTypingSpeed(Integer avgTypingSpeed) {
        this.avgTypingSpeed = avgTypingSpeed;
    }

    public BigDecimal getAvgTotalScore() {
        return avgTotalScore;
    }

    public void setAvgTotalScore(BigDecimal avgTotalScore) {
        this.avgTotalScore = avgTotalScore;
    }

    public BigDecimal getPassRate() {
        return passRate;
    }

    public void setPassRate(BigDecimal passRate) {
        this.passRate = passRate;
    }

    public BigDecimal getExcellentRate() {
        return excellentRate;
    }

    public void setExcellentRate(BigDecimal excellentRate) {
        this.excellentRate = excellentRate;
    }
}
