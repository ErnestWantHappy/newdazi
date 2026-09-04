package com.ruoyi.business.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.PracticalRubricSnapshot;

public interface PracticalRubricSnapshotMapper
{
    PracticalRubricSnapshot selectLatest(@Param("lessonId") Long lessonId,
                                          @Param("questionId") Long questionId);

    PracticalRubricSnapshot selectByVersionId(@Param("versionId") Long versionId);

    int insert(PracticalRubricSnapshot snapshot);
}
