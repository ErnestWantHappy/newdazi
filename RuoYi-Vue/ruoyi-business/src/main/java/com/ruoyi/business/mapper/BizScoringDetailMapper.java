package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizScoringDetail;

/**
 * 操作题分项得分记录 Mapper接口
 * 
 * @author ruoyi
 * @date 2026-01-07
 */
public interface BizScoringDetailMapper 
{
    /**
     * 查询分项得分
     */
    BizScoringDetail selectBizScoringDetailByDetailId(Long detailId);

    /**
     * 查询答题记录的所有分项得分
     */
    List<BizScoringDetail> selectDetailsByAnswerId(Long answerId);

    /**
     * 新增分项得分
     */
    int insertBizScoringDetail(BizScoringDetail bizScoringDetail);

    /**
     * 修改分项得分
     */
    int updateBizScoringDetail(BizScoringDetail bizScoringDetail);

    /**
     * 批量保存/更新分项得分（使用 ON DUPLICATE KEY UPDATE）
     */
    int batchSaveOrUpdate(@Param("list") List<BizScoringDetail> list);

    /**
     * 删除分项得分
     */
    int deleteBizScoringDetailByDetailId(Long detailId);

    /**
     * 删除答题记录的所有分项得分
     */
    int deleteBizScoringDetailByAnswerId(Long answerId);
}
