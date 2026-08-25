package com.ruoyi.business.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ruoyi.business.domain.BizLessonGuideSheetBinding;
import com.ruoyi.business.domain.BizLessonTool;
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
    /** 课程永久所属入学年份，新建由教师首页分组显式传入，编辑时原值回显。 */
    private String entryYear;
    private String semester;
    private Integer lessonNum;
    
    /** 出题模式: 0=固定顺序, 1=随机排序, 2=随机抽取 */
    private Integer shuffleMode;
    /** 随机抽取选择题数量 */
    private Integer randomChoiceCount;
    /** 随机抽取判断题数量 */
    private Integer randomJudgmentCount;

    /** 课程用途：assessment / attendance */
    private String lessonMode;

    /** 教师课堂说明（学生可见） */
    private String teacherNote;

    /** 是否开启自动推进下一课（测评课） */
    private Boolean autoAdvanceEnabled;

    /** 有成绩人数占比阈值（%） */
    private Integer autoAdvanceThresholdPct;

    /** 达标后延迟小时数 */
    private java.math.BigDecimal autoAdvanceDelayHours;

    /** 课程级物联网实验开关（考勤课强制关闭） */
    private Boolean iotEnabled;

    /** 关闭时保留当前绑定和历史答卷，仅停止学生访问。 */
    private Boolean guideSheetEnabled;

    /** 保存课程时选择的来源模板ID。 */
    private Long sourceSheetId;

    /** 只有明确选择不同模板时才允许生成新快照。 */
    private Boolean guideSheetReplaceRequested;

    /** 查询课程时返回当前绑定及其不可变快照。 */
    private BizLessonGuideSheetBinding guideSheetBinding;

    // --- LessonDetailVo 自己特有的字段 ---
    /** 课程包含的题目列表（包含题干等完整信息） */
    private List<BizLessonQuestionDetailVo> questions;

    /** 课程指派的班级编号列表 */
    private List<String> assignedClassCodes;

    /** 新指派班级的理论题初始开放状态；已有班级状态不因编辑课程被覆盖。 */
    private Boolean initialTheoryOpen;

    /** 新指派班级的操作题初始开放状态；已有班级状态不因编辑课程被覆盖。 */
    private Boolean initialPracticalOpen;

    /** 本节课工具（学生端实验工具面板，随课程保存） */
    private List<BizLessonTool> lessonTools;

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

    public String getEntryYear() {
        return entryYear;
    }

    public void setEntryYear(String entryYear) {
        this.entryYear = entryYear;
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

    public String getLessonMode() {
        return lessonMode;
    }

    public void setLessonMode(String lessonMode) {
        this.lessonMode = lessonMode;
    }

    public String getTeacherNote() {
        return teacherNote;
    }

    public void setTeacherNote(String teacherNote) {
        this.teacherNote = teacherNote;
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

    public Boolean getIotEnabled() {
        return iotEnabled;
    }

    public void setIotEnabled(Boolean iotEnabled) {
        this.iotEnabled = iotEnabled;
    }

    public Boolean getGuideSheetEnabled() {
        return guideSheetEnabled;
    }

    public void setGuideSheetEnabled(Boolean guideSheetEnabled) {
        this.guideSheetEnabled = guideSheetEnabled;
    }

    public Long getSourceSheetId() {
        return sourceSheetId;
    }

    public void setSourceSheetId(Long sourceSheetId) {
        this.sourceSheetId = sourceSheetId;
    }

    @JsonProperty("guideSheetSourceSheetId")
    public Long getGuideSheetSourceSheetId() {
        return sourceSheetId;
    }

    @JsonProperty("guideSheetSourceSheetId")
    public void setGuideSheetSourceSheetId(Long guideSheetSourceSheetId) {
        this.sourceSheetId = guideSheetSourceSheetId;
    }

    public Boolean getGuideSheetReplaceRequested() {
        return guideSheetReplaceRequested;
    }

    public void setGuideSheetReplaceRequested(Boolean guideSheetReplaceRequested) {
        this.guideSheetReplaceRequested = guideSheetReplaceRequested;
    }

    public BizLessonGuideSheetBinding getGuideSheetBinding() {
        return guideSheetBinding;
    }

    public void setGuideSheetBinding(BizLessonGuideSheetBinding guideSheetBinding) {
        this.guideSheetBinding = guideSheetBinding;
    }

    @JsonProperty("currentGuideSheetBinding")
    public BizLessonGuideSheetBinding getCurrentGuideSheetBinding() {
        return guideSheetBinding;
    }

    @JsonProperty("currentGuideSheetBinding")
    public void setCurrentGuideSheetBinding(BizLessonGuideSheetBinding currentGuideSheetBinding) {
        this.guideSheetBinding = currentGuideSheetBinding;
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

    public Boolean getInitialTheoryOpen() {
        return initialTheoryOpen;
    }

    public void setInitialTheoryOpen(Boolean initialTheoryOpen) {
        this.initialTheoryOpen = initialTheoryOpen;
    }

    public Boolean getInitialPracticalOpen() {
        return initialPracticalOpen;
    }

    public void setInitialPracticalOpen(Boolean initialPracticalOpen) {
        this.initialPracticalOpen = initialPracticalOpen;
    }

    public List<BizLessonTool> getLessonTools() {
        return lessonTools;
    }

    public void setLessonTools(List<BizLessonTool> lessonTools) {
        this.lessonTools = lessonTools;
    }

    public List<String> getAllClassesInGrade() {
        return allClassesInGrade;
    }

    public void setAllClassesInGrade(List<String> allClassesInGrade) {
        this.allClassesInGrade = allClassesInGrade;
    }
}
