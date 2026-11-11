// 服务端示例运行（旧浏览器兜底）单测：只跑公开测试点，不落提交、成绩或历史
package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.ProgrammingQuestionConfig;
import com.ruoyi.business.domain.ProgrammingTestCase;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.judge.Judge0Client;
import com.ruoyi.business.judge.Judge0Properties;
import com.ruoyi.business.judge.Judge0Request;
import com.ruoyi.business.judge.Judge0Result;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.ProgrammingJudgeMapper;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;

class ProgrammingPublicRunTest {
    private ProgrammingSubmissionService service;
    private ProgrammingJudgeMapper programmingMapper;
    private BizLessonAssignmentMapper assignments;
    private Judge0Client client;
    private final BizStudent student = new BizStudent();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        service = new ProgrammingSubmissionService();
        programmingMapper = mock(ProgrammingJudgeMapper.class);
        assignments = mock(BizLessonAssignmentMapper.class);
        client = mock(Judge0Client.class);
        BizLessonMapper lessons = mock(BizLessonMapper.class);
        BizLessonQuestionMapper lessonQuestions = mock(BizLessonQuestionMapper.class);
        RedisCache redisCache = mock(RedisCache.class);
        // 限流已全部收敛到 RedisCache 的原子封装，这里只模拟“抢到学生锁、班级计数为 1”
        when(redisCache.setCacheObjectIfAbsent(anyString(), any(), anyLong(), any())).thenReturn(true);
        when(redisCache.incrementCounterWithTtl(anyString(), anyLong(), any())).thenReturn(1L);
        Judge0Properties properties = new Judge0Properties();
        ReflectionTestUtils.setField(properties, "mode", "http"); ReflectionTestUtils.setField(properties, "maxPolls", 2); ReflectionTestUtils.setField(properties, "pollIntervalMs", 100);
        ReflectionTestUtils.setField(properties, "classConcurrency", 8); ReflectionTestUtils.setField(properties, "maxSourceBytes", 65536);

        student.setStudentId(30L); student.setDeptId(9L); student.setEntryYear("2025"); student.setClassCode("1");
        BizLessonAssignment current = new BizLessonAssignment(); current.setLessonId(10L); current.setPracticalOpen(1);
        when(assignments.selectCurrentLessonByClass("2025", "1", 9L)).thenReturn(10L);
        when(assignments.selectCurrentAssignment("2025", "1", 9L)).thenReturn(current);
        BizLesson lesson = new BizLesson(); lesson.setLessonId(10L); lesson.setDeptId(9L);
        when(lessons.selectBizLessonByLessonId(10L)).thenReturn(lesson);
        BizLessonQuestionDetailVo question = new BizLessonQuestionDetailVo(); question.setQuestionId(20L); question.setQuestionType("practical"); question.setPracticalMode("PYTHON");
        when(lessonQuestions.selectDetailsByLessonId(10L)).thenReturn(Collections.singletonList(question));

        ReflectionTestUtils.setField(service, "programmingMapper", programmingMapper);
        ReflectionTestUtils.setField(service, "lessonAssignmentMapper", assignments);
        ReflectionTestUtils.setField(service, "lessonMapper", lessons);
        ReflectionTestUtils.setField(service, "lessonQuestionMapper", lessonQuestions);
        ReflectionTestUtils.setField(service, "redisCache", redisCache);
        ReflectionTestUtils.setField(service, "httpClient", client);
        ReflectionTestUtils.setField(service, "mockClient", client);
        ReflectionTestUtils.setField(service, "properties", properties);
    }

    private void givenPublicCase(String expected) {
        ProgrammingQuestionConfig config = new ProgrammingQuestionConfig(); config.setEnabled("1"); config.setTimeLimitSeconds(3D); config.setMemoryLimitKb(131072); config.setMaxProcesses(1); config.setMaxFileSizeKb(64); config.setMaxOutputKb(32);
        when(programmingMapper.selectConfig(20L)).thenReturn(config);
        ProgrammingTestCase testCase = new ProgrammingTestCase(); testCase.setTestCaseId(5L); testCase.setCaseName("示例1"); testCase.setInputText("8"); testCase.setExpectedOutput(expected); testCase.setIsPublic("1");
        when(programmingMapper.selectPublicTestCases(20L)).thenReturn(Arrays.asList(testCase));
    }

    private void givenJudgeOutput(String stdout) {
        Judge0Result started = new Judge0Result(); started.setToken("t1");
        Judge0Result done = new Judge0Result(); done.setStatusId(3); done.setStdout(stdout);
        when(client.submit(any(Judge0Request.class))).thenReturn(started);
        when(client.poll(anyString())).thenReturn(done);
    }

    @Test
    void 只跑公开示例且与示例一致时通过() {
        givenPublicCase("请买票"); givenJudgeOutput("请买票\n");
        Map<String, Object> data = service.runPublicCases(student, 9L, 10L, 20L, "print(1)");
        @SuppressWarnings("unchecked") List<Map<String, Object>> cases = (List<Map<String, Object>>) data.get("cases");
        assertEquals("SERVER", data.get("mode"));
        assertEquals(1, cases.size());
        assertEquals("ACCEPTED", cases.get(0).get("statusCode"));
        assertEquals("请买票\n", cases.get(0).get("actualOutput"));
        verify(programmingMapper, never()).insertSubmission(any());
        verify(programmingMapper, never()).insertSubmissionCase(any());
    }

    @Test
    void 与示例不一致时返回不一致() {
        givenPublicCase("请买票"); givenJudgeOutput("免票");
        @SuppressWarnings("unchecked") List<Map<String, Object>> cases = (List<Map<String, Object>>) service.runPublicCases(student, 9L, 10L, 20L, "print(1)").get("cases");
        assertEquals("WRONG_ANSWER", cases.get(0).get("statusCode"));
    }

    @Test
    void 未指派当前课或没有示例时拒绝() {
        when(assignments.selectCurrentLessonByClass("2025", "1", 9L)).thenReturn(11L);
        assertThrows(ServiceException.class, () -> service.runPublicCases(student, 9L, 10L, 20L, "print(1)"));
        verifyNoMoreInteractions(programmingMapper);
    }
}
