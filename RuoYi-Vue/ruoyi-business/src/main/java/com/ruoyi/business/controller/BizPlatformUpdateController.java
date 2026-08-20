package com.ruoyi.business.controller;

import com.ruoyi.business.domain.BizPlatformUpdate;
import com.ruoyi.business.service.IBizPlatformUpdateService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 平台更新记录：教师和教研员只读，管理员负责修订和发布。 */
@RestController
@RequestMapping("/business/platform-update")
public class BizPlatformUpdateController extends BaseController
{
    @Autowired
    private IBizPlatformUpdateService updateService;

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher') and @ss.hasPermi('business:platformUpdate:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) String keyword)
    {
        startPage();
        List<BizPlatformUpdate> list = updateService.selectPublishedList(keyword);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasRole('admin') and @ss.hasPermi('business:platformUpdate:list')")
    @GetMapping("/manage/list")
    public TableDataInfo manageList(BizPlatformUpdate query)
    {
        startPage();
        return getDataTable(updateService.selectManageList(query));
    }

    @PreAuthorize("@ss.hasRole('admin') and @ss.hasPermi('business:platformUpdate:add')")
    @Log(title = "平台更新", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizPlatformUpdate update)
    {
        return toAjax(updateService.create(update));
    }

    @PreAuthorize("@ss.hasRole('admin') and @ss.hasPermi('business:platformUpdate:edit')")
    @Log(title = "平台更新", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizPlatformUpdate update)
    {
        return toAjax(updateService.update(update));
    }

    @PreAuthorize("@ss.hasRole('admin') and @ss.hasPermi('business:platformUpdate:publish')")
    @Log(title = "平台更新发布状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{updateId}/status/{status}")
    public AjaxResult changeStatus(@PathVariable Long updateId, @PathVariable String status)
    {
        return toAjax(updateService.changeStatus(updateId, status));
    }
}
