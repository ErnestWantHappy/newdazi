package com.ruoyi.business.mapper;

import java.util.Date;

import com.ruoyi.business.domain.BizGuideSheetDraft;
import org.apache.ibatis.annotations.Param;

public interface GuideSheetDraftMapper
{
    BizGuideSheetDraft selectByOwnerAndKey(@Param("ownerId") Long ownerId,
                                           @Param("clientDraftKey") String clientDraftKey);

    int insertDraft(BizGuideSheetDraft draft);

    int updateDraftCas(@Param("ownerId") Long ownerId,
                       @Param("clientDraftKey") String clientDraftKey,
                       @Param("expectedRevision") Long expectedRevision,
                       @Param("sheetId") Long sheetId,
                       @Param("contentJson") String contentJson,
                       @Param("updateBy") String updateBy,
                       @Param("updateTime") Date updateTime);

    int reopenCompletedDraft(@Param("ownerId") Long ownerId,
                             @Param("clientDraftKey") String clientDraftKey,
                             @Param("sheetId") Long sheetId,
                             @Param("contentJson") String contentJson,
                             @Param("updateBy") String updateBy,
                             @Param("updateTime") Date updateTime);

    int completeDraftCas(@Param("ownerId") Long ownerId,
                         @Param("clientDraftKey") String clientDraftKey,
                         @Param("expectedRevision") Long expectedRevision,
                         @Param("updateBy") String updateBy,
                         @Param("completedTime") Date completedTime);
}
