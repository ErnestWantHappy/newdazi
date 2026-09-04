package com.ruoyi.business.domain.vo;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.business.domain.BizResearchPost;

/** 留言及课程资源搜索结果对象。 */
public class ResearchPostVo extends BizResearchPost
{
    private static final long serialVersionUID = 1L;
    private String authorName;
    private String authorUserName;
    private String deptName;
    private String topicTitle;
    private Boolean edited;
    private Boolean owner;
    private List<ResearchResourceVo> resources = new ArrayList<>();

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getAuthorUserName() { return authorUserName; }
    public void setAuthorUserName(String authorUserName) { this.authorUserName = authorUserName; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getTopicTitle() { return topicTitle; }
    public void setTopicTitle(String topicTitle) { this.topicTitle = topicTitle; }
    public Boolean getEdited() { return edited; }
    public void setEdited(Boolean edited) { this.edited = edited; }
    public Boolean getOwner() { return owner; }
    public void setOwner(Boolean owner) { this.owner = owner; }
    public List<ResearchResourceVo> getResources() { return resources; }
    public void setResources(List<ResearchResourceVo> resources) { this.resources = resources; }
}
