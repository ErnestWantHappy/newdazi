package com.ruoyi.business.domain.vo;

/**
 * 教师首页 - 简化课程信息视图对象
 */
public class LessonInfoVo {
    /** 课程ID */
    private Long lessonId;

    /** 课程标题 */
    private String lessonTitle;

    /** 第几课 */
    private Integer lessonNum;

    /** 课程创建时确定的开设年级，用于届别内的当前/历史课程分栏。 */
    private Long grade;

    /** 课程永久所属入学年份，前端跳转成绩时以课程自身值为准。 */
    private String entryYear;

    /** 创建时间，用于教师首页按最新课程排序 */
    private java.util.Date createTime;

    /**
     * 核心修复：添加无参数的构造函数
     * 这对于某些框架的JSON序列化和反序列化至关重要
     */
    public LessonInfoVo() {
    }

    // --- Getter and Setter ---
    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public String getLessonTitle() {
        return lessonTitle;
    }

    public void setLessonTitle(String lessonTitle) {
        this.lessonTitle = lessonTitle;
    }

    public Integer getLessonNum() {
        return lessonNum;
    }

    public void setLessonNum(Integer lessonNum) {
        this.lessonNum = lessonNum;
    }

    public Long getGrade() {
        return grade;
    }

    public void setGrade(Long grade) {
        this.grade = grade;
    }

    public String getEntryYear() {
        return entryYear;
    }

    public void setEntryYear(String entryYear) {
        this.entryYear = entryYear;
    }

    public java.util.Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(java.util.Date createTime) {
        this.createTime = createTime;
    }

    /** 课程类型: self=自建, shared=共享 */
    private String courseType;

    /** 是否包含操作题 */
    private boolean hasPractical;

    /** 是否存在已开启的班级在线协作房间，用于教师首页显示入口。 */
    private boolean hasCollaboration;

    /** 课程用途：assessment 常规课 / attendance 课堂考勤 */
    private String lessonMode;

    /** 是否开启自动推进下一课（仅常规课） */
    private Boolean autoAdvanceEnabled;

    /** 自动推进阈值百分比，默认 50 */
    private Integer autoAdvanceThresholdPct;

    /** 自动推进延迟小时数 */
    private java.math.BigDecimal autoAdvanceDelayHours;

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public boolean isHasPractical() {
        return hasPractical;
    }

    public void setHasPractical(boolean hasPractical) {
        this.hasPractical = hasPractical;
    }

    public boolean isHasCollaboration() {
        return hasCollaboration;
    }

    public void setHasCollaboration(boolean hasCollaboration) {
        this.hasCollaboration = hasCollaboration;
    }

    public String getLessonMode() {
        return lessonMode;
    }

    public void setLessonMode(String lessonMode) {
        this.lessonMode = lessonMode;
    }

    public Boolean getAutoAdvanceEnabled() {
        return autoAdvanceEnabled;
    }

    public void setAutoAdvanceEnabled(Boolean autoAdvanceEnabled) {
        this.autoAdvanceEnabled = autoAdvanceEnabled;
    }

    public Integer getAutoAdvanceThresholdPct() {
        return autoAdvanceThresholdPct;
    }

    public void setAutoAdvanceThresholdPct(Integer autoAdvanceThresholdPct) {
        this.autoAdvanceThresholdPct = autoAdvanceThresholdPct;
    }

    public java.math.BigDecimal getAutoAdvanceDelayHours() {
        return autoAdvanceDelayHours;
    }

    public void setAutoAdvanceDelayHours(java.math.BigDecimal autoAdvanceDelayHours) {
        this.autoAdvanceDelayHours = autoAdvanceDelayHours;
    }

    /** 已指派的班级列表, 例如: ["1班", "5班"] */
    private java.util.List<String> assignedClasses;

    /** 每个已指派班级的操作题期限状态，禁止合并成课程级单一截止时间。 */
    private java.util.List<PracticalGradingStatusVo> practicalDeadlineClasses;

    public java.util.List<String> getAssignedClasses() {
        return assignedClasses;
    }

    public void setAssignedClasses(java.util.List<String> assignedClasses) {
        this.assignedClasses = assignedClasses;
    }

    public java.util.List<PracticalGradingStatusVo> getPracticalDeadlineClasses() {
        return practicalDeadlineClasses;
    }

    public void setPracticalDeadlineClasses(
            java.util.List<PracticalGradingStatusVo> practicalDeadlineClasses) {
        this.practicalDeadlineClasses = practicalDeadlineClasses;
    }
}
