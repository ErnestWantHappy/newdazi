package com.ruoyi.business.config;

import com.ruoyi.common.core.domain.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 业务模块全局异常处理器
 * 统一处理 Controller 层未捕获的异常，减少重复的 try-catch 代码
 *
 * @author ruoyi
 */
@RestControllerAdvice(basePackages = "com.ruoyi.business.controller")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 参数校验异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public AjaxResult handleIllegalArgument(IllegalArgumentException e) {
        log.warn("参数校验失败: {}", e.getMessage());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntime(RuntimeException e) {
        log.error("运行时异常", e);
        return AjaxResult.error("服务器内部错误: " + e.getMessage());
    }

    /**
     * 兜底：未知异常
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e) {
        log.error("未知异常", e);
        return AjaxResult.error("服务器内部错误");
    }
}
