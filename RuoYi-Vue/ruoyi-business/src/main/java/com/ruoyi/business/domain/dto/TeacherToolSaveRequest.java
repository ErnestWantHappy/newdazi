package com.ruoyi.business.domain.dto;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/** 教师工具保存请求。 */
public class TeacherToolSaveRequest
{
    @NotBlank(message = "工具名称不能为空")
    @Size(max = 100, message = "工具名称不能超过100个字符")
    private String title;
    @NotBlank(message = "工具说明不能为空")
    @Size(max = 500, message = "工具说明不能超过500个字符")
    private String description;
    @NotBlank(message = "工具地址不能为空")
    @Size(max = 1000, message = "工具地址不能超过1000个字符")
    private String url;
    @Size(max = 1000, message = "图标地址不能超过1000个字符")
    private String iconUrl;
    @Size(max = 500, message = "标签不能超过500个字符")
    private String tags;
    private String accessType;
    private String sourceType;
    @Size(max = 200, message = "来源标识不能超过200个字符")
    private String sourceRef;
    private String isRecommended;
    @Min(value = 0, message = "推荐排序不能小于0")
    private Integer recommendOrder;
    @Min(value = 0, message = "工具排序不能小于0")
    private Integer sortOrder;
    private String status;
    @NotEmpty(message = "请至少选择一个分类")
    private List<Long> categoryIds = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getAccessType() { return accessType; }
    public void setAccessType(String accessType) { this.accessType = accessType; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceRef() { return sourceRef; }
    public void setSourceRef(String sourceRef) { this.sourceRef = sourceRef; }
    public String getIsRecommended() { return isRecommended; }
    public void setIsRecommended(String isRecommended) { this.isRecommended = isRecommended; }
    public Integer getRecommendOrder() { return recommendOrder; }
    public void setRecommendOrder(Integer recommendOrder) { this.recommendOrder = recommendOrder; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Long> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<Long> categoryIds) { this.categoryIds = categoryIds; }
}
