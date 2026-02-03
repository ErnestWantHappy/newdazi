package com.ruoyi.business.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.business.domain.vo.StudentProfileVo;
import com.ruoyi.business.service.IStudentProfileService;

/**
 * 学生成绩画像 Controller
 */
@RestController
@RequestMapping("/business/student-profile")
public class StudentProfileController extends BaseController {

    @Autowired
    private IStudentProfileService studentProfileService;

    /**
     * 获取学生画像汇总数据
     */
    @GetMapping("/summary/{studentId}")
    public AjaxResult getSummary(
        @PathVariable Long studentId,
        @RequestParam(required = false) String semesterStart,
        @RequestParam(required = false) String semesterEnd
    ) {
        StudentProfileVo profile = studentProfileService.getStudentProfile(studentId, semesterStart, semesterEnd);
        if (profile == null) {
            return AjaxResult.error("未找到学生信息");
        }
        return AjaxResult.success(profile);
    }

    /**
     * 获取历次课程成绩（含班级平均对比）
     */
    @GetMapping("/courses/{studentId}")
    public AjaxResult getCourseScores(
        @PathVariable Long studentId,
        @RequestParam(required = false) String semesterStart,
        @RequestParam(required = false) String semesterEnd
    ) {
        List<StudentProfileVo.CourseScoreItem> list = studentProfileService.getCourseScores(studentId, semesterStart, semesterEnd);
        return AjaxResult.success(list);
    }

    /**
     * 获取打字速度数据
     */
    @GetMapping("/typing/{studentId}")
    public AjaxResult getTypingSpeeds(
        @PathVariable Long studentId,
        @RequestParam(required = false) String semesterStart,
        @RequestParam(required = false) String semesterEnd
    ) {
        List<StudentProfileVo.TypingSpeedItem> list = studentProfileService.getTypingSpeeds(studentId, semesterStart, semesterEnd);
        return AjaxResult.success(list);
    }

    /**
     * 获取课堂表现分数据
     */
    @GetMapping("/performance/{studentId}")
    public AjaxResult getPerformances(
        @PathVariable Long studentId,
        @RequestParam(required = false) String semesterStart,
        @RequestParam(required = false) String semesterEnd
    ) {
        List<StudentProfileVo.PerformanceItem> list = studentProfileService.getPerformances(studentId, semesterStart, semesterEnd);
        return AjaxResult.success(list);
    }

    /**
     * 获取排名变化数据
     */
    @GetMapping("/rank/{studentId}")
    public AjaxResult getRankChanges(
        @PathVariable Long studentId,
        @RequestParam(required = false) String semesterStart,
        @RequestParam(required = false) String semesterEnd
    ) {
        List<StudentProfileVo.RankItem> list = studentProfileService.getRankChanges(studentId, semesterStart, semesterEnd);
        return AjaxResult.success(list);
    }

    /**
     * 获取班级列表（年级/班级）
     */
    @GetMapping("/classes")
    public AjaxResult getClasses() {
        return AjaxResult.success(studentProfileService.getClassList());
    }

    /**
     * 获取指定班级的学生列表
     */
    @GetMapping("/list")
    public AjaxResult getStudentList(
        @RequestParam String entryYear,
        @RequestParam String classCode
    ) {
        return AjaxResult.success(studentProfileService.getStudentList(entryYear, classCode));
    }
}
