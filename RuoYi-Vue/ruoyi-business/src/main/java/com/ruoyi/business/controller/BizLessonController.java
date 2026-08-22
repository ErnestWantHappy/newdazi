package com.ruoyi.business.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.business.domain.vo.LessonDetailVo;
import com.ruoyi.business.domain.BizLessonCheckin;
import com.ruoyi.business.domain.BizTeacherClass;
import com.ruoyi.business.mapper.BizLessonCheckinMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizTeacherClassMapper;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.service.IBizLessonService;
import com.ruoyi.business.service.LessonAutoAdvanceService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

import java.util.Map;

/**
 * 课程管理Controller
 *
 * @author ruoyi
 * @date 2025-08-21
 */
@RestController
@RequestMapping("/business/lesson")
public class BizLessonController extends BaseController
{
    @Autowired
    private IBizLessonService bizLessonService;

    @Autowired
    private BizLessonCheckinMapper lessonCheckinMapper;

    @Autowired
    private BizLessonMapper lessonMapper;

    @Autowired
    private LessonAutoAdvanceService lessonAutoAdvanceService;

    @Autowired
    private BizTeacherClassMapper teacherClassMapper;

    /**
     * 教师查看某考勤课的各班签到汇总。
     * 创建教师和管理员可看本校该届全部班级，任课教师仅看自己管理的班级。
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:query')")
    @GetMapping("/checkin-summary")
    public AjaxResult checkinSummary(@RequestParam Long lessonId, @RequestParam String entryYear)
    {
        if (lessonId == null || StringUtils.isBlank(entryYear))
        {
            return error("参数不完整");
        }
        BizLesson lesson = getAuthorizedCheckinLesson(lessonId);
        if (lesson == null)
        {
            return error("课程不存在或无权查看签到");
        }
        Long deptId = SecurityUtils.getDeptId();
        List<BizLessonCheckin> summary = lessonCheckinMapper.selectClassSummaryByLesson(
                lessonId, entryYear.trim(), deptId);
        if (!canViewAllCheckinClasses(lesson))
        {
            Long userId = SecurityUtils.getUserId();
            summary.removeIf(row -> !canManageCheckinClass(userId, deptId, entryYear, row.getClassCode()));
        }
        long total = summary.stream().mapToLong(row -> row.getTotalCount() == null ? 0L : row.getTotalCount()).sum();
        long checked = summary.stream().mapToLong(row -> row.getCheckedInCount() == null ? 0L : row.getCheckedInCount()).sum();
        return success(summary)
                .put("total", total)
                .put("checkedInCount", checked)
                .put("lessonMode", lesson.getLessonMode() == null ? "assessment" : lesson.getLessonMode())
                .put("lessonTitle", lesson.getLessonTitle());
    }

    /**
     * 获取课程完整详情 (用于课程设计器 "修改" 模式)
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:query')")
    @GetMapping(value = "/details/{lessonId}")
    public AjaxResult getLessonDetails(@PathVariable("lessonId") Long lessonId)
    {
        Object details = bizLessonService.selectLessonDetailsByLessonId(lessonId);
        if (details == null)
        {
            return error("课程不存在");
        }
        return success(details);
    }

    /**
     * 教师查看某班签到名单（课堂考勤）
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:query')")
    @GetMapping("/checkin-roster")
    public AjaxResult checkinRoster(
            @RequestParam Long lessonId,
            @RequestParam String entryYear,
            @RequestParam String classCode)
    {
        if (lessonId == null || StringUtils.isBlank(entryYear) || StringUtils.isBlank(classCode))
        {
            return error("参数不完整");
        }
        BizLesson lesson = getAuthorizedCheckinLesson(lessonId);
        if (lesson == null)
        {
            return error("课程不存在或无权查看签到");
        }
        Long deptId = SecurityUtils.getDeptId();
        String pureClass = classCode.replace("班", "").trim();
        // 签到名单：本校 +（创建人/管理员/任教该班）。考勤课未必有「当前指派」校验，故不强制 assignment。
        Long userId = SecurityUtils.getUserId();
        if (!canViewAllCheckinClasses(lesson))
        {
            if (!canManageCheckinClass(userId, deptId, entryYear, pureClass))
            {
                return error("只能查看自己管理班级的签到");
            }
        }
        List<BizLessonCheckin> roster = lessonCheckinMapper.selectRosterByLessonAndClass(
                lessonId, pureClass, entryYear, deptId);
        long checked = roster.stream().filter(r -> r.getCheckinId() != null).count();
        return success(roster)
                .put("total", roster.size())
                .put("checkedInCount", checked)
                .put("lessonMode", lesson.getLessonMode() == null ? "assessment" : lesson.getLessonMode())
                .put("lessonTitle", lesson.getLessonTitle());
    }

    /** 直接查询课程，保留任课教师查看自己班级签到的既有权限边界。 */
    private BizLesson getAuthorizedCheckinLesson(Long lessonId)
    {
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        Long deptId = SecurityUtils.getDeptId();
        if (lesson == null || (lesson.getDeptId() != null && !lesson.getDeptId().equals(deptId)))
        {
            return null;
        }
        return lesson;
    }

    private boolean canViewAllCheckinClasses(BizLesson lesson)
    {
        Long userId = SecurityUtils.getUserId();
        return SecurityUtils.isAdmin(userId)
                || userId.equals(lesson.getCreatorId())
                || (lesson.getCreatorId() == null && SecurityUtils.getUsername().equals(lesson.getCreateBy()));
    }

    private boolean canManageCheckinClass(Long userId, Long deptId, String entryYear, String classCode)
    {
        BizTeacherClass probe = new BizTeacherClass();
        probe.setUserId(userId);
        probe.setDeptId(deptId);
        probe.setEntryYear(entryYear.trim());
        probe.setClassCode(classCode);
        return teacherClassMapper.checkTeacherClassExists(probe) > 0;
    }

    /**
     * 一站式保存课程所有信息 (用于课程设计器 "保存" 按钮)
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:add') or @ss.hasPermi('business:lesson:edit')")
    @Log(title = "课程设计与指派", businessType = BusinessType.INSERT,
            isSaveRequestData = false, isSaveResponseData = false)
    @PostMapping("/save-all")
    public AjaxResult saveAll(@RequestBody LessonDetailVo lessonDetailVo)
    {
        // 核心修复：使用 success() 方法替代 toAjax()
        return success(bizLessonService.saveLessonDetails(lessonDetailVo));
    }

    /**
     * 教师首页：读取统一课程推进策略（全校常规课共用）
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:query') or @ss.hasPermi('business:lesson:edit')")
    @GetMapping("/advance-policy")
    public AjaxResult getAdvancePolicy()
    {
        return success(bizLessonService.getTeacherAdvancePolicy());
    }

    /**
     * 教师首页：保存统一课程推进策略，并同步到该教师全部常规课
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:edit')")
    @Log(title = "课程推进设置", businessType = BusinessType.UPDATE)
    @PutMapping("/advance-policy")
    public AjaxResult updateAdvancePolicy(@RequestBody LessonDetailVo body)
    {
        return success(bizLessonService.updateTeacherAdvancePolicy(body));
    }

    /**
     * 手动一键课堂推进：多选班级，将各班当前课立刻切到下一课（达统一阈值，不等待延迟）
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:edit')")
    @Log(title = "手动一键课堂推进", businessType = BusinessType.UPDATE)
    @PostMapping("/manual-advance")
    public AjaxResult manualAdvance(@RequestBody Map<String, Object> body)
    {
        if (body == null)
        {
            return error("参数不能为空");
        }
        String entryYear = body.get("entryYear") == null ? null : String.valueOf(body.get("entryYear"));
        java.util.List<String> classCodes = new java.util.ArrayList<>();
        Object codes = body.get("classCodes");
        if (codes instanceof java.util.List)
        {
            for (Object item : (java.util.List<?>) codes)
            {
                if (item != null && StringUtils.isNotBlank(String.valueOf(item)))
                {
                    classCodes.add(String.valueOf(item));
                }
            }
        }
        // 兼容旧参数：单个 classCode
        if (classCodes.isEmpty() && body.get("classCode") != null)
        {
            classCodes.add(String.valueOf(body.get("classCode")));
        }
        Map<String, Object> data = lessonAutoAdvanceService.manualAdvanceClasses(entryYear, classCodes);
        return success(data).put("msg", data.get("message"));
    }


    // =================================================================
    // 以下是若依自动生成的标准接口
    // =================================================================

    /**
     * 查询课程管理列表
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizLesson bizLesson)
    {
        startPage();
        List<BizLesson> list = bizLessonService.selectBizLessonList(bizLesson);
        return getDataTable(list);
    }

    /**
     * 导出课程管理列表
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:export')")
    @Log(title = "课程管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizLesson bizLesson)
    {
        List<BizLesson> list = bizLessonService.selectBizLessonList(bizLesson);
        ExcelUtil<BizLesson> util = new ExcelUtil<BizLesson>(BizLesson.class);
        util.exportExcel(response, list, "课程管理数据");
    }

    /**
     * 获取课程管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:query')")
    @GetMapping(value = "/{lessonId}")
    public AjaxResult getInfo(@PathVariable("lessonId") Long lessonId)
    {
        return success(bizLessonService.selectBizLessonByLessonId(lessonId));
    }

    /**
     * 新增课程管理
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:add')")
    @Log(title = "课程管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizLesson bizLesson)
    {
        return error("课程新增必须通过课程设计器保存");
    }

    /**
     * 修改课程管理
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:edit')")
    @Log(title = "课程管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizLesson bizLesson)
    {
        return error("课程修改必须通过课程设计器保存");
    }

    /**
     * 删除课程管理
     */
    @PreAuthorize("@ss.hasPermi('business:lesson:remove')")
    @Log(title = "课程管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{lessonIds}")
    public AjaxResult remove(@PathVariable Long[] lessonIds)
    {
        return toAjax(bizLessonService.deleteBizLessonByLessonIds(lessonIds));
    }
}
