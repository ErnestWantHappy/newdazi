package com.ruoyi.business.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.business.domain.vo.PracticalSubmissionVo;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;

import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.domain.BizLessonAssignment;

/**
 * 教师批改操作题 Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/teacher/grading")
public class TeacherGradingController extends BaseController {

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;
    
    @Autowired
    private BizLessonQuestionMapper lessonQuestionMapper;
    
    @Autowired
    private BizLessonAssignmentMapper lessonAssignmentMapper;
    
    @Autowired
    private com.ruoyi.business.mapper.BizStudentMapper studentMapper;

    /**
     * 获取课程分配的班级列表（用于班级选择下拉框）
     * P4: 同时返回每个班级的学生人数
     */
    @GetMapping("/classes/{lessonId}")
    public AjaxResult getClassesByLesson(@PathVariable Long lessonId) {
        BizLessonAssignment query = new BizLessonAssignment();
        query.setLessonId(lessonId);
        List<BizLessonAssignment> assignments = lessonAssignmentMapper.selectBizLessonAssignmentList(query);
        
        // 获取当前登录用户信息
        Long userId = com.ruoyi.common.utils.SecurityUtils.getUserId();
        Long deptId = com.ruoyi.common.utils.SecurityUtils.getDeptId();
        
        // P4: 为每个班级查询学生人数
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (BizLessonAssignment assignment : assignments) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("classCode", assignment.getClassCode());
            item.put("entryYear", assignment.getEntryYear());
            item.put("assignmentId", assignment.getAssignmentId());
            // 查询该班级的学生总数
            com.ruoyi.business.domain.BizStudent studentQuery = new com.ruoyi.business.domain.BizStudent();
            studentQuery.setClassCode(assignment.getClassCode());
            studentQuery.setEntryYear(assignment.getEntryYear());
            studentQuery.setTeacherUserId(userId);  // 设置教师用户ID
            studentQuery.setDeptId(deptId);          // 设置部门ID
            List<?> students = studentMapper.selectBizStudentList(studentQuery);
            item.put("totalStudents", students.size());
            result.add(item);
        }
        return AjaxResult.success(result);
    }

    /**
     * 获取课程的操作题列表（用于选择要批改的题目）
     */
    @GetMapping("/practical-questions/{lessonId}")
    public AjaxResult getPracticalQuestions(@PathVariable Long lessonId) {
        List<BizLessonQuestionDetailVo> questions = lessonQuestionMapper.selectDetailsByLessonId(lessonId);
        // 只返回操作题
        List<BizLessonQuestionDetailVo> practicalQuestions = questions.stream()
            .filter(q -> "practical".equals(q.getQuestionType()))
            .collect(java.util.stream.Collectors.toList());
        return AjaxResult.success(practicalQuestions);
    }

    /**
     * P5: 获取班级所有学生的操作题提交情况（含未提交）
     */
    @GetMapping("/practical-submissions")
    public AjaxResult getPracticalSubmissions(
            @RequestParam Long lessonId,
            @RequestParam(required = false) Long questionId,
            @RequestParam(required = false) String classCode,
            @RequestParam(required = false) String entryYear) {
        List<PracticalSubmissionVo> submissions = studentAnswerMapper.selectPracticalSubmissions(lessonId, questionId, classCode, entryYear);
        return AjaxResult.success(submissions);
    }

    @Autowired
    private com.ruoyi.business.mapper.BizScoringDetailMapper scoringDetailMapper;
    
    @Autowired
    private com.ruoyi.business.mapper.BizScoringItemMapper scoringItemMapper;

    /**
     * P6: 批改打分（支持分项评分）
     */
    @PostMapping("/grade")
    public AjaxResult grade(@RequestBody GradeRequest request) {
        if (request.getAnswerId() == null || request.getScore() == null) {
            return AjaxResult.error("参数不完整");
        }
        if (request.getScore() < 0) {
            return AjaxResult.error("分数不能为负数");
        }
        
        // 保存总分
        int rows = studentAnswerMapper.updateScore(request.getAnswerId(), request.getScore());
        
        // P6: 如果有分项得分，也保存（先删除旧的）
        if (request.getScoringDetails() != null && !request.getScoringDetails().isEmpty()) {
            // 先删除该答题的旧分项得分
            scoringDetailMapper.deleteBizScoringDetailByAnswerId(request.getAnswerId());
            // 再插入新的
            for (ScoringDetailRequest detail : request.getScoringDetails()) {
                com.ruoyi.business.domain.BizScoringDetail sd = new com.ruoyi.business.domain.BizScoringDetail();
                sd.setAnswerId(request.getAnswerId());
                sd.setItemId(detail.getItemId());
                sd.setScore(detail.getScore());
                scoringDetailMapper.insertBizScoringDetail(sd);
            }
        }
        
        return rows > 0 ? AjaxResult.success("批改成功") : AjaxResult.error("批改失败");
    }
    
    /**
     * P6: 获取答题的分项得分
     */
    @GetMapping("/scoring-details/{answerId}")
    public AjaxResult getScoringDetails(@PathVariable Long answerId) {
        return AjaxResult.success(scoringDetailMapper.selectDetailsByAnswerId(answerId));
    }
    
    /**
     * P6: 获取题目的评分项列表
     */
    @GetMapping("/scoring-items")
    public AjaxResult getScoringItems(@RequestParam(required = false) Long lessonId, @RequestParam Long questionId) {
        return AjaxResult.success(scoringItemMapper.selectItemsByQuestion(questionId));
    }

    /**
     * 批改请求体
     */
    public static class GradeRequest {
        private Long answerId;
        private Integer score;
        private java.util.List<ScoringDetailRequest> scoringDetails;  // P6: 分项得分列表

        public Long getAnswerId() { return answerId; }
        public void setAnswerId(Long answerId) { this.answerId = answerId; }

        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
        
        public java.util.List<ScoringDetailRequest> getScoringDetails() { return scoringDetails; }
        public void setScoringDetails(java.util.List<ScoringDetailRequest> scoringDetails) { this.scoringDetails = scoringDetails; }
    }
    
    /**
     * P6: 分项得分请求体
     */
    public static class ScoringDetailRequest {
        private Long itemId;
        private Integer score;
        
        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }
        
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
    }
}

