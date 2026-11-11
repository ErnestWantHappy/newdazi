package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.ruoyi.business.mapper.TextbookCaseFeedbackMapper;

/**
 * 同一投票行首次 INSERT 与后续 UPDATE 必须共享同一套理由语义：
 * 空串是明确清空，null 是快捷改票保留旧理由，撤销票必须清空理由。
 */
class TextbookCaseFeedbackVoteSqlTest {

    @Test
    void insertAndUpdateUseSameReasonSemantics() throws Exception {
        String sql = TextbookCaseFeedbackMapper.class.getMethod("vote", Long.class, Long.class, int.class, String.class)
                .getAnnotation(Insert.class).value()[0].replaceAll("\\s+", " ");
        assertTrue(sql.contains("VALUES(#{caseId},#{userId},#{vote},CASE WHEN #{vote}=0 THEN NULL ELSE NULLIF(#{reason},'') END)"),
                "首次插入必须把空串归一为 NULL，撤销票直接写 NULL");
        assertTrue(sql.contains("reason=CASE WHEN VALUES(vote)=0 THEN NULL WHEN #{reason} IS NOT NULL THEN NULLIF(#{reason},'') ELSE reason END"),
                "更新分支必须直接看参数：空串清空，null 保留旧理由");
        assertTrue(!sql.contains("VALUES(reason)"), "不能再用 INSERT 值判断，否则空串清空会退化成保留旧理由");
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "REVIEW_MYSQL_URL", matches = ".*xueyeceping_server_20260729.*")
    void realMysqlVoteSequenceKeepsSingleRowSemantics() throws Exception {
        UnpooledDataSource ds = new UnpooledDataSource("com.mysql.cj.jdbc.Driver",
                System.getenv("REVIEW_MYSQL_URL"), System.getenv("REVIEW_MYSQL_USER"),
                System.getenv("REVIEW_MYSQL_PASSWORD"));
        if (System.getenv("REVIEW_MYSQL_DRIVER") != null) {
            ds.setDriverClassLoader(new URLClassLoader(new URL[] {
                    new File(System.getenv("REVIEW_MYSQL_DRIVER")).toURI().toURL()
            }, getClass().getClassLoader()));
        }
        String prefix = "review_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "_";
        String table = prefix + "biz_textbook_case_feedback";
        Configuration config = new Configuration(new Environment("review", new JdbcTransactionFactory(), ds));
        config.addInterceptor(new PrefixTable(prefix));
        config.addMapper(TextbookCaseFeedbackMapper.class);
        try (SqlSession session = new SqlSessionFactoryBuilder().build(config).openSession()) {
            Connection connection = session.getConnection();
            try (Statement st = connection.createStatement()) {
                // 只复制结构到随机测试表，绝不写业务表。
                st.execute("CREATE TABLE " + table + " LIKE biz_textbook_case_feedback");
            }
            TextbookCaseFeedbackMapper mapper = session.getMapper(TextbookCaseFeedbackMapper.class);
            long caseId = 991001L;
            long userId = 991002L;
            mapper.vote(caseId, userId, 1, "");
            assertNull(reason(connection, table, caseId, userId), "首次空原因应为 NULL");
            mapper.vote(caseId, userId, 1, "好");
            assertEquals("好", reason(connection, table, caseId, userId));
            mapper.vote(caseId, userId, 1, null);
            assertEquals("好", reason(connection, table, caseId, userId), "快捷改票应保留旧理由");
            mapper.vote(caseId, userId, -1, "");
            assertNull(reason(connection, table, caseId, userId), "空串应清空理由");
            mapper.vote(caseId, userId, 0, null);
            assertNull(reason(connection, table, caseId, userId), "撤销票应清空理由");
            session.rollback();
        } finally {
            try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
                st.execute("DROP TABLE IF EXISTS " + table);
            }
        }
    }

    private String reason(Connection connection, String table, long caseId, long userId) throws Exception {
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery("SELECT reason FROM " + table
                        + " WHERE case_id=" + caseId + " AND user_id=" + userId)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    /** Mapper 注解写死业务表名，测试时只把 SQL 指向随机克隆表。 */
    @org.apache.ibatis.plugin.Intercepts(@org.apache.ibatis.plugin.Signature(
            type = org.apache.ibatis.executor.statement.StatementHandler.class,
            method = "prepare", args = { Connection.class, Integer.class }))
    public static class PrefixTable implements org.apache.ibatis.plugin.Interceptor {
        private final String prefix;

        PrefixTable(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Object intercept(org.apache.ibatis.plugin.Invocation invocation) throws Throwable {
            org.apache.ibatis.executor.statement.StatementHandler handler =
                    (org.apache.ibatis.executor.statement.StatementHandler) invocation.getTarget();
            org.apache.ibatis.mapping.BoundSql bound = handler.getBoundSql();
            String sql = bound.getSql().replaceAll("\\bbiz_textbook_case_feedback\\b",
                    prefix + "biz_textbook_case_feedback");
            org.apache.ibatis.reflection.SystemMetaObject.forObject(bound).setValue("sql", sql);
            return invocation.proceed();
        }
    }
}
