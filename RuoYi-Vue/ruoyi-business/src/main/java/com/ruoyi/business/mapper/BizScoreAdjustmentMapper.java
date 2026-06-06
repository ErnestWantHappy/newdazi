package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.BizScoreAdjustment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 作业分人工修正记录Mapper接口
 */
public interface BizScoreAdjustmentMapper {
    int insert(BizScoreAdjustment adjustment);

    BizScoreAdjustment selectLatestByStudentAndLesson(@Param("studentId") Long studentId,
                                                      @Param("lessonId") Long lessonId,
                                                      @Param("deptId") Long deptId);

    List<BizScoreAdjustment> selectLatestByStudentIdsAndLessons(@Param("studentIds") List<Long> studentIds,
                                                                @Param("lessonIds") List<Long> lessonIds,
                                                                @Param("deptId") Long deptId);
}
