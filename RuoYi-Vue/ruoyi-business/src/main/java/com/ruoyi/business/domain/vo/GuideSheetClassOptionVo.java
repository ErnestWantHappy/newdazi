package com.ruoyi.business.domain.vo;

import java.io.Serializable;

/**
 * 电子导学单班级选项。
 */
public class GuideSheetClassOptionVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String entryYear;
    private String classCode;

    public GuideSheetClassOptionVo()
    {
    }

    public GuideSheetClassOptionVo(String entryYear, String classCode)
    {
        this.entryYear = entryYear;
        this.classCode = classCode;
    }

    public String getEntryYear()
    {
        return entryYear;
    }

    public void setEntryYear(String entryYear)
    {
        this.entryYear = entryYear;
    }

    public String getClassCode()
    {
        return classCode;
    }

    public void setClassCode(String classCode)
    {
        this.classCode = classCode;
    }

    public String getKey()
    {
        return entryYear + ":" + classCode;
    }

    public String getLabel()
    {
        return entryYear + "级 " + classCode + "班";
    }
}
