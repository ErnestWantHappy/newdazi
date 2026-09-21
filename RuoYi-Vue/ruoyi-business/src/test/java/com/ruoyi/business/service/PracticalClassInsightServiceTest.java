package com.ruoyi.business.service;

import com.ruoyi.business.domain.vo.PracticalSubmissionVo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PracticalClassInsightServiceTest
{
    @Test void changingScoreOrDetailInvalidatesFingerprintWithoutChangingCount() throws Exception
    {
        PracticalClassInsightService service = new PracticalClassInsightService();
        TeacherPaperSubmissionService scope = org.mockito.Mockito.mock(TeacherPaperSubmissionService.class);
        com.ruoyi.business.mapper.BizStudentAnswerMapper answers = org.mockito.Mockito.mock(com.ruoyi.business.mapper.BizStudentAnswerMapper.class);
        com.ruoyi.business.mapper.BizScoringDetailMapper details = org.mockito.Mockito.mock(com.ruoyi.business.mapper.BizScoringDetailMapper.class);
        com.ruoyi.common.core.redis.RedisCache cache = org.mockito.Mockito.mock(com.ruoyi.common.core.redis.RedisCache.class);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "scope", scope);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "answers", answers);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "details", details);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "cache", cache);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "json", new com.fasterxml.jackson.databind.ObjectMapper());
        com.ruoyi.common.core.domain.model.LoginUser user = new com.ruoyi.common.core.domain.model.LoginUser(); user.setUserId(7L);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user, null));
        try
        {
            com.ruoyi.business.domain.dto.PaperImportRequest request = new com.ruoyi.business.domain.dto.PaperImportRequest();
            request.lessonId = 1L; request.questionId = 2L; request.entryYear = "2024"; request.classCode = "1";
            com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo question = new com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo(); question.setQuestionContent("作品");
            org.mockito.Mockito.when(scope.requireQuestion(request)).thenReturn(question);
            org.mockito.Mockito.when(scope.requireScope(request)).thenReturn(10L);
            com.ruoyi.business.domain.BizStudent student = new com.ruoyi.business.domain.BizStudent(); student.setStudentId(8L);
            org.mockito.Mockito.when(scope.roster(request)).thenReturn(java.util.Collections.singletonList(student));
            PracticalSubmissionVo row = new PracticalSubmissionVo(); row.setAnswerId(9L); row.setStudentId(8L); row.setSubmitted(true); row.setScore(8); row.setMaxScore(10);
            org.mockito.Mockito.when(answers.selectPracticalSubmissions(1L,2L,"1","2024",10L)).thenReturn(java.util.Collections.singletonList(row));
            com.ruoyi.business.domain.BizScoringDetail detail = new com.ruoyi.business.domain.BizScoringDetail(); detail.setItemId(3L); detail.setScore(8);
            org.mockito.Mockito.when(details.selectDetailsByAnswerId(9L)).thenReturn(new java.util.ArrayList<>(java.util.Collections.singletonList(detail)));
            Object first = service.snapshot(request).get("fingerprint");
            row.setScore(7); Object second = service.snapshot(request).get("fingerprint"); assertNotEquals(first, second);
            detail.setScore(6); assertNotEquals(second, service.snapshot(request).get("fingerprint"));
        }
        finally { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }
    }
    @Test void excludesMissingUnconfirmedAndReturnedButIncludesConfirmedZero()
    {
        PracticalSubmissionVo row = new PracticalSubmissionVo(); row.setSubmitted(false);
        assertFalse(PracticalClassInsightService.isConfirmed(row));
        row.setSubmitted(true); assertFalse(PracticalClassInsightService.isConfirmed(row));
        row.setScore(0); assertTrue(PracticalClassInsightService.isConfirmed(row));
        row.setTaskState("RETURNED"); assertFalse(PracticalClassInsightService.isConfirmed(row));
        row.setTaskState("GRADED"); row.setScore(8); assertTrue(PracticalClassInsightService.isConfirmed(row));
    }
}
