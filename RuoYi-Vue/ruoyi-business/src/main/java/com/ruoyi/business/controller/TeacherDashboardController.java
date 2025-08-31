package com.ruoyi.business.controller;

import com.ruoyi.business.domain.vo.GradeGroupVo;
import com.ruoyi.business.service.IBizLessonService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
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
public class TeacherDashboardController extends BaseController
{
    @Autowired
    private IBizLessonService lessonService;

    /**
     * 获取教师首页的完整数据
     * 这是我们开发蓝图中的 "接口一"
     */
    @GetMapping("/dashboard-data")
    public AjaxResult getDashboardData()
    {
        // 我们将在下一步中实现这个核心方法
        List<GradeGroupVo> dashboardData = lessonService.getTeacherDashboardData();
        return AjaxResult.success(dashboardData);
    }
}
