package com.ruoyi.business.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.business.service.CollaborationActivityService;
import com.ruoyi.common.core.domain.AjaxResult;

/** 小组协作活动与教师轨迹查看入口。 */
@RestController
@RequestMapping("/business/collaboration")
public class CollaborationActivityController
{
    @Autowired private CollaborationActivityService service;
    @PostMapping("/lesson/{lessonId}/activities") @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    public AjaxResult create(@PathVariable Long lessonId, @RequestBody Map<String, Object> request) throws Exception { return AjaxResult.success(service.create(lessonId, request)); }
    @GetMapping("/lesson/{lessonId}/activities") @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    public AjaxResult list(@PathVariable Long lessonId) { return AjaxResult.success(service.list(lessonId)); }
    @GetMapping("/lesson/{lessonId}/activity-setup") @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    public AjaxResult setup(@PathVariable Long lessonId) { return AjaxResult.success(service.setup(lessonId)); }
    @GetMapping("/activity/{activityId}") @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    public AjaxResult detail(@PathVariable Long activityId) { return AjaxResult.success(service.detail(activityId)); }
    @PostMapping("/room/{roomId}/heartbeat") public AjaxResult heartbeat(@PathVariable Long roomId) { service.recordHeartbeat(roomId); return AjaxResult.success(); }
    @PostMapping("/room/{roomId}/leave") public AjaxResult leave(@PathVariable Long roomId) { service.recordLeave(roomId); return AjaxResult.success(); }
    @GetMapping("/room/{roomId}/timeline") @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    public AjaxResult timeline(@PathVariable Long roomId) { return AjaxResult.success(service.timeline(roomId)); }
}
