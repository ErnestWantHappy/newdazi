package com.ruoyi.business.domain.vo;

/**
 * 区域抽测操作题评分项。
 */
public class CountyExamScoringItemVo
{
    private Long itemId;

    private Long questionId;

    private String itemName;

    /** 题库配置的百分比权重。 */
    private Integer weightPercent;

    /** 按本场抽测题目满分折算后的分项满分。 */
    private Integer maxScore;

    private Integer orderNum;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public Long getQuestionId()
    {
        return questionId;
    }

    public void setQuestionId(Long questionId)
    {
        this.questionId = questionId;
    }

    public String getItemName()
    {
        return itemName;
    }

    public void setItemName(String itemName)
    {
        this.itemName = itemName;
    }

    public Integer getWeightPercent()
    {
        return weightPercent;
    }

    public void setWeightPercent(Integer weightPercent)
    {
        this.weightPercent = weightPercent;
    }

    public Integer getMaxScore()
    {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore)
    {
        this.maxScore = maxScore;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }
}
