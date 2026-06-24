package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizGuideSheetProgress;
import com.ruoyi.business.domain.vo.GuideSheetProgressVo;
import org.apache.ibatis.annotations.Param;

public interface GuideSheetProgressMapper
{
    public List<GuideSheetProgressVo> selectBySheetAndClass(@Param("sheetId") Long sheetId,
                                                              @Param("classCode") String classCode);

    public List<GuideSheetProgressVo> selectBySheetId(@Param("sheetId") Long sheetId);

    public BizGuideSheetProgress selectBySheetAndStudent(@Param("sheetId") Long sheetId,
                                                           @Param("studentId") Long studentId);

    public int insertOrUpdate(BizGuideSheetProgress progress);

    public int deleteBySheetId(Long sheetId);
}
