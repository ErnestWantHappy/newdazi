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
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.ClassGroupingMapper;
import com.ruoyi.common.exception.ServiceException;

/** 通用班级分组服务。分组关系与座位布局分开保存，避免拖座位改变协作路由。 */
@Service
public class ClassGroupingService {
    @Autowired private ClassGroupingMapper mapper;
    @Autowired private BizLessonMapper lessonMapper;
    @Autowired private StudentPresenceService presenceService;

    private void requireClass(Long userId, Long deptId, String entryYear, String classCode) {
        if (deptId == null || entryYear == null || classCode == null ||
            mapper.countManagedClass(userId, deptId, entryYear, classCode) == 0) {
            throw new ServiceException("您没有该班级的管理权限");
        }
    }

    public Map<String, Object> listSchemes(Long userId, Long deptId, String entryYear, String classCode) {
        requireClass(userId, deptId, entryYear, classCode);
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
        requireClass(userId, deptId, entryYear, classCode);
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
        scheme.put("schemeName", name.trim()); scheme.put("schemeVersion", 1); scheme.put("creatorUserId", userId);
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

    /** 按组数平均或按学号连续区间生成一份可继续编辑的方案。 */
    public Map<String,Object> generateScheme(Long userId, Long deptId, String entryYear, String classCode, Map<String,Object> request) {
        int groupCount = intValue(request == null ? null : request.get("groupCount"), 0);
        if (groupCount < 1) throw new ServiceException("分组数量至少为1");
        List<Map<String,Object>> students = mapper.selectClassStudents(deptId, entryYear, classCode);
        if (groupCount > students.size()) throw new ServiceException("分组数量不能大于学生人数");
        String mode = stringValue(request.get("mode"));
        List<Map<String,Object>> groups = new ArrayList<>();
        for (int i = 0; i < groupCount; i++) { Map<String,Object> group = new LinkedHashMap<>(); group.put("groupName", "第" + (i + 1) + "组"); group.put("studentIds", new ArrayList<Long>()); groups.add(group); }
        for (int index = 0; index < students.size(); index++) {
            int target = "RANGE".equalsIgnoreCase(mode) ? (index * groupCount / students.size()) : (index % groupCount);
            Object ids = groups.get(target).get("studentIds"); if (ids instanceof List) ((List<Object>) ids).add(longValue(students.get(index).get("studentId")));
        }
        Map<String,Object> payload = new LinkedHashMap<>(); payload.put("schemeName", request.get("schemeName")); payload.put("groups", groups);
        return saveScheme(userId, deptId, entryYear, classCode, payload);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> generateSnapshot(Long userId, Long lessonId, String entryYear, String classCode, Long schemeId) {
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        if (lesson == null || !lesson.getDeptId().equals(com.ruoyi.common.utils.SecurityUtils.getDeptId())) throw new ServiceException("课程不存在或不属于当前学校");
        requireClass(userId, lesson.getDeptId(), entryYear, classCode);
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
        requireClass(userId, deptId, entryYear, classCode); Map<String,Object> layout = mapper.selectLayout(userId, deptId, entryYear, classCode); Long layoutId = layout == null ? null : longValue(layout.get("layoutId"));
        List<Map<String,Object>> students = mapper.selectDesktopStudents(deptId, entryYear, classCode, layoutId);
        List<Long> ids = new ArrayList<>(); for (Map<String,Object> student : students) ids.add(longValue(student.get("studentId")));
        Map<Long,Map<String,Object>> presences = presenceService.summary(ids);
        for (Map<String,Object> student : students) { Map<String,Object> presence = presences.get(longValue(student.get("studentId"))); if (presence != null) student.putAll(presence); }
        Map<String,Object> result = new LinkedHashMap<>(); result.put("layout", layout); result.put("students", students); return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> saveLayout(Long userId, Long deptId, String entryYear, String classCode, Map<String,Object> request) {
        requireClass(userId, deptId, entryYear, classCode); Map<String,Object> layout = mapper.selectLayout(userId, deptId, entryYear, classCode); Map<String,Object> row = new HashMap<>();
        int columns = intValue(request == null ? null : request.get("columnsCount"), 6); if (columns < 1 || columns > 24) throw new ServiceException("列数必须在1到24之间");
        if (layout == null) { row.put("teacherUserId", userId); row.put("deptId", deptId); row.put("entryYear", entryYear); row.put("classCode", classCode); row.put("columnsCount", columns); mapper.insertLayout(row); layout = mapper.selectLayout(userId, deptId, entryYear, classCode); } else { row.put("layoutId", layout.get("layoutId")); row.put("columnsCount", columns); mapper.updateLayout(row); }
        Long layoutId = longValue(layout.get("layoutId")); mapper.deleteLayoutItems(layoutId); List<Map<String,Object>> items = mapList(request == null ? null : request.get("items")); int sort = 0; Set<Long> seen = new HashSet<>();
        for (Map<String,Object> item : items) { Long studentId = longValue(item.get("studentId")); if (studentId == null || !seen.add(studentId)) continue; Map<String,Object> x = new HashMap<>(); x.put("layoutId", layoutId); x.put("studentId", studentId); x.put("gridRow", intValue(item.get("gridRow"), 0)); x.put("gridCol", intValue(item.get("gridCol"), 0)); x.put("sortNo", sort++); mapper.insertLayoutItem(x); }
        return desktop(userId, deptId, entryYear, classCode);
    }

    private static List<Map<String,Object>> mapList(Object value) { List<Map<String,Object>> out = new ArrayList<>(); if (value instanceof List) for (Object v : (List<?>) value) if (v instanceof Map) out.add((Map<String,Object>) v); return out; }
    private static List<Object> objectList(Object value) { return value instanceof List ? new ArrayList<>((List<?>) value) : Collections.emptyList(); }
    private static Long longValue(Object value) { if (value == null) return null; return value instanceof Number ? ((Number)value).longValue() : Long.valueOf(String.valueOf(value)); }
    private static int intValue(Object value, int fallback) { try { return value == null ? fallback : Integer.parseInt(String.valueOf(value)); } catch (Exception e) { return fallback; } }
    private static String stringValue(Object value) { return value == null ? null : String.valueOf(value); }
    private static String sha256(String text) { try { byte[] hash = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)); StringBuilder b = new StringBuilder(); for (byte x : hash) b.append(String.format("%02x", x)); return b.toString(); } catch (Exception e) { throw new IllegalStateException(e); } }
}
