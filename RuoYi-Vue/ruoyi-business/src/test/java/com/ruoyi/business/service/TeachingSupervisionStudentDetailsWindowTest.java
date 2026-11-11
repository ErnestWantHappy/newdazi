package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

/** 学生明细的操作题统计必须和班级汇总使用同一个左闭右开活动时间窗，否则两行数字对不上。 */
class TeachingSupervisionStudentDetailsWindowTest {

    @Test
    void practicalJoinUsesActivityStartInclusiveAndEndExclusive() throws Exception {
        Configuration configuration = new Configuration();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("mapper/business/TeachingSupervisionMapper.xml")) {
            new XMLMapperBuilder(input, configuration, "TeachingSupervisionMapper.xml",
                    configuration.getSqlFragments()).parse();
        }
        Map<String, Object> q = new HashMap<>();
        q.put("deptId", 1L);
        q.put("lessonId", 2L);
        q.put("entryYear", 2024);
        q.put("classCode", "1");
        q.put("keyword", null);
        q.put("activityStartTime", new Date(1000L));
        q.put("activityEndTime", new Date(2000L));
        Map<String, Object> params = new HashMap<>();
        params.put("q", q);
        String sql = configuration.getMappedStatement(
                "com.ruoyi.business.mapper.TeachingSupervisionMapper.selectStudentDetails")
                .getBoundSql(params).getSql().replaceAll("\\s+", " ");
        assertTrue(sql.contains("practical.lesson_id = ? AND practical.submit_time >= ? AND practical.submit_time < ?"));
        assertTrue(sql.contains("practical.student_answer IS NOT NULL"));
    }
}
