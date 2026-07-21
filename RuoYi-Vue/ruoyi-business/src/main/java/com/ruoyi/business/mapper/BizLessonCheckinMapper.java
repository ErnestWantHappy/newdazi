package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizLessonCheckin;

/**
 * 课堂考勤签到 Mapper
 */
public interface BizLessonCheckinMapper
{
    BizLessonCheckin selectByLessonAndStudent(@Param("lessonId") Long lessonId, @Param("studentId") Long studentId);

    int insertIgnore(BizLessonCheckin checkin);

    List<BizLessonCheckin> selectRosterByLessonAndClass(
            @Param("lessonId") Long lessonId,
            @Param("classCode") String classCode,
            @Param("entryYear") String entryYear,
            @Param("deptId") Long deptId);

    int countByLesson(@Param("lessonId") Long lessonId);
}
