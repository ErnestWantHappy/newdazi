package com.ruoyi.business.domain.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 教师工具单页目录。 */
public class TeacherToolCatalogVo implements Serializable
{
    private static final long serialVersionUID = 1L;
    private List<TeacherToolVo> recommended = new ArrayList<>();
    private List<TeacherToolCategoryVo> categories = new ArrayList<>();
    /** 教学任务快捷筛选；与分类导航是两套不同口径，前端不得混为一组。 */
    private List<TeacherToolTaskVo> tasks = new ArrayList<>();
    /** 检索别名，前端按 toolId 关联后参与搜索匹配。 */
    private List<TeacherToolTermVo> terms = new ArrayList<>();

    public List<TeacherToolVo> getRecommended() { return recommended; }
    public void setRecommended(List<TeacherToolVo> recommended) { this.recommended = recommended; }
    public List<TeacherToolCategoryVo> getCategories() { return categories; }
    public void setCategories(List<TeacherToolCategoryVo> categories) { this.categories = categories; }
    public List<TeacherToolTaskVo> getTasks() { return tasks; }
    public void setTasks(List<TeacherToolTaskVo> tasks) { this.tasks = tasks; }
    public List<TeacherToolTermVo> getTerms() { return terms; }
    public void setTerms(List<TeacherToolTermVo> terms) { this.terms = terms; }
}
