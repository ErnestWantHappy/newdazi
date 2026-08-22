package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Date;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import com.ruoyi.business.util.AcademicYearUtils;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.business.judge.Judge0Client;
import com.ruoyi.business.judge.Judge0Properties;
import com.ruoyi.business.judge.Judge0Request;
import com.ruoyi.business.judge.Judge0Result;
import com.ruoyi.business.judge.Judge0StatusMapper;
import com.ruoyi.business.judge.OutputComparator;

/** 独立 Python 刷题服务：不读取或写入课程答案和课程成绩。 */
@Service
public class PythonPracticeService {
    @Autowired private PythonPracticeMapper mapper;
    @Autowired private BizQuestionMapper questionMapper;
    @Autowired private ProgrammingJudgeMapper judgeMapper;
    @Autowired private BizStudentMapper studentMapper;
    @Autowired private BizTeacherClassMapper teacherClassMapper;
    @Autowired private SysDeptMapper deptMapper;
    @Autowired private Judge0Properties judgeProperties;
    @Autowired @Qualifier("judge0HttpClient") private Judge0Client httpClient;
    @Autowired @Qualifier("judge0MockClient") private Judge0Client mockClient;

    public List<Map<String, Object>> teacherPlans(Long deptId, String entryYear) {
        List<Map<String, Object>> plans = mapper.selectPlans(deptId, entryYear);
        String schoolType = requireSchoolType(deptId);
        for (Map<String, Object> plan : plans) {
            Long versionId = number(plan.get("plan_version_id"));
            List<Map<String, Object>> classes = versionId == null
                ? Collections.<Map<String, Object>>emptyList()
                : decorateCurrentClasses(mapper.selectPlanClasses(versionId), schoolType);
            plan.put("class_names", joinClassLabels(classes));
        }
        return plans;
    }

    public List<Map<String, Object>> managedClasses(Long userId, Long deptId) {
        List<Map<String, Object>> classes = mapper.selectManagedClasses(userId, deptId,
            SecurityUtils.isAdmin(userId) || SecurityUtils.hasRole("researcher"));
        return decorateCurrentClasses(classes, requireSchoolType(deptId));
    }

    public Map<String, Object> planDetail(Long planId, Long deptId) {
        Map<String, Object> plan = mapper.selectPlan(planId);
        requirePlanDept(plan, deptId);
        Long versionId = number(plan.get("plan_version_id"));
        plan.put("questions", versionId == null ? Collections.emptyList() : mapper.selectPlanQuestions(versionId));
        String schoolType = requireSchoolType(deptId);
        plan.put("classes", versionId == null ? Collections.emptyList()
            : decorateCurrentClasses(mapper.selectPlanClasses(versionId), schoolType));
        Long publishedVersionId = number(plan.get("published_version_id"));
        plan.put("publishedClasses", publishedVersionId == null ? Collections.emptyList()
            : decorateCurrentClasses(mapper.selectPlanClasses(publishedVersionId), schoolType));
        return plan;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPlan(Map<String, Object> request, Long userId, String username, Long deptId) {
        require(request, "planName");
        List<Map<String, String>> classes = parseClasses(request.get("classes"));
        if (classes.isEmpty()) throw new ServiceException("请至少选择一个目标班级");
        validateManagedClasses(classes, userId, deptId);
        Map<String, Object> plan = new HashMap<String, Object>(request);
        String entryYear = classes.get(0).get("entryYear");
        plan.put("grade", intValue(request.get("grade"), 0));
        plan.put("entryYear", entryYear);
        plan.put("semester", "0");
        plan.put("deptId", deptId);
        plan.put("creatorId", userId);
        plan.put("createBy", username);
        mapper.insertPlan(plan);
        Map<String, Object> version = new HashMap<String, Object>();
        version.put("planId", plan.get("planId")); version.put("versionNo", 1); version.put("creatorId", userId); version.put("createBy", username);
        mapper.insertPlanVersion(version);
        replacePlanClasses(number(version.get("planVersionId")), classes, userId, deptId, username);
        Map<String, Object> result = new HashMap<String, Object>(plan);
        result.put("planVersionId", version.get("planVersionId")); result.put("versionNo", 1);
        result.put("classes", classes);
        return result;
    }

    /** 题单删除采用物理删除，学生练习记录一并清理，不再保留归档入口。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deletePlan(Long planId, Long deptId) {
        Map<String, Object> plan = mapper.selectPlan(planId);
        requirePlanDept(plan, deptId);
        Map<String, Object> result = new HashMap<String, Object>();
        mapper.deletePlanDrafts(planId);
        mapper.deletePlanSubmissionCases(planId);
        mapper.deletePlanSubmissions(planId);
        mapper.deletePlanProgress(planId);
        mapper.deleteAllPlanClasses(planId);
        mapper.deletePlanQuestions(planId);
        mapper.deleteExtensionQuestions(planId);
        mapper.deleteExtensionClasses(planId);
        mapper.deletePlanSnapshotCases(planId);
        mapper.deletePlanSnapshots(planId);
        mapper.deletePlanVersions(planId);
        mapper.deleteExtensions(planId);
        mapper.deletePlan(planId);
        result.put("deleteMode", "HARD_DELETED");
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updatePlan(Long planId, Map<String, Object> request, Long userId,
                                          String username, Long deptId) {
        require(request, "planName");
        Map<String, Object> plan = mapper.selectPlan(planId);
        requirePlanDept(plan, deptId);
        List<Map<String, String>> classes = parseClasses(request.get("classes"));
        if (classes.isEmpty()) throw new ServiceException("请至少选择一个目标班级");
        validateManagedClasses(classes, userId, deptId);
        Long editableVersionId = ensureEditableVersion(number(plan.get("plan_version_id")), userId, username, deptId);
        mapper.updatePlanName(planId, String.valueOf(request.get("planName")).trim(), username);
        replacePlanClasses(editableVersionId, classes, userId, deptId, username);
        return planDetail(planId, deptId);
    }

    /** 已发布版本保持不变；首次修改时复制成新草稿，学生继续使用旧发布版本。 */
    private Long ensureEditableVersion(Long planVersionId, Long userId, String username, Long deptId) {
        Map<String, Object> version = mapper.selectPlanByVersion(planVersionId);
        requirePlanDept(version, deptId);
        String status = String.valueOf(version.get("version_status"));
        if ("DRAFT".equals(status)) return planVersionId;
        if (!"PUBLISHED".equals(status)) throw new ServiceException("该题单版本不能修改");
        Long planId = number(version.get("plan_id"));
        Map<String, Object> existing = mapper.selectLatestDraftVersion(planId);
        if (existing != null) return number(existing.get("plan_version_id"));
        Map<String, Object> draft = new HashMap<String, Object>();
        draft.put("planId", planId);
        draft.put("versionNo", mapper.selectMaxVersionNo(planId) + 1);
        draft.put("creatorId", userId);
        draft.put("createBy", username);
        mapper.insertPlanVersion(draft);
        Long targetId = number(draft.get("planVersionId"));
        mapper.clonePlanClasses(planVersionId, targetId, username);
        mapper.cloneSnapshots(planVersionId, targetId, username);
        mapper.cloneSnapshotCases(planVersionId, targetId);
        mapper.clonePlanQuestions(planVersionId, targetId);
        return targetId;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addQuestion(Long planVersionId, Long questionId, Integer sortNo, String stage, String username, Long deptId) {
        Long editableVersionId = ensureEditableVersion(planVersionId, SecurityUtils.getUserId(), username, deptId);
        return addQuestionToVersion(editableVersionId, questionId, sortNo, stage, username);
    }

    private Map<String, Object> addQuestionToVersion(Long planVersionId, Long questionId, Integer sortNo,
                                                     String stage, String username) {
        if (mapper.countPlanVersionQuestion(planVersionId, questionId) > 0) throw new ServiceException("该题已在当前题单中");
        BizQuestion question = questionMapper.selectBizQuestionByQuestionId(questionId);
        if (question == null || !"practical".equalsIgnoreCase(question.getQuestionType()) || !"PYTHON".equalsIgnoreCase(question.getPracticalMode())) {
            throw new ServiceException("只能加入 Python 在线编程题");
        }
        ProgrammingQuestionConfig config = judgeMapper.selectConfig(questionId);
        if (config == null || !"1".equals(config.getEnabled())) throw new ServiceException("该题尚未启用判题规则");
        if (!"VALID".equals(config.getValidationStatus())) throw new ServiceException("该题尚未通过参考代码验证，不能加入题单");
        validateTestCases(questionId);
        Map<String, Object> snapshot = new HashMap<String, Object>();
        snapshot.put("sourceType", "BASE_VERSION"); snapshot.put("sourceId", planVersionId); snapshot.put("questionId", questionId);
        snapshot.put("snapshotHash", UUID.randomUUID().toString().replace("-", "")); snapshot.put("questionContent", question.getQuestionContent());
        snapshot.put("questionTitle", config.getTitle()); snapshot.put("difficulty", question.getDifficulty()); snapshot.put("knowledgePoints", config.getKnowledgePoints()); snapshot.put("noInput", config.getNoInput());
        snapshot.put("inputDescription", config.getInputDescription()); snapshot.put("outputDescription", config.getOutputDescription()); snapshot.put("sampleExplanation", config.getSampleExplanation());
        snapshot.put("constraintsText", config.getConstraintsText()); snapshot.put("notesText", config.getNotesText()); snapshot.put("starterCode", config.getStarterCode());
        snapshot.put("timeLimitSeconds", config.getTimeLimitSeconds()); snapshot.put("memoryLimitKb", config.getMemoryLimitKb()); snapshot.put("maxProcesses", config.getMaxProcesses()); snapshot.put("maxFileSizeKb", config.getMaxFileSizeKb()); snapshot.put("maxOutputKb", config.getMaxOutputKb()); snapshot.put("createBy", username);
        mapper.insertSnapshot(snapshot);
        List<ProgrammingTestCase> cases = judgeMapper.selectTestCases(questionId);
        int order = 1;
        for (ProgrammingTestCase item : cases) {
            Map<String, Object> row = new HashMap<String, Object>(); row.put("snapshotId", snapshot.get("snapshotId")); row.put("caseName", item.getCaseName()); row.put("inputText", item.getInputText()); row.put("expectedOutput", item.getExpectedOutput()); row.put("isPublic", item.getIsPublic()); row.put("scoreWeight", item.getScoreWeight()); row.put("orderNum", order++); mapper.insertSnapshotCase(row);
        }
        Map<String, Object> link = new HashMap<String, Object>(); link.put("planVersionId", planVersionId); link.put("questionId", questionId); link.put("snapshotId", snapshot.get("snapshotId")); link.put("sortNo", sortNo == null ? mapper.selectPlanQuestionCount(planVersionId) + 1 : sortNo); link.put("stage", stage == null ? "BEGINNER" : stage); link.put("requiredFlag", "1"); mapper.insertPlanQuestion(link);
        snapshot.put("planVersionId", planVersionId);
        return snapshot;
    }

    /** 从 V2 系统题中按知识点和难度自动配题，教师仍可在发布前增删调整。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> recommendQuestions(Long planVersionId, Integer requestedCount, String username, Long deptId) {
        Long editableVersionId = ensureEditableVersion(planVersionId, SecurityUtils.getUserId(), username, deptId);
        int count = requestedCount == null ? 12 : requestedCount.intValue();
        if (count < 1 || count > 30) throw new ServiceException("一次推荐题数应在 1 至 30 道之间");
        List<Map<String, Object>> selected = selectRecommended(mapper.selectRecommendedQuestions(editableVersionId), count);
        if (selected.isEmpty()) {
            throw new ServiceException("暂无可推荐的 V2 系统题，请先完成系统题库导入和验证");
        }
        int sortNo = mapper.selectPlanQuestionCount(editableVersionId) + 1;
        for (Map<String, Object> item : selected) {
            Long questionId = number(item.get("question_id"));
            addQuestionToVersion(editableVersionId, questionId, sortNo++, stageByDifficulty(String.valueOf(item.get("difficulty"))), username);
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("recommendedCount", selected.size());
        result.put("requestedCount", count);
        result.put("questions", selected);
        result.put("planVersionId", editableVersionId);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addQuestions(Long planVersionId, List<?> questionIds, String username, Long deptId) {
        if (questionIds == null || questionIds.isEmpty()) throw new ServiceException("请选择要加入的题目");
        if (questionIds.size() > 100) throw new ServiceException("一次最多加入 100 道题");
        Long editableVersionId = ensureEditableVersion(planVersionId, SecurityUtils.getUserId(), username, deptId);
        int sortNo = mapper.selectPlanQuestionCount(editableVersionId) + 1;
        int added = 0;
        Set<Long> unique = new HashSet<Long>();
        for (Object item : questionIds) {
            Long questionId = numberOrNull(item);
            if (questionId == null || !unique.add(questionId)) continue;
            addQuestionToVersion(editableVersionId, questionId, sortNo++, null, username);
            added++;
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("planVersionId", editableVersionId);
        result.put("addedCount", added);
        return result;
    }

    /** 固定比例为简单 50%、中等约 40%、困难补足，并优先让主知识点不重复。 */
    private static List<Map<String, Object>> selectRecommended(List<Map<String, Object>> candidates, int requestedCount) {
        List<Map<String, Object>> source = candidates == null ? Collections.<Map<String, Object>>emptyList() : candidates;
        int target = Math.min(Math.max(0, requestedCount), source.size());
        List<Map<String, Object>> selected = new ArrayList<Map<String, Object>>();
        int simple = (target + 1) / 2;
        int medium = Math.min(target - simple, (int) Math.round(target * 0.4D));
        int hard = target - simple - medium;
        pickDiverse(source, "SIMPLE", simple, selected);
        pickDiverse(source, "MEDIUM", medium, selected);
        pickDiverse(source, "HARD", hard, selected);
        pickDiverse(source, null, target - selected.size(), selected);
        return selected;
    }

    private static void pickDiverse(List<Map<String, Object>> source, String difficulty, int count, List<Map<String, Object>> selected) {
        if (count <= 0) return;
        java.util.Set<Long> selectedIds = new java.util.HashSet<Long>();
        java.util.Set<String> knowledge = new java.util.HashSet<String>();
        for (Map<String, Object> item : selected) {
            selectedIds.add(number(item.get("question_id")));
            knowledge.add(primaryKnowledge(item.get("knowledge_points")));
        }
        int before = selected.size();
        for (Map<String, Object> item : source) {
            if (selected.size() - before >= count) break;
            Long id = number(item.get("question_id"));
            if (selectedIds.contains(id) || (difficulty != null && !difficulty.equals(String.valueOf(item.get("difficulty"))))) continue;
            String point = primaryKnowledge(item.get("knowledge_points"));
            if (knowledge.contains(point)) continue;
            selected.add(item); selectedIds.add(id); knowledge.add(point);
        }
        for (Map<String, Object> item : source) {
            if (selected.size() - before >= count) break;
            Long id = number(item.get("question_id"));
            if (selectedIds.contains(id) || (difficulty != null && !difficulty.equals(String.valueOf(item.get("difficulty"))))) continue;
            selected.add(item); selectedIds.add(id);
        }
    }

    private static String primaryKnowledge(Object value) {
        String text = value == null ? "未分类" : String.valueOf(value).trim();
        if (text.isEmpty()) return "未分类";
        int comma = text.indexOf('，');
        if (comma < 0) comma = text.indexOf(',');
        return comma < 0 ? text : text.substring(0, comma).trim();
    }

    private static String stageByDifficulty(String difficulty) {
        return "HARD".equals(difficulty) ? "ADVANCED" : ("MEDIUM".equals(difficulty) ? "SYNTAX" : "BEGINNER");
    }

    /**
     * 已发布题单允许直接撤下题目，但不删除快照和学生提交，保证历史练习可追溯。
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeQuestion(Long planVersionId, Long questionId, Long deptId) {
        Long editableVersionId = ensureEditableVersion(planVersionId, SecurityUtils.getUserId(), SecurityUtils.getUsername(), deptId);
        if (mapper.deletePlanQuestion(editableVersionId, questionId) != 1) {
            throw new ServiceException("题目不存在或已被移除");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> reorderQuestions(Long planVersionId, List<?> questionIds, Long deptId) {
        if (questionIds == null || questionIds.isEmpty()) throw new ServiceException("题目顺序不能为空");
        Long editableVersionId = ensureEditableVersion(planVersionId, SecurityUtils.getUserId(), SecurityUtils.getUsername(), deptId);
        if (questionIds.size() != mapper.selectPlanQuestionCount(editableVersionId)) throw new ServiceException("题目顺序与当前题单不一致，请刷新后重试");
        mapper.offsetPlanQuestionSort(editableVersionId);
        Set<Long> unique = new HashSet<Long>();
        int sortNo = 1;
        for (Object item : questionIds) {
            Long questionId = numberOrNull(item);
            if (questionId == null || !unique.add(questionId)
                || mapper.updatePlanQuestionSort(editableVersionId, questionId, sortNo++) != 1) {
                throw new ServiceException("题目顺序包含无效或重复题目");
            }
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("planVersionId", editableVersionId);
        result.put("questions", mapper.selectPlanQuestions(editableVersionId));
        return result;
    }

    public Map<String, Object> analytics(Long planVersionId, Long deptId, String entryYear,
                                         String classCode, Long userId) {
        Map<String, Object> plan = mapper.selectPlanByVersion(planVersionId);
        requirePlanDept(plan, deptId);
        if (entryYear == null || entryYear.trim().isEmpty() || classCode == null || classCode.trim().isEmpty()) {
            throw new ServiceException("请选择题单中的一个班级查看学情");
        }
        ensureManagedClass(userId, deptId, entryYear.trim(), classCode.trim());
        if (mapper.countPlanClass(planVersionId, deptId, entryYear.trim(), classCode.trim()) == 0) {
            throw new ServiceException("该班级不属于当前题单");
        }
        List<Map<String, Object>> students = mapper.selectPlanAnalyticsStudents(planVersionId, deptId, entryYear.trim(), classCode.trim());
        decorateCurrentClasses(students, requireSchoolType(deptId));
        List<Map<String, Object>> questions = mapper.selectPlanAnalyticsQuestions(planVersionId, deptId, entryYear.trim(), classCode.trim());
        int started = 0, completed = 0, submissions = 0;
        for (Map<String, Object> row : students) {
            int attempted = intValue(row.get("attempted_count"), 0);
            int passed = intValue(row.get("passed_count"), 0);
            int total = intValue(row.get("question_count"), 0);
            if (attempted > 0) started++;
            if (total > 0 && passed >= total) completed++;
            submissions += intValue(row.get("submit_count"), 0);
        }
        for (Map<String, Object> row : questions) {
            int target = intValue(row.get("target_student_count"), 0);
            int passed = intValue(row.get("passed_student_count"), 0);
            int attempted = intValue(row.get("attempted_student_count"), 0);
            row.put("passRate", target == 0 ? 0D : Math.round(1000D * passed / target) / 10D);
            row.put("attemptRate", target == 0 ? 0D : Math.round(1000D * attempted / target) / 10D);
        }
        Map<String, Object> summary = new HashMap<String, Object>();
        summary.put("targetStudents", students.size()); summary.put("questionCount", questions.size());
        summary.put("startedStudents", started); summary.put("completedStudents", completed);
        summary.put("completionRate", students.isEmpty() ? 0D : Math.round(1000D * completed / students.size()) / 10D);
        summary.put("totalSubmissions", submissions);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("summary", summary); result.put("students", students); result.put("questions", questions);
        return result;
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
        if (mapper.countPlanClasses(planVersionId) == 0) throw new ServiceException("发布前必须至少选择一个目标班级");
        mapper.retractPublishedVersions(planId, planVersionId);
        if (mapper.publishVersion(planId, planVersionId) != 1) throw new ServiceException("题单版本不存在或不属于当前题单");
        Integer versionNo = mapper.selectVersionNo(planVersionId);
        if (versionNo == null) throw new ServiceException("题单版本不存在");
        mapper.updateCurrentVersion(planId, versionNo);
        return mapper.selectPlan(planId);
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
        List<Map<String, Object>> history = mapper.selectSubmissions(student.getStudentId(), sourceType, sourceId, questionId);
        for (Map<String, Object> submission : history) {
            submission.put("caseResults", mapper.selectSubmissionCases(number(submission.get("submission_id"))));
        }
        result.put("history", history);
        return result;
    }

    public Map<String, Object> saveDraft(Long userId, String sourceType, Long sourceId, Long questionId, String sourceCode) {
        BizStudent student = requireStudent(userId); assertStudentScope(student, student.getDeptId(), sourceType, sourceId, questionId); Map<String, Object> question = mapper.selectQuestion(sourceType, sourceId, questionId);
        if (question == null) throw new ServiceException("题目不存在或未开放");
        Map<String, Object> row = new HashMap<String, Object>(); row.put("studentId", student.getStudentId()); row.put("sourceType", sourceType); row.put("sourceId", sourceId); row.put("questionId", questionId); row.put("snapshotId", question.get("snapshot_id")); row.put("sourceCode", sourceCode == null ? "" : sourceCode); mapper.upsertDraft(row); return row;
    }

    public Map<String, Object> submit(Long userId, String sourceType, Long sourceId, Long questionId, String sourceCode, String submitType, String customInput, PythonPracticeSubmissionWorker submissionWorker) {
        BizStudent student = requireStudent(userId); assertStudentScope(student, student.getDeptId(), sourceType, sourceId, questionId); Map<String, Object> question = mapper.selectQuestion(sourceType, sourceId, questionId);
        if (question == null) throw new ServiceException("题目不存在或未开放");
        String normalizedType = submitType == null ? "SUBMIT" : submitType.trim().toUpperCase();
        if (!"RUN".equals(normalizedType) && !"SUBMIT".equals(normalizedType) && !"CUSTOM_RUN".equals(normalizedType)) throw new ServiceException("不支持的运行方式");
        String code = sourceCode == null ? "" : sourceCode;
        if (code.trim().isEmpty()) throw new ServiceException("请先输入 Python 代码");
        if (code.length() > 131072) throw new ServiceException("代码长度不能超过 128KB");
        String input = customInput == null ? "" : customInput;
        if (input.length() > 65536) throw new ServiceException("自定义输入不能超过 64KB");
        Map<String, Object> row = new HashMap<String, Object>(); row.put("submissionKey", UUID.randomUUID().toString().replace("-", "")); row.put("studentId", student.getStudentId()); row.put("sourceType", sourceType); row.put("sourceId", sourceId); row.put("questionId", questionId); row.put("snapshotId", question.get("snapshot_id")); row.put("sourceCode", code); row.put("customInput", "CUSTOM_RUN".equals(normalizedType) ? input : null); row.put("submitType", normalizedType); mapper.insertSubmission(row); row.put("statusCode", "WAITING"); row.put("statusMessage", "已进入独立刷题判题队列");
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
            boolean customRun = "CUSTOM_RUN".equals(String.valueOf(submission.get("submit_type")));
            List<Map<String, Object>> cases;
            if (customRun) {
                Map<String, Object> customCase = new HashMap<String, Object>();
                customCase.put("case_name", "自定义输入"); customCase.put("input_text", submission.get("custom_input")); customCase.put("is_public", "1"); customCase.put("order_num", 1);
                customCase.put("time_limit_seconds", submission.get("time_limit_seconds")); customCase.put("memory_limit_kb", submission.get("memory_limit_kb"));
                cases = Collections.singletonList(customCase);
            } else {
                cases = mapper.selectSnapshotCases(number(submission.get("snapshot_id")), "RUN".equals(submission.get("submit_type")));
            }
            if (cases.isEmpty()) throw new ServiceException("题目尚未配置测试点");
            int passed = 0; String firstFailure = "";
            int order = 1;
            for (Map<String, Object> item : cases) {
                String expectedOutput = customRun ? null : stringOrNull(item.get("expected_output"));
                Judge0Request request = new Judge0Request(); request.setSourceCode(String.valueOf(submission.get("source_code"))); request.setStdin(stringOrNull(item.get("input_text"))); request.setExpectedOutput(expectedOutput);
                request.setCpuTimeLimit(decimal(item.get("time_limit_seconds"), 2D)); request.setMemoryLimitKb(intValue(item.get("memory_limit_kb"), 131072));
                Judge0Result result = client().submit(request);
                if (result == null || result.getToken() == null) throw new ServiceException("Judge0 未返回提交令牌");
                for (int poll = 0; poll < Math.max(1, judgeProperties.getMaxPolls()); poll++) { Thread.sleep(Math.max(100, judgeProperties.getPollIntervalMs())); result = client().poll(result.getToken()); if (result != null && result.isFinished()) break; }
                String status = customRun ? Judge0StatusMapper.toPlatformStatus(result) : caseStatus(result, expectedOutput);
                if ("ACCEPTED".equals(status)) passed++; else if (firstFailure.isEmpty()) firstFailure = status;
                boolean publicCase = customRun || "1".equals(String.valueOf(item.get("is_public")));
                Map<String, Object> caseRow = new HashMap<String, Object>();
                caseRow.put("submissionId", submissionId); caseRow.put("snapshotCaseId", number(item.get("snapshot_case_id"))); caseRow.put("caseName", stringOrEmpty(item.get("case_name"))); caseRow.put("isPublic", publicCase ? "1" : "0"); caseRow.put("statusCode", status);
                caseRow.put("judge0StatusId", result == null ? null : result.getStatusId()); caseRow.put("timeSeconds", result == null ? null : result.getTimeSeconds()); caseRow.put("memoryKb", result == null ? null : result.getMemoryKb()); caseRow.put("outputText", publicCase && result != null ? result.getStdout() : null); caseRow.put("errorSummary", publicCase ? judgeError(result) : statusMessage(status)); caseRow.put("orderNum", order++);
                mapper.insertSubmissionCase(caseRow);
            }
            String status = passed == cases.size() ? "ACCEPTED" : (passed > 0 ? "PARTIAL" : (firstFailure.isEmpty() ? "SERVICE_ERROR" : firstFailure));
            Map<String, Object> result = new HashMap<String, Object>(); result.put("submissionId", submissionId); result.put("statusCode", status); result.put("statusMessage", customRun && "ACCEPTED".equals(status) ? "运行完成" : statusMessage(status)); result.put("score", customRun ? null : Math.round(100F * passed / cases.size())); result.put("passedCaseCount", passed); result.put("totalCaseCount", cases.size()); result.put("judgeSummary", customRun ? "自定义输入运行" : passed + "/" + cases.size()); mapper.updateSubmissionResult(result);
            if ("SUBMIT".equalsIgnoreCase(String.valueOf(submission.get("submit_type")))) {
                Map<String, Object> progress = new HashMap<String, Object>();
                progress.put("studentId", submission.get("student_id")); progress.put("sourceType", submission.get("source_type")); progress.put("sourceId", submission.get("source_id")); progress.put("questionId", submission.get("question_id"));
                progress.put("bestScore", Math.round(100F * passed / cases.size())); progress.put("passedFlag", passed == cases.size() ? "1" : "0");
                progress.put("firstPassTime", passed == cases.size() ? new Date() : null); mapper.upsertProgress(progress);
            }
        } catch (Exception ex) { Map<String, Object> result = new HashMap<String, Object>(); result.put("submissionId", submissionId); result.put("statusCode", "SERVICE_ERROR"); result.put("statusMessage", "判题服务异常，代码和提交已保留"); result.put("score", null); result.put("passedCaseCount", 0); result.put("totalCaseCount", 0); result.put("judgeSummary", "判题服务异常"); mapper.updateSubmissionResult(result); }
    }

    private Judge0Client client() { if ("mock".equalsIgnoreCase(judgeProperties.getMode())) return mockClient; if ("http".equalsIgnoreCase(judgeProperties.getMode())) return httpClient; throw new ServiceException("Judge0 判题服务未启用"); }
    private static String caseStatus(Judge0Result result, String expectedOutput) {
        String status = Judge0StatusMapper.toPlatformStatus(result);
        if (("ACCEPTED".equals(status) || "WRONG_ANSWER".equals(status)) && result != null) {
            return OutputComparator.matches(expectedOutput, result.getStdout()) ? "ACCEPTED" : "WRONG_ANSWER";
        }
        return status;
    }
    private static int intValue(Object value, int fallback) { return value == null ? fallback : Integer.valueOf(String.valueOf(value)); }
    private static double decimal(Object value, double fallback) { return value == null ? fallback : Double.valueOf(String.valueOf(value)); }
    private static String stringOrNull(Object value) { return value == null ? null : String.valueOf(value); }
    private static String stringOrEmpty(Object value) { return value == null ? "" : String.valueOf(value); }
    private static String judgeError(Judge0Result result) {
        if (result == null) return "判题服务未返回结果";
        String detail = result.getCompileOutput();
        if (detail == null || detail.trim().isEmpty()) detail = result.getStderr();
        if (detail == null || detail.trim().isEmpty()) detail = result.getMessage();
        if (detail == null || detail.trim().isEmpty()) return null;
        return detail.length() > 1000 ? detail.substring(0, 1000) : detail;
    }
    private String statusMessage(String status) { if ("ACCEPTED".equals(status)) return "通过"; if ("PARTIAL".equals(status)) return "部分通过"; if ("WRONG_ANSWER".equals(status)) return "答案错误"; if ("SYNTAX_ERROR".equals(status)) return "语法错误"; if ("RUNTIME_ERROR".equals(status)) return "运行时错误"; if ("TIME_LIMIT".equals(status)) return "运行超时"; if ("MEMORY_LIMIT".equals(status)) return "内存超限"; if ("SERVICE_BUSY".equals(status)) return "判题队列繁忙"; return "判题服务异常，代码和提交已保留"; }

    private BizStudent requireStudent(Long userId) { BizStudent student = studentMapper.selectBizStudentByUserId(userId); if (student == null) throw forbidden("当前账号不是学生"); return student; }
    private void assertStudentScope(BizStudent student, Long deptId, String sourceType, Long sourceId, Long questionId) {
        Long actualDept = deptId == null ? student.getDeptId() : deptId;
        boolean allowed = false;
        for (Map<String, Object> item : mapper.selectStudentQuestions(actualDept, student.getEntryYear(), student.getClassCode())) {
            if (sourceType.equals(item.get("source_type")) && sourceId != null && sourceId.equals(number(item.get("source_id"))) && questionId != null && questionId.equals(number(item.get("question_id")))) { allowed = true; break; }
        }
        if (!allowed) throw forbidden("题目不属于当前学生可练习范围");
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
        if (plan == null) throw new ServiceException("题单不存在");
        if (deptId != null && !deptId.equals(number(plan.get("dept_id")))) throw forbidden("无权访问该题单");
    }
    private void replacePlanClasses(Long planVersionId, List<Map<String, String>> classes, Long userId,
                                    Long deptId, String username) {
        mapper.deletePlanClasses(planVersionId);
        for (Map<String, String> item : classes) {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("planVersionId", planVersionId); row.put("deptId", deptId);
            row.put("entryYear", item.get("entryYear")); row.put("classCode", item.get("classCode"));
            row.put("createBy", username); mapper.insertPlanClass(row);
        }
    }

    private void validateManagedClasses(List<Map<String, String>> classes, Long userId, Long deptId) {
        for (Map<String, String> item : classes) {
            ensureManagedClass(userId, deptId, item.get("entryYear"), item.get("classCode"));
        }
    }

    private static List<Map<String, String>> parseClasses(Object value) {
        if (!(value instanceof List)) return Collections.emptyList();
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        Set<String> seen = new HashSet<String>();
        for (Object raw : (List<?>) value) {
            String entryYear = null, classCode = null;
            if (raw instanceof Map) {
                Map<?, ?> item = (Map<?, ?>) raw;
                Object yearValue = item.containsKey("entryYear") ? item.get("entryYear") : item.get("entry_year");
                Object classValue = item.containsKey("classCode") ? item.get("classCode") : item.get("class_code");
                entryYear = yearValue == null ? null : String.valueOf(yearValue).trim();
                classCode = classValue == null ? null : String.valueOf(classValue).trim();
            } else if (raw != null) {
                String text = String.valueOf(raw).trim();
                int split = text.indexOf("::");
                if (split > 0) { entryYear = text.substring(0, split).trim(); classCode = text.substring(split + 2).trim(); }
            }
            if (entryYear == null || entryYear.isEmpty() || classCode == null || classCode.isEmpty()) continue;
            String key = entryYear + "\n" + classCode;
            if (!seen.add(key)) continue;
            Map<String, String> item = new HashMap<String, String>();
            item.put("entryYear", entryYear); item.put("classCode", classCode); result.add(item);
        }
        return result;
    }
    private void ensureManagedClass(Long userId, Long deptId, String entryYear, String classCode) {
        if (SecurityUtils.isAdmin(userId) || SecurityUtils.hasRole("researcher")) return;
        BizTeacherClass query = new BizTeacherClass(); query.setUserId(userId); query.setDeptId(deptId); query.setEntryYear(entryYear); query.setClassCode(classCode);
        if (teacherClassMapper.checkTeacherClassExists(query) <= 0) throw forbidden("当前教师无权管理该班级");
    }

    /** 只向教师展示当前仍在校的教学班，毕业届和未来入学年份不会混入选项。 */
    private List<Map<String, Object>> decorateCurrentClasses(List<Map<String, Object>> source, String schoolType) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        LocalDate today = LocalDate.now();
        if (source == null) return result;
        for (Map<String, Object> row : source) {
            String entryYear = stringOrNull(row.get("entry_year"));
            String classCode = stringOrNull(row.get("class_code"));
            try {
                int grade = AcademicYearUtils.resolveAbsoluteGrade(entryYear, schoolType, today);
                row.put("absolute_grade", grade);
                row.put("class_label", AcademicYearUtils.resolveClassLabel(entryYear, classCode, schoolType, today));
                result.add(row);
            }
            catch (IllegalArgumentException ignored) {
                // 教师当前选班只呈现在校班级；历史届仍保留在原始学生和成绩数据中。
            }
        }
        result.sort(Comparator
            .comparingInt((Map<String, Object> row) -> intValue(row.get("absolute_grade"), Integer.MAX_VALUE))
            .thenComparingInt(row -> intValue(row.get("class_code"), Integer.MAX_VALUE)));
        return result;
    }

    private String requireSchoolType(Long deptId) {
        SysDept dept = deptMapper.selectDeptById(deptId);
        if (dept == null || dept.getSchoolType() == null || dept.getSchoolType().trim().isEmpty()) {
            throw new ServiceException("当前学校未配置小学、初中或高中学段，暂时无法换算教学班名称");
        }
        return dept.getSchoolType().trim();
    }

    private static String joinClassLabels(List<Map<String, Object>> classes) {
        StringBuilder result = new StringBuilder();
        for (Map<String, Object> item : classes) {
            if (result.length() > 0) result.append('、');
            result.append(String.valueOf(item.get("class_label")));
        }
        return result.toString();
    }

    private static ServiceException forbidden(String message) {
        return new ServiceException(message, HttpStatus.FORBIDDEN);
    }

    private static Long numberOrNull(Object value) {
        if (value instanceof Number) return ((Number) value).longValue();
        if (value == null || String.valueOf(value).trim().isEmpty()) return null;
        try { return Long.valueOf(String.valueOf(value).trim()); }
        catch (NumberFormatException ex) { return null; }
    }
}
