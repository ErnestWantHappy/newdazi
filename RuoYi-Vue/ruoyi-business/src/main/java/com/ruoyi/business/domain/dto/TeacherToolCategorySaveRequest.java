package com.ruoyi.business.domain.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/** 教师工具分类保存请求。 */
public class TeacherToolCategorySaveRequest
{
    @NotBlank(message = "分类编码不能为空")
    @Pattern(regexp = "[a-z][a-z0-9-]{1,31}", message = "分类编码应为2至32位小写字母、数字或短横线")
    private String categoryCode;
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过50个字符")
    private String categoryName;
    @Size(max = 200, message = "分类说明不能超过200个字符")
    private String description;
    @Size(max = 50, message = "图标名称不能超过50个字符")
    private String icon;
    @NotBlank(message = "请选择分类层级")
    private String sectionLevel;
    private String defaultExpanded;
    @Min(value = 1, message = "预览数量不能小于1")
    @Max(value = 20, message = "预览数量不能超过20")
    private Integer previewLimit;
    @Min(value = 0, message = "排序不能小于0")
    private Integer sortOrder;
    private String status;

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
}
