package com.ruoyi.business.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.business.domain.BizLessonTool;
import com.ruoyi.business.domain.BizStudentTool;
import com.ruoyi.business.domain.vo.StudentToolScopeGroup;

/**
 * 学生实验工具服务：常驻工具 + 本节课工具 + 按班匹配。
 */
public interface StudentToolService
{
    /** 教师按学校分页查询常驻工具列表 */
    List<BizStudentTool> listTools(Long deptId, String keyword);

    /** 查询单个工具（含适用范围） */
    BizStudentTool getTool(Long deptId, Long toolId);

    /** 新增常驻工具（含适用范围，事务） */
    BizStudentTool createTool(Long deptId, BizStudentTool tool, List<StudentToolScopeGroup> scopes);

    /** 修改常驻工具（适用范围全量替换，事务） */
    BizStudentTool updateTool(Long deptId, BizStudentTool tool, List<StudentToolScopeGroup> scopes);

    /** 删除常驻工具（级联删适用范围，事务） */
    void deleteTools(Long deptId, Long[] toolIds);

    /** 学生端获取工具：本节课 + 常驻（按学校/年份/班级匹配） */
    Map<String, Object> getToolsForStudent(Long deptId, String entryYear, String classCode, Long lessonId);

    /** 获取课程的本节课工具（课程设计器/教师端用） */
    List<BizLessonTool> getLessonTools(Long lessonId);

    /** 全量替换课程的本节课工具（事务） */
    void replaceLessonTools(Long lessonId, List<BizLessonTool> tools);
}
