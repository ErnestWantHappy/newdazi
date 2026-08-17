package com.ruoyi.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.dto.CollaborationSettingsRequest;
import com.ruoyi.business.service.CollaborationRoomService;
import com.ruoyi.common.core.domain.AjaxResult;

/** 平台内教师和学生使用的在线协作入口。 */
@RestController
@RequestMapping("/business/collaboration")
public class CollaborationController
{
    @Autowired private CollaborationRoomService service;

    @GetMapping("/health")
    public AjaxResult health()
    {
        return AjaxResult.success(service.health());
    }

    @GetMapping("/lesson/{lessonId}")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    public AjaxResult settings(@PathVariable Long lessonId)
    {
        return AjaxResult.success(service.teacherSettings(lessonId));
    }

    @PutMapping("/lesson/{lessonId}")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    public AjaxResult saveSettings(@PathVariable Long lessonId,
                                   @RequestBody CollaborationSettingsRequest request) throws Exception
    {
        return AjaxResult.success(service.saveTeacherSettings(lessonId, request));
    }

    @GetMapping("/student/current")
    public AjaxResult currentStudentRooms()
    {
        // 现有学生账号以 biz_student 学籍事实识别，不能依赖并未批量维护的 sys_user_role。
        // 匿名访问仍由 Spring Security 统一拦截，班级边界继续由业务层严格校验。
        return AjaxResult.success(service.currentStudentRooms());
    }

    @GetMapping("/room/{roomId}/session")
    public AjaxResult session(@PathVariable Long roomId)
    {
        // 教师与学生身份都由房间业务事实判断，避免合法学生因缺少系统角色被提前拒绝。
        return AjaxResult.success(service.createSession(roomId));
    }
}
