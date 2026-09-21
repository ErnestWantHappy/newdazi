package com.ruoyi.business.service;

import java.io.InputStream;
import java.util.Collections;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResearchShareMapperTest
{
    @Test
    void shareCiphertextIsReadSavedAndRevoked() throws Exception
    {
        String resource = "mapper/business/ResearchActivityMapper.xml";
        Configuration config = new Configuration();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource))
        {
            assertNotNull(input);
            new XMLMapperBuilder(input, config, resource, config.getSqlFragments()).parse();
        }
        String prefix = "com.ruoyi.business.mapper.ResearchActivityMapper.";
        for (String id : new String[] {"selectTopicByIdAny", "updatePublicShare", "revokePublicShare"})
        {
            String sql = config.getMappedStatement(prefix + id).getBoundSql(Collections.emptyMap()).getSql();
            assertTrue(sql.contains("public_share_token_ciphertext"), id);
        }
        assertTrue(config.getResultMap(prefix + "TopicEntityResult").getResultMappings().stream()
                .anyMatch(mapping -> "publicShareTokenCiphertext".equals(mapping.getProperty())));
    }
}
