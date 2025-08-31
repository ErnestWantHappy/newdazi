package com.ruoyi.business.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.service.IBizLessonAssignmentService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 课程班级指派Controller
 * 
 * @author ruoyi
 * @date 2025-08-25
 */
@RestController
@RequestMapping("/business/assignment")
public class BizLessonAssignmentController extends BaseController
{
    @Autowired
    private IBizLessonAssignmentService bizLessonAssignmentService;

    /**
     * 查询课程班级指派列表
     */
    @PreAuthorize("@ss.hasPermi('business:assignment:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizLessonAssignment bizLessonAssignment)
    {
        startPage();
        List<BizLessonAssignment> list = bizLessonAssignmentService.selectBizLessonAssignmentList(bizLessonAssignment);
        return getDataTable(list);
    }

    /**
     * 导出课程班级指派列表
     */
    @PreAuthorize("@ss.hasPermi('business:assignment:export')")
    @Log(title = "课程班级指派", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizLessonAssignment bizLessonAssignment)
    {
        List<BizLessonAssignment> list = bizLessonAssignmentService.selectBizLessonAssignmentList(bizLessonAssignment);
        ExcelUtil<BizLessonAssignment> util = new ExcelUtil<BizLessonAssignment>(BizLessonAssignment.class);
        util.exportExcel(response, list, "课程班级指派数据");
    }

    /**
     * 获取课程班级指派详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:assignment:query')")
    @GetMapping(value = "/{assignmentId}")
    public AjaxResult getInfo(@PathVariable("assignmentId") Long assignmentId)
    {
        return success(bizLessonAssignmentService.selectBizLessonAssignmentByAssignmentId(assignmentId));
    }

    /**
     * 新增课程班级指派
     */
    @PreAuthorize("@ss.hasPermi('business:assignment:add')")
    @Log(title = "课程班级指派", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizLessonAssignment bizLessonAssignment)
    {
        return toAjax(bizLessonAssignmentService.insertBizLessonAssignment(bizLessonAssignment));
    }

    /**
     * 修改课程班级指派
     */
    @PreAuthorize("@ss.hasPermi('business:assignment:edit')")
    @Log(title = "课程班级指派", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizLessonAssignment bizLessonAssignment)
    {
        return toAjax(bizLessonAssignmentService.updateBizLessonAssignment(bizLessonAssignment));
    }

    /**
     * 删除课程班级指派
     */
    @PreAuthorize("@ss.hasPermi('business:assignment:remove')")
    @Log(title = "课程班级指派", businessType = BusinessType.DELETE)
	@DeleteMapping("/{assignmentIds}")
    public AjaxResult remove(@PathVariable Long[] assignmentIds)
    {
        return toAjax(bizLessonAssignmentService.deleteBizLessonAssignmentByAssignmentIds(assignmentIds));
    }
}
