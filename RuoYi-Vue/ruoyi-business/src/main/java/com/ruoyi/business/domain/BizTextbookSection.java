package com.ruoyi.business.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 电子教材-目录条目对象 biz_textbook_section
 *
 * kind：FRONT 前置、UNIT_INTRO 单元导入、LESSON 课次、SUMMARY 单元小结、APPENDIX 附录。
 */
public class BizTextbookSection extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 条目ID */
    private Long sectionId;

    /** 册ID */
    private Long bookId;

    /** 单元ID，前置与附录为空 */
    private Long unitId;

    /** 册内稳定键：front / unit-2-intro / lesson-6 / summary-2 / appendix */
    private String sectionKey;

    /** 条目类型 */
    private String kind;

    /** 课次序号，仅 LESSON */
    private Integer lessonNo;

    /** 条目标题 */
    private String title;

    /** 起始印刷页 */
    private Integer printedStart;

    /** 结束印刷页 */
    private Integer printedEnd;

    /** 起始 PDF 页 */
    private Integer pdfStart;

    /** 结束 PDF 页 */
    private Integer pdfEnd;

    /** 册内排序 */
    private Integer sortNo;

    public Long getSectionId()
    {
        return sectionId;
    }

    public void setSectionId(Long sectionId)
    {
        this.sectionId = sectionId;
    }

    public Long getBookId()
    {
        return bookId;
    }

    public void setBookId(Long bookId)
    {
        this.bookId = bookId;
    }

    public Long getUnitId()
    {
        return unitId;
    }

    public void setUnitId(Long unitId)
    {
        this.unitId = unitId;
    }

    public String getSectionKey()
    {
        return sectionKey;
    }

    public void setSectionKey(String sectionKey)
    {
        this.sectionKey = sectionKey;
    }

    public String getKind()
    {
        return kind;
    }

    public void setKind(String kind)
    {
        this.kind = kind;
    }

    public Integer getLessonNo()
    {
        return lessonNo;
    }

    public void setLessonNo(Integer lessonNo)
    {
        this.lessonNo = lessonNo;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Integer getPrintedStart()
    {
        return printedStart;
    }

    public void setPrintedStart(Integer printedStart)
    {
        this.printedStart = printedStart;
    }

    public Integer getPrintedEnd()
    {
        return printedEnd;
    }

    public void setPrintedEnd(Integer printedEnd)
    {
        this.printedEnd = printedEnd;
    }

    public Integer getPdfStart()
    {
        return pdfStart;
    }

    public void setPdfStart(Integer pdfStart)
    {
        this.pdfStart = pdfStart;
    }

    public Integer getPdfEnd()
    {
        return pdfEnd;
    }

    public void setPdfEnd(Integer pdfEnd)
    {
        this.pdfEnd = pdfEnd;
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
