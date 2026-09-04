package com.ruoyi.business.controller;

import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.FlowchartQuestionConfig;
import com.ruoyi.business.service.FlowchartService;
import com.ruoyi.business.service.ClassroomTaskStateService;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.domain.BizStudent;
import org.springframework.beans.factory.annotation.Autowired;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/** 画程流程图操作题接口。 */
@RestController
@RequestMapping("/business/flowchart")
public class FlowchartController extends BaseController {
    private final FlowchartService service;

    @Autowired private ClassroomTaskStateService taskStateService;
    @Autowired private BizStudentMapper studentMapper;

    public FlowchartController(FlowchartService service) { this.service = service; }

    @PreAuthorize("@ss.hasPermi('business:question:query') or @ss.hasPermi('business:question:edit')")
    @GetMapping("/question/{questionId}")
    public AjaxResult question(@PathVariable Long questionId) {
        Long userId = SecurityUtils.getUserId();
        return success(service.teacherConfig(questionId, userId, SecurityUtils.isAdmin(userId)));
    }

    /**
     * 课程选题时允许查看公开题的学生基础图，但不暴露标准答案和结构规则。
     */
    @PreAuthorize("@ss.hasPermi('business:question:query') or @ss.hasPermi('business:question:edit')")
    @GetMapping("/question/{questionId}/preview")
    public AjaxResult questionPreview(@PathVariable Long questionId) {
        Long userId = SecurityUtils.getUserId();
        return success(service.teacherPreview(questionId, userId, SecurityUtils.isAdmin(userId)));
    }

    @PreAuthorize("@ss.hasPermi('business:question:edit') or @ss.hasPermi('business:question:add')")
    @PutMapping("/question/{questionId}")
    public AjaxResult saveQuestion(@PathVariable Long questionId,
                                   @RequestBody FlowchartQuestionConfig request) {
        Long userId = SecurityUtils.getUserId();
        return success(service.saveTeacherConfig(questionId, request, userId,
                SecurityUtils.isAdmin(userId), SecurityUtils.getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('business:question:edit') or @ss.hasPermi('business:question:add')")
    @PostMapping("/question/generate-rules")
    public AjaxResult generateRules(@RequestBody Map<String, Object> request) {
        Object answerJson = request == null ? null : request.get("answerJson");
        if (answerJson == null) throw new ServiceException("标准答案不能为空");
        // BaseController 同时存在 success(String message)，显式按数据返回，避免规则 JSON 被误放进 msg。
        return success((Object) service.generateRules(String.valueOf(answerJson)));
    }

    @PreAuthorize("@studentSs.isStudent()")
    @GetMapping("/student/workspace")
    public AjaxResult workspace(@RequestParam Long lessonId, @RequestParam Long questionId) {
        return success(service.studentWorkspace(SecurityUtils.getUserId(), SecurityUtils.getDeptId(),
                lessonId, questionId));
    }

    @PreAuthorize("@studentSs.isStudent()")
    @PutMapping("/student/draft")
    public AjaxResult draft(@RequestBody Map<String, Object> request) {
        Long lessonId = requiredLong(request, "lessonId");
        Long questionId = requiredLong(request, "questionId");
        Object result = service.saveDraft(SecurityUtils.getUserId(), SecurityUtils.getDeptId(),
                lessonId, questionId, requiredInt(request, "expectedRevision"), requiredString(request, "documentJson"));
        BizStudent student = currentStudent();
        taskStateService.markSafely(student, student.getDeptId(), lessonId, questionId,
                ClassroomTaskStateService.WORKING);
        return success(result);
    }

    @PreAuthorize("@studentSs.isStudent()")
    @PostMapping("/student/submit")
    public AjaxResult submit(@RequestBody Map<String, Object> request) {
        Long lessonId = requiredLong(request, "lessonId");
        Long questionId = requiredLong(request, "questionId");
        Object result = service.submit(SecurityUtils.getUserId(), SecurityUtils.getDeptId(),
                lessonId, questionId, requiredInt(request, "expectedRevision"));
        BizStudent student = currentStudent();
        taskStateService.markSafely(student, student.getDeptId(), lessonId, questionId,
                ClassroomTaskStateService.SUBMITTED);
        return success(result);
    }

    @PreAuthorize("@studentSs.isStudent()")
    @PostMapping("/student/reopen")
    public AjaxResult reopen(@RequestBody Map<String, Object> request) {
        return success(service.reopen(SecurityUtils.getUserId(), SecurityUtils.getDeptId(),
                requiredLong(request, "lessonId"), requiredLong(request, "questionId")));
    }

    @PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('admin')")
    @GetMapping("/grading/submission")
    public AjaxResult grading(@RequestParam Long lessonId, @RequestParam Long questionId,
                              @RequestParam Long studentId,
                              @RequestParam(required = false) Integer versionNo) {
        return success(service.gradingSubmission(lessonId, questionId, studentId, versionNo));
    }

    private Long requiredLong(Map<String, Object> request, String key) {
        Object value = request == null ? null : request.get(key);
        if (value == null) throw new ServiceException(key + " 不能为空");
        if (value instanceof Number) return ((Number) value).longValue();
        try { return Long.valueOf(String.valueOf(value)); }
        catch (NumberFormatException e) { throw new ServiceException(key + " 参数不正确"); }
    }

    private BizStudent currentStudent() {
        BizStudent student = studentMapper.selectBizStudentByUserId(SecurityUtils.getUserId());
        if (student == null) throw new ServiceException("未找到学生信息");
        return student;
    }

    private Integer requiredInt(Map<String, Object> request, String key) {
        Object value = request == null ? null : request.get(key);
        if (value == null) throw new ServiceException(key + " 不能为空");
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.valueOf(String.valueOf(value)); }
        catch (NumberFormatException e) { throw new ServiceException(key + " 参数不正确"); }
    }

    private String requiredString(Map<String, Object> request, String key) {
        Object value = request == null ? null : request.get(key);
        if (value == null) throw new ServiceException(key + " 不能为空");
        return String.valueOf(value);
    }
}
