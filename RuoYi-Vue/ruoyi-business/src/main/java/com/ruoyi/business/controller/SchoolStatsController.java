package com.ruoyi.business.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.business.domain.vo.SchoolClassStatsVO;
import com.ruoyi.business.service.ISchoolStatsService;

/**
 * 学校班级统计Controller
 * 
 * @author ruoyi
 * @date 2026-02-03
 */
@RestController
@RequestMapping("/business/schoolStats")
public class SchoolStatsController extends BaseController
{
    @Autowired
    private ISchoolStatsService schoolStatsService;

    /**
     * 查询学校班级统计列表
     * 返回每个学校每个年级每个班级的学生人数
     */
    @PreAuthorize("@ss.hasPermi('business:schoolStats:list')")
    @GetMapping("/list")
    public AjaxResult list()
    {
        List<SchoolClassStatsVO> list = schoolStatsService.selectSchoolClassStats();
        return success(list);
    }

    /**
     * 查询学校汇总统计
     * 返回每个学校的班级总数和学生总数
     */
    @PreAuthorize("@ss.hasPermi('business:schoolStats:list')")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        List<SchoolClassStatsVO> list = schoolStatsService.selectSchoolSummary();
        return success(list);
    }
}
