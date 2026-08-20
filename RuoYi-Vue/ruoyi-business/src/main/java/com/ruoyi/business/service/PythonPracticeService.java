package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Date;
import java.util.Calendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Qualifier;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.ProgrammingQuestionConfig;
import com.ruoyi.business.domain.ProgrammingTestCase;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.ProgrammingJudgeMapper;
import com.ruoyi.business.mapper.PythonPracticeMapper;
import com.ruoyi.business.mapper.BizTeacherClassMapper;
import com.ruoyi.business.domain.BizTeacherClass;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.business.judge.Judge0Client;
import com.ruoyi.business.judge.Judge0Properties;
import com.ruoyi.business.judge.Judge0Request;
import com.ruoyi.business.judge.Judge0Result;
import com.ruoyi.business.judge.Judge0StatusMapper;

/** 独立 Python 刷题服务：不读取或写入课程答案和课程成绩。 */
@Service
public class PythonPracticeService {
    @Autowired private PythonPracticeMapper mapper;
    @Autowired private BizQuestionMapper questionMapper;
    @Autowired private ProgrammingJudgeMapper judgeMapper;
    @Autowired private BizStudentMapper studentMapper;
    @Autowired private BizTeacherClassMapper teacherClassMapper;
    @Autowired private Judge0Properties judgeProperties;
    @Autowired @Qualifier("judge0HttpClient") private Judge0Client httpClient;
    @Autowired @Qualifier("judge0MockClient") private Judge0Client mockClient;

    public List<Map<String, Object>> teacherPlans(Long deptId, String entryYear) {
        return mapper.selectPlans(deptId, entryYear);
    }

    public Map<String, Object> planDetail(Long planId, Long deptId) {
        Map<String, Object> plan = mapper.selectPlan(planId);
        requirePlanDept(plan, deptId);
        Long versionId = number(plan.get("plan_version_id"));
        plan.put("questions", versionId == null ? Collections.emptyList() : mapper.selectPlanQuestions(versionId));
        return plan;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPlan(Map<String, Object> request, Long userId, String username, Long deptId) {
        require(request, "grade", "planName");
        Map<String, Object> plan = new HashMap<String, Object>(request);
        int grade = Integer.parseInt(String.valueOf(request.get("grade")));
        if (grade < 1 || grade > 12) throw new ServiceException("年级参数无效");
        // 题单按当前学年创建，界面不再让教师手填届别，避免同一年级误配学生届别。
        plan.put("entryYear", String.valueOf(entryYearByGrade(grade)));
        plan.put("semester", "0");
        plan.put("deptId", deptId);
        plan.put("creatorId", userId);
        plan.put("createBy", username);
        Map<String, Object> existing = mapper.selectPlanByScope(deptId, grade, "0", String.valueOf(plan.get("entryYear")));
        if (existing != null) {
            throw new ServiceException("该年级已有 Python 基础题单“" + String.valueOf(existing.get("plan_name")) + "”，请直接配置已有题单；如需重建，请先删除未发布题单。");
        }
        try {
            mapper.insertPlan(plan);
        } catch (DuplicateKeyException ex) {
            throw new ServiceException("该年级已有 Python 基础题单，请直接配置已有题单；如需重建，请先删除未发布题单。");
        }
        Map<String, Object> version = new HashMap<String, Object>();
        version.put("planId", plan.get("planId")); version.put("versionNo", 1); version.put("creatorId", userId); version.put("createBy", username);
        mapper.insertPlanVersion(version);
        Map<String, Object> result = new HashMap<String, Object>(plan);
        result.put("planVersionId", version.get("planVersionId")); result.put("versionNo", 1);
        return result;
    }

    /** 仅允许删除从未发布的题单，避免破坏学生已经产生的刷题历史。 */
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long planId, Long deptId) {
        Map<String, Object> plan = mapper.selectPlan(planId);
        requirePlanDept(plan, deptId);
        if (mapper.countPublishedVersions(planId) > 0) {
            throw new ServiceException("已发布题单不能删除；请使用撤回或复制新版本。");
        }
        mapper.deletePlanQuestions(planId);
        mapper.deleteExtensionQuestions(planId);
        mapper.deleteExtensionClasses(planId);
        mapper.deletePlanSnapshotCases(planId);
        mapper.deletePlanSnapshots(planId);
        mapper.deletePlanVersions(planId);
        mapper.deleteExtensions(planId);
        mapper.deletePlan(planId);
    }

    private static int currentSchoolYear() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        return month > 7 || (month == 7 && day >= 20) ? year : year - 1;
    }

    private static int entryYearByGrade(int grade) {
        // 小学一年级和初中七年级都是新的入学起点，不能用一条公式跨学段推算。
        return currentSchoolYear() - (grade <= 6 ? grade - 1 : grade - 7);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addQuestion(Long planVersionId, Long questionId, Integer sortNo, String stage, String username, Long deptId) {
        Map<String, Object> version = mapper.selectPlanByVersion(planVersionId);
        requirePlanDept(version, deptId);
        if (version == null || !"DRAFT".equals(String.valueOf(version.get("version_status")))) {
            throw new ServiceException("已发布或已撤回的题单版本不能修改");
        }
        if (mapper.countPlanVersionQuestion(planVersionId, questionId) > 0) {
            throw new ServiceException("该题已在当前基础题单中");
        }
        BizQuestion question = questionMapper.selectBizQuestionByQuestionId(questionId);
        if (question == null || !"practical".equalsIgnoreCase(question.getQuestionType()) || !"PYTHON".equalsIgnoreCase(question.getPracticalMode())) {
            throw new ServiceException("只能加入 Python 在线编程题");
        }
        ProgrammingQuestionConfig config = judgeMapper.selectConfig(questionId);
        if (config == null || !"1".equals(config.getEnabled())) throw new ServiceException("该题尚未启用判题规则");
        validateTestCases(questionId);
        Map<String, Object> snapshot = new HashMap<String, Object>();
        snapshot.put("sourceType", "BASE_VERSION"); snapshot.put("sourceId", planVersionId); snapshot.put("questionId", questionId);
        snapshot.put("snapshotHash", UUID.randomUUID().toString().replace("-", "")); snapshot.put("questionContent", question.getQuestionContent());
        snapshot.put("inputDescription", config.getInputDescription()); snapshot.put("outputDescription", config.getOutputDescription()); snapshot.put("sampleExplanation", config.getSampleExplanation());
        snapshot.put("constraintsText", config.getConstraintsText()); snapshot.put("notesText", config.getNotesText()); snapshot.put("starterCode", config.getStarterCode());
        snapshot.put("timeLimitSeconds", config.getTimeLimitSeconds()); snapshot.put("memoryLimitKb", config.getMemoryLimitKb()); snapshot.put("maxProcesses", config.getMaxProcesses()); snapshot.put("maxFileSizeKb", config.getMaxFileSizeKb()); snapshot.put("maxOutputKb", config.getMaxOutputKb()); snapshot.put("createBy", username);
        mapper.insertSnapshot(snapshot);
        List<ProgrammingTestCase> cases = judgeMapper.selectTestCases(questionId);
        int order = 1;
        for (ProgrammingTestCase item : cases) {
            Map<String, Object> row = new HashMap<String, Object>(); row.put("snapshotId", snapshot.get("snapshotId")); row.put("caseName", item.getCaseName()); row.put("inputText", item.getInputText()); row.put("expectedOutput", item.getExpectedOutput()); row.put("isPublic", item.getIsPublic()); row.put("scoreWeight", item.getScoreWeight()); row.put("orderNum", order++); mapper.insertSnapshotCase(row);
        }
        Map<String, Object> link = new HashMap<String, Object>(); link.put("planVersionId", planVersionId); link.put("questionId", questionId); link.put("snapshotId", snapshot.get("snapshotId")); link.put("sortNo", sortNo == null ? 1 : sortNo); link.put("stage", stage == null ? "BEGINNER" : stage); link.put("requiredFlag", "1"); mapper.insertPlanQuestion(link);
        return snapshot;
    }

    public List<Map<String, Object>> extensions(Long planId, Long deptId) { requirePlanDept(mapper.selectPlan(planId), deptId); return mapper.selectExtensions(planId); }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createExtension(Map<String, Object> request, Long planId, Long userId, Long deptId, String username) {
        requirePlanDept(mapper.selectPlan(planId), deptId);
        require(request, "extensionName", "entryYear");
        Object classValue = request.get("classCode");
        Object classList = request.get("classes");
        if (classValue == null && !(classList instanceof List && !((List<?>) classList).isEmpty())) throw new ServiceException("至少选择一个加练班级");
        Map<String, Object> row = new HashMap<String, Object>(request); row.put("planId", planId); row.put("creatorId", userId); row.put("createBy", username); mapper.insertExtension(row);
        List<String> classes = new ArrayList<String>();
        if (classList instanceof List) for (Object item : (List<?>) classList) if (item != null && !String.valueOf(item).trim().isEmpty()) classes.add(String.valueOf(item).trim());
        if (classes.isEmpty()) for (String item : String.valueOf(classValue).split(",")) if (!item.trim().isEmpty()) classes.add(item.trim());
        for (String classCode : classes) ensureManagedClass(userId, deptId, String.valueOf(request.get("entryYear")), classCode);
        for (String classCode : classes) { Map<String, Object> cls = new HashMap<String, Object>(); cls.put("extensionId", row.get("extensionId")); cls.put("deptId", deptId); cls.put("entryYear", request.get("entryYear")); cls.put("classCode", classCode); mapper.insertExtensionClass(cls); }
        return row;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addExtensionQuestion(Long extensionId, Long questionId, Integer sortNo, String username, Long deptId) {
        Map<String, Object> extension = mapper.selectExtension(extensionId);
        requirePlanDept(extension == null ? null : mapper.selectPlan(number(extension.get("plan_id"))), deptId);
        if (extension == null || !"DRAFT".equals(String.valueOf(extension.get("status")))) {
            throw new ServiceException("已发布或已撤回的加练包不能修改");
        }
        Long planId = number(extension.get("plan_id"));
        if (mapper.countBaseQuestion(planId, questionId) > 0) {
            throw new ServiceException("该题已在年级基础题中，无需重复加入");
        }
        if (mapper.countPublishedExtensionConflict(extensionId, questionId) > 0) {
            throw new ServiceException("该题已在目标班级的其他已发布加练包中");
        }
        if (mapper.countExtensionQuestion(extensionId, questionId) > 0) {
            throw new ServiceException("该题已在当前加练包中");
        }
        BizQuestion question = questionMapper.selectBizQuestionByQuestionId(questionId); ProgrammingQuestionConfig config = judgeMapper.selectConfig(questionId);
        if (question == null || config == null || !"1".equals(config.getEnabled()) || !"practical".equalsIgnoreCase(question.getQuestionType()) || !"PYTHON".equalsIgnoreCase(question.getPracticalMode())) throw new ServiceException("只能加入已启用的 Python 在线编程题");
        validateTestCases(questionId);
        Map<String, Object> snapshot = new HashMap<String, Object>(); snapshot.put("sourceType", "EXTENSION"); snapshot.put("sourceId", extensionId); snapshot.put("questionId", questionId); snapshot.put("snapshotHash", UUID.randomUUID().toString().replace("-", "")); snapshot.put("questionContent", question.getQuestionContent()); snapshot.put("inputDescription", config.getInputDescription()); snapshot.put("outputDescription", config.getOutputDescription()); snapshot.put("sampleExplanation", config.getSampleExplanation()); snapshot.put("constraintsText", config.getConstraintsText()); snapshot.put("notesText", config.getNotesText()); snapshot.put("starterCode", config.getStarterCode()); snapshot.put("timeLimitSeconds", config.getTimeLimitSeconds()); snapshot.put("memoryLimitKb", config.getMemoryLimitKb()); snapshot.put("maxProcesses", config.getMaxProcesses()); snapshot.put("maxFileSizeKb", config.getMaxFileSizeKb()); snapshot.put("maxOutputKb", config.getMaxOutputKb()); snapshot.put("createBy", username); mapper.insertSnapshot(snapshot);
        int order = 1; for (ProgrammingTestCase item : judgeMapper.selectTestCases(questionId)) { Map<String, Object> row = new HashMap<String, Object>(); row.put("snapshotId", snapshot.get("snapshotId")); row.put("caseName", item.getCaseName()); row.put("inputText", item.getInputText()); row.put("expectedOutput", item.getExpectedOutput()); row.put("isPublic", item.getIsPublic()); row.put("scoreWeight", item.getScoreWeight()); row.put("orderNum", order++); mapper.insertSnapshotCase(row); }
        Map<String, Object> link = new HashMap<String, Object>(); link.put("extensionId", extensionId); link.put("questionId", questionId); link.put("snapshotId", snapshot.get("snapshotId")); link.put("sortNo", sortNo == null ? 1 : sortNo); mapper.insertExtensionQuestion(link); return snapshot;
    }

    public List<Map<String, Object>> analytics(String sourceType, Long sourceId, Long deptId, String entryYear, String classCode, Long userId) {
        requireAnalyticsSource(sourceType, sourceId, deptId);
        if (classCode != null && !classCode.trim().isEmpty()) ensureManagedClass(userId, deptId, entryYear, classCode.trim());
        return mapper.selectAnalytics(sourceType, sourceId, deptId, entryYear, classCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> publish(Long planId, Long planVersionId, Long deptId) {
        requirePlanDept(mapper.selectPlan(planId), deptId);
        Map<String, Object> version = mapper.selectPlanByVersion(planVersionId);
        if (version == null || !planId.equals(number(version.get("plan_id")))) throw new ServiceException("题单版本不存在或不属于当前题单");
        int questionCount = mapper.selectPlanQuestionCount(planVersionId);
        if (questionCount == 0 || mapper.selectSnapshotPublicCaseCount(planVersionId, true) < questionCount || mapper.selectSnapshotPublicCaseCount(planVersionId, false) < questionCount) {
            throw new ServiceException("发布前必须为每道题配置公开和隐藏测试点");
        }
        mapper.retractPublishedVersions(planId, planVersionId);
        if (mapper.publishVersion(planId, planVersionId) != 1) throw new ServiceException("题单版本不存在或不属于当前题单");
        Integer versionNo = mapper.selectVersionNo(planVersionId);
        if (versionNo == null) throw new ServiceException("题单版本不存在");
        mapper.updateCurrentVersion(planId, versionNo);
        return mapper.selectPlan(planId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> publishExtension(Long extensionId, Long deptId) {
        Map<String, Object> extension = mapper.selectExtension(extensionId);
        if (extension == null) throw new ServiceException("加练包不存在");
        requirePlanDept(mapper.selectPlan(number(extension.get("plan_id"))), deptId);
        int questionCount = mapper.selectExtensionQuestionCount(extensionId);
        if (questionCount == 0 || mapper.selectExtensionPublicCaseCount(extensionId, true) < questionCount || mapper.selectExtensionPublicCaseCount(extensionId, false) < questionCount) {
            throw new ServiceException("发布前必须为每道加练题配置公开和隐藏测试点");
        }
        if (mapper.publishExtension(extensionId) != 1) throw new ServiceException("加练包不是可发布的草稿");
        return mapper.selectExtension(extensionId);
    }

    public Map<String, Object> retractExtension(Long extensionId, Long deptId) {
        Map<String, Object> extension = mapper.selectExtension(extensionId);
        requirePlanDept(extension == null ? null : mapper.selectPlan(number(extension.get("plan_id"))), deptId);
        if (mapper.retractExtension(extensionId) != 1) throw new ServiceException("加练包不是已发布状态");
        return mapper.selectExtension(extensionId);
    }

    public Map<String, Object> studentOverview(Long userId, Long deptId) {
        BizStudent student = studentMapper.selectBizStudentByUserId(userId);
        if (student == null) throw new ServiceException("当前账号不是学生");
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("student", student);
        List<Map<String, Object>> questions = mapper.selectStudentQuestions(student.getDeptId(), student.getEntryYear(), student.getClassCode());
        Map<String, Map<String, Object>> progress = new HashMap<String, Map<String, Object>>();
        for (Map<String, Object> item : mapper.selectStudentProgress(student.getStudentId())) progress.put(progressKey(item), item);
        for (Map<String, Object> item : questions) item.put("progress", progress.get(progressKey(item)));
        result.put("questions", questions);
        return result;
    }

    public Map<String, Object> studentQuestion(Long userId, Long deptId, String sourceType, Long sourceId, Long questionId) {
        BizStudent student = requireStudent(userId);
        assertStudentScope(student, deptId, sourceType, sourceId, questionId);
        Map<String, Object> question = mapper.selectQuestion(sourceType, sourceId, questionId);
        if (question == null) throw new ServiceException("题目不存在或未开放");
        Map<String, Object> result = new HashMap<String, Object>(question);
        result.put("draft", mapper.selectDraft(student.getStudentId(), sourceType, sourceId, questionId));
        result.put("publicCases", mapper.selectSnapshotCases(number(question.get("snapshot_id")), true));
        result.put("history", mapper.selectSubmissions(student.getStudentId(), sourceType, sourceId, questionId));
        return result;
    }

    public Map<String, Object> saveDraft(Long userId, String sourceType, Long sourceId, Long questionId, String sourceCode) {
        BizStudent student = requireStudent(userId); assertStudentScope(student, student.getDeptId(), sourceType, sourceId, questionId); Map<String, Object> question = mapper.selectQuestion(sourceType, sourceId, questionId);
        if (question == null) throw new ServiceException("题目不存在或未开放");
        Map<String, Object> row = new HashMap<String, Object>(); row.put("studentId", student.getStudentId()); row.put("sourceType", sourceType); row.put("sourceId", sourceId); row.put("questionId", questionId); row.put("snapshotId", question.get("snapshot_id")); row.put("sourceCode", sourceCode == null ? "" : sourceCode); mapper.upsertDraft(row); return row;
    }

    public Map<String, Object> submit(Long userId, String sourceType, Long sourceId, Long questionId, String sourceCode, String submitType, PythonPracticeSubmissionWorker submissionWorker) {
        BizStudent student = requireStudent(userId); assertStudentScope(student, student.getDeptId(), sourceType, sourceId, questionId); Map<String, Object> question = mapper.selectQuestion(sourceType, sourceId, questionId);
        if (question == null) throw new ServiceException("题目不存在或未开放");
        Map<String, Object> row = new HashMap<String, Object>(); row.put("submissionKey", UUID.randomUUID().toString().replace("-", "")); row.put("studentId", student.getStudentId()); row.put("sourceType", sourceType); row.put("sourceId", sourceId); row.put("questionId", questionId); row.put("snapshotId", question.get("snapshot_id")); row.put("sourceCode", sourceCode == null ? "" : sourceCode); row.put("submitType", submitType == null ? "SUBMIT" : submitType); mapper.insertSubmission(row); row.put("statusCode", "WAITING"); row.put("statusMessage", "已进入独立刷题判题队列");
        try {
            submissionWorker.judge(number(row.get("submissionId")));
        }
        catch (RuntimeException ex) {
            // 异步执行器满载会在投递阶段直接拒绝；必须结束本次记录，避免学生端永久显示“等待判题”。
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("submissionId", row.get("submissionId"));
            result.put("statusCode", "SERVICE_BUSY");
            result.put("statusMessage", "判题队列繁忙，请稍后重新提交");
            result.put("score", null);
            result.put("passedCaseCount", 0);
            result.put("totalCaseCount", 0);
            result.put("judgeSummary", "判题队列繁忙");
            mapper.updateSubmissionResult(result);
            row.put("statusCode", result.get("statusCode"));
            row.put("statusMessage", result.get("statusMessage"));
        }
        return row;
    }

    /** 独立提交的异步判题，只更新 Python 刷题提交表。 */
    public void judgeSubmission(Long submissionId) {
        Map<String, Object> submission = mapper.selectSubmission(submissionId);
        if (submission == null || mapper.markSubmissionJudging(submissionId) != 1) return;
        try {
            List<Map<String, Object>> cases = mapper.selectSnapshotCases(number(submission.get("snapshot_id")), "RUN".equals(submission.get("submit_type")));
            if (cases.isEmpty()) throw new ServiceException("题目尚未配置测试点");
            int passed = 0; String firstFailure = "";
            for (Map<String, Object> item : cases) {
                Judge0Request request = new Judge0Request(); request.setSourceCode(String.valueOf(submission.get("source_code"))); request.setStdin((String) item.get("input_text")); request.setExpectedOutput((String) item.get("expected_output"));
                request.setCpuTimeLimit(decimal(item.get("time_limit_seconds"), 2D)); request.setMemoryLimitKb(intValue(item.get("memory_limit_kb"), 131072));
                Judge0Result result = client().submit(request);
                if (result == null || result.getToken() == null) throw new ServiceException("Judge0 未返回提交令牌");
                for (int poll = 0; poll < Math.max(1, judgeProperties.getMaxPolls()); poll++) { Thread.sleep(Math.max(100, judgeProperties.getPollIntervalMs())); result = client().poll(result.getToken()); if (result != null && result.isFinished()) break; }
                String status = Judge0StatusMapper.toPlatformStatus(result == null ? null : result.getStatusId());
                if ("ACCEPTED".equals(status)) passed++; else if (firstFailure.isEmpty()) firstFailure = status;
            }
            String status = passed == cases.size() ? "ACCEPTED" : (passed > 0 ? "PARTIAL" : (firstFailure.isEmpty() ? "SERVICE_ERROR" : firstFailure));
            Map<String, Object> result = new HashMap<String, Object>(); result.put("submissionId", submissionId); result.put("statusCode", status); result.put("statusMessage", statusMessage(status)); result.put("score", Math.round(100F * passed / cases.size())); result.put("passedCaseCount", passed); result.put("totalCaseCount", cases.size()); result.put("judgeSummary", passed + "/" + cases.size()); mapper.updateSubmissionResult(result);
            if ("SUBMIT".equalsIgnoreCase(String.valueOf(submission.get("submit_type")))) {
                Map<String, Object> progress = new HashMap<String, Object>();
                progress.put("studentId", submission.get("student_id")); progress.put("sourceType", submission.get("source_type")); progress.put("sourceId", submission.get("source_id")); progress.put("questionId", submission.get("question_id"));
                progress.put("bestScore", Math.round(100F * passed / cases.size())); progress.put("passedFlag", passed == cases.size() ? "1" : "0");
                progress.put("firstPassTime", passed == cases.size() ? new Date() : null); mapper.upsertProgress(progress);
            }
        } catch (Exception ex) { Map<String, Object> result = new HashMap<String, Object>(); result.put("submissionId", submissionId); result.put("statusCode", "SERVICE_ERROR"); result.put("statusMessage", "判题服务异常，代码和提交已保留"); result.put("score", null); result.put("passedCaseCount", 0); result.put("totalCaseCount", 0); result.put("judgeSummary", "判题服务异常"); mapper.updateSubmissionResult(result); }
    }

    private Judge0Client client() { if ("mock".equalsIgnoreCase(judgeProperties.getMode())) return mockClient; if ("http".equalsIgnoreCase(judgeProperties.getMode())) return httpClient; throw new ServiceException("Judge0 判题服务未启用"); }
    private static int intValue(Object value, int fallback) { return value == null ? fallback : Integer.valueOf(String.valueOf(value)); }
    private static double decimal(Object value, double fallback) { return value == null ? fallback : Double.valueOf(String.valueOf(value)); }
    private String statusMessage(String status) { if ("ACCEPTED".equals(status)) return "通过"; if ("PARTIAL".equals(status)) return "部分通过"; if ("WRONG_ANSWER".equals(status)) return "答案错误"; if ("SYNTAX_ERROR".equals(status)) return "语法错误"; if ("TIME_LIMIT".equals(status)) return "运行超时"; if ("MEMORY_LIMIT".equals(status)) return "内存超限"; return "判题服务异常，代码和提交已保留"; }

    private BizStudent requireStudent(Long userId) { BizStudent student = studentMapper.selectBizStudentByUserId(userId); if (student == null) throw new ServiceException("当前账号不是学生"); return student; }
    private void assertStudentScope(BizStudent student, Long deptId, String sourceType, Long sourceId, Long questionId) {
        Long actualDept = deptId == null ? student.getDeptId() : deptId;
        boolean allowed = false;
        for (Map<String, Object> item : mapper.selectStudentQuestions(actualDept, student.getEntryYear(), student.getClassCode())) {
            if (sourceType.equals(item.get("source_type")) && sourceId != null && sourceId.equals(number(item.get("source_id"))) && questionId != null && questionId.equals(number(item.get("question_id")))) { allowed = true; break; }
        }
        if (!allowed) throw new ServiceException("题目不属于当前学生可练习范围");
    }
    private static void require(Map<String, Object> request, String... keys) { for (String key : keys) if (request.get(key) == null || String.valueOf(request.get(key)).trim().isEmpty()) throw new ServiceException("缺少参数：" + key); }
    private static Long number(Object value) { return value == null ? null : Long.valueOf(String.valueOf(value)); }
    private void validateTestCases(Long questionId) {
        List<ProgrammingTestCase> cases = judgeMapper.selectTestCases(questionId);
        boolean hasPublic = false; boolean hasHidden = false;
        for (ProgrammingTestCase item : cases) { if ("1".equals(item.getIsPublic())) hasPublic = true; else hasHidden = true; }
        if (cases.isEmpty() || !hasPublic || !hasHidden) throw new ServiceException("题目必须同时配置公开和隐藏测试点");
    }
    private static String progressKey(Map<String, Object> item) { return String.valueOf(item.get("source_type")) + ":" + item.get("source_id") + ":" + item.get("question_id"); }
    private static void requirePlanDept(Map<String, Object> plan, Long deptId) {
        if (plan == null || (deptId != null && !deptId.equals(number(plan.get("dept_id"))))) throw new ServiceException("题单不存在或无权访问");
    }
    private void requireAnalyticsSource(String sourceType, Long sourceId, Long deptId) {
        if (sourceId == null) throw new ServiceException("学习情况来源不能为空");
        if ("BASE_VERSION".equals(sourceType)) {
            requirePlanDept(mapper.selectPlanByVersion(sourceId), deptId);
            return;
        }
        if ("EXTENSION".equals(sourceType)) {
            Map<String, Object> extension = mapper.selectExtension(sourceId);
            requirePlanDept(extension == null ? null : mapper.selectPlan(number(extension.get("plan_id"))), deptId);
            return;
        }
        throw new ServiceException("学习情况来源无效");
    }
    private void ensureManagedClass(Long userId, Long deptId, String entryYear, String classCode) {
        if (SecurityUtils.isAdmin(userId) || SecurityUtils.hasRole("researcher")) return;
        BizTeacherClass query = new BizTeacherClass(); query.setUserId(userId); query.setDeptId(deptId); query.setEntryYear(entryYear); query.setClassCode(classCode);
        if (teacherClassMapper.checkTeacherClassExists(query) <= 0) throw new ServiceException("当前教师无权管理该班级");
    }
}
