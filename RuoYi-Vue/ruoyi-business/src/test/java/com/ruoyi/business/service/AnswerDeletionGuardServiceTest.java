package com.ruoyi.business.service;

import java.util.Arrays;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerDeletionGuardServiceTest
{
    @Mock
    private BizStudentAnswerMapper studentAnswerMapper;

    @InjectMocks
    private AnswerDeletionGuardService service;

    @Test
    void lessonWithAnswersCannotBeHardDeleted()
    {
        when(studentAnswerMapper.countByLessonIds(Arrays.asList(5L, 6L))).thenReturn(12);

        assertThrows(ServiceException.class, () -> service.assertLessonsDeletable(new Long[] { 5L, 6L }));
    }

    @Test
    void studentWithoutAnswersMayBeDeleted()
    {
        when(studentAnswerMapper.countByStudentIds(Arrays.asList(30L))).thenReturn(0);

        assertDoesNotThrow(() -> service.assertStudentsDeletable(new Long[] { 30L }));
    }
}
