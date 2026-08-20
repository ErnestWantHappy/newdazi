package com.ruoyi.business.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.service.PythonPracticeService;
import com.ruoyi.business.service.PythonPracticeSubmissionWorker;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;

@RestController
@RequestMapping("/business/python-practice/student")
public class PythonPracticeStudentController extends BaseController {
    @Autowired private PythonPracticeService service;
    @Autowired private PythonPracticeSubmissionWorker submissionWorker;

    @PreAuthorize("@studentSs.isStudent()")
    @GetMapping("/overview")
    public AjaxResult overview() { return success(service.studentOverview(SecurityUtils.getUserId(), SecurityUtils.getDeptId())); }

    @PreAuthorize("@studentSs.isStudent()")
    @GetMapping("/question")
    public AjaxResult question(@RequestParam String sourceType, @RequestParam Long sourceId, @RequestParam Long questionId) { return success(service.studentQuestion(SecurityUtils.getUserId(), SecurityUtils.getDeptId(), sourceType, sourceId, questionId)); }

    @PreAuthorize("@studentSs.isStudent()")
    @PostMapping("/draft")
    public AjaxResult draft(@RequestBody Map<String, Object> request) { return success(service.saveDraft(SecurityUtils.getUserId(), (String) request.get("sourceType"), Long.valueOf(String.valueOf(request.get("sourceId"))), Long.valueOf(String.valueOf(request.get("questionId"))), (String) request.get("sourceCode"))); }

    @PreAuthorize("@studentSs.isStudent()")
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody Map<String, Object> request) { return success(service.submit(SecurityUtils.getUserId(), (String) request.get("sourceType"), Long.valueOf(String.valueOf(request.get("sourceId"))), Long.valueOf(String.valueOf(request.get("questionId"))), (String) request.get("sourceCode"), (String) request.get("submitType"), submissionWorker)); }
}
