package com.ruoyi.business.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.business.service.ClassGroupingService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;

/** 班级分组和教师学生桌面接口。 */
@RestController
@RequestMapping("/business/class-group")
public class ClassGroupingController extends BaseController {
    @Autowired private ClassGroupingService service;

    @PreAuthorize("@ss.hasPermi('business:teacherClass:list')")
    @GetMapping("/schemes")
    public AjaxResult schemes(@RequestParam String entryYear, @RequestParam String classCode) { return success(service.listSchemes(SecurityUtils.getUserId(), SecurityUtils.getDeptId(), entryYear, classCode)); }

    @PreAuthorize("@ss.hasPermi('business:teacherClass:edit')")
    @PostMapping("/schemes")
    public AjaxResult saveScheme(@RequestBody Map<String,Object> request) { return success(service.saveScheme(SecurityUtils.getUserId(), SecurityUtils.getDeptId(), String.valueOf(request.get("entryYear")), String.valueOf(request.get("classCode")), request)); }

    @PreAuthorize("@ss.hasPermi('business:teacherClass:edit')")
    @PostMapping("/schemes/generate")
    public AjaxResult generate(@RequestBody Map<String,Object> request) { return success(service.generateScheme(SecurityUtils.getUserId(), SecurityUtils.getDeptId(), String.valueOf(request.get("entryYear")), String.valueOf(request.get("classCode")), request)); }

    @PreAuthorize("@ss.hasPermi('business:lesson:edit')")
    @PostMapping("/lessons/{lessonId}/snapshots")
    public AjaxResult snapshot(@PathVariable Long lessonId, @RequestBody Map<String,Object> request) { Long schemeId = request.get("schemeId") == null ? null : Long.valueOf(String.valueOf(request.get("schemeId"))); return success(service.generateSnapshot(SecurityUtils.getUserId(), lessonId, String.valueOf(request.get("entryYear")), String.valueOf(request.get("classCode")), schemeId)); }

    @PreAuthorize("@ss.hasPermi('business:teacherClass:list')")
    @GetMapping("/desktop")
    public AjaxResult desktop(@RequestParam String entryYear, @RequestParam String classCode) { return success(service.desktop(SecurityUtils.getUserId(), SecurityUtils.getDeptId(), entryYear, classCode)); }

    @PreAuthorize("@ss.hasPermi('business:teacherClass:edit')")
    @PutMapping("/desktop/layout")
    public AjaxResult layout(@RequestBody Map<String,Object> request) { return success(service.saveLayout(SecurityUtils.getUserId(), SecurityUtils.getDeptId(), String.valueOf(request.get("entryYear")), String.valueOf(request.get("classCode")), request)); }

    @PreAuthorize("@ss.hasPermi('business:teacherClass:remove')")
    @DeleteMapping("/schemes/{schemeId}")
    public AjaxResult deleteScheme(@PathVariable Long schemeId) { service.deleteScheme(SecurityUtils.getUserId(), schemeId); return success(); }
}
