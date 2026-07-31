package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.query.TeachingSupervisionQuery;

public interface TeachingSupervisionMapper
{
    long countSchoolSummaries(@Param("q") TeachingSupervisionQuery query);
    List<Map<String, Object>> selectSchoolSummaries(@Param("q") TeachingSupervisionQuery query);
    List<Map<String, Object>> selectTeacherSummaries(@Param("q") TeachingSupervisionQuery query);
    List<Map<String, Object>> selectCourseSummaries(@Param("q") TeachingSupervisionQuery query);
    List<Map<String, Object>> selectClassSummaries(@Param("q") TeachingSupervisionQuery query);
    List<Map<String, Object>> selectStudentDetails(@Param("q") TeachingSupervisionQuery query);
    List<Map<String, Object>> selectQuestionDetails(@Param("lessonId") Long lessonId);
    List<Map<String, Object>> selectPracticalAnswerDetails(@Param("lessonId") Long lessonId,
                                                           @Param("entryYear") String entryYear,
                                                           @Param("classCode") String classCode,
                                                           @Param("studentId") Long studentId);
}
