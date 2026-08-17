package com.ruoyi.business.controller;

import java.util.Arrays;
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
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 学生管理Controller
 * * @author zdx
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
        // 注意：设置schoolId的逻辑已移入Service层，此处不再需要
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
        // 注意：设置schoolId的逻辑已移入Service层，此处不再需要
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
     * 按班级批量删除学生
     */
    @PreAuthorize("@ss.hasPermi('business:student:remove')")
    @Log(title = "学生管理-按班级删除", businessType = BusinessType.DELETE)
    @DeleteMapping("/byClass")
    public AjaxResult removeByClass(String entryYear, String classCode, Long deptId)
    {
        if (com.ruoyi.common.utils.StringUtils.isEmpty(entryYear) || com.ruoyi.common.utils.StringUtils.isEmpty(classCode)) {
            return AjaxResult.error("入学年份和班级不能为空");
        }
        // 如果未传入 deptId，则默认使用当前用户的 deptId
        if (deptId == null) {
            com.ruoyi.common.core.domain.model.LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser != null && loginUser.getUser() != null) {
                deptId = loginUser.getUser().getDeptId();
            }
        }
        int rows = bizStudentService.deleteBizStudentByClass(entryYear, classCode, deptId);
        return success("成功清空 " + rows + " 名学生");
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
        BizStudent first = new BizStudent();
        first.setStudentNo("01");
        first.setEntryYear("2025");
        first.setClassCode("01");
        first.setStudentName("示例学生一");
        first.setRemark("示例行，导入前请替换");

        BizStudent second = new BizStudent();
        second.setStudentNo("02");
        second.setEntryYear("2025");
        second.setClassCode("02");
        second.setStudentName("示例学生二");
        second.setRemark("班号只填 01～10，不要写 601、602");

        // 模板带两行示例，避免教师把年级号误写进班级编号。
        util.exportExcel(response, Arrays.asList(first, second), "学生导入模板");
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

    /**
     * 重置密码
     */
    @PreAuthorize("@ss.hasPermi('business:student:edit')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody Long[] userIds) // 参数从SysUser改为Long[]
    {
        if (userIds == null || userIds.length == 0) {
            return AjaxResult.error("参数不能为空！");
        }
        bizStudentService.resetStudentPwd(userIds);
        return AjaxResult.success();
    }

    /**
     * 查询学生锁定状态
     */
    @PreAuthorize("@ss.hasPermi('business:student:list')")
    @GetMapping("/lockStatus")
    public AjaxResult getLockStatus(@org.springframework.web.bind.annotation.RequestParam(value = "userNames") String userNames)
    {
        if (userNames == null || userNames.isEmpty()) {
            return AjaxResult.success(new java.util.HashMap<>());
        }
        
        com.ruoyi.common.core.redis.RedisCache redisCache = 
            com.ruoyi.common.utils.spring.SpringUtils.getBean(com.ruoyi.common.core.redis.RedisCache.class);
        
        java.util.Map<String, Boolean> lockStatusMap = new java.util.HashMap<>();
        String[] names = userNames.split(",");
        for (String userName : names) {
            String cacheKey = com.ruoyi.common.constant.CacheConstants.PWD_ERR_CNT_KEY + userName.trim();
            Integer retryCount = redisCache.getCacheObject(cacheKey);
            // 达到5次则锁定
            lockStatusMap.put(userName.trim(), retryCount != null && retryCount >= 5);
        }
        return AjaxResult.success(lockStatusMap);
    }
}
