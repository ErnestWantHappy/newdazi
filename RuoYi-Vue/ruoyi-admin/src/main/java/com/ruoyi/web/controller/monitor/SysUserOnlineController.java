package com.ruoyi.web.controller.monitor;

import java.util.*;
import java.util.stream.Collectors;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.framework.web.service.OnlinePresenceService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.service.ISysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 在线人数按近期有效活动去重，不再按登录会话计数。 */
@RestController
@RequestMapping("/monitor/online")
public class SysUserOnlineController {
    @Autowired private OnlinePresenceService presence;
    @Autowired private TokenService tokenService;
    @Autowired private ISysDeptService deptService;

    @PreAuthorize("@ss.hasPermi('monitor:online:list')")
    @GetMapping("/list")
    public Map<String, Object> list(String ipaddr, String userName, Long deptId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        Map<Long, SysDept> deptMap = depts.stream().collect(Collectors.toMap(SysDept::getDeptId, d -> d, (a,b) -> a));
        List<JSONObject> all = presence.activeUsers();
        List<JSONObject> filtered = new ArrayList<>();
        for (JSONObject row : all) {
            SysDept dept = deptMap.get(row.getLong("deptId"));
            row.put("deptName", dept == null ? "未知部门" : dept.getDeptName());
            if (!matches(row, ipaddr, userName, deptId, dept)) continue;
            filtered.add(row);
        }
        int size = Math.max(1, Math.min(100, pageSize));
        int from = (int)Math.min(filtered.size(), (long)(Math.max(1, pageNum) - 1) * size);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200); result.put("msg", "查询成功");
        result.put("rows", filtered.subList(from, Math.min(filtered.size(), from + size)));
        result.put("total", filtered.size()); result.put("onlineTotal", all.size());
        result.put("windowMinutes", 5); result.put("sampledAt", System.currentTimeMillis());
        return result;
    }

    static boolean matches(JSONObject row, String ip, String name, Long deptId, SysDept dept) {
        if (ip != null && !ip.trim().isEmpty() && !Objects.equals(ip.trim(), row.getString("ipaddr"))) return false;
        if (name != null && !name.trim().isEmpty() && !row.getString("userName").contains(name.trim())) return false;
        if (deptId == null) return true;
        if (Objects.equals(deptId, row.getLong("deptId"))) return true;
        return dept != null && dept.getAncestors() != null
                && Arrays.asList(dept.getAncestors().split(",")).contains(deptId.toString());
    }

    @PreAuthorize("@ss.hasPermi('monitor:online:list')")
    @GetMapping("/deptTree")
    public AjaxResult deptTree() {
        return AjaxResult.success(deptService.buildDeptTreeSelect(deptService.selectDeptList(new SysDept())));
    }

    /** 保留按会话强退语义，明确仅退出该行最近活动的会话。 */
    @PreAuthorize("@ss.hasPermi('monitor:online:forceLogout')")
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @DeleteMapping("/{tokenId}")
    public AjaxResult forceLogout(@PathVariable String tokenId) {
        tokenService.delLoginUser(tokenId);
        return AjaxResult.success();
    }
}
