package com.ruoyi.business.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.CollaborationRoom;
import com.ruoyi.business.domain.PracticalQuestionMaterial;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.ClassGroupingMapper;
import com.ruoyi.business.mapper.CollaborationMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/** 非计分小组协作活动。活动房间仍复用既有 Provider 和版本保存链路。 */
@Service
public class CollaborationActivityService
{
    @Autowired private CollaborationMapper mapper;
    @Autowired private ClassGroupingMapper groupingMapper;
    @Autowired private BizLessonMapper lessonMapper;
    @Autowired private CollaborationRoomService roomService;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> create(Long lessonId, Map<String, Object> request) throws IOException
    {
        BizLesson lesson = requireTeacherLesson(lessonId);
        String entryYear = value(request, "entryYear");
        String classCode = value(request, "classCode");
        Long snapshotId = number(request.get("snapshotId"));
        if (snapshotId == null || StringUtils.isBlank(entryYear) || StringUtils.isBlank(classCode))
            throw new ServiceException("请先选择已冻结的课时分组快照");
        Map<String, Object> snapshot = groupingMapper.selectSnapshot(lessonId, lesson.getDeptId(), entryYear, classCode);
        if (snapshot == null || !snapshotId.equals(number(snapshot.get("snapshotId"))))
            throw new ServiceException("协作活动只能使用本课程本班已冻结的分组快照");
        List<Map<String, Object>> mappings = mapList(request.get("groupTasks"));
        List<Map<String, Object>> groups = mapper.selectSnapshotGroups(snapshotId);
        if (mappings.size() != groups.size() || groups.isEmpty())
            throw new ServiceException("每个课时小组都必须绑定一个任务版本");
        Map<String, Object> activity = new LinkedHashMap<String, Object>();
        activity.put("lessonId", lessonId); activity.put("deptId", lesson.getDeptId());
        activity.put("entryYear", entryYear); activity.put("classCode", classCode); activity.put("snapshotId", snapshotId);
        activity.put("activityTitle", StringUtils.defaultIfBlank(value(request, "activityTitle"), lesson.getLessonTitle() + "小组协作"));
        activity.put("creatorUserId", SecurityUtils.getUserId());
        mapper.insertActivity(activity);
        Long activityId = number(activity.get("activityId"));
        List<Map<String, Object>> versions = new ArrayList<Map<String, Object>>();
        Set<Long> mappedGroups = new HashSet<Long>();
        for (int i = 0; i < mappings.size(); i++)
        {
            Map<String, Object> item = mappings.get(i);
            Long groupId = number(item.get("snapshotGroupId"));
            if (!containsGroup(groups, groupId) || !mappedGroups.add(groupId)) throw new ServiceException("任务映射必须覆盖每个小组且不能重复");
            Long questionId = number(item.get("questionId")); Long materialId = number(item.get("materialId"));
            PracticalQuestionMaterial material = roomService.requireGroupActivityStarter(lessonId, questionId, materialId);
            Map<String, Object> version = new LinkedHashMap<String, Object>();
            version.put("activityId", activityId); version.put("questionId", questionId); version.put("sourceMaterialId", materialId);
            version.put("versionName", StringUtils.defaultIfBlank(value(item, "versionName"), material.getOriginalFileName())); version.put("sortNo", i);
            mapper.insertTaskVersion(version);
            Long taskVersionId = number(version.get("taskVersionId"));
            CollaborationRoom room = roomService.createGroupActivityRoom(lesson, entryYear, classCode, material,
                    taskVersionId, StringUtils.defaultString((String) version.get("versionName")));
            Map<String, Object> link = new LinkedHashMap<String, Object>();
            link.put("activityId", activityId); link.put("snapshotGroupId", groupId); link.put("taskVersionId", taskVersionId); link.put("roomId", room.getRoomId());
            mapper.insertGroupTask(link); versions.add(link);
        }
        return detail(activityId);
    }

    public List<Map<String, Object>> list(Long lessonId)
    {
        requireTeacherLesson(lessonId);
        return mapper.selectActivitiesByLesson(lessonId, SecurityUtils.getDeptId());
    }

    public Map<String, Object> setup(Long lessonId)
    {
        BizLesson lesson = requireTeacherLesson(lessonId);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        List<Map<String, Object>> snapshots = mapper.selectSnapshotsByLesson(lessonId, lesson.getDeptId());
        for (Map<String, Object> snapshot : snapshots)
            snapshot.put("groups", mapper.selectSnapshotGroups(number(snapshot.get("snapshotId"))));
        result.put("snapshots", snapshots);
        result.put("candidates", roomService.teacherSettings(lessonId).get("candidates"));
        return result;
    }

    public Map<String, Object> detail(Long activityId)
    {
        Map<String, Object> activity = mapper.selectActivity(activityId);
        if (activity == null) throw new ServiceException("协作活动不存在");
        requireTeacherLesson(number(activity.get("lessonId")));
        activity.put("groupTasks", mapper.selectGroupTasks(activityId));
        return activity;
    }

    public void recordHeartbeat(Long roomId)
    {
        CollaborationRoom room = roomService.requireRoom(roomId);
        String scope = roomService.assertRoomAccess(roomId);
        if (!"STUDENT".equals(scope)) throw new ServiceException("仅学生协作会话可发送心跳");
        mapper.insertOperationEvent(roomId, SecurityUtils.getUserId(), mapper.selectStudentIdByUserId(SecurityUtils.getUserId()), "HEARTBEAT", null, new Date());
    }

    public void recordLeave(Long roomId)
    {
        roomService.assertRoomAccess(roomId);
        mapper.insertOperationEvent(roomId, SecurityUtils.getUserId(), mapper.selectStudentIdByUserId(SecurityUtils.getUserId()), "LEAVE", null, new Date());
    }

    public List<Map<String, Object>> timeline(Long roomId)
    {
        CollaborationRoom room = roomService.requireRoom(roomId);
        requireTeacherLesson(room.getLessonId());
        return mapper.selectOperationEvents(roomId);
    }

    private BizLesson requireTeacherLesson(Long lessonId)
    {
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        Long userId = SecurityUtils.getUserId();
        if (lesson == null || !SecurityUtils.getDeptId().equals(lesson.getDeptId())) throw new ServiceException("课程不存在或不属于当前学校");
        if (!SecurityUtils.isAdmin(userId) && !userId.equals(lesson.getCreatorId())) throw new ServiceException("只能管理自己创建的课程");
        return lesson;
    }
    private static boolean containsGroup(List<Map<String, Object>> groups, Long id) { for (Map<String, Object> group : groups) if (id != null && id.equals(number(group.get("snapshotGroupId")))) return true; return false; }
    @SuppressWarnings("unchecked") private static List<Map<String, Object>> mapList(Object value) { return value instanceof List ? (List<Map<String, Object>>) value : new ArrayList<Map<String, Object>>(); }
    private static Long number(Object value) { return value == null ? null : value instanceof Number ? ((Number) value).longValue() : Long.valueOf(String.valueOf(value)); }
    private static String value(Map<String, Object> map, String key) { return map == null || map.get(key) == null ? null : String.valueOf(map.get(key)); }
}
