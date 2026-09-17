package com.ruoyi.business.domain.vo;

import com.ruoyi.business.domain.BizResearchTopic;

/** 主题页面对象。 */
public class ResearchTopicVo extends BizResearchTopic
{
    private static final long serialVersionUID = 1L;
    private String creatorName;
    private String creatorUserName;
    private String deptName;
    private Boolean edited;
    private Boolean owner;

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public String getCreatorUserName() { return creatorUserName; }
    public void setCreatorUserName(String creatorUserName) { this.creatorUserName = creatorUserName; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Boolean getEdited() { return edited; }
    public void setEdited(Boolean edited) { this.edited = edited; }
    public Boolean getOwner() { return owner; }
    public void setOwner(Boolean owner) { this.owner = owner; }
}
