package com.ruoyi.business.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.business.domain.BizClassroomPerformance;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.BizClassroomPerformanceMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.service.GuideSheetAccessService;

/**
 * 课堂表现（平时分）Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/classroom-performance")
@PreAuthorize("@ss.hasPermi('business:score:list') or @ss.hasRole('teacher') or @ss.hasRole('admin')")
public class ClassroomPerformanceController extends BaseController {

    @Autowired
    private BizClassroomPerformanceMapper performanceMapper;

    @Autowired
    private BizStudentMapper studentMapper;

    @Autowired
    private GuideSheetAccessService guideSheetAccessService;

    /**
     * 查询班级的课堂表现列表
     */
    @GetMapping("/list")
    public AjaxResult list(
            @RequestParam Long lessonId,
            @RequestParam String classCode,
            @RequestParam String entryYear) {
        guideSheetAccessService.assertCanViewLessonClass(lessonId, entryYear, classCode);
        Long deptId = SecurityUtils.getDeptId();
        List<BizClassroomPerformance> list =
                performanceMapper.selectListByLessonAndClass(lessonId, classCode, entryYear, deptId);
        return AjaxResult.success(list);
    }

    /**
     * 保存/更新课堂表现
     */
    @PostMapping("/save")
    public AjaxResult save(@RequestBody BizClassroomPerformance performance) {
        if (performance.getStudentId() == null || performance.getLessonId() == null) {
            return AjaxResult.error("参数不完整");
        }
        if (performance.getScore() == null) {
            performance.setScore(0);
        }
        if (performance.getScore() < -10 || performance.getScore() > 10) {
            return AjaxResult.error("平时分范围为 -10 到 +10");
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(performance.getReason())) {
            return AjaxResult.error("请填写课堂表现原因");
        }

        String scopeError = validateStudentLessonScope(performance.getStudentId(), performance.getLessonId());
        if (scopeError != null) {
            return AjaxResult.error(scopeError);
        }

        BizClassroomPerformance existing = performanceMapper.selectByStudentAndLesson(performance.getStudentId(), performance.getLessonId());
        if (existing != null && Integer.valueOf(1).equals(existing.getIsAbsent())) {
            return AjaxResult.error("该学生本节课已请假，请先取消请假后再记录课堂表现");
        }

        performance.setTeacherId(SecurityUtils.getUserId());
        performance.setDeptId(SecurityUtils.getDeptId());

        int rows = performanceMapper.insertOrUpdate(performance);
        return rows > 0 ? AjaxResult.success("保存成功") : AjaxResult.error("保存失败");
    }

    /**
     * 批量保存课堂表现
     */
    @PostMapping("/batch-save")
    public AjaxResult batchSave(@RequestBody BatchSaveRequest request) {
        if (request.getLessonId() == null || request.getPerformances() == null) {
            return AjaxResult.error("参数不完整");
        }

        Long teacherId = SecurityUtils.getUserId();
        Long deptId = SecurityUtils.getDeptId();
        int successCount = 0;

        for (PerformanceItem item : request.getPerformances()) {
            if (item.getStudentId() == null) {
                continue;
            }
            String scopeError = validateStudentLessonScope(item.getStudentId(), request.getLessonId());
            if (scopeError != null) {
                return AjaxResult.error(scopeError);
            }

            BizClassroomPerformance performance = new BizClassroomPerformance();
            performance.setStudentId(item.getStudentId());
            performance.setLessonId(request.getLessonId());
            performance.setScore(item.getScore() != null ? item.getScore() : 0);
            performance.setReason(item.getReason());
            performance.setTeacherId(teacherId);
            performance.setDeptId(deptId);

            if (org.apache.commons.lang3.StringUtils.isBlank(performance.getReason())) {
                return AjaxResult.error("请填写每条课堂表现的原因");
            }
            BizClassroomPerformance existing = performanceMapper.selectByStudentAndLesson(item.getStudentId(), request.getLessonId());
            if (existing != null && Integer.valueOf(1).equals(existing.getIsAbsent())) {
                return AjaxResult.error("存在已请假的学生，请先取消请假后再记录课堂表现");
            }

            if (performance.getScore() < -10) {
                performance.setScore(-10);
            }
            if (performance.getScore() > 10) {
                performance.setScore(10);
            }

            performanceMapper.insertOrUpdate(performance);
            successCount++;
        }

        return AjaxResult.success("成功保存 " + successCount + " 条记录");
    }

    /**
     * 查询单个学生的课堂表现
     */
    @GetMapping("/get")
    public AjaxResult get(@RequestParam Long studentId, @RequestParam Long lessonId) {
        String scopeError = validateStudentLessonScope(studentId, lessonId);
        if (scopeError != null) {
            return AjaxResult.error(scopeError);
        }
        BizClassroomPerformance performance = performanceMapper.selectByStudentAndLesson(studentId, lessonId);
        return AjaxResult.success(performance);
    }

    private String validateStudentLessonScope(Long studentId, Long lessonId) {
        Long deptId = SecurityUtils.getDeptId();
        BizStudent student = studentMapper.selectBizStudentByStudentId(studentId);
        if (student == null) {
            return "学生不存在";
        }
        if (student.getDeptId() != null && !deptId.equals(student.getDeptId())) {
            return "不能修改其他学校的学生表现分";
        }
        try {
            guideSheetAccessService.assertCanViewLessonClass(
                    lessonId, student.getEntryYear(), student.getClassCode());
        } catch (ServiceException e) {
            return e.getMessage();
        }
        return null;
    }

    // ============ 请求体定义 ============

    public static class BatchSaveRequest {
        private Long lessonId;
        private List<PerformanceItem> performances;

        public Long getLessonId() { return lessonId; }
        public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
        public List<PerformanceItem> getPerformances() { return performances; }
        public void setPerformances(List<PerformanceItem> performances) { this.performances = performances; }
    }

    public static class PerformanceItem {
        private Long studentId;
        private Integer score;
        private String reason;

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
