package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizGuideSheetProgress extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long bindingId;
    private Long studentId;
    private Long deptId;
    private String entryYear;
    private String classCode;
    private Integer currentPage;
    private String isSubmitted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastHeartbeat;

    private String progressDetail;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBindingId() { return bindingId; }
    public void setBindingId(Long bindingId) { this.bindingId = bindingId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
    public Integer getCurrentPage() { return currentPage; }
    public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }
    public String getIsSubmitted() { return isSubmitted; }
    public void setIsSubmitted(String isSubmitted) { this.isSubmitted = isSubmitted; }
    public Date getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(Date lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    public String getProgressDetail() { return progressDetail; }
    public void setProgressDetail(String progressDetail) { this.progressDetail = progressDetail; }
}
