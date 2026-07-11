package com.ruoyi.system.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysPerfEvent;

/**
 * 系统性能事件 数据层
 */
public interface SysPerfEventMapper
{
    int insertSysPerfEvent(SysPerfEvent event);

    List<SysPerfEvent> selectRecentEvents(@Param("since") Date since, @Param("eventType") String eventType);

    int deleteBefore(@Param("before") Date before);
}
