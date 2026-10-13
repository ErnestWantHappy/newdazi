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
