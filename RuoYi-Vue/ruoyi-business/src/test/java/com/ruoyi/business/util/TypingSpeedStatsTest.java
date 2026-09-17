package com.ruoyi.business.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 服务端打字统计口径：伪造值必须被重算与封顶拦截。 */
class TypingSpeedStatsTest
{
    @Test
    void forgedSpeedIsRecomputedAndCapped()
    {
        // 控制台 2 秒注入 290 字正确全文：原始速度 8700，封顶 500（客户端自报的 88888 根本不再读取）。
        TypingSpeedStats stats = TypingSpeedStats.resolve(290, 290, 290, 2);
        assertEquals(500, stats.getTypingSpeed());
        assertEquals(100.0, stats.getAccuracyRate());
        assertEquals(100.0, stats.getCompletionRate());
    }

    @Test
    void normalInputKeepsSaneStats()
    {
        // 180 秒打对 90/100 字：速度 30，正确率 90，站点完成率按原文 290 算约 31。
        TypingSpeedStats stats = TypingSpeedStats.resolve(90, 100, 290, 180);
        assertEquals(30, stats.getTypingSpeed());
        assertEquals(90.0, stats.getAccuracyRate());
        assertEquals(31.0, stats.getCompletionRate());
    }

    @Test
    void illegalTimeFallsBackToOneMinute()
    {
        TypingSpeedStats stats = TypingSpeedStats.resolve(60, 60, 290, 0);
        assertEquals(60, stats.getTypingSpeed());
    }

    @Test
    void emptyInputYieldsZeroWithoutCrash()
    {
        TypingSpeedStats stats = TypingSpeedStats.resolve(0, 0, 290, 300);
        assertEquals(0, stats.getTypingSpeed());
        assertEquals(0.0, stats.getAccuracyRate());
        assertEquals(0.0, stats.getCompletionRate());
    }

    @Test
    void countCorrectPrefixMatchesLegacyLoop()
    {
        assertEquals(3, TypingSpeedStats.countCorrectPrefix("abcdef", "abcXYZ"));
        assertEquals(0, TypingSpeedStats.countCorrectPrefix("abc", "XYZ"));
        assertEquals(0, TypingSpeedStats.countCorrectPrefix(null, "abc"));
        assertEquals(0, TypingSpeedStats.countCorrectPrefix("abc", null));
    }

    @Test
    void clampSpeedBlocksExtremeClientValue()
    {
        assertEquals(500, TypingSpeedStats.clampSpeed(88888));
        assertEquals(45, TypingSpeedStats.clampSpeed(45));
        assertEquals(0, TypingSpeedStats.clampSpeed(null));
        assertEquals(0, TypingSpeedStats.clampSpeed(-10));
    }
}
