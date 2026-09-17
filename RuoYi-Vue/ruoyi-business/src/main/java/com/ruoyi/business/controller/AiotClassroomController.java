package com.ruoyi.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.service.AiotClassroomService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * AIoT 课堂上下文接口（供同源部署的课堂工具调用）。
 *
 * 设计约束：
 * - 完全复用县平台现有登录态（JWT -> LoginUser），不新增任何账号体系；
 * - 身份、班级、小组一律由后台按当前登录人计算，前端提交的组号不作为依据；
 * - 不返回 MQTT 账号/口令/Topic，浏览器永远接触不到 Broker 凭据；
 * - 只读接口，不修改任何业务数据，不影响平台既有功能。
 * - experimentId 可选：为空时保持取第一个实验（旧食堂工具兼容）；
 *   新语音控灯工具必须显式传入，避免一课多实验串课。
 */
@RestController
@RequestMapping("/aiot-classroom")
public class AiotClassroomController extends BaseController
{
    @Autowired private AiotClassroomService service;

    /** 学生上下文：本人班级、小组、设备在线状态（无小组时 status=NO_GROUP） */
    @GetMapping("/student/context")
    @PreAuthorize("@studentSs.isStudent()")
    public AjaxResult studentContext(@RequestParam Long lessonId,
                                     @RequestParam(required = false) Long experimentId)
    {
        return success(service.studentContext(lessonId, experimentId));
    }
    /** 教师上下文：不传 lessonId 返回教师班级课程列表；传 lessonId 返回课程/班级/分组明细 */
    @GetMapping("/teacher/context")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")
    public AjaxResult teacherContext(@RequestParam(required = false) Long lessonId,
                                     @RequestParam(required = false) String entryYear,
                                     @RequestParam(required = false) String classCode,
                                     @RequestParam(required = false) Long experimentId)
    {
        return success(service.teacherContext(lessonId, entryYear, classCode, experimentId));
    }
}
