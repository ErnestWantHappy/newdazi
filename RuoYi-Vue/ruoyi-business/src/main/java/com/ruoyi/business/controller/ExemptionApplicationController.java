package com.ruoyi.business.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.service.ExemptionApplicationService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 教师免抽测申请。
 */
@RestController
@RequestMapping("/business/exemption")
public class ExemptionApplicationController extends BaseController
{
    private static final String TEACHER_AUTH =
            "@ss.hasRole('teacher') and @ss.hasPermi('business:exemption:apply')";
    private static final String REVIEW_AUTH =
            "(@ss.hasRole('researcher') or @ss.hasRole('admin')) "
            + "and @ss.hasPermi('business:exemption:review')";
    private static final String STANDARD_AUTH =
            "(@ss.hasRole('researcher') or @ss.hasRole('admin')) "
            + "and @ss.hasPermi('business:exemption:standard')";

    @Autowired
    private ExemptionApplicationService service;

    @GetMapping("/preview")
    @PreAuthorize(TEACHER_AUTH)
    public AjaxResult preview(@RequestParam String academicYear,
                              @RequestParam String semester,
                              @RequestParam Integer grade)
    {
        return AjaxResult.success(service.preview(academicYear, semester, grade));
    }

    @PostMapping("/applications")
    @PreAuthorize(TEACHER_AUTH)
    @Log(title = "提交教师免抽测申请", businessType = BusinessType.INSERT)
    public AjaxResult submit(@RequestBody Map<String, Object> request)
    {
        return AjaxResult.success(service.submit(request));
    }

    @GetMapping("/applications/my")
    @PreAuthorize(TEACHER_AUTH)
    public AjaxResult myApplications()
    {
        return AjaxResult.success(service.myApplications());
    }

    @GetMapping("/applications/{applicationId}")
    @PreAuthorize("(" + TEACHER_AUTH + ") or (" + REVIEW_AUTH + ")")
    public AjaxResult detail(@PathVariable Long applicationId)
    {
        return AjaxResult.success(service.detail(applicationId));
    }

    @GetMapping("/review/applications")
    @PreAuthorize(REVIEW_AUTH)
    public TableDataInfo reviewApplications(@RequestParam Map<String, Object> query)
    {
        startPage();
        List<Map<String, Object>> rows = service.reviewApplications(
                query == null ? new LinkedHashMap<>() : query);
        return getDataTable(rows);
    }

    @PutMapping("/review/applications/{applicationId}")
    @PreAuthorize(REVIEW_AUTH)
    @Log(title = "审核教师免抽测申请", businessType = BusinessType.UPDATE)
    public AjaxResult review(@PathVariable Long applicationId,
                             @RequestBody Map<String, Object> request)
    {
        return AjaxResult.success(service.review(applicationId, request));
    }

    @GetMapping("/standards")
    @PreAuthorize(STANDARD_AUTH)
    public AjaxResult standards(@RequestParam String academicYear,
                                @RequestParam String semester)
    {
        return AjaxResult.success(service.standards(academicYear, semester));
    }

    @PutMapping("/standards")
    @PreAuthorize(STANDARD_AUTH)
    @Log(title = "设置免抽测应使用课数", businessType = BusinessType.UPDATE)
    public AjaxResult saveStandard(@RequestBody Map<String, Object> request)
    {
        service.saveStandard(request);
        return AjaxResult.success();
    }
}
