package com.ruoyi.business.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.service.QuestionImportService;
import com.ruoyi.common.core.domain.AjaxResult;

@RestController
@RequestMapping("/business/question/import-batches")
@PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher') and @ss.hasPermi('business:question:import')")
public class QuestionImportController {
    @Autowired private QuestionImportService service;
    @PostMapping public AjaxResult upload(@RequestParam("file") MultipartFile file,@RequestParam String requestId,
            @RequestParam(defaultValue="false") boolean allowDuplicate) throws IOException {return AjaxResult.success(service.upload(file,requestId,allowDuplicate));}
    @GetMapping public AjaxResult recent(){return AjaxResult.success(service.recent());}
    @GetMapping("/{id}") public AjaxResult detail(@PathVariable Long id){return AjaxResult.success(service.detail(id));}
    @PatchMapping("/{id}/items/{itemId}") public AjaxResult patch(@PathVariable Long id,@PathVariable Long itemId,@RequestBody QuestionImportService.Patch request){return AjaxResult.success(service.patch(id,itemId,request));}
    @PostMapping("/{id}/review") public AjaxResult review(@PathVariable Long id,@RequestBody Revision request){return AjaxResult.success(service.reviewGlobal(id,request.expectedRevision));}
    @PostMapping("/{id}/confirm") public AjaxResult confirm(@PathVariable Long id,@RequestBody Revision request){return AjaxResult.success(service.confirm(id,request.expectedRevision));}
    @GetMapping("/{id}/result") public AjaxResult result(@PathVariable Long id){return AjaxResult.success(service.detail(id));}
    public static class Revision {public Long expectedRevision;}
}
