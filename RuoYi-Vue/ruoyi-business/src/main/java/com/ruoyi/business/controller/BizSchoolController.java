package com.ruoyi.business.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.business.domain.BizSchool;
import com.ruoyi.business.service.IBizSchoolService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 学校信息Controller
 * 
 * @author ruoyi
 * @date 2025-06-24
 */
@RestController
@RequestMapping("/business/school")
public class BizSchoolController extends BaseController
{
    @Autowired
    private IBizSchoolService bizSchoolService;

    /**
     * 查询学校信息列表
     */
    @PreAuthorize("@ss.hasPermi('business:school:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizSchool bizSchool)
    {
        startPage();
        List<BizSchool> list = bizSchoolService.selectBizSchoolList(bizSchool);
        return getDataTable(list);
    }

    /**
     * 导出学校信息列表
     */
    @PreAuthorize("@ss.hasPermi('business:school:export')")
    @Log(title = "学校信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizSchool bizSchool)
    {
        List<BizSchool> list = bizSchoolService.selectBizSchoolList(bizSchool);
        ExcelUtil<BizSchool> util = new ExcelUtil<BizSchool>(BizSchool.class);
        util.exportExcel(response, list, "学校信息数据");
    }

    /**
     * 获取学校信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:school:query')")
    @GetMapping(value = "/{schoolId}")
    public AjaxResult getInfo(@PathVariable("schoolId") Long schoolId)
    {
        return success(bizSchoolService.selectBizSchoolBySchoolId(schoolId));
    }

    /**
     * 新增学校信息
     */
    @PreAuthorize("@ss.hasPermi('business:school:add')")
    @Log(title = "学校信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizSchool bizSchool)
    {
        return toAjax(bizSchoolService.insertBizSchool(bizSchool));
    }

    /**
     * 修改学校信息
     */
    @PreAuthorize("@ss.hasPermi('business:school:edit')")
    @Log(title = "学校信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizSchool bizSchool)
    {
        return toAjax(bizSchoolService.updateBizSchool(bizSchool));
    }

    /**
     * 删除学校信息
     */
    @PreAuthorize("@ss.hasPermi('business:school:remove')")
    @Log(title = "学校信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{schoolIds}")
    public AjaxResult remove(@PathVariable Long[] schoolIds)
    {
        return toAjax(bizSchoolService.deleteBizSchoolBySchoolIds(schoolIds));
    }
}
