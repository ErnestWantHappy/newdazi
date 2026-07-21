package com.ruoyi.business.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.ruoyi.business.domain.BizGuideSheet;
import org.apache.ibatis.annotations.Param;

public interface GuideSheetMapper
{
    List<BizGuideSheet> selectBizGuideSheetList(BizGuideSheet guideSheet);

    BizGuideSheet selectBizGuideSheetBySheetId(Long sheetId);

    int insertBizGuideSheet(BizGuideSheet guideSheet);

    /**
     * 按版本更新模板，返回0表示模板已归档或发生并发修改。
     */
    int updateBizGuideSheet(BizGuideSheet guideSheet);

    int archiveBySheetId(@Param("sheetId") Long sheetId,
                         @Param("updateBy") String updateBy,
                         @Param("updateTime") Date updateTime);

    List<Map<String, Object>> selectCreatorList(Long countyDeptId);
}
