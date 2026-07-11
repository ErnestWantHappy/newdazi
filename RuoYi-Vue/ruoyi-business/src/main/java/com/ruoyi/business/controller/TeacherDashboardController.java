package com.ruoyi.business.controller;

import com.ruoyi.business.domain.vo.GradeGroupVo;
import com.ruoyi.business.domain.vo.LessonRankingVo;
import com.ruoyi.business.service.IBizLessonService;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    /**
     * 获取教师首页的完整数据
     */
    @GetMapping("/dashboard-data")
    public AjaxResult getDashboardData()
    {
        List<GradeGroupVo> dashboardData = lessonService.getTeacherDashboardData();
        return AjaxResult.success(dashboardData);
    }

    /**
     * 获取课程排行榜（实时成绩排名）
     * @param lessonId 课程ID
     */
    @GetMapping("/lesson-ranking/{lessonId}")
    public AjaxResult getLessonRanking(@PathVariable Long lessonId)
    {
        List<LessonRankingVo> rankings = studentAnswerMapper.selectLessonRanking(lessonId);
        // 添加排名序号
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRanking(i + 1);
        }
        return AjaxResult.success(rankings);
    }
}
