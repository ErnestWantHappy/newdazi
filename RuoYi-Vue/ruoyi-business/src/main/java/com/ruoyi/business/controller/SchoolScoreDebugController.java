package com.ruoyi.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

/**
 * 临时数据库调试接口
 */
@RestController
@RequestMapping("/business/debug")
public class SchoolScoreDebugController extends BaseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/checkData")
    public AjaxResult checkData() {
        StringBuilder sb = new StringBuilder();
        
        // 1. 检查最近的 biz_lesson
        List<Map<String, Object>> lessons = jdbcTemplate.queryForList(
            "SELECT lesson_id, lesson_title, create_time FROM biz_lesson ORDER BY create_time DESC LIMIT 5");
        sb.append("最近的5个课程:\n");
        for(Map<String, Object> m : lessons) {
            sb.append(m).append("\n");
        }
        
        // 2. 检查最近的 biz_student_answer
        List<Map<String, Object>> answers = jdbcTemplate.queryForList(
            "SELECT answer_id, lesson_id, student_id, submit_time FROM biz_student_answer ORDER BY submit_time DESC LIMIT 5");
        sb.append("\n最近的5条答题记录:\n");
        for(Map<String, Object> m : answers) {
            sb.append(m).append("\n");
        }

        // 3. 检查关联查询 (不带时间限制)
        String joinSql = "SELECT COUNT(*) as count FROM biz_student_answer a " +
                         "INNER JOIN biz_lesson l ON a.lesson_id = l.lesson_id " +
                         "INNER JOIN biz_student s ON a.student_id = s.student_id " +
                         "INNER JOIN sys_user u ON s.user_id = u.user_id " +
                         "INNER JOIN sys_dept d ON u.dept_id = d.dept_id " +
                         "WHERE d.del_flag = '0'";
        Map<String, Object> countMap = jdbcTemplate.queryForMap(joinSql);
        sb.append("\n无时间限制的关联总数: ").append(countMap.get("count"));

        return AjaxResult.success(sb.toString());
    }
}
