package com.ruoyi.business.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.business.domain.vo.LessonDetailVo;
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
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.service.IBizLessonService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 课程管理Controller
 *
 * @author ruoyi
 * @date 2025-08-21
 */
@RestController
@RequestMapping("/business/lesson")
public class BizLessonController extends BaseController
{
    @Autowired
    private IBizLessonService bizLessonService;

    /**
     * 获取课程完整详情 (用于课程设计器 "修改" 模式)
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:query')")
    @GetMapping(value = "/details/{lessonId}")
    public AjaxResult getLessonDetails(@PathVariable("lessonId") Long lessonId)
    {
        return success(bizLessonService.selectLessonDetailsByLessonId(lessonId));
    }

    /**
     * 一站式保存课程所有信息 (用于课程设计器 "保存" 按钮)
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:add') or @ss.hasPermi('business:lesson:edit')")
    @Log(title = "课程设计与指派", businessType = BusinessType.INSERT)
    @PostMapping("/save-all")
    public AjaxResult saveAll(@RequestBody LessonDetailVo lessonDetailVo)
    {
        // 核心修复：使用 success() 方法替代 toAjax()
        return success(bizLessonService.saveLessonDetails(lessonDetailVo));
    }


    // =================================================================
    // 以下是若依自动生成的标准接口
    // =================================================================

    /**
     * 查询课程管理列表
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizLesson bizLesson)
    {
        startPage();
        List<BizLesson> list = bizLessonService.selectBizLessonList(bizLesson);
        return getDataTable(list);
    }

    /**
     * 导出课程管理列表
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:export')")
    @Log(title = "课程管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizLesson bizLesson)
    {
        List<BizLesson> list = bizLessonService.selectBizLessonList(bizLesson);
        ExcelUtil<BizLesson> util = new ExcelUtil<BizLesson>(BizLesson.class);
        util.exportExcel(response, list, "课程管理数据");
    }

    /**
     * 获取课程管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:query')")
    @GetMapping(value = "/{lessonId}")
    public AjaxResult getInfo(@PathVariable("lessonId") Long lessonId)
    {
        return success(bizLessonService.selectBizLessonByLessonId(lessonId));
    }

    /**
     * 新增课程管理
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:add')")
    @Log(title = "课程管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizLesson bizLesson)
    {
        return toAjax(bizLessonService.insertBizLesson(bizLesson));
    }

    /**
     * 修改课程管理
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:edit')")
    @Log(title = "课程管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizLesson bizLesson)
    {
        return toAjax(bizLessonService.updateBizLesson(bizLesson));
    }

    /**
     * 删除课程管理
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:remove')")
    @Log(title = "课程管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{lessonIds}")
    public AjaxResult remove(@PathVariable Long[] lessonIds)
    {
        return toAjax(bizLessonService.deleteBizLessonByLessonIds(lessonIds));
    }
}
