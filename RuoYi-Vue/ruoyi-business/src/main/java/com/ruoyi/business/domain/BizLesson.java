package com.ruoyi.business.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 课程/主题对象 biz_lesson
 * 
 * @author ruoyi
 * @date 2025-06-23
 */
public class BizLesson extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 课程ID */
    private Long lessonId;

    /** 课程/主题名称 */
    @Excel(name = "课程/主题名称")
    private String lessonName;

    /** 类型 (daily日常, exam考试, regional区域检测) */
    @Excel(name = "类型 (daily日常, exam考试, regional区域检测)")
    private String lessonType;

    /** 所属学校ID */
    @Excel(name = "所属学校ID")
    private Long schoolId;

    /** 年级 (如: 3, 7) */
    @Excel(name = "年级 (如: 3, 7)")
    private Long gradeLevel;

    /** 适用的班级ID列表 (逗号分隔, 如: 101,102) */
    @Excel(name = "适用的班级ID列表 (逗号分隔, 如: 101,102)")
    private String classIds;

    /** 创建教师ID */
    @Excel(name = "创建教师ID")
    private Long creatorId;

    /** 状态 (0草稿 1已发布) */
    @Excel(name = "状态 (0草稿 1已发布)")
    private String status;

    public void setLessonId(Long lessonId) 
    {
        this.lessonId = lessonId;
    }

    public Long getLessonId() 
    {
        return lessonId;
    }

    public void setLessonName(String lessonName) 
    {
        this.lessonName = lessonName;
    }

    public String getLessonName() 
    {
        return lessonName;
    }

    public void setLessonType(String lessonType) 
    {
        this.lessonType = lessonType;
    }

    public String getLessonType() 
    {
        return lessonType;
    }

    public void setSchoolId(Long schoolId) 
    {
        this.schoolId = schoolId;
    }

    public Long getSchoolId() 
    {
        return schoolId;
    }

    public void setGradeLevel(Long gradeLevel) 
    {
        this.gradeLevel = gradeLevel;
    }

    public Long getGradeLevel() 
    {
        return gradeLevel;
    }

    public void setClassIds(String classIds) 
    {
        this.classIds = classIds;
    }

    public String getClassIds() 
    {
        return classIds;
    }

    public void setCreatorId(Long creatorId) 
    {
        this.creatorId = creatorId;
    }

    public Long getCreatorId() 
    {
        return creatorId;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("lessonId", getLessonId())
            .append("lessonName", getLessonName())
            .append("lessonType", getLessonType())
            .append("schoolId", getSchoolId())
            .append("gradeLevel", getGradeLevel())
            .append("classIds", getClassIds())
            .append("creatorId", getCreatorId())
            .append("createTime", getCreateTime())
            .append("status", getStatus())
            .toString();
    }
}
