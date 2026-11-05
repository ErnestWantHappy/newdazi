package com.ruoyi.business.service;

import com.ruoyi.business.domain.BizQuestion;
import java.io.InputStream;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuestionWorkbenchMapperTest {
    private String sql(String mode) throws Exception {
        Configuration config = new Configuration();
        config.getTypeAliasRegistry().registerAlias("BizQuestion", BizQuestion.class);
        String resource = "mapper/business/BizQuestionMapper.xml";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            new XMLMapperBuilder(input, config, resource, config.getSqlFragments()).parse();
        }
        BizQuestion query = new BizQuestion();
        query.setQuestionType("practical");
        query.setPracticalMode(mode);
        query.setCreatorId(7L);
        return config.getMappedStatement("com.ruoyi.business.mapper.BizQuestionMapper.selectBizQuestionList")
            .getBoundSql(query).getSql().replaceAll("\\s+", " ");
    }

    @Test void fileScopeIncludesOnlyFilesAndLegacyNullModes() throws Exception {
        String sql = sql("FILE");
        assertTrue(sql.contains("(q.practical_mode = 'FILE' or q.practical_mode is null or q.practical_mode = '')"));
        assertTrue(sql.contains("q.question_type = 'practical'"));
        // 分类条件不能改变私有题归属范围。
        assertTrue(sql.contains("q.creator_id = ?"));
    }

    @Test void otherModesKeepExactFilterAndUnscopedQueryRemainsUnscoped() throws Exception {
        for (String mode : new String[] {"PYTHON", "DATA_LAB", "FLOWCHART"}) {
            assertTrue(sql(mode).contains("q.practical_mode = ?"));
            assertFalse(sql(mode).contains("q.practical_mode is null"));
        }
        assertFalse(sql(null).contains("q.practical_mode = ?"));
        assertFalse(sql(null).contains("q.practical_mode is null"));
    }
}
