package com.ruoyi.business.domain;

import java.util.List;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 课程/作业信息表对象 biz_lesson
 * * @author ruoyi
 * @date 2025-08-18
 */
public class BizLesson extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 课程ID */
    private Long lessonId;

    /** 课程/作业标题 */
    private String lessonTitle;

    /** 年级 (例如: 1, 2, 3, 4, 5, 6) */
    private Long grade;

    /** 学期 (0上册, 1下册) */
    private String semester;

    /** 第几课 (例如: 1, 2, 3) */
    private Integer lessonNum;

    /** 创建教师ID */
    private Long creatorId;

    /** 出题模式: 0=固定顺序, 1=随机排序, 2=随机抽取 */
    private Integer shuffleMode;

    /** 随机抽取选择题数量 (模式2时有效) */
    private Integer randomChoiceCount;

    /** 随机抽取判断题数量 (模式2时有效) */
    private Integer randomJudgmentCount;

    /** 课程包含的题目列表 (非数据库字段) */
    private List<BizLessonQuestion> questions;

    // --- Getter and Setter methods ---

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

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
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

    public List<BizLessonQuestion> getQuestions()
    {
        return questions;
    }

    public void setQuestions(List<BizLessonQuestion> questions)
    {
        this.questions = questions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("lessonId", getLessonId())
                .append("lessonTitle", getLessonTitle())
                .append("grade", getGrade())
                .append("semester", getSemester())
                .append("lessonNum", getLessonNum())
                .append("creatorId", getCreatorId())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("questions", getQuestions()) // 补充 questions
                .toString();
    }
}
