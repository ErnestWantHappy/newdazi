package com.ruoyi.business.mapper;

import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 受保护资源引用查询。
 *
 * <p>文件路径本身不是权限凭证，统一从业务数据反查资源归属。</p>
 */
public interface ResourceAccessMapper
{
    Long selectStudentAnswerIdByResource(@Param("resource") String resource);

    Long selectCountyAnswerIdByResource(@Param("resource") String resource);

    Long selectQuestionIdByResource(@Param("resource") String resource);

    Map<String, Object> selectExemptionAttachmentOwner(@Param("resource") String resource);

    int countCurrentLessonQuestionForStudent(@Param("studentId") Long studentId,
                                             @Param("questionId") Long questionId);

    int countCountyQuestionResource(@Param("resource") String resource);

    int countCountyQuestionResourceForStudent(@Param("resource") String resource,
                                              @Param("studentId") Long studentId);

    int countCountyQuestionResourceForGrader(@Param("resource") String resource,
                                             @Param("graderId") Long graderId);

    int countCountyAnswerForActiveGrader(@Param("answerId") Long answerId,
                                         @Param("graderId") Long graderId);
}
