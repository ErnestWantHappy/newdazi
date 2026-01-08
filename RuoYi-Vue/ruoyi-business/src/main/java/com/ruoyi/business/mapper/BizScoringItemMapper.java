package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizScoringItem;

/**
 * 操作题评分项定义 Mapper接口
 * 
 * @author ruoyi
 * @date 2026-01-07
 */
public interface BizScoringItemMapper 
{
    /**
     * 查询评分项
     */
    BizScoringItem selectBizScoringItemByItemId(Long itemId);

    /**
     * 查询评分项列表（按课程和题目）
     */
    List<BizScoringItem> selectBizScoringItemList(BizScoringItem bizScoringItem);

    /**
     * 按题目查询评分项列表
     */
    List<BizScoringItem> selectItemsByQuestion(@Param("questionId") Long questionId);

    /**
     * 新增评分项
     */
    int insertBizScoringItem(BizScoringItem bizScoringItem);

    /**
     * 修改评分项
     */
    int updateBizScoringItem(BizScoringItem bizScoringItem);

    /**
     * 删除评分项
     */
    int deleteBizScoringItemByItemId(Long itemId);

    /**
     * 批量删除评分项
     */
    int deleteBizScoringItemByItemIds(Long[] itemIds);

    /**
     * 删除题目的所有评分项
     */
    int deleteBizScoringItemByQuestion(@Param("questionId") Long questionId);
}
