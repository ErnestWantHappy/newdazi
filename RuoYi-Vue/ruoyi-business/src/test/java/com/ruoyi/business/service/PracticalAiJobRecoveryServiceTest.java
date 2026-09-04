package com.ruoyi.business.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.PracticalAiJob;
import com.ruoyi.business.mapper.PracticalAiGradingMapper;

@ExtendWith(MockitoExtension.class)
class PracticalAiJobRecoveryServiceTest
{
    @Mock private PracticalAiGradingMapper mapper;
    @Mock private PracticalAiJobWorker worker;
    private PracticalAiJobRecoveryService service;

    @BeforeEach
    void setUp()
    {
        service = new PracticalAiJobRecoveryService();
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ReflectionTestUtils.setField(service, "worker", worker);
    }

    @Test
    void shouldResetOnlyInterruptedResultAndResumeJobAfterRestart()
    {
        PracticalAiJob job = new PracticalAiJob();
        job.setJobId(3L); job.setJobStatus("RUNNING");
        when(mapper.selectRecoverableJobs()).thenReturn(Collections.singletonList(job));
        when(mapper.resetInterruptedResults(3L)).thenReturn(1);

        service.recoverIncompleteJobs();

        verify(mapper).resetInterruptedResults(3L);
        verify(mapper).updateJobStatus(eq(3L), eq("PENDING"), isNull(), isNull(), isNull());
        verify(mapper).updateJobHeartbeat(3L, null);
        verify(worker).run(3L);
    }
}
