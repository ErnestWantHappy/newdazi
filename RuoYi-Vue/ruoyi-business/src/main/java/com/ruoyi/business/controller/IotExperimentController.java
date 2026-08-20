package com.ruoyi.business.controller;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.dto.IotClassGroupingRequest;
import com.ruoyi.business.domain.dto.IotDeviceRequest;
import com.ruoyi.business.domain.dto.IotExperimentRequest;
import com.ruoyi.business.domain.dto.IotGroupRequest;
import com.ruoyi.business.domain.dto.IotRotatePasscodeRequest;
import com.ruoyi.business.service.IotExperimentService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;

/** 教师与学生物联网实验、分组、口令与实时数据接口；浏览器不直接访问 Broker。 */
@RestController
@RequestMapping("/business/iot")
public class IotExperimentController extends BaseController
{
    @Autowired private IotExperimentService service;

    @GetMapping("/experiments")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")
    public AjaxResult experiments(@RequestParam Long lessonId)
    {
        return success(service.listExperiments(lessonId));
    }

    @PostMapping("/experiments")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "物联网实验", businessType = BusinessType.INSERT, isSaveRequestData = false)
    public AjaxResult createExperiment(@Valid @RequestBody IotExperimentRequest request)
    {
        return success(service.createExperiment(request));
    }

    @GetMapping("/lesson-classes")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")
    public AjaxResult lessonClasses(@RequestParam Long lessonId)
    {
        return success(service.listLessonClasses(lessonId));
    }

    @GetMapping("/class-config")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")
    public AjaxResult classConfig(@RequestParam Long experimentId, @RequestParam String entryYear, @RequestParam String classCode)
    {
        return success(service.getClassConfig(experimentId, entryYear, classCode));
    }

    @PostMapping("/generate-grouping")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "物联网班级分组", businessType = BusinessType.INSERT, isSaveRequestData = false)
    public AjaxResult generateGrouping(@Valid @RequestBody IotClassGroupingRequest request)
    {
        return success(service.generateClassGrouping(request));
    }

    @PostMapping("/rotate-passcode")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "物联网课堂口令", businessType = BusinessType.UPDATE, isSaveRequestData = false)
    public AjaxResult rotatePasscode(@Valid @RequestBody IotRotatePasscodeRequest request)
    {
        return success(service.rotateClassPasscode(request));
    }

    @GetMapping("/class-card")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")
    public AjaxResult classCard(@RequestParam Long experimentId, @RequestParam String entryYear, @RequestParam String classCode)
    {
        return success(service.getClassCard(experimentId, entryYear, classCode));
    }

    @GetMapping("/experiments/{experimentId}/groups")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")
    public AjaxResult groups(@PathVariable Long experimentId,
                             @RequestParam(required = false) String entryYear,
                             @RequestParam(required = false) String classCode)
    {
        if (entryYear != null && classCode != null)
        {
            return success(service.listGroups(experimentId, entryYear, classCode));
        }
        return success(service.listGroups(experimentId));
    }

    @GetMapping("/groups/{groupId}/devices")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")
    public AjaxResult devices(@PathVariable Long groupId)
    {
        return success(service.listDevices(groupId));
    }

    @PostMapping("/groups")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "物联网小组", businessType = BusinessType.INSERT, isSaveRequestData = false)
    public AjaxResult createGroup(@Valid @RequestBody IotGroupRequest request)
    {
        return success(service.createGroup(request));
    }

    @PostMapping("/devices")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "物联网设备", businessType = BusinessType.INSERT, isSaveRequestData = false)
    public AjaxResult createDevice(@Valid @RequestBody IotDeviceRequest request)
    {
        return success(service.createDevice(request));
    }

    @PostMapping("/devices/{deviceId}/credential")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "物联网设备凭据", businessType = BusinessType.UPDATE, isSaveRequestData = false)
    public AjaxResult rotateCredential(@PathVariable Long deviceId)
    {
        return success(service.rotateCredential(deviceId));
    }

    @GetMapping("/experiments/{experimentId}/dashboard")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")
    public AjaxResult dashboard(@PathVariable Long experimentId,
                                @RequestParam(required = false) String entryYear,
                                @RequestParam(required = false) String classCode,
                                @RequestParam(defaultValue = "50") int limit)
    {
        return success(service.dashboard(experimentId, entryYear, classCode, limit));
    }

    /**
     * 学生端物联实验概览（仅返回当前学生所在班级与小组信息）
     */
    @GetMapping("/student/overview")
    @PreAuthorize("@studentSs.isStudent()")
    public AjaxResult studentOverview(@RequestParam(required = false) Long lessonId)
    {
        return success(service.getStudentOverview(lessonId));
    }
}
