package com.ruoyi.business.controller;

import com.ruoyi.business.service.StudentFeedbackService;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/business/student/feedback")
@PreAuthorize("@studentSs.isStudent()")
public class StudentFeedbackController {
    @Autowired private StudentFeedbackService service;
    @GetMapping("/lessons") public AjaxResult lessons() { return AjaxResult.success(service.lessons()); }
    @GetMapping public AjaxResult mine(@RequestParam Long lessonId) throws Exception { return AjaxResult.success(service.mine(lessonId)); }
}
