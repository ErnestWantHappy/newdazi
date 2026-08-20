package com.ruoyi.business.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.IotClassConfig;
import com.ruoyi.business.domain.IotDevice;
import com.ruoyi.business.domain.IotEvent;
import com.ruoyi.business.domain.IotExperiment;
import com.ruoyi.business.domain.IotGroup;
import com.ruoyi.business.domain.IotGroupStudent;
import com.ruoyi.business.domain.IotMessage;

/** 物联网实验、班级分组、快照、设备、消息与诊断数据访问。 */
public interface IotMapper
{
    IotExperiment selectExperimentById(@Param("experimentId") Long experimentId);
    List<IotExperiment> selectExperimentsByLesson(@Param("lessonId") Long lessonId, @Param("deptId") Long deptId);
    int insertExperiment(IotExperiment experiment);

    IotClassConfig selectClassConfig(@Param("experimentId") Long experimentId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);
    int insertClassConfig(IotClassConfig config);
    int updateClassConfig(IotClassConfig config);

    IotGroup selectGroupById(@Param("groupId") Long groupId);
    List<IotGroup> selectGroupsByExperiment(@Param("experimentId") Long experimentId);
    List<IotGroup> selectGroupsByExperimentAndClass(@Param("experimentId") Long experimentId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);
    List<IotGroup> selectGroupsByExperimentForTeacher(@Param("experimentId") Long experimentId, @Param("userId") Long userId, @Param("deptId") Long deptId);
    IotGroup selectGroupByTopic(@Param("topic") String topic);
    int insertGroup(IotGroup group);
    int deleteGroupsByExperimentAndClass(@Param("experimentId") Long experimentId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);
    int touchGroup(@Param("groupId") Long groupId, @Param("lastSeenAt") Date lastSeenAt);

    List<IotGroupStudent> selectGroupStudentsByGroupId(@Param("groupId") Long groupId);
    List<IotGroupStudent> selectGroupStudentsByExperimentAndClass(@Param("experimentId") Long experimentId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);
    IotGroupStudent selectGroupStudentByExpAndStudent(@Param("experimentId") Long experimentId, @Param("studentId") Long studentId);
    int insertGroupStudent(IotGroupStudent gs);
    int deleteGroupStudentsByExperimentAndClass(@Param("experimentId") Long experimentId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);

    List<Map<String, Object>> selectAssignedClassesByLesson(@Param("lessonId") Long lessonId, @Param("deptId") Long deptId);
    List<BizStudent> selectStudentsByClass(@Param("deptId") Long deptId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);

    IotDevice selectDeviceById(@Param("deviceId") Long deviceId);
    IotDevice selectDeviceByTopic(@Param("topic") String topic);
    List<IotDevice> selectDevicesByGroup(@Param("groupId") Long groupId);
    int insertDevice(IotDevice device);
    int updateDeviceCredential(IotDevice device);
    int updateDeviceStatus(@Param("deviceId") Long deviceId, @Param("status") String status);
    List<IotDevice> selectExpiredDevices(@Param("now") Date now);
    int touchDevice(@Param("deviceId") Long deviceId, @Param("lastSeenAt") Date lastSeenAt);

    int countTeacherGroupScope(@Param("experimentId") Long experimentId, @Param("userId") Long userId, @Param("deptId") Long deptId);
    int countTeacherLessonScope(@Param("lessonId") Long lessonId, @Param("userId") Long userId, @Param("deptId") Long deptId);
    int countTeacherClassScope(@Param("userId") Long userId, @Param("deptId") Long deptId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);

    int insertMessage(IotMessage message);
    List<IotMessage> selectRecentMessages(@Param("experimentId") Long experimentId, @Param("limit") int limit);
    List<IotMessage> selectRecentMessagesByGroup(@Param("groupId") Long groupId, @Param("limit") int limit);
    List<IotMessage> selectRecentMessagesForTeacher(@Param("experimentId") Long experimentId, @Param("userId") Long userId, @Param("deptId") Long deptId, @Param("limit") int limit);

    int insertEvent(IotEvent event);
    List<IotEvent> selectRecentEvents(@Param("experimentId") Long experimentId, @Param("limit") int limit);
    List<IotEvent> selectRecentEventsForTeacher(@Param("experimentId") Long experimentId, @Param("userId") Long userId, @Param("deptId") Long deptId, @Param("limit") int limit);
}
