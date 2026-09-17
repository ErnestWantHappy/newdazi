package com.ruoyi.business.domain.dto;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;

/** 首次或再次发送通知请求。 */
public class ResearchNotificationSendRequest
{
    /** 兼容既有接口字段；当前所有活动通知统一按站内通知处理。 */
    private String noticeLevel = "1";
    @NotBlank(message = "请选择通知范围")
    private String noticeScope;
    private List<String> stageCodes = new ArrayList<>();
    private List<Long> teacherUserIds = new ArrayList<>();

    public String getNoticeLevel() { return noticeLevel; }
    public void setNoticeLevel(String noticeLevel) { this.noticeLevel = noticeLevel; }
    public String getNoticeScope() { return noticeScope; }
    public void setNoticeScope(String noticeScope) { this.noticeScope = noticeScope; }
    public List<String> getStageCodes() { return stageCodes; }
    public void setStageCodes(List<String> stageCodes) { this.stageCodes = stageCodes; }
    public List<Long> getTeacherUserIds() { return teacherUserIds; }
    public void setTeacherUserIds(List<Long> teacherUserIds) { this.teacherUserIds = teacherUserIds; }
}
