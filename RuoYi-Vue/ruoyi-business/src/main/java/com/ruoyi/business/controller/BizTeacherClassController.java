package com.ruoyi.business.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.business.domain.BizTeacherClass;
import com.ruoyi.business.service.IBizTeacherClassService;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 教师班级管理Controller
 * 
 * @author zdx
 * @date 2025-12-29
 */
@RestController
@RequestMapping("/business/teacherClass")
public class BizTeacherClassController extends BaseController
{
    @Autowired
    private IBizTeacherClassService bizTeacherClassService;

    /**
     * 查询当前教师管理的班级列表
     */
    @PreAuthorize("@ss.hasPermi('business:teacherClass:list')")
    @GetMapping("/list")
    public TableDataInfo list()
    {
        Long userId = SecurityUtils.getUserId();
        Long deptId = SecurityUtils.getDeptId();
        List<BizTeacherClass> list = bizTeacherClassService.selectMyClassList(userId, deptId);
        return getDataTable(list);
    }

    /**
     * 查询我管理的班级（不分页，用于下拉框）
     */
    @PreAuthorize("@ss.hasPermi('business:teacherClass:list')")
    @GetMapping("/myClasses")
    public AjaxResult myClasses()
    {
        Long userId = SecurityUtils.getUserId();
        Long deptId = SecurityUtils.getDeptId();
        List<BizTeacherClass> list = bizTeacherClassService.selectMyClassList(userId, deptId);
        return success(list);
    }

    /**
     * 查询学校内所有可选班级（用于添加班级管理时的下拉选择）
     */
    @PreAuthorize("@ss.hasPermi('business:teacherClass:list')")
    @GetMapping("/availableClasses")
    public AjaxResult availableClasses()
    {
        Long deptId = SecurityUtils.getDeptId();
        List<BizTeacherClass> list = bizTeacherClassService.selectAvailableClasses(deptId);
        return success(list);
    }

    /**
     * 新增教师班级管理
     */
    @PreAuthorize("@ss.hasPermi('business:teacherClass:add')")
    @Log(title = "教师班级管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizTeacherClass bizTeacherClass)
    {
        // 自动填充当前用户信息
        bizTeacherClass.setUserId(SecurityUtils.getUserId());
        bizTeacherClass.setDeptId(SecurityUtils.getDeptId());
        return toAjax(bizTeacherClassService.insertBizTeacherClass(bizTeacherClass));
    }

    /**
     * 批量新增教师班级管理
     */
    @PreAuthorize("@ss.hasPermi('business:teacherClass:add')")
    @Log(title = "教师班级管理", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult batchAdd(@RequestBody List<BizTeacherClass> list)
    {
        Long userId = SecurityUtils.getUserId();
        Long deptId = SecurityUtils.getDeptId();
        for (BizTeacherClass item : list)
        {
            item.setUserId(userId);
            item.setDeptId(deptId);
        }
        return toAjax(bizTeacherClassService.batchInsertBizTeacherClass(list));
    }

    /**
     * 删除教师班级管理
     */
    @PreAuthorize("@ss.hasPermi('business:teacherClass:remove')")
    @Log(title = "教师班级管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizTeacherClassService.deleteBizTeacherClassByIds(ids));
    }
}
