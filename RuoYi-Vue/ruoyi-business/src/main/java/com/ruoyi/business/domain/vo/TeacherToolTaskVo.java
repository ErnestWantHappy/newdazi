package com.ruoyi.business.domain.vo;

import java.io.Serializable;

/** 教师工具的教学任务快捷筛选；toolCount 按已上架且启用分类的工具去重统计。 */
public class TeacherToolTaskVo implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String taskCode;
    private String taskName;
    private String description;
    private Integer sortOrder;
    private Integer toolCount;
    private String status;
    private String delFlag;

    public String getTaskCode() { return taskCode; }
    public void setTaskCode(String taskCode) { this.taskCode = taskCode; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getToolCount() { return toolCount; }
    public void setToolCount(Integer toolCount) { this.toolCount = toolCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
