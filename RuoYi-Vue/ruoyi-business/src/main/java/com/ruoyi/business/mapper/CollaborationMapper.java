package com.ruoyi.business.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.CollaborationRoom;
import com.ruoyi.business.domain.CollaborationUploadTicket;

/** 在线协作房间、文件版本和历史 WPS 票据持久化。 */
public interface CollaborationMapper
{
    /** 教师首页批量标记存在开放协作房间的课程，避免逐课程查询。 */
    List<Long> selectOpenLessonIdsByLessonIds(@Param("lessonIds") List<Long> lessonIds,
                                              @Param("deptId") Long deptId);

    CollaborationRoom selectRoomById(@Param("roomId") Long roomId);

    CollaborationRoom selectRoomByPublicFileId(@Param("publicFileId") String publicFileId);

    CollaborationRoom selectRoomByClass(@Param("lessonId") Long lessonId,
                                        @Param("questionId") Long questionId,
                                        @Param("deptId") Long deptId,
                                        @Param("entryYear") String entryYear,
                                        @Param("classCode") String classCode);

    List<CollaborationRoom> selectRoomsByLesson(@Param("lessonId") Long lessonId,
                                                @Param("deptId") Long deptId);

    int insertRoom(CollaborationRoom room);

    int updateRoomStatus(@Param("lessonId") Long lessonId,
                         @Param("deptId") Long deptId,
                         @Param("status") String status);

    int reopenRoom(@Param("roomId") Long roomId, @Param("status") String status);

    int updateRoomProvider(@Param("roomId") Long roomId,
                           @Param("provider") String provider,
                           @Param("providerSessionKey") String providerSessionKey);

    int markRoomOpened(@Param("roomId") Long roomId, @Param("openTime") Date openTime);

    int updateRoomCallback(@Param("roomId") Long roomId,
                           @Param("callbackType") String callbackType,
                           @Param("callbackStatus") String callbackStatus,
                           @Param("wpsRequestId") String wpsRequestId,
                           @Param("errorMessage") String errorMessage,
                           @Param("updateTime") Date updateTime);

    int insertRevision(@Param("roomId") Long roomId,
                       @Param("versionNo") Integer versionNo,
                       @Param("fileName") String fileName,
                       @Param("filePath") String filePath,
                       @Param("fileSize") Long fileSize,
                       @Param("sha256") String sha256,
                       @Param("digestType") String digestType,
                       @Param("digest") String digest,
                       @Param("manualSave") Boolean manualSave,
                       @Param("savedByUserId") Long savedByUserId,
                       @Param("createTime") Date createTime);

    /** 教师监管：按时间倒序读取房间全部不可变版本（含保存人昵称）。 */
    List<Map<String, Object>> selectRevisionsByRoomId(@Param("roomId") Long roomId);

    int insertUploadTicket(CollaborationUploadTicket ticket);

    CollaborationUploadTicket selectUploadTicket(@Param("ticketToken") String ticketToken);

    int markTicketUploaded(@Param("ticketToken") String ticketToken,
                           @Param("uploadedFileSize") Long uploadedFileSize,
                           @Param("uploadedSha256") String uploadedSha256,
                           @Param("updateTime") Date updateTime);

    int markTicketCompleted(@Param("ticketId") Long ticketId,
                            @Param("status") String status,
                            @Param("completedTime") Date completedTime,
                            @Param("errorMessage") String errorMessage);

    int commitRoomVersion(@Param("roomId") Long roomId,
                          @Param("expectedVersion") Integer expectedVersion,
                          @Param("nextVersion") Integer nextVersion,
                          @Param("fileName") String fileName,
                          @Param("filePath") String filePath,
                          @Param("fileExtension") String fileExtension,
                          @Param("mimeType") String mimeType,
                          @Param("fileSize") Long fileSize,
                          @Param("sha256") String sha256,
                          @Param("modifierUserId") Long modifierUserId,
                          @Param("saveTime") Date saveTime);

    int insertCallbackEvent(@Param("roomId") Long roomId,
                            @Param("publicFileId") String publicFileId,
                            @Param("callbackType") String callbackType,
                            @Param("callbackStatus") String callbackStatus,
                            @Param("wpsRequestId") String wpsRequestId,
                            @Param("userId") Long userId,
                            @Param("remoteIp") String remoteIp,
                            @Param("durationMs") Long durationMs,
                            @Param("errorCode") String errorCode,
                            @Param("errorMessage") String errorMessage,
                            @Param("createTime") Date createTime);

    int insertActivity(Map<String,Object> row);
    int insertTaskVersion(Map<String,Object> row);
    int insertGroupTask(Map<String,Object> row);
    Map<String,Object> selectActivity(@Param("activityId") Long activityId);
    List<Map<String,Object>> selectActivitiesByLesson(@Param("lessonId") Long lessonId, @Param("deptId") Long deptId);
    /** 引用指定分组快照的小组协作活动数，用于重新分组前判断历史房间是否失联。 */
    int countActivitiesBySnapshot(@Param("snapshotId") Long snapshotId);
    List<Map<String,Object>> selectGroupTasks(@Param("activityId") Long activityId);
    List<Map<String,Object>> selectSnapshotGroups(@Param("snapshotId") Long snapshotId);
    Long selectStudentIdByUserId(@Param("userId") Long userId);
    int insertOperationEvent(@Param("roomId") Long roomId, @Param("userId") Long userId,
                             @Param("studentId") Long studentId, @Param("eventType") String eventType,
                             @Param("eventDetail") String eventDetail, @Param("createTime") Date createTime);
    List<Map<String,Object>> selectOperationEvents(@Param("roomId") Long roomId);
    int countActivityRoomMembership(@Param("roomId") Long roomId, @Param("studentId") Long studentId);
    int countActivityRoom(@Param("roomId") Long roomId);
    int insertRevisionDiff(@Param("revisionId") Long revisionId, @Param("status") String status,
                           @Param("summary") String summary, @Param("error") String error, @Param("processedTime") Date processedTime);
    int updateRevisionDiff(@Param("revisionId") Long revisionId, @Param("status") String status,
                           @Param("summary") String summary, @Param("error") String error, @Param("processedTime") Date processedTime);
    Map<String,Object> selectRevisionByRoomVersion(@Param("roomId") Long roomId, @Param("versionNo") Integer versionNo);
    Map<String,Object> selectRevisionPair(@Param("roomId") Long roomId, @Param("versionNo") Integer versionNo);
    int freezeActivityForRoom(@Param("roomId") Long roomId, @Param("frozenTime") Date frozenTime);
    List<Map<String,Object>> selectSnapshotsByLesson(@Param("lessonId") Long lessonId, @Param("deptId") Long deptId);
    /** 题库来源的协作起始文件：全校公开或本人创建的文件作品题；creatorId 为空表示管理员不过滤。 */
    List<Map<String,Object>> selectBankStarterCandidates(@Param("creatorId") Long creatorId);
    /** 协作选题弹窗：按年级/学期/课次/关键词分页搜索起始文件。 */
    List<Map<String,Object>> selectBankStarterSearch(@Param("creatorId") Long creatorId, @Param("grade") Long grade, @Param("semester") String semester, @Param("lessonNum") Integer lessonNum, @Param("keyword") String keyword);
    /** 快照各组成员明细：学号+姓名，供教师房间列表展示。 */
    List<Map<String,Object>> selectSnapshotGroupMembers(@Param("snapshotId") Long snapshotId);
    Long lockLesson(@Param("lessonId") Long lessonId);
    Map<String,Object> selectCurrentActivity(@Param("lessonId") Long lessonId, @Param("deptId") Long deptId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);
    Map<String,Object> selectActivityByRequest(@Param("requestId") String requestId);
    int identifyActivity(@Param("activityId") Long activityId, @Param("requestId") String requestId, @Param("requestHash") String requestHash);
    int archiveActivity(@Param("activityId") Long activityId, @Param("status") String status);
    int archiveActivityRooms(@Param("activityId") Long activityId);
    /** 旧全班设置只关闭未绑定小组活动的房间，小组房间与历史作品不受影响。 */
    int closeNonActivityRooms(@Param("lessonId") Long lessonId, @Param("deptId") Long deptId);
    /** 未开始轮次的整体替换：按活动删除房间、版本、事件与映射，磁盘文件由调用方清理。 */
    List<CollaborationRoom> selectRoomsByActivity(@Param("activityId") Long activityId);
    int deleteGroupTasksByActivity(@Param("activityId") Long activityId);
    int deleteTaskVersionsByActivity(@Param("activityId") Long activityId);
    int deleteActivityRow(@Param("activityId") Long activityId);
    int deleteOperationEventsByRoom(@Param("roomId") Long roomId);
    int deleteRevisionDiffsByRoom(@Param("roomId") Long roomId);
    int deleteRevisionsByRoom(@Param("roomId") Long roomId);
    int deleteUploadTicketsByRoom(@Param("roomId") Long roomId);
    int deleteCallbackEventsByRoom(@Param("roomId") Long roomId);
    int deleteRoom(@Param("roomId") Long roomId);
    List<Map<String,Object>> selectStudentHistory(@Param("studentId") Long studentId, @Param("deptId") Long deptId);
    /** 房间花名册：本组学生名单、各人最近进入时间，供编辑器成员抽屉展示。 */
    List<Map<String,Object>> selectRoomRoster(@Param("roomId") Long roomId);
    /** 房间教师进入记录：进入过的教师展示名与最近进入时间。 */
    List<Map<String,Object>> selectRoomTeacherEnters(@Param("roomId") Long roomId);
}
