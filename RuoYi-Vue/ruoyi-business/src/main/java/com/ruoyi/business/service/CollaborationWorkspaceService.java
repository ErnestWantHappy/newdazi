package com.ruoyi.business.service;

import java.io.IOException;
import java.util.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.ClassGroupingMapper;
import com.ruoyi.business.mapper.CollaborationMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/** 协作工作台直接保存课程班级分组，不写课堂大屏的固定分组方案。 */
@Service
public class CollaborationWorkspaceService {
    @Autowired private BizLessonMapper lessonMapper;
    @Autowired private BizLessonAssignmentMapper assignmentMapper;
    @Autowired private ClassGroupingMapper groupingMapper;
    @Autowired private CollaborationMapper mapper;
    @Autowired private CollaborationRoomService roomService;
    @Autowired private CollaborationActivityService activityService;

    public Map<String,Object> load(Long lessonId) {
        BizLesson lesson = requireLesson(lessonId);
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("lessonId", lessonId); result.put("lessonTitle", lesson.getLessonTitle());
        result.put("health", roomService.health());
        result.put("candidates", roomService.bankMaterialCandidates());
        List<Map<String,Object>> classes = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (BizLessonAssignment item : assignmentMapper.selectAssignmentsByLessonId(lessonId)) {
            if (!lesson.getDeptId().equals(item.getDeptId())) continue;
            String code = normalizeClass(item.getClassCode());
            if (!seen.add(item.getEntryYear() + ":" + code)) continue;
            Map<String,Object> row = new LinkedHashMap<>();
            row.put("entryYear", item.getEntryYear()); row.put("classCode", code);
            row.put("students", groupingMapper.selectClassStudents(lesson.getDeptId(), item.getEntryYear(), code));
            Map<String,Object> current = mapper.selectCurrentActivity(lessonId, lesson.getDeptId(), item.getEntryYear(), code);
            row.put("current", current == null ? null : activityService.detail(id(current.get("activityId"))));
            classes.add(row);
        }
        result.put("classes", classes);
        result.put("activities", activityService.list(lessonId));
        result.put("legacyRooms", roomService.teacherSettings(lessonId).get("rooms"));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> save(Long lessonId, Map<String,Object> request) throws IOException {
        BizLesson lesson = requireLesson(lessonId);
        if (request == null) throw new ServiceException("请先选择文档并设置分组");
        String requestId = text(request.get("requestId"));
        if (!requestId.matches("[A-Za-z0-9-]{16,36}")) throw new ServiceException("保存标识无效，请刷新页面后重试");
        String year = text(request.get("entryYear"));
        String code = normalizeClass(text(request.get("classCode")));
        // 同一课程的保存和首次进入共用行锁，避免双击建房以及学生进入与改组发生竞态。
        mapper.lockLesson(lessonId);
        if (groupingMapper.countLessonAssignment(lessonId, lesson.getDeptId(), year, code) == 0)
            throw new ServiceException("只能为本课程已指派的班级设置协作");
        List<Map<String,Object>> students = groupingMapper.selectClassStudents(lesson.getDeptId(), year, code);
        List<Long> materialIds = ids(request.get("materialIds"));
        List<Map<String,Object>> groups = maps(request.get("groups"));
        validateGroups(students, materialIds, groups);
        StringBuilder fingerprint = new StringBuilder(lessonId + ":" + year + ":" + code + ":" + materialIds);
        for (Map<String,Object> group : groups) fingerprint.append('|').append(group.get("materialId")).append(':').append(ids(group.get("studentIds")));
        String requestHash = DigestUtils.sha256Hex(fingerprint.toString());
        Map<String,Object> retry = mapper.selectActivityByRequest(requestId);
        if (retry != null) {
            if (!lessonId.equals(id(retry.get("lessonId"))) || !requestHash.equals(retry.get("requestHash")))
                throw new ServiceException("保存请求已变化，请刷新后重试");
            return activityService.detail(id(retry.get("activityId")));
        }
        Map<String,Object> current = mapper.selectCurrentActivity(lessonId, lesson.getDeptId(), year, code);
        Long previousId = current == null ? null : id(current.get("activityId"));
        if (!Objects.equals(previousId, id(request.get("currentActivityId"))))
            throw new ServiceException("本班协作已被更新，请刷新后重新设置");
        boolean started = current != null && current.get("frozenTime") != null;
        if (started && !Boolean.TRUE.equals(request.get("newRound")))
            throw new ServiceException("学生已经开始协作，请新建一轮以保留已有作品");
        if (!Boolean.TRUE.equals(roomService.health().get("ready"))) throw new ServiceException("协作服务暂不可用，请稍后重试");
        Map<Long,Map<String,Object>> candidates = new HashMap<>();
        for (Map<String,Object> item : roomService.bankMaterialCandidates()) candidates.put(id(item.get("materialId")), item);
        for (Long materialId : materialIds) {
            Map<String,Object> item = candidates.get(materialId);
            if (item == null) throw new ServiceException("所选文档已不可用，请重新选择");
            roomService.requireGroupActivityStarter(lessonId, id(item.get("questionId")), materialId);
        }
        Map<String,Object> priorSnapshot = groupingMapper.selectSnapshot(lessonId, lesson.getDeptId(), year, code);
        Long roundNo;
        if (previousId != null && !started) {
            // 学生尚未进入：直接替换当前未开始轮次，不产生无意义的新轮次，旧房间文件一并清理。
            deleteUnstartedBundle(previousId, id(current.get("snapshotId")));
            Long previousRound = priorSnapshot == null ? null : id(priorSnapshot.get("roundNo"));
            roundNo = previousRound == null ? 1L : previousRound;
        } else {
            Long previousRound = priorSnapshot == null ? null : id(priorSnapshot.get("roundNo"));
            roundNo = previousRound == null ? 1L : previousRound + 1;
        }
        Map<String,Object> snapshot = new HashMap<>();
        snapshot.put("lessonId", lessonId); snapshot.put("deptId", lesson.getDeptId());
        snapshot.put("entryYear", year); snapshot.put("classCode", code);
        snapshot.put("roundNo", roundNo);
        snapshot.put("snapshotHash", requestHash);
        groupingMapper.insertSnapshot(snapshot);
        Long snapshotId = id(snapshot.get("snapshotId"));
        List<Map<String,Object>> mappings = new ArrayList<>();
        int groupNo = 0;
        for (Map<String,Object> input : groups) {
            Map<String,Object> group = new HashMap<>();
            group.put("snapshotId", snapshotId); group.put("groupNo", ++groupNo);
            group.put("sortNo", groupNo); group.put("groupName", "第" + groupNo + "组");
            groupingMapper.insertSnapshotGroup(group);
            Long groupId = id(group.get("snapshotGroupId"));
            int memberNo = 0;
            for (Long studentId : ids(input.get("studentIds"))) {
                Map<String,Object> member = new HashMap<>();
                member.put("snapshotId", snapshotId); member.put("snapshotGroupId", groupId);
                member.put("studentId", studentId); member.put("sortNo", ++memberNo);
                groupingMapper.insertSnapshotMember(member);
            }
            Long materialId = id(input.get("materialId"));
            Map<String,Object> candidate = candidates.get(materialId);
            Map<String,Object> mapping = new HashMap<>();
            mapping.put("snapshotGroupId", groupId); mapping.put("materialId", materialId);
            mapping.put("questionId", candidate.get("questionId")); mapping.put("versionName", candidate.get("fileName"));
            mappings.add(mapping);
        }
        Map<String,Object> activityRequest = new HashMap<>();
        activityRequest.put("snapshotId", snapshotId); activityRequest.put("entryYear", year);
        activityRequest.put("classCode", code); activityRequest.put("groupTasks", mappings);
        Map<String,Object> saved = activityService.create(lessonId, activityRequest);
        Long activityId = id(saved.get("activityId"));
        mapper.identifyActivity(activityId, requestId, requestHash);
        // 未开始轮次已在保存前整体替换，这里只归档真正开始过的旧轮次为只读历史。
        if (previousId != null && started) {
            mapper.archiveActivityRooms(previousId);
            mapper.archiveActivity(previousId, "ARCHIVED");
        }
        return saved;
    }

    /** 删除学生尚未进入的旧轮次：房间版本事件映射快照整体清除，磁盘副本一并删除。 */
    private void deleteUnstartedBundle(Long activityId, Long snapshotId) {
        for (com.ruoyi.business.domain.CollaborationRoom room : mapper.selectRoomsByActivity(activityId)) {
            mapper.deleteOperationEventsByRoom(room.getRoomId());
            mapper.deleteRevisionDiffsByRoom(room.getRoomId());
            mapper.deleteRevisionsByRoom(room.getRoomId());
            mapper.deleteUploadTicketsByRoom(room.getRoomId());
            mapper.deleteCallbackEventsByRoom(room.getRoomId());
            mapper.deleteRoom(room.getRoomId());
            try {
                java.nio.file.Path file = roomService.resolveStoredFile(room.getCurrentFilePath());
                java.nio.file.Files.deleteIfExists(file);
                java.nio.file.Path dir = file.getParent();
                if (dir != null) java.nio.file.Files.deleteIfExists(dir);
            } catch (Exception ignored) { }
        }
        mapper.deleteGroupTasksByActivity(activityId);
        mapper.deleteTaskVersionsByActivity(activityId);
        mapper.deleteActivityRow(activityId);
        if (snapshotId != null) {
            groupingMapper.deleteSnapshotMembers(snapshotId);
            groupingMapper.deleteSnapshotGroups(snapshotId);
            groupingMapper.deleteSnapshot(snapshotId);
        }
    }
    static void validateGroups(List<Map<String,Object>> students, List<Long> materials, List<Map<String,Object>> groups) {
        if (students.isEmpty()) throw new ServiceException("本班没有可分组的学生");
        Set<Long> materialSet = new HashSet<>(materials);
        if (materials.isEmpty() || materialSet.size() != materials.size() || groups.size() != materials.size() || groups.size() > students.size())
            throw new ServiceException("每份所选文档对应一个小组，组数不能超过学生人数");
        Set<Long> valid = new HashSet<>();
        for (Map<String,Object> student : students) valid.add(id(student.get("studentId")));
        Set<Long> seen = new HashSet<>();
        for (Map<String,Object> group : groups) {
            if (!materialSet.contains(id(group.get("materialId")))) throw new ServiceException("各组只能使用上方已选的文档");
            List<Long> members = ids(group.get("studentIds"));
            if (members.isEmpty()) throw new ServiceException("每个小组至少需要一名学生");
            for (Long member : members) {
                if (!valid.contains(member)) throw new ServiceException("分组不能包含外班学生");
                if (!seen.add(member)) throw new ServiceException("每名学生只能属于一个小组");
            }
        }
        if (!seen.equals(valid)) throw new ServiceException("分组不能遗漏本班学生");
    }

    private BizLesson requireLesson(Long lessonId) {
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        if (lesson == null || !Objects.equals(lesson.getDeptId(), SecurityUtils.getDeptId())) throw new ServiceException("课程不存在或不属于当前学校");
        if (!SecurityUtils.isAdmin(SecurityUtils.getUserId()) && !Objects.equals(lesson.getCreatorId(), SecurityUtils.getUserId())) throw new ServiceException("只能管理自己创建的课程");
        return lesson;
    }
    static Long id(Object value) {
        if (value == null || "".equals(value)) return null;
        try { return Long.valueOf(String.valueOf(value)); } catch (NumberFormatException e) { throw new ServiceException("分组参数无效，请刷新后重试"); }
    }
    private static String text(Object value) { return value == null ? "" : String.valueOf(value).trim(); }
    private static String normalizeClass(String code) { return code != null && code.matches("[0-9]+") ? String.valueOf(Long.parseLong(code)) : code; }
    private static List<Long> ids(Object value) {
        List<Long> result = new ArrayList<>();
        if (!(value instanceof List)) return result;
        for (Object item : (List<?>) value) { Long id = id(item); if (id == null || id <= 0) throw new ServiceException("分组编号无效"); result.add(id); }
        return result;
    }
    @SuppressWarnings("unchecked") private static List<Map<String,Object>> maps(Object value) {
        if (!(value instanceof List)) return Collections.emptyList();
        for (Object item : (List<?>) value) if (!(item instanceof Map)) throw new ServiceException("分组参数无效");
        return (List<Map<String,Object>>) value;
    }
}
