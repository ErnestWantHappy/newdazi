package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 教师-班级管理关联对象 biz_teacher_class
 * 
 * @author zdx
 * @date 2025-12-29
 */
public class BizTeacherClass extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 教师用户ID */
    @Excel(name = "教师用户ID")
    private Long userId;

    /** 学校ID */
    @Excel(name = "学校ID")
    private Long deptId;

    /** 入学年份 */
    @Excel(name = "入学年份")
    private String entryYear;

    /** 班级编号 */
    @Excel(name = "班级编号")
    private String classCode;

    /** 学校名称（非数据库字段，用于展示） */
    private String deptName;

    /** 教师姓名（非数据库字段，用于展示） */
    private String userName;

    /** 学生人数（非数据库字段，用于展示） */
    private Integer studentCount;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setDeptId(Long deptId) 
    {
        this.deptId = deptId;
    }

    public Long getDeptId() 
    {
        return deptId;
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

    public String getDeptName() 
    {
        return deptName;
    }

    public void setDeptName(String deptName) 
    {
        this.deptName = deptName;
    }

    public String getUserName() 
    {
        return userName;
    }

    public void setUserName(String userName) 
    {
        this.userName = userName;
    }

    public Integer getStudentCount() 
    {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) 
    {
        this.studentCount = studentCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("deptId", getDeptId())
            .append("entryYear", getEntryYear())
            .append("classCode", getClassCode())
            .append("createTime", getCreateTime())
            .toString();
    }
}
