package com.ruoyi.business.controller;

import com.ruoyi.business.domain.vo.GradeGroupVo;
import com.ruoyi.business.domain.vo.LessonInfoVo;
import com.ruoyi.business.domain.vo.PracticalGradingStatusVo;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
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
import java.util.Map;
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

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    /**
     * 获取教师首页的完整数据
     */
    @GetMapping("/dashboard-data")
    public AjaxResult getDashboardData()
    {
        List<GradeGroupVo> dashboardData = lessonService.getTeacherDashboardData();
        Long deptId = SecurityUtils.getDeptId();
        Long userId = SecurityUtils.getUserId();
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
                // 历史课程可能缺少指派记录，但已有答题仍需提醒教师批改；
                // 与批改弹窗复用同一班级事实范围，避免首页红点和弹窗状态不一致。
                List<Map<String, Object>> classRows = studentAnswerMapper.selectClassStatusByLesson(
                        lesson.getLessonId(), userId, deptId);
                for (Map<String, Object> classRow : classRows)
                {
                    String entryYear = String.valueOf(classRow.get("entryYear"));
                    String classCode = String.valueOf(classRow.get("classCode"));
                    classStatuses.add(deadlineService.getStatus(
                            lesson.getLessonId(), deptId, entryYear, classCode, true));
                }
                lesson.setPracticalDeadlineClasses(classStatuses);
            }
        }
        return AjaxResult.success(dashboardData);
    }

}
