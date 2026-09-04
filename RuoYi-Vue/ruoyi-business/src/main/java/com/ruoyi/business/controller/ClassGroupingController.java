package com.ruoyi.business.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.business.service.ClassGroupingService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/** 班级分组和教师学生桌面接口。 */
@RestController
@RequestMapping("/business/class-group")
public class ClassGroupingController extends BaseController {
    @Autowired private ClassGroupingService service;

    @PreAuthorize("@ss.hasPermi('business:teacherClass:list')")
    @GetMapping("/schemes")
    public AjaxResult schemes(@RequestParam String entryYear, @RequestParam String classCode) { return success(service.listSchemes(SecurityUtils.getUserId(), SecurityUtils.getDeptId(), entryYear, classCode)); }

    // 旧教师角色使用 add 作为班级管理写权限，兼容该权限才能配置分组。
    @PreAuthorize("@ss.hasAnyPermi('business:teacherClass:edit,business:teacherClass:add')")
    @PostMapping("/schemes")
    public AjaxResult saveScheme(@RequestBody Map<String,Object> request) { return success(service.saveScheme(SecurityUtils.getUserId(), SecurityUtils.getDeptId(), requiredText(request, "entryYear", "请选择年级"), requiredText(request, "classCode", "请选择班级"), request)); }

    @PreAuthorize("@ss.hasAnyPermi('business:teacherClass:edit,business:teacherClass:add')")
    @PostMapping("/schemes/generate")
    public AjaxResult generate(@RequestBody Map<String,Object> request) { return success(service.generateScheme(SecurityUtils.getUserId(), SecurityUtils.getDeptId(), requiredText(request, "entryYear", "请选择年级"), requiredText(request, "classCode", "请选择班级"), request)); }

    @PreAuthorize("@ss.hasPermi('business:lesson:edit')")
    @PostMapping("/lessons/{lessonId}/snapshots")
    public AjaxResult snapshot(@PathVariable Long lessonId, @RequestBody Map<String,Object> request) { Long schemeId = optionalLong(request, "schemeId"); return success(service.generateSnapshot(SecurityUtils.getUserId(), lessonId, requiredText(request, "entryYear", "请选择年级"), requiredText(request, "classCode", "请选择班级"), schemeId)); }

    @PreAuthorize("@ss.hasPermi('business:teacherClass:list')")
    @GetMapping("/desktop")
    public AjaxResult desktop(@RequestParam String entryYear, @RequestParam String classCode) { return success(service.desktop(SecurityUtils.getUserId(), SecurityUtils.getDeptId(), entryYear, classCode)); }

    /** 课堂大屏一次读取本班终端、分组、作答、表现和请假数据。 */
    @PreAuthorize("@ss.hasPermi('business:teacherClass:list')")
    @GetMapping("/desktop/overview")
    public AjaxResult desktopOverview(@RequestParam Long lessonId, @RequestParam String entryYear, @RequestParam String classCode) {
        return success(service.desktopOverview(SecurityUtils.getUserId(), SecurityUtils.getDeptId(), lessonId, entryYear, classCode));
    }

    @PreAuthorize("@ss.hasAnyPermi('business:teacherClass:edit,business:teacherClass:add')")
    @PutMapping("/desktop/layout")
    public AjaxResult layout(@RequestBody Map<String,Object> request) { return success(service.saveLayout(SecurityUtils.getUserId(), SecurityUtils.getDeptId(), requiredText(request, "entryYear", "请选择年级"), requiredText(request, "classCode", "请选择班级"), request)); }

    @PreAuthorize("@ss.hasPermi('business:teacherClass:remove')")
    @DeleteMapping("/schemes/{schemeId}")
    public AjaxResult deleteScheme(@PathVariable Long schemeId) { service.deleteScheme(SecurityUtils.getUserId(), schemeId); return success(); }

    private String requiredText(Map<String, Object> request, String key, String message) {
        String value = request == null ? null : String.valueOf(request.get(key));
        if (StringUtils.isBlank(value) || "null".equalsIgnoreCase(value.trim())) throw new ServiceException(message);
        return value.trim();
    }

    private Long optionalLong(Map<String, Object> request, String key) {
        String value = request == null || request.get(key) == null ? null : String.valueOf(request.get(key)).trim();
        if (StringUtils.isBlank(value) || "null".equalsIgnoreCase(value)) return null;
        try { return Long.valueOf(value); }
        catch (NumberFormatException e) { throw new ServiceException("分组方案参数不正确"); }
    }
}
