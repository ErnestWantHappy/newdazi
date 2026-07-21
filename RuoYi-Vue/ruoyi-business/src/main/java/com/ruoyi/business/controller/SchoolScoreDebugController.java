package com.ruoyi.business.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;

/**
 * 临时数据库调试接口（上线前禁用）。
 * 保留路径避免前端旧书签 404，任何访问均拒绝。
 */
@RestController
@RequestMapping("/business/debug")
public class SchoolScoreDebugController extends BaseController {

    @GetMapping("/checkData")
    public AjaxResult checkData() {
        throw new ServiceException("调试接口已禁用");
    }
}
