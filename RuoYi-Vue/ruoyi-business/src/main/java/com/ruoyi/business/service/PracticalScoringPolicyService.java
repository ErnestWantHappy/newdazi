package com.ruoyi.business.service;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.BizScoringDetail;
import com.ruoyi.business.domain.BizScoringItem;
import com.ruoyi.business.domain.vo.PracticalScoringItemVo;
import com.ruoyi.business.utils.ScoringWeightAllocator;
import com.ruoyi.common.exception.ServiceException;

/**
 * 普通课程操作题的统一评分规则。
 */
@Service
public class PracticalScoringPolicyService
{
    public static final String MODE_NUMERIC = "NUMERIC";
    public static final String MODE_STAR_TOTAL = "STAR_TOTAL";
    public static final String MODE_STAR_ITEM = "STAR_ITEM";

    public List<PracticalScoringItemVo> buildScoringItems(
            List<BizScoringItem> items, int questionScore)
    {
        List<PracticalScoringItemVo> result = new ArrayList<PracticalScoringItemVo>();
        if (items == null || items.isEmpty())
        {
            return result;
        }

        List<Integer> maxScores = ScoringWeightAllocator.allocate(items, questionScore);
        if (maxScores.size() != items.size())
        {
            return result;
        }

        for (int i = 0; i < items.size(); i++)
        {
            BizScoringItem item = items.get(i);
            PracticalScoringItemVo vo = new PracticalScoringItemVo();
            vo.setItemId(item.getItemId());
            vo.setQuestionId(item.getQuestionId());
            vo.setItemName(item.getItemName());
            vo.setWeightPercent(item.getItemScore());
            vo.setMaxScore(maxScores.get(i));
            vo.setOrderNum(item.getOrderNum());
            result.add(vo);
        }
        return result;
    }

    /**
     * 校验分项并返回服务端确认的最终分数。
     */
    public int resolveFinalScore(Integer requestedScore,
                                 int questionScore,
                                 List<PracticalScoringItemVo> scoringItems,
                                 List<BizScoringDetail> scoringDetails)
    {
        return resolveFinalScore(MODE_NUMERIC, requestedScore, null,
                questionScore, scoringItems, scoringDetails);
    }

    /**
     * 星级结果必须由服务端按快照满分重算；前端分数只用于发现陈旧或篡改请求。
     */
    public int resolveFinalScore(String mode,
                                 Integer requestedScore,
                                 Integer starCount,
                                 int questionScore,
                                 List<PracticalScoringItemVo> scoringItems,
                                 List<BizScoringDetail> scoringDetails)
    {
        if (requestedScore == null)
        {
            throw new ServiceException("分数不能为空");
        }
        if (requestedScore < 0 || requestedScore > questionScore)
        {
            throw new ServiceException("分数必须在0到题目满分之间");
        }
        String normalizedMode = mode == null || mode.trim().isEmpty()
                ? MODE_NUMERIC : mode.trim().toUpperCase();
        if (MODE_STAR_TOTAL.equals(normalizedMode))
        {
            if (scoringDetails != null && !scoringDetails.isEmpty())
            {
                throw new ServiceException("整题评星不能同时提交分项明细");
            }
            int calculated = calculateStarScore(questionScore, starCount);
            if (requestedScore != calculated)
            {
                throw new ServiceException("星级折算分数已变化，请刷新后重试");
            }
            return calculated;
        }
        if (MODE_STAR_ITEM.equals(normalizedMode))
        {
            return resolveStarItemScore(requestedScore, scoringItems, scoringDetails);
        }
        if (!MODE_NUMERIC.equals(normalizedMode))
        {
            throw new ServiceException("评分模式无效");
        }
        if (scoringDetails == null || scoringDetails.isEmpty())
        {
            return requestedScore;
        }
        if (scoringItems == null || scoringItems.isEmpty())
        {
            throw new ServiceException("当前题目没有有效评分项，请使用直接打分");
        }
        if (scoringDetails.size() != scoringItems.size())
        {
            throw new ServiceException("请完成全部评分项");
        }

        Map<Long, Integer> maxScores = new HashMap<Long, Integer>();
        for (PracticalScoringItemVo item : scoringItems)
        {
            maxScores.put(item.getItemId(), item.getMaxScore());
        }

        Set<Long> submittedItemIds = new HashSet<Long>();
        int detailTotal = 0;
        for (BizScoringDetail detail : scoringDetails)
        {
            if (detail == null || detail.getItemId() == null || detail.getScore() == null)
            {
                throw new ServiceException("评分项参数不完整");
            }
            Integer maxScore = maxScores.get(detail.getItemId());
            if (maxScore == null || !submittedItemIds.add(detail.getItemId()))
            {
                throw new ServiceException("评分项不属于当前题目或存在重复");
            }
            if (detail.getScore() < 0 || detail.getScore() > maxScore)
            {
                throw new ServiceException("分项得分超出允许范围");
            }
            detailTotal += detail.getScore();
        }
        if (detailTotal != requestedScore)
        {
            throw new ServiceException("分项得分合计必须与总分一致");
        }
        return detailTotal;
    }

    private int resolveStarItemScore(Integer requestedScore,
                                     List<PracticalScoringItemVo> scoringItems,
                                     List<BizScoringDetail> scoringDetails)
    {
        if (scoringItems == null || scoringItems.isEmpty())
        {
            throw new ServiceException("当前题目没有有效评分项，不能使用逐项评星");
        }
        if (scoringDetails == null || scoringDetails.size() != scoringItems.size())
        {
            throw new ServiceException("请完成全部评分项");
        }
        Map<Long, Integer> maxScores = new HashMap<Long, Integer>();
        for (PracticalScoringItemVo item : scoringItems)
        {
            maxScores.put(item.getItemId(), item.getMaxScore());
        }
        Set<Long> submittedItemIds = new HashSet<Long>();
        int total = 0;
        for (BizScoringDetail detail : scoringDetails)
        {
            if (detail == null || detail.getItemId() == null)
            {
                throw new ServiceException("评分项参数不完整");
            }
            Integer maxScore = maxScores.get(detail.getItemId());
            if (maxScore == null || !submittedItemIds.add(detail.getItemId()))
            {
                throw new ServiceException("评分项不属于当前题目或存在重复");
            }
            int calculated = calculateStarScore(maxScore, detail.getStarCount());
            if (detail.getScore() == null || detail.getScore() != calculated)
            {
                throw new ServiceException("评分项星级折算分数已变化，请刷新后重试");
            }
            detail.setScore(calculated);
            total += calculated;
        }
        if (total != requestedScore)
        {
            throw new ServiceException("评分项星级合计与总分不一致");
        }
        return total;
    }

    public int calculateStarScore(int maxScore, Integer starCount)
    {
        if (maxScore < 0)
        {
            throw new ServiceException("评分满分不能为负数");
        }
        if (starCount == null || starCount < 0 || starCount > 5)
        {
            throw new ServiceException("星级必须是0到5之间的整数");
        }
        return BigDecimal.valueOf(maxScore)
                .multiply(BigDecimal.valueOf(starCount))
                .divide(BigDecimal.valueOf(5), 0, RoundingMode.HALF_UP)
                .intValue();
    }
}
