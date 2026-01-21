package com.ruoyi.business.domain;

/**
 * 县考班级指派表 biz_county_exam_class
 * 
 * @author ruoyi
 */
public class CountyExamClass {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 县考ID */
    private Long examId;

    /** 学校ID */
    private Long deptId;

    /** 类型(1按行政班 2按教学班) */
    private String type;

    /** 入学年份 */
    private String entryYear;

    /** 班级编号 */
    private String classCode;

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

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
