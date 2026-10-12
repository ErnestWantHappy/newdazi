package com.ruoyi.business.service;

import java.sql.*;
import java.util.*;
import com.ruoyi.business.domain.BizStudentTaskState;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizStudentTaskStateMapper;
import com.ruoyi.business.mapper.ClassGroupingMapper;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;

/** 只在显式指定的隔离库创建独立随机前缀验收表，真实 Mapper SQL 的结果不能由 mock 替代。 */
@EnabledIfEnvironmentVariable(named = "REVIEW_MYSQL_URL", matches = ".*xueyeceping_classroom_accept_20261002.*")
class PracticalWithdrawalMysqlTest {
    @Test void deletionAndStateTransitionUseRealSqlAndRollbackTogether() throws Exception {
        UnpooledDataSource ds = new UnpooledDataSource("com.mysql.cj.jdbc.Driver", System.getenv("REVIEW_MYSQL_URL"),
                System.getenv("REVIEW_MYSQL_USER"), System.getenv("REVIEW_MYSQL_PASSWORD"));
        // 业务模块不额外引入运行时驱动，隔离验收复用后端制品中的现有驱动。
        if (System.getenv("REVIEW_MYSQL_DRIVER") != null) {
            ds.setDriverClassLoader(new java.net.URLClassLoader(new java.net.URL[]{
                    new java.io.File(System.getenv("REVIEW_MYSQL_DRIVER")).toURI().toURL()
            }, getClass().getClassLoader()));
        }
        final String prefix = "review_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "_";
        Configuration config = new Configuration(new Environment("review", new JdbcTransactionFactory(), ds));
        config.addInterceptor(new TestTables(prefix));
        config.getTypeAliasRegistry().registerAliases("com.ruoyi.business.domain");
        for (String mapper : new String[]{"BizStudentAnswerMapper", "BizStudentTaskStateMapper", "ClassGroupingMapper"}) {
            String resource = "mapper/business/" + mapper + ".xml";
            try (java.io.InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
                new XMLMapperBuilder(in, config, resource, config.getSqlFragments()).parse();
            }
        }
        try (SqlSession session = new SqlSessionFactoryBuilder().build(config).openSession()) {
            Connection connection = session.getConnection();
            try (Statement st = connection.createStatement()) {
                st.execute("CREATE TABLE " + prefix + "biz_student_answer LIKE biz_student_answer");
                st.execute("CREATE TABLE " + prefix + "biz_student_task_state LIKE biz_student_task_state");
                st.execute("CREATE TABLE " + prefix + "biz_question LIKE biz_question");
                st.execute("CREATE TABLE " + prefix + "biz_lesson_question LIKE biz_lesson_question");
                st.execute("INSERT INTO " + prefix + "biz_student_answer(answer_id,student_id,lesson_id,question_id,student_answer,score,submit_time,practical_artifact_id,practical_version_id) VALUES(1,7,9,10,'work.pdf',NULL,NOW(),11,12)");
                st.execute("INSERT INTO " + prefix + "biz_question(question_id,question_type,question_content) VALUES(10,'practical','测试')");
                st.execute("INSERT INTO " + prefix + "biz_lesson_question(lesson_id,question_id,question_score) VALUES(9,10,10)");
            }
            BizStudentTaskState state = new BizStudentTaskState();
            state.setDeptId(3L); state.setLessonId(9L); state.setQuestionId(10L); state.setStudentId(7L);
            state.setChangedAt(new java.util.Date()); state.setTaskState("SUBMITTED");
            BizStudentTaskStateMapper states = session.getMapper(BizStudentTaskStateMapper.class);
            states.upsert(state); session.commit();
            // 普通心跳不能把已提交降成作答中。
            state.setTaskState("WORKING"); states.upsert(state);
            assertEquals("SUBMITTED", states.selectOneForUpdate(9L, 10L, 7L).getTaskState());
            session.getMapper(BizStudentAnswerMapper.class).clearPracticalAnswer(1L, 11L);
            states.withdrawSubmission(state);
            assertEquals("WORKING", states.selectOneForUpdate(9L, 10L, 7L).getTaskState());
            assertEquals(Long.valueOf(2), states.selectOneForUpdate(9L, 10L, 7L).getStateVersion());
            try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT student_answer,submit_time,practical_version_id FROM " + prefix + "biz_student_answer WHERE answer_id=1")) {
                assertTrue(rs.next()); assertEquals("", rs.getString(1)); assertNull(rs.getTimestamp(2)); assertNull(rs.getObject(3));
            }
            Map<String,Object> overview = session.getMapper(ClassGroupingMapper.class).selectDesktopAnswerOverview(9L, Collections.singletonList(7L)).get(0);
            assertEquals(0, ((Number)overview.get("practicalSubmittedCount")).intValue());
            assertEquals(0, ((Number)overview.get("practicalAnsweredCount")).intValue());
            BizStudentAnswerMapper answerMapper = session.getMapper(BizStudentAnswerMapper.class);
            Map<String,Object> score = answerMapper.selectScoreSummaryByStudent(7L, 9L).get(0);
            assertEquals(0, ((Number)score.get("practicalPending")).intValue());
            assertEquals(0, ((Number)score.get("submittedAnswerCount")).intValue());
            score = answerMapper.selectScoreSummaryByStudents(Collections.singletonList(7L), Collections.singletonList(9L)).get(0);
            assertEquals(0, ((Number)score.get("practicalPending")).intValue());
            assertEquals(0, ((Number)score.get("submittedAnswerCount")).intValue());
            session.rollback();
            assertEquals("SUBMITTED", states.selectOneForUpdate(9L, 10L, 7L).getTaskState());
            try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT submit_time,practical_version_id FROM " + prefix + "biz_student_answer WHERE answer_id=1")) {
                assertTrue(rs.next()); assertNotNull(rs.getTimestamp(1)); assertEquals(12L, rs.getLong(2));
            }
            score = answerMapper.selectScoreSummaryByStudent(7L, 9L).get(0);
            assertEquals(1, ((Number)score.get("practicalPending")).intValue());
            try (Statement st = connection.createStatement()) { st.executeUpdate("UPDATE " + prefix + "biz_student_answer SET score=0 WHERE answer_id=1"); }
            session.clearCache();
            score = answerMapper.selectScoreSummaryByStudent(7L, 9L).get(0);
            assertEquals(0, ((Number)score.get("practicalPending")).intValue());
            assertEquals(1, ((Number)score.get("submittedAnswerCount")).intValue());
        } finally {
            // 仅清理本测试随机命名的四张表，绝不删除业务表。
            try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
                for (String name : TABLES) st.execute("DROP TABLE IF EXISTS " + prefix + name);
            }
        }
    }
    private static final String[] TABLES = {"biz_student_answer", "biz_student_task_state", "biz_question", "biz_lesson_question"};
    @org.apache.ibatis.plugin.Intercepts(@org.apache.ibatis.plugin.Signature(type=org.apache.ibatis.executor.statement.StatementHandler.class,
            method="prepare", args={Connection.class, Integer.class}))
    public static class TestTables implements org.apache.ibatis.plugin.Interceptor {
        private final String prefix;
        TestTables(String prefix) { this.prefix = prefix; }
        @Override public Object intercept(org.apache.ibatis.plugin.Invocation invocation) throws Throwable {
            org.apache.ibatis.executor.statement.StatementHandler handler = (org.apache.ibatis.executor.statement.StatementHandler) invocation.getTarget();
            org.apache.ibatis.mapping.BoundSql bound = handler.getBoundSql();
            String sql = bound.getSql();
            for (String name : TABLES) sql = sql.replaceAll("\\b" + name + "\\b", prefix + name);
            org.apache.ibatis.reflection.SystemMetaObject.forObject(bound).setValue("sql", sql);
            return invocation.proceed();
        }
    }
}
