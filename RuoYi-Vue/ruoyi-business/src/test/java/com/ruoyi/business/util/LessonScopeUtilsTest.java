package com.ruoyi.business.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.common.exception.ServiceException;

/**
 * 常规入口的课程范围守卫：旧数据没有 lesson_scope 时按常规课放行，
 * 拓展课和历史课必须在指派、导学单、协作、分组、物联网等常规入口被拒绝。
 */
class LessonScopeUtilsTest
{
    @Test
    void legacyRowsWithoutScopeCountAsRegular()
    {
        BizLesson lesson = new BizLesson();
        assertTrue(LessonScopeUtils.isRegular(lesson));
        assertDoesNotThrow(() -> LessonScopeUtils.requireRegular(lesson, "拒绝"));
    }

    @Test
    void regularLessonPasses()
    {
        BizLesson lesson = new BizLesson();
        lesson.setLessonScope("regular");
        assertDoesNotThrow(() -> LessonScopeUtils.requireRegular(lesson, "拒绝"));
    }

    @Test
    void extensionAndLegacyLessonsAreRejected()
    {
        BizLesson extension = new BizLesson();
        extension.setLessonScope("extension");
        assertFalse(LessonScopeUtils.isRegular(extension));
        assertThrows(ServiceException.class, () -> LessonScopeUtils.requireRegular(extension, "拒绝"));

        BizLesson legacy = new BizLesson();
        legacy.setLessonScope("legacy");
        assertThrows(ServiceException.class, () -> LessonScopeUtils.requireRegular(legacy, "拒绝"));
    }
}
