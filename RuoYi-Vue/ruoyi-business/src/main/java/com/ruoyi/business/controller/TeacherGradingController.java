package com.ruoyi.business.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.PracticalSubmissionVo;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.business.service.PracticalGradingDeadlineService;

/**
 * 教师批改操作题 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/teacher/grading")
@PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('admin')")
public class TeacherGradingController extends BaseController {

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    @Autowired
    private BizLessonQuestionMapper lessonQuestionMapper;

    @Autowired
    private BizLessonMapper lessonMapper;

    @Autowired
    private BizStudentMapper studentMapper;

    @Autowired
    private GuideSheetAccessService guideSheetAccessService;

    @Autowired
    private PracticalGradingDeadlineService practicalGradingDeadlineService;

    @Autowired
    private com.ruoyi.business.service.PracticalPreviewRetryService practicalPreviewRetryService;

    @Autowired
    private com.ruoyi.business.mapper.BizScoringDetailMapper scoringDetailMapper;

    @Autowired
    private com.ruoyi.business.mapper.BizScoringItemMapper scoringItemMapper;

    /**
     * 获取课程的班级列表（用于批改页面班级选择下拉框）
     */
    @GetMapping("/classes/{lessonId}")
    public AjaxResult getClassesByLesson(@PathVariable Long lessonId) {
        assertLessonVisibleToCurrentTeacher(lessonId);
        Long userId = SecurityUtils.getUserId();
        Long deptId = SecurityUtils.getDeptId();
        List<java.util.Map<String, Object>> result =
                studentAnswerMapper.selectClassStatusByLesson(lessonId, userId, deptId);
        return AjaxResult.success(result);
    }

    /**
     * 获取课程的操作题列表（用于选择要批改的题目）
     */
    @GetMapping("/practical-questions/{lessonId}")
    public AjaxResult getPracticalQuestions(@PathVariable Long lessonId) {
        assertLessonVisibleToCurrentTeacher(lessonId);
        List<BizLessonQuestionDetailVo> questions = lessonQuestionMapper.selectDetailsByLessonId(lessonId);
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
        Long deptId = SecurityUtils.getDeptId();
        if (StringUtils.isBlank(classCode) || StringUtils.isBlank(entryYear)) {
            throw new com.ruoyi.common.exception.ServiceException("入学年份和班级编号必须同时提供");
        }
        guideSheetAccessService.assertCanViewLessonClass(lessonId, entryYear.trim(), classCode.trim());
        List<PracticalSubmissionVo> submissions = studentAnswerMapper.selectPracticalSubmissions(
                lessonId, questionId, classCode.trim(), entryYear.trim(), deptId);
        return AjaxResult.success(submissions);
    }

    /**
     * 当前课程、班级、操作题下失败文件重新转换
     */
    @PostMapping("/retry-failed-previews")
    public AjaxResult retryFailedPreviews(@RequestBody RetryPreviewRequest request) {
        if (request.getLessonId() == null || request.getQuestionId() == null) {
            return AjaxResult.error("课程和操作题不能为空");
        }
        if (StringUtils.isBlank(request.getClassCode()) || StringUtils.isBlank(request.getEntryYear())) {
            return AjaxResult.error("班级和入学年份不能为空");
        }

        Long deptId = SecurityUtils.getDeptId();
        guideSheetAccessService.assertCanViewLessonClass(
                request.getLessonId(), request.getEntryYear().trim(), request.getClassCode().trim());
        java.util.Map<String, Object> result = practicalPreviewRetryService.retryFailedPreviewsByQuestionAndClass(
                request.getLessonId(), request.getQuestionId(), request.getClassCode(), request.getEntryYear(), deptId);
        return AjaxResult.success(result);
    }

    /**
     * P6: 批改打分（支持分项评分）。
     * 必须校验答题所属学生/课程是否在当前教师管理范围内，禁止仅凭 answerId 越权改分。
     */
    @PostMapping("/grade")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult grade(@RequestBody GradeRequest request) {
        if (request.getAnswerId() == null || request.getScore() == null) {
            return AjaxResult.error("参数不完整");
        }
        if (request.getScore() < 0) {
            return AjaxResult.error("分数不能为负数");
        }
        if (request.getScore() > 100) {
            return AjaxResult.error("分数不能超过100");
        }

        BizStudentAnswer answer = studentAnswerMapper.selectById(request.getAnswerId());
        if (answer == null) {
            return AjaxResult.error("答题记录不存在");
        }
        String scopeError = validateGradeScope(answer.getStudentId(), answer.getLessonId());
        if (scopeError != null) {
            return AjaxResult.error(scopeError);
        }

        // 期限校验必须和改分处于同一事务，避免已逾期后仍写入部分评分明细。
        practicalGradingDeadlineService.assertCanGrade(request.getAnswerId());
        int rows = studentAnswerMapper.updateScore(request.getAnswerId(), request.getScore());

        if (request.getScoringDetails() != null && !request.getScoringDetails().isEmpty()) {
            scoringDetailMapper.deleteBizScoringDetailByAnswerId(request.getAnswerId());
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
     * 获取课程班级操作题批改期限状态，页面倒计时统一使用服务端时间计算。
     */
    @GetMapping("/deadline-status")
    public AjaxResult getDeadlineStatus(@RequestParam Long lessonId,
                                        @RequestParam String entryYear,
                                        @RequestParam String classCode) {
        if (StringUtils.isBlank(entryYear) || StringUtils.isBlank(classCode)) {
            throw new ServiceException("入学年份和班级编号不能为空");
        }
        guideSheetAccessService.assertCanViewLessonClass(
                lessonId, entryYear.trim(), classCode.trim());
        return AjaxResult.success(practicalGradingDeadlineService.getStatus(
                lessonId, SecurityUtils.getDeptId(), entryYear.trim(), classCode.trim(), true));
    }

    /**
     * P6: 获取答题的分项得分
     */
    @GetMapping("/scoring-details/{answerId}")
    public AjaxResult getScoringDetails(@PathVariable Long answerId) {
        BizStudentAnswer answer = studentAnswerMapper.selectById(answerId);
        if (answer == null) {
            return AjaxResult.error("答题记录不存在");
        }
        String scopeError = validateGradeScope(answer.getStudentId(), answer.getLessonId());
        if (scopeError != null) {
            return AjaxResult.error(scopeError);
        }
        return AjaxResult.success(scoringDetailMapper.selectDetailsByAnswerId(answerId));
    }

    /**
     * P6: 获取题目的评分项列表
     */
    @GetMapping("/scoring-items")
    public AjaxResult getScoringItems(@RequestParam Long lessonId,
                                      @RequestParam Long questionId) {
        assertLessonVisibleToCurrentTeacher(lessonId);
        boolean questionInLesson = lessonQuestionMapper.selectDetailsByLessonId(lessonId).stream()
                .anyMatch(question -> questionId.equals(question.getQuestionId()));
        if (!questionInLesson) {
            throw new ServiceException("题目不属于当前课程");
        }
        return AjaxResult.success(scoringItemMapper.selectItemsByQuestion(questionId));
    }

    /**
     * 与成绩改分一致：本校 + 课程已指派该班 +（创建人/管理员/任教班级）。
     */
    private String validateGradeScope(Long studentId, Long lessonId) {
        if (studentId == null || lessonId == null) {
            return "答题记录数据不完整";
        }
        Long deptId = SecurityUtils.getDeptId();
        BizStudent student = studentMapper.selectBizStudentByStudentId(studentId);
        if (student == null) {
            return "学生不存在";
        }
        if (student.getDeptId() != null && !deptId.equals(student.getDeptId())) {
            return "不能批改其他学校的学生";
        }
        try {
            guideSheetAccessService.assertCanViewLessonClass(
                    lessonId, student.getEntryYear(), student.getClassCode());
        } catch (ServiceException e) {
            return e.getMessage();
        }
        return null;
    }

    private BizLesson assertLessonVisibleToCurrentTeacher(Long lessonId) {
        if (lessonId == null) {
            throw new ServiceException("课程不能为空");
        }
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        Long deptId = SecurityUtils.getDeptId();
        if (lesson == null || lesson.getDeptId() == null || !lesson.getDeptId().equals(deptId)) {
            throw new ServiceException("课程不存在或不属于当前学校");
        }
        Long userId = SecurityUtils.getUserId();
        boolean creator = userId.equals(lesson.getCreatorId())
                || (lesson.getCreatorId() == null && SecurityUtils.getUsername().equals(lesson.getCreateBy()));
        if (!SecurityUtils.isAdmin(userId) && !creator
                && studentAnswerMapper.selectClassStatusByLesson(lessonId, userId, deptId).isEmpty()) {
            throw new ServiceException("无权查看其他教师的课程批改数据");
        }
        return lesson;
    }

    // ============ 请求体定义 ============

    public static class GradeRequest {
        private Long answerId;
        private Integer score;
        private java.util.List<ScoringDetailRequest> scoringDetails;

        public Long getAnswerId() { return answerId; }
        public void setAnswerId(Long answerId) { this.answerId = answerId; }

        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }

        public java.util.List<ScoringDetailRequest> getScoringDetails() { return scoringDetails; }
        public void setScoringDetails(java.util.List<ScoringDetailRequest> scoringDetails) {
            this.scoringDetails = scoringDetails;
        }
    }

    public static class ScoringDetailRequest {
        private Long itemId;
        private Integer score;

        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }

        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
    }

    public static class RetryPreviewRequest {
        private Long lessonId;
        private Long questionId;
        private String classCode;
        private String entryYear;

        public Long getLessonId() { return lessonId; }
        public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }

        public String getClassCode() { return classCode; }
        public void setClassCode(String classCode) { this.classCode = classCode; }

        public String getEntryYear() { return entryYear; }
        public void setEntryYear(String entryYear) { this.entryYear = entryYear; }
    }
}
