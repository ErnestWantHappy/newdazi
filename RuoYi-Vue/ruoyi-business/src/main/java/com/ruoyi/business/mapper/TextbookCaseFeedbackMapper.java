package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.*;

/** 主键限制每人每课例一条，不用累加计数避免改票、重试造成重复。 */
public interface TextbookCaseFeedbackMapper
{
    @Select("SELECT COUNT(*) FROM biz_textbook_case c JOIN biz_textbook_book b ON b.book_id=c.book_id AND b.status='0' "
        + "JOIN biz_textbook_section s ON s.book_id=c.book_id AND s.section_key=c.section_key AND s.kind='LESSON' "
        + "WHERE c.case_id=#{caseId} AND c.status='PUBLISHED'")
    int published(Long caseId);

    @Select("SELECT COALESCE(SUM(vote=1),0) AS likes,COALESCE(SUM(vote=-1),0) AS dislikes,"
        + "COALESCE(MAX(CASE WHEN user_id=#{userId} THEN vote ELSE NULL END),0) AS myVote,"
        + "MAX(CASE WHEN user_id=#{userId} THEN reason ELSE NULL END) AS myReason "
        + "FROM biz_textbook_case_feedback WHERE case_id=#{caseId}")
    Map<String, Object> summary(@Param("caseId") Long caseId, @Param("userId") Long userId);

    @Insert("INSERT INTO biz_textbook_case_feedback(case_id,user_id,vote,reason) VALUES(#{caseId},#{userId},#{vote},#{reason}) "
        + "ON DUPLICATE KEY UPDATE "
        + "vote=VALUES(vote), "
        + "reason=CASE WHEN VALUES(vote)=0 THEN NULL WHEN VALUES(reason) IS NOT NULL THEN NULLIF(VALUES(reason),'') ELSE reason END, "
        + "updated_at=NOW()")
    int vote(@Param("caseId") Long caseId, @Param("userId") Long userId, @Param("vote") int vote, @Param("reason") String reason);

    @Select("SELECT vote, reason, updated_at AS updatedAt "
        + "FROM biz_textbook_case_feedback "
        + "WHERE case_id=#{caseId} AND vote!=0 AND reason IS NOT NULL AND TRIM(reason)!='' "
        + "ORDER BY updated_at DESC LIMIT 50")
    List<Map<String, Object>> comments(@Param("caseId") Long caseId);

    // 直接获取排他锁，避免INSERT IGNORE共享锁升级时多个请求互相等待。
    @Insert("INSERT INTO biz_textbook_case_invitation(user_id) VALUES(#{userId}) ON DUPLICATE KEY UPDATE user_id=VALUES(user_id)")
    int ensureInvitation(Long userId);

    @Select("SELECT user_id,last_attempt_at,last_invited_at FROM biz_textbook_case_invitation WHERE user_id=#{userId} FOR UPDATE")
    Map<String, Object> lockInvitation(Long userId);

    @Select("SELECT vote,last_invited_at,reason FROM biz_textbook_case_feedback WHERE case_id=#{caseId} AND user_id=#{userId} FOR UPDATE")
    Map<String, Object> feedback(@Param("caseId") Long caseId, @Param("userId") Long userId);

    @Insert("INSERT INTO biz_textbook_case_invitation(user_id, last_attempt_at, last_invited_at) "
        + "VALUES(#{userId}, NOW(), CASE WHEN #{invited} THEN NOW() ELSE NULL END) "
        + "ON DUPLICATE KEY UPDATE last_attempt_at=NOW(), "
        + "last_invited_at=CASE WHEN #{invited} THEN NOW() ELSE last_invited_at END")
    int attempted(@Param("userId") Long userId, @Param("invited") boolean invited);

    @Insert("INSERT INTO biz_textbook_case_feedback(case_id,user_id,last_invited_at) VALUES(#{caseId},#{userId},NOW()) "
        + "ON DUPLICATE KEY UPDATE last_invited_at=NOW()")
    int invited(@Param("caseId") Long caseId, @Param("userId") Long userId);
}
