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
import com.ruoyi.business.domain.BizLessonQuestion;
import com.ruoyi.business.service.IBizLessonQuestionService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 课程-题目关联Controller
 * 
 * @author ruoyi
 * @date 2025-08-18
 */
@RestController
@RequestMapping("/businesss/question")
public class BizLessonQuestionController extends BaseController
{
    @Autowired
    private IBizLessonQuestionService bizLessonQuestionService;

    /**
     * 查询课程-题目关联列表
     */
    @PreAuthorize("@ss.hasPermi('businesss:question:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizLessonQuestion bizLessonQuestion)
    {
        startPage();
        List<BizLessonQuestion> list = bizLessonQuestionService.selectBizLessonQuestionList(bizLessonQuestion);
        return getDataTable(list);
    }

    /**
     * 导出课程-题目关联列表
     */
    @PreAuthorize("@ss.hasPermi('businesss:question:export')")
    @Log(title = "课程-题目关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizLessonQuestion bizLessonQuestion)
    {
        List<BizLessonQuestion> list = bizLessonQuestionService.selectBizLessonQuestionList(bizLessonQuestion);
        ExcelUtil<BizLessonQuestion> util = new ExcelUtil<BizLessonQuestion>(BizLessonQuestion.class);
        util.exportExcel(response, list, "课程-题目关联数据");
    }

    /**
     * 获取课程-题目关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('businesss:question:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(bizLessonQuestionService.selectBizLessonQuestionById(id));
    }

    /**
     * 新增课程-题目关联
     */
    @PreAuthorize("@ss.hasPermi('businesss:question:add')")
    @Log(title = "课程-题目关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizLessonQuestion bizLessonQuestion)
    {
        return toAjax(bizLessonQuestionService.insertBizLessonQuestion(bizLessonQuestion));
    }

    /**
     * 修改课程-题目关联
     */
    @PreAuthorize("@ss.hasPermi('businesss:question:edit')")
    @Log(title = "课程-题目关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizLessonQuestion bizLessonQuestion)
    {
        return toAjax(bizLessonQuestionService.updateBizLessonQuestion(bizLessonQuestion));
    }

    /**
     * 删除课程-题目关联
     */
    @PreAuthorize("@ss.hasPermi('businesss:question:remove')")
    @Log(title = "课程-题目关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizLessonQuestionService.deleteBizLessonQuestionByIds(ids));
    }
}
