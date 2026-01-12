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

    /** 课程类型: self=自建, shared=共享 */
    private String courseType;

    /** 是否包含操作题 */
    private boolean hasPractical;

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

    /** 已指派的班级列表, 例如: ["1班", "5班"] */
    private java.util.List<String> assignedClasses;

    public java.util.List<String> getAssignedClasses() {
        return assignedClasses;
    }

    public void setAssignedClasses(java.util.List<String> assignedClasses) {
        this.assignedClasses = assignedClasses;
    }
}
