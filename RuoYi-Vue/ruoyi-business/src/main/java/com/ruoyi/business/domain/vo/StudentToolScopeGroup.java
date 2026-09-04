package com.ruoyi.business.domain.vo;

import java.util.List;

/**
 * 学生实验工具适用范围：按年级分组表达。
 * allGrade=true 表示整个年级生效（classCodes 忽略）；
 * 否则 classCodes 为该年级下指定的班级列表。
 */
public class StudentToolScopeGroup
{
    /** 入学年份/级，如 2024 */
    private String entryYear;

    /** 是否整个年级生效 */
    private Boolean allGrade;

    /** 指定班级列表（allGrade=false 时使用） */
    private List<String> classCodes;

    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }
    public Boolean getAllGrade() { return allGrade; }
    public void setAllGrade(Boolean allGrade) { this.allGrade = allGrade; }
    public List<String> getClassCodes() { return classCodes; }
    public void setClassCodes(List<String> classCodes) { this.classCodes = classCodes; }
}
