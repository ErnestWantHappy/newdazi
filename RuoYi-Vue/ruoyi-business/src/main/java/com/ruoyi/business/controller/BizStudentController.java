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
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.service.IBizStudentService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile; // 确保在文件顶部有这个import
import com.ruoyi.common.utils.SecurityUtils; // 确保在文件顶部有这个import
/**
 * 学生管理Controller
 * 
 * @author zdx
 * @date 2025-06-25
 */
@RestController
@RequestMapping("/business/student")
public class BizStudentController extends BaseController
{
    @Autowired
    private IBizStudentService bizStudentService;

    /**
     * 查询学生管理列表
     */
    @PreAuthorize("@ss.hasPermi('business:student:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizStudent bizStudent)
    {
        startPage();
        List<BizStudent> list = bizStudentService.selectBizStudentList(bizStudent);
        return getDataTable(list);
    }

    /**
     * 导出学生管理列表
     */
    @PreAuthorize("@ss.hasPermi('business:student:export')")
    @Log(title = "学生管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizStudent bizStudent)
    {
        List<BizStudent> list = bizStudentService.selectBizStudentList(bizStudent);
        ExcelUtil<BizStudent> util = new ExcelUtil<BizStudent>(BizStudent.class);
        util.exportExcel(response, list, "学生管理数据");
    }

    /**
     * 获取学生管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:student:query')")
    @GetMapping(value = "/{studentId}")
    public AjaxResult getInfo(@PathVariable("studentId") Long studentId)
    {
        return success(bizStudentService.selectBizStudentByStudentId(studentId));
    }

    /**
     * 新增学生管理
     */
    @PreAuthorize("@ss.hasPermi('business:student:add')")
    @Log(title = "学生管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizStudent bizStudent)
    {
        bizStudent.setSchoolId(getLoginUser().getUser().getDeptId()); // 我们仍然从deptId获取
        return toAjax(bizStudentService.insertBizStudent(bizStudent));
    }

    /**
     * 修改学生管理
     */
    @PreAuthorize("@ss.hasPermi('business:student:edit')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizStudent bizStudent)
    {
        bizStudent.setSchoolId(getLoginUser().getUser().getDeptId());
        return toAjax(bizStudentService.updateBizStudent(bizStudent));
    }

    /**
     * 删除学生管理
     */
    @PreAuthorize("@ss.hasPermi('business:student:remove')")
    @Log(title = "学生管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{studentIds}")
    public AjaxResult remove(@PathVariable Long[] studentIds)
    {
        return toAjax(bizStudentService.deleteBizStudentByStudentIds(studentIds));
    }

    /**
     * 下载学生导入模板
     */
    @Log(title = "学生管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('business:student:import')")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<BizStudent> util = new ExcelUtil<BizStudent>(BizStudent.class);
        util.importTemplateExcel(response, "学生数据");
    }

    /**
     * 导入学生数据
     */
    @Log(title = "学生管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('business:student:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file) throws Exception
    {
        ExcelUtil<BizStudent> util = new ExcelUtil<BizStudent>(BizStudent.class);
        List<BizStudent> studentList = util.importExcel(file.getInputStream());
        String operName = SecurityUtils.getUsername();
        String message = bizStudentService.importStudent(studentList, operName);
        return AjaxResult.success(message);
    }


}
