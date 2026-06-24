package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizGuideSheet;
import com.ruoyi.business.domain.vo.GuideSheetProgressVo;
import com.ruoyi.business.domain.vo.GuideSheetVo;

public interface IGuideSheetService
{
    public GuideSheetVo selectGuideSheetDetail(Long sheetId);

    public BizGuideSheet getBySheetId(Long sheetId);

    public List<BizGuideSheet> selectBizGuideSheetList(BizGuideSheet bizGuideSheet);

    public int insertBizGuideSheet(BizGuideSheet bizGuideSheet);

    public int updateBizGuideSheet(BizGuideSheet bizGuideSheet);

    public int deleteBizGuideSheetBySheetIds(Long[] sheetIds);

    public GuideSheetVo saveGuideSheetDetail(GuideSheetVo vo);

    public int publishGuideSheet(Long sheetId);

    public int closeGuideSheet(Long sheetId);

    public GuideSheetVo getStudentGuideSheet(Long deptId, String entryYear, String classCode);

    public List<GuideSheetProgressVo> getProgress(Long sheetId, String classCode);
}
