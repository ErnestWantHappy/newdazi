package com.ruoyi.business.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizClassroomPerformance;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.ClassroomStudentTaskSummaryVo;
import com.ruoyi.business.mapper.BizClassroomPerformanceMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.ClassGroupingMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/** 通用班级分组服务。分组关系与座位布局分开保存，避免拖座位改变协作路由。 */
@Service
public class ClassGroupingService {
    @Autowired private ClassGroupingMapper mapper;
    @Autowired private BizLessonMapper lessonMapper;
    @Autowired private BizLessonQuestionMapper lessonQuestionMapper;
    @Autowired private BizClassroomPerformanceMapper performanceMapper;
    @Autowired private ClassroomTaskStateService taskStateService;
    @Autowired private StudentPresenceService presenceService;

    private void requireClass(Long userId, Long deptId, String entryYear, String classCode) {
        if (deptId == null || entryYear == null || classCode == null) {
            throw new ServiceException("您没有该班级的管理权限");
        }
        if (!SecurityUtils.isAdmin(userId) && mapper.countManagedClass(userId, deptId, entryYear, classCode) == 0)
            throw new ServiceException("您没有该班级的管理权限");
    }

    /**
     * 班级事实以任课关系为准，不能把教师当前主部门误当成其实际负责班级的学校。
     * 同一教师在多个学校拥有同一年级同一班号时，不猜测目标学校，避免跨校读取。
     */
    private Long resolveManagedClassDept(Long userId, Long currentDeptId, String entryYear, String classCode) {
        if (entryYear == null || entryYear.trim().isEmpty() || classCode == null || classCode.trim().isEmpty()) {
            throw new ServiceException("入学年份和班级编号不能为空");
        }
        if (SecurityUtils.isAdmin(userId)) {
            if (currentDeptId == null) throw new ServiceException("当前学校不能为空");
            return currentDeptId;
        }
        List<Long> deptIds = mapper.selectManagedClassDeptIds(userId, entryYear, classCode);
        if (deptIds == null || deptIds.isEmpty()) throw new ServiceException("您没有该班级的管理权限");
        if (deptIds.size() > 1) throw new ServiceException("该年级班号在多个学校存在，请从具体课程或班级入口进入");
        return deptIds.get(0);
    }

    public Map<String, Object> listSchemes(Long userId, Long deptId, String entryYear, String classCode) {
        deptId = resolveManagedClassDept(userId, deptId, entryYear, classCode);
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String,Object>> schemes = mapper.selectSchemes(deptId, entryYear, classCode);
        for (Map<String,Object> scheme : schemes) {
            Long id = longValue(scheme.get("schemeId"));
            scheme.put("groups", mapper.selectGroups(id));
            scheme.put("members", mapper.selectMembers(id));
        }
        result.put("schemes", schemes);
        result.put("students", mapper.selectClassStudents(deptId, entryYear, classCode));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> saveScheme(Long userId, Long deptId, String entryYear, String classCode, Map<String,Object> request) {
        deptId = resolveManagedClassDept(userId, deptId, entryYear, classCode);
        String name = stringValue(request == null ? null : request.get("schemeName"));
        if (name == null || name.trim().isEmpty()) throw new ServiceException("分组方案名称不能为空");
        List<Map<String,Object>> groups = mapList(request.get("groups"));
        if (groups.isEmpty()) throw new ServiceException("至少需要一个分组");
        List<Map<String,Object>> students = mapper.selectClassStudents(deptId, entryYear, classCode);
        Set<Long> valid = new HashSet<>();
        for (Map<String,Object> s : students) valid.add(longValue(s.get("studentId")));
        Set<Long> seen = new HashSet<>();
        Map<String,Object> scheme = new HashMap<>();
        scheme.put("deptId", deptId); scheme.put("entryYear", entryYear); scheme.put("classCode", classCode);
        scheme.put("schemeName", name.trim());
        scheme.put("schemeVersion", mapper.selectNextSchemeVersion(deptId, entryYear, classCode, name.trim()));
        scheme.put("creatorUserId", userId);
        mapper.insertScheme(scheme);
        Long schemeId = longValue(scheme.get("schemeId"));
        int groupNo = 1;
        for (Map<String,Object> group : groups) {
            List<Object> ids = objectList(group.get("studentIds"));
            if (ids.isEmpty()) throw new ServiceException("分组不能为空");
            Long leader = null;
            List<Long> normalized = new ArrayList<>();
            for (Object idValue : ids) {
                Long id = longValue(idValue);
                if (id == null || !valid.contains(id) || !seen.add(id)) throw new ServiceException("学生分组必须不重不漏");
                normalized.add(id);
                if (leader == null || id < leader) leader = id;
            }
            Long requestedLeader = longValue(group.get("leaderStudentId"));
            if (requestedLeader != null && normalized.contains(requestedLeader)) leader = requestedLeader;
            Map<String,Object> row = new HashMap<>();
            row.put("schemeId", schemeId); row.put("groupNo", groupNo);
            row.put("groupName", stringValue(group.get("groupName")) == null ? "第" + groupNo + "组" : group.get("groupName"));
            row.put("color", group.get("color")); row.put("sortNo", groupNo); row.put("leaderStudentId", leader);
            mapper.insertGroup(row);
            Long groupId = longValue(row.get("groupId"));
            int sort = 0;
            for (Long studentId : normalized) { Map<String,Object> member = new HashMap<>(); member.put("schemeId", schemeId); member.put("groupId", groupId); member.put("studentId", studentId); member.put("sortNo", sort++); mapper.insertMember(member); }
            groupNo++;
        }
        if (seen.size() != valid.size()) throw new ServiceException("学生分组必须覆盖当前班级全部学生");
        return detail(schemeId);
    }

    public Map<String,Object> detail(Long schemeId) {
        Map<String,Object> result = mapper.selectScheme(schemeId);
        if (result == null) throw new ServiceException("分组方案不存在");
        result.put("groups", mapper.selectGroups(schemeId)); result.put("members", mapper.selectMembers(schemeId));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteScheme(Long userId, Long schemeId) {
        Map<String,Object> scheme = mapper.selectScheme(schemeId);
        if (scheme == null) throw new ServiceException("分组方案不存在");
        requireClass(userId, longValue(scheme.get("deptId")), stringValue(scheme.get("entryYear")), stringValue(scheme.get("classCode")));
        mapper.deleteMembers(schemeId); mapper.deleteGroups(schemeId); mapper.deleteScheme(schemeId);
    }

    /**
     * 按每组人数生成一份可继续编辑的方案。保留 groupCount 是为了让已发布前端仍能继续使用。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> generateScheme(Long userId, Long deptId, String entryYear, String classCode, Map<String,Object> request) {
        deptId = resolveManagedClassDept(userId, deptId, entryYear, classCode);
        if (request == null) throw new ServiceException("分组参数不能为空");
        List<Map<String,Object>> students = mapper.selectClassStudents(deptId, entryYear, classCode);
        if (students.isEmpty()) throw new ServiceException("当前班级没有可分组的学生");
        int membersPerGroup = intValue(request == null ? null : request.get("membersPerGroup"), 0);
        boolean legacyGroupCount = membersPerGroup < 1;
        if (legacyGroupCount) membersPerGroup = intValue(request == null ? null : request.get("groupCount"), 0);
        if (membersPerGroup < 1) throw new ServiceException(legacyGroupCount ? "分组数量至少为1" : "每组人数至少为1");
        int groupCount = legacyGroupCount ? membersPerGroup : (students.size() + membersPerGroup - 1) / membersPerGroup;
        if (legacyGroupCount && groupCount > students.size()) throw new ServiceException("分组数量不能大于学生人数");
        String mode = stringValue(request.get("mode"));
        List<Map<String,Object>> groups = new ArrayList<>();
        for (int i = 0; i < groupCount; i++) { Map<String,Object> group = new LinkedHashMap<>(); group.put("groupName", "第" + (i + 1) + "组"); group.put("studentIds", new ArrayList<Long>()); groups.add(group); }
        for (int index = 0; index < students.size(); index++) {
            int target = "RANGE".equalsIgnoreCase(mode)
                    ? (legacyGroupCount ? (index * groupCount / students.size()) : index / membersPerGroup)
                    : (index % groupCount);
            @SuppressWarnings("unchecked")
            List<Long> studentIds = (List<Long>) groups.get(target).get("studentIds");
            studentIds.add(longValue(students.get(index).get("studentId")));
        }
        Map<String,Object> payload = new LinkedHashMap<>(); payload.put("schemeName", request.get("schemeName")); payload.put("groups", groups);
        return saveScheme(userId, deptId, entryYear, classCode, payload);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> generateSnapshot(Long userId, Long lessonId, String entryYear, String classCode, Long schemeId) {
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        if (lesson == null || !lesson.getDeptId().equals(com.ruoyi.common.utils.SecurityUtils.getDeptId())) throw new ServiceException("课程不存在或不属于当前学校");
        requireLessonOwner(userId, lesson);
        requireClass(userId, lesson.getDeptId(), entryYear, classCode);
        if (mapper.countLessonAssignment(lessonId, lesson.getDeptId(), entryYear, classCode) == 0)
            throw new ServiceException("课程未指派给该班级，不能冻结分组快照");
        Map<String,Object> existing = mapper.selectSnapshot(lessonId, lesson.getDeptId(), entryYear, classCode);
        if (existing != null) return existing;
        Map<String,Object> source = mapper.selectScheme(schemeId);
        if (source == null || !lesson.getDeptId().equals(longValue(source.get("deptId"))) || !entryYear.equals(source.get("entryYear")) || !classCode.equals(source.get("classCode"))) throw new ServiceException("分组方案与班级不匹配");
        List<Map<String,Object>> groups = mapper.selectGroups(schemeId); List<Map<String,Object>> members = mapper.selectMembers(schemeId);
        StringBuilder canonical = new StringBuilder(); for (Map<String,Object> m : members) canonical.append(m.get("groupId")).append(':').append(m.get("studentId")).append(';');
        Map<String,Object> snapshot = new HashMap<>(); snapshot.put("lessonId", lessonId); snapshot.put("deptId", lesson.getDeptId()); snapshot.put("entryYear", entryYear); snapshot.put("classCode", classCode); snapshot.put("sourceSchemeId", schemeId); snapshot.put("sourceSchemeVersion", source.get("schemeVersion")); snapshot.put("snapshotHash", sha256(canonical.toString())); mapper.insertSnapshot(snapshot);
        Long snapshotId = longValue(snapshot.get("snapshotId"));
        Map<Long,Long> groupIds = new HashMap<>();
        for (Map<String,Object> g : groups) { Map<String,Object> row = new HashMap<>(); row.put("snapshotId", snapshotId); row.put("groupNo", g.get("groupNo")); row.put("groupName", g.get("groupName")); row.put("color", g.get("color")); row.put("sortNo", g.get("sortNo")); row.put("leaderStudentId", g.get("leaderStudentId")); mapper.insertSnapshotGroup(row); groupIds.put(longValue(g.get("groupId")), longValue(row.get("snapshotGroupId"))); }
        for (Map<String,Object> m : members) { Map<String,Object> row = new HashMap<>(); row.put("snapshotId", snapshotId); row.put("snapshotGroupId", groupIds.get(longValue(m.get("groupId")))); row.put("studentId", m.get("studentId")); row.put("sortNo", m.get("sortNo")); mapper.insertSnapshotMember(row); }
        return mapper.selectSnapshot(lessonId, lesson.getDeptId(), entryYear, classCode);
    }

    public Map<String,Object> desktop(Long userId, Long deptId, String entryYear, String classCode) {
        deptId = resolveManagedClassDept(userId, deptId, entryYear, classCode);
        return desktopByResolvedDept(userId, deptId, entryYear, classCode);
    }

    /**
     * 课堂大屏的单次读取入口。成绩、请假和任务状态都以班级学生为主表，未作答学生不会丢失。
     */
    public Map<String,Object> desktopOverview(Long userId, Long deptId, Long lessonId, String entryYear, String classCode) {
        if (lessonId == null) throw new ServiceException("课程参数不能为空");
        deptId = resolveManagedClassDept(userId, deptId, entryYear, classCode);
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        if (lesson == null || lesson.getDeptId() == null || !deptId.equals(lesson.getDeptId())) {
            throw new ServiceException("课程不存在或不属于当前学校");
        }
        if (mapper.countLessonAssignment(lessonId, deptId, entryYear, classCode) == 0) {
            throw new ServiceException("课程未指派给当前班级");
        }

        Map<String,Object> result = desktopByResolvedDept(userId, deptId, entryYear, classCode);
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> students = (List<Map<String,Object>>) result.get("students");
        List<Long> studentIds = new ArrayList<>();
        for (Map<String,Object> student : students) studentIds.add(longValue(student.get("studentId")));

        List<BizLessonQuestionDetailVo> questions = lessonQuestionMapper.selectDetailsByLessonId(lessonId);
        Set<Long> typingQuestionIds = new HashSet<>();
        Set<Long> theoryQuestionIds = new HashSet<>();
        Set<Long> practicalQuestionIds = new HashSet<>();
        for (BizLessonQuestionDetailVo question : questions) {
            if (question == null || question.getQuestionId() == null) continue;
            if ("typing".equals(question.getQuestionType())) typingQuestionIds.add(question.getQuestionId());
            else if ("choice".equals(question.getQuestionType()) || "judgment".equals(question.getQuestionType())) theoryQuestionIds.add(question.getQuestionId());
            else if ("practical".equals(question.getQuestionType())) practicalQuestionIds.add(question.getQuestionId());
        }
        result.put("hasTyping", !typingQuestionIds.isEmpty());
        result.put("hasTheory", !theoryQuestionIds.isEmpty());
        result.put("hasPractical", !practicalQuestionIds.isEmpty());

        Map<Long,Map<String,Object>> answerByStudent = studentIds.isEmpty()
                ? Collections.emptyMap()
                : indexByStudent(mapper.selectDesktopAnswerOverview(lessonId, studentIds));
        Map<Long,BizClassroomPerformance> performanceByStudent = new HashMap<>();
        if (!studentIds.isEmpty()) {
            for (BizClassroomPerformance performance : performanceMapper.selectByStudentIdsAndLessons(studentIds, Collections.singletonList(lessonId), deptId)) {
                performanceByStudent.put(performance.getStudentId(), performance);
            }
        }
        Map<Long,ClassroomStudentTaskSummaryVo> taskByStudent = new HashMap<>();
        for (ClassroomStudentTaskSummaryVo task : taskStateService.listClassSummary(deptId, lessonId, entryYear, classCode)) {
            taskByStudent.put(task.getStudentId(), task);
        }
        for (Map<String,Object> student : students) {
            Long studentId = longValue(student.get("studentId"));
            Map<String,Object> answer = answerByStudent.get(studentId);
            student.put("typing", typingData(answer));
            student.put("theory", theoryData(answer, theoryQuestionIds.size()));
            student.put("practical", practicalData(answer, practicalQuestionIds.size()));
            BizClassroomPerformance performance = performanceByStudent.get(studentId);
            student.put("performance", performanceData(performance));
            ClassroomStudentTaskSummaryVo task = taskByStudent.get(studentId);
            if (task != null) applyTask(student, task);
        }
        return result;
    }

    private Map<String,Object> desktopByResolvedDept(Long userId, Long deptId, String entryYear, String classCode) {
        Map<String,Object> layout = mapper.selectLayout(userId, deptId, entryYear, classCode); Long layoutId = layout == null ? null : longValue(layout.get("layoutId"));
        List<Map<String,Object>> students = mapper.selectDesktopStudents(deptId, entryYear, classCode, layoutId);
        List<Long> ids = new ArrayList<>(); for (Map<String,Object> student : students) ids.add(longValue(student.get("studentId")));
        Map<Long,Map<String,Object>> presences = presenceService.summary(ids);
        for (Map<String,Object> student : students) { Map<String,Object> presence = presences.get(longValue(student.get("studentId"))); if (presence != null) student.putAll(presence); }
        Map<String,Object> result = new LinkedHashMap<>(); result.put("layout", layout); result.put("students", students); return result;
    }

    private Map<Long,Map<String,Object>> indexByStudent(List<Map<String,Object>> rows) {
        Map<Long,Map<String,Object>> result = new HashMap<>();
        if (rows != null) for (Map<String,Object> row : rows) result.put(longValue(row.get("studentId")), row);
        return result;
    }

    private Map<String,Object> typingData(Map<String,Object> answer) {
        Map<String,Object> result = new LinkedHashMap<>();
        boolean hasData = number(answer, "typingAnsweredCount") > 0;
        result.put("hasData", hasData);
        result.put("score", hasData ? number(answer, "typingScore") : null);
        result.put("speed", hasData ? numberOrNull(answer, "avgTypingSpeed") : null);
        result.put("accuracy", hasData ? numberOrNull(answer, "avgTypingAccuracy") : null);
        return result;
    }

    private Map<String,Object> theoryData(Map<String,Object> answer, int totalQuestionCount) {
        Map<String,Object> result = new LinkedHashMap<>();
        int answered = number(answer, "theoryAnsweredCount");
        int correct = number(answer, "theoryCorrectCount");
        result.put("hasData", answered > 0);
        result.put("score", answered > 0 ? number(answer, "theoryScore") : null);
        result.put("answeredCount", answered);
        result.put("correctCount", correct);
        result.put("totalQuestionCount", totalQuestionCount);
        result.put("accuracy", totalQuestionCount == 0 ? null : Math.round(correct * 1000.0 / totalQuestionCount) / 10.0);
        return result;
    }

    private Map<String,Object> practicalData(Map<String,Object> answer, int totalQuestionCount) {
        Map<String,Object> result = new LinkedHashMap<>();
        int submitted = number(answer, "practicalSubmittedCount");
        int scored = number(answer, "practicalScoredCount");
        result.put("answeredCount", number(answer, "practicalAnsweredCount"));
        result.put("submittedCount", submitted);
        result.put("totalQuestionCount", totalQuestionCount);
        result.put("scoredCount", scored);
        result.put("submitted", totalQuestionCount > 0 && submitted == totalQuestionCount);
        // scored 是 score 非空的记录数，因此批改结果为 0 分也会正常显示。
        result.put("score", scored > 0 ? number(answer, "practicalScore") : null);
        return result;
    }

    private Map<String,Object> performanceData(BizClassroomPerformance performance) {
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("score", performance == null || performance.getScore() == null ? 0 : performance.getScore());
        result.put("reason", performance == null ? null : performance.getReason());
        result.put("isAbsent", performance != null && Integer.valueOf(1).equals(performance.getIsAbsent()));
        return result;
    }

    private void applyTask(Map<String,Object> student, ClassroomStudentTaskSummaryVo task) {
        student.put("totalQuestionCount", task.getTotalQuestionCount());
        student.put("startedQuestionCount", task.getStartedQuestionCount());
        student.put("taskState", task.getTaskState());
        student.put("changedAt", task.getChangedAt());
    }

    private int number(Map<String,Object> values, String key) {
        Object value = values == null ? null : values.get(key);
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private Number numberOrNull(Map<String,Object> values, String key) {
        Object value = values == null ? null : values.get(key);
        return value instanceof Number ? (Number) value : null;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> saveLayout(Long userId, Long deptId, String entryYear, String classCode, Map<String,Object> request) {
        deptId = resolveManagedClassDept(userId, deptId, entryYear, classCode);
        int columns = intValue(request == null ? null : request.get("columnsCount"), 6); if (columns < 1 || columns > 24) throw new ServiceException("列数必须在1到24之间");
        List<Map<String,Object>> items = mapList(request == null ? null : request.get("items"));
        Set<Long> valid = new HashSet<>();
        for (Map<String,Object> student : mapper.selectClassStudents(deptId, entryYear, classCode)) valid.add(longValue(student.get("studentId")));
        Set<Long> seen = new HashSet<>();
        for (Map<String,Object> item : items) {
            Long studentId = longValue(item.get("studentId"));
            if (studentId == null || !valid.contains(studentId) || !seen.add(studentId))
                throw new ServiceException("座位布局只能包含当前班级且不能重复的学生");
        }
        if (seen.size() != valid.size()) throw new ServiceException("座位布局必须覆盖当前班级全部学生");
        Map<String,Object> layout = mapper.selectLayout(userId, deptId, entryYear, classCode); Map<String,Object> row = new HashMap<>();
        if (layout == null) { row.put("teacherUserId", userId); row.put("deptId", deptId); row.put("entryYear", entryYear); row.put("classCode", classCode); row.put("columnsCount", columns); mapper.insertLayout(row); layout = mapper.selectLayout(userId, deptId, entryYear, classCode); } else { row.put("layoutId", layout.get("layoutId")); row.put("columnsCount", columns); mapper.updateLayout(row); }
        Long layoutId = longValue(layout.get("layoutId")); mapper.deleteLayoutItems(layoutId); int sort = 0;
        for (Map<String,Object> item : items) { Long studentId = longValue(item.get("studentId")); Map<String,Object> x = new HashMap<>(); x.put("layoutId", layoutId); x.put("studentId", studentId); x.put("gridRow", intValue(item.get("gridRow"), 0)); x.put("gridCol", intValue(item.get("gridCol"), 0)); x.put("sortNo", sort++); mapper.insertLayoutItem(x); }
        return desktop(userId, deptId, entryYear, classCode);
    }

    private void requireLessonOwner(Long userId, BizLesson lesson) {
        boolean owner = userId != null && userId.equals(lesson.getCreatorId());
        String userName = SecurityUtils.getUsername();
        boolean legacyOwner = lesson.getCreatorId() == null && userName != null && userName.equals(lesson.getCreateBy());
        if (!SecurityUtils.isAdmin(userId) && !owner && !legacyOwner)
            throw new ServiceException("只有课程创建教师可以冻结分组快照");
    }

    private static List<Map<String,Object>> mapList(Object value) { List<Map<String,Object>> out = new ArrayList<>(); if (value instanceof List) for (Object v : (List<?>) value) if (v instanceof Map) { Map<String,Object> item = new LinkedHashMap<>(); for (Map.Entry<?, ?> entry : ((Map<?, ?>) v).entrySet()) if (entry.getKey() instanceof String) item.put((String) entry.getKey(), entry.getValue()); out.add(item); } return out; }
    private static List<Object> objectList(Object value) { return value instanceof List ? new ArrayList<>((List<?>) value) : Collections.emptyList(); }
    private static Long longValue(Object value) { if (value == null) return null; return value instanceof Number ? ((Number)value).longValue() : Long.valueOf(String.valueOf(value)); }
    private static int intValue(Object value, int fallback) { try { return value == null ? fallback : Integer.parseInt(String.valueOf(value)); } catch (Exception e) { return fallback; } }
    private static String stringValue(Object value) { return value == null ? null : String.valueOf(value); }
    private static String sha256(String text) { try { byte[] hash = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)); StringBuilder b = new StringBuilder(); for (byte x : hash) b.append(String.format("%02x", x)); return b.toString(); } catch (Exception e) { throw new IllegalStateException(e); } }
}
