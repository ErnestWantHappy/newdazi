package com.ruoyi.business.domain.vo;

import java.util.Date;

/**
 * 教师课堂大屏使用的学生课程任务汇总，不替代每题的权威状态记录。
 */
public class ClassroomStudentTaskSummaryVo
{
    private Long studentId;
    private String studentName;
    private String studentNo;
    private Integer totalQuestionCount;
    private Integer startedQuestionCount;
    private Integer enteredQuestionCount;
    private Integer workingQuestionCount;
    private Integer submittedQuestionCount;
    private Integer gradedQuestionCount;
    private Integer returnedQuestionCount;
    private String taskState;
    private Date changedAt;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public Integer getTotalQuestionCount() { return totalQuestionCount; }
    public void setTotalQuestionCount(Integer totalQuestionCount) { this.totalQuestionCount = totalQuestionCount; }
    public Integer getStartedQuestionCount() { return startedQuestionCount; }
    public void setStartedQuestionCount(Integer startedQuestionCount) { this.startedQuestionCount = startedQuestionCount; }
    public Integer getEnteredQuestionCount() { return enteredQuestionCount; }
    public void setEnteredQuestionCount(Integer enteredQuestionCount) { this.enteredQuestionCount = enteredQuestionCount; }
    public Integer getWorkingQuestionCount() { return workingQuestionCount; }
    public void setWorkingQuestionCount(Integer workingQuestionCount) { this.workingQuestionCount = workingQuestionCount; }
    public Integer getSubmittedQuestionCount() { return submittedQuestionCount; }
    public void setSubmittedQuestionCount(Integer submittedQuestionCount) { this.submittedQuestionCount = submittedQuestionCount; }
    public Integer getGradedQuestionCount() { return gradedQuestionCount; }
    public void setGradedQuestionCount(Integer gradedQuestionCount) { this.gradedQuestionCount = gradedQuestionCount; }
    public Integer getReturnedQuestionCount() { return returnedQuestionCount; }
    public void setReturnedQuestionCount(Integer returnedQuestionCount) { this.returnedQuestionCount = returnedQuestionCount; }
    public String getTaskState() { return taskState; }
    public void setTaskState(String taskState) { this.taskState = taskState; }
    public Date getChangedAt() { return changedAt; }
    public void setChangedAt(Date changedAt) { this.changedAt = changedAt; }
}
