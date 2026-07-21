package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizGuideSheetAnswer;
import org.apache.ibatis.annotations.Param;

public interface GuideSheetAnswerMapper
{
    List<BizGuideSheetAnswer> selectBizGuideSheetAnswerList(BizGuideSheetAnswer answer);

    BizGuideSheetAnswer selectBizGuideSheetAnswerByAnswerId(Long answerId);

    BizGuideSheetAnswer selectByStudentAndBinding(@Param("studentId") Long studentId,
                                                  @Param("bindingId") Long bindingId);

    List<BizGuideSheetAnswer> selectByBindingAndClass(@Param("bindingId") Long bindingId,
                                                      @Param("deptId") Long deptId,
                                                      @Param("entryYear") String entryYear,
                                                      @Param("classCode") String classCode);

    Double selectAvgScore(@Param("bindingId") Long bindingId,
                          @Param("deptId") Long deptId,
                          @Param("entryYear") String entryYear,
                          @Param("classCode") String classCode);

    int insertBizGuideSheetAnswer(BizGuideSheetAnswer answer);

    int updateBizGuideSheetAnswer(BizGuideSheetAnswer answer);

    int updateGradingFields(BizGuideSheetAnswer answer);
}
