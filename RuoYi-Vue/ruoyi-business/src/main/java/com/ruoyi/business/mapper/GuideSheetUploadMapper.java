package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizGuideSheetUpload;
import org.apache.ibatis.annotations.Param;

public interface GuideSheetUploadMapper
{
    public List<BizGuideSheetUpload> selectBySheetId(Long sheetId);

    public List<BizGuideSheetUpload> selectBySheetAndClass(@Param("sheetId") Long sheetId,
                                                             @Param("classCode") String classCode);

    public int insertBizGuideSheetUpload(BizGuideSheetUpload upload);

    public int deleteBySheetId(Long sheetId);
}
