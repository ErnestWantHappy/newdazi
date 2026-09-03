package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizStudentTaskState;
import org.apache.ibatis.annotations.Param;

/** 学生课堂任务状态 Mapper。 */
public interface BizStudentTaskStateMapper
{
    int upsert(BizStudentTaskState state);

    BizStudentTaskState selectOne(@Param("lessonId") Long lessonId,
                                  @Param("questionId") Long questionId,
                                  @Param("studentId") Long studentId);

    List<BizStudentTaskState> selectClassStates(@Param("deptId") Long deptId,
                                                @Param("lessonId") Long lessonId,
                                                @Param("questionId") Long questionId,
                                                @Param("entryYear") String entryYear,
                                                @Param("classCode") String classCode);
}
