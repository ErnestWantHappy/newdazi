package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 学生在某节课某道题上的权威任务状态。
 */
public class BizStudentTaskState
{
    private Long stateId;
    private Long deptId;
    private Long lessonId;
    private Long questionId;
    private Long studentId;
    private String taskState;
    private Long stateVersion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date changedAt;

    /** 名单展示字段。 */
    private String studentName;
    private String studentNo;
    private String entryYear;
    private String classCode;

    public Long getStateId() { return stateId; }
    public void setStateId(Long stateId) { this.stateId = stateId; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getTaskState() { return taskState; }
    public void setTaskState(String taskState) { this.taskState = taskState; }
    public Long getStateVersion() { return stateVersion; }
    public void setStateVersion(Long stateVersion) { this.stateVersion = stateVersion; }
    public Date getChangedAt() { return changedAt; }
    public void setChangedAt(Date changedAt) { this.changedAt = changedAt; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
}
