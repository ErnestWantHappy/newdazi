package com.ruoyi.business.domain.vo;

import java.util.ArrayList;
import java.util.List;

/** 教师首页顶部通知摘要。 */
public class ResearchNotificationSummaryVo
{
    private long unreadCount;
    private List<ResearchNotificationVo> items = new ArrayList<>();

    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
    public List<ResearchNotificationVo> getItems() { return items; }
    public void setItems(List<ResearchNotificationVo> items) { this.items = items; }
}
