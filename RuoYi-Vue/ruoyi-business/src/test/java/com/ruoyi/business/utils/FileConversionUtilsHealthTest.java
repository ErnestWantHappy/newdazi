package com.ruoyi.business.utils;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LibreOffice 健康判定纯逻辑测试（不启动真实 Office 进程）。
 */
class FileConversionUtilsHealthTest {

    @Test
    void presentationsAndSpreadsheetsUseIsolatedConversion() {
        assertTrue(FileConversionUtils.requiresIsolatedCli("student.PPTX"));
        assertTrue(FileConversionUtils.requiresIsolatedCli("student.xlsx"));
        assertFalse(FileConversionUtils.requiresIsolatedCli("student.docx"));
        assertFalse(FileConversionUtils.requiresIsolatedCli("student.pdf"));
    }

    @Test
    void windowsOfficeProfileUsesTripleSlashFileUri() {
        String uri = FileConversionUtils.libreOfficeUserProfileUri(
                new File("D:\\program\\upload\\.lo-profile-test"));
        assertTrue(uri.startsWith("file:///"));
        assertTrue(uri.contains("D:/program/upload/.lo-profile-test"));
    }

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

    // ==================== 2026-10-12 回归：假健康 / 退避 / 毒文件隔离 ====================

    @Test
    void postRebuildObservationIsUnhealthyWhenAttemptedButNeverSucceeded() {
        // 回归 130 事故：重建清零了在途与失败计数，旧逻辑立刻报 healthy=true。
        // 现在只要"重建过 + 超过软超时 + 期间确实有转换尝试且仍无成功"就判为不健康。
        assertTrue(FileConversionUtils.isPostRebuildObservationUnhealthy(
                true, 200_000L, 120_000L, 3));
    }

    @Test
    void postRebuildObservationIsHealthyWhenPoolIsMerelyIdle() {
        // 深夜无人提交时不能误判：没有转换尝试就说明只是空闲，不该反复重启
        assertFalse(FileConversionUtils.isPostRebuildObservationUnhealthy(
                true, 3_600_000L, 120_000L, 0));
    }

    @Test
    void postRebuildObservationIsHealthyBeforeSoftTimeout() {
        assertFalse(FileConversionUtils.isPostRebuildObservationUnhealthy(
                true, 50_000L, 120_000L, 5));
    }

    @Test
    void postRebuildObservationIsHealthyWhenAlreadyRecovered() {
        // 已出现真实成功（awaiting=false）即恢复，退避也随之清零
        assertFalse(FileConversionUtils.isPostRebuildObservationUnhealthy(
                false, 3_600_000L, 120_000L, 9));
    }

    @Test
    void cooldownBacksOffExponentiallyWithoutSuccess() {
        // 基础 2 分钟：1 次无成功 -> 4 分钟，2 次 -> 8 分钟，3 次 -> 16 分钟
        assertEquals(120_000L, FileConversionUtils.effectiveCooldownMs(120_000L, 0));
        assertEquals(240_000L, FileConversionUtils.effectiveCooldownMs(120_000L, 1));
        assertEquals(480_000L, FileConversionUtils.effectiveCooldownMs(120_000L, 2));
        assertEquals(960_000L, FileConversionUtils.effectiveCooldownMs(120_000L, 3));
        // 增长到 3 次后封顶，不会无限膨胀
        assertEquals(960_000L, FileConversionUtils.effectiveCooldownMs(120_000L, 99));
    }

    @Test
    void cooldownIsCappedAtThirtyMinutes() {
        // 基础 20 分钟，翻倍后应被 30 分钟上限截断
        assertEquals(30L * 60L * 1000L, FileConversionUtils.effectiveCooldownMs(20L * 60L * 1000L, 1));
    }

    @Test
    void poisonFileBecomesIsolatedAfterThresholdHit() {
        String poison = "/data/upload/upload/student-answer-artifact/poison-test/case-a.docx";
        assertFalse(FileConversionUtils.isPoisonFileIsolated(poison));
        assertEquals(0, FileConversionUtils.getPoisonFileHitCount(poison));

        // 阈值默认 1：崩一次即隔离，不再让它第二次进共享池
        FileConversionUtils.registerPoisonFileHit(poison);
        assertEquals(1, FileConversionUtils.getPoisonFileHitCount(poison));
        assertTrue(FileConversionUtils.isPoisonFileIsolated(poison));

        // 命中次数持续累加，便于运维观察
        FileConversionUtils.registerPoisonFileHit(poison);
        assertEquals(2, FileConversionUtils.getPoisonFileHitCount(poison));
    }

    @Test
    void unrelatedHealthyFileIsNotIsolated() {
        String healthy = "/data/upload/upload/student-answer-artifact/poison-test/case-b.docx";
        assertFalse(FileConversionUtils.isPoisonFileIsolated(healthy));
    }

    @Test
    void nullPathIsNeverIsolated() {
        assertFalse(FileConversionUtils.isPoisonFileIsolated(null));
        assertFalse(FileConversionUtils.isUnconvertible(null));
        assertEquals(0, FileConversionUtils.registerPoisonFileHit(null));
    }

    @Test
    void unconvertibleFileIsRecordedButNotDeleted() {
        // 隔离进程也失败后登记为不可转换，用于停止自动重试；原始文件始终保留
        String broken = "/data/upload/upload/student-answer-artifact/poison-test/case-c.docx";
        assertFalse(FileConversionUtils.isUnconvertible(broken));

        FileConversionUtils.markUnconvertible(broken, "文件内容异常");
        assertTrue(FileConversionUtils.isUnconvertible(broken));
        assertEquals("文件内容异常", FileConversionUtils.getUnconvertibleReason(broken));
        assertNull(FileConversionUtils.getUnconvertibleReason(
                "/data/upload/upload/student-answer-artifact/poison-test/never-seen.docx"));
    }

    @Test
    void unconvertibleMessageIsRecognisedRegardlessOfPrefix() {
        // 落库文本可能被截断或带前缀，判定只看关键片段，避免标记失效后又被捞回重试
        assertTrue(FileConversionUtils.isUnconvertibleMessage(FileConversionUtils.UNCONVERTIBLE_MESSAGE));
        assertTrue(FileConversionUtils.isUnconvertibleMessage(
                "转换执行异常：" + FileConversionUtils.UNCONVERTIBLE_MESSAGE));
    }

    @Test
    void ordinaryFailureMessageIsNotTreatedAsUnconvertible() {
        // 普通失败（如服务重启、文件不存在）必须允许重试，不能误判成永久不可转换
        assertFalse(FileConversionUtils.isUnconvertibleMessage("PDF 文件生成失败"));
        assertFalse(FileConversionUtils.isUnconvertibleMessage("源文件不存在"));
        assertFalse(FileConversionUtils.isUnconvertibleMessage(null));
        assertFalse(FileConversionUtils.isUnconvertibleMessage(""));
    }

    // ==================== 2026-10-13 回归：崩溃判定改用 PID 身份丢失 ====================

    @Test
    void crashIsDetectedWhenAnOldPidDisappears() {
        // 实测场景：旧进程 A 崩溃，jodconverter 立刻补起新进程 C。
        // 按进程数看是 2→2（判不出崩溃），但 A 已消失——必须判为崩溃。
        Set<Long> before = new LinkedHashSet<>(Arrays.asList(1001L, 1002L));
        Set<Long> after = new LinkedHashSet<>(Arrays.asList(1002L, 1003L));
        assertTrue(FileConversionUtils.hasProcessIdentityLoss(before, after),
                "旧 PID 消失即应判为崩溃，否则毒文件永远无法登记");
    }

    @Test
    void healthyConversionWithSamePidsIsNotACrash() {
        Set<Long> before = new LinkedHashSet<>(Arrays.asList(1001L, 1002L));
        Set<Long> after = new LinkedHashSet<>(Arrays.asList(1001L, 1002L));
        assertFalse(FileConversionUtils.hasProcessIdentityLoss(before, after));
    }

    @Test
    void emptyBeforeSetNeverClaimsCrash() {
        // 采集失败（空集）时不能凭空判定崩溃，否则会把正常文件误隔离
        assertFalse(FileConversionUtils.hasProcessIdentityLoss(new LinkedHashSet<>(), new LinkedHashSet<>()));
        assertFalse(FileConversionUtils.hasProcessIdentityLoss(null, new LinkedHashSet<>()));
    }

    @Test
    void vanishedAfterSetCountsAsCrash() {
        // 采集侧整个空了（进程全灭）必须判崩，宁可走隔离也不能放过
        Set<Long> before = new LinkedHashSet<>(Collections.singletonList(1001L));
        assertTrue(FileConversionUtils.hasProcessIdentityLoss(before, new LinkedHashSet<>()));
        assertTrue(FileConversionUtils.hasProcessIdentityLoss(before, null));
    }

    @Test
    void onlyNonSofficeLinesAreIgnoredWhenCountingPids() {
        // pid 解析只认 soffice.*，其他进程不得混入，否则身份判定失真
        assertNotNull(FileConversionUtils.listOfficeProcessPids());
    }
}
