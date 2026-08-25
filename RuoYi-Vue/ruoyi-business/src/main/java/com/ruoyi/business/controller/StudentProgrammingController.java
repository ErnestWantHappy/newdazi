package com.ruoyi.business.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.ProgrammingDraft;
import com.ruoyi.business.domain.ProgrammingQuestionConfig;
import com.ruoyi.business.domain.ProgrammingSubmission;
import com.ruoyi.business.domain.vo.StudentProgrammingSubmissionVo;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.business.service.ProgrammingSubmissionService;
import com.ruoyi.business.service.ProgrammingSubmissionWorker;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/** 学生只通过平台请求编程服务，接口中不透出 Judge0 URL 或隐藏测试内容。 */
@RestController
@RequestMapping("/business/student-home/programming")
@PreAuthorize("@studentSs.isStudent()")
public class StudentProgrammingController extends BaseController {
    @Autowired private BizStudentMapper studentMapper;
    @Autowired private GuideSheetAccessService guideSheetAccessService;
    @Autowired private ProgrammingSubmissionService programmingSubmissionService;
    @Autowired private ProgrammingSubmissionWorker programmingSubmissionWorker;

    @GetMapping("/{lessonId}/{questionId}")
    public AjaxResult detail(@PathVariable Long lessonId, @PathVariable Long questionId) {
        BizStudent student = currentStudent(); Long deptId = SecurityUtils.getDeptId();
        ProgrammingQuestionConfig config = programmingSubmissionService.getStudentConfig(student, deptId, lessonId, questionId);
        ProgrammingDraft draft = programmingSubmissionService.getDraft(student, deptId, lessonId, questionId);
        Map<String, Object> data = new LinkedHashMap<String, Object>(); data.put("config", config); data.put("publicCases", programmingSubmissionService.getStudentPublicCases(student, deptId, lessonId, questionId)); data.put("draft", draft); data.put("history", programmingSubmissionService.getStudentHistory(student, deptId, lessonId, questionId));
        return success(data);
    }

    @PutMapping("/draft")
    public AjaxResult saveDraft(@RequestBody StudentProgrammingRequest request) {
        BizStudent student = currentStudent(); programmingSubmissionService.saveDraft(student, SecurityUtils.getDeptId(), request.lessonId, request.questionId, request.sourceCode); return success("草稿已保存");
    }

    @PostMapping("/run")
    public AjaxResult run(@RequestBody StudentProgrammingRequest request, HttpServletRequest httpRequest) { return submit(request, "RUN", httpRequest); }
    @PostMapping("/custom-run")
    public AjaxResult customRun(@RequestBody StudentProgrammingRequest request, HttpServletRequest httpRequest) { return submit(request, "CUSTOM_RUN", httpRequest); }
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody StudentProgrammingRequest request, HttpServletRequest httpRequest) { return submit(request, "SUBMIT", httpRequest); }

    @PostMapping("/{lessonId}/{questionId}/submissions/{submissionId}/cancel")
    public AjaxResult cancel(@PathVariable Long lessonId, @PathVariable Long questionId, @PathVariable Long submissionId) {
        BizStudent student = currentStudent(); programmingSubmissionService.cancel(student, SecurityUtils.getDeptId(), lessonId, questionId, submissionId); return success("提交已取消");
    }

    private AjaxResult submit(StudentProgrammingRequest request, String kind, HttpServletRequest httpRequest) {
        BizStudent student = currentStudent(); guideSheetAccessService.assertNoPendingCountyExam();
        ProgrammingSubmission submission = programmingSubmissionService.submit(student, SecurityUtils.getDeptId(), request.lessonId, request.questionId, request.sourceCode, request.customInput, request.submissionKey, kind, httpRequest.getRemoteAddr(), programmingSubmissionWorker);
        List<StudentProgrammingSubmissionVo> history = programmingSubmissionService.getStudentHistory(student, SecurityUtils.getDeptId(), request.lessonId, request.questionId);
        StudentProgrammingSubmissionVo safe = history.stream().filter(item -> submission.getSubmissionId().equals(item.getSubmissionId())).findFirst().orElse(null);
        String message = "RUN".equals(kind) ? "示例运行已进入队列" : ("CUSTOM_RUN".equals(kind) ? "自定义运行已进入队列" : "正式提交已进入队列");
        return success(message).put("submission", safe);
    }

    private BizStudent currentStudent() {
        LoginUser login = SecurityUtils.getLoginUser(); if (login == null) throw new ServiceException("用户未登录");
        BizStudent student = studentMapper.selectBizStudentByUserId(login.getUserId()); if (student == null) throw new ServiceException("未找到学生信息"); return student;
    }

    public static class StudentProgrammingRequest {
        private Long lessonId; private Long questionId; private String sourceCode; private String customInput; private String submissionKey;
        public Long getLessonId() { return lessonId; } public void setLessonId(Long v) { lessonId = v; }
        public Long getQuestionId() { return questionId; } public void setQuestionId(Long v) { questionId = v; }
        public String getSourceCode() { return sourceCode; } public void setSourceCode(String v) { sourceCode = v; }
        public String getCustomInput() { return customInput; } public void setCustomInput(String v) { customInput = v; }
        public String getSubmissionKey() { return submissionKey; } public void setSubmissionKey(String v) { submissionKey = v; }
    }
}
