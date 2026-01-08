package com.ruoyi.business.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 操作题提交记录 - 批改用视图对象
 */
public class PracticalSubmissionVo {
    
    /** 答题记录ID */
    private Long answerId;
    
    /** 学生ID */
    private Long studentId;
    
    /** 学生姓名 */
    private String studentName;
    
    /** 学号 */
    private String studentNo;
    
    /** 班级代码 */
    private String classCode;
    
    /** 题目ID */
    private Long questionId;
    
    /** 题目内容 */
    private String questionContent;
    
    /** 学生答案（文件路径） */
    private String studentAnswer;
    
    /** PDF预览路径 */
    private String previewPath;
    
    /** 已批分数（null表示未批改） */
    private Integer score;
    
    /** 该题满分 */
    private Integer maxScore;
    
    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;
    
    /** P5: 是否已提交 */
    private Boolean submitted;

    // Getters and Setters
    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }

    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }

    public String getPreviewPath() { return previewPath; }
    public void setPreviewPath(String previewPath) { this.previewPath = previewPath; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }

    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
    
    public Boolean getSubmitted() { return submitted; }
    public void setSubmitted(Boolean submitted) { this.submitted = submitted; }
}
