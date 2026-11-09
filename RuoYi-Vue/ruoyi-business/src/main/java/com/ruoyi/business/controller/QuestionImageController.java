package com.ruoyi.business.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.service.QuestionImageService;
import com.ruoyi.common.core.domain.AjaxResult;

@RestController
@RequestMapping("/business/question-images")
public class QuestionImageController {
    @Autowired private QuestionImageService images;
    @PostMapping
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher') and (@ss.hasPermi('business:question:add') or @ss.hasPermi('business:question:edit') or @ss.hasPermi('business:question:import'))")
    public AjaxResult upload(@RequestParam("file") MultipartFile file) throws IOException { return AjaxResult.success(images.upload(file)); }

    @GetMapping("/{id}/content")
    public void content(@PathVariable Long id, @RequestParam(required=false) Long questionId,
            @RequestParam(required=false) Long lessonId, @RequestParam(required=false) Long examId,
            @RequestParam(required=false) Long batchId, HttpServletResponse response) throws IOException {
        Map<String,Object> row=images.readable(id,questionId,lessonId,examId,batchId);
        Path file=images.resolve(row);
        if (!Files.isRegularFile(file)) { response.setStatus(404);return; }
        response.setContentType(String.valueOf(row.get("mime_type")));
        response.setHeader("X-Content-Type-Options","nosniff");
        response.setHeader("Cache-Control","private, no-store");
        Files.copy(file,response.getOutputStream());
    }
}
