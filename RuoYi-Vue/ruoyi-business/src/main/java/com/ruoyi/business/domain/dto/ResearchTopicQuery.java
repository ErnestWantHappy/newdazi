package com.ruoyi.business.domain.dto;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/** 活动主题检索条件。 */
public class ResearchTopicQuery
{
    private String keyword;
    private String keywordLike;
    private String topicType;
    private Long authorId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date activityBeginTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date activityEndTime;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getKeywordLike() { return keywordLike; }
    public void setKeywordLike(String keywordLike) { this.keywordLike = keywordLike; }
    public String getTopicType() { return topicType; }
    public void setTopicType(String topicType) { this.topicType = topicType; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public Date getBeginTime() { return beginTime; }
    public void setBeginTime(Date beginTime) { this.beginTime = beginTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public Date getActivityBeginTime() { return activityBeginTime; }
    public void setActivityBeginTime(Date activityBeginTime) { this.activityBeginTime = activityBeginTime; }
    public Date getActivityEndTime() { return activityEndTime; }
    public void setActivityEndTime(Date activityEndTime) { this.activityEndTime = activityEndTime; }
}
