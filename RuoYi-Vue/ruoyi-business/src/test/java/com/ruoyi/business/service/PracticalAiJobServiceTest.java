package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.PracticalAiJob;
import com.ruoyi.business.domain.PracticalAttachment;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.domain.TeacherPracticalReferenceAnswer;
import com.ruoyi.business.domain.vo.PracticalSubmissionVo;
import com.ruoyi.business.mapper.PracticalAiGradingMapper;
import com.ruoyi.business.mapper.PracticalArtifactMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class PracticalAiJobServiceTest
{
    @Mock private PracticalAiGradingMapper mapper;
    @Mock private TeacherAiConfigService configService;
    @Mock private PracticalAiJobWorker worker;
    @Mock private PracticalAiReferenceAnswerService referenceAnswerService;
    @Mock private PracticalArtifactMapper artifactMapper;
    @Mock private PracticalFilePolicyService filePolicyService;
    private PracticalAiJobService service;

    @BeforeEach
    void setUp()
    {
        service = new PracticalAiJobService();
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ReflectionTestUtils.setField(service, "configService", configService);
        ReflectionTestUtils.setField(service, "worker", worker);
        ReflectionTestUtils.setField(service, "referenceAnswerService", referenceAnswerService);
        ReflectionTestUtils.setField(service, "artifactMapper", artifactMapper);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(service, "filePolicyService", filePolicyService);
        TeacherPracticalReferenceAnswer reference = new TeacherPracticalReferenceAnswer();
        reference.setReferenceId(1L); reference.setOriginalFileName("answer.png");
        reference.setResourcePath("/profile/upload/answer.png"); reference.setFileExtension("png");
        org.mockito.Mockito.lenient().when(referenceAnswerService.current(7L, 9L, 10L, 11L)).thenReturn(reference);
        org.mockito.Mockito.lenient().when(artifactMapper.selectMaterialsByQuestion(11L)).thenReturn(Collections.emptyList());
    }

    @Test
    void shouldCreateOneResultPerEligibleImmutableVersion()
    {
        TeacherAiConfig config = config();
        when(configService.statusForUpdate(7L)).thenReturn(config);
        when(configService.apiKey(config)).thenReturn("secret");
        org.mockito.Mockito.doAnswer(invocation -> {
            ((PracticalAiJob) invocation.getArgument(0)).setJobId(55L); return 1;
        }).when(mapper).insertJob(any(PracticalAiJob.class));

        PracticalAiJob job = service.create(7L, 9L, 10L, 11L, "2024", "1", "UNGRADED_ONLY",
                Arrays.asList(submission(true), submission(false)));

        assertEquals(55L, job.getJobId());
        assertEquals(1, job.getTotalCount());
        assertEquals(1, job.getSkippedCount());
        verify(mapper).insertResult(any());
        verify(worker).run(55L);
    }

    @Test
    void shouldReuseRunningJobUnderTeacherConfigRowLock()
    {
        TeacherAiConfig config = config();
        PracticalAiJob running = new PracticalAiJob(); running.setJobId(88L); running.setJobStatus("RUNNING");
        when(configService.statusForUpdate(7L)).thenReturn(config);
        when(configService.apiKey(config)).thenReturn("secret");
        when(mapper.selectActiveJob(7L, 10L, 11L, "2024", "1")).thenReturn(running);

        assertEquals(88L, service.create(7L, 9L, 10L, 11L, "2024", "1", "UNGRADED_ONLY",
                Collections.singletonList(submission(true))).getJobId());
        verify(mapper, never()).insertJob(any());
        verify(worker, never()).run(any());
    }

    @Test
    void shouldFinishPausedJobImmediatelyWhenCancelled()
    {
        PracticalAiJob paused = new PracticalAiJob(); paused.setJobId(88L); paused.setJobStatus("PAUSED");
        when(mapper.selectJob(88L, 7L)).thenReturn(paused);

        service.cancel(88L, 7L);

        verify(mapper).updatePendingResultsStatus(org.mockito.ArgumentMatchers.eq(88L),
                org.mockito.ArgumentMatchers.eq("CANCELLED"), org.mockito.ArgumentMatchers.eq("教师已取消"),
                org.mockito.ArgumentMatchers.any());
        verify(mapper).updateJobCounts(88L);
        verify(mapper).updateJobStatus(org.mockito.ArgumentMatchers.eq(88L),
                org.mockito.ArgumentMatchers.eq("CANCELLED"), org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.isNull());
        verifyNoInteractions(worker);
    }

    private TeacherAiConfig config()
    {
        TeacherAiConfig config = new TeacherAiConfig();
        config.setProviderCode("QWEN"); config.setModelName("qwen3.7-plus"); return config;
    }

    private PracticalSubmissionVo submission(boolean eligible)
    {
        PracticalSubmissionVo submission = new PracticalSubmissionVo();
        submission.setSubmitted(true); submission.setAnswerId(eligible ? 101L : 102L);
        if (eligible)
        {
            submission.setPracticalVersionId(201L); submission.setRubricSnapshotId(301L);
            PracticalAttachment attachment = new PracticalAttachment();
            attachment.setNormalizedStatus("success"); attachment.setNormalizedPages(Arrays.asList("/profile/upload/page.jpg"));
            submission.setAttachments(Collections.singletonList(attachment));
        }
        return submission;
    }
}
