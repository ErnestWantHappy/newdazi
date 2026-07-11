package com.ruoyi.business.controller;

import com.ruoyi.business.domain.CountyExam;
import com.ruoyi.business.service.ICountyExamService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 县考管理 Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/countyExam")
public class CountyExamController extends BaseController {

    @Autowired
    private ICountyExamService countyExamService;

    /**
     * 查询县考列表
     */
    @PreAuthorize("@ss.hasPermi('business:countyExam:list')")
    @GetMapping("/list")
    public TableDataInfo list(CountyExam countyExam) {
        startPage();
        List<CountyExam> list = countyExamService.selectCountyExamList(countyExam);
        return getDataTable(list);
    }

    /**
     * 获取县考详情
     */
    @PreAuthorize("@ss.hasPermi('business:countyExam:query')")
    @GetMapping("/{examId}")
    public AjaxResult getInfo(@PathVariable Long examId) {
        return success(countyExamService.selectCountyExamById(examId));
    }

    /**
     * 新增县考
     */
    @PreAuthorize("@ss.hasPermi('business:countyExam:add')")
    @Log(title = "县考管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CountyExam countyExam) {
        return toAjax(countyExamService.insertCountyExam(countyExam));
    }

    /**
     * 修改县考
     */
    @PreAuthorize("@ss.hasPermi('business:countyExam:edit')")
    @Log(title = "县考管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CountyExam countyExam) {
        return toAjax(countyExamService.updateCountyExam(countyExam));
    }

    /**
     * 删除县考
     */
    @PreAuthorize("@ss.hasPermi('business:countyExam:remove')")
    @Log(title = "县考管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{examIds}")
    public AjaxResult remove(@PathVariable Long[] examIds) {
        return toAjax(countyExamService.deleteCountyExamByIds(examIds));
    }
}
