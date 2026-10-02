package com.ruoyi.business.controller;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.DataLabConfig;
import com.ruoyi.business.service.IDataLabService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 数据分析实验题（DATA_LAB）控制器
 */
@RestController
@RequestMapping("/business/dataLab")
public class DataLabController extends BaseController {

    @Autowired
    private IDataLabService dataLabService;

    // ==========================================
    // 教师端接口
    // ==========================================

    /**
     * 保存或修改实验题配置
     */
    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit') or @ss.hasPermi('business:question:add') or @ss.hasPermi('business:question:edit')")
    @PostMapping("/config")
    public AjaxResult saveConfig(@RequestBody DataLabConfig config) {
        return toAjax(dataLabService.saveConfig(config, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    /**
     * 获取实验题配置详情
     */
    @PreAuthorize("@ss.hasPermi('business:pythonPractice:query') or @ss.hasPermi('business:question:query')")
    @GetMapping("/config/{questionId}")
    public AjaxResult getConfig(@PathVariable Long questionId) {
        return success(dataLabService.getConfig(questionId));
    }

    /**
     * 上传 Excel 数据集
     */
    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit') or @ss.hasPermi('business:question:add') or @ss.hasPermi('business:question:edit')")
    @PostMapping("/uploadExcel")
    public AjaxResult uploadExcel(@RequestParam("file") MultipartFile file) {
        return success(dataLabService.uploadExcel(file, SecurityUtils.getUserId()));
    }

    /**
     * 查询某题单下该题的所有学生提交
     */
    @PreAuthorize("@ss.hasPermi('business:pythonPractice:query')")
    @GetMapping("/submissions")
    public AjaxResult getSubmissions(@RequestParam Long questionId,
                                     @RequestParam(required = false, defaultValue = "PLAN") String sourceType,
                                     @RequestParam(required = false) Long sourceId) {
        return success(dataLabService.getSubmissions(questionId, sourceType, sourceId));
    }

    /**
     * 查看学生提交详情
     */
    @PreAuthorize("@ss.hasPermi('business:pythonPractice:query')")
    @GetMapping("/submission/{submissionId}")
    public AjaxResult getSubmissionDetail(@PathVariable Long submissionId) {
        return success(dataLabService.getSubmissionDetail(submissionId));
    }

    /**
     * 教师评分与评语
     */
    @PreAuthorize("@ss.hasPermi('business:pythonPractice:edit')")
    @PutMapping("/submission/{submissionId}/score")
    public AjaxResult scoreSubmission(@PathVariable Long submissionId, @RequestBody Map<String, Object> body) {
        Integer score = body.get("score") == null ? null : Integer.valueOf(String.valueOf(body.get("score")));
        String scoreComment = body.get("scoreComment") == null ? null : String.valueOf(body.get("scoreComment"));
        return toAjax(dataLabService.scoreSubmission(submissionId, score, scoreComment, SecurityUtils.getUserId(), toLong(body.get("revision"))));
    }

    // ==========================================
    // 学生端接口
    // ==========================================

    /**
     * 学生端获取实验题配置
     */
    @PreAuthorize("@studentSs.isStudent()")
    @GetMapping("/student/config/{questionId}")
    public AjaxResult getStudentConfig(@PathVariable Long questionId, @RequestParam Long sourceId) {
        return success(dataLabService.getStudentConfig(SecurityUtils.getUserId(), questionId, sourceId));
    }

    /**
     * 下载实验绑定的 Excel 文件流（支持学生和教师下载）
     */
    @GetMapping("/student/excel/{questionId}")
    @PreAuthorize("@studentSs.isStudent()")
    public void downloadExcel(@PathVariable Long questionId, @RequestParam Long sourceId, HttpServletResponse response) throws IOException {
        dataLabService.downloadExcel(questionId, sourceId, response);
    }

    /**
     * 学生提交实验代码与图表
     */
    @PreAuthorize("@studentSs.isStudent()")
    @PostMapping("/student/submit")
    public AjaxResult submit(@RequestBody Map<String, Object> body) {
        Long questionId = toLong(body.get("questionId"));
        if (questionId == null) {
            return error("题目ID不能为空");
        }
        String sourceType = body.get("sourceType") != null ? String.valueOf(body.get("sourceType")) : "PLAN";
        Long sourceId = toLong(body.get("sourceId"));
        String sourceCode = body.get("sourceCode") != null ? String.valueOf(body.get("sourceCode")) : "";
        String chartImageBase64 = body.get("chartImageBase64") != null ? String.valueOf(body.get("chartImageBase64")) : null;
        String outputText = body.get("outputText") != null ? String.valueOf(body.get("outputText")) : null;

        return success(dataLabService.submit(
                SecurityUtils.getUserId(),
                questionId,
                sourceType,
                sourceId,
                sourceCode,
                chartImageBase64,
                outputText
        ));
    }

    /**
     * 学生查询自己的历史提交
     */
    @PreAuthorize("@studentSs.isStudent()")
    @GetMapping("/student/submission")
    public AjaxResult getStudentSubmission(@RequestParam Long questionId,
                                           @RequestParam(required = false, defaultValue = "PLAN") String sourceType,
                                           @RequestParam(required = false) Long sourceId) {
        return success(dataLabService.getStudentSubmission(
                SecurityUtils.getUserId(),
                questionId,
                sourceType,
                sourceId
        ));
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        String str = String.valueOf(val).trim();
        if (str.isEmpty() || "null".equalsIgnoreCase(str)) return null;
        try {
            return Long.valueOf(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
