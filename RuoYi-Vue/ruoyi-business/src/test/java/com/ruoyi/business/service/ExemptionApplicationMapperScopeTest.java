package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

/**
 * 免抽测班级证据必须排除拓展班载体班级，否则没有行政班学生的空班会拉低达标结论。
 */
class ExemptionApplicationMapperScopeTest {
    @Test
    void teacherClassesAndMetricsIgnoreExtensionCarrierClasses() throws Exception {
        Configuration configuration = configuration();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("teacherId", 7L); params.put("deptId", 139L); params.put("entryYear", "2026");
        params.put("startTime", new Date(0L)); params.put("endTime", new Date());
        String classes = sql(configuration,
                "com.ruoyi.business.mapper.ExemptionApplicationMapper.selectTeacherClasses", params);
        String metrics = sql(configuration,
                "com.ruoyi.business.mapper.ExemptionApplicationMapper.selectTeacherCourseMetrics", params);

        assertTrue(classes.contains("NOT EXISTS ( SELECT 1 FROM biz_extension_class ext"));
        assertTrue(classes.contains("ext.class_code = candidate.class_code"));
        // 四类课程来源都要限定常规课：载体班级的拓展课不能进入免抽测使用率。
        assertEquals(4, count(metrics, "IFNULL(lesson.lesson_scope,'regular') = 'regular'"), metrics);
        assertEquals(1, count(classes, "biz_extension_class"), classes);
        assertEquals(1, count(metrics, "biz_extension_class"), metrics);
    }

    private String sql(Configuration configuration, String id, Map<String, Object> params) {
        return configuration.getMappedStatement(id).getBoundSql(params).getSql().replaceAll("\\s+", " ");
    }

    private Configuration configuration() throws Exception {
        Configuration configuration = new Configuration();
        String resource = "mapper/business/ExemptionApplicationMapper.xml";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        return configuration;
    }

    private int count(String text, String part) {
        int total = 0, index = 0;
        while ((index = text.indexOf(part, index)) >= 0) { total++; index += part.length(); }
        return total;
    }
}
