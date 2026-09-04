package com.ruoyi.business.domain;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.common.core.domain.BaseEntity;

/** 教师工具导航项。 */
public class BizTeacherTool extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long toolId;
    private String title;
    private String description;
    private String url;
    private String iconUrl;
    private String tags;
    private String accessType;
    private String sourceType;
    private String sourceRef;
    private String isRecommended;
    private Integer recommendOrder;
    private Integer sortOrder;
    private String status;
    private String delFlag;
    private List<Long> categoryIds = new ArrayList<>();
    private List<String> categoryNames = new ArrayList<>();

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
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
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<Long> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<Long> categoryIds) { this.categoryIds = categoryIds; }
    public List<String> getCategoryNames() { return categoryNames; }
    public void setCategoryNames(List<String> categoryNames) { this.categoryNames = categoryNames; }
}
