package com.ruoyi.system.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.ruoyi.system.domain.SysPerfEvent;

/**
 * 系统性能事件 数据层
 */
public interface SysPerfEventMapper
{
    int insertSysPerfEvent(SysPerfEvent event);

    List<SysPerfEvent> selectRecentEvents(@Param("since") Date since, @Param("eventType") String eventType);

    int deleteBefore(@Param("before") Date before);

    @Select("select sleep(1.1) /* diag-self-test */")
    Integer selfTestSlowQuery();

    @Select("select event_id from sys_perf_event where event_type = 'slow_sql' "
            + "and sql_text like '%diag-self-test%' and occur_time >= date_sub(now(), interval 2 minute) "
            + "order by event_id desc limit 1")
    Long selectSelfTestEventId();
}
