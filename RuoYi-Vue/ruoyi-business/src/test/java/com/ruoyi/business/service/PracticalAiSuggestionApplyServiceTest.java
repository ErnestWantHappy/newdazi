package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.PracticalAiJob;
import com.ruoyi.business.domain.PracticalAiResult;
import com.ruoyi.business.domain.PracticalRubricSnapshot;
import com.ruoyi.business.domain.vo.PracticalScoringItemVo;
import com.ruoyi.business.mapper.BizScoringDetailMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.PracticalAiGradingMapper;
import com.ruoyi.business.mapper.PracticalRubricSnapshotMapper;

@ExtendWith(MockitoExtension.class)
class PracticalAiSuggestionApplyServiceTest
{
    @Mock private PracticalAiGradingMapper aiMapper;
    @Mock private BizStudentAnswerMapper answerMapper;
    @Mock private BizScoringDetailMapper detailMapper;
    @Mock private PracticalRubricSnapshotMapper snapshotMapper;
    @Mock private PracticalRubricSnapshotService rubricService;
    @Mock private PracticalScoringPolicyService scoringPolicyService;
    @Mock private PracticalGradingDeadlineService deadlineService;
    private PracticalAiSuggestionApplyService service;

    @BeforeEach
    void setUp()
    {
        service = new PracticalAiSuggestionApplyService();
        ReflectionTestUtils.setField(service, "aiMapper", aiMapper);
        ReflectionTestUtils.setField(service, "answerMapper", answerMapper);
        ReflectionTestUtils.setField(service, "detailMapper", detailMapper);
        ReflectionTestUtils.setField(service, "snapshotMapper", snapshotMapper);
        ReflectionTestUtils.setField(service, "rubricService", rubricService);
        ReflectionTestUtils.setField(service, "scoringPolicyService", scoringPolicyService);
        ReflectionTestUtils.setField(service, "deadlineService", deadlineService);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
    }

    @Test
    void shouldNeverOverwriteExistingManualScore()
    {
        PracticalAiJob job = job();
        PracticalAiResult result = result();
        BizStudentAnswer answer = answer(); answer.setScore(18);
        when(aiMapper.selectJob(1L, 7L)).thenReturn(job);
        when(aiMapper.selectResultsByJob(1L)).thenReturn(Collections.singletonList(result));
        when(answerMapper.selectByIdForUpdate(101L)).thenReturn(answer);

        Map<String, Object> summary = service.applyUngraded(1L, 7L, 9L);

        assertEquals(0, summary.get("appliedCount"));
        assertEquals(1, summary.get("skippedManualCount"));
        verify(answerMapper, never()).updateScore(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyInt());
        verify(aiMapper).updateApplyStatus(201L, "SKIPPED_MANUAL_GRADE", null, null);
    }

    @Test
    void shouldApplyValidatedItemScoresToUngradedAnswer()
    {
        PracticalAiJob job = job();
        PracticalAiResult result = result();
        BizStudentAnswer answer = answer();
        PracticalRubricSnapshot snapshot = new PracticalRubricSnapshot();
        snapshot.setSnapshotId(401L); snapshot.setQuestionScore(30);
        PracticalScoringItemVo first = item(11L, 15); PracticalScoringItemVo second = item(12L, 15);
        when(aiMapper.selectJob(1L, 7L)).thenReturn(job);
        when(aiMapper.selectResultsByJob(1L)).thenReturn(Collections.singletonList(result));
        when(answerMapper.selectByIdForUpdate(101L)).thenReturn(answer);
        when(snapshotMapper.selectByVersionId(301L)).thenReturn(snapshot);
        when(rubricService.buildScoringItems(snapshot)).thenReturn(Arrays.asList(first, second));
        when(scoringPolicyService.resolveFinalScore(
                org.mockito.ArgumentMatchers.eq(25), org.mockito.ArgumentMatchers.eq(30),
                org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyList())).thenReturn(25);

        Map<String, Object> summary = service.applyUngraded(1L, 7L, 9L);

        assertEquals(1, summary.get("appliedCount"));
        verify(answerMapper).updateScore(101L, 25);
        verify(detailMapper, org.mockito.Mockito.times(2)).insertBizScoringDetail(org.mockito.ArgumentMatchers.any());
        verify(aiMapper).updateApplyStatus(org.mockito.ArgumentMatchers.eq(201L),
                org.mockito.ArgumentMatchers.eq("APPLIED"), org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.any());
    }

    private PracticalAiJob job()
    {
        PracticalAiJob job = new PracticalAiJob();
        job.setJobId(1L); job.setTeacherUserId(7L); job.setDeptId(9L);
        job.setLessonId(10L); job.setQuestionId(11L); job.setJobStatus("COMPLETED");
        job.setReferenceAnswerJson("[{}]");
        return job;
    }

    private PracticalAiResult result()
    {
        PracticalAiResult result = new PracticalAiResult();
        result.setResultId(201L); result.setJobId(1L); result.setAnswerId(101L);
        result.setPracticalVersionId(301L); result.setRubricSnapshotId(401L);
        result.setResultStatus("SUCCESS"); result.setApplyStatus("NOT_APPLIED");
        result.setSuggestedScore(25);
        result.setScoringDetailsJson("[{\"itemId\":11,\"score\":10},{\"itemId\":12,\"score\":15}]");
        return result;
    }

    private BizStudentAnswer answer()
    {
        BizStudentAnswer answer = new BizStudentAnswer();
        answer.setAnswerId(101L); answer.setLessonId(10L); answer.setQuestionId(11L);
        answer.setPracticalVersionId(301L); answer.setStudentAnswer("/profile/upload/work.docx");
        return answer;
    }

    private PracticalScoringItemVo item(Long id, int max)
    {
        PracticalScoringItemVo item = new PracticalScoringItemVo(); item.setItemId(id); item.setMaxScore(max); return item;
    }
}
