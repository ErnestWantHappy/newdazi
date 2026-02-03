package com.ruoyi.business.domain.vo;

/**
 * 学校班级统计VO
 */
public class SchoolClassStatsVO {

    /** 学校ID */
    private Long deptId;

    /** 学校名称 */
    private String deptName;

    /** 学校类型 (1小学 2初中 3高中) */
    private String schoolType;

    /** 入学年份 */
    private String entryYear;

    /** 班级编号 */
    private String classCode;

    /** 学生人数 */
    private Integer studentCount;

    /** 班级数量 (汇总用) */
    private Integer classCount;

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

    public String getEntryYear() {
        return entryYear;
    }

    public void setEntryYear(String entryYear) {
        this.entryYear = entryYear;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public Integer getClassCount() {
        return classCount;
    }

    public void setClassCount(Integer classCount) {
        this.classCount = classCount;
    }
}
