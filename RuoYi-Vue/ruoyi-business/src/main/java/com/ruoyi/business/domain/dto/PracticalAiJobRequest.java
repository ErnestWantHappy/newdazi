package com.ruoyi.business.domain.dto;

/** 创建班级操作题 AI 建议任务。 */
public class PracticalAiJobRequest
{
    private Long lessonId;
    private Long questionId;
    private String entryYear;
    private String classCode;
    private String scopeMode;
    public Long getLessonId() { return lessonId; } public void setLessonId(Long v) { lessonId=v; }
    public Long getQuestionId() { return questionId; } public void setQuestionId(Long v) { questionId=v; }
    public String getEntryYear() { return entryYear; } public void setEntryYear(String v) { entryYear=v; }
    public String getClassCode() { return classCode; } public void setClassCode(String v) { classCode=v; }
    public String getScopeMode() { return scopeMode; } public void setScopeMode(String v) { scopeMode=v; }
}
