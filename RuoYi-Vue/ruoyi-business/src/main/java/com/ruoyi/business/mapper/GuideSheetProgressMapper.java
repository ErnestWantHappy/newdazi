package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizGuideSheetProgress;
import com.ruoyi.business.domain.vo.GuideSheetProgressVo;
import org.apache.ibatis.annotations.Param;

public interface GuideSheetProgressMapper
{
    List<GuideSheetProgressVo> selectByBindingAndClass(@Param("bindingId") Long bindingId,
                                                       @Param("deptId") Long deptId,
                                                       @Param("entryYear") String entryYear,
                                                       @Param("classCode") String classCode);

    List<GuideSheetProgressVo> selectByBindingId(Long bindingId);

    /**
     * 通过课程指派补齐尚未开始填写的学生。
     */
    List<GuideSheetProgressVo> selectFullProgressByBindingAndClass(@Param("bindingId") Long bindingId,
                                                                   @Param("deptId") Long deptId,
                                                                   @Param("entryYear") String entryYear,
                                                                   @Param("classCode") String classCode);

    List<GuideSheetProgressVo> selectFullProgressByBindingId(Long bindingId);

    BizGuideSheetProgress selectByBindingAndStudent(@Param("bindingId") Long bindingId,
                                                     @Param("studentId") Long studentId);

    int insertOrUpdate(BizGuideSheetProgress progress);

    /**
     * 上传发生在首次自动保存之前时补齐看板起始行，已存在的数据不得被回退。
     */
    int insertStartedIfAbsent(BizGuideSheetProgress progress);
}
