package com.ruoyi.business.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.business.service.ExtensionClassService;
import com.ruoyi.common.core.domain.AjaxResult;

@RestController
@RequestMapping("/business/extension/student")
@PreAuthorize("@studentSs.isStudent()")
public class ExtensionStudentController {
    @Autowired private ExtensionClassService service;
    @GetMapping("/context") public AjaxResult context() { return AjaxResult.success(service.context()); }
    @PostMapping("/space") public AjaxResult space(@RequestBody Map<String,String> body) { service.saveSpace(body.get("space")); return AjaxResult.success(); }
    @GetMapping("/home") public AjaxResult home() { return AjaxResult.success(service.home()); }
}
