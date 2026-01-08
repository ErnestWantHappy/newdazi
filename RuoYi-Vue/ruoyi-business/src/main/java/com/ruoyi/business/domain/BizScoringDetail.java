package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 操作题分项得分记录对象 biz_scoring_detail
 * 
 * @author ruoyi
 * @date 2026-01-07
 */
public class BizScoringDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long detailId;

    /** 答题记录ID */
    @Excel(name = "答题记录ID")
    private Long answerId;

    /** 评分项ID */
    @Excel(name = "评分项ID")
    private Long itemId;

    /** 得分 */
    @Excel(name = "得分")
    private Integer score;

    public void setDetailId(Long detailId) 
    {
        this.detailId = detailId;
    }

    public Long getDetailId() 
    {
        return detailId;
    }

    public void setAnswerId(Long answerId) 
    {
        this.answerId = answerId;
    }

    public Long getAnswerId() 
    {
        return answerId;
    }

    public void setItemId(Long itemId) 
    {
        this.itemId = itemId;
    }

    public Long getItemId() 
    {
        return itemId;
    }

    public void setScore(Integer score) 
    {
        this.score = score;
    }

    public Integer getScore() 
    {
        return score;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("detailId", getDetailId())
            .append("answerId", getAnswerId())
            .append("itemId", getItemId())
            .append("score", getScore())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
