package com.ruoyi.business.domain.vo;

/**
 * 课程排行榜VO
 * 
 * @author zdx
 * @date 2025-12-30
 */
public class LessonRankingVo {
    
    /** 排名 */
    private Integer ranking;
    
    /** 学生ID */
    private Long studentId;
    
    /** 学生姓名 */
    private String studentName;
    
    /** 学号 */
    private String studentNo;
    
    /** 班级 */
    private String classCode;
    
    /** 总分 */
    private Integer totalScore;
    
    /** 答题时间(秒) */
    private Integer totalTime;
    
    /** 正确题数 */
    private Integer correctCount;
    
    /** 总题数 */
    private Integer totalCount;

    // Getters and Setters
    public Integer getRanking() { return ranking; }
    public void setRanking(Integer ranking) { this.ranking = ranking; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public Integer getTotalTime() { return totalTime; }
    public void setTotalTime(Integer totalTime) { this.totalTime = totalTime; }

    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
}
