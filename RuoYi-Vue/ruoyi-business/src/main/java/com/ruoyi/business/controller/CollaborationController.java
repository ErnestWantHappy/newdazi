package com.ruoyi.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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

    /** 题库来源的协作起始文件（公开或本人创建），设计器未保存课程时也可选用，与课程操作题无关。 */
    @GetMapping("/bank-materials")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    public AjaxResult bankMaterials()
    {
        return AjaxResult.success(service.bankMaterialCandidates());
    }
    /** 协作选题弹窗：分页搜索起始文件，供教师按年级/学期/课次/关键词挑选。 */
    @GetMapping("/bank-materials/search")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    public AjaxResult searchBankMaterials(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "grade", required = false) Long grade,
            @RequestParam(value = "semester", required = false) String semester,
            @RequestParam(value = "lessonNum", required = false) Integer lessonNum,
            @RequestParam(value = "keyword", required = false) String keyword)
    {
        return AjaxResult.success(service.searchBankMaterials(grade, semester, lessonNum, keyword, pageNum, pageSize));
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
    /** 编辑器成员抽屉：身份与可见范围由房间业务事实判断，学生仅限本人小组。 */
    @GetMapping("/room/{roomId}/roster")
    public AjaxResult roster(@PathVariable Long roomId)
    {
        return AjaxResult.success(service.roomRoster(roomId));
    }

    @GetMapping("/student/history")
    public AjaxResult history() { return AjaxResult.success(service.studentHistory()); }

    /**
     * 教师监管：查看房间不可变版本历史（谁在何时保存了哪个版本）。
     */
    @GetMapping("/room/{roomId}/revisions")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    public AjaxResult revisions(@PathVariable Long roomId)
    {
        return AjaxResult.success(service.listRevisions(roomId));
    }
}
