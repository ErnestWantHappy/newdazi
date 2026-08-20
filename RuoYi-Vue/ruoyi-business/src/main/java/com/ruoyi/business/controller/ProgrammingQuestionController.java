package com.ruoyi.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.ProgrammingQuestionConfig;
import com.ruoyi.business.service.ProgrammingSubmissionService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;

/** 教师题库页的 Python 专属配置接口，沿用现有题库权限。 */
@RestController
@RequestMapping("/business/programming/question")
public class ProgrammingQuestionController extends BaseController {
    @Autowired private ProgrammingSubmissionService programmingSubmissionService;

    @PreAuthorize("@ss.hasPermi('business:question:query')")
    @GetMapping("/{questionId}")
    public AjaxResult get(@PathVariable Long questionId) {
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
}
