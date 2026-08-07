package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Date;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.PracticalAttachment;
import com.ruoyi.business.domain.vo.PracticalSubmissionVo;

@ExtendWith(MockitoExtension.class)
class PracticalArtifactRetryServiceTest
{
    @Mock private PracticalAttachmentConversionService conversionService;
    private PracticalArtifactService service;

    @BeforeEach
    void setUp()
    {
        service = new PracticalArtifactService();
        ReflectionTestUtils.setField(service, "conversionService", conversionService);
    }

    @Test
    void shouldRetryFailedAndStaleAttachmentsButProtectActiveOrExhaustedConversions()
    {
        PracticalAttachment failed = attachment(1L, "failed", new Date(), 0);
        PracticalAttachment stale = attachment(2L, "converting",
                new Date(System.currentTimeMillis() - 11L * 60L * 1000L), 1);
        PracticalAttachment active = attachment(3L, "converting", new Date(), 0);
        PracticalAttachment exhausted = attachment(4L, "failed", new Date(), 3);
        PracticalSubmissionVo submission = new PracticalSubmissionVo();
        submission.setAttachments(Arrays.asList(failed, stale, active, exhausted));
        when(conversionService.retry(1L)).thenReturn(true);
        when(conversionService.retry(2L)).thenReturn(true);

        assertEquals(2, service.retryFailedAttachments(Collections.singletonList(submission)));
        verify(conversionService).retry(1L);
        verify(conversionService).retry(2L);
        verify(conversionService, never()).retry(3L);
        verify(conversionService, never()).retry(4L);
    }

    private PracticalAttachment attachment(Long id, String status, Date lastRetry, int retryCount)
    {
        PracticalAttachment attachment = new PracticalAttachment();
        attachment.setAttachmentId(id);
        attachment.setNormalizedStatus(status);
        attachment.setNormalizedLastRetryTime(lastRetry);
        attachment.setNormalizedRetryCount(retryCount);
        return attachment;
    }
}
