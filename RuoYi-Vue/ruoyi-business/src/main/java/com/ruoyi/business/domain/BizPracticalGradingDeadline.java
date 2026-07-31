package com.ruoyi.business.domain;

import java.util.Date;

/**
 * 课程班级操作题批改期限。
 */
public class BizPracticalGradingDeadline
{
    private Long deadlineId;
    private Long lessonId;
    private Long deptId;
    private String entryYear;
    private String classCode;
    private Date triggerTime;
    private Integer triggerAnsweredCount;
    private Integer triggerStudentCount;
    private Integer deadlineDays;
    private Date originalDeadlineTime;
    private Date currentDeadlineTime;
    private String initializationSource;
    private String lastAdjustmentType;
    private String createBy;

    public Long getDeadlineId() { return deadlineId; }
    public void setDeadlineId(Long deadlineId) { this.deadlineId = deadlineId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
    public Date getTriggerTime() { return triggerTime; }
    public void setTriggerTime(Date triggerTime) { this.triggerTime = triggerTime; }
    public Integer getTriggerAnsweredCount() { return triggerAnsweredCount; }
    public void setTriggerAnsweredCount(Integer triggerAnsweredCount) { this.triggerAnsweredCount = triggerAnsweredCount; }
    public Integer getTriggerStudentCount() { return triggerStudentCount; }
    public void setTriggerStudentCount(Integer triggerStudentCount) { this.triggerStudentCount = triggerStudentCount; }
    public Integer getDeadlineDays() { return deadlineDays; }
    public void setDeadlineDays(Integer deadlineDays) { this.deadlineDays = deadlineDays; }
    public Date getOriginalDeadlineTime() { return originalDeadlineTime; }
    public void setOriginalDeadlineTime(Date originalDeadlineTime) { this.originalDeadlineTime = originalDeadlineTime; }
    public Date getCurrentDeadlineTime() { return currentDeadlineTime; }
    public void setCurrentDeadlineTime(Date currentDeadlineTime) { this.currentDeadlineTime = currentDeadlineTime; }
    public String getInitializationSource() { return initializationSource; }
    public void setInitializationSource(String initializationSource) { this.initializationSource = initializationSource; }
    public String getLastAdjustmentType() { return lastAdjustmentType; }
    public void setLastAdjustmentType(String lastAdjustmentType) { this.lastAdjustmentType = lastAdjustmentType; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
}
