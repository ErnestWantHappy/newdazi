package com.ruoyi.business.domain;

/**
 * 评卷任务分配表 biz_county_exam_grader
 * 
 * @author ruoyi
 */
public class CountyExamGrader {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 县考ID */
    private Long examId;

    /** 操作题ID */
    private Long questionId;

    /** 教师ID */
    private Long graderId;

    /** 教师姓名 */
    private String graderName;

    /** 教师账号 */
    private String userName;

    /** 所属学校 */
    private String deptName;

    /** 分配数量 */
    private Integer targetCount;

    /** 已评数量 */
    private Integer gradedCount;

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

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getGraderId() {
        return graderId;
    }

    public void setGraderId(Long graderId) {
        this.graderId = graderId;
    }

    public String getGraderName() {
        return graderName;
    }

    public void setGraderName(String graderName) {
        this.graderName = graderName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Integer getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(Integer targetCount) {
        this.targetCount = targetCount;
    }

    public Integer getGradedCount() {
        return gradedCount;
    }

    public void setGradedCount(Integer gradedCount) {
        this.gradedCount = gradedCount;
    }
}
