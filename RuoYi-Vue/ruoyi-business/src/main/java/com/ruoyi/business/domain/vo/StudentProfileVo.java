package com.ruoyi.business.domain.vo;

import java.util.List;

/**
 * 学生成绩画像 VO
 */
public class StudentProfileVo {
    
    /** 学生ID */
    private Long studentId;
    
    /** 学生姓名 */
    private String studentName;
    
    /** 班级编号 */
    private String classCode;
    
    /** 入学年份 */
    private String entryYear;
    
    /** 年级名称 */
    private String gradeName;
    
    /** 总课程数 */
    private Integer totalCourses;
    
    /** 总体平均分 */
    private Double averageScore;
    
    /** 课堂表现平均分（0分不参与计算） */
    private Double performanceAvg;
    
    /** 打字平均速度 */
    private Double typingSpeedAvg;
    
    /** 当前班级排名 */
    private Integer currentRank;
    
    /** 班级总人数 */
    private Integer classTotal;

    /** 备注 */
    private String remark;

    // Getters and Setters
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }

    public String getGradeName() { return gradeName; }
    public void setGradeName(String gradeName) { this.gradeName = gradeName; }

    public Integer getTotalCourses() { return totalCourses; }
    public void setTotalCourses(Integer totalCourses) { this.totalCourses = totalCourses; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

    public Double getPerformanceAvg() { return performanceAvg; }
    public void setPerformanceAvg(Double performanceAvg) { this.performanceAvg = performanceAvg; }

    public Double getTypingSpeedAvg() { return typingSpeedAvg; }
    public void setTypingSpeedAvg(Double typingSpeedAvg) { this.typingSpeedAvg = typingSpeedAvg; }

    public Integer getCurrentRank() { return currentRank; }
    public void setCurrentRank(Integer currentRank) { this.currentRank = currentRank; }

    public Integer getClassTotal() { return classTotal; }
    public void setClassTotal(Integer classTotal) { this.classTotal = classTotal; }

    /**
     * 课程成绩数据项
     */
    public static class CourseScoreItem {
        private Long lessonId;
        private String lessonTitle;
        private Integer lessonNum;
        private Double studentScore;
        private Double classAvgScore;
        private java.util.Date submitTime;

        public Long getLessonId() { return lessonId; }
        public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

        public String getLessonTitle() { return lessonTitle; }
        public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }

        public Integer getLessonNum() { return lessonNum; }
        public void setLessonNum(Integer lessonNum) { this.lessonNum = lessonNum; }

        public Double getStudentScore() { return studentScore; }
        public void setStudentScore(Double studentScore) { this.studentScore = studentScore; }

        public Double getClassAvgScore() { return classAvgScore; }
        public void setClassAvgScore(Double classAvgScore) { this.classAvgScore = classAvgScore; }

        public java.util.Date getSubmitTime() { return submitTime; }
        public void setSubmitTime(java.util.Date submitTime) { this.submitTime = submitTime; }
    }

    /**
     * 打字速度数据项
     */
    public static class TypingSpeedItem {
        private Long lessonId;
        private String lessonTitle;
        private Integer typingSpeed;
        private Integer gradeBaseline; // 年级基准
        private java.util.Date submitTime;

        public Long getLessonId() { return lessonId; }
        public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

        public String getLessonTitle() { return lessonTitle; }
        public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }

        public Integer getTypingSpeed() { return typingSpeed; }
        public void setTypingSpeed(Integer typingSpeed) { this.typingSpeed = typingSpeed; }

        public Integer getGradeBaseline() { return gradeBaseline; }
        public void setGradeBaseline(Integer gradeBaseline) { this.gradeBaseline = gradeBaseline; }

        public java.util.Date getSubmitTime() { return submitTime; }
        public void setSubmitTime(java.util.Date submitTime) { this.submitTime = submitTime; }
    }

    /**
     * 课堂表现分数据项
     */
    public static class PerformanceItem {
        private Long lessonId;
        private String lessonTitle;
        private Integer performance;
        private java.util.Date recordTime;

        public Long getLessonId() { return lessonId; }
        public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

        public String getLessonTitle() { return lessonTitle; }
        public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }

        public Integer getPerformance() { return performance; }
        public void setPerformance(Integer performance) { this.performance = performance; }

        public java.util.Date getRecordTime() { return recordTime; }
        public void setRecordTime(java.util.Date recordTime) { this.recordTime = recordTime; }
    }

    /**
     * 排名变化数据项
     */
    public static class RankItem {
        private Long lessonId;
        private String lessonTitle;
        private Integer rank;
        private Integer classTotal;
        private Double cumulativeAvg; // 截至当时的累计平均分

        public Long getLessonId() { return lessonId; }
        public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

        public String getLessonTitle() { return lessonTitle; }
        public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }

        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }

        public Integer getClassTotal() { return classTotal; }
        public void setClassTotal(Integer classTotal) { this.classTotal = classTotal; }

        public Double getCumulativeAvg() { return cumulativeAvg; }
        public void setCumulativeAvg(Double cumulativeAvg) { this.cumulativeAvg = cumulativeAvg; }
    }
}
