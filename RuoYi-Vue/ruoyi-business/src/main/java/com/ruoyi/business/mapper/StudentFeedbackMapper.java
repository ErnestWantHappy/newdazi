package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.*;
import com.ruoyi.business.domain.dto.PracticalInsightV3.Job;

public interface StudentFeedbackMapper {
    @Select("SELECT publication_id FROM biz_student_feedback_publication WHERE report_id=#{id} AND content_hash=#{hash}")
    Long publishedReport(@Param("id") Long id,@Param("hash") String hash);
    @Insert("INSERT INTO biz_student_feedback_publication(report_id,content_hash,teacher_id,dept_id,lesson_id,question_id,entry_year,class_code) VALUES(#{job.reportId},#{hash},#{job.teacherId},#{job.deptId},#{job.lessonId},#{job.questionId},#{job.entryYear},#{job.classCode})")
    int publish(@Param("job") Job job,@Param("hash") String hash);
    @Insert("INSERT INTO biz_student_feedback_snapshot(publication_id,student_id,source_version_id,payload_json) VALUES(#{publication},#{student},#{version},#{payload})")
    int snapshot(@Param("publication") Long publication, @Param("student") Long student, @Param("version") Long version, @Param("payload") String payload);
    @Select("SELECT s.payload_json payloadJson,s.source_version_id sourceVersionId,p.publication_id publicationId,p.report_id reportId,p.published_at publishedAt FROM biz_student_feedback_snapshot s JOIN biz_student_feedback_publication p ON p.publication_id=s.publication_id WHERE s.student_id=#{student} AND p.dept_id=#{dept} AND p.lesson_id=#{lesson} AND p.question_id=#{question} ORDER BY p.publication_id DESC LIMIT 1")
    Map<String,Object> latest(@Param("student") Long student,@Param("dept") Long dept,@Param("lesson") Long lesson,@Param("question") Long question);
    @Select("SELECT DISTINCT l.lesson_id lessonId,l.lesson_title lessonTitle FROM biz_student_answer a JOIN biz_lesson l ON l.lesson_id=a.lesson_id WHERE a.student_id=#{student} ORDER BY l.lesson_id DESC")
    List<Map<String,Object>> lessons(Long student);
}
