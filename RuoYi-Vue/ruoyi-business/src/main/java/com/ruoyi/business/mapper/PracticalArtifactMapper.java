package com.ruoyi.business.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.PracticalArtifact;
import com.ruoyi.business.domain.PracticalAttachment;
import com.ruoyi.business.domain.PracticalQuestionMaterial;
import com.ruoyi.business.domain.PracticalSubmissionVersion;

/**
 * 操作题逻辑作品持久化。
 */
public interface PracticalArtifactMapper
{
    PracticalArtifact selectByContext(@Param("contextType") String contextType,
                                      @Param("contextId") Long contextId,
                                      @Param("studentId") Long studentId,
                                      @Param("questionId") Long questionId);

    PracticalArtifact selectByContextForUpdate(@Param("contextType") String contextType,
                                               @Param("contextId") Long contextId,
                                               @Param("studentId") Long studentId,
                                               @Param("questionId") Long questionId);

    int insertArtifact(PracticalArtifact artifact);

    int updateCurrentVersion(@Param("artifactId") Long artifactId,
                             @Param("currentVersionId") Long currentVersionId,
                             @Param("latestVersionNo") Integer latestVersionNo,
                             @Param("expectedLockVersion") Integer expectedLockVersion,
                             @Param("updateTime") Date updateTime);

    int insertVersion(PracticalSubmissionVersion version);

    int bindVersionAnswer(@Param("versionId") Long versionId,
                          @Param("sourceAnswerId") Long sourceAnswerId);

    PracticalSubmissionVersion selectVersionById(@Param("versionId") Long versionId);

    int supersedeVersion(@Param("versionId") Long versionId,
                         @Param("scoreSnapshot") Integer scoreSnapshot,
                         @Param("scoringDetailsJson") String scoringDetailsJson,
                         @Param("invalidatedTime") Date invalidatedTime);

    int deleteCurrentVersion(@Param("versionId") Long versionId,
                             @Param("scoreSnapshot") Integer scoreSnapshot,
                             @Param("scoringDetailsJson") String scoringDetailsJson,
                             @Param("invalidatedTime") Date invalidatedTime);

    int insertAttachment(PracticalAttachment attachment);

    List<PracticalAttachment> selectAttachmentsByVersion(@Param("versionId") Long versionId);

    PracticalAttachment selectAttachmentById(@Param("attachmentId") Long attachmentId);

    int claimAttachmentPreview(@Param("attachmentId") Long attachmentId,
                               @Param("claimedAt") Date claimedAt);

    int claimAttachmentNormalization(@Param("attachmentId") Long attachmentId,
                                     @Param("claimedAt") Date claimedAt);

    int updateAttachmentPreview(PracticalAttachment attachment);

    int updateAttachmentNormalization(PracticalAttachment attachment);

    PracticalAttachment selectReusableNormalization(@Param("sha256") String sha256,
                                                     @Param("rendererVersion") String rendererVersion,
                                                     @Param("excludeAttachmentId") Long excludeAttachmentId);

    int resetAttachmentNormalization(@Param("attachmentId") Long attachmentId,
                                     @Param("retryTime") Date retryTime);

    List<PracticalQuestionMaterial> selectMaterialsByQuestion(@Param("questionId") Long questionId);

    int deleteMaterialsByQuestion(@Param("questionId") Long questionId);

    int insertQuestionMaterial(PracticalQuestionMaterial material);
}
