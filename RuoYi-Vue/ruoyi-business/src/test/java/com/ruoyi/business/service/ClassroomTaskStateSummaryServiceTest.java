package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import com.ruoyi.business.domain.vo.ClassroomStudentTaskSummaryVo;
import com.ruoyi.business.mapper.BizStudentTaskStateMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClassroomTaskStateSummaryServiceTest
{
    @Mock private BizStudentTaskStateMapper mapper;
    @Mock private ClassroomTaskStateWriter writer;
    @InjectMocks private ClassroomTaskStateService service;

    @Test
    void shouldPrioritizeActionableStateInClassroomSummary()
    {
        ClassroomStudentTaskSummaryVo returned = summary(3, 1, 1, 0, 0, 0, 1);
        ClassroomStudentTaskSummaryVo working = summary(3, 1, 0, 1, 0, 0, 0);
        ClassroomStudentTaskSummaryVo submitted = summary(3, 1, 0, 0, 1, 0, 0);
        ClassroomStudentTaskSummaryVo graded = summary(3, 1, 0, 0, 0, 1, 0);
        ClassroomStudentTaskSummaryVo untouched = summary(3, 0, 0, 0, 0, 0, 0);
        ClassroomStudentTaskSummaryVo noTask = summary(0, 0, 0, 0, 0, 0, 0);
        when(mapper.selectClassSummary(3L, 9L, "2025", "1"))
                .thenReturn(Arrays.asList(returned, working, submitted, graded, untouched, noTask));

        service.listClassSummary(3L, 9L, "2025", "1班");

        assertEquals(ClassroomTaskStateService.RETURNED, returned.getTaskState());
        assertEquals(ClassroomTaskStateService.WORKING, working.getTaskState());
        assertEquals(ClassroomTaskStateService.SUBMITTED, submitted.getTaskState());
        assertEquals(ClassroomTaskStateService.GRADED, graded.getTaskState());
        assertEquals(ClassroomTaskStateService.NOT_ENTERED, untouched.getTaskState());
        assertEquals("NO_TASK", noTask.getTaskState());
    }

    @Test
    void shouldIgnoreEmptySummaryRows()
    {
        ClassroomStudentTaskSummaryVo noTask = summary(0, 0, 0, 0, 0, 0, 0);
        when(mapper.selectClassSummary(3L, 9L, "2025", "1"))
                .thenReturn(Arrays.asList(null, noTask));

        List<ClassroomStudentTaskSummaryVo> result = service.listClassSummary(3L, 9L, "2025", "1班");

        assertEquals(1, result.size());
        assertEquals("NO_TASK", result.get(0).getTaskState());
    }

    private ClassroomStudentTaskSummaryVo summary(int total, int started, int entered, int working,
                                                   int submitted, int graded, int returned)
    {
        ClassroomStudentTaskSummaryVo value = new ClassroomStudentTaskSummaryVo();
        value.setTotalQuestionCount(total);
        value.setStartedQuestionCount(started);
        value.setEnteredQuestionCount(entered);
        value.setWorkingQuestionCount(working);
        value.setSubmittedQuestionCount(submitted);
        value.setGradedQuestionCount(graded);
        value.setReturnedQuestionCount(returned);
        return value;
    }
}
