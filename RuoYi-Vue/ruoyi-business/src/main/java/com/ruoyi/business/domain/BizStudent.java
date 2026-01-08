package com.ruoyi.business.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 学生业务对象 biz_student
 *
 * @author ruoyi
 * @date 2025-07-01
 */
public class BizStudent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 学生主键ID */
    private Long studentId;

    /** 关联的用户ID (sys_user.user_id) */
    private Long userId;

    /** 学号 */
    @Excel(name = "学号")
    private String studentNo;

    /** 入学年份 */
    @Excel(name = "入学年份")
    private String entryYear;

    /** 班级编号 */
    @Excel(name = "班级编号")
    private String classCode;

    /** 真实姓名 (用于导入和列表展示) */
    @Excel(name = "真实姓名")
    private String studentName;

    /** 登录账号 (用于列表展示, 非数据库字段) */
    private String userName;

    /** 部门ID (仅用于数据权限查询, 非数据库字段) */
    private Long deptId;

    /** 教师用户ID (仅用于数据权限查询, 非数据库字段) */
    private Long teacherUserId;


    // --- Getter and Setter methods ---

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getTeacherUserId() {
        return teacherUserId;
    }

    public void setTeacherUserId(Long teacherUserId) {
        this.teacherUserId = teacherUserId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("studentId", getStudentId())
                .append("userId", getUserId())
                .append("studentNo", getStudentNo())
                .append("entryYear", getEntryYear())
                .append("classCode", getClassCode())
                .append("studentName", getStudentName())
                .append("userName", getUserName())
                .append("deptId", getDeptId())
                .toString();
    }
}