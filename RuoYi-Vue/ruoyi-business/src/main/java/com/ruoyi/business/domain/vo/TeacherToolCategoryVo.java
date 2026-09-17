package com.ruoyi.business.domain.vo;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.business.domain.BizTeacherToolCategory;

/** 带工具列表的教师工具分类。 */
public class TeacherToolCategoryVo extends BizTeacherToolCategory
{
    private static final long serialVersionUID = 1L;
    private List<TeacherToolVo> tools = new ArrayList<>();

    public List<TeacherToolVo> getTools() { return tools; }
    public void setTools(List<TeacherToolVo> tools) { this.tools = tools; }
}
