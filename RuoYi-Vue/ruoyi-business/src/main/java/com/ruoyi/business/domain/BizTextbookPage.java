package com.ruoyi.business.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 电子教材-页对象 biz_textbook_page
 *
 * 页面图存放在教材根目录之外（如 /data/apps/xueyeceping/shared/textbook），
 * 由后端鉴权后按 ETag 返回，不走无鉴权的 /profile 静态路径。
 */
public class BizTextbookPage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 页ID */
    private Long pageId;

    /** 册ID */
    private Long bookId;

    /** PDF 页码（从 1 开始） */
    private Integer pageNo;

    /** 页脚印刷页码，前置页为空 */
    private Integer printedNo;

    /** 相对教材根目录的图片路径 */
    private String imagePath;

    /** 像素宽 */
    private Integer width;

    /** 像素高 */
    private Integer height;

    /** 文件字节数 */
    private Integer bytes;

    /** 内容哈希，用作 ETag */
    private String sha256;

    public Long getPageId()
    {
        return pageId;
    }

    public void setPageId(Long pageId)
    {
        this.pageId = pageId;
    }

    public Long getBookId()
    {
        return bookId;
    }

    public void setBookId(Long bookId)
    {
        this.bookId = bookId;
    }

    public Integer getPageNo()
    {
        return pageNo;
    }

    public void setPageNo(Integer pageNo)
    {
        this.pageNo = pageNo;
    }

    public Integer getPrintedNo()
    {
        return printedNo;
    }

    public void setPrintedNo(Integer printedNo)
    {
        this.printedNo = printedNo;
    }

    public String getImagePath()
    {
        return imagePath;
    }

    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }

    public Integer getWidth()
    {
        return width;
    }

    public void setWidth(Integer width)
    {
        this.width = width;
    }

    public Integer getHeight()
    {
        return height;
    }

    public void setHeight(Integer height)
    {
        this.height = height;
    }

    public Integer getBytes()
    {
        return bytes;
    }

    public void setBytes(Integer bytes)
    {
        this.bytes = bytes;
    }

    public String getSha256()
    {
        return sha256;
    }

    public void setSha256(String sha256)
    {
        this.sha256 = sha256;
    }
}
