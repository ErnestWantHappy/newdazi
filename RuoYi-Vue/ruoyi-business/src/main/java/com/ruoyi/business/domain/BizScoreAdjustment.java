package com.ruoyi.business.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 作业分人工修正记录对象 biz_score_adjustment
 */
public class BizScoreAdjustment extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 修正记录ID */
    private Long adjustmentId;

    /** 学生ID */
    private Long studentId;

    /** 课程ID */
    private Long lessonId;

    /** 所属学校ID */
    private Long deptId;

    /** 操作教师ID */
    private Long teacherId;

    /** 原始作业分 */
    private Integer originalHomeworkScore;

    /** 修正后作业分 */
    private Integer adjustedHomeworkScore;

    /** 操作类型：ADJUST 修正，CANCEL 取消修正 */
    private String actionType;

    /** 修正原因 */
    private String reason;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getAdjustmentId() {
        return adjustmentId;
    }

    public void setAdjustmentId(Long adjustmentId) {
        this.adjustmentId = adjustmentId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Integer getOriginalHomeworkScore() {
        return originalHomeworkScore;
    }

    public void setOriginalHomeworkScore(Integer originalHomeworkScore) {
        this.originalHomeworkScore = originalHomeworkScore;
    }

    public Integer getAdjustedHomeworkScore() {
        return adjustedHomeworkScore;
    }

    public void setAdjustedHomeworkScore(Integer adjustedHomeworkScore) {
        this.adjustedHomeworkScore = adjustedHomeworkScore;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
