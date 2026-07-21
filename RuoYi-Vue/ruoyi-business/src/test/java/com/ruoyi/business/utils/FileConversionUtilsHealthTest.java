package com.ruoyi.business.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * LibreOffice 健康判定纯逻辑测试（不启动真实 Office 进程）。
 */
class FileConversionUtilsHealthTest {

    @Test
    void healthyWhenServiceUpAndNoHang() {
        String reason = FileConversionUtils.evaluateUnhealthyReason(
                true, 10, 25, 5, 0, 3, 0, 0L, 120_000L);
        assertNull(reason);
    }

    @Test
    void unhealthyWhenServiceUnavailable() {
        String reason = FileConversionUtils.evaluateUnhealthyReason(
                false, 0, 25, 5, 0, 3, 0, 0L, 120_000L);
        assertEquals("服务不可用", reason);
    }

    @Test
    void unhealthyWhenNoProcessButMarkedAvailable() {
        String reason = FileConversionUtils.evaluateUnhealthyReason(
                true, 0, 25, 5, 0, 3, 0, 0L, 120_000L);
        assertEquals("服务标记可用但无 soffice 进程", reason);
    }

    @Test
    void unhealthyWhenProcessCountExceedsThreshold() {
        String reason = FileConversionUtils.evaluateUnhealthyReason(
                true, 30, 25, 5, 0, 3, 0, 0L, 120_000L);
        assertNotNull(reason);
        assertEquals(true, reason.contains("进程数超过阈值"));
    }

    @Test
    void unhealthyWhenConsecutiveFailuresReachThreshold() {
        String reason = FileConversionUtils.evaluateUnhealthyReason(
                true, 10, 25, 5, 3, 3, 0, 0L, 120_000L);
        assertNotNull(reason);
        assertEquals(true, reason.contains("连续服务失败"));
    }

    @Test
    void healthyWhenFailuresBelowThreshold() {
        String reason = FileConversionUtils.evaluateUnhealthyReason(
                true, 10, 25, 5, 2, 3, 0, 0L, 120_000L);
        assertNull(reason);
    }

    @Test
    void unhealthyWhenInFlightSoftHangAndNoRecentSuccess() {
        String reason = FileConversionUtils.evaluateUnhealthyReason(
                true, 10, 25, 5, 0, 3, 2, 120_000L, 120_000L, 120_000L);
        assertNotNull(reason);
        assertEquals(true, reason.contains("在途转换挂起超时"));
    }

    @Test
    void healthyWhenInFlightLongButRecentSuccess() {
        // 高压排队：oldest 可能 > softHang，但池子仍在出 success，不应误杀
        String reason = FileConversionUtils.evaluateUnhealthyReason(
                true, 10, 25, 5, 0, 3, 5, 300_000L, 120_000L, 5_000L);
        assertNull(reason);
    }

    @Test
    void healthyWhenInFlightButBelowSoftHang() {
        String reason = FileConversionUtils.evaluateUnhealthyReason(
                true, 10, 25, 5, 0, 3, 2, 60_000L, 120_000L, -1L);
        assertNull(reason);
    }

    @Test
    void unhealthyWhenProcessTooFewWithFailures() {
        String reason = FileConversionUtils.evaluateUnhealthyReason(
                true, 1, 25, 5, 2, 3, 0, 0L, 120_000L);
        assertNotNull(reason);
        assertEquals(true, reason.contains("进程数严重偏少"));
    }

    @Test
    void lowProcessAloneDoesNotTriggerWithoutFailures() {
        // 启动毛刺：进程暂时偏少但无失败，不应误杀
        String reason = FileConversionUtils.evaluateUnhealthyReason(
                true, 1, 25, 5, 0, 3, 0, 0L, 120_000L);
        assertNull(reason);
    }


    @Test
    void unhealthyWhenProcessTooFewWithInFlight() {
        // 半死：仅剩 1~2 个进程（exe+bin），在途卡住，即使失败计数仍为 0 也应触发
        String reason = FileConversionUtils.evaluateUnhealthyReason(
                true, 2, 25, 5, 0, 3, 3, 45_000L, 120_000L, 5_000L);
        assertNotNull(reason);
        assertEquals(true, reason.contains("进程数严重偏少"));
    }

    @Test
    void unhealthyWhenProcessTooFewAndNoSuccessLong() {
        String reason = FileConversionUtils.evaluateUnhealthyReason(
                true, 2, 25, 5, 0, 3, 0, 0L, 120_000L, 130_000L);
        assertNotNull(reason);
        assertEquals(true, reason.contains("进程数严重偏少"));
    }

    @Test
    void unhealthyWhenNoSuccessDespiteAvailableService() {
        String reason = FileConversionUtils.evaluateUnhealthyReason(
                true, 10, 25, 5, 1, 3, 0, 0L, 120_000L, 150_000L);
        assertNotNull(reason);
        assertEquals(true, reason.contains("持续无成功转换"));
    }
}
