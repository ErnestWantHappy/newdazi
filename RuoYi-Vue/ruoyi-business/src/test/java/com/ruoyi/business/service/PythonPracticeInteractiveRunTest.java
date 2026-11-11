package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.judge.Judge0Client;
import com.ruoyi.business.judge.Judge0Properties;
import com.ruoyi.business.judge.Judge0Request;
import com.ruoyi.business.judge.Judge0Result;
import com.ruoyi.business.judge.JudgeSourceWrapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.PythonPracticeMapper;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;

/**
 * 刷题页“调试运行”必须和做题页一致：边跑边输入、不写提交和成绩。
 */
class PythonPracticeInteractiveRunTest {
    private PythonPracticeService service;
    private PythonPracticeMapper mapper;
    private Judge0Client client;

    @BeforeEach
    void setUp() {
        service = new PythonPracticeService();
        mapper = mock(PythonPracticeMapper.class);
        BizStudentMapper students = mock(BizStudentMapper.class);
        client = mock(Judge0Client.class);
        Judge0Properties properties = new Judge0Properties();
        ReflectionTestUtils.setField(properties, "mode", "http");
        ReflectionTestUtils.setField(properties, "maxPolls", 2);
        ReflectionTestUtils.setField(properties, "pollIntervalMs", 100);
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ReflectionTestUtils.setField(service, "studentMapper", students);
        ReflectionTestUtils.setField(service, "httpClient", client);
        ReflectionTestUtils.setField(service, "mockClient", client);
        ReflectionTestUtils.setField(service, "judgeProperties", properties);
        ReflectionTestUtils.setField(service, "redisCache", allowingRedis());

        BizStudent student = new BizStudent();
        student.setStudentId(30L);
        student.setDeptId(9L);
        student.setEntryYear("2025");
        student.setClassCode("1");
        when(students.selectBizStudentByUserId(5L)).thenReturn(student);
        // 题目在学生学习空间内
        Map<String, Object> visible = new HashMap<String, Object>();
        visible.put("source_type", "BASE_VERSION");
        visible.put("source_id", 1L);
        visible.put("question_id", 8L);
        when(mapper.selectStudentQuestions(9L, "2025", "1"))
                .thenReturn(Collections.singletonList(visible));
        Map<String, Object> question = new HashMap<String, Object>();
        question.put("snapshot_id", 80L);
        question.put("practical_mode", "PYTHON");
        question.put("time_limit_seconds", 2);
        question.put("memory_limit_kb", 131072);
        question.put("max_processes", 8);
        question.put("max_file_size_kb", 1024);
        question.put("max_output_kb", 64);
        when(mapper.selectQuestion("BASE_VERSION", 1L, 8L)).thenReturn(question);
    }

    @Test
    void stopsAtInputAndReplaysPromptInOrder() {
        givenResult(3, JudgeSourceWrapper.PROMPT_START + "请输入年龄"
                + JudgeSourceWrapper.PROMPT_END + "免票\n", null);

        Map<String, Object> data = service.runInteractive(5L, "regular", "BASE_VERSION", 1L, 8L,
                "age = input(\"请输入年龄\")\nprint(\"免票\")", "3");

        assertEquals(false, data.get("needInput"));
        assertEquals(true, data.get("success"));
        assertEquals("请输入年龄3\n免票", String.valueOf(data.get("output")));
    }

    @Test
    void reportsNeedInputSoFrontendKeepsAsking() {
        givenResult(3, JudgeSourceWrapper.PROMPT_START + "请输入年龄" + JudgeSourceWrapper.PROMPT_END,
                JudgeSourceWrapper.NEED_INPUT);

        Map<String, Object> data = service.runInteractive(5L, "regular", "BASE_VERSION", 1L, 8L,
                "input(\"请输入年龄\")", "");

        assertEquals(true, data.get("needInput"));
        assertEquals(true, data.get("success"));
    }

    @Test
    void debugRunNeverWritesSubmissionOrProgress() {
        givenResult(3, "免票\n", null);

        service.runInteractive(5L, "regular", "BASE_VERSION", 1L, 8L, "print(\"免票\")", "");

        verify(mapper, never()).insertSubmission(any());
        verify(mapper, never()).insertSubmissionCase(any());
        verify(mapper, never()).upsertProgress(any());
    }

    @Test
    void questionOutsideStudentSpaceIsRejectedBeforeRunning() {
        givenResult(3, "免票\n", null);

        assertThrows(ServiceException.class, () -> service.runInteractive(5L, "regular",
                "BASE_VERSION", 1L, 999L, "print(1)", ""));
        verify(client, never()).submit(any(Judge0Request.class));
    }

    @Test
    void emptyCodeIsRejectedWithoutCallingJudge() {
        assertThrows(ServiceException.class, () -> service.runInteractive(5L, "regular",
                "BASE_VERSION", 1L, 8L, "   ", ""));
        verify(client, never()).submit(any(Judge0Request.class));
    }

    @Test
    void syntaxErrorTextIsShownToStudent() {
        givenResult(6, "", "SyntaxError: invalid syntax");

        Map<String, Object> data = service.runInteractive(5L, "regular", "BASE_VERSION", 1L, 8L,
                "print(", "");

        assertEquals(false, data.get("success"));
        assertTrue(String.valueOf(data.get("output")).contains("SyntaxError"));
        assertEquals("SYNTAX_ERROR", data.get("statusCode"));
    }

    @Test
    void concurrentRunFromSameStudentIsRejectedBeforeCallingJudge() {
        ReflectionTestUtils.setField(service, "redisCache", busyRedis());

        ServiceException error = assertThrows(ServiceException.class, () -> service.runInteractive(5L,
                "regular", "BASE_VERSION", 1L, 8L, "print(1)", ""));

        assertTrue(error.getMessage().contains("上一次运行还没结束"), error.getMessage());
        verify(client, never()).submit(any(Judge0Request.class));
    }

    @Test
    void unfinishedJudgeNeverWritesScoreOrProgress() {
        assertSubmissionServiceFailure(2);
    }

    @Test
    void judgeServiceErrorNeverWritesScoreOrProgress() {
        assertSubmissionServiceFailure(13);
    }

    private void assertSubmissionServiceFailure(int statusId) {
        Map<String, Object> submission = new HashMap<String, Object>();
        submission.put("submit_type", "SUBMIT");
        submission.put("source_code", "print(1)");
        submission.put("snapshot_id", 80L);
        when(mapper.selectSubmission(91L)).thenReturn(submission);
        when(mapper.markSubmissionJudging(91L)).thenReturn(1);
        Map<String, Object> testCase = new HashMap<String, Object>();
        testCase.put("expected_output", "1");
        when(mapper.selectSnapshotCases(80L, false)).thenReturn(Collections.singletonList(testCase));
        givenResult(statusId, "", null);
        // 未完成结果仍须保留令牌，模拟 Judge0 多次返回处理中。
        Judge0Result pending = new Judge0Result();
        pending.setToken("t1");
        pending.setStatusId(statusId);
        when(client.poll(anyString())).thenReturn(pending);

        service.judgeSubmission(91L);

        org.mockito.ArgumentCaptor<Map<String, Object>> saved = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(mapper).updateSubmissionResult(saved.capture());
        assertEquals("SERVICE_ERROR", saved.getValue().get("statusCode"));
        org.junit.jupiter.api.Assertions.assertNull(saved.getValue().get("score"));
        verify(mapper, never()).upsertProgress(any());
        verify(mapper, never()).insertSubmissionCase(any());
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

    /** 交互运行有学生/班级限流；这里让同一学生始终拿到槽位，专注验证运行结果本身。 */
    private RedisCache allowingRedis() {
        RedisCache cache = mock(RedisCache.class);
        when(cache.setCacheObjectIfAbsent(anyString(), any(), anyLong(), any(java.util.concurrent.TimeUnit.class)))
                .thenReturn(true);
        when(cache.incrementCounterWithTtl(anyString(), anyLong(), any(java.util.concurrent.TimeUnit.class)))
                .thenReturn(1L);
        return cache;
    }

    /** 模拟同一学生上一次运行还没释放槽位：第二次点击必须被挡住，而不是并发占用判题机。 */
    private RedisCache busyRedis() {
        RedisCache cache = mock(RedisCache.class);
        when(cache.setCacheObjectIfAbsent(anyString(), any(), anyLong(), any(java.util.concurrent.TimeUnit.class)))
                .thenReturn(false);
        return cache;
    }
}
