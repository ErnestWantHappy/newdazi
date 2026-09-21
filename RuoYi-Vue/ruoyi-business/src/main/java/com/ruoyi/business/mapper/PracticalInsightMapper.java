package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.*;
import com.ruoyi.business.domain.dto.PracticalInsightV3.*;

public interface PracticalInsightMapper
{
    String JOB_COLUMNS = "report_id reportId,teacher_id teacherId,dept_id deptId,lesson_id lessonId,question_id questionId,entry_year entryYear,class_code classCode,fingerprint,status,stage,run_token runToken,input_json inputJson,report_json reportJson,completed,failed,prompt_tokens promptTokens,completion_tokens completionTokens,error_message errorMessage,created_at createdAt,updated_at updatedAt";
    @Insert("INSERT INTO biz_practical_insight_report(teacher_id,dept_id,lesson_id,question_id,entry_year,class_code,fingerprint,status,stage,input_json,created_at,updated_at) VALUES(#{teacherId},#{deptId},#{lessonId},#{questionId},#{entryYear},#{classCode},#{fingerprint},'PENDING','PENDING',#{inputJson},NOW(),NOW())")
    @Options(useGeneratedKeys=true, keyProperty="reportId")
    int insert(Job job);
    @Select("SELECT " + JOB_COLUMNS + " FROM biz_practical_insight_report WHERE report_id=#{id}")
    Job get(Long id);
    @Select("SELECT " + JOB_COLUMNS + " FROM biz_practical_insight_report WHERE teacher_id=#{teacher} AND fingerprint=#{fingerprint}")
    Job find(@Param("teacher") Long teacher, @Param("fingerprint") String fingerprint);
    @Select("SELECT " + JOB_COLUMNS + " FROM biz_practical_insight_report WHERE teacher_id=#{teacher} AND lesson_id=#{scope.lessonId} AND question_id=#{scope.questionId} AND entry_year=#{scope.entryYear} AND class_code=#{scope.classCode} ORDER BY report_id DESC LIMIT 1")
    Job latest(@Param("teacher") Long teacher, @Param("scope") Request scope);
    @Select("SELECT COUNT(*) FROM biz_practical_insight_report WHERE teacher_id=#{teacher} AND status IN ('PENDING','RUNNING')")
    int active(Long teacher);
    @Update("UPDATE biz_practical_insight_report SET status='RUNNING',run_token=#{token},updated_at=NOW() WHERE report_id=#{id} AND status='PENDING'")
    int claim(@Param("id") Long id, @Param("token") String token);
    @Update("UPDATE biz_practical_insight_report SET stage=#{stage},updated_at=NOW() WHERE report_id=#{id} AND run_token=#{token} AND status='RUNNING'")
    int stage(@Param("id") Long id, @Param("token") String token, @Param("stage") String stage);
    @Update("UPDATE biz_practical_insight_report SET prompt_tokens=prompt_tokens+#{input},completion_tokens=completion_tokens+#{output},updated_at=NOW() WHERE report_id=#{id}")
    int usage(@Param("id") Long id, @Param("input") long input, @Param("output") long output);
    @Update("UPDATE biz_practical_insight_report SET completed=#{completed},failed=#{failed},report_json=#{reportJson},updated_at=NOW() WHERE report_id=#{reportId} AND run_token=#{runToken} AND status='RUNNING'")
    int progress(Job job);
    @Update("UPDATE biz_practical_insight_report SET status=#{status},stage=#{status},error_message=#{message},updated_at=NOW() WHERE report_id=#{id} AND run_token=#{token} AND status='RUNNING'")
    int finish(@Param("id") Long id, @Param("token") String token, @Param("status") String status, @Param("message") String message);
    @Update("UPDATE biz_practical_insight_report SET status='CANCELLED',updated_at=NOW() WHERE report_id=#{id} AND status IN ('PENDING','RUNNING')")
    int cancel(Long id);
    @Update("UPDATE biz_practical_insight_report SET status='PENDING',error_message=NULL,updated_at=NOW() WHERE report_id=#{id} AND status IN ('FAILED','PARTIAL','INTERRUPTED','CANCELLED')")
    int retry(Long id);
    @Select("SELECT report_id FROM biz_practical_insight_report WHERE teacher_id=#{teacher} AND status IN ('RUNNING','PENDING') AND updated_at < DATE_SUB(NOW(), INTERVAL 30 MINUTE)")
    List<Long> staleIds(Long teacher);
    @Update("UPDATE biz_practical_insight_report SET status='INTERRUPTED',error_message='任务中断，请核对后手动重试；上次请求可能已计费',updated_at=NOW() WHERE report_id=#{id} AND status IN ('RUNNING','PENDING') AND updated_at < DATE_SUB(NOW(), INTERVAL 30 MINUTE)")
    int interruptStale(Long id);
    @Select("SELECT paper_json FROM biz_practical_insight_paper_map WHERE teacher_id=#{teacher} AND fingerprint=#{fingerprint}")
    String paper(@Param("teacher") Long teacher, @Param("fingerprint") String fingerprint);
    @Insert("INSERT INTO biz_practical_insight_paper_map VALUES(#{teacher},#{fingerprint},#{json},NOW()) ON DUPLICATE KEY UPDATE paper_json=VALUES(paper_json)")
    int savePaper(@Param("teacher") Long teacher, @Param("fingerprint") String fingerprint, @Param("json") String json);
    @Select("SELECT report_id reportId,student_id studentId,fingerprint,status,result_json resultJson FROM biz_practical_insight_student_evidence WHERE report_id=#{id} ORDER BY student_id")
    List<EvidenceRow> evidence(Long id);
    @Select("SELECT e.result_json FROM biz_practical_insight_student_evidence e JOIN biz_practical_insight_report r ON r.report_id=e.report_id WHERE r.teacher_id=#{teacher} AND e.student_id=#{student} AND e.fingerprint=#{fingerprint} AND e.status='SUCCESS' ORDER BY e.report_id DESC LIMIT 1")
    String reusable(@Param("teacher") Long teacher, @Param("student") Long student, @Param("fingerprint") String fingerprint);
    @Insert("INSERT INTO biz_practical_insight_student_evidence VALUES(#{reportId},#{studentId},#{fingerprint},#{status},#{resultJson},NOW()) ON DUPLICATE KEY UPDATE fingerprint=VALUES(fingerprint),status=VALUES(status),result_json=VALUES(result_json),updated_at=NOW()")
    int saveEvidence(EvidenceRow row);
    @Select("SELECT evidence_json FROM biz_practical_ai_result WHERE answer_id=#{answer} AND practical_version_id=#{version} AND result_status='SUCCESS' ORDER BY result_id DESC LIMIT 1")
    String recognition(@Param("answer") Long answer, @Param("version") Long version);
}
