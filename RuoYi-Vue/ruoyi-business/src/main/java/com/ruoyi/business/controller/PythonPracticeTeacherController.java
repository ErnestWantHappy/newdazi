package com.ruoyi.business.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.service.PythonPracticeService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;

@RestController
@RequestMapping("/business/python-practice/teacher")
public class PythonPracticeTeacherController extends BaseController {
    @Autowired private PythonPracticeService service;

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:query')")
    @GetMapping("/plans")
    public AjaxResult plans(@RequestParam(required = false) String entryYear) { return success(service.teacherPlans(SecurityUtils.getDeptId(), entryYear == null ? "" : entryYear)); }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:query')")
    @GetMapping("/plans/{planId}")
    public AjaxResult detail(@PathVariable Long planId) { return success(service.planDetail(planId, SecurityUtils.getDeptId())); }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit')")
    @PostMapping("/plans")
    public AjaxResult create(@RequestBody Map<String, Object> request) { return success(service.createPlan(request, SecurityUtils.getUserId(), SecurityUtils.getUsername(), SecurityUtils.getDeptId())); }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit')")
    @DeleteMapping("/plans/{planId}")
    public AjaxResult delete(@PathVariable Long planId) { service.deletePlan(planId, SecurityUtils.getDeptId()); return success(); }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit')")
    @PostMapping("/versions/{planVersionId}/questions")
    public AjaxResult addQuestion(@PathVariable Long planVersionId, @RequestBody Map<String, Object> request) { Object id = request.get("questionId"); return success(service.addQuestion(planVersionId, Long.valueOf(String.valueOf(id)), request.get("sortNo") == null ? null : Integer.valueOf(String.valueOf(request.get("sortNo"))), (String) request.get("stage"), SecurityUtils.getUsername(), SecurityUtils.getDeptId())); }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit')")
    @DeleteMapping("/versions/{planVersionId}/questions/{questionId}")
    public AjaxResult removeQuestion(@PathVariable Long planVersionId, @PathVariable Long questionId) { service.removeQuestion(planVersionId, questionId, SecurityUtils.getDeptId()); return success(); }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:publish')")
    @PostMapping("/plans/{planId}/versions/{planVersionId}/publish")
    public AjaxResult publish(@PathVariable Long planId, @PathVariable Long planVersionId) { return success(service.publish(planId, planVersionId, SecurityUtils.getDeptId())); }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit')")
    @GetMapping("/plans/{planId}/extensions")
    public AjaxResult extensions(@PathVariable Long planId) { return success(service.extensions(planId, SecurityUtils.getDeptId())); }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit')")
    @PostMapping("/plans/{planId}/extensions")
    public AjaxResult createExtension(@PathVariable Long planId, @RequestBody Map<String, Object> request) { return success(service.createExtension(request, planId, SecurityUtils.getUserId(), SecurityUtils.getDeptId(), SecurityUtils.getUsername())); }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit')")
    @PostMapping("/extensions/{extensionId}/questions")
    public AjaxResult addExtensionQuestion(@PathVariable Long extensionId, @RequestBody Map<String, Object> request) { return success(service.addExtensionQuestion(extensionId, Long.valueOf(String.valueOf(request.get("questionId"))), request.get("sortNo") == null ? null : Integer.valueOf(String.valueOf(request.get("sortNo"))), SecurityUtils.getUsername(), SecurityUtils.getDeptId())); }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:publish')")
    @PostMapping("/extensions/{extensionId}/publish")
    public AjaxResult publishExtension(@PathVariable Long extensionId) { return success(service.publishExtension(extensionId, SecurityUtils.getDeptId())); }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:publish')")
    @PostMapping("/extensions/{extensionId}/retract")
    public AjaxResult retractExtension(@PathVariable Long extensionId) { return success(service.retractExtension(extensionId, SecurityUtils.getDeptId())); }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:analytics')")
    @GetMapping("/analytics")
    public AjaxResult analytics(@RequestParam String sourceType, @RequestParam Long sourceId, @RequestParam(required = false) String entryYear, @RequestParam(required = false) String classCode) { return success(service.analytics(sourceType, sourceId, SecurityUtils.getDeptId(), entryYear, classCode, SecurityUtils.getUserId())); }
}
