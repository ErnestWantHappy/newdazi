package com.ruoyi.business.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.exception.ServiceException;

/**
 * AD 连通性试验接口（上线前禁用）。
 * 原实现含 LDAP 硬编码占位与 @Bean 副作用，正式环境禁止暴露。
 */
@RestController
@RequestMapping("/test")
public class AdTestController {

    @GetMapping("/ad")
    public String testAdConnection() {
        throw new ServiceException("测试接口已禁用");
    }
}
