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
    
    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

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
        
        System.out.println("====== 学校成绩查询 DEBUG START ======");
        System.out.println("请求参数 academicYear: " + academicYear + ", semester: " + semester);
        System.out.println("计算日期范围: " + dates[0] + " ~ " + dates[1]);
        
        List<SchoolScoreStatsVO> list = schoolScoreMapper.selectSchoolScoreStats(dates[0], dates[1], deptName);
        
        System.out.println("查询结果数量: " + (list != null ? list.size() : "null"));
        
        if (list == null || list.isEmpty()) {
            System.out.println("!!! 发现结果为空，开始深度诊断 !!!");
            try {
                // 1. 检查有没有该时间段的课程
                Integer lessonCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM biz_lesson WHERE create_time BETWEEN ? AND ?", Integer.class, dates[0], dates[1]);
                System.out.println("诊断1 - 该时间段内的课程数量: " + lessonCount);
                if (lessonCount > 0) {
                     List<Map<String, Object>> lessons = jdbcTemplate.queryForList(
                        "SELECT lesson_id, lesson_title, create_time FROM biz_lesson WHERE create_time BETWEEN ? AND ? LIMIT 3", dates[0], dates[1]);
                     System.out.println("       示例课程: " + lessons);
                } else {
                     // 查询最近的一条课程记录
                     try {
                        Map<String, Object> latestLesson = jdbcTemplate.queryForMap(
                            "SELECT create_time FROM biz_lesson ORDER BY create_time DESC LIMIT 1");
                        System.out.println("       !!! 系统中最近的一条课程时间是: " + latestLesson.get("create_time"));
                     } catch (Exception ex) {
                        System.out.println("       !!! 系统中似乎没有任何课程记录");
                     }
                }

                // 2. 检查总答题记录表
                Integer totalAnswers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM biz_student_answer", Integer.class);
                System.out.println("诊断2 - 总答题记录数: " + totalAnswers);

                // 3. 检查关联逻辑 (无时间限制)
                String joinCheckSql = "SELECT COUNT(*) FROM biz_student_answer a " +
                                      "INNER JOIN biz_lesson l ON a.lesson_id = l.lesson_id " +
                                      "INNER JOIN biz_student s ON a.student_id = s.student_id " +
                                      "INNER JOIN sys_user u ON s.user_id = u.user_id " +
                                      "INNER JOIN sys_dept d ON u.dept_id = d.dept_id";
                Integer joinCount = jdbcTemplate.queryForObject(joinCheckSql, Integer.class);
                System.out.println("诊断3 - 全表完整关联(Inner Join)的记录数: " + joinCount);
                
                if (joinCount == 0) {
                     System.out.println("       !!! 严重: 关联查询结果为0，说明表之间关联断裂。可能是 student_id 或 user_id 或 dept_id 对应不上");
                }

            } catch (Exception e) {
                System.out.println("诊断过程出错: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("第一条数据: " + list.get(0).getDeptName() + " - " + list.get(0).getAvgTypingSpeed());
        }
        System.out.println("====== 学校成绩查询 DEBUG END ======");
        
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
