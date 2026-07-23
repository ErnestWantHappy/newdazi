package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/** 教研活动主题。 */
public class BizResearchTopic extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long topicId;
    private String topicType;
    private String title;
    private String contentHtml;
    private String contentText;
    private String noticeLevel;
    private String noticeScope;
    private String noticeStages;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date activityTime;
    private String isPinned;
    private Long viewCount;
    private Long replyCount;
    private Long downloadCount;
    private Date lastActivityTime;
    private Long creatorId;
    private Long deptId;
    private String delFlag;

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public String getTopicType() { return topicType; }
    public void setTopicType(String topicType) { this.topicType = topicType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContentHtml() { return contentHtml; }
    public void setContentHtml(String contentHtml) { this.contentHtml = contentHtml; }
    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public String getNoticeLevel() { return noticeLevel; }
    public void setNoticeLevel(String noticeLevel) { this.noticeLevel = noticeLevel; }
    public String getNoticeScope() { return noticeScope; }
    public void setNoticeScope(String noticeScope) { this.noticeScope = noticeScope; }
    public String getNoticeStages() { return noticeStages; }
    public void setNoticeStages(String noticeStages) { this.noticeStages = noticeStages; }
    public Date getActivityTime() { return activityTime; }
    public void setActivityTime(Date activityTime) { this.activityTime = activityTime; }
    public String getIsPinned() { return isPinned; }
    public void setIsPinned(String isPinned) { this.isPinned = isPinned; }
    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    public Long getReplyCount() { return replyCount; }
    public void setReplyCount(Long replyCount) { this.replyCount = replyCount; }
    public Long getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Long downloadCount) { this.downloadCount = downloadCount; }
    public Date getLastActivityTime() { return lastActivityTime; }
    public void setLastActivityTime(Date lastActivityTime) { this.lastActivityTime = lastActivityTime; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
