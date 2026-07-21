package com.ruoyi.business.controller;

import com.ruoyi.business.domain.vo.GradeGroupVo;
import com.ruoyi.business.service.IBizLessonService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 教师端首页仪表盘
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/teacher")
@PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('admin')")
public class TeacherDashboardController extends BaseController
{
    @Autowired
    private IBizLessonService lessonService;

    /**
     * 获取教师首页的完整数据
     */
    @GetMapping("/dashboard-data")
    public AjaxResult getDashboardData()
    {
        List<GradeGroupVo> dashboardData = lessonService.getTeacherDashboardData();
        return AjaxResult.success(dashboardData);
    }

}
