package com.ruoyi.business.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 统一题库表对象 biz_question (最终修正版)
 */
public class BizQuestion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long questionId;

    @Excel(name = "题目类型", readConverterExp = "choice=选择题,typing=打字题,judgment=判断题")
    private String questionType;

    @Excel(name = "题目内容")
    private String questionContent;

    @Excel(name = "年级", readConverterExp = "1=一年级,2=二年级,3=三年级,4=四年级,5=五年级,6=六年级,7=七年级,8=八年级,9=九年级")
    private Long grade;

    @Excel(name = "学期", readConverterExp = "0=上册,1=下册")
    private String semester;

    @Excel(name = "选项A")
    private String optionA;

    @Excel(name = "选项B")
    private String optionB;

    @Excel(name = "选项C")
    private String optionC;

    @Excel(name = "选项D")
    private String optionD;

    @Excel(name = "标准答案")
    private String answer;

    @Excel(name = "题目解析")
    private String analysis;

    private String filePath;

    /** 新增：预览文件路径 (由系统生成，无需Excel注解) */
    private String previewPath;

    @Excel(name = "是否公开", dictType = "sys_yes_no")
    private String isPublic;

    private Long creatorId;

    @Excel(name = "打字时长(分钟)")
    private Integer typingDuration;

    private Integer wordCount;

    // --- Getter and Setter methods ---
    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
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

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getPreviewPath() {
        return previewPath;
    }

    public void setPreviewPath(String previewPath) {
        this.previewPath = previewPath;
    }

    public String getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(String isPublic) {
        this.isPublic = isPublic;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Integer getTypingDuration() {
        return typingDuration;
    }

    public void setTypingDuration(Integer typingDuration) {
        this.typingDuration = typingDuration;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("questionId", getQuestionId())
                .append("questionType", getQuestionType())
                .append("questionContent", getQuestionContent())
                .append("grade", getGrade())
                .append("semester", getSemester())
                .append("optionA", getOptionA())
                .append("optionB", getOptionB())
                .append("optionC", getOptionC())
                .append("optionD", getOptionD())
                .append("answer", getAnswer())
                .append("analysis", getAnalysis())
                .append("filePath", getFilePath())
                .append("previewPath", getPreviewPath())
                .append("isPublic", getIsPublic())
                .append("creatorId", getCreatorId())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("typingDuration",getTypingDuration())
                .append("wordCount",getWordCount())
                .toString();
    }
}
