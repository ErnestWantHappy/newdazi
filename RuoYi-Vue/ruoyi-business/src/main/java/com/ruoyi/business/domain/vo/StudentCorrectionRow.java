package com.ruoyi.business.domain.vo;

import java.io.Serializable;
import com.ruoyi.common.annotation.Excel;

/**
 * 学生批量纠错行。
 *
 * studentId 和原登录账号共同定位原记录，正式纠错时不会删除或新建学生。
 */
public class StudentCorrectionRow implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "学生永久编号", width = 18, prompt = "系统定位字段，请勿修改")
    private Long studentId;

    @Excel(name = "原登录账号", width = 24, prompt = "系统校验字段，请勿修改")
    private String originalUserName;

    @Excel(name = "真实姓名", width = 18)
    private String studentName;

    @Excel(name = "入学年份", width = 14)
    private String entryYear;

    @Excel(name = "班级编号", width = 14, prompt = "只填 01～99")
    private String classCode;

    @Excel(name = "学号", width = 12, prompt = "只填 01～99")
    private String studentNo;

    @Excel(name = "备注", width = 32)
    private String remark;

    /** Excel 中的行号，便于教师定位错误。 */
    private Integer rowNumber;

    private String currentUserName;
    private String currentStudentName;
    private String currentEntryYear;
    private String currentClassCode;
    private String currentStudentNo;
    private String targetUserName;
    private Boolean valid;
    private Boolean changed;
    private String message;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getOriginalUserName() { return originalUserName; }
    public void setOriginalUserName(String originalUserName) { this.originalUserName = originalUserName; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getRowNumber() { return rowNumber; }
    public void setRowNumber(Integer rowNumber) { this.rowNumber = rowNumber; }
    public String getCurrentUserName() { return currentUserName; }
    public void setCurrentUserName(String currentUserName) { this.currentUserName = currentUserName; }
    public String getCurrentStudentName() { return currentStudentName; }
    public void setCurrentStudentName(String currentStudentName) { this.currentStudentName = currentStudentName; }
    public String getCurrentEntryYear() { return currentEntryYear; }
    public void setCurrentEntryYear(String currentEntryYear) { this.currentEntryYear = currentEntryYear; }
    public String getCurrentClassCode() { return currentClassCode; }
    public void setCurrentClassCode(String currentClassCode) { this.currentClassCode = currentClassCode; }
    public String getCurrentStudentNo() { return currentStudentNo; }
    public void setCurrentStudentNo(String currentStudentNo) { this.currentStudentNo = currentStudentNo; }
    public String getTargetUserName() { return targetUserName; }
    public void setTargetUserName(String targetUserName) { this.targetUserName = targetUserName; }
    public Boolean getValid() { return valid; }
    public void setValid(Boolean valid) { this.valid = valid; }
    public Boolean getChanged() { return changed; }
    public void setChanged(Boolean changed) { this.changed = changed; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
