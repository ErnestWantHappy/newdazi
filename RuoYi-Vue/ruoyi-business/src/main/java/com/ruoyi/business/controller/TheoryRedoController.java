package com.ruoyi.business.controller;

import com.ruoyi.business.domain.dto.TheoryRedoRequest;
import com.ruoyi.business.service.TheoryRedoService;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/business/score/theory-redo")
@PreAuthorize("@ss.hasPermi('business:score:list') and (@ss.hasRole('teacher') or @ss.hasRole('admin'))")
public class TheoryRedoController
{
    @Autowired private TheoryRedoService service;

    @GetMapping
    public AjaxResult context(@RequestParam Long lessonId, @RequestParam String entryYear, @RequestParam String classCode)
    {
        return AjaxResult.success(service.context(lessonId, entryYear, classCode));
    }

    @PostMapping
    public AjaxResult reset(@RequestBody TheoryRedoRequest request)
    {
        return AjaxResult.success().put("changedCount", service.reset(request));
    }

    @PostMapping("/typing")
    public AjaxResult resetTyping(@RequestBody TheoryRedoRequest request)
    {
        return AjaxResult.success().put("changedCount", service.resetTyping(request));
    }
}
