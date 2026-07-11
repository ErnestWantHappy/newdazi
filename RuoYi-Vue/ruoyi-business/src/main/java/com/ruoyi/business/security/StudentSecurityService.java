package com.ruoyi.business.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.framework.web.service.PermissionService;

/**
 * 学生身份鉴权，与前端 getInfo 角色补丁保持一致。
 * 优先按 sys_role.role_key 判断，再回退 biz_student 业务表。
 */
@Service("studentSs")
public class StudentSecurityService
{
    @Autowired
    private PermissionService permissionService;

    @Autowired
    private BizStudentMapper bizStudentMapper;

    public boolean isStudent()
    {
        if (permissionService.hasRole("student"))
        {
            return true;
        }
        Long userId = SecurityUtils.getUserId();
        return userId != null && bizStudentMapper.selectBizStudentByUserId(userId) != null;
    }
}
