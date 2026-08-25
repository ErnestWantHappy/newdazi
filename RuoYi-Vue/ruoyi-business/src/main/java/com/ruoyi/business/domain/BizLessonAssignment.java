package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 课程班级指派对象 biz_lesson_assignment
 * 
 * @author ruoyi
 * @date 2025-08-25
 */
public class BizLessonAssignment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 指派记录ID */
    private Long assignmentId;

    /** 课程ID (关联 biz_lesson.lesson_id) */
    @Excel(name = "课程ID (关联 biz_lesson.lesson_id)")
    private Long lessonId;

    /** 指派班级的入学年份 */
    @Excel(name = "指派班级的入学年份")
    private String entryYear;

    /** 指派班级的班级编号 */
    @Excel(name = "指派班级的班级编号")
    private String classCode;

    /** 指派教师的用户ID (关联 sys_user.user_id) */
    private Long assignerId;

    /** 所属学校ID */
    @Excel(name = "所属学校ID")
    private Long deptId;

    /** 指派时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "指派时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date assignTime;

    /** 当前班级首次达到自动推进阈值的时间，各班独立计时 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date autoAdvanceReadyTime;

    /** 理论测试题开放开关 1=开放（推进课程自动复位） */
    private Integer theoryOpen;

    /** 操作题开放开关 1=开放（推进课程自动复位） */
    private Integer practicalOpen;

    public void setAssignmentId(Long assignmentId) 
    {
        this.assignmentId = assignmentId;
    }

    public Long getAssignmentId() 
    {
        return assignmentId;
    }

    public void setLessonId(Long lessonId) 
    {
        this.lessonId = lessonId;
    }

    public Long getLessonId() 
    {
        return lessonId;
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

    public void setAssignerId(Long assignerId) 
    {
        this.assignerId = assignerId;
    }

    public Long getAssignerId() 
    {
        return assignerId;
    }

    public Long getDeptId() 
    {
        return deptId;
    }

    public void setDeptId(Long deptId) 
    {
        this.deptId = deptId;
    }

    public void setAssignTime(Date assignTime) 
    {
        this.assignTime = assignTime;
    }

    public Date getAssignTime() 
    {
        return assignTime;
    }

    public Date getAutoAdvanceReadyTime()
    {
        return autoAdvanceReadyTime;
    }

    public void setAutoAdvanceReadyTime(Date autoAdvanceReadyTime)
    {
        this.autoAdvanceReadyTime = autoAdvanceReadyTime;
    }

    public Integer getTheoryOpen()
    {
        return theoryOpen;
    }

    public void setTheoryOpen(Integer theoryOpen)
    {
        this.theoryOpen = theoryOpen;
    }

    public Integer getPracticalOpen()
    {
        return practicalOpen;
    }

    public void setPracticalOpen(Integer practicalOpen)
    {
        this.practicalOpen = practicalOpen;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("assignmentId", getAssignmentId())
            .append("lessonId", getLessonId())
            .append("entryYear", getEntryYear())
            .append("classCode", getClassCode())
            .append("assignerId", getAssignerId())
            .append("assignTime", getAssignTime())
            .append("autoAdvanceReadyTime", getAutoAdvanceReadyTime())
            .toString();
    }
}
