package com.ruoyi.business.util;

import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.common.exception.ServiceException;

/**
 * 常规课程入口的课程范围守卫。
 *
 * 拓展课有独立的签到、题单与成员体系，历史课只读；它们如果被普通课程接口（指派、导学单、协作、分组、物联网）
 * 按 lessonId 直接引用，会污染常规班的当前课或写入不该有的业务数据。
 */
public final class LessonScopeUtils
{
    private LessonScopeUtils() { }

    /** 旧数据没有 lesson_scope，按常规课处理，避免历史课程被整体拒绝。 */
    public static boolean isRegular(BizLesson lesson)
    {
        return lesson == null || lesson.getLessonScope() == null || "regular".equals(lesson.getLessonScope());
    }

    public static void requireRegular(BizLesson lesson, String message)
    {
        if (!isRegular(lesson))
        {
            throw new ServiceException(message);
        }
    }
}
