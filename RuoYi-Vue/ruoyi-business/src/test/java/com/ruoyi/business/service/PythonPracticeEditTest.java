package com.ruoyi.business.service;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.*;
import com.ruoyi.business.mapper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PythonPracticeEditTest {
    @Test
    void appendUsesMaximumAndReusesSnapshotWithoutDeletingHistory() {
        PythonPracticeMapper mapper = mock(PythonPracticeMapper.class);
        BizQuestionMapper questions = mock(BizQuestionMapper.class);
        ProgrammingJudgeMapper judge = mock(ProgrammingJudgeMapper.class);
        PythonPracticeService service = new PythonPracticeService();
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ReflectionTestUtils.setField(service, "questionMapper", questions);
        ReflectionTestUtils.setField(service, "judgeMapper", judge);
        ReflectionTestUtils.setField(service, "dataLabMapper", mock(DataLabMapper.class));
        BizQuestion question = new BizQuestion();
        question.setQuestionType("practical"); question.setPracticalMode("PYTHON");
        when(questions.selectBizQuestionByQuestionId(9L)).thenReturn(question);
        ProgrammingQuestionConfig config = new ProgrammingQuestionConfig();
        config.setValidationStatus("VALID");
        when(judge.selectConfig(9L)).thenReturn(config);
        ProgrammingTestCase visible = new ProgrammingTestCase(), hidden = new ProgrammingTestCase();
        visible.setIsPublic("1"); hidden.setIsPublic("0");
        when(judge.selectTestCases(9L)).thenReturn(Arrays.asList(visible, hidden));
        when(mapper.selectReusableSnapshot(2L, 9L)).thenReturn(80L);
        when(mapper.selectMaxPlanQuestionSort(2L)).thenReturn(7);
        Map<?, ?> result = ReflectionTestUtils.invokeMethod(service, "addQuestionToVersion", 2L, 9L, null, null, "test");
        assertEquals(80L, result.get("snapshotId"));
        verify(mapper).insertPlanQuestion(argThat(row -> Integer.valueOf(8).equals(row.get("sortNo")) && Long.valueOf(80).equals(row.get("snapshotId"))));
        verify(mapper, never()).insertSnapshot(anyMap());
        verify(mapper, never()).insertSnapshotCase(anyMap());
        when(mapper.countPlanQuestionBySort(2L, 3)).thenReturn(1);
        assertEquals(Integer.valueOf(8), ReflectionTestUtils.invokeMethod(service, "resolveSortNo", 2L, 3));
    }

    @Test
    void draftAndPublishedEditsAcquireParentLockBeforeVersionReads() {
        PythonPracticeMapper mapper = mock(PythonPracticeMapper.class);
        PythonPracticeService service = new PythonPracticeService();
        ReflectionTestUtils.setField(service, "mapper", mapper);
        Map<String, Object> version = new HashMap<>();
        version.put("plan_id", 1L); version.put("dept_id", 10L); version.put("version_status", "DRAFT");
        when(mapper.selectPlanByVersion(2L)).thenReturn(version);
        assertEquals(Long.valueOf(2), ReflectionTestUtils.invokeMethod(service, "ensureEditableVersion", 2L, 20L, "test", 10L));
        org.mockito.InOrder order = inOrder(mapper);
        order.verify(mapper).lockPlanByVersion(2L);
        order.verify(mapper).selectPlanByVersion(2L);
        version.put("version_status", "PUBLISHED");
        Map<String, Object> draft = new HashMap<>(); draft.put("plan_version_id", 3L);
        when(mapper.selectLatestDraftVersion(1L)).thenReturn(draft);
        assertEquals(Long.valueOf(3), ReflectionTestUtils.invokeMethod(service, "ensureEditableVersion", 2L, 20L, "test", 10L));
        verify(mapper, never()).insertPlanVersion(anyMap());
    }
}
