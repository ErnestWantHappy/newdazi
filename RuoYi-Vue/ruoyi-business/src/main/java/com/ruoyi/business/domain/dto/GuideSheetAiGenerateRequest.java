package com.ruoyi.business.domain.dto;

/**
 * 教师端智能生成教学内容的请求。
 */
public class GuideSheetAiGenerateRequest
{
    private String action;
    private Integer grade;
    private Integer lessonNum;
    private String topic;
    private String input;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }
    public Integer getLessonNum() { return lessonNum; }
    public void setLessonNum(Integer lessonNum) { this.lessonNum = lessonNum; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
}
