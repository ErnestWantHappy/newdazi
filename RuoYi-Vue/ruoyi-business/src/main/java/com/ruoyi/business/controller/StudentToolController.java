package com.ruoyi.business.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizLessonTool;
import com.ruoyi.business.domain.BizStudentTool;
import com.ruoyi.business.domain.vo.StudentToolScopeGroup;
import com.ruoyi.business.service.StudentToolService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 学生实验工具。
 * 教师：管理常驻工具（本校隔离）与课程本节课工具；
 * 学生：读取当前课程对应的工具列表（本节课 + 按班匹配的常驻）。
 */
@RestController
@RequestMapping("/business/student-tool")
public class StudentToolController extends BaseController
{
    private static final String TEACHER_GUARD = "(@ss.hasRole('teacher') or @ss.hasRole('researcher') or @ss.hasRole('admin'))";

    @Autowired
    private StudentToolService service;

    /** 教师：分页查询本校常驻工具 */
    @PreAuthorize("@ss.hasPermi('business:studentTool:list') and " + TEACHER_GUARD)
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String keyword)
    {
        Long deptId = SecurityUtils.getDeptId();
        return success(service.listTools(deptId, keyword));
    }

    /** 教师：查询单个常驻工具（含适用范围） */
    @PreAuthorize("@ss.hasPermi('business:studentTool:list') and " + TEACHER_GUARD)
    @GetMapping("/{toolId}")
    public AjaxResult get(@PathVariable Long toolId)
    {
        BizStudentTool tool = service.getTool(SecurityUtils.getDeptId(), toolId);
        if (tool == null)
        {
            return error("工具不存在或无权限");
        }
        return success(tool);
    }

    /** 教师：新增常驻工具 */
    @PreAuthorize("@ss.hasPermi('business:studentTool:manage') and " + TEACHER_GUARD)
    @Log(title = "学生实验工具", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ToolSaveRequest request)
    {
        if (request == null || request.getTool() == null)
        {
            return error("参数不完整");
        }
        return success(service.createTool(SecurityUtils.getDeptId(), request.getTool(), request.getScopes()));
    }

    /** 教师：修改常驻工具 */
    @PreAuthorize("@ss.hasPermi('business:studentTool:manage') and " + TEACHER_GUARD)
    @Log(title = "学生实验工具", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ToolSaveRequest request)
    {
        if (request == null || request.getTool() == null)
        {
            return error("参数不完整");
        }
        return success(service.updateTool(SecurityUtils.getDeptId(), request.getTool(), request.getScopes()));
    }

    /** 教师：批量删除常驻工具（级联删适用范围） */
    @PreAuthorize("@ss.hasPermi('business:studentTool:manage') and " + TEACHER_GUARD)
    @Log(title = "学生实验工具", businessType = BusinessType.DELETE)
    @DeleteMapping("/{toolIds}")
    public AjaxResult remove(@PathVariable Long[] toolIds)
    {
        service.deleteTools(SecurityUtils.getDeptId(), toolIds);
        return success();
    }

    /** 教师：查询某课程的本节课工具 */
    @PreAuthorize("@ss.hasPermi('business:studentTool:list') and " + TEACHER_GUARD)
    @GetMapping("/lesson/{lessonId}")
    public AjaxResult lessonTools(@PathVariable Long lessonId)
    {
        return success(service.getLessonTools(lessonId));
    }

    /** 教师：全量替换某课程的本节课工具 */
    @PreAuthorize("@ss.hasPermi('business:studentTool:manage') and " + TEACHER_GUARD)
    @Log(title = "本节课工具", businessType = BusinessType.UPDATE)
    @PutMapping("/lesson/{lessonId}")
    public AjaxResult saveLessonTools(@PathVariable Long lessonId, @RequestBody List<BizLessonTool> tools)
    {
        if (tools == null) { tools = new java.util.ArrayList<>(); }
        service.replaceLessonTools(lessonId, tools);
        return success();
    }

    /** 学生：当前课程的工具列表（本节课 + 常驻） */
    @PreAuthorize("@studentSs.isStudent()")
    @GetMapping("/mine")
    public AjaxResult mine(@RequestParam(required = false) Long lessonId)
    {
        Long deptId = SecurityUtils.getDeptId();
        // 学生的入学年份/班级：由学生首页接口主链路提供更完整；此处从当前课程关联课程设计器常用上下文拿不到则返回课程工具。为简化：无参数时不返回常驻匹配。
        if (lessonId == null)
        {
            return success(new java.util.HashMap<String, Object>());
        }
        // 注：学生端主入口使用增强后的 current-lesson；本接口为可选即时刷新通道，仅返回本节课工具。
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("lessonTools", service.getLessonTools(lessonId));
        map.put("residentTools", new java.util.ArrayList<>());
        return success(map);
    }

    /** 请求体：工具 + 适用范围（按年级分组） */
    public static class ToolSaveRequest
    {
        private BizStudentTool tool;
        private List<StudentToolScopeGroup> scopes;

        public BizStudentTool getTool() { return tool; }
        public void setTool(BizStudentTool tool) { this.tool = tool; }
        public List<StudentToolScopeGroup> getScopes() { return scopes; }
        public void setScopes(List<StudentToolScopeGroup> scopes) { this.scopes = scopes; }
    }
}
