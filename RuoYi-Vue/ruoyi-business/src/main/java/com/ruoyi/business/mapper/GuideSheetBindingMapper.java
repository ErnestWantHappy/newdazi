package com.ruoyi.business.mapper;

import java.util.Date;
import com.ruoyi.business.domain.BizLessonGuideSheetBinding;
import org.apache.ibatis.annotations.Param;

public interface GuideSheetBindingMapper
{
    BizLessonGuideSheetBinding selectByBindingId(Long bindingId);

    BizLessonGuideSheetBinding selectCurrentByLessonId(Long lessonId);

    BizLessonGuideSheetBinding selectEnabledByLessonId(Long lessonId);

    int countCurrentByLessonId(Long lessonId);

    int countByLessonId(Long lessonId);

    int insertBinding(BizLessonGuideSheetBinding binding);

    int updateEnabled(@Param("bindingId") Long bindingId,
                      @Param("enabled") String enabled,
                      @Param("updateBy") String updateBy,
                      @Param("updateTime") Date updateTime);

    int archiveCurrentByLessonId(@Param("lessonId") Long lessonId,
                                 @Param("updateBy") String updateBy,
                                 @Param("archivedTime") Date archivedTime);
}
