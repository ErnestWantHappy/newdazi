package com.ruoyi.business.domain;

import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 电子教材-册对象 biz_textbook_book
 *
 * 册只描述"哪本书"，不与课程绑定；教师进入阅读器后手动选择课次。
 */
public class BizTextbookBook extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 册ID */
    private Long bookId;

    /** 册编码，如 xxkj-7-0-2026 */
    private String bookCode;

    /** 书名全称 */
    private String title;

    /** 短名，如 七上 */
    private String shortTitle;

    /** 年级 1-9 */
    private Integer grade;

    /** 学期：0上册 1下册 9全一册 */
    private String semester;

    /** 出版社 */
    private String publisher;

    /** 版次年份 */
    private String edition;

    /** PDF 总页数 */
    private Integer totalPages;

    /** 印刷页 = PDF 页 - 偏移 */
    private Integer pageOffset;

    /** 状态：0上架 1下架 */
    private String status;

    /** 书架排序 */
    private Integer sortNo;

    /** 源 PDF 文件名 */
    private String sourceFile;

    /** 源 PDF 哈希 */
    private String sourceSha256;

    /** 导入时间 */
    private Date importedAt;

    /** 学段，由年级推导，不落库：primary / junior */
    private String stage;

    /** 单元数（列表查询时统计） */
    private Integer unitCount;

    /** 课次数（列表查询时统计） */
    private Integer lessonCount;

    public Long getBookId()
    {
        return bookId;
    }

    public void setBookId(Long bookId)
    {
        this.bookId = bookId;
    }

    public String getBookCode()
    {
        return bookCode;
    }

    public void setBookCode(String bookCode)
    {
        this.bookCode = bookCode;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getShortTitle()
    {
        return shortTitle;
    }

    public void setShortTitle(String shortTitle)
    {
        this.shortTitle = shortTitle;
    }

    public Integer getGrade()
    {
        return grade;
    }

    public void setGrade(Integer grade)
    {
        this.grade = grade;
    }

    public String getSemester()
    {
        return semester;
    }

    public void setSemester(String semester)
    {
        this.semester = semester;
    }

    public String getPublisher()
    {
        return publisher;
    }

    public void setPublisher(String publisher)
    {
        this.publisher = publisher;
    }

    public String getEdition()
    {
        return edition;
    }

    public void setEdition(String edition)
    {
        this.edition = edition;
    }

    public Integer getTotalPages()
    {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages)
    {
        this.totalPages = totalPages;
    }

    public Integer getPageOffset()
    {
        return pageOffset;
    }

    public void setPageOffset(Integer pageOffset)
    {
        this.pageOffset = pageOffset;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getSortNo()
    {
        return sortNo;
    }

    public void setSortNo(Integer sortNo)
    {
        this.sortNo = sortNo;
    }

    public String getSourceFile()
    {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile)
    {
        this.sourceFile = sourceFile;
    }

    public String getSourceSha256()
    {
        return sourceSha256;
    }

    public void setSourceSha256(String sourceSha256)
    {
        this.sourceSha256 = sourceSha256;
    }

    public Date getImportedAt()
    {
        return importedAt;
    }

    public void setImportedAt(Date importedAt)
    {
        this.importedAt = importedAt;
    }

    public String getStage()
    {
        return stage;
    }

    public void setStage(String stage)
    {
        this.stage = stage;
    }

    public Integer getUnitCount()
    {
        return unitCount;
    }

    public void setUnitCount(Integer unitCount)
    {
        this.unitCount = unitCount;
    }

    public Integer getLessonCount()
    {
        return lessonCount;
    }

    public void setLessonCount(Integer lessonCount)
    {
        this.lessonCount = lessonCount;
    }
}
