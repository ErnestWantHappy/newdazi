package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizGuideSheetUpload;
import org.apache.ibatis.annotations.Param;

public interface GuideSheetUploadMapper
{
    BizGuideSheetUpload selectByUploadId(Long uploadId);

    List<BizGuideSheetUpload> selectByBindingId(Long bindingId);

    List<BizGuideSheetUpload> selectByBindingAndClass(@Param("bindingId") Long bindingId,
                                                      @Param("deptId") Long deptId,
                                                      @Param("entryYear") String entryYear,
                                                      @Param("classCode") String classCode);

    int insertBizGuideSheetUpload(BizGuideSheetUpload upload);

    BizGuideSheetUpload selectByClientUploadId(@Param("bindingId") Long bindingId,
                                               @Param("studentId") Long studentId,
                                               @Param("clientUploadId") String clientUploadId);
}
