package com.ruoyi.business.domain.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 公开分享留言只读视图。
 * 仅含展示必需字段：不含账号、手机号、作者 ID、内部附件。
 */
public class ResearchPublicPostVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 留言正文（保存时已过滤）。 */
    private String contentHtml;

    /** 作者展示名。 */
    private String authorName;

    /** 作者学校。 */
    private String deptName;

    private Date createTime;

    private Boolean edited;

    public String getContentHtml() { return contentHtml; }

    public void setContentHtml(String contentHtml) { this.contentHtml = contentHtml; }

    public String getAuthorName() { return authorName; }

    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getDeptName() { return deptName; }

    public void setDeptName(String deptName) { this.deptName = deptName; }

    public Date getCreateTime() { return createTime; }

    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Boolean getEdited() { return edited; }

    public void setEdited(Boolean edited) { this.edited = edited; }
}
