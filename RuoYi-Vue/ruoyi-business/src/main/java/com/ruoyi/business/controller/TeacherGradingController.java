package com.ruoyi.business.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
import com.ruoyi.business.domain.PracticalRubricSnapshot;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.PracticalSubmissionVo;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.business.service.PracticalScoringPolicyService;
import com.ruoyi.business.service.PracticalGradingDeadlineService;
import com.ruoyi.business.service.PracticalArtifactService;
import com.ruoyi.business.service.PracticalRubricSnapshotService;
import com.ruoyi.business.service.ClassroomTaskStateService;

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

    @Autowired
    private PracticalScoringPolicyService practicalScoringPolicyService;

    @Autowired
    private PracticalArtifactService practicalArtifactService;

    @Autowired
    private PracticalRubricSnapshotService rubricSnapshotService;

    @Autowired
    private ClassroomTaskStateService classroomTaskStateService;

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
        for (java.util.Map<String, Object> row : result)
        {
            String entryYear = String.valueOf(row.get("entryYear"));
            String classCode = String.valueOf(row.get("classCode"));
            com.ruoyi.business.domain.vo.PracticalGradingStatusVo deadlineStatus =
                    practicalGradingDeadlineService.getStatus(
                            lessonId, deptId, entryYear, classCode, false);
            row.put("deadlineStatusCode", deadlineStatus.getStatusCode());
            row.put("currentDeadlineTime", deadlineStatus.getCurrentDeadlineTime());
            row.put("serverNow", deadlineStatus.getServerNow());
            row.put("canGrade", deadlineStatus.isCanGrade());
        }
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
        practicalArtifactService.enrichSubmissions(submissions);
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
        List<PracticalSubmissionVo> submissions = studentAnswerMapper.selectPracticalSubmissions(
                request.getLessonId(), request.getQuestionId(), request.getClassCode().trim(),
                request.getEntryYear().trim(), deptId);
        practicalArtifactService.enrichSubmissions(submissions);
        result.put("normalizedAccepted", practicalArtifactService.retryFailedAttachments(submissions));
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

        BizStudentAnswer answer = studentAnswerMapper.selectByIdForUpdate(request.getAnswerId());
        if (answer == null) {
            return AjaxResult.error("答题记录不存在");
        }
        String scopeError = validateGradeScope(answer.getStudentId(), answer.getLessonId());
        if (scopeError != null) {
            return AjaxResult.error(scopeError);
        }
        if (StringUtils.isBlank(answer.getStudentAnswer())) {
            return AjaxResult.error("学生尚未提交操作题作品");
        }
        boolean answerChanged = answer.getPracticalVersionId() != null
                ? !Objects.equals(request.getPracticalVersionId(), answer.getPracticalVersionId())
                : request.getSubmitTime() == null || !request.getSubmitTime().equals(answer.getSubmitTime());
        if (answerChanged || !Objects.equals(request.getExpectedScore(), answer.getScore())) {
            return AjaxResult.error("答卷或成绩已发生变化，请刷新后重新批改");
        }

        BizLessonQuestionDetailVo lessonQuestion = findPracticalQuestion(
                answer.getLessonId(), answer.getQuestionId());
        if (lessonQuestion == null || lessonQuestion.getQuestionScore() == null
                || lessonQuestion.getQuestionScore() < 0
                || lessonQuestion.getQuestionScore() > Integer.MAX_VALUE) {
            return AjaxResult.error("当前课程的操作题分值配置无效");
        }
        PracticalRubricSnapshot rubricSnapshot = rubricSnapshotService.resolve(
                answer.getPracticalVersionId(), answer.getLessonId(), lessonQuestion,
                SecurityUtils.getUserId());
        if (rubricSnapshot == null) {
            return AjaxResult.error("评分标准快照不存在，请刷新后重试");
        }
        if (request.getRubricSnapshotId() != null
                && !Objects.equals(request.getRubricSnapshotId(), rubricSnapshot.getSnapshotId())) {
            return AjaxResult.error("评分标准已发生变化，请刷新后重新批改");
        }
        int questionScore = rubricSnapshot.getQuestionScore();

        List<com.ruoyi.business.domain.BizScoringDetail> scoringDetails =
                new ArrayList<com.ruoyi.business.domain.BizScoringDetail>();
        if (request.getScoringDetails() != null) {
            for (ScoringDetailRequest detail : request.getScoringDetails()) {
                com.ruoyi.business.domain.BizScoringDetail scoringDetail =
                        new com.ruoyi.business.domain.BizScoringDetail();
                if (detail != null) {
                    scoringDetail.setAnswerId(request.getAnswerId());
                    scoringDetail.setItemId(detail.getItemId());
                    scoringDetail.setScore(detail.getScore());
                    scoringDetail.setStarCount(detail.getStarCount());
                }
                scoringDetails.add(scoringDetail);
            }
        }
        List<com.ruoyi.business.domain.vo.PracticalScoringItemVo> scoringItems =
                rubricSnapshotService.buildScoringItems(rubricSnapshot);
        int finalScore = practicalScoringPolicyService.resolveFinalScore(
                request.getMode(), request.getScore(), request.getStarCount(),
                questionScore, scoringItems, scoringDetails);

        // 期限校验必须和改分处于同一事务，避免已逾期后仍写入部分评分明细。
        practicalGradingDeadlineService.assertCanGrade(request.getAnswerId());
        studentAnswerMapper.updateScore(request.getAnswerId(), finalScore);

        // 切回直接打分时也要清除旧分项，避免页面再次打开显示过期明细。
        scoringDetailMapper.deleteBizScoringDetailByAnswerId(request.getAnswerId());
        for (com.ruoyi.business.domain.BizScoringDetail detail : scoringDetails) {
            scoringDetailMapper.insertBizScoringDetail(detail);
        }

        BizStudent student = studentMapper.selectBizStudentByStudentId(answer.getStudentId());
        if (student == null || student.getDeptId() == null) throw new ServiceException("学生档案不存在");
        // 管理员批改时当前部门未必就是学生学校，状态归属必须使用学生档案的学校。
        classroomTaskStateService.markSafely(student, student.getDeptId(), answer.getLessonId(),
                answer.getQuestionId(), ClassroomTaskStateService.GRADED);

        return AjaxResult.success("批改成功");
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
                                      @RequestParam Long questionId,
                                      @RequestParam(required = false) Long practicalVersionId) {
        assertLessonVisibleToCurrentTeacher(lessonId);
        BizLessonQuestionDetailVo lessonQuestion = findPracticalQuestion(lessonId, questionId);
        if (lessonQuestion == null) {
            throw new ServiceException("题目不属于当前课程");
        }
        if (lessonQuestion.getQuestionScore() == null || lessonQuestion.getQuestionScore() < 0
                || lessonQuestion.getQuestionScore() > Integer.MAX_VALUE) {
            throw new ServiceException("当前课程的操作题分值配置无效");
        }
        PracticalRubricSnapshot snapshot = rubricSnapshotService.resolve(
                practicalVersionId, lessonId, lessonQuestion, SecurityUtils.getUserId());
        return AjaxResult.success(rubricSnapshotService.buildScoringItems(snapshot));
    }

    private BizLessonQuestionDetailVo findPracticalQuestion(Long lessonId, Long questionId) {
        if (lessonId == null || questionId == null) {
            return null;
        }
        return lessonQuestionMapper.selectDetailsByLessonId(lessonId).stream()
                .filter(question -> questionId.equals(question.getQuestionId()))
                .filter(question -> "practical".equals(question.getQuestionType()))
                .findFirst()
                .orElse(null);
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
        private Integer expectedScore;
        private Long practicalVersionId;
        private Long rubricSnapshotId;
        private String mode;
        private Integer starCount;
        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date submitTime;
        private java.util.List<ScoringDetailRequest> scoringDetails;

        public Long getAnswerId() { return answerId; }
        public void setAnswerId(Long answerId) { this.answerId = answerId; }

        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }

        public Integer getExpectedScore() { return expectedScore; }
        public void setExpectedScore(Integer expectedScore) { this.expectedScore = expectedScore; }

        public Long getPracticalVersionId() { return practicalVersionId; }
        public void setPracticalVersionId(Long practicalVersionId) { this.practicalVersionId = practicalVersionId; }

        public Long getRubricSnapshotId() { return rubricSnapshotId; }
        public void setRubricSnapshotId(Long rubricSnapshotId) { this.rubricSnapshotId = rubricSnapshotId; }

        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }

        public Integer getStarCount() { return starCount; }
        public void setStarCount(Integer starCount) { this.starCount = starCount; }

        public Date getSubmitTime() { return submitTime; }
        public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }

        public java.util.List<ScoringDetailRequest> getScoringDetails() { return scoringDetails; }
        public void setScoringDetails(java.util.List<ScoringDetailRequest> scoringDetails) {
            this.scoringDetails = scoringDetails;
        }
    }

    public static class ScoringDetailRequest {
        private Long itemId;
        private Integer score;
        private Integer starCount;

        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }

        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }

        public Integer getStarCount() { return starCount; }
        public void setStarCount(Integer starCount) { this.starCount = starCount; }
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
