package com.ruoyi.business.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.LessonClassScopeMapper;
import com.ruoyi.business.mapper.PracticalGradingDeadlineMapper;

import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class BizLessonSupervisionFactSyncTest
{
    @Mock private BizLessonAssignmentMapper assignmentMapper;
    @Mock private LessonClassScopeMapper scopeMapper;
    @Mock private PracticalGradingDeadlineMapper deadlineMapper;
    @InjectMocks private BizLessonServiceImpl service;

    @Test
    void reassignmentDeactivatesOldFactsBeforeDeletingOldCurrentAssignment()
    {
        service.deactivateOtherCurrentAssignments(10L, "2025", "1", 99L);

        InOrder order = inOrder(scopeMapper, assignmentMapper);
        order.verify(scopeMapper).markOtherAssignmentsInactive(10L, "2025", "1", 99L);
        order.verify(assignmentMapper).deleteOtherAssignmentsByClass("2025", "1", 10L, 99L);
    }

    @Test
    void lessonDeletionRemovesAuditDeadlineAndScopeInDependencyOrder()
    {
        ReflectionTestUtils.invokeMethod(service, "deleteSupervisionFacts", 99L);

        InOrder order = inOrder(deadlineMapper, scopeMapper);
        order.verify(deadlineMapper).deleteAuditsByLessonId(99L);
        order.verify(deadlineMapper).deleteDeadlinesByLessonId(99L);
        order.verify(scopeMapper).deleteByLessonId(99L);
    }
}
