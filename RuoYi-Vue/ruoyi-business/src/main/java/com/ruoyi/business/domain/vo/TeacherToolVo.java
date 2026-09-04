package com.ruoyi.business.domain.vo;

import com.ruoyi.business.domain.BizTeacherTool;

/** 教师工具查询对象；categoryId 用于组装单页目录。 */
public class TeacherToolVo extends BizTeacherTool
{
    private static final long serialVersionUID = 1L;
    private Long categoryId;
    private String categoryNameText;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryNameText() { return categoryNameText; }
    public void setCategoryNameText(String categoryNameText) { this.categoryNameText = categoryNameText; }
}
