package com.ruoyi.business.domain.vo;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.List;

public class GuideSheetVo extends BaseEntity
{
    private Long sheetId;
    private String sheetTitle;
    private Long lessonId;
    private String lessonTitle;
    private Long creatorId;
    private Long deptId;
    private String formJson;
    private String status;
    private Integer maxPages;
    private String teacherMachineIp;
    private List<String> assignedClassCodes;
    private List<String> allClassesInGrade;
    private Integer answerCount;
    private Integer submittedCount;

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

    public String getLessonTitle() {
        return lessonTitle;
    }

    public void setLessonTitle(String lessonTitle) {
        this.lessonTitle = lessonTitle;
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

    public List<String> getAssignedClassCodes() {
        return assignedClassCodes;
    }

    public void setAssignedClassCodes(List<String> assignedClassCodes) {
        this.assignedClassCodes = assignedClassCodes;
    }

    public List<String> getAllClassesInGrade() {
        return allClassesInGrade;
    }

    public void setAllClassesInGrade(List<String> allClassesInGrade) {
        this.allClassesInGrade = allClassesInGrade;
    }

    public Integer getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(Integer answerCount) {
        this.answerCount = answerCount;
    }

    public Integer getSubmittedCount() {
        return submittedCount;
    }

    public void setSubmittedCount(Integer submittedCount) {
        this.submittedCount = submittedCount;
    }
}
