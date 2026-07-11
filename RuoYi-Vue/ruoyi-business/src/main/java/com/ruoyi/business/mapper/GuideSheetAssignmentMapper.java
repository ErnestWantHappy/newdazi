package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizGuideSheetAssignment;
import org.apache.ibatis.annotations.Param;

public interface GuideSheetAssignmentMapper
{
    public List<BizGuideSheetAssignment> selectBizGuideSheetAssignmentList(BizGuideSheetAssignment assignment);

    public List<String> selectClassCodesBySheetId(Long sheetId);

    public int insertBizGuideSheetAssignment(BizGuideSheetAssignment assignment);

    public int batchInsert(@Param("list") List<BizGuideSheetAssignment> assignments);

    public int deleteBySheetId(Long sheetId);

    public Long selectCurrentSheetByClass(@Param("entryYear") String entryYear,
                                          @Param("classCode") String classCode,
                                          @Param("deptId") Long deptId);
}
