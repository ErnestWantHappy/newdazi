package com.ruoyi.business.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 学生常驻工具适用范围对象 biz_student_tool_scope
 * 一行=（工具, 入学年份/级, 班级）；classCode 为空表示整个年级生效。
 *
 * @author system
 * @date 2026-08-22
 */
public class BizStudentToolScope extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long scopeId;

    private Long toolId;

    /** 入学年份/级，如 2024 */
    private String entryYear;

    /** 班级号，空=全年级生效 */
    private String classCode;

    public Long getScopeId() { return scopeId; }
    public void setScopeId(Long scopeId) { this.scopeId = scopeId; }
    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
}
