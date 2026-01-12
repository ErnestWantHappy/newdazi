package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 学生答题记录实体
 * 
 * @author zdx
 * @date 2025-12-30
 */
public class BizStudentAnswer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long answerId;

    /** 学生ID */
    private Long studentId;

    /** 课程ID */
    private Long lessonId;

    /** 题目ID */
    private Long questionId;

    /** 学生答案 */
    private String studentAnswer;

    /** 是否正确 */
    private Boolean isCorrect;

    /** 得分 */
    private Integer score;

    /** 答题时间(秒) */
    private Integer answerTime;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    /** 打字速度(字符/分钟) */
    private Integer typingSpeed;

    /** 正确率(%) */
    private Double accuracyRate;

    /** 完成率(%) */
    private Double completionRate;

    /** 预览状态：pending/converting/success/failed */
    private String previewStatus;

    /** 预览文件路径 (PDF) */
    private String previewPath;

    // --- 非数据库字段，用于查询展示 ---
    private String studentName;
    private String studentNo;
    private String classCode;

    // Getters and Setters
    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getAnswerTime() { return answerTime; }
    public void setAnswerTime(Integer answerTime) { this.answerTime = answerTime; }

    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public Integer getTypingSpeed() { return typingSpeed; }
    public void setTypingSpeed(Integer typingSpeed) { this.typingSpeed = typingSpeed; }

    public Double getAccuracyRate() { return accuracyRate; }
    public void setAccuracyRate(Double accuracyRate) { this.accuracyRate = accuracyRate; }

    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }

    public String getPreviewStatus() { return previewStatus; }
    public void setPreviewStatus(String previewStatus) { this.previewStatus = previewStatus; }

    public String getPreviewPath() { return previewPath; }
    public void setPreviewPath(String previewPath) { this.previewPath = previewPath; }
}
