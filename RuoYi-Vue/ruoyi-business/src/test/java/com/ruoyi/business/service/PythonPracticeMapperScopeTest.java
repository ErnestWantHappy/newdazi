package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

/**
 * 历史拓展班题单的成员冻结在扩展成员表里，学情 SQL 不能只认 extension。
 */
class PythonPracticeMapperScopeTest {
    @Test
    void analyticsIncludesLegacyPlanScope() throws Exception {
        Configuration configuration = new Configuration();
        String resource = "mapper/business/PythonPracticeMapper.xml";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("planVersionId", 1L); params.put("deptId", 2L);
        for (String id : new String[] {
                "com.ruoyi.business.mapper.PythonPracticeMapper.selectPlanAnalyticsStudents",
                "com.ruoyi.business.mapper.PythonPracticeMapper.selectPlanAnalyticsQuestions" }) {
            String sql = configuration.getMappedStatement(id).getBoundSql(params).getSql()
                    .replaceAll("\\s+", " ");
            assertTrue(sql.contains("ap.plan_scope IN ('extension','legacy')"), id);
            assertTrue(sql.contains("ap.plan_scope='regular'"), id);
            assertEquals(1, count(sql, "ap.plan_scope IN ('extension','legacy')"), id);
        }
    }

    private int count(String text, String part) {
        int total = 0, index = 0;
        while ((index = text.indexOf(part, index)) >= 0) { total++; index += part.length(); }
        return total;
    }
}
