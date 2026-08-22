package com.ruoyi.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.ProgrammingQuestionConfig;
import com.ruoyi.business.domain.dto.PythonQuestionImportRequest;
import com.ruoyi.business.service.ProgrammingSubmissionService;
import com.ruoyi.business.service.PythonQuestionImportService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;

/** 教师题库页的 Python 专属配置接口，沿用现有题库权限。 */
@RestController
@RequestMapping("/business/programming/question")
public class ProgrammingQuestionController extends BaseController {
    @Autowired private ProgrammingSubmissionService programmingSubmissionService;
    @Autowired private PythonQuestionImportService importService;

    /** 编辑接口只允许创建者或管理员读取完整测试点，避免预览接口与编辑接口混用。 */
    @PreAuthorize("@ss.hasPermi('business:question:edit')")
    @GetMapping("/{questionId}")
    public AjaxResult get(@PathVariable Long questionId) {
        Long userId = SecurityUtils.getUserId();
        boolean admin = SecurityUtils.isAdmin(userId);
        ProgrammingQuestionConfig config = programmingSubmissionService.getTeacherConfig(questionId, userId, admin);
        return success(config).put("testCases", programmingSubmissionService.getTeacherTestCases(questionId, userId, admin));
    }

    /** 题库和课程设计器预览只能读取公开样例，绝不返回隐藏测试点。 */
    @PreAuthorize("@ss.hasPermi('business:question:query')")
    @GetMapping("/{questionId}/preview")
    public AjaxResult preview(@PathVariable Long questionId) {
        Long userId = SecurityUtils.getUserId();
        boolean admin = SecurityUtils.isAdmin(userId);
        ProgrammingQuestionConfig config = programmingSubmissionService.getTeacherPreviewConfig(questionId, userId, admin);
        return success(config).put("testCases", programmingSubmissionService.getTeacherPreviewCases(questionId, userId, admin));
    }

    @PreAuthorize("@ss.hasPermi('business:question:edit')")
    @PutMapping("/{questionId}")
    public AjaxResult save(@PathVariable Long questionId, @RequestBody ProgrammingQuestionConfig config) {
        Long userId = SecurityUtils.getUserId();
        programmingSubmissionService.saveTeacherConfig(questionId, config, userId, SecurityUtils.isAdmin(userId), SecurityUtils.getUsername());
        return success("Python 编程题配置已保存");
    }

    @PreAuthorize("@ss.hasPermi('business:question:edit')")
    @PostMapping("/{questionId}/validate")
    public AjaxResult validate(@PathVariable Long questionId) {
        Long userId = SecurityUtils.getUserId();
        return success(programmingSubmissionService.validateTeacherQuestion(
            questionId, userId, SecurityUtils.isAdmin(userId), SecurityUtils.getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('business:question:import')")
    @PostMapping("/import/preview")
    public AjaxResult previewImport(@RequestBody PythonQuestionImportRequest request) {
        return success(importService.preview(request, SecurityUtils.getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('business:question:import')")
    @PostMapping("/import/confirm")
    public AjaxResult confirmImport(@RequestBody java.util.Map<String, String> request) {
        return success(importService.confirm(request == null ? null : request.get("confirmToken"),
            SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }
}
