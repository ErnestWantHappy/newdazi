package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizPracticalGradingDeadline;

public interface PracticalGradingDeadlineMapper
{
    Map<String, Object> selectClassMetrics(@Param("lessonId") Long lessonId,
                                           @Param("deptId") Long deptId,
                                           @Param("entryYear") String entryYear,
                                           @Param("classCode") String classCode);

    BizPracticalGradingDeadline selectDeadline(@Param("lessonId") Long lessonId,
                                                @Param("deptId") Long deptId,
                                                @Param("entryYear") String entryYear,
                                                @Param("classCode") String classCode);

    BizPracticalGradingDeadline selectDeadlineById(@Param("deadlineId") Long deadlineId);

    int insertDeadlineIgnore(BizPracticalGradingDeadline deadline);

    int updateCurrentDeadline(@Param("deadlineId") Long deadlineId,
                              @Param("oldDeadlineTime") java.util.Date oldDeadlineTime,
                              @Param("newDeadlineTime") java.util.Date newDeadlineTime,
                              @Param("adjustmentType") String adjustmentType,
                              @Param("updateBy") String updateBy);

    int insertDeadlineAudit(@Param("deadline") BizPracticalGradingDeadline deadline,
                            @Param("actionType") String actionType,
                            @Param("oldDeadlineTime") java.util.Date oldDeadlineTime,
                            @Param("newDeadlineTime") java.util.Date newDeadlineTime,
                            @Param("reason") String reason,
                            @Param("operatorId") Long operatorId,
                            @Param("operatorName") String operatorName);

    List<Map<String, Object>> selectDeadlineAudits(@Param("deadlineId") Long deadlineId);

    Map<String, Object> selectClassKeyByAnswerId(@Param("answerId") Long answerId);

    List<Map<String, Object>> selectUntriggeredPracticalClasses(@Param("afterScopeId") Long afterScopeId,
                                                                @Param("pageSize") int pageSize);

    int deleteAuditsByLessonId(@Param("lessonId") Long lessonId);

    int deleteDeadlinesByLessonId(@Param("lessonId") Long lessonId);
}
