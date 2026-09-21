package com.ruoyi.business.service;

import java.util.*;
import com.ruoyi.business.domain.*;
import com.ruoyi.business.domain.dto.PracticalUploadTicket;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.mapper.*;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PracticalPaperVersionGuardTest
{
    @Test void rejectsNewlyGradedAnswerUnderLockBeforeArchivingOrClearingScore()
    {
        PracticalArtifactService service = new PracticalArtifactService();
        RedisCache cache = mock(RedisCache.class);
        PracticalArtifactMapper artifacts = mock(PracticalArtifactMapper.class);
        BizStudentAnswerMapper answers = mock(BizStudentAnswerMapper.class);
        ReflectionTestUtils.setField(service,"redisCache",cache);
        ReflectionTestUtils.setField(service,"artifactMapper",artifacts);
        ReflectionTestUtils.setField(service,"studentAnswerMapper",answers);
        ReflectionTestUtils.setField(service,"filePolicyService",new PracticalFilePolicyService());
        PracticalUploadTicket ticket = new PracticalUploadTicket(); ticket.setStudentId(1L); ticket.setLessonId(2L); ticket.setQuestionId(3L); ticket.setFileExtension("pdf");
        when(cache.getCacheObject("student:practical-artifact-ticket:token")).thenReturn(ticket);
        PracticalArtifact artifact = new PracticalArtifact(); artifact.setArtifactId(4L); artifact.setCurrentVersionId(5L);
        when(artifacts.selectByContextForUpdate("LESSON",2L,1L,3L)).thenReturn(artifact);
        BizStudentAnswer answer = new BizStudentAnswer(); answer.setAnswerId(6L); answer.setStudentAnswer("work.pdf"); answer.setScore(9);
        when(answers.selectLatestByStudentLessonQuestion(1L,2L,3L)).thenReturn(answer);
        when(answers.selectByIdForUpdate(6L)).thenReturn(answer);
        BizLessonQuestionDetailVo question = new BizLessonQuestionDetailVo(); question.setQuestionId(3L); question.setPracticalAllowedExtensions("pdf");
        assertThrows(ServiceException.class, () -> service.submitTeacherPaperArtifact(1L,7L,question,2L,5L,Collections.singletonList("token"),true,null));
        verify(artifacts, never()).insertVersion(any()); verify(answers, never()).upsertAnswer(any());
        assertThrows(ServiceException.class, () -> service.submitTeacherPaperArtifact(1L,7L,question,2L,99L,Collections.singletonList("token"),true,9));
        verify(artifacts, never()).supersedeVersion(any(),any(),any(),any());
    }
}
