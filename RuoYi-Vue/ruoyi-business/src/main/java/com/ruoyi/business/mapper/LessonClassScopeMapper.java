package com.ruoyi.business.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizLessonAssignment;

/**
 * 维护课程与班级的稳定事实，当前指派删除后仍保留历史关系。
 */
public interface LessonClassScopeMapper
{
    int upsertCurrentAssignment(BizLessonAssignment assignment);

    int markLessonAssignmentsInactive(@Param("lessonId") Long lessonId);

    int markAssignmentInactive(@Param("lessonId") Long lessonId,
                               @Param("deptId") Long deptId,
                               @Param("entryYear") String entryYear,
                               @Param("classCode") String classCode);

    int markOtherAssignmentsInactive(@Param("deptId") Long deptId,
                                     @Param("entryYear") String entryYear,
                                     @Param("classCode") String classCode,
                                     @Param("currentLessonId") Long currentLessonId);

    int deleteByLessonId(@Param("lessonId") Long lessonId);
}
