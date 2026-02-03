package com.ruoyi.business.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.business.domain.BizClassroomPerformance;
import com.ruoyi.business.mapper.BizClassroomPerformanceMapper;

/**
 * 课堂表现（平时分）Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/classroom-performance")
public class ClassroomPerformanceController extends BaseController {

    @Autowired
    private BizClassroomPerformanceMapper performanceMapper;

    /**
     * 查询班级的课堂表现列表
     */
    @GetMapping("/list")
    public AjaxResult list(
            @RequestParam Long lessonId,
            @RequestParam String classCode,
            @RequestParam String entryYear) {
        List<BizClassroomPerformance> list = performanceMapper.selectListByLessonAndClass(lessonId, classCode, entryYear);
        return AjaxResult.success(list);
    }

    /**
     * 保存/更新课堂表现
     */
    @PostMapping("/save")
    public AjaxResult save(@RequestBody BizClassroomPerformance performance) {
        // 参数校验
        if (performance.getStudentId() == null || performance.getLessonId() == null) {
            return AjaxResult.error("参数不完整");
        }
        if (performance.getScore() == null) {
            performance.setScore(0);
        }
        // 分数范围限制：-10 ~ +10
        if (performance.getScore() < -10 || performance.getScore() > 10) {
            return AjaxResult.error("平时分范围为 -10 到 +10");
        }
        
        // 设置教师ID
        performance.setTeacherId(SecurityUtils.getUserId());
        
        // 使用 INSERT ... ON DUPLICATE KEY UPDATE
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
        int successCount = 0;
        
        for (PerformanceItem item : request.getPerformances()) {
            BizClassroomPerformance performance = new BizClassroomPerformance();
            performance.setStudentId(item.getStudentId());
            performance.setLessonId(request.getLessonId());
            performance.setScore(item.getScore() != null ? item.getScore() : 0);
            performance.setReason(item.getReason());
            performance.setTeacherId(teacherId);
            
            // 分数范围限制
            if (performance.getScore() < -10) performance.setScore(-10);
            if (performance.getScore() > 10) performance.setScore(10);
            
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
        BizClassroomPerformance performance = performanceMapper.selectByStudentAndLesson(studentId, lessonId);
        return AjaxResult.success(performance);
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
