package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.PracticalAttachment;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.PracticalArtifactMapper;
import com.ruoyi.business.utils.FileConversionUtils;

/**
 * 题库素材（第三条转换通道）的失败收口回归。
 * <p>
 * 背景：2026-10-13 线上发现第 5 个异常 DOCX 是题库素材而非学生答卷，
 * 该通道当时只写“PDF 文件生成失败”，计数没顶满就被反复捞回重转，
 * 每捞一次把共享池再打崩一轮。这里锁住修复后的行为。
 */
@ExtendWith(MockitoExtension.class)
class PracticalAttachmentConversionFailureTest
{
    @Mock private PracticalArtifactMapper artifactMapper;
    @Mock private BizStudentAnswerMapper studentAnswerMapper;

    private PracticalAttachmentConversionService service;

    @BeforeEach
    void setUp()
    {
        service = new PracticalAttachmentConversionService();
        ReflectionTestUtils.setField(service, "artifactMapper", artifactMapper);
        ReflectionTestUtils.setField(service, "studentAnswerMapper", studentAnswerMapper);
    }

    @Test
    void unconvertibleMaterialIsMarkedAndRetryCountersAreMaxedOut() throws Exception
    {
        // 共享池与一次性隔离进程都失败的素材：FileConversionUtils 已登记为不可转换
        String broken = "/data/upload/upload/student-answer-artifact/failure-test/broken.docx";
        registerUnconvertible(broken);

        PracticalAttachment attachment = attachment(44522L);
        service.markOfficeConversionFailed(attachment, broken);

        assertEquals("failed", attachment.getPreviewStatus());
        assertEquals("failed", attachment.getNormalizedStatus());
        // 两个计数都必须顶满：自动重试看 normalized_retry_count，
        // 人工重转走的 reset 条件同样是 normalized_retry_count < 3
        assertEquals(3, attachment.getPreviewRetryCount());
        assertEquals(3, attachment.getNormalizedRetryCount());
        assertTrue(FileConversionUtils.isUnconvertibleMessage(attachment.getPreviewErrorMessage()),
                "标记文本必须落库，否则重启后内存登记丢失又会被捞回重转");
        assertTrue(FileConversionUtils.isUnconvertibleMessage(attachment.getNormalizedErrorMessage()));
    }

    @Test
    void ordinaryFailureKeepsCountersUntouchedSoItCanStillBeRetried()
    {
        // 服务重启、磁盘满等临时故障必须保留重试能力，不能被误判成永久不可转换
        String healthy = "/data/upload/upload/student-answer-artifact/failure-test/healthy.docx";

        PracticalAttachment attachment = attachment(101L);
        service.markOfficeConversionFailed(attachment, healthy);

        assertEquals("failed", attachment.getPreviewStatus());
        assertEquals("failed", attachment.getNormalizedStatus());
        assertEquals(null, attachment.getPreviewRetryCount(), "普通失败不得顶满计数，否则临时故障无法自愈");
        assertEquals(null, attachment.getNormalizedRetryCount());
        assertEquals("PDF 文件生成失败", attachment.getPreviewErrorMessage());
    }

    @Test
    void nullPathIsTreatedAsOrdinaryFailureNotAsUnconvertible()
    {
        // 路径缺失属于异常入参，不能因此把附件永久拉黑
        PracticalAttachment attachment = attachment(102L);
        service.markOfficeConversionFailed(attachment, null);

        assertEquals("failed", attachment.getPreviewStatus());
        assertEquals(null, attachment.getNormalizedRetryCount());
        assertEquals("PDF 文件生成失败", attachment.getPreviewErrorMessage());
    }

    @Test
    void rejectedQueueReleasesClaimWithoutConsumingRetry() {
        PracticalAttachment attachment = attachment(103L);
        attachment.setFileKind("PDF");
        org.mockito.Mockito.when(artifactMapper.selectAttachmentById(103L)).thenReturn(attachment);
        org.mockito.Mockito.when(artifactMapper.claimAttachmentNormalization(org.mockito.ArgumentMatchers.eq(103L),
                org.mockito.ArgumentMatchers.any())).thenReturn(1);
        PracticalAttachmentConversionService proxy = org.mockito.Mockito.mock(PracticalAttachmentConversionService.class);
        ReflectionTestUtils.setField(service, "self", proxy);
        org.mockito.Mockito.doThrow(new java.util.concurrent.RejectedExecutionException("full"))
                .when(proxy).processClaimedAsync(103L);

        org.junit.jupiter.api.Assertions.assertFalse(service.convertAsync(103L));

        org.mockito.Mockito.verify(artifactMapper).releaseAttachmentNormalizationClaim(103L);
        org.mockito.Mockito.verify(artifactMapper, org.mockito.Mockito.never())
                .resetAttachmentNormalization(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void afterCommitConversionFailureDoesNotFailSubmittedWork() {
        PracticalArtifactService artifacts = new PracticalArtifactService();
        PracticalAttachmentConversionService conversion = org.mockito.Mockito.mock(PracticalAttachmentConversionService.class);
        ReflectionTestUtils.setField(artifacts, "conversionService", conversion);
        org.mockito.Mockito.when(conversion.convertAsync(104L)).thenThrow(new IllegalStateException("temporary"));
        PracticalAttachment attachment = attachment(104L);
        attachment.setNormalizedStatus("pending");
        org.springframework.transaction.support.TransactionSynchronizationManager.initSynchronization();
        try {
            ReflectionTestUtils.invokeMethod(artifacts, "registerAfterCommit", java.util.Collections.emptyList(),
                    java.util.Collections.singletonList(attachment));
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                    org.springframework.transaction.support.TransactionSynchronizationManager.getSynchronizations().get(0).afterCommit());
            org.mockito.Mockito.verify(conversion).convertAsync(104L);
        } finally {
            org.springframework.transaction.support.TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void pendingQueueRetryUsesAtomicClaimWithoutIncrementingFailureCount() {
        PracticalAttachment attachment = attachment(105L);
        attachment.setNormalizedStatus("pending");
        org.mockito.Mockito.when(artifactMapper.selectAttachmentById(105L)).thenReturn(attachment);
        PracticalAttachmentConversionService proxy = org.mockito.Mockito.mock(PracticalAttachmentConversionService.class);
        ReflectionTestUtils.setField(service, "self", proxy);
        org.mockito.Mockito.when(proxy.convertAsync(105L)).thenReturn(true);
        assertTrue(service.retry(105L));
        org.mockito.Mockito.verify(proxy).convertAsync(105L);
        org.mockito.Mockito.verify(artifactMapper, org.mockito.Mockito.never())
                .resetAttachmentNormalization(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void recoverableMapperIncludesAgedPendingWithBatchAndRetryLimits() throws Exception {
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        try (java.io.InputStream input = getClass().getClassLoader()
                .getResourceAsStream("mapper/business/PracticalArtifactMapper.xml")) {
            new org.apache.ibatis.builder.xml.XMLMapperBuilder(input, configuration, "PracticalArtifactMapper.xml",
                    configuration.getSqlFragments()).parse();
        }
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("failedBefore", new java.util.Date());
        params.put("stuckBefore", new java.util.Date());
        params.put("maxRetryCount", 3);
        String sql = configuration.getMappedStatement("com.ruoyi.business.mapper.PracticalArtifactMapper.selectRecoverableAttachments")
                .getBoundSql(params).getSql().replaceAll("\\s+", " ");
        assertTrue(sql.contains("normalized_status = 'pending'"));
        assertTrue(sql.contains("file_kind IN ('OFFICE', 'PDF', 'IMAGE')"));
        assertTrue(sql.contains("COALESCE(update_time, create_time) <= ?"));
        assertTrue(sql.contains("normalized_retry_count < ?"));
        assertTrue(sql.contains("LIMIT 200"));
        String release = configuration.getMappedStatement("com.ruoyi.business.mapper.PracticalArtifactMapper.releaseAttachmentNormalizationClaim")
                .getBoundSql(java.util.Collections.singletonMap("attachmentId", 1L)).getSql();
        org.junit.jupiter.api.Assertions.assertFalse(release.contains("normalized_retry_count"));
        assertTrue(release.contains("normalized_status = 'converting'"));
    }

    private PracticalAttachment attachment(Long id)
    {
        PracticalAttachment attachment = new PracticalAttachment();
        attachment.setAttachmentId(id);
        attachment.setVersionId(1L);
        // fileOrder 非 0，避开 syncLegacyPreview 回写答题行的分支，专注本用例的断言
        attachment.setFileOrder(1);
        return attachment;
    }

    /**
     * 通过反射登记“不可转换”文件。
     * <p>
     * markUnconvertible 是 FileConversionUtils 的包级私有方法（只在隔离通道失败后由
     * 该类内部调用），本测试在 service 包内，无法直接访问，也不应为测试放宽生产可见性。
     */
    private void registerUnconvertible(String path) throws Exception
    {
        java.lang.reflect.Method method = FileConversionUtils.class
                .getDeclaredMethod("markUnconvertible", String.class, String.class);
        method.setAccessible(true);
        method.invoke(null, path, "文件内容异常（隔离转换同样失败）");
    }
}
