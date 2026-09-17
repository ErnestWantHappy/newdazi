package com.ruoyi.business.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.vo.SchoolScoreStatsVO;
import com.ruoyi.business.mapper.SchoolScoreMapper;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 学校成绩查询 Controller
 */
@RestController
@RequestMapping("/business/schoolScore")
public class SchoolScoreController extends BaseController {

    @Autowired
    private SchoolScoreMapper schoolScoreMapper;
    

    /**
     * 查询全区各校成绩统计
     */
    @PreAuthorize("@ss.hasPermi('business:schoolScore:stats')")
    @GetMapping("/termStats")
    public AjaxResult termStats(
            @RequestParam String academicYear, // 如 "2024" (表示2024-2025学年)
            @RequestParam String semester,     // "1" 或 "2"
            @RequestParam(required = false) String deptName) {
        String[] dates = getSemesterDateRange(academicYear, semester);

        List<SchoolScoreStatsVO> list = schoolScoreMapper.selectSchoolScoreStats(dates[0], dates[1], deptName);
        return AjaxResult.success(list);
    }

    /**
     * 查询指定学校的班级详情
     */
    @PreAuthorize("@ss.hasPermi('business:schoolScore:stats')")
    @GetMapping("/classDetails")
    public TableDataInfo classDetails(
            @RequestParam String academicYear,
            @RequestParam String semester,
            @RequestParam Long deptId) {
        
        String[] dates = getSemesterDateRange(academicYear, semester);
        startPage();
        List<SchoolScoreStatsVO> list = schoolScoreMapper.selectClassScoreStats(dates[0], dates[1], deptId);
        return getDataTable(list);
    }

    /**
     * 计算学期时间范围
     * 第一学期：当年9月1日 ~ 次年1月31日
     * 第二学期：次年2月1日 ~ 次年8月31日
     */
    private String[] getSemesterDateRange(String academicYearStr, String semester) {
        int year = Integer.parseInt(academicYearStr);
        String startDate, endDate;

        if ("1".equals(semester)) {
            // 第一学期: 2024-09-01 ~ 2025-01-31
            startDate = year + "-09-01";
            endDate = (year + 1) + "-01-31";
        } else {
            // 第二学期: 2025-02-01 ~ 2025-08-31
            startDate = (year + 1) + "-02-01";
            endDate = (year + 1) + "-08-31";
        }
        return new String[]{startDate, endDate};
    }
}
