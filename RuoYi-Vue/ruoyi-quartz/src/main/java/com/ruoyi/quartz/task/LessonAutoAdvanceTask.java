package com.ruoyi.quartz.task;

import com.ruoyi.business.service.LessonAutoAdvanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 课程自动推进下一课定时任务（P2-B3）。
 * 调用目标：lessonAutoAdvanceTask.scanAndAdvance
 */
@Component("lessonAutoAdvanceTask")
public class LessonAutoAdvanceTask
{
    @Autowired
    private LessonAutoAdvanceService lessonAutoAdvanceService;

    public String scanAndAdvance()
    {
        Map<String, Object> result = lessonAutoAdvanceService.scanAndAdvance();
        return String.format("自动推进扫描完成：扫描 %s 门课，推进班级 %s 次，跳过 %s",
                result.get("scanned"),
                result.get("advanced"),
                result.get("skipped"));
    }
}
