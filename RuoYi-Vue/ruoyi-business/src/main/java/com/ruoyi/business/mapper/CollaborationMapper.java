package com.ruoyi.business.mapper;

import java.util.Date;
import java.util.List;
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
}
