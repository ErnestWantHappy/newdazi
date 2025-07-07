package com.ruoyi.business.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;


/**
 * 学生业务对象 biz_student
 * * @author ruoyi
 * @date 2025-07-01
 */
public class BizStudent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 学生主键ID */
    private Long studentId;

    /** 关联的用户ID (sys_user.user_id) */
    private Long userId;

    /** 所属学校ID */
    private Long schoolId;

    /** 学号 */
    @Excel(name = "学号")
    private String studentNo;

    /** 入学年份 */
    @Excel(name = "入学年份")
    private String entryYear;

    /** 班级编号 */
    @Excel(name = "班级编号")
    private String classCode;

    /** 真实姓名 (用于导入) */
    @Excel(name = "真实姓名")
    private String studentName; // 我们使用这个字段来接收Excel中的“真实姓名”

    // a non-persistent field for class name, which is not in the biz_student table
    private String className;

    public void setStudentId(Long studentId)
    {
        this.studentId = studentId;
    }

    public Long getStudentId()
    {
        return studentId;
    }
    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }
    public void setSchoolId(Long schoolId)
    {
        this.schoolId = schoolId;
    }

    public Long getSchoolId()
    {
        return schoolId;
    }
    public void setStudentNo(String studentNo)
    {
        this.studentNo = studentNo;
    }

    public String getStudentNo()
    {
        return studentNo;
    }
    public void setEntryYear(String entryYear)
    {
        this.entryYear = entryYear;
    }

    public String getEntryYear()
    {
        return entryYear;
    }
    public void setClassCode(String classCode)
    {
        this.classCode = classCode;
    }

    public String getClassCode()
    {
        return classCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}