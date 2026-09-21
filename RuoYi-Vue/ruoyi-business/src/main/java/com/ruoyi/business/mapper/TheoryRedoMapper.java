package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizLessonAssignment;

public interface TheoryRedoMapper
{
    Long lockStudent(@Param("studentId") Long studentId);
    BizLessonAssignment lockCurrentAssignment(@Param("deptId") Long deptId,
            @Param("entryYear") String entryYear, @Param("classCode") String classCode);
    long revision(@Param("studentId") Long studentId, @Param("lessonId") Long lessonId);
    long typingRevision(@Param("studentId") Long studentId, @Param("lessonId") Long lessonId);
    int archiveTyping(@Param("studentId") Long studentId, @Param("lessonId") Long lessonId,
            @Param("deptId") Long deptId, @Param("entryYear") String entryYear,
            @Param("classCode") String classCode, @Param("teacherId") Long teacherId,
            @Param("revision") long revision, @Param("snapshot") String snapshot);
    List<Map<String, Object>> classStatus(@Param("deptId") Long deptId,
            @Param("entryYear") String entryYear, @Param("classCode") String classCode,
            @Param("lessonId") Long lessonId);
    int archive(@Param("studentId") Long studentId, @Param("lessonId") Long lessonId,
            @Param("deptId") Long deptId, @Param("entryYear") String entryYear,
            @Param("classCode") String classCode, @Param("teacherId") Long teacherId,
            @Param("revision") long revision, @Param("snapshot") String snapshot);
}
