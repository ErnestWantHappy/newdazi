package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 操作题评分项定义对象 biz_scoring_item
 * 
 * @author ruoyi
 * @date 2026-01-07
 */
public class BizScoringItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 评分项ID */
    private Long itemId;

    /** 题目ID */
    @Excel(name = "题目ID")
    private Long questionId;

    /** 评分项名称 */
    @Excel(name = "评分项名称")
    private String itemName;

    /** 该项满分 */
    @Excel(name = "该项满分")
    private Integer itemScore;

    /** 排序 */
    @Excel(name = "排序")
    private Integer orderNum;

    public void setItemId(Long itemId) 
    {
        this.itemId = itemId;
    }

    public Long getItemId() 
    {
        return itemId;
    }

    public void setQuestionId(Long questionId) 
    {
        this.questionId = questionId;
    }

    public Long getQuestionId() 
    {
        return questionId;
    }

    public void setItemName(String itemName) 
    {
        this.itemName = itemName;
    }

    public String getItemName() 
    {
        return itemName;
    }

    public void setItemScore(Integer itemScore) 
    {
        this.itemScore = itemScore;
    }

    public Integer getItemScore() 
    {
        return itemScore;
    }

    public void setOrderNum(Integer orderNum) 
    {
        this.orderNum = orderNum;
    }

    public Integer getOrderNum() 
    {
        return orderNum;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("itemId", getItemId())

            .append("questionId", getQuestionId())
            .append("itemName", getItemName())
            .append("itemScore", getItemScore())
            .append("orderNum", getOrderNum())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
