package com.ruoyi.business.domain.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;

/** 主题新增/编辑请求；正文不实现 toString，避免进入操作日志。 */
public class ResearchTopicSaveRequest
{
    @NotBlank(message = "请选择主题类型")
    private String topicType;
    @NotBlank(message = "请输入主题标题")
    @Size(max = 200, message = "主题标题不能超过200个字符")
    private String title;
    @NotBlank(message = "请输入主题正文")
    private String contentHtml;
    private String noticeLevel = "0";
    private String noticeScope = "0";
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date activityTime;
    private List<String> stageCodes = new ArrayList<>();
    private List<Long> teacherUserIds = new ArrayList<>();

    public String getTopicType() { return topicType; }
    public void setTopicType(String topicType) { this.topicType = topicType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContentHtml() { return contentHtml; }
    public void setContentHtml(String contentHtml) { this.contentHtml = contentHtml; }
    public String getNoticeLevel() { return noticeLevel; }
    public void setNoticeLevel(String noticeLevel) { this.noticeLevel = noticeLevel; }
    public String getNoticeScope() { return noticeScope; }
    public void setNoticeScope(String noticeScope) { this.noticeScope = noticeScope; }
    public Date getActivityTime() { return activityTime; }
    public void setActivityTime(Date activityTime) { this.activityTime = activityTime; }
    public List<String> getStageCodes() { return stageCodes; }
    public void setStageCodes(List<String> stageCodes) { this.stageCodes = stageCodes; }
    public List<Long> getTeacherUserIds() { return teacherUserIds; }
    public void setTeacherUserIds(List<Long> teacherUserIds) { this.teacherUserIds = teacherUserIds; }
}
