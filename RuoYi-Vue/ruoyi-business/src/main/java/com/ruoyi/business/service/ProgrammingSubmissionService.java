package com.ruoyi.business.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.ProgrammingDraft;
import com.ruoyi.business.domain.ProgrammingQuestionConfig;
import com.ruoyi.business.domain.ProgrammingSubmission;
import com.ruoyi.business.domain.ProgrammingSubmissionCase;
import com.ruoyi.business.domain.ProgrammingTestCase;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.StudentProgrammingPublicCaseVo;
import com.ruoyi.business.domain.vo.StudentProgrammingSubmissionCaseVo;
import com.ruoyi.business.domain.vo.StudentProgrammingSubmissionVo;
import com.ruoyi.business.judge.Judge0Client;
import com.ruoyi.business.judge.Judge0Properties;
import com.ruoyi.business.judge.Judge0Request;
import com.ruoyi.business.judge.Judge0Result;
import com.ruoyi.business.judge.Judge0StatusMapper;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.ProgrammingJudgeMapper;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.data.redis.core.ValueOperations;

@Service
public class ProgrammingSubmissionService {
    private static final String STATUS_WAITING = "WAITING";
    private static final String STATUS_JUDGING = "JUDGING";
    private static final String STATUS_SERVICE_ERROR = "SERVICE_ERROR";
    private static final String STATUS_CANCELLED = "CANCELLED";
    @Autowired private ProgrammingJudgeMapper programmingMapper;
    @Autowired private BizQuestionMapper questionMapper;
    @Autowired private BizLessonQuestionMapper lessonQuestionMapper;
    @Autowired private BizLessonAssignmentMapper lessonAssignmentMapper;
    @Autowired private BizLessonMapper lessonMapper;
    @Autowired private BizStudentAnswerMapper studentAnswerMapper;
    @Autowired private BizStudentMapper studentMapper;
    @Autowired private RedisCache redisCache;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private Judge0Properties properties;
    @Autowired @Qualifier("judge0HttpClient") private Judge0Client httpClient;
    @Autowired @Qualifier("judge0MockClient") private Judge0Client mockClient;

    public ProgrammingQuestionConfig getTeacherConfig(Long questionId, Long currentUserId, boolean admin) {
        assertQuestionOwner(requirePythonQuestion(questionId), currentUserId, admin);
        return programmingMapper.selectConfig(questionId);
    }
    public List<ProgrammingTestCase> getTeacherTestCases(Long questionId, Long currentUserId, boolean admin) {
        assertQuestionOwner(requirePythonQuestion(questionId), currentUserId, admin);
        return programmingMapper.selectTestCases(questionId);
    }

    /**
     * 题库选择器允许教师预览公开题，但不能因此泄露隐藏测试点。
     * 完整配置编辑仍严格限定为创建者或管理员。
     */
    public ProgrammingQuestionConfig getTeacherPreviewConfig(Long questionId, Long currentUserId, boolean admin) {
        assertQuestionPreviewAccess(requirePythonQuestion(questionId), currentUserId, admin);
        return programmingMapper.selectConfig(questionId);
    }

    public List<ProgrammingTestCase> getTeacherPreviewCases(Long questionId, Long currentUserId, boolean admin) {
        assertQuestionPreviewAccess(requirePythonQuestion(questionId), currentUserId, admin);
        return programmingMapper.selectPublicTestCases(questionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveTeacherConfig(Long questionId, ProgrammingQuestionConfig config, Long currentUserId, boolean admin, String username) {
        BizQuestion question = requirePythonQuestion(questionId);
        assertQuestionOwner(question, currentUserId, admin);
        if (config == null) throw new ServiceException("编程题配置不能为空");
        config.setQuestionId(questionId); normalizeConfig(config); validateTestCases(config.getTestCases());
        config.setCreateBy(username); config.setUpdateBy(username); programmingMapper.upsertConfig(config);
        programmingMapper.deleteTestCases(questionId);
        int order = 1;
        for (ProgrammingTestCase testCase : config.getTestCases()) { testCase.setQuestionId(questionId); testCase.setOrderNum(order++); testCase.setCreateBy(username); testCase.setUpdateBy(username); programmingMapper.insertTestCase(testCase); }
    }

    public ProgrammingDraft getDraft(BizStudent student, Long deptId, Long lessonId, Long questionId) {
        assertStudentQuestionAccess(student, deptId, lessonId, questionId);
        return programmingMapper.selectDraft(student.getStudentId(), lessonId, questionId);
    }

    /** 返回给学生的配置只包含题目说明和资源限制，测试点单独按公开范围脱敏。 */
    public ProgrammingQuestionConfig getStudentConfig(BizStudent student, Long deptId, Long lessonId, Long questionId) {
        assertStudentQuestionAccess(student, deptId, lessonId, questionId);
        ProgrammingQuestionConfig stored = programmingMapper.selectConfig(questionId);
        if (stored == null || !"1".equals(stored.getEnabled())) throw new ServiceException("该编程题暂未开放");
        ProgrammingQuestionConfig safe = new ProgrammingQuestionConfig(); safe.setQuestionId(stored.getQuestionId()); safe.setLanguageCode("python"); safe.setStarterCode(stored.getStarterCode()); safe.setInputDescription(stored.getInputDescription()); safe.setOutputDescription(stored.getOutputDescription()); safe.setSampleExplanation(stored.getSampleExplanation()); safe.setConstraintsText(stored.getConstraintsText()); safe.setNotesText(stored.getNotesText()); safe.setTimeLimitSeconds(stored.getTimeLimitSeconds()); safe.setMemoryLimitKb(stored.getMemoryLimitKb()); safe.setMaxOutputKb(stored.getMaxOutputKb());
        return safe;
    }

    public List<StudentProgrammingPublicCaseVo> getStudentPublicCases(BizStudent student, Long deptId, Long lessonId, Long questionId) {
        assertStudentQuestionAccess(student, deptId, lessonId, questionId);
        List<StudentProgrammingPublicCaseVo> result = new ArrayList<StudentProgrammingPublicCaseVo>();
        for (ProgrammingTestCase testCase : programmingMapper.selectPublicTestCases(questionId)) {
            StudentProgrammingPublicCaseVo item = new StudentProgrammingPublicCaseVo();
            item.setTestCaseId(testCase.getTestCaseId()); item.setCaseName(testCase.getCaseName()); item.setInputText(testCase.getInputText()); item.setExpectedOutput(testCase.getExpectedOutput()); result.add(item);
        }
        return result;
    }

    public void saveDraft(BizStudent student, Long deptId, Long lessonId, Long questionId, String sourceCode) {
        assertStudentQuestionAccess(student, deptId, lessonId, questionId); validateSource(sourceCode);
        ProgrammingDraft draft = new ProgrammingDraft(); draft.setStudentId(student.getStudentId()); draft.setLessonId(lessonId); draft.setQuestionId(questionId); draft.setSourceCode(sourceCode); programmingMapper.upsertDraft(draft);
    }

    public ProgrammingSubmission submit(BizStudent student, Long deptId, Long lessonId, Long questionId, String sourceCode, String submissionKey, String kind, String requestIp, ProgrammingSubmissionWorker worker) {
        assertStudentQuestionAccess(student, deptId, lessonId, questionId); validateSource(sourceCode);
        if (!"RUN".equals(kind) && !"SUBMIT".equals(kind)) throw new ServiceException("不支持的编程操作");
        if (submissionKey == null || !submissionKey.matches("[A-Za-z0-9_-]{8,64}")) throw new ServiceException("提交幂等键格式错误");
        ProgrammingSubmission existing = programmingMapper.selectSubmissionByKey(student.getStudentId(), lessonId, questionId, submissionKey);
        if (existing != null) return existing;
        try {
            reserveRateAndSlot(student, deptId, lessonId, questionId);
        } catch (ServiceException e) {
            // 同一幂等键并发重试时，返回已落库的原提交而不是误报学生锁占用。
            existing = programmingMapper.selectSubmissionByKey(student.getStudentId(), lessonId, questionId, submissionKey);
            if (existing != null) return existing;
            throw e;
        }
        try {
            existing = programmingMapper.selectSubmissionByKey(student.getStudentId(), lessonId, questionId, submissionKey);
            if (existing != null) { releaseReservation(student, lessonId, questionId); return existing; }
            ProgrammingSubmission submission = new ProgrammingSubmission(); submission.setSubmissionKey(submissionKey); submission.setStudentId(student.getStudentId()); submission.setLessonId(lessonId); submission.setQuestionId(questionId); submission.setSourceCode(sourceCode); submission.setSubmissionKind(kind); submission.setStatusCode(STATUS_WAITING); submission.setStatusMessage("等待判题"); submission.setRequestIp(requestIp); submission.setSubmittedAt(new Date());
            programmingMapper.insertSubmission(submission);
            redisCache.setCacheObject(slotKey(submission.getSubmissionId()), classSlotKey(student, lessonId, questionId), 180, TimeUnit.SECONDS);
            try {
                worker.judge(submission.getSubmissionId());
            } catch (RuntimeException e) {
                completeServiceFailure(submission, "判题任务无法进入队列：" + safeMessage(e));
                releaseSlot(submission.getSubmissionId(), student, lessonId, questionId);
            }
            return submission;
        } catch (DuplicateKeyException e) {
            releaseReservation(student, lessonId, questionId);
            existing = programmingMapper.selectSubmissionByKey(student.getStudentId(), lessonId, questionId, submissionKey);
            if (existing != null) return existing;
            throw e;
        } catch (RuntimeException e) { releaseReservation(student, lessonId, questionId); throw e; }
    }

    public List<StudentProgrammingSubmissionVo> getStudentHistory(BizStudent student, Long deptId, Long lessonId, Long questionId) {
        assertStudentQuestionAccess(student, deptId, lessonId, questionId);
        List<ProgrammingSubmission> list = programmingMapper.selectStudentSubmissions(student.getStudentId(), lessonId, questionId, 30);
        List<ProgrammingTestCase> publicCases = programmingMapper.selectPublicTestCases(questionId);
        Map<Long, ProgrammingTestCase> casesById = new HashMap<Long, ProgrammingTestCase>();
        for (ProgrammingTestCase item : publicCases) casesById.put(item.getTestCaseId(), item);
        List<StudentProgrammingSubmissionVo> result = new ArrayList<StudentProgrammingSubmissionVo>();
        for (ProgrammingSubmission item : list) {
            StudentProgrammingSubmissionVo safe = new StudentProgrammingSubmissionVo();
            safe.setSubmissionId(item.getSubmissionId()); safe.setSourceCode(item.getSourceCode()); safe.setSubmissionKind(item.getSubmissionKind()); safe.setStatusCode(item.getStatusCode()); safe.setStatusMessage(item.getStatusMessage()); safe.setScore(item.getScore()); safe.setPassedCaseCount(item.getPassedCaseCount()); safe.setTotalCaseCount(item.getTotalCaseCount()); safe.setTimeSeconds(item.getTimeSeconds()); safe.setMemoryKb(item.getMemoryKb()); safe.setSubmittedAt(item.getSubmittedAt()); safe.setJudgedAt(item.getJudgedAt()); safe.setCancelledAt(item.getCancelledAt());
            List<StudentProgrammingSubmissionCaseVo> safeCases = new ArrayList<StudentProgrammingSubmissionCaseVo>();
            for (ProgrammingSubmissionCase row : programmingMapper.selectSubmissionCases(item.getSubmissionId(), true)) {
                ProgrammingTestCase testCase = casesById.get(row.getTestCaseId());
                StudentProgrammingSubmissionCaseVo safeCase = new StudentProgrammingSubmissionCaseVo(); safeCase.setTestCaseId(row.getTestCaseId()); safeCase.setCaseName(testCase == null ? null : testCase.getCaseName()); safeCase.setInputText(testCase == null ? null : testCase.getInputText()); safeCase.setExpectedOutput(testCase == null ? null : testCase.getExpectedOutput()); safeCase.setActualOutput(row.getOutputText()); safeCase.setStatusCode(row.getStatusCode()); safeCase.setTimeSeconds(row.getTimeSeconds()); safeCase.setMemoryKb(row.getMemoryKb()); safeCase.setErrorMessage(row.getErrorSummary()); safeCases.add(safeCase);
            }
            safe.setCases(safeCases); result.add(safe);
        }
        return result;
    }

    public void cancel(BizStudent student, Long deptId, Long lessonId, Long questionId, Long submissionId) {
        assertStudentQuestionAccess(student, deptId, lessonId, questionId);
        int changed = programmingMapper.cancelSubmission(submissionId, student.getStudentId(), new Date());
        if (changed == 0) throw new ServiceException("仅等待中的本人提交可以取消");
        releaseSlot(submissionId, student, lessonId, questionId);
    }

    /** 进程异常重启后，旧队列不能继续占用学生锁，也不能被误判为零分。 */
    public void markStuckSubmissionAsServiceFailure(Long submissionId) {
        ProgrammingSubmission submission = programmingMapper.selectSubmissionById(submissionId);
        if (submission == null || (!STATUS_WAITING.equals(submission.getStatusCode()) && !STATUS_JUDGING.equals(submission.getStatusCode()))) return;
        completeServiceFailure(submission, "判题进程重启后未恢复，代码和提交已保留");
        releaseSlot(submission.getSubmissionId(), submission.getStudentId(), submission.getLessonId(), submission.getQuestionId());
    }

    /** 由独立线程调用；任何异常都记录为服务异常，绝不以零分代替。 */
    public void judgeSubmission(Long submissionId) {
        ProgrammingSubmission submission = programmingMapper.selectSubmissionById(submissionId);
        if (submission == null || programmingMapper.markJudging(submissionId, new Date()) != 1) return;
        try {
            ProgrammingQuestionConfig config = programmingMapper.selectConfig(submission.getQuestionId());
            if (config == null || !"1".equals(config.getEnabled())) throw new ServiceException("该编程题暂未开放判题");
            List<ProgrammingTestCase> cases = "RUN".equals(submission.getSubmissionKind()) ? programmingMapper.selectPublicTestCases(submission.getQuestionId()) : programmingMapper.selectTestCases(submission.getQuestionId());
            if (cases == null || cases.isEmpty()) throw new ServiceException("题目尚未配置可执行测试点");
            List<ProgrammingSubmissionCase> results = new ArrayList<ProgrammingSubmissionCase>();
            boolean serviceFailure = false;
            for (ProgrammingTestCase testCase : cases) {
                Judge0Result result = execute(submission.getSourceCode(), testCase, config);
                ProgrammingSubmissionCase caseResult = toCaseResult(submission.getSubmissionId(), testCase, result);
                programmingMapper.insertSubmissionCase(caseResult); results.add(caseResult);
                if (STATUS_SERVICE_ERROR.equals(caseResult.getStatusCode())) { serviceFailure = true; break; }
            }
            complete(submission, config, cases, results, serviceFailure ? STATUS_SERVICE_ERROR : null);
        } catch (Exception e) {
            completeServiceFailure(submission, safeMessage(e));
        } finally { releaseSlot(submission.getSubmissionId(), submission.getStudentId(), submission.getLessonId(), submission.getQuestionId()); }
    }

    private Judge0Result execute(String sourceCode, ProgrammingTestCase testCase, ProgrammingQuestionConfig config) throws InterruptedException {
        Judge0Request request = new Judge0Request(); request.setSourceCode(sourceCode); request.setStdin(testCase.getInputText()); request.setExpectedOutput(testCase.getExpectedOutput()); request.setCpuTimeLimit(config.getTimeLimitSeconds()); request.setMemoryLimitKb(config.getMemoryLimitKb()); request.setMaxProcesses(config.getMaxProcesses()); request.setMaxFileSizeKb(config.getMaxFileSizeKb()); request.setMaxOutputKb(config.getMaxOutputKb());
        Judge0Client client = resolveClient(); Judge0Result result = client.submit(request);
        if (result == null || result.getToken() == null) throw new ServiceException("Judge0 未返回提交令牌");
        for (int i = 0; i < Math.max(1, properties.getMaxPolls()); i++) { Thread.sleep(Math.max(100, properties.getPollIntervalMs())); result = client.poll(result.getToken()); if (result != null && result.isFinished()) return result; }
        throw new ServiceException("Judge0 判题轮询超时");
    }

    private Judge0Client resolveClient() {
        if ("mock".equalsIgnoreCase(properties.getMode())) return mockClient;
        if ("http".equalsIgnoreCase(properties.getMode())) return httpClient;
        throw new ServiceException("Judge0 判题服务未启用");
    }

    private ProgrammingSubmissionCase toCaseResult(Long submissionId, ProgrammingTestCase testCase, Judge0Result result) {
        ProgrammingSubmissionCase row = new ProgrammingSubmissionCase(); row.setSubmissionId(submissionId); row.setTestCaseId(testCase.getTestCaseId()); row.setIsPublic(testCase.getIsPublic()); row.setJudge0StatusId(result == null ? null : result.getStatusId()); row.setStatusCode(mapStatus(result));
        if (result != null) {
            row.setTimeSeconds(result.getTimeSeconds());
            row.setMemoryKb(result.getMemoryKb());
            if ("1".equals(testCase.getIsPublic())) {
                row.setOutputText(trim(result.getStdout(), 65536));
                row.setErrorSummary(trim(firstNonEmpty(result.getCompileOutput(), result.getStderr(), result.getMessage()), 1000));
            }
        }
        return row;
    }

    private void complete(final ProgrammingSubmission submission, final ProgrammingQuestionConfig config, final List<ProgrammingTestCase> testCases, final List<ProgrammingSubmissionCase> results, final String forcedStatus) {
        transactionTemplate.execute(status -> { completeInTransaction(submission, config, testCases, results, forcedStatus); return null; });
    }

    private void completeInTransaction(ProgrammingSubmission submission, ProgrammingQuestionConfig config, List<ProgrammingTestCase> testCases, List<ProgrammingSubmissionCase> results, String forcedStatus) {
        ProgrammingSubmission finalRow = programmingMapper.selectSubmissionById(submission.getSubmissionId());
        if (finalRow == null || STATUS_CANCELLED.equals(finalRow.getStatusCode())) return;
        int passed = 0; double totalWeight = 0D; double passedWeight = 0D; double maxTime = 0D; int maxMemory = 0;
        Map<Long, ProgrammingTestCase> casesById = new HashMap<Long, ProgrammingTestCase>();
        for (ProgrammingTestCase testCase : testCases) { casesById.put(testCase.getTestCaseId(), testCase); totalWeight += testCase.getScoreWeight() == null ? 1D : testCase.getScoreWeight(); }
        for (ProgrammingSubmissionCase result : results) {
            ProgrammingTestCase testCase = casesById.get(result.getTestCaseId());
            if (testCase == null) continue;
            double weight = testCase.getScoreWeight() == null ? 1D : testCase.getScoreWeight();
            if ("ACCEPTED".equals(result.getStatusCode())) { passed++; passedWeight += weight; }
            if (result.getTimeSeconds() != null) maxTime = Math.max(maxTime, result.getTimeSeconds());
            if (result.getMemoryKb() != null) maxMemory = Math.max(maxMemory, result.getMemoryKb());
        }
        String status = forcedStatus == null ? summarize(results, passed) : forcedStatus;
        finalRow.setStatusCode(status); finalRow.setStatusMessage(statusMessage(status)); finalRow.setPassedCaseCount(passed); finalRow.setTotalCaseCount(testCases.size()); finalRow.setTimeSeconds(maxTime); finalRow.setMemoryKb(maxMemory); finalRow.setJudgedAt(new Date());
        if ("SUBMIT".equals(finalRow.getSubmissionKind()) && !STATUS_SERVICE_ERROR.equals(status)) {
            int questionScore = lessonQuestionScore(finalRow.getLessonId(), finalRow.getQuestionId()); int score = totalWeight <= 0D ? 0 : (int) Math.round(questionScore * passedWeight / totalWeight); finalRow.setScore(score);
        }
        if (programmingMapper.updateSubmissionResult(finalRow) != 1) return;
        if ("SUBMIT".equals(finalRow.getSubmissionKind()) && !STATUS_SERVICE_ERROR.equals(status)) writeExistingAnswer(finalRow, finalRow.getScore(), "ACCEPTED".equals(status));
    }

    private void completeServiceFailure(ProgrammingSubmission submission, String message) {
        ProgrammingSubmission row = programmingMapper.selectSubmissionById(submission.getSubmissionId()); if (row == null || STATUS_CANCELLED.equals(row.getStatusCode())) return;
        row.setStatusCode(STATUS_SERVICE_ERROR); row.setStatusMessage("判题服务异常，代码和提交已保留"); row.setErrorSummary(message); row.setPassedCaseCount(0); row.setTotalCaseCount(0); row.setJudgedAt(new Date()); programmingMapper.updateSubmissionResult(row);
    }

    private void writeExistingAnswer(ProgrammingSubmission submission, int score, boolean correct) {
        BizStudentAnswer answer = new BizStudentAnswer(); answer.setStudentId(submission.getStudentId()); answer.setLessonId(submission.getLessonId()); answer.setQuestionId(submission.getQuestionId()); answer.setStudentAnswer(submission.getSourceCode()); answer.setScore(score); answer.setIsCorrect(correct); answer.setSubmitTime(new Date()); answer.setAnswerTime(submission.getTimeSeconds() == null ? 0 : (int) Math.ceil(submission.getTimeSeconds())); studentAnswerMapper.upsertAnswer(answer);
    }

    private void assertStudentQuestionAccess(BizStudent student, Long deptId, Long lessonId, Long questionId) {
        if (student == null || lessonId == null || questionId == null) throw new ServiceException("课程或题目参数不完整");
        Long current = lessonAssignmentMapper.selectCurrentLessonByClass(student.getEntryYear(), student.getClassCode(), deptId);
        if (!lessonId.equals(current)) throw new ServiceException("只能操作当前指派课程的编程题");
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId); if (lesson == null || lesson.getDeptId() == null || !deptId.equals(lesson.getDeptId())) throw new ServiceException("课程不存在或无权访问");
        for (BizLessonQuestionDetailVo q : lessonQuestionMapper.selectDetailsByLessonId(lessonId)) if (questionId.equals(q.getQuestionId()) && isPythonPractical(q)) return;
        throw new ServiceException("题目不存在或不是 Python 编程题");
    }

    private BizQuestion requirePythonQuestion(Long questionId) { BizQuestion question = questionMapper.selectBizQuestionByQuestionId(questionId); if (!isPythonPractical(question)) throw new ServiceException("题目不存在或不是 Python 在线编程操作题"); return question; }
    private boolean isPythonPractical(BizQuestion question) { return question != null && "practical".equalsIgnoreCase(question.getQuestionType()) && "PYTHON".equalsIgnoreCase(question.getPracticalMode()); }
    private boolean isPythonPractical(BizLessonQuestionDetailVo question) { return question != null && "practical".equalsIgnoreCase(question.getQuestionType()) && "PYTHON".equalsIgnoreCase(question.getPracticalMode()); }
    private void assertQuestionOwner(BizQuestion question, Long currentUserId, boolean admin) { if (!admin && (currentUserId == null || !currentUserId.equals(question.getCreatorId()))) throw new ServiceException("无权访问他人创建的 Python 题配置"); }
    private void assertQuestionPreviewAccess(BizQuestion question, Long currentUserId, boolean admin) {
        if (admin || (currentUserId != null && currentUserId.equals(question.getCreatorId())) || "Y".equalsIgnoreCase(question.getIsPublic()) || "1".equals(question.getIsPublic())) return;
        throw new ServiceException("无权预览他人创建的私有 Python 题");
    }
    private void validateSource(String sourceCode) { if (sourceCode == null || sourceCode.trim().isEmpty()) throw new ServiceException("代码不能为空"); if (sourceCode.getBytes(StandardCharsets.UTF_8).length > Math.max(1024, properties.getMaxSourceBytes())) throw new ServiceException("代码超过允许大小"); }
    private void normalizeConfig(ProgrammingQuestionConfig c) { if (c.getTimeLimitSeconds() == null || c.getTimeLimitSeconds() < 0.1D || c.getTimeLimitSeconds() > 10D) throw new ServiceException("时限应在 0.1 至 10 秒之间"); if (c.getMemoryLimitKb() == null || c.getMemoryLimitKb() < 16384 || c.getMemoryLimitKb() > 524288) throw new ServiceException("内存应在 16MB 至 512MB 之间"); if (c.getMaxProcesses() == null || c.getMaxProcesses() < 1 || c.getMaxProcesses() > 8) throw new ServiceException("进程数应在 1 至 8 之间"); if (c.getMaxFileSizeKb() == null || c.getMaxFileSizeKb() < 1 || c.getMaxFileSizeKb() > 4096) throw new ServiceException("文件限制应在 1KB 至 4MB 之间"); if (c.getMaxOutputKb() == null || c.getMaxOutputKb() < 1 || c.getMaxOutputKb() > 1024) throw new ServiceException("输出限制应在 1KB 至 1MB 之间"); validateTextLength("输入说明", c.getInputDescription(), 20000); validateTextLength("输出说明", c.getOutputDescription(), 20000); validateTextLength("样例解释", c.getSampleExplanation(), 20000); validateTextLength("限制条件", c.getConstraintsText(), 20000); validateTextLength("注意事项", c.getNotesText(), 20000); validateTextLength("初始代码", c.getStarterCode(), Math.max(1024, properties.getMaxSourceBytes())); }
    private void validateTestCases(List<ProgrammingTestCase> cases) { if (cases == null || cases.isEmpty()) throw new ServiceException("至少配置一个测试点"); if (cases.size() > 50) throw new ServiceException("测试点数量不能超过 50 个"); boolean hidden = false; double totalWeight = 0D; for (ProgrammingTestCase c : cases) { validateTextLength("测试点名称", c.getCaseName(), 128); validateTextLength("测试点输入", c.getInputText(), 65536); validateTextLength("测试点期望输出", c.getExpectedOutput(), 65536); if (c.getExpectedOutput() == null || c.getExpectedOutput().trim().isEmpty()) throw new ServiceException("测试点期望输出不能为空"); if (!"1".equals(c.getIsPublic())) { c.setIsPublic("0"); hidden = true; } if (c.getScoreWeight() == null || c.getScoreWeight() <= 0D) throw new ServiceException("测试点权重必须大于零"); totalWeight += c.getScoreWeight(); } if (!hidden) throw new ServiceException("至少需要一个隐藏测试点"); if (totalWeight > 100000D) throw new ServiceException("测试点权重总和过大"); }
    private void validateTextLength(String label, String value, int maxBytes) { if (value != null && value.getBytes(StandardCharsets.UTF_8).length > maxBytes) throw new ServiceException(label + "超过允许大小"); }
    private String mapStatus(Judge0Result r) { return Judge0StatusMapper.toPlatformStatus(r == null ? null : r.getStatusId()); }
    private String summarize(List<ProgrammingSubmissionCase> rows, int passed) { if (passed == rows.size()) return "ACCEPTED"; if (passed > 0) return "PARTIAL"; for (ProgrammingSubmissionCase row : rows) if (!"WRONG_ANSWER".equals(row.getStatusCode())) return row.getStatusCode(); return "WRONG_ANSWER"; }
    private String statusMessage(String status) { if ("ACCEPTED".equals(status)) return "通过"; if ("PARTIAL".equals(status)) return "部分通过"; if ("WRONG_ANSWER".equals(status)) return "答案错误"; if ("SYNTAX_ERROR".equals(status)) return "语法错误"; if ("RUNTIME_ERROR".equals(status)) return "运行错误"; if ("TIME_LIMIT".equals(status)) return "运行超时"; if ("MEMORY_LIMIT".equals(status)) return "内存超限"; return "判题服务异常，代码和提交已保留"; }
    private int lessonQuestionScore(Long lessonId, Long questionId) { for (BizLessonQuestionDetailVo q : lessonQuestionMapper.selectDetailsByLessonId(lessonId)) if (questionId.equals(q.getQuestionId())) return q.getQuestionScore() == null ? 0 : q.getQuestionScore().intValue(); return 0; }
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void reserveRateAndSlot(BizStudent student, Long deptId, Long lessonId, Long questionId) {
        ValueOperations<String, Object> values = redisCache.redisTemplate.opsForValue();
        String rateKey = "judge0:rate:" + student.getStudentId() + ":" + (System.currentTimeMillis() / 60000L); Long count = values.increment(rateKey); if (count != null && count.longValue() == 1L) redisCache.expire(rateKey, 70, TimeUnit.SECONDS); if (count != null && count.longValue() > properties.getStudentSubmitsPerMinute()) throw new ServiceException("提交过于频繁，请稍后再试");
        Boolean acquired = values.setIfAbsent(studentLockKey(student.getStudentId(), lessonId, questionId), "1", 120, TimeUnit.SECONDS); if (!Boolean.TRUE.equals(acquired)) throw new ServiceException("本题已有提交正在判题，请等待结果或取消等待中的提交");
        Long active = values.increment(classSlotKey(student, lessonId, questionId)); if (active != null && active.longValue() == 1L) redisCache.expire(classSlotKey(student, lessonId, questionId), 180, TimeUnit.SECONDS); if (active != null && active.longValue() > properties.getClassConcurrency()) { values.decrement(classSlotKey(student, lessonId, questionId)); redisCache.deleteObject(studentLockKey(student.getStudentId(), lessonId, questionId)); throw new ServiceException("本班判题队列已满，请稍后重试"); }
    }
    private void releaseSlot(Long submissionId, Long studentId, Long lessonId, Long questionId) {
        BizStudent student = studentMapper.selectBizStudentByStudentId(studentId);
        if (student != null) releaseSlot(submissionId, student, lessonId, questionId);
    }
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void releaseSlot(Long submissionId, BizStudent student, Long lessonId, Long questionId) {
        if (submissionId != null) {
            // 取消和异步线程可能先后到达；释放标记保证一个提交最多归还一次班级槽位。
            Boolean firstRelease = redisCache.redisTemplate.opsForValue().setIfAbsent(releasedSlotKey(submissionId), "1", 10, TimeUnit.MINUTES);
            if (!Boolean.TRUE.equals(firstRelease)) return;
            redisCache.deleteObject(slotKey(submissionId));
        }
        releaseReservation(student, lessonId, questionId);
    }
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void releaseReservation(BizStudent student, Long lessonId, Long questionId) {
        redisCache.deleteObject(studentLockKey(student.getStudentId(), lessonId, questionId));
        String classKey = classSlotKey(student, lessonId, questionId);
        if (redisCache.redisTemplate.opsForValue().get(classKey) != null) redisCache.redisTemplate.opsForValue().decrement(classKey);
    }
    private String studentLockKey(Long studentId, Long lessonId, Long questionId) { return "judge0:active:student:" + studentId + ":" + lessonId + ":" + questionId; }
    private String classSlotKey(BizStudent s, Long lessonId, Long questionId) { return "judge0:active:class:" + s.getDeptId() + ":" + s.getEntryYear() + ":" + s.getClassCode(); }
    private String slotKey(Long submissionId) { return "judge0:slot:submission:" + submissionId; }
    private String releasedSlotKey(Long submissionId) { return "judge0:released:submission:" + submissionId; }
    private String trim(String value, int max) { if (value == null) return null; String v = value.replaceAll("[\\r\\n]+", " ").trim(); return v.length() <= max ? v : v.substring(0, max); }
    private String firstNonEmpty(String... values) { for (String v : values) if (v != null && !v.trim().isEmpty()) return v; return null; }
    private String safeMessage(Exception e) { return trim(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage(), 1000); }
}
