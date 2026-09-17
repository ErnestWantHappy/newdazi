package com.ruoyi.business.domain;

import java.util.Date;

public class ProgrammingDraft {
    private Long draftId; private Long studentId; private Long lessonId; private Long questionId; private String sourceCode; private Date updateTime;
    public Long getDraftId() { return draftId; } public void setDraftId(Long v) { draftId = v; }
    public Long getStudentId() { return studentId; } public void setStudentId(Long v) { studentId = v; }
    public Long getLessonId() { return lessonId; } public void setLessonId(Long v) { lessonId = v; }
    public Long getQuestionId() { return questionId; } public void setQuestionId(Long v) { questionId = v; }
    public String getSourceCode() { return sourceCode; } public void setSourceCode(String v) { sourceCode = v; }
    public Date getUpdateTime() { return updateTime; } public void setUpdateTime(Date v) { updateTime = v; }
}
