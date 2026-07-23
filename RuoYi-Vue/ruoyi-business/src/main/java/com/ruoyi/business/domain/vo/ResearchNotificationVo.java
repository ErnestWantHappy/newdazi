package com.ruoyi.business.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/** 当前登录教师的一条通知，不暴露其他接收人。 */
public class ResearchNotificationVo
{
    private Long recipientId;
    private Long topicId;
    private String topicTitle;
    private String noticeLevel;
    private String readFlag;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date activityTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date readTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date notifyTime;
    private String creatorName;

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public String getTopicTitle() { return topicTitle; }
    public void setTopicTitle(String topicTitle) { this.topicTitle = topicTitle; }
    public String getNoticeLevel() { return noticeLevel; }
    public void setNoticeLevel(String noticeLevel) { this.noticeLevel = noticeLevel; }
    public String getReadFlag() { return readFlag; }
    public void setReadFlag(String readFlag) { this.readFlag = readFlag; }
    public Date getActivityTime() { return activityTime; }
    public void setActivityTime(Date activityTime) { this.activityTime = activityTime; }
    public Date getReadTime() { return readTime; }
    public void setReadTime(Date readTime) { this.readTime = readTime; }
    public Date getNotifyTime() { return notifyTime; }
    public void setNotifyTime(Date notifyTime) { this.notifyTime = notifyTime; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
}
