package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.ruoyi.business.config.ClassroomWebSocketHandler;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizStudentTaskState;
import com.ruoyi.business.mapper.BizStudentTaskStateMapper;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClassroomTaskStateServiceTest
{
    @Mock private BizStudentTaskStateMapper stateMapper;
    @Mock private ClassroomWebSocketHandler webSocketHandler;
    @InjectMocks private ClassroomTaskStateWriter service;

    @Test
    void shouldPersistVersionedStateAndBroadcastAuthoritativePayload()
    {
        BizStudent student = student();
        BizStudentTaskState saved = state("WORKING", 2L);
        when(stateMapper.selectOne(9L, 10L, 7L)).thenReturn(state("ENTERED", 1L), saved);

        BizStudentTaskState result = service.mark(student, 3L, 9L, 10L,
                ClassroomTaskStateService.WORKING);

        assertEquals(2L, result.getStateVersion());
        verify(stateMapper).upsert(org.mockito.ArgumentMatchers.argThat(value ->
                "WORKING".equals(value.getTaskState())));
        verify(webSocketHandler).broadcastToClassroom(eq(3L), eq("2025"), eq("1班"), eq(9L),
                contains("\"stateVersion\":2"));
    }

    @Test
    void shouldAllowReturningFromWorkingState()
    {
        when(stateMapper.selectOne(9L, 10L, 7L)).thenReturn(state("WORKING", 2L), state("RETURNED", 3L));

        BizStudentTaskState result = service.mark(student(), 3L, 9L, 10L,
                ClassroomTaskStateService.RETURNED);

        assertEquals("RETURNED", result.getTaskState());
        verify(stateMapper).upsert(org.mockito.ArgumentMatchers.argThat(value ->
                "RETURNED".equals(value.getTaskState())));
    }

    @Test
    void shouldAllowGradingReturnedTask()
    {
        when(stateMapper.selectOne(9L, 10L, 7L)).thenReturn(state("RETURNED", 2L), state("GRADED", 3L));

        BizStudentTaskState result = service.mark(student(), 3L, 9L, 10L,
                ClassroomTaskStateService.GRADED);

        assertEquals("GRADED", result.getTaskState());
    }

    @Test
    void shouldNotWriteOrBroadcastWhenStateIsUnchanged()
    {
        BizStudentTaskState entered = state("ENTERED", 2L);
        when(stateMapper.selectOne(9L, 10L, 7L)).thenReturn(entered);

        BizStudentTaskState result = service.mark(student(), 3L, 9L, 10L,
                ClassroomTaskStateService.ENTERED);

        assertEquals(entered, result);
        verify(stateMapper, never()).upsert(org.mockito.ArgumentMatchers.any());
        verify(webSocketHandler, never()).broadcastToClassroom(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    private BizStudent student()
    {
        BizStudent student = new BizStudent();
        student.setStudentId(7L);
        student.setEntryYear("2025");
        student.setClassCode("1班");
        return student;
    }

    private BizStudentTaskState state(String taskState, Long version)
    {
        BizStudentTaskState state = new BizStudentTaskState();
        state.setDeptId(3L);
        state.setLessonId(9L);
        state.setQuestionId(10L);
        state.setStudentId(7L);
        state.setTaskState(taskState);
        state.setStateVersion(version);
        return state;
    }
}
