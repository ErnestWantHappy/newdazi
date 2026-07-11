package com.ruoyi.business.domain.vo;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.List;

/**
 * 课程完整详细信息视图对象 (最终重构版)
 * 核心修复：不再继承 BizLesson，避免方法冲突
 */
public class LessonDetailVo extends BaseEntity {

    // --- 从 BizLesson 中平移过来的核心字段 ---
    private Long lessonId;
    private String lessonTitle;
    private Long grade;
    private String semester;
    private Integer lessonNum;
    
    /** 出题模式: 0=固定顺序, 1=随机排序, 2=随机抽取 */
    private Integer shuffleMode;
    /** 随机抽取选择题数量 */
    private Integer randomChoiceCount;
    /** 随机抽取判断题数量 */
    private Integer randomJudgmentCount;

    // --- LessonDetailVo 自己特有的字段 ---
    /** 课程包含的题目列表（包含题干等完整信息） */
    private List<BizLessonQuestionDetailVo> questions;

    /** 课程指派的班级编号列表 */
    private List<String> assignedClassCodes;

    /** 当前年级下所有可选的班级列表 */
    private List<String> allClassesInGrade;

    // --- 所有字段的 Getter and Setter ---

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

    public Long getGrade() {
        return grade;
    }

    public void setGrade(Long grade) {
        this.grade = grade;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Integer getLessonNum() {
        return lessonNum;
    }

    public void setLessonNum(Integer lessonNum) {
        this.lessonNum = lessonNum;
    }

    public Integer getShuffleMode() {
        return shuffleMode;
    }

    public void setShuffleMode(Integer shuffleMode) {
        this.shuffleMode = shuffleMode;
    }

    public Integer getRandomChoiceCount() {
        return randomChoiceCount;
    }

    public void setRandomChoiceCount(Integer randomChoiceCount) {
        this.randomChoiceCount = randomChoiceCount;
    }

    public Integer getRandomJudgmentCount() {
        return randomJudgmentCount;
    }

    public void setRandomJudgmentCount(Integer randomJudgmentCount) {
        this.randomJudgmentCount = randomJudgmentCount;
    }

    public List<BizLessonQuestionDetailVo> getQuestions() {
        return questions;
    }

    public void setQuestions(List<BizLessonQuestionDetailVo> questions) {
        this.questions = questions;
    }

    public List<String> getAssignedClassCodes() {
        return assignedClassCodes;
    }

    public void setAssignedClassCodes(List<String> assignedClassCodes) {
        this.assignedClassCodes = assignedClassCodes;
    }

    public List<String> getAllClassesInGrade() {
        return allClassesInGrade;
    }

    public void setAllClassesInGrade(List<String> allClassesInGrade) {
        this.allClassesInGrade = allClassesInGrade;
    }
}
