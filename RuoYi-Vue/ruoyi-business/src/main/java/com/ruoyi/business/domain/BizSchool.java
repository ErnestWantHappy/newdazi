package com.ruoyi.business.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 学校信息对象 biz_school
 * 
 * @author ruoyi
 * @date 2025-06-24
 */
public class BizSchool extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 学校ID */
    private Long schoolId;

    /** 学校完整名称 */
    @Excel(name = "学校完整名称")
    private String schoolName;

    /** 学校类型 (1小学 2初中 3高中) */
    @Excel(name = "学校类型 (1小学 2初中 3高中)")
    private String schoolType;

    public void setSchoolId(Long schoolId) 
    {
        this.schoolId = schoolId;
    }

    public Long getSchoolId() 
    {
        return schoolId;
    }

    public void setSchoolName(String schoolName) 
    {
        this.schoolName = schoolName;
    }

    public String getSchoolName() 
    {
        return schoolName;
    }

    public void setSchoolType(String schoolType) 
    {
        this.schoolType = schoolType;
    }

    public String getSchoolType() 
    {
        return schoolType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("schoolId", getSchoolId())
            .append("schoolName", getSchoolName())
            .append("schoolType", getSchoolType())
            .toString();
    }
}
