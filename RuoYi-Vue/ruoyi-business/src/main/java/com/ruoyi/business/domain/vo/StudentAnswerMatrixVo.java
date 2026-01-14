package com.ruoyi.business.domain.vo;

import java.util.List;
import java.util.Map;

public class StudentAnswerMatrixVo {
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String className;
    
    // 列表形式存储，按题目顺序
    private List<StudentQuestionResult> results;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<StudentQuestionResult> getResults() {
        return results;
    }

    public void setResults(List<StudentQuestionResult> results) {
        this.results = results;
    }
}
