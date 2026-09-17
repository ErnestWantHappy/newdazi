package com.ruoyi.business.controller;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.service.PracticalGradingDeadlineService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 教研员和管理员维护操作题批改期限。
 */
@RestController
@RequestMapping("/business/practical-deadline")
@PreAuthorize("@ss.hasRole('researcher') or @ss.hasRole('admin')")
public class PracticalGradingDeadlineController extends BaseController
{
    @Autowired
    private PracticalGradingDeadlineService deadlineService;

    @GetMapping("/config")
    @PreAuthorize("(@ss.hasRole('researcher') or @ss.hasRole('admin')) and "
            + "@ss.hasPermi('business:practicalDeadline:config')")
    public AjaxResult getConfig()
    {
        return AjaxResult.success().put("deadlineDays", deadlineService.getDeadlineDays());
    }

    @PutMapping("/config")
    @PreAuthorize("(@ss.hasRole('researcher') or @ss.hasRole('admin')) and "
            + "@ss.hasPermi('business:practicalDeadline:config')")
    @Log(title = "操作题批改期限配置", businessType = BusinessType.UPDATE)
    public AjaxResult updateConfig(@RequestBody DeadlineConfigRequest request)
    {
        deadlineService.updateDeadlineDays(request.getDeadlineDays(), SecurityUtils.getUsername());
        return AjaxResult.success("配置已更新，仅影响以后首次触发的课程班级");
    }

    @PostMapping("/{deadlineId}/adjust")
    @PreAuthorize("(@ss.hasRole('researcher') or @ss.hasRole('admin')) and "
            + "@ss.hasPermi('business:practicalDeadline:adjust')")
    @Log(title = "操作题批改期限调整", businessType = BusinessType.UPDATE)
    public AjaxResult adjust(@PathVariable Long deadlineId,
                             @RequestBody DeadlineAdjustRequest request)
    {
        return AjaxResult.success(deadlineService.adjustDeadline(
                deadlineId, request.getNewDeadlineTime(), request.getReason(),
                SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @GetMapping("/{deadlineId}/audits")
    @PreAuthorize("(@ss.hasRole('researcher') or @ss.hasRole('admin')) and "
            + "@ss.hasPermi('business:teachingSupervision:view')")
    public AjaxResult audits(@PathVariable Long deadlineId)
    {
        return AjaxResult.success(deadlineService.getAdjustmentHistory(deadlineId));
    }

    public static class DeadlineConfigRequest
    {
        private int deadlineDays;
        public int getDeadlineDays() { return deadlineDays; }
        public void setDeadlineDays(int deadlineDays) { this.deadlineDays = deadlineDays; }
    }

    public static class DeadlineAdjustRequest
    {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date newDeadlineTime;
        private String reason;
        public Date getNewDeadlineTime() { return newDeadlineTime; }
        public void setNewDeadlineTime(Date newDeadlineTime) { this.newDeadlineTime = newDeadlineTime; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
