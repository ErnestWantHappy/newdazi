package com.ruoyi.business.controller;

import javax.validation.Valid;
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
import com.ruoyi.business.domain.dto.TeacherToolCategorySaveRequest;
import com.ruoyi.business.domain.dto.TeacherToolQuery;
import com.ruoyi.business.domain.dto.TeacherToolSaveRequest;
import com.ruoyi.business.service.TeacherToolService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/** 教师工具浏览与教研员维护接口。 */
@RestController
@RequestMapping("/business/teacher-tools")
public class TeacherToolController extends BaseController
{
    private static final String VIEW_GUARD = "(@ss.hasRole('teacher') or @ss.hasRole('researcher') or @ss.hasRole('admin'))";
    private static final String MANAGE_GUARD = "(@ss.hasRole('researcher') or @ss.hasRole('admin'))";

    @Autowired
    private TeacherToolService service;

    @GetMapping("/catalog")
    @PreAuthorize("@ss.hasPermi('business:teacherTool:list') and " + VIEW_GUARD)
    public AjaxResult catalog()
    {
        return success(service.getCatalog());
    }

    @GetMapping("/manage/categories")
    @PreAuthorize("@ss.hasPermi('business:teacherTool:manage') and " + MANAGE_GUARD)
    public AjaxResult categories()
    {
        return success(service.listCategories());
    }

    @PostMapping("/manage/categories")
    @PreAuthorize("@ss.hasPermi('business:teacherTool:manage') and " + MANAGE_GUARD)
    @Log(title = "教师工具分类", businessType = BusinessType.INSERT)
    public AjaxResult createCategory(@Valid @RequestBody TeacherToolCategorySaveRequest request)
    {
        return success(service.createCategory(request));
    }

    @PutMapping("/manage/categories/{categoryId}")
    @PreAuthorize("@ss.hasPermi('business:teacherTool:manage') and " + MANAGE_GUARD)
    @Log(title = "教师工具分类", businessType = BusinessType.UPDATE)
    public AjaxResult updateCategory(@PathVariable Long categoryId,
                                     @Valid @RequestBody TeacherToolCategorySaveRequest request)
    {
        return success(service.updateCategory(categoryId, request));
    }

    @PutMapping("/manage/categories/{categoryId}/status")
    @PreAuthorize("@ss.hasPermi('business:teacherTool:manage') and " + MANAGE_GUARD)
    @Log(title = "教师工具分类状态", businessType = BusinessType.UPDATE)
    public AjaxResult updateCategoryStatus(@PathVariable Long categoryId, @RequestParam String status)
    {
        service.updateCategoryStatus(categoryId, status);
        return success();
    }

    @GetMapping("/manage/tools")
    @PreAuthorize("@ss.hasPermi('business:teacherTool:manage') and " + MANAGE_GUARD)
    public TableDataInfo tools(TeacherToolQuery query)
    {
        startPage();
        return getDataTable(service.listTools(query));
    }

    @GetMapping("/manage/tools/{toolId}")
    @PreAuthorize("@ss.hasPermi('business:teacherTool:manage') and " + MANAGE_GUARD)
    public AjaxResult tool(@PathVariable Long toolId)
    {
        return success(service.getTool(toolId));
    }

    @PostMapping("/manage/tools")
    @PreAuthorize("@ss.hasPermi('business:teacherTool:manage') and " + MANAGE_GUARD)
    @Log(title = "教师工具", businessType = BusinessType.INSERT)
    public AjaxResult createTool(@Valid @RequestBody TeacherToolSaveRequest request)
    {
        return success(service.createTool(request));
    }

    @PutMapping("/manage/tools/{toolId}")
    @PreAuthorize("@ss.hasPermi('business:teacherTool:manage') and " + MANAGE_GUARD)
    @Log(title = "教师工具", businessType = BusinessType.UPDATE)
    public AjaxResult updateTool(@PathVariable Long toolId, @Valid @RequestBody TeacherToolSaveRequest request)
    {
        return success(service.updateTool(toolId, request));
    }

    @PutMapping("/manage/tools/{toolId}/status")
    @PreAuthorize("@ss.hasPermi('business:teacherTool:manage') and " + MANAGE_GUARD)
    @Log(title = "教师工具状态", businessType = BusinessType.UPDATE)
    public AjaxResult updateToolStatus(@PathVariable Long toolId, @RequestParam String status)
    {
        service.updateToolStatus(toolId, status);
        return success();
    }

    @DeleteMapping("/manage/tools/{toolId}")
    @PreAuthorize("@ss.hasPermi('business:teacherTool:manage') and " + MANAGE_GUARD)
    @Log(title = "教师工具", businessType = BusinessType.DELETE)
    public AjaxResult deleteTool(@PathVariable Long toolId)
    {
        service.deleteTool(toolId);
        return success();
    }

    @PutMapping("/manage/tools/{toolId}/restore")
    @PreAuthorize("@ss.hasPermi('business:teacherTool:manage') and " + MANAGE_GUARD)
    @Log(title = "教师工具恢复", businessType = BusinessType.UPDATE)
    public AjaxResult restoreTool(@PathVariable Long toolId)
    {
        service.restoreTool(toolId);
        return success();
    }
}
