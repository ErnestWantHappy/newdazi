package com.ruoyi.business.service;

import com.ruoyi.business.domain.ProgrammingSubmission;
import com.ruoyi.business.domain.ProgrammingSubmissionCase;
import com.ruoyi.business.domain.ProgrammingTestCase;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.ProgrammingQuestionConfig;
import com.ruoyi.business.judge.Judge0Result;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.ProgrammingJudgeMapper;
import com.ruoyi.business.domain.vo.StudentProgrammingSubmissionVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgrammingSubmissionServiceTest {
    @Mock private ProgrammingJudgeMapper programmingMapper;
    @Mock private BizStudentAnswerMapper studentAnswerMapper;
    @Mock private BizQuestionMapper questionMapper;
    private ProgrammingSubmissionService service;

    @BeforeEach
    void setUp() {
        service = new ProgrammingSubmissionService();
        ReflectionTestUtils.setField(service, "programmingMapper", programmingMapper);
        ReflectionTestUtils.setField(service, "studentAnswerMapper", studentAnswerMapper);
        ReflectionTestUtils.setField(service, "questionMapper", questionMapper);
    }

    @Test
    void hiddenCaseNeverCarriesOutputOrErrorToStudentHistory() {
        ProgrammingTestCase testCase = new ProgrammingTestCase();
        testCase.setTestCaseId(7L);
        testCase.setIsPublic("0");
        Judge0Result judgeResult = new Judge0Result();
        judgeResult.setStatusId(4);
        judgeResult.setStdout("secret output");
        judgeResult.setStderr("secret error");

        ProgrammingSubmissionCase row = ReflectionTestUtils.invokeMethod(service, "toCaseResult", 1L, testCase, judgeResult);

        assertEquals("WRONG_ANSWER", row.getStatusCode());
        assertNull(row.getOutputText());
        assertNull(row.getErrorSummary());
    }

    @Test
    void serviceFailureDoesNotWriteExistingStudentAnswer() {
        ProgrammingSubmission existing = new ProgrammingSubmission();
        existing.setSubmissionId(3L);
        existing.setStatusCode("JUDGING");
        when(programmingMapper.selectSubmissionById(3L)).thenReturn(existing);

        ReflectionTestUtils.invokeMethod(service, "completeServiceFailure", existing, "Judge0 无响应");

        ArgumentCaptor<ProgrammingSubmission> submissionCaptor = ArgumentCaptor.forClass(ProgrammingSubmission.class);
        verify(programmingMapper).updateSubmissionResult(submissionCaptor.capture());
        assertEquals("SERVICE_ERROR", submissionCaptor.getValue().getStatusCode());
        assertNull(submissionCaptor.getValue().getScore());
        verify(studentAnswerMapper, never()).upsertAnswer(any());
    }

    @Test
    void pythonJudgeRequiresPythonPracticalMode() {
        BizQuestion pythonPractical = new BizQuestion();
        pythonPractical.setQuestionType("practical");
        pythonPractical.setPracticalMode("PYTHON");

        BizQuestion filePractical = new BizQuestion();
        filePractical.setQuestionType("practical");
        filePractical.setPracticalMode("FILE");

        assertEquals(true, ReflectionTestUtils.invokeMethod(service, "isPythonPractical", pythonPractical));
        assertEquals(false, ReflectionTestUtils.invokeMethod(service, "isPythonPractical", filePractical));
    }

    @Test
    void studentSubmissionResponseNeverDeclaresJudge0TokenOrRequestIp() {
        assertThrows(NoSuchMethodException.class, () -> StudentProgrammingSubmissionVo.class.getMethod("getJudge0Token"));
        assertThrows(NoSuchMethodException.class, () -> StudentProgrammingSubmissionVo.class.getMethod("getRequestIp"));
        assertThrows(NoSuchMethodException.class, () -> StudentProgrammingSubmissionVo.class.getMethod("getErrorSummary"));
    }

    @Test
    void programmingConfigurationRejectsOversizedTestCaseList() {
        ProgrammingQuestionConfig config = new ProgrammingQuestionConfig();
        config.setTimeLimitSeconds(2D);
        config.setMemoryLimitKb(131072);
        config.setMaxProcesses(2);
        config.setMaxFileSizeKb(1024);
        config.setMaxOutputKb(64);
        java.util.List<ProgrammingTestCase> cases = new java.util.ArrayList<ProgrammingTestCase>();
        for (int i = 0; i < 51; i++) {
            ProgrammingTestCase item = new ProgrammingTestCase();
            item.setCaseName("测试" + i);
            item.setExpectedOutput("ok");
            item.setScoreWeight(1D);
            item.setIsPublic(i == 0 ? "0" : "1");
            cases.add(item);
        }

        assertThrows(Exception.class, () -> ReflectionTestUtils.invokeMethod(service, "validateTestCases", cases));
    }

    @Test
    void publicQuestionPreviewReturnsOnlyPublicCasesForAnotherTeacher() {
        BizQuestion question = new BizQuestion();
        question.setQuestionType("practical");
        question.setPracticalMode("PYTHON");
        question.setIsPublic("1");
        question.setCreatorId(99L);
        when(questionMapper.selectBizQuestionByQuestionId(1755L)).thenReturn(question);
        ProgrammingTestCase publicCase = new ProgrammingTestCase();
        publicCase.setIsPublic("1");
        when(programmingMapper.selectPublicTestCases(1755L)).thenReturn(java.util.Collections.singletonList(publicCase));

        assertEquals(1, service.getTeacherPreviewCases(1755L, 2L, false).size());
        verify(programmingMapper).selectPublicTestCases(1755L);
        verify(programmingMapper, never()).selectTestCases(1755L);
    }
}
