package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 课程导学单不可变快照绑定。
 */
public class BizLessonGuideSheetBinding extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long bindingId;
    private Long lessonId;
    private Long sourceSheetId;
    private Integer sourceVersion;
    private String snapshotTitle;
    private Integer snapshotGrade;
    private String snapshotSemester;
    private Integer snapshotLessonNum;
    private String snapshotFormJson;
    private Integer snapshotMaxPages;
    private String snapshotTeacherMachineIp;
    private String isCurrent;
    private String enabled;
    private Long creatorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date archivedTime;

    public Long getBindingId() { return bindingId; }
    public void setBindingId(Long bindingId) { this.bindingId = bindingId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getSourceSheetId() { return sourceSheetId; }
    public void setSourceSheetId(Long sourceSheetId) { this.sourceSheetId = sourceSheetId; }
    public Integer getSourceVersion() { return sourceVersion; }
    public void setSourceVersion(Integer sourceVersion) { this.sourceVersion = sourceVersion; }
    public String getSnapshotTitle() { return snapshotTitle; }
    public void setSnapshotTitle(String snapshotTitle) { this.snapshotTitle = snapshotTitle; }
    public Integer getSnapshotGrade() { return snapshotGrade; }
    public void setSnapshotGrade(Integer snapshotGrade) { this.snapshotGrade = snapshotGrade; }
    public String getSnapshotSemester() { return snapshotSemester; }
    public void setSnapshotSemester(String snapshotSemester) { this.snapshotSemester = snapshotSemester; }
    public Integer getSnapshotLessonNum() { return snapshotLessonNum; }
    public void setSnapshotLessonNum(Integer snapshotLessonNum) { this.snapshotLessonNum = snapshotLessonNum; }
    public String getSnapshotFormJson() { return snapshotFormJson; }
    public void setSnapshotFormJson(String snapshotFormJson) { this.snapshotFormJson = snapshotFormJson; }
    public Integer getSnapshotMaxPages() { return snapshotMaxPages; }
    public void setSnapshotMaxPages(Integer snapshotMaxPages) { this.snapshotMaxPages = snapshotMaxPages; }
    public String getSnapshotTeacherMachineIp() { return snapshotTeacherMachineIp; }
    public void setSnapshotTeacherMachineIp(String snapshotTeacherMachineIp) { this.snapshotTeacherMachineIp = snapshotTeacherMachineIp; }
    public String getIsCurrent() { return isCurrent; }
    public void setIsCurrent(String isCurrent) { this.isCurrent = isCurrent; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public Date getArchivedTime() { return archivedTime; }
    public void setArchivedTime(Date archivedTime) { this.archivedTime = archivedTime; }
}
