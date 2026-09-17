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
    public AjaxResult draft(@RequestBody Map<String, Object> request) {
        Long sourceId = requiredLong(request, "sourceId");
        Long questionId = requiredLong(request, "questionId");
        String sourceType = strOrNull(request.get("sourceType"));
        if (sourceId == null || questionId == null || sourceType == null) {
            return error("参数不完整：sourceType/sourceId/questionId 必须为有效值");
        }
        return success(service.saveDraft(SecurityUtils.getUserId(), sourceType, sourceId, questionId, strOrNull(request.get("sourceCode"))));
    }

    @PreAuthorize("@studentSs.isStudent()")
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody Map<String, Object> request) {
        Long sourceId = requiredLong(request, "sourceId");
        Long questionId = requiredLong(request, "questionId");
        String sourceType = strOrNull(request.get("sourceType"));
        if (sourceId == null || questionId == null || sourceType == null) {
            return error("参数不完整：sourceType/sourceId/questionId 必须为有效值");
        }
        return success(service.submit(SecurityUtils.getUserId(), sourceType, sourceId, questionId,
            strOrNull(request.get("sourceCode")), strOrNull(request.get("submitType")),
            strOrNull(request.get("customInput")), submissionWorker));
    }

    /** 缺失或非法数字统一返回 null 并由入口报参数错误，避免 Long.valueOf("null") 直接 500 */
    private Long requiredLong(Map<String, Object> request, String key) {
        Object value = request == null ? null : request.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        String text = value == null ? null : String.valueOf(value).trim();
        if (text == null || text.isEmpty() || "null".equals(text)) {
            return null;
        }
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String strOrNull(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return "null".equals(text) ? null : text;
    }
}
