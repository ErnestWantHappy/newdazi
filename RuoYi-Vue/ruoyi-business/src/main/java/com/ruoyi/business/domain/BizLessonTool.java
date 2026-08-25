package com.ruoyi.business.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 学生本节课工具对象 biz_lesson_tool（随课程走）
 *
 * @author system
 * @date 2026-08-22
 */
public class BizLessonTool extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long toolId;

    private Long lessonId;

    /** 工具名称 */
    private String toolName;

    /** 工具网址 */
    private String toolUrl;

    /** 排序（小在前） */
    private Integer sortOrder;

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public String getToolUrl() { return toolUrl; }
    public void setToolUrl(String toolUrl) { this.toolUrl = toolUrl; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
