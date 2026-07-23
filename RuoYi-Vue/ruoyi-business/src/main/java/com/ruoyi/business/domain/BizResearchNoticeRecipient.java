package com.ruoyi.business.domain;

import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/** 教研活动通知接收人账号快照。 */
public class BizResearchNoticeRecipient extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long recipientId;
    private Long topicId;
    private Long userId;
    private String sourceType;
    private String sourceValue;
    private String noticeLevel;
    private String readFlag;
    private Date readTime;
    private Date notifyTime;

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceValue() { return sourceValue; }
    public void setSourceValue(String sourceValue) { this.sourceValue = sourceValue; }
    public String getNoticeLevel() { return noticeLevel; }
    public void setNoticeLevel(String noticeLevel) { this.noticeLevel = noticeLevel; }
    public String getReadFlag() { return readFlag; }
    public void setReadFlag(String readFlag) { this.readFlag = readFlag; }
    public Date getReadTime() { return readTime; }
    public void setReadTime(Date readTime) { this.readTime = readTime; }
    public Date getNotifyTime() { return notifyTime; }
    public void setNotifyTime(Date notifyTime) { this.notifyTime = notifyTime; }
}
