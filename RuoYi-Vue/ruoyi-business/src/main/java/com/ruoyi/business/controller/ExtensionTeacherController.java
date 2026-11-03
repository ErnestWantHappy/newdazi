package com.ruoyi.business.controller;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.business.service.ExtensionClassService;
import com.ruoyi.common.core.domain.AjaxResult;

@RestController
@RequestMapping("/business/extension/teacher")
@PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
public class ExtensionTeacherController {
    @Autowired private ExtensionClassService service;
    @GetMapping("/classes") public AjaxResult classes() { return AjaxResult.success(service.classes()); }
    @GetMapping("/teachers") public AjaxResult teachers() { return AjaxResult.success(service.teachers()); }
    @PostMapping("/classes") public AjaxResult create(@RequestBody Map<String,Object> row) { return AjaxResult.success(service.create(row)); }
    @PutMapping("/classes/{id}") public AjaxResult update(@PathVariable Long id,@RequestBody Map<String,Object> row) { service.update(id,row); return AjaxResult.success(); }
    @PostMapping("/classes/{id}/archive") public AjaxResult archive(@PathVariable Long id) { service.archive(id); return AjaxResult.success(); }
    @GetMapping("/classes/{id}/members") public AjaxResult members(@PathVariable Long id) { return AjaxResult.success(service.members(id)); }
    @GetMapping("/candidates") public AjaxResult candidates(@RequestParam(defaultValue="") String keyword) { return AjaxResult.success(service.candidates(keyword)); }
    @PostMapping("/classes/{id}/members") public AjaxResult add(@PathVariable Long id,@RequestBody Map<String,List<Long>> body) { service.add(id,body.get("studentIds")); return AjaxResult.success(); }
    @DeleteMapping("/classes/{id}/members/{sid}") public AjaxResult remove(@PathVariable Long id,@PathVariable Long sid) { service.remove(id,sid); return AjaxResult.success(); }
    @PostMapping("/classes/{id}/lessons") public AjaxResult lesson(@PathVariable Long id,@RequestBody Map<String,Object> row) { return AjaxResult.success(service.createLesson(id,row)); }
    @GetMapping("/classes/{id}/dashboard") public AjaxResult dashboard(@PathVariable Long id) { return AjaxResult.success(service.dashboard(id)); }
    @GetMapping("/classes/{id}/legacy") public AjaxResult legacy(@PathVariable Long id) { return AjaxResult.success(service.legacy(id)); }
    @GetMapping("/classes/{id}/lessons/{lessonId}/roster") public AjaxResult roster(@PathVariable Long id,@PathVariable Long lessonId) { return AjaxResult.success(service.roster(id,lessonId)); }
}
