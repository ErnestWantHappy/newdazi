package com.ruoyi.business.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.business.domain.BizScoringItem;
import com.ruoyi.business.mapper.BizScoringItemMapper;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 操作题评分项管理 Controller
 * 
 * @author ruoyi
 * @date 2026-01-07
 */
@RestController
@RequestMapping("/business/scoring/item")
@PreAuthorize("@ss.hasAnyRoles('teacher,admin,researcher')")
public class BizScoringItemController extends BaseController
{
    @Autowired
    private BizScoringItemMapper scoringItemMapper;

    @Autowired
    private BizQuestionMapper questionMapper;

    /**
     * 获取题目的评分项列表
     */
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) Long lessonId, @RequestParam Long questionId)
    {
        assertQuestionVisible(questionId);
        List<BizScoringItem> list = scoringItemMapper.selectItemsByQuestion(questionId);
        return AjaxResult.success(list);
    }

    /**
     * 获取评分项详细信息
     */
    @GetMapping("/{itemId}")
    public AjaxResult getInfo(@PathVariable Long itemId)
    {
        BizScoringItem item = scoringItemMapper.selectBizScoringItemByItemId(itemId);
        if (item == null) {
            throw new ServiceException("评分项不存在");
        }
        assertQuestionVisible(item.getQuestionId());
        return AjaxResult.success(item);
    }

    private void assertQuestionVisible(Long questionId) {
        BizQuestion question = questionMapper.selectBizQuestionByQuestionId(questionId);
        if (question == null) {
            throw new ServiceException("题目不存在");
        }
        Long userId = SecurityUtils.getUserId();
        boolean creator = userId.equals(question.getCreatorId())
                || (question.getCreatorId() == null
                && SecurityUtils.getUsername().equals(question.getCreateBy()));
        if (!SecurityUtils.isAdmin(userId) && !creator && !"Y".equalsIgnoreCase(question.getIsPublic())) {
            throw new ServiceException("无权查看该题目的评分项");
        }
    }
}
