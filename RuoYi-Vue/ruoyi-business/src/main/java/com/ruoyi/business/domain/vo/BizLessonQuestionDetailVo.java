package com.ruoyi.business.domain.vo;

import com.ruoyi.business.domain.BizLessonQuestion;

/**
 * 课程题目关联详情 - 视图对象
 * (比 BizLessonQuestion 多了题干和题型)
 */
public class BizLessonQuestionDetailVo extends BizLessonQuestion {
    private String questionContent;
    private String questionType;
    
    // 选项字段
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String answer;
    
    // 打字题字段
    private Integer typingDuration;
    private Integer wordCount;
    
    // 附件路径
    private String previewPath;
    private String filePath;  // 操作题素材文件路径

    // 错题本相关
    private String studentAnswer;
    private String analysis;

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
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

    public String getPreviewPath() {
        return previewPath;
    }

    public void setPreviewPath(String previewPath) {
        this.previewPath = previewPath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getStudentAnswer() {
        return studentAnswer;
    }

    public void setStudentAnswer(String studentAnswer) {
        this.studentAnswer = studentAnswer;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }
}
