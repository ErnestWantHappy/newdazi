package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizStudentTaskState;
import com.ruoyi.business.domain.vo.ClassroomStudentTaskSummaryVo;
import org.apache.ibatis.annotations.Param;

/** 学生课堂任务状态 Mapper。 */
public interface BizStudentTaskStateMapper
{
    List<BizStudentTaskState> selectStudentLessonStates(@Param("studentId") Long studentId,
                                                       @Param("lessonId") Long lessonId);
    List<BizStudentTaskState> selectStudentReturnedStates(@Param("studentId") Long studentId);
    int upsert(BizStudentTaskState state);

    int withdrawSubmission(BizStudentTaskState state);

    BizStudentTaskState selectOneForUpdate(@Param("lessonId") Long lessonId,
                                          @Param("questionId") Long questionId,
                                          @Param("studentId") Long studentId);

    BizStudentTaskState selectOne(@Param("lessonId") Long lessonId,
                                  @Param("questionId") Long questionId,
                                  @Param("studentId") Long studentId);

    List<BizStudentTaskState> selectClassStates(@Param("deptId") Long deptId,
                                                @Param("lessonId") Long lessonId,
                                                @Param("questionId") Long questionId,
                                                @Param("entryYear") String entryYear,
                                                @Param("classCode") String classCode);

    List<ClassroomStudentTaskSummaryVo> selectClassSummary(@Param("deptId") Long deptId,
                                                            @Param("lessonId") Long lessonId,
                                                            @Param("entryYear") String entryYear,
                                                            @Param("classCode") String classCode);
}
