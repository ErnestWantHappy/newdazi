package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 学生常驻工具对象 biz_student_tool
 *
 * @author system
 * @date 2026-08-22
 */
public class BizStudentTool extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long toolId;

    /** 工具名称（学生端显示） */
    private String toolName;

    /** 工具网址 */
    private String toolUrl;

    /** 简要说明（可选） */
    private String toolDesc;

    /** 排序（小在前） */
    private Integer sortOrder;

    /** 启用 1=启用 0=停用 */
    private Integer enabled;

    /** 学校ID（数据隔离，空=平台级） */
    private Long deptId;

    /** 适用范围（非表字段，教师端提交/展示用） */
    private String scopeText;

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public String getToolUrl() { return toolUrl; }
    public void setToolUrl(String toolUrl) { this.toolUrl = toolUrl; }
    public String getToolDesc() { return toolDesc; }
    public void setToolDesc(String toolDesc) { this.toolDesc = toolDesc; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getScopeText() { return scopeText; }
    public void setScopeText(String scopeText) { this.scopeText = scopeText; }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("toolId", getToolId())
            .append("toolName", getToolName())
            .append("toolUrl", getToolUrl())
            .append("sortOrder", getSortOrder())
            .append("enabled", getEnabled())
            .append("deptId", getDeptId())
            .toString();
    }
}
