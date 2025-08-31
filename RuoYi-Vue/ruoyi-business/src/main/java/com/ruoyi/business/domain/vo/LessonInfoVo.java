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
}
