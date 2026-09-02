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

    public FlowchartService(FlowchartMapper flowchartMapper,
                            BizQuestionMapper questionMapper,
                            BizStudentMapper studentMapper,
                            BizLessonAssignmentMapper assignmentMapper,
                            BizLessonQuestionMapper lessonQuestionMapper,
                            BizStudentAnswerMapper answerMapper,
                            GuideSheetAccessService guideSheetAccessService,
                            FlowchartDocumentService documentService,
                            FlowchartStructureCheckService checkService) {
        this.flowchartMapper = flowchartMapper;
        this.questionMapper = questionMapper;
        this.studentMapper = studentMapper;
        this.assignmentMapper = assignmentMapper;
        this.lessonQuestionMapper = lessonQuestionMapper;
        this.answerMapper = answerMapper;
        this.guideSheetAccessService = guideSheetAccessService;
        this.documentService = documentService;
        this.checkService = checkService;
    }

    public FlowchartQuestionConfig teacherConfig(Long questionId, Long userId, boolean admin) {
        assertQuestionOwner(questionId, userId, admin);
        FlowchartQuestionConfig config = flowchartMapper.selectQuestionConfig(questionId);
        return config == null ? defaultConfig(questionId) : config;
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
        normalized.setStarterJson(documentService.normalizeDocument(request.getStarterJson()));
        normalized.setAnswerJson(documentService.normalizeDocument(request.getAnswerJson()));
        if (documentService.readDocument(normalized.getStarterJson()).path("nodes").isEmpty()) {
            throw new ServiceException("请先制作发给学生的基础流程图");
        }
        if (documentService.readDocument(normalized.getAnswerJson()).path("nodes").isEmpty()) {
            throw new ServiceException("请先制作画程标准答案");
        }
        normalized.setPermissionsJson(documentService.normalizePermissions(request.getPermissionsJson()));
        normalized.setRulesJson(request.getRulesJson() == null || request.getRulesJson().trim().isEmpty()
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
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("snapshot", snapshot);
        result.put("draft", draft);
        result.put("latestSubmission", latest);
        result.put("readOnly", latest != null && (draft.getBaseSubmissionVersion() == null
                || draft.getBaseSubmissionVersion() < latest.getVersionNo()));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public FlowchartDraft saveDraft(Long userId, Long deptId, Long lessonId, Long questionId,
                                    Integer expectedRevision, String documentJson) {
        BizStudent student = assertStudentAccess(userId, deptId, lessonId, questionId, true);
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
        FlowchartDraft draft = flowchartMapper.selectDraftForUpdate(student.getStudentId(), lessonId, questionId);
        if (draft == null) throw new ServiceException("草稿不存在，请先打开画程");
        if (expectedRevision == null || !expectedRevision.equals(draft.getRevision())) {
            throw new ServiceException("草稿尚未完成同步，请等待显示“已保存”后再提交");
        }
        FlowchartSubmission duplicate = flowchartMapper.selectSubmissionByDraftRevision(
                student.getStudentId(), lessonId, questionId, draft.getRevision());
        if (duplicate != null) return duplicate;

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
        return submission;
    }

    @Transactional(rollbackFor = Exception.class)
    public FlowchartDraft reopen(Long userId, Long deptId, Long lessonId, Long questionId) {
        BizStudent student = assertStudentAccess(userId, deptId, lessonId, questionId, true);
        FlowchartSubmission latest = flowchartMapper.selectLatestSubmission(student.getStudentId(), lessonId, questionId);
        if (latest == null) throw new ServiceException("尚无正式提交，无需补交重开");
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
        guideSheetAccessService.assertCanViewLessonClass(lessonId, student.getEntryYear(), student.getClassCode());
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

    private void assertQuestionOwner(Long questionId, Long userId, boolean admin) {
        BizQuestion question = questionMapper.selectBizQuestionByQuestionId(questionId);
        if (question == null) throw new ServiceException("题目不存在");
        if (!admin && (question.getCreatorId() == null || !question.getCreatorId().equals(userId))) {
            throw new ServiceException("无权查看或修改他人创建的画程题目");
        }
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
        Long currentLesson = assignmentMapper.selectCurrentLessonByClass(
                student.getEntryYear(), student.getClassCode(), studentDeptId);
        if (currentLesson == null || !currentLesson.equals(lessonId)) {
            throw new ServiceException("只能进入本人班级当前课程的画程题目");
        }
        List<Long> questionIds = lessonQuestionMapper.selectQuestionIdsByLessonId(lessonId);
        if (questionIds == null || !questionIds.contains(questionId)) throw new ServiceException("题目不属于当前课程");
        if (!isFlowchartQuestion(questionMapper.selectBizQuestionByQuestionId(questionId))) {
            throw new ServiceException("当前题目不是画程流程图操作题");
        }
        if (requireOpen) {
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
        if (config == null) throw new ServiceException("画程题目尚未完成基础图和标准答案配置");
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
