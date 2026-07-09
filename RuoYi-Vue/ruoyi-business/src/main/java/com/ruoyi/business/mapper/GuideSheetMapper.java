package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.business.domain.BizGuideSheet;

public interface GuideSheetMapper
{
    public List<BizGuideSheet> selectBizGuideSheetList(BizGuideSheet bizGuideSheet);

    public BizGuideSheet selectBizGuideSheetBySheetId(Long sheetId);

    public int insertBizGuideSheet(BizGuideSheet bizGuideSheet);

    public int updateBizGuideSheet(BizGuideSheet bizGuideSheet);

    public int deleteBizGuideSheetBySheetId(Long sheetId);

    public int deleteBizGuideSheetBySheetIds(Long[] sheetIds);

    public List<Map<String, Object>> selectCreatorList(Long deptId);
}
