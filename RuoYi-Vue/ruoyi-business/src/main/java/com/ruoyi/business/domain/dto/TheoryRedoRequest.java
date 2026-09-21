package com.ruoyi.business.domain.dto;

import java.util.Map;

public class TheoryRedoRequest
{
    private Long lessonId;
    private String entryYear;
    private String classCode;
    private Map<Long, Long> studentRevisions;

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long value) { lessonId = value; }
    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String value) { entryYear = value; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String value) { classCode = value; }
    public Map<Long, Long> getStudentRevisions() { return studentRevisions; }
    public void setStudentRevisions(Map<Long, Long> value) { studentRevisions = value; }
}
