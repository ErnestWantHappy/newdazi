package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.CountyExam;
import com.ruoyi.business.domain.CountyExamStudent;
import com.ruoyi.business.mapper.CountyExamAnswerMapper;
import com.ruoyi.business.mapper.CountyExamMapper;
import com.ruoyi.business.mapper.CountyExamStudentMapper;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountyExamAnalysisServiceTest
{
    @Mock
    private CountyExamMapper countyExamMapper;

    @Mock
    private CountyExamStudentMapper studentMapper;

    @Mock
    private CountyExamAnswerMapper answerMapper;

    @InjectMocks
    private CountyExamServiceImpl service;

    @BeforeEach
    void prepareManagerContext()
    {
        SysRole role = new SysRole();
        role.setRoleKey("researcher");
        SysUser user = new SysUser();
        user.setUserName("researcher");
        user.setRoles(Collections.singletonList(role));
        LoginUser loginUser = new LoginUser(10L, 100L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldBuildOfficialSummaryWithoutStudentIdentityRows()
    {
        CountyExam exam = new CountyExam();
        exam.setExamId(7L);
        exam.setStatus("3");
        exam.setTotalScore(100);
        when(countyExamMapper.selectCountyExamById(7L)).thenReturn(exam);

        Map<String, Object> school = new HashMap<String, Object>();
        school.put("deptName", "测试学校");
        List<Map<String, Object>> schools = Collections.singletonList(school);
        when(studentMapper.selectSummaryRows(7L)).thenReturn(schools);
        when(studentMapper.selectParticipants(7L)).thenReturn(Arrays.asList(
                student(0, "1"), student(59, "1"), student(60, "1"), student(100, "1")));

        Map<String, Object> question = new HashMap<String, Object>();
        question.put("questionId", 101L);
        List<Map<String, Object>> questions = Collections.singletonList(question);
        when(answerMapper.selectQuestionPerformance(7L)).thenReturn(questions);

        Map<String, Object> summary = service.getSummary(7L);
        Map<?, ?> overview = (Map<?, ?>) summary.get("overview");

        assertSame(schools, summary.get("schools"));
        assertSame(questions, summary.get("questions"));
        assertTrue((Boolean) summary.get("official"));
        assertFalse(summary.containsKey("students"));
        assertEquals(4, overview.get("participantCount"));
        assertEquals(54.8D, overview.get("averageScore"));
        assertEquals(50D, overview.get("passRate"));
        assertEquals(1, ((Map<?, ?>) ((List<?>) summary.get("distribution")).get(9)).get("count"));
    }

    @Test
    void shouldNormalizeDistributionForNonHundredPointExam()
    {
        List<Map<String, Object>> distribution = CountyExamServiceImpl.buildScoreDistribution(
                Arrays.asList(student(5, "1"), student(10, "1")), 10);

        assertEquals(1, distribution.get(5).get("count"));
        assertEquals(1, distribution.get(9).get("count"));
    }

    private CountyExamStudent student(int totalScore, String status)
    {
        CountyExamStudent student = new CountyExamStudent();
        student.setTotalScore(BigDecimal.valueOf(totalScore));
        student.setStatus(status);
        return student;
    }
}
