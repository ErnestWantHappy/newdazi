package com.ruoyi.business.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 电子教材-单元对象 biz_textbook_unit
 */
public class BizTextbookUnit extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 单元ID */
    private Long unitId;

    /** 册ID */
    private Long bookId;

    /** 单元序号（册内） */
    private Integer unitNo;

    /** 单元标题 */
    private String title;

    /** 册内排序 */
    private Integer sortNo;

    public Long getUnitId()
    {
        return unitId;
    }

    public void setUnitId(Long unitId)
    {
        this.unitId = unitId;
    }

    public Long getBookId()
    {
        return bookId;
    }

    public void setBookId(Long bookId)
    {
        this.bookId = bookId;
    }

    public Integer getUnitNo()
    {
        return unitNo;
    }

    public void setUnitNo(Integer unitNo)
    {
        this.unitNo = unitNo;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Integer getSortNo()
    {
        return sortNo;
    }

    public void setSortNo(Integer sortNo)
    {
        this.sortNo = sortNo;
    }
}
