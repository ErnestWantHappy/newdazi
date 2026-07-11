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
    private String isPublic;

    /** 列表展示用：创建人姓名（来自sys_user.nick_name） */
    private String creatorName;
    /** 列表展示用：已提交人数 */
    private Integer submittedCount;
    /** 列表展示用：总分配学生数 */
    private Integer totalAssigned;
    /** 列表展示用：平均分 */
    private Double avgScore;
    /** 列表展示用：完成率（百分比） */
    private Double completionRate;
    /** 列表展示用：正确率（百分比） */
    private Double accuracyRate;

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

    public String getIsPublic() { return isPublic; }
    public void setIsPublic(String isPublic) { this.isPublic = isPublic; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public Integer getSubmittedCount() { return submittedCount; }
    public void setSubmittedCount(Integer submittedCount) { this.submittedCount = submittedCount; }

    public Integer getTotalAssigned() { return totalAssigned; }
    public void setTotalAssigned(Integer totalAssigned) { this.totalAssigned = totalAssigned; }

    public Double getAvgScore() { return avgScore; }
    public void setAvgScore(Double avgScore) { this.avgScore = avgScore; }

    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }

    public Double getAccuracyRate() { return accuracyRate; }
    public void setAccuracyRate(Double accuracyRate) { this.accuracyRate = accuracyRate; }

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
