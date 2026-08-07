package com.ruoyi.business.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.ruoyi.business.domain.BizScoringItem;

/**
 * 将百分比评分项稳定折算为题目绝对分值。
 */
public final class ScoringWeightAllocator
{
    private ScoringWeightAllocator()
    {
    }

    /**
     * 使用最大余数法分配分值，避免逐项四舍五入造成总分缺失。
     *
     * @return 与评分项顺序一致的绝对满分；配置无效时返回空列表
     */
    public static List<Integer> allocate(List<BizScoringItem> items, int questionScore)
    {
        if (items == null || items.isEmpty() || questionScore < 0)
        {
            return Collections.emptyList();
        }

        int totalWeight = 0;
        for (BizScoringItem item : items)
        {
            if (item == null || item.getItemScore() == null || item.getItemScore() < 0)
            {
                return Collections.emptyList();
            }
            totalWeight += item.getItemScore();
        }
        if (totalWeight != 100)
        {
            return Collections.emptyList();
        }

        List<Integer> maxScores = new ArrayList<Integer>();
        List<Integer> remainders = new ArrayList<Integer>();
        List<Integer> indexes = new ArrayList<Integer>();
        int allocated = 0;
        for (int i = 0; i < items.size(); i++)
        {
            long weightedScore = (long) items.get(i).getItemScore() * questionScore;
            int maxScore = (int) (weightedScore / 100L);
            maxScores.add(maxScore);
            remainders.add((int) (weightedScore % 100L));
            indexes.add(i);
            allocated += maxScore;
        }

        Collections.sort(indexes, new Comparator<Integer>()
        {
            @Override
            public int compare(Integer left, Integer right)
            {
                int remainderCompare = Integer.compare(remainders.get(right), remainders.get(left));
                return remainderCompare != 0 ? remainderCompare : Integer.compare(left, right);
            }
        });
        for (int i = 0; i < questionScore - allocated; i++)
        {
            int index = indexes.get(i);
            maxScores.set(index, maxScores.get(index) + 1);
        }
        return maxScores;
    }
}
