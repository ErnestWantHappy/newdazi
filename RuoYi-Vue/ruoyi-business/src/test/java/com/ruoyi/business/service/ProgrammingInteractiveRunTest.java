package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.ProgrammingQuestionConfig;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.judge.Judge0Client;
import com.ruoyi.business.judge.Judge0Properties;
import com.ruoyi.business.judge.Judge0Request;
import com.ruoyi.business.judge.Judge0Result;
import com.ruoyi.business.judge.JudgeSourceWrapper;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.ProgrammingJudgeMapper;
import com.ruoyi.common.core.redis.RedisCache;

/**
 * 旧浏览器降级到服务器运行时的交互流程：
 * 每次只交“已经填过的输入”，程序到下一个 input() 停下，前端继续问。
 */
class ProgrammingInteractiveRunTest {
    private ProgrammingSubmissionService service;
    private Judge0Client client;
    private final BizStudent student = new BizStudent();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        service = new ProgrammingSubmissionService();
        ProgrammingJudgeMapper mapper = mock(ProgrammingJudgeMapper.class);
        BizLessonAssignmentMapper assignments = mock(BizLessonAssignmentMapper.class);
        client = mock(Judge0Client.class);
        BizLessonMapper lessons = mock(BizLessonMapper.class);
        BizLessonQuestionMapper lessonQuestions = mock(BizLessonQuestionMapper.class);
        RedisCache redisCache = mock(RedisCache.class);
        // 交互运行与示例运行共用同一把学生锁，这里只模拟“抢到锁、班级计数为 1”
        when(redisCache.setCacheObjectIfAbsent(anyString(), any(), anyLong(), any())).thenReturn(true);
        when(redisCache.incrementCounterWithTtl(anyString(), anyLong(), any())).thenReturn(1L);

        Judge0Properties properties = new Judge0Properties();
        ReflectionTestUtils.setField(properties, "mode", "http");
        ReflectionTestUtils.setField(properties, "maxPolls", 2);
        ReflectionTestUtils.setField(properties, "pollIntervalMs", 100);
        ReflectionTestUtils.setField(properties, "classConcurrency", 8);
        ReflectionTestUtils.setField(properties, "maxSourceBytes", 65536);

        student.setStudentId(30L);
        student.setDeptId(9L);
        student.setEntryYear("2025");
        student.setClassCode("1");
        BizLessonAssignment current = new BizLessonAssignment();
        current.setLessonId(10L);
        current.setPracticalOpen(1);
        when(assignments.selectCurrentLessonByClass("2025", "1", 9L)).thenReturn(10L);
        when(assignments.selectCurrentAssignment("2025", "1", 9L)).thenReturn(current);
        BizLesson lesson = new BizLesson();
        lesson.setLessonId(10L);
        lesson.setDeptId(9L);
        when(lessons.selectBizLessonByLessonId(10L)).thenReturn(lesson);
        BizLessonQuestionDetailVo question = new BizLessonQuestionDetailVo();
        question.setQuestionId(20L);
        question.setQuestionType("practical");
        question.setPracticalMode("PYTHON");
        when(lessonQuestions.selectDetailsByLessonId(10L)).thenReturn(Collections.singletonList(question));

        ProgrammingQuestionConfig config = new ProgrammingQuestionConfig();
        config.setEnabled("1");
        config.setTimeLimitSeconds(3D);
        config.setMemoryLimitKb(131072);
        config.setMaxProcesses(1);
        config.setMaxFileSizeKb(64);
        config.setMaxOutputKb(32);
        when(mapper.selectConfig(20L)).thenReturn(config);

        ReflectionTestUtils.setField(service, "programmingMapper", mapper);
        ReflectionTestUtils.setField(service, "lessonAssignmentMapper", assignments);
        ReflectionTestUtils.setField(service, "lessonMapper", lessons);
        ReflectionTestUtils.setField(service, "lessonQuestionMapper", lessonQuestions);
        ReflectionTestUtils.setField(service, "redisCache", redisCache);
        ReflectionTestUtils.setField(service, "httpClient", client);
        ReflectionTestUtils.setField(service, "mockClient", client);
        ReflectionTestUtils.setField(service, "properties", properties);
    }

    @Test
    void programStopsAtNextInputAndReportsNeedInput() {
        givenResult(3, JudgeSourceWrapper.PROMPT_START + "请输入您的年龄"
                + JudgeSourceWrapper.PROMPT_END, JudgeSourceWrapper.NEED_INPUT);

        Map<String, Object> data = service.runPublicCases(student, 9L, 10L, 20L,
                "age = input(\"请输入您的年龄\")\nprint(\"免票\")", "");

        assertEquals(true, data.get("success"));
        assertEquals(true, data.get("needInput"));
        assertEquals("请输入您的年龄", String.valueOf(data.get("output")).trim());
        assertFalse(String.valueOf(data.get("output")).contains(JudgeSourceWrapper.PROMPT_START));
    }

    @Test
    void replayShowsPromptInputAndResultInOrder() {
        givenResult(3, JudgeSourceWrapper.PROMPT_START + "请输入您的年龄"
                + JudgeSourceWrapper.PROMPT_END + "免票\n", null);

        Map<String, Object> data = service.runPublicCases(student, 9L, 10L, 20L,
                "age = input(\"请输入您的年龄\")\nprint(\"免票\")", "3");

        assertEquals(false, data.get("needInput"));
        assertEquals(true, data.get("success"));
        // 换行必须保留：提示语、输入、结果各占一行，不再粘成“请输入您的年龄免票”
        assertEquals("请输入您的年龄3\n免票", String.valueOf(data.get("output")));
    }

    @Test
    void typedInputIsPassedToJudgeAsStdin() {
        givenResult(3, "免票\n", null);
        final String[] seen = new String[1];
        when(client.submit(any(Judge0Request.class))).thenAnswer(invocation -> {
            Judge0Request request = invocation.getArgument(0);
            seen[0] = request.getStdin();
            Judge0Result queued = new Judge0Result();
            queued.setToken("t1");
            return queued;
        });
        Judge0Result done = new Judge0Result();
        done.setStatusId(3);
        done.setStdout("免票\n");
        when(client.poll(anyString())).thenReturn(done);

        service.runPublicCases(student, 9L, 10L, 20L, "print(input())", "3\n5");

        assertEquals("3\n5", seen[0]);
    }

    @Test
    void programErrorIsReturnedAsFailureWithReadableText() {
        givenResult(11, JudgeSourceWrapper.PROMPT_START + "年龄"
                + JudgeSourceWrapper.PROMPT_END, "Traceback (most recent call last):\nValueError: invalid literal");

        Map<String, Object> data = service.runPublicCases(student, 9L, 10L, 20L,
                "int(input(\"年龄\"))", "abc");

        assertEquals(false, data.get("success"));
        assertEquals(false, data.get("needInput"));
        assertTrue(String.valueOf(data.get("output")).contains("ValueError"));
    }

    private void givenResult(int statusId, String stdout, String stderr) {
        Judge0Result started = new Judge0Result();
        started.setToken("t1");
        Judge0Result done = new Judge0Result();
        done.setStatusId(statusId);
        done.setStdout(stdout);
        done.setStderr(stderr);
        when(client.submit(any(Judge0Request.class))).thenReturn(started);
        when(client.poll(anyString())).thenReturn(done);
    }
}
