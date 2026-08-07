package com.ruoyi.business.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.PracticalAiJob;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.domain.dto.PracticalAiJobRequest;
import com.ruoyi.business.domain.dto.TeacherAiConfigRequest;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.PracticalSubmissionVo;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.business.service.PracticalAiJobService;
import com.ruoyi.business.service.PracticalArtifactService;
import com.ruoyi.business.service.TeacherAiConfigService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/** 教师配置个人模型并创建、查看、控制班级 AI 建议任务。 */
@RestController
@RequestMapping("/business/teacher/grading/ai")
@PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('admin')")
public class PracticalAiGradingController extends BaseController
{
    @Autowired private TeacherAiConfigService configService;
    @Autowired private PracticalAiJobService jobService;
    @Autowired private GuideSheetAccessService accessService;
    @Autowired private BizLessonQuestionMapper lessonQuestionMapper;
    @Autowired private BizStudentAnswerMapper answerMapper;
    @Autowired private PracticalArtifactService artifactService;

    @GetMapping("/config")
    public AjaxResult config()
    {
        TeacherAiConfig config = configService.status(SecurityUtils.getUserId());
        Map<String, Object> status = new LinkedHashMap<String, Object>();
        status.put("configured", config != null);
        status.put("masterKeyConfigured", configService.isMasterKeyConfigured());
        status.put("providerCode", config == null ? "QWEN" : config.getProviderCode());
        status.put("modelName", config == null ? TeacherAiConfigService.DEFAULT_MODEL : config.getModelName());
        status.put("apiKeyHint", config == null ? null : config.getApiKeyHint());
        status.put("enabled", config != null && Boolean.TRUE.equals(config.getEnabled()));
        return AjaxResult.success(status);
    }

    @PutMapping("/config")
    public AjaxResult saveConfig(@RequestBody TeacherAiConfigRequest request)
    {
        TeacherAiConfig saved = configService.save(SecurityUtils.getUserId(), request);
        return AjaxResult.success("API Key 已加密保存", saved.getApiKeyHint());
    }

    @DeleteMapping("/config")
    public AjaxResult deleteConfig()
    {
        configService.delete(SecurityUtils.getUserId());
        return AjaxResult.success("AI 配置已删除");
    }

    @PostMapping("/config/test")
    public AjaxResult testConfig()
    {
        configService.testConnection(SecurityUtils.getUserId());
        return AjaxResult.success("API Key、网络和视觉模型连通正常");
    }

    @PostMapping("/jobs")
    public AjaxResult createJob(@RequestBody PracticalAiJobRequest request)
    {
        validateRequest(request);
        accessService.assertCanViewLessonClass(request.getLessonId(), request.getEntryYear(), request.getClassCode());
        assertPracticalQuestion(request.getLessonId(), request.getQuestionId());
        List<PracticalSubmissionVo> submissions = answerMapper.selectPracticalSubmissions(
                request.getLessonId(), request.getQuestionId(), request.getClassCode(),
                request.getEntryYear(), SecurityUtils.getDeptId());
        artifactService.enrichSubmissions(submissions);
        PracticalAiJob job = jobService.create(SecurityUtils.getUserId(), SecurityUtils.getDeptId(),
                request.getLessonId(), request.getQuestionId(), request.getEntryYear(),
                request.getClassCode(), submissions);
        return AjaxResult.success(job);
    }

    @GetMapping("/jobs/{jobId}")
    public AjaxResult job(@PathVariable Long jobId)
    {
        return AjaxResult.success(jobService.detail(jobId, SecurityUtils.getUserId()));
    }

    @PostMapping("/jobs/{jobId}/pause") public AjaxResult pause(@PathVariable Long jobId)
    { jobService.pause(jobId, SecurityUtils.getUserId()); return AjaxResult.success("任务已暂停"); }
    @PostMapping("/jobs/{jobId}/resume") public AjaxResult resume(@PathVariable Long jobId)
    { jobService.resume(jobId, SecurityUtils.getUserId()); return AjaxResult.success("任务已继续"); }
    @PostMapping("/jobs/{jobId}/cancel") public AjaxResult cancel(@PathVariable Long jobId)
    { jobService.cancel(jobId, SecurityUtils.getUserId()); return AjaxResult.success("已请求取消任务"); }
    @PostMapping("/jobs/{jobId}/retry-failed") public AjaxResult retry(@PathVariable Long jobId)
    { jobService.retryFailed(jobId, SecurityUtils.getUserId()); return AjaxResult.success("失败作品已重新入队"); }

    private void validateRequest(PracticalAiJobRequest request)
    {
        if (request == null || request.getLessonId() == null || request.getQuestionId() == null
                || StringUtils.isBlank(request.getEntryYear()) || StringUtils.isBlank(request.getClassCode()))
            throw new ServiceException("课程、班级和操作题不能为空");
        request.setEntryYear(request.getEntryYear().trim());
        request.setClassCode(request.getClassCode().trim());
    }

    private void assertPracticalQuestion(Long lessonId, Long questionId)
    {
        for (BizLessonQuestionDetailVo question : lessonQuestionMapper.selectDetailsByLessonId(lessonId))
            if (questionId.equals(question.getQuestionId()) && "practical".equals(question.getQuestionType())) return;
        throw new ServiceException("题目不属于当前课程或不是操作题");
    }
}
