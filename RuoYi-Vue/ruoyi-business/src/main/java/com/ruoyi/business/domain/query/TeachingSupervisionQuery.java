package com.ruoyi.business.domain.query;

import java.util.Date;

/**
 * 课程与成绩监管筛选条件。
 */
public class TeachingSupervisionQuery
{
    private String academicYear;
    private String semester;
    private Date startTime;
    private Date endTime;
    private String usageStartDate;
    private String usageEndDate;
    private Date activityStartTime;
    private Date activityEndTime;
    private Boolean usageDateFiltered;
    private String usageSort;
    private Long deptId;
    private Long teacherId;
    private Long lessonId;
    private String keyword;
    private String lessonMode;
    private String entryYear;
    private String grade;
    private String classCode;
    private Boolean hasPractical;
    private String statusCode;

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public String getUsageStartDate() { return usageStartDate; }
    public void setUsageStartDate(String usageStartDate) { this.usageStartDate = usageStartDate; }
    public String getUsageEndDate() { return usageEndDate; }
    public void setUsageEndDate(String usageEndDate) { this.usageEndDate = usageEndDate; }
    public Date getActivityStartTime() { return activityStartTime; }
    public void setActivityStartTime(Date activityStartTime) { this.activityStartTime = activityStartTime; }
    public Date getActivityEndTime() { return activityEndTime; }
    public void setActivityEndTime(Date activityEndTime) { this.activityEndTime = activityEndTime; }
    public Boolean getUsageDateFiltered() { return usageDateFiltered; }
    public void setUsageDateFiltered(Boolean usageDateFiltered) { this.usageDateFiltered = usageDateFiltered; }
    public String getUsageSort() { return usageSort; }
    public void setUsageSort(String usageSort) { this.usageSort = usageSort; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getLessonMode() { return lessonMode; }
    public void setLessonMode(String lessonMode) { this.lessonMode = lessonMode; }
    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
    public Boolean getHasPractical() { return hasPractical; }
    public void setHasPractical(Boolean hasPractical) { this.hasPractical = hasPractical; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
}
