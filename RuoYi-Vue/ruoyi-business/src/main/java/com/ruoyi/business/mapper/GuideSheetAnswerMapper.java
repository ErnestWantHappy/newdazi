package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizGuideSheetAnswer;
import org.apache.ibatis.annotations.Param;

public interface GuideSheetAnswerMapper
{
    public List<BizGuideSheetAnswer> selectBizGuideSheetAnswerList(BizGuideSheetAnswer answer);

    public BizGuideSheetAnswer selectBizGuideSheetAnswerByAnswerId(Long answerId);

    public BizGuideSheetAnswer selectByStudentAndSheet(@Param("studentId") Long studentId,
                                                        @Param("sheetId") Long sheetId);

    public List<BizGuideSheetAnswer> selectBySheetIdByClassCode(@Param("sheetId") Long sheetId,
                                                                       @Param("classCode") String classCode);

    public Double selectAvgScore(@Param("sheetId") Long sheetId,
                                  @Param("classCode") String classCode);

    public int insertBizGuideSheetAnswer(BizGuideSheetAnswer answer);

    public int updateBizGuideSheetAnswer(BizGuideSheetAnswer answer);

    public int deleteBizGuideSheetAnswerByAnswerIds(Long[] answerIds);

    /**
     * 按导学单ID删除所有答案记录（级联删除）
     */
    public int deleteBySheetId(Long sheetId);
}
