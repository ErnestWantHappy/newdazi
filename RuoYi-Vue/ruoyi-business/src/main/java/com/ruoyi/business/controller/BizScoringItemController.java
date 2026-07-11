package com.ruoyi.business.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.business.domain.BizScoringItem;
import com.ruoyi.business.mapper.BizScoringItemMapper;

/**
 * 操作题评分项管理 Controller
 * 
 * @author ruoyi
 * @date 2026-01-07
 */
@RestController
@RequestMapping("/business/scoring/item")
public class BizScoringItemController extends BaseController
{
    @Autowired
    private BizScoringItemMapper scoringItemMapper;

    /**
     * 获取题目的评分项列表
     */
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) Long lessonId, @RequestParam Long questionId)
    {
        List<BizScoringItem> list = scoringItemMapper.selectItemsByQuestion(questionId);
        return AjaxResult.success(list);
    }

    /**
     * 获取评分项详细信息
     */
    @GetMapping("/{itemId}")
    public AjaxResult getInfo(@PathVariable Long itemId)
    {
        return AjaxResult.success(scoringItemMapper.selectBizScoringItemByItemId(itemId));
    }

    /**
     * 新增评分项
     */
    @PostMapping
    public AjaxResult add(@RequestBody BizScoringItem bizScoringItem)
    {
        return toAjax(scoringItemMapper.insertBizScoringItem(bizScoringItem));
    }

    /**
     * 修改评分项
     */
    @PutMapping
    public AjaxResult edit(@RequestBody BizScoringItem bizScoringItem)
    {
        return toAjax(scoringItemMapper.updateBizScoringItem(bizScoringItem));
    }

    /**
     * 删除评分项
     */
    @DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds)
    {
        return toAjax(scoringItemMapper.deleteBizScoringItemByItemIds(itemIds));
    }
    
    /**
     * 批量保存评分项（先删后增）
     */
    @PostMapping("/batch")
    public AjaxResult batchSave(@RequestParam(required = false) Long lessonId, 
                                 @RequestParam Long questionId,
                                 @RequestBody List<BizScoringItem> items)
    {
        // 先删除原有评分项
        scoringItemMapper.deleteBizScoringItemByQuestion(questionId);
        
        // 批量新增
        int order = 0;
        for (BizScoringItem item : items) {
            item.setQuestionId(questionId);
            item.setOrderNum(order++);
            scoringItemMapper.insertBizScoringItem(item);
        }
        
        return AjaxResult.success("评分项保存成功");
    }

}
