package com.ruoyi.business.service;

import java.util.List;
import java.util.Map;

import com.ruoyi.business.domain.BizGuideSheet;
import com.ruoyi.business.domain.vo.GuideSheetProgressVo;
import com.ruoyi.business.domain.vo.GuideSheetVo;

public interface IGuideSheetService
{
    GuideSheetVo selectGuideSheetDetail(Long sheetId);

    BizGuideSheet getBySheetId(Long sheetId);

    List<BizGuideSheet> selectBizGuideSheetList(BizGuideSheet query);

    GuideSheetVo saveGuideSheetDetail(GuideSheetVo vo);

    int archiveGuideSheet(Long sheetId);

    List<GuideSheetProgressVo> getProgress(Long bindingId, Long deptId, String entryYear, String classCode);

    List<Map<String, Object>> getCreatorList();
}
