package com.ruoyi.business.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class BizGuideSheet extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long sheetId;
    private String sheetTitle;
    private Long lessonId;
    private Long creatorId;
    private Long deptId;
    private String formJson;
    private String status;
    private Integer maxPages;
    private String teacherMachineIp;

    public Long getSheetId() {
        return sheetId;
    }

    public void setSheetId(Long sheetId) {
        this.sheetId = sheetId;
    }

    public String getSheetTitle() {
        return sheetTitle;
    }

    public void setSheetTitle(String sheetTitle) {
        this.sheetTitle = sheetTitle;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getFormJson() {
        return formJson;
    }

    public void setFormJson(String formJson) {
        this.formJson = formJson;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getMaxPages() {
        return maxPages;
    }

    public void setMaxPages(Integer maxPages) {
        this.maxPages = maxPages;
    }

    public String getTeacherMachineIp() {
        return teacherMachineIp;
    }

    public void setTeacherMachineIp(String teacherMachineIp) {
        this.teacherMachineIp = teacherMachineIp;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("sheetId", getSheetId())
                .append("sheetTitle", getSheetTitle())
                .append("lessonId", getLessonId())
                .append("creatorId", getCreatorId())
                .append("deptId", getDeptId())
                .append("status", getStatus())
                .append("maxPages", getMaxPages())
                .append("teacherMachineIp", getTeacherMachineIp())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
