package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class BizGuideSheetProgress extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long sheetId;
    private Long studentId;
    private String classCode;
    private Integer currentPage;
    private String isSubmitted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastHeartbeat;

    /** 进度详情JSON */
    private String progressDetail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSheetId() {
        return sheetId;
    }

    public void setSheetId(Long sheetId) {
        this.sheetId = sheetId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public String getIsSubmitted() {
        return isSubmitted;
    }

    public void setIsSubmitted(String isSubmitted) {
        this.isSubmitted = isSubmitted;
    }

    public Date getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Date lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public String getProgressDetail() { return progressDetail; }
    public void setProgressDetail(String progressDetail) { this.progressDetail = progressDetail; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("sheetId", getSheetId())
                .append("studentId", getStudentId())
                .append("classCode", getClassCode())
                .append("currentPage", getCurrentPage())
                .append("isSubmitted", getIsSubmitted())
                .append("lastHeartbeat", getLastHeartbeat())
                .toString();
    }
}
