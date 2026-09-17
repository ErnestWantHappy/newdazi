package com.ruoyi.quartz.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.business.service.PracticalGradingDeadlineService;

/**
 * 操作题批改期限漏触发补偿。
 */
@Component("practicalGradingDeadlineTask")
public class PracticalGradingDeadlineTask
{
    @Autowired
    private PracticalGradingDeadlineService deadlineService;

    public String reconcileTriggers()
    {
        return deadlineService.reconcileTriggers().toString();
    }
}
