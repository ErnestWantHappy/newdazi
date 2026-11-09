package com.ruoyi.business.domain.vo;

import java.io.Serializable;

/** 检索别名：term 为教师常用说法，toolId 为对应工具，供搜索按同一维护位置命中。 */
public class TeacherToolTermVo implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long termId;
    private String term;
    private Long toolId;
    private Integer sortOrder;

    public Long getTermId() { return termId; }
    public void setTermId(Long termId) { this.termId = termId; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
