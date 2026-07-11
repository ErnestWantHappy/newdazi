package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizGuideSheetProgress;
import com.ruoyi.business.domain.vo.GuideSheetProgressVo;
import org.apache.ibatis.annotations.Param;

public interface GuideSheetProgressMapper
{
    public List<GuideSheetProgressVo> selectBySheetAndClass(@Param("sheetId") Long sheetId,
                                                              @Param("entryYear") String entryYear,
                                                              @Param("classCode") String classCode);

    public List<GuideSheetProgressVo> selectBySheetId(@Param("sheetId") Long sheetId);

    /**
     * 获取班级全部学生进度（含未开始的学生），通过 biz_guide_sheet_assignment + biz_student LEFT JOIN progress
     */
    public List<GuideSheetProgressVo> selectFullProgressBySheetAndClass(@Param("sheetId") Long sheetId,
                                                                         @Param("entryYear") String entryYear,
                                                                         @Param("classCode") String classCode);

    /**
     * 获取全部班级学生进度（含未开始的学生），通过 biz_guide_sheet_assignment + biz_student LEFT JOIN progress
     */
    public List<GuideSheetProgressVo> selectFullProgressBySheetId(@Param("sheetId") Long sheetId);

    public BizGuideSheetProgress selectBySheetAndStudent(@Param("sheetId") Long sheetId,
                                                           @Param("studentId") Long studentId);

    public int insertOrUpdate(BizGuideSheetProgress progress);

    public int deleteBySheetId(Long sheetId);
}
