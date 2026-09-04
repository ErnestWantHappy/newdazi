package com.ruoyi.business.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/** 教师工具分类。 */
public class BizTeacherToolCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private String description;
    private String icon;
    private String sectionLevel;
    private String defaultExpanded;
    private Integer previewLimit;
    private Integer sortOrder;
    private String status;
    private String delFlag;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getSectionLevel() { return sectionLevel; }
    public void setSectionLevel(String sectionLevel) { this.sectionLevel = sectionLevel; }
    public String getDefaultExpanded() { return defaultExpanded; }
    public void setDefaultExpanded(String defaultExpanded) { this.defaultExpanded = defaultExpanded; }
    public Integer getPreviewLimit() { return previewLimit; }
    public void setPreviewLimit(Integer previewLimit) { this.previewLimit = previewLimit; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
