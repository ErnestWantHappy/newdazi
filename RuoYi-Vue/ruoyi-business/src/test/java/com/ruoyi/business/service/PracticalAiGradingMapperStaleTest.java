package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

/** 暂停恢复只能回收明显卡死的处理中结果，不能把刚派发、仍在请求模型的份重置后重复处理。 */
class PracticalAiGradingMapperStaleTest {

    @Test
    void resetStaleProcessingRequiresTimeoutAndJobScope() throws Exception {
        Configuration configuration = new Configuration();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("mapper/business/PracticalAiGradingMapper.xml")) {
            new XMLMapperBuilder(input, configuration, "PracticalAiGradingMapper.xml",
                    configuration.getSqlFragments()).parse();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("jobId", 88L);
        params.put("staleMinutes", 15);
        String sql = configuration.getMappedStatement(
                "com.ruoyi.business.mapper.PracticalAiGradingMapper.resetStaleProcessingResults")
                .getBoundSql(params).getSql().replaceAll("\\s+", " ");
        assertTrue(sql.contains("WHERE job_id=? AND result_status='PROCESSING'"));
        assertTrue(sql.contains("processing_started_time IS NULL OR processing_started_time < DATE_SUB(NOW(), INTERVAL ? MINUTE)"));
        assertTrue(sql.contains("SET result_status='PENDING'"));
    }
}
