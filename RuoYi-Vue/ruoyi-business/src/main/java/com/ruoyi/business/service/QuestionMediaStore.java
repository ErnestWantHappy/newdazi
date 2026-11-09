package com.ruoyi.business.service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import com.ruoyi.common.exception.ServiceException;

/** 本功能的小表使用参数化SQL，避免将文件路径和业务授权混入通用上传。 */
@Repository
public class QuestionMediaStore {
    private final JdbcTemplate jdbc;
    public QuestionMediaStore(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public JdbcTemplate jdbc() { return jdbc; }
    public Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
        return rows.isEmpty() ? null : rows.get(0);
    }
    public long count(String sql, Object... args) {
        Long n = jdbc.queryForObject(sql, Long.class, args);
        return n == null ? 0 : n;
    }
    public long insert(String sql, Object... args) {
        GeneratedKeyHolder holder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i=0; i<args.length; i++) ps.setObject(i+1, args[i]);
            return ps;
        }, holder);
        if (holder.getKey() == null) throw new ServiceException("未能保存记录，请重试");
        return holder.getKey().longValue();
    }
    public static long number(Object n) { return ((Number) n).longValue(); }
}
