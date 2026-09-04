package com.ruoyi.business.domain.vo;

import java.util.ArrayList;
import java.util.List;

/** 教师工具单页目录。 */
public class TeacherToolCatalogVo
{
    private List<TeacherToolVo> recommended = new ArrayList<>();
    private List<TeacherToolCategoryVo> categories = new ArrayList<>();

    public List<TeacherToolVo> getRecommended() { return recommended; }
    public void setRecommended(List<TeacherToolVo> recommended) { this.recommended = recommended; }
    public List<TeacherToolCategoryVo> getCategories() { return categories; }
    public void setCategories(List<TeacherToolCategoryVo> categories) { this.categories = categories; }
}
