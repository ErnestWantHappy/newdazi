package com.ruoyi.business.domain.vo;

import java.util.List;

/**
 * 教师首页 - 年级分组视图对象
 */
public class GradeGroupVo {
    /** 入学年份 */
    private String entryYear;

    /** 当前计算出的年级ID, 例如: 8 */
    private Long gradeId;

    /** 当前计算出的年级名称, 例如: "八年级" */
    private String gradeName;

    /** 这个年级下所有的班级列表, 例如: ["1班", "2班"] */
    private List<String> allClassesInGrade;

    /** 这个年级下已创建的课程列表 */
    private List<LessonInfoVo> lessons;

    /**
     * 核心修复：添加无参数的构造函数
     */
    public GradeGroupVo() {
    }

    // --- Getter and Setter ---
    public String getEntryYear() {
        return entryYear;
    }

    public void setEntryYear(String entryYear) {
        this.entryYear = entryYear;
    }

    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public List<String> getAllClassesInGrade() {
        return allClassesInGrade;
    }

    public void setAllClassesInGrade(List<String> allClassesInGrade) {
        this.allClassesInGrade = allClassesInGrade;
    }

    public List<LessonInfoVo> getLessons() {
        return lessons;
    }

    public void setLessons(List<LessonInfoVo> lessons) {
        this.lessons = lessons;
    }
}
