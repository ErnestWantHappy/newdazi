package com.ruoyi.business.service;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.mapper.PythonPracticeMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PythonPracticeDeletePlanTest {
    @Test
    void deletePlanAlwaysRemovesTheWholePlanChain() {
        PythonPracticeMapper mapper = mock(PythonPracticeMapper.class);
        PythonPracticeService service = new PythonPracticeService();
        ReflectionTestUtils.setField(service, "mapper", mapper);
        Map<String, Object> plan = new HashMap<String, Object>();
        plan.put("plan_id", 13L);
        plan.put("dept_id", 169L);
        when(mapper.selectPlan(13L)).thenReturn(plan);

        Map<String, Object> result = service.deletePlan(13L, 169L);

        assertEquals("HARD_DELETED", result.get("deleteMode"));
        InOrder order = inOrder(mapper);
        order.verify(mapper).selectPlan(13L);
        order.verify(mapper).deletePlanDrafts(13L);
        order.verify(mapper).deletePlanSubmissionCases(13L);
        order.verify(mapper).deletePlanSubmissions(13L);
        order.verify(mapper).deletePlanProgress(13L);
        order.verify(mapper).deleteAllPlanClasses(13L);
        order.verify(mapper).deletePlanQuestions(13L);
        order.verify(mapper).deleteExtensionQuestions(13L);
        order.verify(mapper).deleteExtensionClasses(13L);
        order.verify(mapper).deletePlanSnapshotCases(13L);
        order.verify(mapper).deletePlanSnapshots(13L);
        order.verify(mapper).deletePlanVersions(13L);
        order.verify(mapper).deleteExtensions(13L);
        order.verify(mapper).deletePlan(13L);
        verifyNoMoreInteractions(mapper);
    }
}
