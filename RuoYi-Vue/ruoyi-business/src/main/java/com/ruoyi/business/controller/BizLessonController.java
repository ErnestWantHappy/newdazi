package com.ruoyi.business.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.core.domain.entity.SysUser;
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
 * 课程/主题Controller
 * 
 * @author ruoyi
 * @date 2025-06-23
 */
@RestController
@RequestMapping("/business/lesson")
public class BizLessonController extends BaseController
{
    @Autowired
    private IBizLessonService bizLessonService;

    /**
     * 查询课程/主题列表
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
     * 导出课程/主题列表
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:export')")
    @Log(title = "课程/主题", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizLesson bizLesson)
    {
        List<BizLesson> list = bizLessonService.selectBizLessonList(bizLesson);
        ExcelUtil<BizLesson> util = new ExcelUtil<BizLesson>(BizLesson.class);
        util.exportExcel(response, list, "课程/主题数据");
    }

    /**
     * 获取课程/主题详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:query')")
    @GetMapping(value = "/{lessonId}")
    public AjaxResult getInfo(@PathVariable("lessonId") Long lessonId)
    {
        return success(bizLessonService.selectBizLessonByLessonId(lessonId));
    }

    /**
     * 新增课程/主题
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:add')")
    @Log(title = "课程/主题", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizLesson bizLesson)
    {
        // 1. 获取当前登录的用户信息
        SysUser currentUser = getLoginUser().getUser();

        // 2. 从当前用户信息中获取schoolId
        //    (前提是我们已经在SysUser实体中关联了schoolId)
        Long schoolId = currentUser.getSchoolId();

        // 3. 将获取到的schoolId设置到要新增的课程对象中
        bizLesson.setSchoolId(schoolId);

        // 4. 将创建者的ID也设置进去 (可选，但推荐)
        bizLesson.setCreatorId(currentUser.getUserId());
        return toAjax(bizLessonService.insertBizLesson(bizLesson));
    }

    /**
     * 修改课程/主题
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:edit')")
    @Log(title = "课程/主题", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizLesson bizLesson)
    {
        return toAjax(bizLessonService.updateBizLesson(bizLesson));
    }

    /**
     * 删除课程/主题
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:remove')")
    @Log(title = "课程/主题", businessType = BusinessType.DELETE)
	@DeleteMapping("/{lessonIds}")
    public AjaxResult remove(@PathVariable Long[] lessonIds)
    {
        return toAjax(bizLessonService.deleteBizLessonByLessonIds(lessonIds));
    }
}
