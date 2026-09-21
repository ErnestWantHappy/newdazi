package com.ruoyi.business.controller;

import java.nio.file.Files;
import javax.servlet.http.HttpServletResponse;
import com.ruoyi.business.domain.dto.PracticalInsightV3.Request;
import com.ruoyi.business.service.PracticalInsightReportService;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/business/teacher/grading/insight-v3")
@PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('admin')")
public class PracticalInsightController
{
    @Autowired private PracticalInsightReportService reports;
    @PostMapping("/latest") public AjaxResult latest(@RequestBody Request request) throws Exception { return AjaxResult.success(reports.latest(request)); }
    @PostMapping public AjaxResult create(@RequestBody Request request) throws Exception { return AjaxResult.success(reports.create(request)); }
    @GetMapping("/{id}") public AjaxResult get(@PathVariable Long id) throws Exception { return AjaxResult.success(reports.get(id)); }
    @PostMapping("/{id}/cancel") public AjaxResult cancel(@PathVariable Long id) throws Exception { return AjaxResult.success(reports.cancel(id)); }
    @PostMapping("/{id}/retry") public AjaxResult retry(@PathVariable Long id) throws Exception { return AjaxResult.success(reports.retry(id)); }
    @GetMapping("/{id}/students/{studentId}") public AjaxResult student(@PathVariable Long id, @PathVariable Long studentId) throws Exception
    { return AjaxResult.success(reports.student(id, studentId)); }
    @GetMapping("/{id}/students/{studentId}/pages/{page}") public void page(@PathVariable Long id, @PathVariable Long studentId,
            @PathVariable int page, HttpServletResponse response) throws Exception
    {
        java.io.File file = reports.page(id, studentId, page);
        response.setContentType("image/jpeg"); response.setHeader("Cache-Control", "no-store");
        Files.copy(file.toPath(), response.getOutputStream());
    }
}
