package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.PracticalRubricSnapshot;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.mapper.BizScoringItemMapper;
import com.ruoyi.business.mapper.PracticalArtifactMapper;
import com.ruoyi.business.mapper.PracticalRubricSnapshotMapper;

@ExtendWith(MockitoExtension.class)
class PracticalRubricSnapshotServiceTest
{
    @Mock private PracticalRubricSnapshotMapper snapshotMapper;
    @Mock private BizScoringItemMapper scoringItemMapper;
    @Mock private PracticalArtifactMapper artifactMapper;

    private PracticalRubricSnapshotService service;

    @BeforeEach
    void setUp()
    {
        service = new PracticalRubricSnapshotService();
        ReflectionTestUtils.setField(service, "snapshotMapper", snapshotMapper);
        ReflectionTestUtils.setField(service, "scoringItemMapper", scoringItemMapper);
        ReflectionTestUtils.setField(service, "artifactMapper", artifactMapper);
        ReflectionTestUtils.setField(service, "scoringPolicyService", new PracticalScoringPolicyService());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
    }

    @Test
    void shouldReuseSnapshotWhenRubricHasNotChanged()
    {
        stubCurrentRubric();
        PracticalRubricSnapshot existing = snapshot("原题干", 40, 2);
        when(snapshotMapper.selectLatest(10L, 101L)).thenReturn(existing);

        PracticalRubricSnapshot actual = service.ensureLatest(10L, question("原题干", 40L), 7L);

        assertSame(existing, actual);
        verify(snapshotMapper, never()).insert(any(PracticalRubricSnapshot.class));
    }

    @Test
    void shouldCreateNextVersionWhenRubricChanges()
    {
        stubCurrentRubric();
        when(snapshotMapper.selectLatest(10L, 101L)).thenReturn(snapshot("原题干", 40, 2));
        ArgumentCaptor<PracticalRubricSnapshot> captor = ArgumentCaptor.forClass(PracticalRubricSnapshot.class);

        service.ensureLatest(10L, question("新题干", 30L), 7L);

        verify(snapshotMapper).insert(captor.capture());
        assertEquals(3, captor.getValue().getSnapshotVersion());
        assertEquals("新题干", captor.getValue().getQuestionContent());
        assertEquals(30, captor.getValue().getQuestionScore());
        assertEquals(7L, captor.getValue().getCreatedByUserId());
    }

    @Test
    void shouldResolveTheSnapshotAlreadyBoundToSubmissionVersion()
    {
        PracticalRubricSnapshot bound = snapshot("提交时题干", 40, 1);
        when(snapshotMapper.selectByVersionId(99L)).thenReturn(bound);

        PracticalRubricSnapshot actual = service.resolve(99L, 10L, question("后来题干", 20L), 7L);

        assertSame(bound, actual);
        verify(snapshotMapper, never()).selectLatest(any(Long.class), any(Long.class));
    }

    private BizLessonQuestionDetailVo question(String content, Long score)
    {
        BizLessonQuestionDetailVo question = new BizLessonQuestionDetailVo();
        question.setQuestionId(101L);
        question.setQuestionType("practical");
        question.setQuestionContent(content);
        question.setQuestionScore(score);
        return question;
    }

    private void stubCurrentRubric()
    {
        when(scoringItemMapper.selectItemsByQuestion(101L)).thenReturn(Collections.emptyList());
        when(artifactMapper.selectMaterialsByQuestion(101L)).thenReturn(Collections.emptyList());
    }

    private PracticalRubricSnapshot snapshot(String content, int score, int version)
    {
        PracticalRubricSnapshot snapshot = new PracticalRubricSnapshot();
        snapshot.setSnapshotId((long) version);
        snapshot.setLessonId(10L);
        snapshot.setQuestionId(101L);
        snapshot.setSnapshotVersion(version);
        snapshot.setQuestionContent(content);
        snapshot.setQuestionScore(score);
        snapshot.setScoringItemsJson("[]");
        snapshot.setReferenceMaterialsJson("[]");
        return snapshot;
    }
}
