package com.ruoyi.business.controller;

import java.util.Collections;
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
    public AjaxResult plans(@RequestParam(required = false) String entryYear) {
        return success(service.teacherPlans(SecurityUtils.getDeptId(), entryYear == null ? "" : entryYear));
    }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:query')")
    @GetMapping("/classes")
    public AjaxResult classes() {
        return success(service.managedClasses(SecurityUtils.getUserId(), SecurityUtils.getDeptId()));
    }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:query')")
    @GetMapping("/plans/{planId}")
    public AjaxResult detail(@PathVariable Long planId) {
        return success(service.planDetail(planId, SecurityUtils.getDeptId()));
    }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit')")
    @PostMapping("/plans")
    public AjaxResult create(@RequestBody Map<String, Object> request) {
        return success(service.createPlan(request, SecurityUtils.getUserId(), SecurityUtils.getUsername(), SecurityUtils.getDeptId()));
    }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit')")
    @PutMapping("/plans/{planId}")
    public AjaxResult update(@PathVariable Long planId, @RequestBody Map<String, Object> request) {
        return success(service.updatePlan(planId, request, SecurityUtils.getUserId(), SecurityUtils.getUsername(), SecurityUtils.getDeptId()));
    }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit')")
    @DeleteMapping("/plans/{planId}")
    public AjaxResult delete(@PathVariable Long planId) {
        return success(service.deletePlan(planId, SecurityUtils.getDeptId()));
    }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit')")
    @PostMapping("/versions/{planVersionId}/questions")
    public AjaxResult addQuestion(@PathVariable Long planVersionId, @RequestBody Map<String, Object> request) {
        Long questionId = longOrNull(request == null ? null : request.get("questionId"));
        if (questionId == null) return error("参数不完整：questionId 必须为有效数字");
        return success(service.addQuestion(planVersionId, questionId,
            intOrNull(request.get("sortNo")), strOrNull(request.get("stage")),
            SecurityUtils.getUsername(), SecurityUtils.getDeptId()));
    }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit')")
    @PostMapping("/versions/{planVersionId}/questions/batch")
    public AjaxResult addQuestions(@PathVariable Long planVersionId, @RequestBody Map<String, Object> request) {
        Object value = request == null ? null : request.get("questionIds");
        List<?> ids = value instanceof List ? (List<?>) value : Collections.emptyList();
        return success(service.addQuestions(planVersionId, ids, SecurityUtils.getUsername(), SecurityUtils.getDeptId()));
    }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit')")
    @PutMapping("/versions/{planVersionId}/questions/order")
    public AjaxResult reorder(@PathVariable Long planVersionId, @RequestBody Map<String, Object> request) {
        Object value = request == null ? null : request.get("questionIds");
        List<?> ids = value instanceof List ? (List<?>) value : Collections.emptyList();
        return success(service.reorderQuestions(planVersionId, ids, SecurityUtils.getDeptId()));
    }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit')")
    @PostMapping("/versions/{planVersionId}/recommend")
    public AjaxResult recommend(@PathVariable Long planVersionId,
                                @RequestBody(required = false) Map<String, Object> request) {
        Integer count = intOrNull(request == null ? null : request.get("count"));
        return success(service.recommendQuestions(planVersionId, count, SecurityUtils.getUsername(), SecurityUtils.getDeptId()));
    }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit')")
    @DeleteMapping("/versions/{planVersionId}/questions/{questionId}")
    public AjaxResult removeQuestion(@PathVariable Long planVersionId, @PathVariable Long questionId) {
        service.removeQuestion(planVersionId, questionId, SecurityUtils.getDeptId());
        return success();
    }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:publish')")
    @PostMapping("/plans/{planId}/versions/{planVersionId}/publish")
    public AjaxResult publish(@PathVariable Long planId, @PathVariable Long planVersionId) {
        return success(service.publish(planId, planVersionId, SecurityUtils.getDeptId()));
    }

    @PreAuthorize("@ss.hasPermi('business:pythonPractice:analytics')")
    @GetMapping("/analytics")
    public AjaxResult analytics(@RequestParam Long planVersionId,
                                @RequestParam String entryYear,
                                @RequestParam String classCode) {
        return success(service.analytics(planVersionId, SecurityUtils.getDeptId(), entryYear,
            classCode, SecurityUtils.getUserId()));
    }

    private Long longOrNull(Object value) {
        if (value instanceof Number) return ((Number) value).longValue();
        String text = value == null ? null : String.valueOf(value).trim();
        if (text == null || text.isEmpty() || "null".equals(text)) return null;
        try { return Long.valueOf(text); } catch (NumberFormatException e) { return null; }
    }

    private Integer intOrNull(Object value) {
        if (value instanceof Number) return ((Number) value).intValue();
        String text = value == null ? null : String.valueOf(value).trim();
        if (text == null || text.isEmpty() || "null".equals(text)) return null;
        try { return Integer.valueOf(text); } catch (NumberFormatException e) { return null; }
    }

    private String strOrNull(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value);
        return "null".equals(text) ? null : text;
    }
}
