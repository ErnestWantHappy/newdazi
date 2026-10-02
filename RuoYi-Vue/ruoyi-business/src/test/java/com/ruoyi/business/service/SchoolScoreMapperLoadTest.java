package com.ruoyi.business.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 真实加载 Mapper，避免仅编译 Java 时漏掉 XML 损坏。 */
class SchoolScoreMapperLoadTest
{
    @Test
    void schoolAndClassQueriesLoadWithAndWithoutSchoolFilter() throws Exception
    {
        String resource = "mapper/business/SchoolScoreMapper.xml";
        Configuration config = new Configuration();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            new XMLMapperBuilder(input, config, resource, config.getSqlFragments()).parse();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", "2026-09-01");
        params.put("endDate", "2026-09-20");
        params.put("deptId", 167L);
        for (String filter : new String[] {null, "学校"}) {
            params.put("deptName", filter);
            for (String id : new String[] {"selectSchoolScoreStats", "selectClassScoreStats"}) {
                assertNotNull(config.getMappedStatement("com.ruoyi.business.mapper.SchoolScoreMapper." + id)
                        .getBoundSql(params).getSql());
            }
        }
        params.put("entryYear", "2024");
        params.put("schoolType", "2");
        for (String id : new String[] {"selectSchoolScoreStats", "selectClassScoreStats"}) {
            String sql = config.getMappedStatement("com.ruoyi.business.mapper.SchoolScoreMapper." + id)
                    .getBoundSql(params).getSql();
            assertTrue(sql.contains("s.entry_year = ?"));
            assertTrue(sql.contains("d.school_type = ?"));
        }
    }
}
