package com.ruoyi.business.domain.vo;

import java.util.Date;

/**
 * 前后端共用的操作题批改期限状态。
 */
public class PracticalGradingStatusVo
{
    private Long deadlineId;
    private Long lessonId;
    private Long deptId;
    private String entryYear;
    private String classCode;
    private boolean hasPractical;
    private int answeredStudentCount;
    private int totalStudentCount;
    private int remainingStudentsToTrigger;
    private int dueCount;
    private int gradedCount;
    private int ungradedCount;
    private String statusCode;
    private boolean canGrade;
    private Date serverNow;
    private Date triggerTime;
    private Date currentDeadlineTime;
    private String lastAdjustmentType;

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
    public boolean isHasPractical() { return hasPractical; }
    public void setHasPractical(boolean hasPractical) { this.hasPractical = hasPractical; }
    public int getAnsweredStudentCount() { return answeredStudentCount; }
    public void setAnsweredStudentCount(int answeredStudentCount) { this.answeredStudentCount = answeredStudentCount; }
    public int getTotalStudentCount() { return totalStudentCount; }
    public void setTotalStudentCount(int totalStudentCount) { this.totalStudentCount = totalStudentCount; }
    public int getRemainingStudentsToTrigger() { return remainingStudentsToTrigger; }
    public void setRemainingStudentsToTrigger(int remainingStudentsToTrigger) { this.remainingStudentsToTrigger = remainingStudentsToTrigger; }
    public int getDueCount() { return dueCount; }
    public void setDueCount(int dueCount) { this.dueCount = dueCount; }
    public int getGradedCount() { return gradedCount; }
    public void setGradedCount(int gradedCount) { this.gradedCount = gradedCount; }
    public int getUngradedCount() { return ungradedCount; }
    public void setUngradedCount(int ungradedCount) { this.ungradedCount = ungradedCount; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    public boolean isCanGrade() { return canGrade; }
    public void setCanGrade(boolean canGrade) { this.canGrade = canGrade; }
    public Date getServerNow() { return serverNow; }
    public void setServerNow(Date serverNow) { this.serverNow = serverNow; }
    public Date getTriggerTime() { return triggerTime; }
    public void setTriggerTime(Date triggerTime) { this.triggerTime = triggerTime; }
    public Date getCurrentDeadlineTime() { return currentDeadlineTime; }
    public void setCurrentDeadlineTime(Date currentDeadlineTime) { this.currentDeadlineTime = currentDeadlineTime; }
    public String getLastAdjustmentType() { return lastAdjustmentType; }
    public void setLastAdjustmentType(String lastAdjustmentType) { this.lastAdjustmentType = lastAdjustmentType; }
}
