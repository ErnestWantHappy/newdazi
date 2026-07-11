package com.ruoyi.business.domain;

import java.util.Date;

/**
 * 县考答题记录表 biz_county_exam_answer
 * 
 * @author ruoyi
 */
public class CountyExamAnswer {
    private static final long serialVersionUID = 1L;

    /** 答题记录ID */
    private Long answerId;

    /** 县考ID */
    private Long examId;

    /** 学生ID */
    private Long studentId;

    /** 题目ID */
    private Long questionId;

    /** 学生答案 */
    private String studentAnswer;

    /** 得分 */
    private Integer score;

    /** 是否正确 */
    private Integer isCorrect;

    /** 评卷人ID */
    private Long graderId;

    /** 评卷状态(0待评/1已评) */
    private String gradingStatus;

    /** 答题用时(秒) */
    private Integer answerTime;

    /** 文件路径 */
    private String filePath;

    /** 提交时间 */
    private Date submitTime;

    /** 打字速度 */
    private Integer typingSpeed;

    /** 正确率 */
    private Double accuracyRate;

    /** 完成率 */
    private Double completionRate;

    /** 预览状态 */
    private String previewStatus;

    /** 预览路径 */
    private String previewPath;

    /** 重试次数 */
    private Integer previewRetryCount;

    /** 最近重试时间 */
    private Date previewLastRetryTime;

    /** 预览错误信息 */
    private String previewErrorMessage;

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getStudentAnswer() {
        return studentAnswer;
    }

    public void setStudentAnswer(String studentAnswer) {
        this.studentAnswer = studentAnswer;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Integer isCorrect) {
        this.isCorrect = isCorrect;
    }

    public Long getGraderId() {
        return graderId;
    }

    public void setGraderId(Long graderId) {
        this.graderId = graderId;
    }

    public String getGradingStatus() {
        return gradingStatus;
    }

    public void setGradingStatus(String gradingStatus) {
        this.gradingStatus = gradingStatus;
    }

    public Integer getAnswerTime() {
        return answerTime;
    }

    public void setAnswerTime(Integer answerTime) {
        this.answerTime = answerTime;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public Integer getTypingSpeed() {
        return typingSpeed;
    }

    public void setTypingSpeed(Integer typingSpeed) {
        this.typingSpeed = typingSpeed;
    }

    public Double getAccuracyRate() {
        return accuracyRate;
    }

    public void setAccuracyRate(Double accuracyRate) {
        this.accuracyRate = accuracyRate;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public String getPreviewStatus() {
        return previewStatus;
    }

    public void setPreviewStatus(String previewStatus) {
        this.previewStatus = previewStatus;
    }

    public String getPreviewPath() {
        return previewPath;
    }

    public void setPreviewPath(String previewPath) {
        this.previewPath = previewPath;
    }

    public Integer getPreviewRetryCount() {
        return previewRetryCount;
    }

    public void setPreviewRetryCount(Integer previewRetryCount) {
        this.previewRetryCount = previewRetryCount;
    }

    public Date getPreviewLastRetryTime() {
        return previewLastRetryTime;
    }

    public void setPreviewLastRetryTime(Date previewLastRetryTime) {
        this.previewLastRetryTime = previewLastRetryTime;
    }

    public String getPreviewErrorMessage() {
        return previewErrorMessage;
    }

    public void setPreviewErrorMessage(String previewErrorMessage) {
        this.previewErrorMessage = previewErrorMessage;
    }
}
