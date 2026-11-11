package com.ruoyi.business.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Isolation;
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
    @Autowired private com.ruoyi.business.mapper.BizLessonAssignmentMapper assignmentMapper;
    @Autowired private com.ruoyi.business.mapper.ProgrammingJudgeMapper programmingJudgeMapper;
    @Autowired private com.ruoyi.business.service.PracticalPythonRunService practicalPythonRunService;

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
        Long currentLesson = assignmentMapper.selectCurrentLessonByClass(entryYear.trim(),
                com.ruoyi.business.config.ClassroomRoomKey.normalizeClassCode(classCode), deptId);
        for (PracticalSubmissionVo row : submissions) {
            row.setCanReturn(lessonId.equals(currentLesson) && row.getScore() != null
                    && !ClassroomTaskStateService.RETURNED.equals(row.getTaskState())
                    && !"PYTHON".equalsIgnoreCase(row.getPracticalMode()) && !"DATA_LAB".equalsIgnoreCase(row.getPracticalMode()));
        }
        return AjaxResult.success(submissions);
    }

    /**
     * Python 编程题批量采用系统判题最高分。
     * 只覆盖已正式提交且已有系统判题分的学生；已退回、无系统分或分数越界的学生跳过。
     */
    @PostMapping("/python/batch-adopt-system-scores")
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public AjaxResult batchAdoptPythonSystemScores(@RequestBody PythonBatchAdoptRequest request) {
        if (request.getLessonId() == null || request.getQuestionId() == null) {
            return AjaxResult.error("课程和操作题不能为空");
        }
        if (StringUtils.isBlank(request.getClassCode()) || StringUtils.isBlank(request.getEntryYear())) {
            return AjaxResult.error("班级和入学年份不能为空");
        }
        String classCode = request.getClassCode().trim();
        String entryYear = request.getEntryYear().trim();
        Long deptId = SecurityUtils.getDeptId();
        guideSheetAccessService.assertCanViewLessonClass(request.getLessonId(), entryYear, classCode);
        BizLessonQuestionDetailVo lessonQuestion = findPracticalQuestion(
                request.getLessonId(), request.getQuestionId());
        if (lessonQuestion == null) {
            return AjaxResult.error("操作题不存在或不属于当前课程");
        }
        if (!"PYTHON".equalsIgnoreCase(lessonQuestion.getPracticalMode())) {
            return AjaxResult.error("仅 Python 操作题支持批量采用系统分");
        }
        if (lessonQuestion.getQuestionScore() == null || lessonQuestion.getQuestionScore() < 0) {
            return AjaxResult.error("操作题满分配置无效，不能批量采用系统分");
        }
        com.ruoyi.business.domain.vo.PracticalGradingStatusVo deadlineStatus =
                practicalGradingDeadlineService.getStatus(
                        request.getLessonId(), deptId, entryYear, classCode, false);
        if (deadlineStatus == null || !deadlineStatus.isCanGrade()) {
            return AjaxResult.error("已逾期，操作题批改已锁定");
        }
        List<PracticalSubmissionVo> submissions = studentAnswerMapper.selectPracticalSubmissions(
                request.getLessonId(), request.getQuestionId(), classCode, entryYear, deptId);
        Map<Long, PythonBatchAdoptSnapshot> expectedByStudent = new HashMap<>();
        if (request.getExpectedSnapshots() != null) {
            for (PythonBatchAdoptSnapshot snapshot : request.getExpectedSnapshots()) {
                if (snapshot != null && snapshot.getStudentId() != null) {
                    expectedByStudent.put(snapshot.getStudentId(), snapshot);
                }
            }
        }
        int totalCount = 0;
        int adoptedCount = 0;
        int skippedCount = 0;
        for (PracticalSubmissionVo row : submissions) {
            if (!Boolean.TRUE.equals(row.getSubmitted())) {
                continue;
            }
            totalCount++;
            PythonBatchAdoptSnapshot expected = expectedByStudent.get(row.getStudentId());
            // 新版页面传了快照却缺少该生，说明是页面打开后才交的，教师还没看过，不能覆盖。
            if (expected == null ? !expectedByStudent.isEmpty() : !matchesExpectedSnapshot(expected, row)) {
                // 教师页面加载后若有人改分或学生重交，不能按旧页面批量覆盖。
                skippedCount++;
                continue;
            }
            Integer systemScore = row.getSystemScore();
            if (systemScore == null
                    || systemScore < 0
                    || systemScore > lessonQuestion.getQuestionScore()) {
                skippedCount++;
                continue;
            }
            BizStudent student = studentMapper.selectBizStudentByStudentId(row.getStudentId());
            if (student == null || student.getDeptId() == null) {
                skippedCount++;
                continue;
            }
            com.ruoyi.business.domain.ProgrammingSubmission bestSubmission = null;
            BizStudentAnswer lockedAnswer;
            if (row.getAnswerId() != null) {
                lockedAnswer = studentAnswerMapper.selectByIdForUpdate(row.getAnswerId());
                if (lockedAnswer == null || !sameAnswerSnapshot(lockedAnswer, row)) {
                    skippedCount++;
                    continue;
                }
                if (classroomTaskStateService.isReturnedForUpdate(
                        row.getStudentId(), request.getLessonId(), request.getQuestionId())) {
                    skippedCount++;
                    continue;
                }
                bestSubmission = programmingJudgeMapper.selectBestFinalSubmission(
                        row.getStudentId(), request.getLessonId(), request.getQuestionId());
                if (!sameSystemSubmission(bestSubmission, row.getSystemSubmissionId(), systemScore)) {
                    skippedCount++;
                    continue;
                }
            } else {
                // 没有答题行时先锁任务状态，再按当前最高分判题记录补建；
                // 并发学生重交若已先插入答题行，insertAnswerIfAbsent 返回 0，本学生留到下一轮再处理。
                if (classroomTaskStateService.isReturnedForUpdate(
                        row.getStudentId(), request.getLessonId(), request.getQuestionId())) {
                    skippedCount++;
                    continue;
                }
                bestSubmission = programmingJudgeMapper.selectBestFinalSubmission(
                        row.getStudentId(), request.getLessonId(), request.getQuestionId());
                if (!sameSystemSubmission(bestSubmission, row.getSystemSubmissionId(), systemScore)) {
                    skippedCount++;
                    continue;
                }
                BizStudentAnswer created = buildPythonAnswer(request.getLessonId(), row, bestSubmission);
                if (studentAnswerMapper.insertAnswerIfAbsent(created) != 1) {
                    skippedCount++;
                    continue;
                }
                lockedAnswer = studentAnswerMapper.selectByStudentLessonQuestionForUpdate(
                        row.getStudentId(), request.getLessonId(), request.getQuestionId());
                if (lockedAnswer == null) {
                    throw new ServiceException("Python 答题记录补建失败");
                }
            }
            String sourceCode = bestSubmission == null ? row.getSystemSourceCode() : bestSubmission.getSourceCode();
            studentAnswerMapper.updateScoreAndStudentAnswer(lockedAnswer.getAnswerId(), systemScore, sourceCode);
            scoringDetailMapper.deleteBizScoringDetailByAnswerId(lockedAnswer.getAnswerId());
            classroomTaskStateService.markInTransaction(student, student.getDeptId(),
                    request.getLessonId(), request.getQuestionId(), ClassroomTaskStateService.GRADED);
            adoptedCount++;
        }
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<String, Object>();
        result.put("adoptedCount", adoptedCount);
        result.put("skippedCount", skippedCount);
        result.put("totalCount", totalCount);
        return AjaxResult.success(result);
    }

    /** 教师运行查看当前最高分提交，不产生判题记录，也不影响正式成绩。 */
    @PostMapping("/python/{submissionId}/run")
    public AjaxResult runPythonSubmission(@PathVariable Long submissionId,
            @RequestBody(required = false) Map<String, String> body) {
        com.ruoyi.business.domain.ProgrammingSubmission submission =
                programmingJudgeMapper.selectSubmissionById(submissionId);
        if (submission == null || !"SUBMIT".equals(submission.getSubmissionKind())) {
            throw new ServiceException("Python 提交记录不存在");
        }
        BizStudent student = studentMapper.selectBizStudentByStudentId(submission.getStudentId());
        if (student == null) throw new ServiceException("学生档案不存在");
        guideSheetAccessService.assertCanViewLessonClass(
                submission.getLessonId(), student.getEntryYear(), student.getClassCode());
        String source = submission.getSourceCode();
        if (source == null) throw new ServiceException("该提交没有可运行的代码");
        if (source.length() > 65536) throw new ServiceException("在线运行的代码不能超过64 KiB，请下载后运行");
        return AjaxResult.success(practicalPythonRunService.run(source,
                body == null ? null : body.get("stdin")));
    }

    /**
     * 教师直接运行当前展示的代码。
     *
     * 有些学生还没提交，但教师需要先验证自己的讲法与代码写法；
     * 这里同样只运行、不写判题记录和成绩，权限按课程归属校验。
     */
    @PostMapping("/python/run")
    public AjaxResult runPythonSource(@RequestBody Map<String, String> body) {
        if (body == null) throw new ServiceException("运行参数不能为空");
        Long lessonId = null;
        String rawLessonId = body.get("lessonId");
        if (StringUtils.isNotEmpty(rawLessonId)) {
            try {
                lessonId = Long.valueOf(rawLessonId.trim());
            } catch (NumberFormatException ex) {
                throw new ServiceException("课程参数无效");
            }
        }
        assertLessonVisibleToCurrentTeacher(lessonId);
        String source = body.get("sourceCode");
        if (StringUtils.isEmpty(source)) throw new ServiceException("没有可运行的代码");
        if (source.length() > 65536) throw new ServiceException("在线运行的代码不能超过64 KiB，请下载后运行");
        return AjaxResult.success(practicalPythonRunService.run(source, body.get("stdin")));
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
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public AjaxResult grade(@RequestBody GradeRequest request) {
        if (request.getScore() == null) {
            return AjaxResult.error("参数不完整");
        }

        BizStudentAnswer answer;
        com.ruoyi.business.domain.ProgrammingSubmission systemSubmission = null;
        if (request.getAnswerId() == null) {
            if (request.getSystemSubmissionId() == null) {
                return AjaxResult.error("参数不完整");
            }
            systemSubmission = programmingJudgeMapper.selectSubmissionById(request.getSystemSubmissionId());
            if (systemSubmission == null
                    || !"SUBMIT".equals(systemSubmission.getSubmissionKind())
                    || systemSubmission.getScore() == null) {
                return AjaxResult.error("系统判题记录不存在或尚未完成");
            }
            com.ruoyi.business.domain.ProgrammingSubmission best = programmingJudgeMapper.selectBestFinalSubmission(
                    systemSubmission.getStudentId(), systemSubmission.getLessonId(), systemSubmission.getQuestionId());
            if (best == null || !Objects.equals(best.getSubmissionId(), systemSubmission.getSubmissionId())) {
                return AjaxResult.error("系统判题结果已更新，请刷新后重新批改");
            }
            BizLessonQuestionDetailVo systemQuestion = findPracticalQuestion(
                    systemSubmission.getLessonId(), systemSubmission.getQuestionId());
            if (systemQuestion == null || !"PYTHON".equalsIgnoreCase(systemQuestion.getPracticalMode())) {
                return AjaxResult.error("系统判题记录不属于当前 Python 操作题");
            }
            String scopeError = validateGradeScope(systemSubmission.getStudentId(), systemSubmission.getLessonId());
            if (scopeError != null) {
                return AjaxResult.error(scopeError);
            }
            if (classroomTaskStateService.isReturnedForUpdate(
                    systemSubmission.getStudentId(), systemSubmission.getLessonId(), systemSubmission.getQuestionId())) {
                return AjaxResult.error("作品已退回，请等待学生重新提交后再批改");
            }
            BizStudentAnswer created = new BizStudentAnswer();
            created.setStudentId(systemSubmission.getStudentId());
            created.setLessonId(systemSubmission.getLessonId());
            created.setQuestionId(systemSubmission.getQuestionId());
            created.setStudentAnswer(systemSubmission.getSourceCode());
            created.setSubmitTime(systemSubmission.getJudgedAt() == null
                    ? systemSubmission.getSubmittedAt() : systemSubmission.getJudgedAt());
            created.setAnswerTime(systemSubmission.getTimeSeconds() == null
                    ? 0 : (int) Math.ceil(systemSubmission.getTimeSeconds()));
            studentAnswerMapper.insertAnswerIfAbsent(created);
            answer = studentAnswerMapper.selectByStudentLessonQuestionForUpdate(
                    systemSubmission.getStudentId(), systemSubmission.getLessonId(), systemSubmission.getQuestionId());
        } else {
            answer = studentAnswerMapper.selectByIdForUpdate(request.getAnswerId());
        }
        if (answer == null) {
            return AjaxResult.error("答题记录不存在");
        }
        Long answerId = answer.getAnswerId();
        String scopeError = validateGradeScope(answer.getStudentId(), answer.getLessonId());
        if (scopeError != null) {
            return AjaxResult.error(scopeError);
        }
        BizLessonQuestionDetailVo answerQuestion = findPracticalQuestion(answer.getLessonId(), answer.getQuestionId());
        boolean python = answerQuestion != null && "PYTHON".equalsIgnoreCase(answerQuestion.getPracticalMode());
        if (python) {
            com.ruoyi.business.domain.ProgrammingSubmission best = programmingJudgeMapper.selectBestFinalSubmission(
                    answer.getStudentId(), answer.getLessonId(), answer.getQuestionId());
            if (request.getSystemSubmissionId() != null
                    && (best == null || !Objects.equals(best.getSubmissionId(), request.getSystemSubmissionId()))) {
                return AjaxResult.error("系统判题结果已更新，请刷新后重新批改");
            }
            // 页面打开时没有提交、现在却有了，说明学生刚交新代码，教师还没看过，不能直接给分。
            if (best != null && request.getSystemSubmissionId() == null) {
                return AjaxResult.error("学生有新的判题提交，请刷新后重新批改");
            }
            if (StringUtils.isBlank(answer.getStudentAnswer()) && best == null) {
                return AjaxResult.error("学生尚未提交操作题作品");
            }
            systemSubmission = best;
        } else if (StringUtils.isBlank(answer.getStudentAnswer())) {
            return AjaxResult.error("学生尚未提交操作题作品");
        }
        if (classroomTaskStateService.isReturnedForUpdate(answer.getStudentId(), answer.getLessonId(), answer.getQuestionId()))
            return AjaxResult.error("作品已退回，请等待学生重新提交后再批改");
        boolean answerChanged;
        if (python) {
            String effectiveAnswer = systemSubmission == null ? answer.getStudentAnswer() : systemSubmission.getSourceCode();
            answerChanged = request.getExpectedAnswer() == null
                    || (!Objects.equals(request.getExpectedAnswer(), effectiveAnswer)
                    && !Objects.equals(request.getExpectedAnswer(), answer.getStudentAnswer()));
        } else {
            answerChanged = answer.getPracticalVersionId() != null
                    ? !Objects.equals(request.getPracticalVersionId(), answer.getPracticalVersionId())
                    : answer.getStudentAnswer().startsWith("FLOWCHART:")
                    ? !Objects.equals(request.getExpectedAnswer(), answer.getStudentAnswer())
                    : request.getSubmitTime() == null || !request.getSubmitTime().equals(answer.getSubmitTime());
        }
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
                    scoringDetail.setAnswerId(answerId);
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
        practicalGradingDeadlineService.assertCanGrade(answerId);
        if (python) {
            String effectiveAnswer = systemSubmission == null ? answer.getStudentAnswer() : systemSubmission.getSourceCode();
            studentAnswerMapper.updateScoreAndStudentAnswer(answerId, finalScore, effectiveAnswer);
        } else {
            studentAnswerMapper.updateScore(answerId, finalScore);
        }

        // 切回直接打分时也要清除旧分项，避免页面再次打开显示过期明细。
        scoringDetailMapper.deleteBizScoringDetailByAnswerId(answerId);
        for (com.ruoyi.business.domain.BizScoringDetail detail : scoringDetails) {
            scoringDetailMapper.insertBizScoringDetail(detail);
        }

        BizStudent student = studentMapper.selectBizStudentByStudentId(answer.getStudentId());
        if (student == null || student.getDeptId() == null) throw new ServiceException("学生档案不存在");
        // 管理员批改时当前部门未必就是学生学校，状态归属必须使用学生档案的学校。
        classroomTaskStateService.markInTransaction(student, student.getDeptId(), answer.getLessonId(),
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

    private boolean sameAnswerSnapshot(BizStudentAnswer answer, PracticalSubmissionVo row) {
        return answer != null
                && Objects.equals(answer.getScore(), row.getScore())
                && Objects.equals(answer.getSubmitTime(), row.getSubmitTime())
                && Objects.equals(answer.getPracticalVersionId(), row.getPracticalVersionId())
                && Objects.equals(answer.getStudentAnswer(), row.getStudentAnswer());
    }

    private boolean matchesExpectedSnapshot(PythonBatchAdoptSnapshot expected, PracticalSubmissionVo row) {
        return Objects.equals(expected.getAnswerId(), row.getAnswerId())
                && Objects.equals(expected.getScore(), row.getScore())
                && Objects.equals(expected.getStudentAnswer(), row.getStudentAnswer())
                && Objects.equals(expected.getPracticalVersionId(), row.getPracticalVersionId())
                && Objects.equals(expected.getSystemSubmissionId(), row.getSystemSubmissionId())
                && Objects.equals(expected.getSystemScore(), row.getSystemScore());
    }

    private boolean sameSystemSubmission(com.ruoyi.business.domain.ProgrammingSubmission submission,
                                         Long expectedSubmissionId, Integer expectedScore) {
        return submission != null
                && Objects.equals(submission.getSubmissionId(), expectedSubmissionId)
                && Objects.equals(submission.getScore(), expectedScore);
    }

    private BizStudentAnswer buildPythonAnswer(Long lessonId, PracticalSubmissionVo row,
                                               com.ruoyi.business.domain.ProgrammingSubmission submission) {
        BizStudentAnswer answer = new BizStudentAnswer();
        answer.setStudentId(row.getStudentId());
        answer.setLessonId(lessonId);
        answer.setQuestionId(row.getQuestionId());
        answer.setStudentAnswer(submission.getSourceCode());
        answer.setSubmitTime(submission.getJudgedAt() == null
                ? submission.getSubmittedAt() : submission.getJudgedAt());
        answer.setAnswerTime(submission.getTimeSeconds() == null
                ? 0 : (int) Math.ceil(submission.getTimeSeconds()));
        return answer;
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

    public static class PythonBatchAdoptRequest {
        private Long lessonId;
        private Long questionId;
        private String classCode;
        private String entryYear;
        private List<PythonBatchAdoptSnapshot> expectedSnapshots;

        public Long getLessonId() { return lessonId; }
        public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public String getClassCode() { return classCode; }
        public void setClassCode(String classCode) { this.classCode = classCode; }
        public String getEntryYear() { return entryYear; }
        public void setEntryYear(String entryYear) { this.entryYear = entryYear; }
        public List<PythonBatchAdoptSnapshot> getExpectedSnapshots() { return expectedSnapshots; }
        public void setExpectedSnapshots(List<PythonBatchAdoptSnapshot> expectedSnapshots) {
            this.expectedSnapshots = expectedSnapshots;
        }
    }

    public static class PythonBatchAdoptSnapshot {
        private Long studentId;
        private Long answerId;
        private Integer score;
        private String studentAnswer;
        private Long practicalVersionId;
        private Long systemSubmissionId;
        private Integer systemScore;

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public Long getAnswerId() { return answerId; }
        public void setAnswerId(Long answerId) { this.answerId = answerId; }
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
        public String getStudentAnswer() { return studentAnswer; }
        public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }
        public Long getPracticalVersionId() { return practicalVersionId; }
        public void setPracticalVersionId(Long practicalVersionId) { this.practicalVersionId = practicalVersionId; }
        public Long getSystemSubmissionId() { return systemSubmissionId; }
        public void setSystemSubmissionId(Long systemSubmissionId) { this.systemSubmissionId = systemSubmissionId; }
        public Integer getSystemScore() { return systemScore; }
        public void setSystemScore(Integer systemScore) { this.systemScore = systemScore; }
    }

    public static class GradeRequest {
        private Long answerId;
        private Integer score;
        private Integer expectedScore;
        private String expectedAnswer;
        public String getExpectedAnswer() { return expectedAnswer; }
        public void setExpectedAnswer(String value) { expectedAnswer = value; }
        private Long systemSubmissionId;
        public Long getSystemSubmissionId() { return systemSubmissionId; }
        public void setSystemSubmissionId(Long value) { systemSubmissionId = value; }
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
