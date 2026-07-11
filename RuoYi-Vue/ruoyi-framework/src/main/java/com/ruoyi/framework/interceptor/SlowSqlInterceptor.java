package com.ruoyi.framework.interceptor;

import java.util.Properties;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.system.service.ISysPerfEventService;

/**
 * MyBatis 慢 SQL 拦截器
 */
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
    @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class })
})
public class SlowSqlInterceptor implements Interceptor
{
    private static final Logger log = LoggerFactory.getLogger(SlowSqlInterceptor.class);

    private static final long SLOW_SQL_THRESHOLD_MS = 1000L;

    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {
        long start = System.currentTimeMillis();
        try
        {
            return invocation.proceed();
        }
        finally
        {
            long duration = System.currentTimeMillis() - start;
            if (duration >= SLOW_SQL_THRESHOLD_MS)
            {
                recordSlowSql(invocation, duration);
            }
        }
    }

    private void recordSlowSql(Invocation invocation, long duration)
    {
        try
        {
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            Object parameter = invocation.getArgs()[1];
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            final String mapperId = mappedStatement.getId();
            final String sql = boundSql.getSql();
            AsyncManager.me().execute(new java.util.TimerTask()
            {
                @Override
                public void run()
                {
                    try
                    {
                        SpringUtils.getBean(ISysPerfEventService.class).recordSlowSql(mapperId, sql, duration);
                    }
                    catch (Exception e)
                    {
                        log.debug("record slow sql skipped: {}", e.getMessage());
                    }
                }
            });
        }
        catch (Exception e)
        {
            log.debug("slow sql interceptor skipped: {}", e.getMessage());
        }
    }

    @Override
    public Object plugin(Object target)
    {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties)
    {
    }
}
