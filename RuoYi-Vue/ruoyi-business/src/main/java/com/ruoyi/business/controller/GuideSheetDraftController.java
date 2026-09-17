package com.ruoyi.business.controller;

import com.ruoyi.business.domain.dto.GuideSheetAiGenerateRequest;
import com.ruoyi.business.domain.dto.GuideSheetBeginnerAssembleRequest;
import com.ruoyi.business.domain.dto.GuideSheetDraftCompleteRequest;
import com.ruoyi.business.domain.dto.GuideSheetDraftSaveRequest;
import com.ruoyi.business.domain.vo.GuideSheetDraftVo;
import com.ruoyi.business.domain.vo.GuideSheetFormJsonVo;
import com.ruoyi.business.service.BeginnerGuideSheetAssemblerService;
import com.ruoyi.business.service.GuideSheetAiContentService;
import com.ruoyi.business.service.GuideSheetDraftService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 新手向导的草稿、表单组装和智能内容接口。
 */
@RestController
@RequestMapping("/business/guide-sheet")
public class GuideSheetDraftController extends BaseController
{
    private final GuideSheetDraftService draftService;
    private final BeginnerGuideSheetAssemblerService assemblerService;
    private final GuideSheetAiContentService aiContentService;

    public GuideSheetDraftController(GuideSheetDraftService draftService,
                                     BeginnerGuideSheetAssemblerService assemblerService,
                                     GuideSheetAiContentService aiContentService)
    {
        this.draftService = draftService;
        this.assemblerService = assemblerService;
        this.aiContentService = aiContentService;
    }

    @PreAuthorize("@ss.hasAnyPermi('business:guideSheet:add,business:guideSheet:edit')")
    @GetMapping("/draft/{draftKey}")
    public AjaxResult restore(@PathVariable String draftKey)
    {
        GuideSheetDraftVo draft = draftService.restore(SecurityUtils.getUserId(), draftKey);
        return success().put(AjaxResult.DATA_TAG, draft);
    }

    @PreAuthorize("@ss.hasAnyPermi('business:guideSheet:add,business:guideSheet:edit')")
    @PutMapping("/draft")
    public AjaxResult save(@RequestBody GuideSheetDraftSaveRequest request)
    {
        return success(draftService.save(
                SecurityUtils.getUserId(), SecurityUtils.getUsername(), request));
    }

    @PreAuthorize("@ss.hasAnyPermi('business:guideSheet:add,business:guideSheet:edit')")
    @PostMapping("/draft/{draftKey}/complete")
    public AjaxResult complete(@PathVariable String draftKey,
                               @RequestBody GuideSheetDraftCompleteRequest request)
    {
        if (request == null)
        {
            throw new ServiceException("草稿版本不能为空");
        }
        return success(draftService.complete(SecurityUtils.getUserId(), SecurityUtils.getUsername(),
                draftKey, request.getRevision()));
    }

    @PreAuthorize("@ss.hasAnyPermi('business:guideSheet:add,business:guideSheet:edit')")
    @PostMapping("/beginner/assemble")
    public AjaxResult assemble(@RequestBody GuideSheetBeginnerAssembleRequest request)
    {
        if (request == null)
        {
            throw new ServiceException("请选择教学结构或教学模块");
        }
        String formJson = assemblerService.assemble(
                request.getPreset(), request.getModules(), request.getExistingFormJson());
        return success(new GuideSheetFormJsonVo(formJson));
    }

    @PreAuthorize("@ss.hasAnyPermi('business:guideSheet:add,business:guideSheet:edit')")
    @PostMapping("/ai/generate")
    public AjaxResult generateContent(@RequestBody GuideSheetAiGenerateRequest request)
    {
        return success(aiContentService.generate(SecurityUtils.getUserId(), request));
    }
}
