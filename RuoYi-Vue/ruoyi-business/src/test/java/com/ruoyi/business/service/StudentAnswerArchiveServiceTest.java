package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.mapper.StudentAnswerArchiveMapper;
import com.ruoyi.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class StudentAnswerArchiveServiceTest
{
    @Mock private StudentAnswerArchiveMapper mapper;
    private StudentAnswerArchiveService service;

    @BeforeEach
    void setUp()
    {
        service = new StudentAnswerArchiveService();
        ReflectionTestUtils.setField(service, "mapper", mapper);
    }

    @Test
    void shouldSkipWhenNoQuestionWasRemoved()
    {
        assertEquals(0, service.archiveRemovedQuestions(279L, Collections.<Long>emptyList()));
        verify(mapper, never()).countLiveAnswers(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void shouldArchiveMetadataBeforeRemovingLiveAnswers()
    {
        when(mapper.countLiveAnswers(279L, Arrays.asList(1882L, 1883L))).thenReturn(5);
        when(mapper.countArchivedAnswers(279L, Arrays.asList(1882L, 1883L))).thenReturn(5);
        when(mapper.countArchivedMetadata(279L, Arrays.asList(1882L, 1883L))).thenReturn(5);
        when(mapper.deleteArchivedLiveAnswers(279L, Arrays.asList(1882L, 1883L))).thenReturn(5);

        assertEquals(5, service.archiveRemovedQuestions(279L, Arrays.asList(1882L, 1883L)));

        verify(mapper).archiveAnswers(279L, Arrays.asList(1882L, 1883L));
        verify(mapper).archiveMetadata(org.mockito.ArgumentMatchers.eq(279L),
                org.mockito.ArgumentMatchers.eq(Arrays.asList(1882L, 1883L)), anyString());
        verify(mapper).deleteArchivedLiveAnswers(279L, Arrays.asList(1882L, 1883L));
    }

    @Test
    void shouldAbortWhenArchiveVerificationIsIncomplete()
    {
        when(mapper.countLiveAnswers(279L, Collections.singletonList(1882L))).thenReturn(5);
        when(mapper.countArchivedAnswers(279L, Collections.singletonList(1882L))).thenReturn(4);

        assertThrows(ServiceException.class,
                () -> service.archiveRemovedQuestions(279L, Collections.singletonList(1882L)));
        verify(mapper, never()).archiveMetadata(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyString());
        verify(mapper, never()).deleteArchivedLiveAnswers(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyList());
    }
}
