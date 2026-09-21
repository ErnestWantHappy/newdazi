package com.ruoyi.business.controller;

import java.util.*;
import com.ruoyi.business.domain.dto.PaperImportRequest;
import com.ruoyi.business.service.*;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/business/teacher/grading")
@PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('admin')")
public class TeacherPaperImportController
{
    @Autowired private TeacherPaperSubmissionService imports;
    @Autowired private PracticalClassInsightService insights;
    @Autowired private PaperBatchImportService batches;

    @PostMapping("/paper-import/roster")
    public AjaxResult roster(@RequestBody PaperImportRequest request)
    {
        imports.requireQuestion(request);
        // 仅返回选人所需字段，不将学生档案整体暴露给前端。
        List<Map<String, Object>> result = new ArrayList<>();
        imports.roster(request).forEach(s -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("studentId", s.getStudentId()); row.put("studentNo", s.getStudentNo()); row.put("studentName", s.getStudentName());
            result.add(row);
        });
        return AjaxResult.success(result);
    }

    @PostMapping("/paper-import/upload")
    public AjaxResult upload(@RequestParam Long lessonId, @RequestParam Long questionId,
            @RequestParam String entryYear, @RequestParam String classCode, @RequestParam MultipartFile file,
            @RequestParam(defaultValue="false") boolean classPdf) throws Exception
    {
        PaperImportRequest request = new PaperImportRequest();
        request.lessonId = lessonId; request.questionId = questionId; request.entryYear = entryYear; request.classCode = classCode;
        return AjaxResult.success(classPdf ? batches.upload(request, file) : imports.upload(request, file));
    }

    @PostMapping("/paper-import/page-identity")
    public AjaxResult identity(@RequestBody PaperImportRequest request) throws Exception
    { return AjaxResult.success(batches.identity(request)); }

    @PostMapping("/paper-import/split")
    public AjaxResult split(@RequestBody PaperImportRequest request) throws Exception
    { return AjaxResult.success(batches.split(request)); }

    @PostMapping("/paper-import/submit")
    public AjaxResult submit(@RequestBody PaperImportRequest request) throws Exception
    {
        return AjaxResult.success(imports.submit(request));
    }

    @PostMapping("/class-insight")
    public AjaxResult insight(@RequestBody PaperImportRequest request) throws Exception
    {
        return AjaxResult.success(insights.snapshot(request));
    }

    @PostMapping("/class-insight/generate")
    public AjaxResult generate(@RequestBody PaperImportRequest request) throws Exception
    {
        Map<String, Object> result = insights.generate(insights.snapshot(request));
        if (!Objects.equals(result.get("fingerprint"), insights.snapshot(request).get("fingerprint")))
            throw new ServiceException("成绩已变化，请刷新学情后重新生成");
        return AjaxResult.success(result);
    }
}
