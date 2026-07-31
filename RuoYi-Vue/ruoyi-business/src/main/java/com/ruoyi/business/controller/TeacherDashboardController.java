package com.ruoyi.business.controller;

import com.ruoyi.business.domain.vo.GradeGroupVo;
import com.ruoyi.business.domain.vo.LessonInfoVo;
import com.ruoyi.business.domain.vo.PracticalGradingStatusVo;
import com.ruoyi.business.service.IBizLessonService;
import com.ruoyi.business.service.PracticalGradingDeadlineService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.ArrayList;
import com.ruoyi.common.utils.SecurityUtils;

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

    @Autowired
    private PracticalGradingDeadlineService deadlineService;

    /**
     * 获取教师首页的完整数据
     */
    @GetMapping("/dashboard-data")
    public AjaxResult getDashboardData()
    {
        List<GradeGroupVo> dashboardData = lessonService.getTeacherDashboardData();
        Long deptId = SecurityUtils.getDeptId();
        for (GradeGroupVo group : dashboardData)
        {
            if (group.getLessons() == null)
            {
                continue;
            }
            for (LessonInfoVo lesson : group.getLessons())
            {
                if (!lesson.isHasPractical())
                {
                    continue;
                }
                List<PracticalGradingStatusVo> classStatuses = new ArrayList<>();
                if (lesson.getAssignedClasses() != null)
                {
                    for (String classLabel : lesson.getAssignedClasses())
                    {
                        String classCode = classLabel == null ? "" : classLabel.replace("班", "").trim();
                        classStatuses.add(deadlineService.getStatus(
                                lesson.getLessonId(), deptId, group.getEntryYear(), classCode, true));
                    }
                }
                lesson.setPracticalDeadlineClasses(classStatuses);
            }
        }
        return AjaxResult.success(dashboardData);
    }

}
