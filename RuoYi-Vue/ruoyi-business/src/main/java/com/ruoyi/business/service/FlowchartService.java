package com.ruoyi.business.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.FlowchartDraft;
import com.ruoyi.business.domain.FlowchartLessonSnapshot;
import com.ruoyi.business.domain.FlowchartQuestionConfig;
import com.ruoyi.business.domain.FlowchartSubmission;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.FlowchartMapper;
import com.ruoyi.business.mapper.BizScoringDetailMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/** 画程题目配置、学生草稿、提交版本和批改读取的业务服务。 */
@Service
public class FlowchartService {
    private final FlowchartMapper flowchartMapper;
    private final BizQuestionMapper questionMapper;
    private final BizStudentMapper studentMapper;
    private final BizLessonAssignmentMapper assignmentMapper;
    private final BizLessonQuestionMapper lessonQuestionMapper;
    private final BizStudentAnswerMapper answerMapper;
    private final GuideSheetAccessService guideSheetAccessService;
    private final FlowchartDocumentService documentService;
    private final FlowchartStructureCheckService checkService;
    private final ClassroomTaskStateService taskStateService;
    @Autowired private BizScoringDetailMapper scoringDetailMapper;
    @Autowired private ObjectMapper objectMapper;

    public FlowchartService(FlowchartMapper flowchartMapper,
                            BizQuestionMapper questionMapper,
                            BizStudentMapper studentMapper,
                            BizLessonAssignmentMapper assignmentMapper,
                            BizLessonQuestionMapper lessonQuestionMapper,
                            BizStudentAnswerMapper answerMapper,
                            GuideSheetAccessService guideSheetAccessService,
                            FlowchartDocumentService documentService,
                            FlowchartStructureCheckService checkService,
                            ClassroomTaskStateService taskStateService) {
        this.flowchartMapper = flowchartMapper;
        this.questionMapper = questionMapper;
        this.studentMapper = studentMapper;
        this.assignmentMapper = assignmentMapper;
        this.lessonQuestionMapper = lessonQuestionMapper;
        this.answerMapper = answerMapper;
        this.guideSheetAccessService = guideSheetAccessService;
        this.documentService = documentService;
        this.checkService = checkService;
        this.taskStateService = taskStateService;
    }

    public FlowchartQuestionConfig teacherConfig(Long questionId, Long userId, boolean admin) {
        assertQuestionOwner(questionId, userId, admin);
        FlowchartQuestionConfig config = flowchartMapper.selectQuestionConfig(questionId);
        return config == null ? defaultConfig(questionId) : config;
    }

    /**
     * 选题预览只提供学生会拿到的基础图，避免公开题的标准答案和结构规则被读取。
     */
    public Map<String, Object> teacherPreview(Long questionId, Long userId, boolean admin) {
        BizQuestion question = requireFlowchartQuestion(questionId);
        assertQuestionPreviewAccess(question, userId, admin);
        FlowchartQuestionConfig config = flowchartMapper.selectQuestionConfig(questionId);
        Map<String, Object> preview = new HashMap<>();
        preview.put("questionId", questionId);
        preview.put("schemaVersion", config == null ? FlowchartDocumentService.SCHEMA_VERSION : config.getSchemaVersion());
        preview.put("starterJson", config == null ? FlowchartDocumentService.EMPTY_DOCUMENT : config.getStarterJson());
        // 选题页面只需要知道题目能否进入课堂，不能因此返回标准答案或规则内容。
        preview.put("configReady", isLessonConfigReady(config));
        return preview;
    }

    @Transactional(rollbackFor = Exception.class)
    public FlowchartQuestionConfig saveTeacherConfig(Long questionId, FlowchartQuestionConfig request,
                                                     Long userId, boolean admin, String username) {
        assertQuestionOwner(questionId, userId, admin);
        BizQuestion question = questionMapper.selectBizQuestionByQuestionId(questionId);
        if (!isFlowchartQuestion(question)) throw new ServiceException("当前题目不是画程流程图操作题");
        if (request == null) throw new ServiceException("画程题目配置不能为空");

        FlowchartQuestionConfig normalized = new FlowchartQuestionConfig();
        normalized.setQuestionId(questionId);
        normalized.setSchemaVersion(FlowchartDocumentService.SCHEMA_VERSION);
        normalized.setStarterJson(documentService.normalizeDocument(request.getStarterJson(), "学生基础图"));
        normalized.setAnswerJson(documentService.normalizeDocument(request.getAnswerJson(), "标准答案"));
        normalized.setPermissionsJson(documentService.normalizePermissions(request.getPermissionsJson()));
        normalized.setRulesJson(documentService.readDocument(normalized.getAnswerJson()).path("nodes").isEmpty()
                ? "[]" : request.getRulesJson() == null || request.getRulesJson().trim().isEmpty()
                ? checkService.generateRules(normalized.getAnswerJson()) : request.getRulesJson());
        // 通过一次零分值结构检查完成规则 JSON 格式和引用校验，避免非法规则进入题库。
        checkService.check(normalized.getAnswerJson(), normalized.getAnswerJson(), normalized.getRulesJson(), 100);
        normalized.setCreateBy(username);
        normalized.setUpdateBy(username);

        FlowchartQuestionConfig existing = flowchartMapper.selectQuestionConfig(questionId);
        if (existing == null) {
            flowchartMapper.insertQuestionConfig(normalized);
        } else {
            Integer expected = request.getConfigRevision();
            if (expected == null || flowchartMapper.updateQuestionConfig(normalized, expected) != 1) {
                throw new ServiceException("画程题目配置已在其他页面更新，请刷新后重试");
            }
        }
        return flowchartMapper.selectQuestionConfig(questionId);
    }

    public String generateRules(String answerJson) {
        return checkService.generateRules(documentService.normalizeDocument(answerJson));
    }

    /**
     * 课程保存时冻结画程口径。已有快照保持不变，避免题库后续修改污染已经布置的课程。
     */
    @Transactional(rollbackFor = Exception.class)
    public void snapshotLesson(Long lessonId, List<BizLessonQuestionDetailVo> questions) {
        if (lessonId == null || questions == null) return;
        for (BizLessonQuestionDetailVo question : questions) {
            if (question != null && "practical".equalsIgnoreCase(question.getQuestionType())
                    && "FLOWCHART".equalsIgnoreCase(question.getPracticalMode())) {
                resolveSnapshot(lessonId, question.getQuestionId());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> studentWorkspace(Long userId, Long deptId, Long lessonId, Long questionId) {
        BizStudent student = assertStudentAccess(userId, deptId, lessonId, questionId, true);
        BizStudentAnswer answer = lockAnswer(student, lessonId, questionId);
        boolean returned = taskStateService.isReturnedForUpdate(student.getStudentId(), lessonId, questionId);
        boolean editable = PracticalSubmissionPolicy.canUpdate(answer, returned);
        FlowchartLessonSnapshot snapshot = resolveSnapshot(lessonId, questionId);
        FlowchartDraft draft = flowchartMapper.selectDraft(student.getStudentId(), lessonId, questionId);
        if (draft == null) {
            draft = new FlowchartDraft();
            draft.setStudentId(student.getStudentId());
            draft.setLessonId(lessonId);
            draft.setQuestionId(questionId);
            draft.setSchemaVersion(snapshot.getSchemaVersion());
            draft.setDocumentJson(snapshot.getStarterJson());
            draft.setBaseSubmissionVersion(null);
            // 两个页面同时首次打开时由数据库唯一键收敛到同一份草稿，其他数据库异常仍应正常抛出。
            flowchartMapper.insertDraft(draft);
            draft = flowchartMapper.selectDraft(student.getStudentId(), lessonId, questionId);
        }
        FlowchartSubmission latest = flowchartMapper.selectLatestSubmission(
                student.getStudentId(), lessonId, questionId);
        if (editable && latest != null && (draft.getBaseSubmissionVersion() == null
                || draft.getBaseSubmissionVersion() < latest.getVersionNo())) {
            // 只推进草稿的基础版本，不覆盖尚未提交的编辑内容。
            flowchartMapper.alignDraftBase(student.getStudentId(), lessonId, questionId, latest.getVersionNo());
            draft.setBaseSubmissionVersion(latest.getVersionNo());
        }
        Map<String, Object> result = new HashMap<String, Object>();
        // 学生端只下发脱敏快照（基础图+权限），标准答案与评分规则不出内网。
        result.put("snapshot", snapshot == null ? null : snapshot.toStudentView());
        result.put("draft", draft);
        result.put("latestSubmission", latest == null ? null : latest.toStudentView());
        result.put("returned", returned);
        result.put("score", answer == null ? null : answer.getScore());
        result.put("readOnly", !editable);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public FlowchartDraft saveDraft(Long userId, Long deptId, Long lessonId, Long questionId,
                                    Integer expectedRevision, String documentJson) {
        BizStudent student = assertStudentAccess(userId, deptId, lessonId, questionId, true);
        assertCanUpdate(student, lessonId, questionId);
        if (expectedRevision == null) throw new ServiceException("草稿修订号不能为空");
        FlowchartSubmission latest = flowchartMapper.selectLatestSubmission(student.getStudentId(), lessonId, questionId);
        FlowchartDraft current = flowchartMapper.selectDraft(student.getStudentId(), lessonId, questionId);
        if (current == null) throw new ServiceException("草稿不存在，请重新打开画程");
        if (latest != null && (current.getBaseSubmissionVersion() == null
                || current.getBaseSubmissionVersion() < latest.getVersionNo())) {
            throw new ServiceException("作品已经提交，如需补交请先由最新版本重新开始");
        }
        FlowchartDraft update = new FlowchartDraft();
        update.setStudentId(student.getStudentId());
        update.setLessonId(lessonId);
        update.setQuestionId(questionId);
        update.setSchemaVersion(FlowchartDocumentService.SCHEMA_VERSION);
        update.setDocumentJson(documentService.normalizeDocument(documentJson));
        if (flowchartMapper.updateDraft(update, expectedRevision) != 1) {
            throw new ServiceException("草稿已在其他页面更新，当前内容已保留在本机，请刷新后选择版本");
        }
        return flowchartMapper.selectDraft(student.getStudentId(), lessonId, questionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public FlowchartSubmission submit(Long userId, Long deptId, Long lessonId, Long questionId,
                                      Integer expectedRevision) {
        BizStudent student = assertStudentAccess(userId, deptId, lessonId, questionId, true);
        BizStudentAnswer previousAnswer = assertCanUpdate(student, lessonId, questionId);
        FlowchartDraft draft = flowchartMapper.selectDraftForUpdate(student.getStudentId(), lessonId, questionId);
        if (draft == null) throw new ServiceException("草稿不存在，请先打开画程");
        if (expectedRevision == null || !expectedRevision.equals(draft.getRevision())) {
            throw new ServiceException("草稿尚未完成同步，请等待显示“已保存”后再提交");
        }
        FlowchartSubmission duplicate = flowchartMapper.selectSubmissionByDraftRevision(
                student.getStudentId(), lessonId, questionId, draft.getRevision());
        if (duplicate != null) {
            if (!taskStateService.isReturnedForUpdate(student.getStudentId(), lessonId, questionId)) return duplicate.toStudentView();
            // 退回后允许原样重交，但必须生成新版本，不能误认作上一次提交的重试。
            if (flowchartMapper.updateDraft(draft, draft.getRevision()) != 1) throw new ServiceException("草稿已变化，请刷新后重交");
            draft = flowchartMapper.selectDraftForUpdate(student.getStudentId(), lessonId, questionId);
        }
        FlowchartSubmission previous = flowchartMapper.selectLatestSubmission(student.getStudentId(), lessonId, questionId);
        if (previous != null && previousAnswer != null && previousAnswer.getScore() != null) {
            try {
                if (flowchartMapper.archiveSubmissionGrade(previous.getSubmissionId(), previousAnswer.getScore(),
                        objectMapper.writeValueAsString(scoringDetailMapper.selectDetailsByAnswerId(previousAnswer.getAnswerId()))) != 1)
                    throw new ServiceException("原作品版本不存在");
            } catch (Exception e) { throw new ServiceException("原成绩归档失败，未替换作品"); }
        }

        FlowchartLessonSnapshot snapshot = resolveSnapshot(lessonId, questionId);
        int maxScore = lessonQuestionScore(lessonId, questionId);
        FlowchartStructureCheckService.CheckOutcome outcome = checkService.check(
                snapshot.getAnswerJson(), draft.getDocumentJson(), snapshot.getRulesJson(), maxScore);
        FlowchartSubmission submission = new FlowchartSubmission();
        submission.setStudentId(student.getStudentId());
        submission.setLessonId(lessonId);
        submission.setQuestionId(questionId);
        submission.setVersionNo(flowchartMapper.selectNextSubmissionVersion(
                student.getStudentId(), lessonId, questionId));
        submission.setDraftRevision(draft.getRevision());
        submission.setSchemaVersion(draft.getSchemaVersion());
        submission.setDocumentJson(draft.getDocumentJson());
        submission.setRulesSnapshotJson(snapshot.getRulesJson());
        submission.setCheckResultJson(outcome.getResultJson());
        submission.setSuggestedScore(outcome.getSuggestedScore());
        flowchartMapper.insertSubmission(submission);

        BizStudentAnswer answer = new BizStudentAnswer();
        answer.setStudentId(student.getStudentId());
        answer.setLessonId(lessonId);
        answer.setQuestionId(questionId);
        answer.setStudentAnswer("FLOWCHART:" + submission.getSubmissionId());
        answer.setIsCorrect(false);
        answer.setScore(null);
        answer.setAnswerTime(0);
        answer.setSubmitTime(new Date());
        answer.setPreviewStatus(null);
        answer.setPreviewPath(null);
        answerMapper.upsertAnswer(answer);
        flowchartMapper.updateSubmissionAnswerId(submission.getSubmissionId(), answer.getAnswerId());
        submission.setAnswerId(answer.getAnswerId());
        if (previousAnswer != null) scoringDetailMapper.deleteBizScoringDetailByAnswerId(previousAnswer.getAnswerId());
        flowchartMapper.alignDraftBase(student.getStudentId(), lessonId, questionId, submission.getVersionNo());
        taskStateService.markInTransaction(student, student.getDeptId(), lessonId, questionId, ClassroomTaskStateService.SUBMITTED);
        return submission.toStudentView();
    }

    @Transactional(rollbackFor = Exception.class)
    public FlowchartDraft reopen(Long userId, Long deptId, Long lessonId, Long questionId) {
        BizStudent student = assertStudentAccess(userId, deptId, lessonId, questionId, true);
        assertCanUpdate(student, lessonId, questionId);
        FlowchartSubmission latest = flowchartMapper.selectLatestSubmission(student.getStudentId(), lessonId, questionId);
        if (latest == null) throw new ServiceException("尚无正式提交，无需补交重开");
        FlowchartDraft existing = flowchartMapper.selectDraftForUpdate(student.getStudentId(), lessonId, questionId);
        if (existing != null && latest.getVersionNo().equals(existing.getBaseSubmissionVersion())) return existing;
        FlowchartDraft draft = new FlowchartDraft();
        draft.setStudentId(student.getStudentId());
        draft.setLessonId(lessonId);
        draft.setQuestionId(questionId);
        draft.setSchemaVersion(latest.getSchemaVersion());
        draft.setDocumentJson(latest.getDocumentJson());
        draft.setBaseSubmissionVersion(latest.getVersionNo());
        flowchartMapper.replaceDraftFromSubmission(draft);
        return flowchartMapper.selectDraft(student.getStudentId(), lessonId, questionId);
    }

    public Map<String, Object> gradingSubmission(Long lessonId, Long questionId, Long studentId,
                                                 Integer versionNo) {
        BizStudent student = studentMapper.selectBizStudentByStudentId(studentId);
        if (student == null) throw new ServiceException("学生不存在");
        guideSheetAccessService.requireViewableLessonClassDept(lessonId, student.getEntryYear(), student.getClassCode());
        FlowchartSubmission submission = flowchartMapper.selectSubmission(
                studentId, lessonId, questionId, versionNo);
        if (submission == null) throw new ServiceException("未找到学生的画程提交");
        FlowchartLessonSnapshot snapshot = flowchartMapper.selectLessonSnapshot(lessonId, questionId);
        if (snapshot == null) throw new ServiceException("画程课程快照不存在");
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("submission", submission);
        result.put("answerJson", snapshot.getAnswerJson());
        result.put("studentName", student.getStudentName());
        return result;
    }

    private FlowchartQuestionConfig defaultConfig(Long questionId) {
        FlowchartQuestionConfig config = new FlowchartQuestionConfig();
        config.setQuestionId(questionId);
        config.setSchemaVersion(FlowchartDocumentService.SCHEMA_VERSION);
        config.setStarterJson(FlowchartDocumentService.EMPTY_DOCUMENT);
        config.setAnswerJson(FlowchartDocumentService.EMPTY_DOCUMENT);
        config.setPermissionsJson(FlowchartDocumentService.DEFAULT_PERMISSIONS);
        config.setRulesJson("[]");
        config.setConfigRevision(0);
        return config;
    }

    private BizStudentAnswer lockAnswer(BizStudent student, Long lessonId, Long questionId) {
        BizStudentAnswer answer = answerMapper.selectLatestByStudentLessonQuestion(student.getStudentId(), lessonId, questionId);
        return answer == null ? null : answerMapper.selectByIdForUpdate(answer.getAnswerId());
    }

    private BizStudentAnswer assertCanUpdate(BizStudent student, Long lessonId, Long questionId) {
        BizStudentAnswer answer = lockAnswer(student, lessonId, questionId);
        PracticalSubmissionPolicy.assertCanUpdate(answer,
                taskStateService.isReturnedForUpdate(student.getStudentId(), lessonId, questionId));
        return answer;
    }

    private void assertQuestionOwner(Long questionId, Long userId, boolean admin) {
        BizQuestion question = requireFlowchartQuestion(questionId);
        if (!admin && (question.getCreatorId() == null || !question.getCreatorId().equals(userId))) {
            throw new ServiceException("无权查看或修改他人创建的画程题目");
        }
    }

    private BizQuestion requireFlowchartQuestion(Long questionId) {
        BizQuestion question = questionMapper.selectBizQuestionByQuestionId(questionId);
        if (question == null) throw new ServiceException("题目不存在");
        if (!isFlowchartQuestion(question)) throw new ServiceException("当前题目不是画程流程图操作题");
        return question;
    }

    private void assertQuestionPreviewAccess(BizQuestion question, Long userId, boolean admin) {
        if (admin || (userId != null && userId.equals(question.getCreatorId()))
                || "Y".equalsIgnoreCase(question.getIsPublic()) || "1".equals(question.getIsPublic())) {
            return;
        }
        throw new ServiceException("无权预览他人创建的私有画程题目");
    }

    private boolean isFlowchartQuestion(BizQuestion question) {
        return question != null && "practical".equals(question.getQuestionType())
                && "FLOWCHART".equalsIgnoreCase(question.getPracticalMode());
    }

    private BizStudent assertStudentAccess(Long userId, Long deptId, Long lessonId,
                                           Long questionId, boolean requireOpen) {
        BizStudent student = studentMapper.selectBizStudentByUserId(userId);
        if (student == null) throw new ServiceException("未找到当前学生信息");
        Long studentDeptId = student.getDeptId() == null ? deptId : student.getDeptId();
        // 工作区首次进入也可能创建草稿，四个学生入口统一锁住当前指派直到事务结束。
        BizLessonAssignment locked = assignmentMapper.selectCurrentAssignmentForUpdate(
                student.getEntryYear(), student.getClassCode(), studentDeptId);
        Long currentLesson = locked == null ? null : locked.getLessonId();
        if (currentLesson == null || !currentLesson.equals(lessonId)) {
            throw new ServiceException("只能进入本人班级当前课程的画程题目");
        }
        boolean returned = taskStateService.isReturned(student.getStudentId(), lessonId, questionId);
        List<Long> questionIds = lessonQuestionMapper.selectQuestionIdsByLessonId(lessonId);
        if (questionIds == null || !questionIds.contains(questionId)) throw new ServiceException("题目不属于当前课程");
        if (!isFlowchartQuestion(questionMapper.selectBizQuestionByQuestionId(questionId))) {
            throw new ServiceException("当前题目不是画程流程图操作题");
        }
        if (requireOpen && !returned) {
            BizLessonAssignment filter = new BizLessonAssignment();
            filter.setLessonId(lessonId);
            filter.setEntryYear(student.getEntryYear());
            filter.setClassCode(student.getClassCode());
            filter.setDeptId(studentDeptId);
            boolean open = false;
            for (BizLessonAssignment assignment : assignmentMapper.selectBizLessonAssignmentList(filter)) {
                if (lessonId.equals(assignment.getLessonId()) && Integer.valueOf(1).equals(assignment.getPracticalOpen())) {
                    open = true;
                    break;
                }
            }
            if (!open) throw new ServiceException("本课操作题尚未开放，请等待老师开启");
        }
        return student;
    }

    private FlowchartLessonSnapshot resolveSnapshot(Long lessonId, Long questionId) {
        FlowchartLessonSnapshot snapshot = flowchartMapper.selectLessonSnapshot(lessonId, questionId);
        if (snapshot != null) return snapshot;
        FlowchartQuestionConfig config = flowchartMapper.selectQuestionConfig(questionId);
        if (!isLessonConfigReady(config)) throw new ServiceException("画程题目尚未完成基础图和标准答案配置，请先由出题教师完成配置后再保存课程");
        snapshot = new FlowchartLessonSnapshot();
        snapshot.setLessonId(lessonId);
        snapshot.setQuestionId(questionId);
        snapshot.setSourceRevision(config.getConfigRevision());
        snapshot.setSchemaVersion(config.getSchemaVersion());
        snapshot.setStarterJson(config.getStarterJson());
        snapshot.setAnswerJson(config.getAnswerJson());
        snapshot.setPermissionsJson(config.getPermissionsJson());
        snapshot.setRulesJson(config.getRulesJson());
        flowchartMapper.insertLessonSnapshot(snapshot);
        return flowchartMapper.selectLessonSnapshot(lessonId, questionId);
    }

    /**
     * 空图是合法的从零作图任务；仍要求配置存在且两张图可以解析。
     */
    private boolean isLessonConfigReady(FlowchartQuestionConfig config) {
        if (config == null) return false;
        try {
            documentService.normalizeDocument(config.getStarterJson(), "学生基础图");
            documentService.normalizeDocument(config.getAnswerJson(), "标准答案");
            return true;
        } catch (ServiceException ignored) {
            // 早期或异常配置不能让题库列表白屏；一律要求出题教师重新打开并保存规范配置。
            return false;
        }
    }

    private int lessonQuestionScore(Long lessonId, Long questionId) {
        for (BizLessonQuestionDetailVo item : lessonQuestionMapper.selectDetailsByLessonId(lessonId)) {
            if (questionId.equals(item.getQuestionId()) && item.getQuestionScore() != null) {
                long value = item.getQuestionScore();
                if (value < 0 || value > Integer.MAX_VALUE) break;
                return (int) value;
            }
        }
        throw new ServiceException("当前课程的画程题目分值无效");
    }
}
