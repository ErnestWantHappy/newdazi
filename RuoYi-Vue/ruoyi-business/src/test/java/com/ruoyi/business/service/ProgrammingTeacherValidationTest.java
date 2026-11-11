package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.ProgrammingQuestionConfig;
import com.ruoyi.business.domain.ProgrammingTestCase;
import com.ruoyi.business.judge.Judge0Client;
import com.ruoyi.business.judge.Judge0Properties;
import com.ruoyi.business.judge.Judge0Request;
import com.ruoyi.business.judge.Judge0Result;
import com.ruoyi.business.judge.JudgeSourceWrapper;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.ProgrammingJudgeMapper;

/**
 * 教师“验题”链路的回归保护：
 * 参考代码里写 input("提示语") 是正常写法，必须能验证通过并把差异讲清楚。
 */
class ProgrammingTeacherValidationTest {
    private ProgrammingSubmissionService service;
    private ProgrammingJudgeMapper mapper;
    private Judge0Client client;
    private BizQuestionMapper questions;

    @BeforeEach
    void setUp() {
        service = new ProgrammingSubmissionService();
        mapper = mock(ProgrammingJudgeMapper.class);
        client = mock(Judge0Client.class);
        BizQuestionMapper questions = mock(BizQuestionMapper.class);
        Judge0Properties properties = new Judge0Properties();
        ReflectionTestUtils.setField(properties, "mode", "http");
        ReflectionTestUtils.setField(properties, "maxPolls", 2);
        ReflectionTestUtils.setField(properties, "pollIntervalMs", 100);
        ReflectionTestUtils.setField(properties, "maxSourceBytes", 65536);
        ReflectionTestUtils.setField(service, "programmingMapper", mapper);
        ReflectionTestUtils.setField(service, "questionMapper", questions);
        ReflectionTestUtils.setField(service, "httpClient", client);
        ReflectionTestUtils.setField(service, "mockClient", client);
        ReflectionTestUtils.setField(service, "properties", properties);

        BizQuestion question = new BizQuestion();
        question.setQuestionId(1L);
        question.setQuestionType("practical");
        question.setPracticalMode("PYTHON");
        question.setCreatorId(7L);
        question.setAnswer("age = input(\"请输入您的年龄\")\nprint(\"免票\")\n");
        when(questions.selectBizQuestionByQuestionId(1L)).thenReturn(question);
        this.questions = questions;
        ProgrammingQuestionConfig config = new ProgrammingQuestionConfig();
        config.setTimeLimitSeconds(3D);
        config.setMemoryLimitKb(131072);
        config.setMaxProcesses(1);
        config.setMaxFileSizeKb(64);
        config.setMaxOutputKb(32);
        when(mapper.selectConfig(1L)).thenReturn(config);
    }

    @Test
    void referenceCodeWithInputPromptPassesValidation() {
        when(mapper.selectTestCases(1L)).thenReturn(Arrays.asList(
                testCase("示例", "3", "免票", "1", 50D),
                testCase("隐藏", "20", "免票", "0", 50D)));
        givenJudgeOutput(JudgeSourceWrapper.PROMPT_START + "请输入您的年龄"
                + JudgeSourceWrapper.PROMPT_END + "免票\n");

        Map<String, Object> result = service.validateTeacherQuestion(1L, 7L, false, "teacher");

        assertEquals(true, result.get("valid"));
        assertEquals("VALID", result.get("validationStatus"));
        assertEquals(2, result.get("passedCount"));
        verifyValidationPassed(1L);
    }

    @Test
    void legacyQuestionWhoseExpectedOutputContainsPromptStillPasses() {
        when(mapper.selectTestCases(1L)).thenReturn(Arrays.asList(
                testCase("示例", "3", "请输入您的年龄免票", "1", 50D),
                testCase("隐藏", "20", "请输入您的年龄免票", "0", 50D)));
        givenJudgeOutput(JudgeSourceWrapper.PROMPT_START + "请输入您的年龄"
                + JudgeSourceWrapper.PROMPT_END + "免票\n");

        Map<String, Object> result = service.validateTeacherQuestion(1L, 7L, false, "teacher");

        assertEquals(true, result.get("valid"), String.valueOf(result.get("cases")));
    }

    @Test
    void failuresExplainExpectedVersusActualWithoutTechnicalTerms() {
        when(mapper.selectTestCases(1L)).thenReturn(Arrays.asList(
                testCase("示例", "3", "半票", "1", 50D),
                testCase("隐藏", "20", "免票", "0", 50D)));
        givenJudgeOutput(JudgeSourceWrapper.PROMPT_START + "请输入您的年龄"
                + JudgeSourceWrapper.PROMPT_END + "免票\n");

        Map<String, Object> result = service.validateTeacherQuestion(1L, 7L, false, "teacher");
        @SuppressWarnings("unchecked") List<Map<String, Object>> cases =
                (List<Map<String, Object>>) result.get("cases");
        Map<String, Object> failed = cases.get(0);

        assertEquals(false, result.get("valid"));
        assertEquals("WRONG_ANSWER", failed.get("statusCode"));
        assertEquals("3", failed.get("inputText"));
        assertEquals("半票", failed.get("expectedOutput"));
        // 实际输出保留提示语，教师能看出“输入提示 + 结果”的完整过程
        assertTrue(String.valueOf(failed.get("actualOutput")).contains("请输入您的年龄免票"));
        // 差异页展示的终端顺序必须和学生本机看到的一致：提示语、输入的 3、再是输出，不能粘连
        assertEquals("请输入您的年龄3\n免票\n", String.valueOf(failed.get("terminalOutput")));
        String hint = String.valueOf(failed.get("hint"));
        assertTrue(hint.contains("半票") && hint.contains("免票"), hint);
        assertFalse(hint.contains("stdout") || hint.contains("Judge0"), hint);
    }

    @Test
    void generatedOutputsUsePlainTextWithoutPromptMarkers() {
        when(mapper.selectTestCases(1L)).thenReturn(Arrays.asList(
                testCase("示例", "3", "免票", "1", 50D),
                testCase("隐藏", "20", "免票", "0", 50D)));
        givenJudgeOutput(JudgeSourceWrapper.PROMPT_START + "请输入您的年龄"
                + JudgeSourceWrapper.PROMPT_END + "免票\n");

        Map<String, Object> result = service.generateExpectedOutputs(1L, 7L, false);
        @SuppressWarnings("unchecked") List<Map<String, Object>> cases =
                (List<Map<String, Object>>) result.get("cases");

        assertEquals(2, cases.size());
        assertEquals("免票\n", cases.get(0).get("expectedOutput"));
        assertFalse(String.valueOf(cases.get(0).get("expectedOutput"))
                .contains(JudgeSourceWrapper.PROMPT_START));
    }

    @Test
    void missingInputDuringValidationIsReportedAsFailureNotSuccess() {
        when(mapper.selectTestCases(1L)).thenReturn(Arrays.asList(
                testCase("示例", "", "免票", "1", 50D),
                testCase("隐藏", "", "免票", "0", 50D)));
        Judge0Result started = new Judge0Result();
        started.setToken("t1");
        Judge0Result done = new Judge0Result();
        done.setStatusId(3);
        done.setStdout(JudgeSourceWrapper.PROMPT_START + "请输入您的年龄" + JudgeSourceWrapper.PROMPT_END);
        done.setStderr(JudgeSourceWrapper.NEED_INPUT);
        when(client.submit(any(Judge0Request.class))).thenReturn(started);
        when(client.poll(anyString())).thenReturn(done);

        Map<String, Object> result = service.validateTeacherQuestion(1L, 7L, false, "teacher");
        @SuppressWarnings("unchecked") List<Map<String, Object>> cases =
                (List<Map<String, Object>>) result.get("cases");

        assertEquals(false, result.get("valid"));
        assertEquals("RUNTIME_ERROR", cases.get(0).get("statusCode"));
        assertNotNull(cases.get(0).get("hint"));
    }

    @Test
    void wrappingIsSkippedForCodeWithoutInput() {
        BizQuestion plain = new BizQuestion();
        plain.setQuestionId(2L);
        plain.setQuestionType("practical");
        plain.setPracticalMode("PYTHON");
        plain.setCreatorId(7L);
        plain.setAnswer("print(\"免票\")\n");
        when(questions.selectBizQuestionByQuestionId(2L)).thenReturn(plain);
        ProgrammingQuestionConfig config = new ProgrammingQuestionConfig();
        config.setTimeLimitSeconds(3D);
        config.setMemoryLimitKb(131072);
        config.setMaxProcesses(1);
        config.setMaxFileSizeKb(64);
        config.setMaxOutputKb(32);
        when(mapper.selectConfig(2L)).thenReturn(config);
        ProgrammingTestCase item = testCase("示例", "3", "免票", "1", 50D);
        ProgrammingTestCase hidden = testCase("隐藏", "20", "免票", "0", 50D);
        when(mapper.selectTestCases(2L)).thenReturn(Arrays.asList(item, hidden));
        givenJudgeOutput("免票\n");

        Map<String, Object> result = service.validateTeacherQuestion(2L, 7L, false, "teacher");

        assertEquals(true, result.get("valid"));
    }

    @Test
    void generatedOutputRejectsIncompleteInputWithoutControlCharacters() {
        when(mapper.selectTestCases(1L)).thenReturn(Arrays.asList(
                testCase("示例", "3", "免票", "1", 50D),
                testCase("隐藏", "20", "免票", "0", 50D)));
        givenJudgeOutput("半截答案");
        Judge0Result done = new Judge0Result();
        done.setStatusId(3);
        done.setStdout("半截答案");
        done.setStderr(JudgeSourceWrapper.NEED_INPUT);
        when(client.poll(anyString())).thenReturn(done);

        Map<String, Object> result = service.generateExpectedOutputs(1L, 7L, false);
        @SuppressWarnings("unchecked") List<Map<String, Object>> cases =
                (List<Map<String, Object>>) result.get("cases");

        assertEquals("RUNTIME_ERROR", cases.get(0).get("statusCode"));
        String error = String.valueOf(cases.get(0).get("errorSummary"));
        assertTrue(error.contains("输入行数不够"));
        assertFalse(error.contains("\u0000"));
    }

    private static ProgrammingTestCase testCase(String name, String input, String expected,
                                                String isPublic, double weight) {
        ProgrammingTestCase testCase = new ProgrammingTestCase();
        testCase.setCaseName(name);
        testCase.setInputText(input);
        testCase.setExpectedOutput(expected);
        testCase.setIsPublic(isPublic);
        testCase.setScoreWeight(weight);
        return testCase;
    }

    private void givenJudgeOutput(String stdout) {
        Judge0Result started = new Judge0Result();
        started.setToken("t1");
        Judge0Result done = new Judge0Result();
        done.setStatusId(3);
        done.setStdout(stdout);
        when(client.submit(any(Judge0Request.class))).thenReturn(started);
        when(client.poll(anyString())).thenReturn(done);
    }

    private void verifyValidationPassed(Long questionId) {
        // 验证结论必须落库，否则前端刷新后又变成“未验证”
        org.mockito.Mockito.verify(mapper)
                .updateValidationStatus(org.mockito.ArgumentMatchers.eq(questionId),
                        org.mockito.ArgumentMatchers.eq("VALID"), anyString());
    }
}
