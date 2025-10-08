package com.ruoyi.web.controller.system;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 授权相关接口
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/auth")
public class SysAuthController
{
    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysUserService userService;

    @PostMapping("/select-school")
    public AjaxResult selectSchool(@RequestBody SelectSchoolBody body)
    {
        if (body == null || body.getDeptId() == null)
        {
            return AjaxResult.error("请先选择校区");
        }
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        if (StringUtils.isNull(loginUser))
        {
            return AjaxResult.error("登录状态已过期");
        }
        ensureSchoolInfo(loginUser);
        List<Long> deptIds = loginUser.getDeptIds();
        if (CollectionUtils.isEmpty(deptIds) || !deptIds.contains(body.getDeptId()))
        {
            return AjaxResult.error("无权访问该校区");
        }
        String oldToken = loginUser.getToken();
        loginUser.setDeptId(body.getDeptId());
        SysUser user = loginUser.getUser();
        if (user != null)
        {
            user.setDeptId(body.getDeptId());
        }
        String token = tokenService.createToken(loginUser);
        tokenService.delLoginUser(oldToken);
        AjaxResult ajax = AjaxResult.success();
        ajax.put(Constants.TOKEN, token);
        ajax.put("selectedSchool", body.getDeptId());
        return ajax;
    }

    private void ensureSchoolInfo(LoginUser loginUser)
    {
        if (!CollectionUtils.isEmpty(loginUser.getDeptIds()) && !CollectionUtils.isEmpty(loginUser.getManageDepts()))
        {
            return;
        }
        List<SysDept> schools = userService.selectDeptsByUserId(loginUser.getUserId());
        if (schools == null)
        {
            schools = new ArrayList<>();
        }
        loginUser.setManageDepts(schools);
        List<Long> ids = new ArrayList<>();
        for (SysDept school : schools)
        {
            ids.add(school.getDeptId());
        }
        loginUser.setDeptIds(ids);
        SysUser user = loginUser.getUser();
        if (user != null)
        {
            user.setDeptIds(ids);
        }
    }

    public static class SelectSchoolBody
    {
        private Long deptId;

        public Long getDeptId()
        {
            return deptId;
        }

        public void setDeptId(Long deptId)
        {
            this.deptId = deptId;
        }
    }
}
