package com.ruoyi.business.service;

import java.util.Map;

/**
 * 课程自动 / 手动推进下一课。
 * 规则：常规课；班级「有成绩」人数占比达阈值后，把指派切到同教师同年级下一课次。
 * 课堂考勤永不参与。
 */
public interface LessonAutoAdvanceService
{
    /**
     * 定时扫描并执行可推进班级（含延迟等待）。
     * @return 摘要：scanned / advanced / skipped 等
     */
    Map<String, Object> scanAndAdvance();

    /**
     * 教师手动一键课堂推进：指定一个或多个班级（自动识别各班当前课），
     * 有成绩达统一阈值即立刻切到下一课，不等待延迟。
     *
     * @param entryYear  入学年
     * @param classCodes 班号列表（可带「班」）
     * @return advanced / failed / details / message 等
     */
    Map<String, Object> manualAdvanceClasses(String entryYear, java.util.List<String> classCodes);
}
