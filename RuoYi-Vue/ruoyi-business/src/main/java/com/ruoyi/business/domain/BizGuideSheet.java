package com.ruoyi.business.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 电子导学单模板。
 */
public class BizGuideSheet extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long sheetId;
    private String sheetTitle;
    private Integer grade;
    private String semester;
    private Integer lessonNum;
    private Long creatorId;
    private Long deptId;
    private Long countyDeptId;
    private String formJson;
    private Integer versionNo;
    private String delFlag;
    private Integer maxPages;
    private String teacherMachineIp;
    private String isPublic;
    private String creatorName;

    public Long getSheetId() { return sheetId; }
    public void setSheetId(Long sheetId) { this.sheetId = sheetId; }
    public String getSheetTitle() { return sheetTitle; }
    public void setSheetTitle(String sheetTitle) { this.sheetTitle = sheetTitle; }
    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public Integer getLessonNum() { return lessonNum; }
    public void setLessonNum(Integer lessonNum) { this.lessonNum = lessonNum; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Long getCountyDeptId() { return countyDeptId; }
    public void setCountyDeptId(Long countyDeptId) { this.countyDeptId = countyDeptId; }
    public String getFormJson() { return formJson; }
    public void setFormJson(String formJson) { this.formJson = formJson; }
    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public Integer getMaxPages() { return maxPages; }
    public void setMaxPages(Integer maxPages) { this.maxPages = maxPages; }
    public String getTeacherMachineIp() { return teacherMachineIp; }
    public void setTeacherMachineIp(String teacherMachineIp) { this.teacherMachineIp = teacherMachineIp; }
    public String getIsPublic() { return isPublic; }
    public void setIsPublic(String isPublic) { this.isPublic = isPublic; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
}
