package com.ruoyi.business.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.dto.GuideSheetAiGenerateRequest;
import com.ruoyi.business.domain.vo.GuideSheetAiContentVo;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuideSheetAiContentServiceTest
{
    @Mock
    private AiChatGateway gateway;

    @Test
    void unconfiguredAiReturnsGenericUnavailableWithoutCallingNetwork() throws Exception
    {
        when(gateway.isConfigured()).thenReturn(false);
        GuideSheetAiContentService service = new GuideSheetAiContentService(gateway);

        GuideSheetAiContentVo result = service.generate(8L, request("learningObjectives"));
        String json = new ObjectMapper().writeValueAsString(result);

        assertFalse(result.isAvailable());
        assertEquals("AI 服务暂不可用", result.getMessage());
        assertFalse(json.contains("provider"));
        assertFalse(json.contains("model"));
        assertFalse(json.contains("baseUrl"));
        assertFalse(json.contains("apiKey"));
        assertFalse(json.contains("prompt"));
        verify(gateway, never()).chat(anyString(), anyInt(), anyInt());
    }

    @Test
    void providerFailureIsSeparatedFromOrdinaryDraftSaving()
    {
        when(gateway.isConfigured()).thenReturn(true);
        when(gateway.chat(anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("secret-key at https://provider.invalid"));
        GuideSheetAiContentService service = new GuideSheetAiContentService(gateway);

        GuideSheetAiContentVo result = service.generate(8L, request("preClassCheck"));

        assertFalse(result.isAvailable());
        assertEquals("AI 服务暂不可用", result.getMessage());
        assertEquals(null, result.getContent());
    }

    @Test
    void successfulGenerationReturnsOnlyDraftContent()
    {
        when(gateway.isConfigured()).thenReturn(true);
        when(gateway.chat(anyString(), anyInt(), anyInt())).thenReturn("1. 认识循环结构");
        GuideSheetAiContentService service = new GuideSheetAiContentService(gateway);
        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);

        GuideSheetAiContentVo result = service.generate(8L, request("generateObjectives"));

        assertTrue(result.isAvailable());
        assertEquals("1. 认识循环结构", result.getContent());
        verify(gateway).chat(prompt.capture(), anyInt(), anyInt());
        assertTrue(prompt.getValue().contains("七年级"));
        assertTrue(prompt.getValue().contains("循环结构"));
    }

    @Test
    void preClassGenerationDoesNotAskForStudentVisibleAnswers()
    {
        when(gateway.isConfigured()).thenReturn(true);
        when(gateway.chat(anyString(), anyInt(), anyInt())).thenReturn("1. 什么是循环？");
        GuideSheetAiContentService service = new GuideSheetAiContentService(gateway);
        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);

        service.generate(8L, request("preClassCheck"));

        verify(gateway).chat(prompt.capture(), anyInt(), anyInt());
        assertTrue(prompt.getValue().contains("不输出答案"));
    }

    @Test
    void unknownActionIsRejectedBeforeCallingProvider()
    {
        when(gateway.isConfigured()).thenReturn(true);
        GuideSheetAiContentService service = new GuideSheetAiContentService(gateway);

        assertThrows(ServiceException.class, () -> service.generate(8L, request("rawPrompt")));
        verify(gateway, never()).chat(anyString(), anyInt(), anyInt());
    }

    @Test
    void sameTeacherIsLimitedToOneRequestPerTenSeconds()
    {
        when(gateway.isConfigured()).thenReturn(true);
        when(gateway.chat(anyString(), anyInt(), anyInt())).thenReturn("内容");
        AtomicLong now = new AtomicLong(1L);
        GuideSheetAiContentService service = new GuideSheetAiContentService(
                gateway, now::get, new Semaphore(4));

        service.generate(8L, request("generateObjectives"));
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.generate(8L, request("generateObjectives")));

        assertTrue(error.getMessage().contains("10 秒"));
        verify(gateway, times(1)).chat(anyString(), anyInt(), anyInt());
    }

    @Test
    void teacherCanRetryAfterRateLimitWindow()
    {
        when(gateway.isConfigured()).thenReturn(true);
        when(gateway.chat(anyString(), anyInt(), anyInt())).thenReturn("内容");
        AtomicLong now = new AtomicLong(1L);
        GuideSheetAiContentService service = new GuideSheetAiContentService(
                gateway, now::get, new Semaphore(4));

        service.generate(8L, request("generateObjectives"));
        now.addAndGet(TimeUnit.SECONDS.toNanos(10));
        service.generate(8L, request("generateObjectives"));

        verify(gateway, times(2)).chat(anyString(), anyInt(), anyInt());
    }

    @Test
    void unconfiguredAiDoesNotConsumeTeacherRateLimit()
    {
        when(gateway.isConfigured()).thenReturn(false, true);
        when(gateway.chat(anyString(), anyInt(), anyInt())).thenReturn("内容");
        GuideSheetAiContentService service = new GuideSheetAiContentService(gateway);

        assertFalse(service.generate(8L, request("generateObjectives")).isAvailable());
        assertTrue(service.generate(8L, request("generateObjectives")).isAvailable());

        verify(gateway, times(1)).chat(anyString(), anyInt(), anyInt());
    }

    @Test
    void globalConcurrencyLimitRejectsFifthRequestAndReleasesPermits() throws Exception
    {
        when(gateway.isConfigured()).thenReturn(true);
        CountDownLatch entered = new CountDownLatch(4);
        CountDownLatch release = new CountDownLatch(1);
        when(gateway.chat(anyString(), anyInt(), anyInt())).thenAnswer(invocation ->
        {
            entered.countDown();
            if (!release.await(5, TimeUnit.SECONDS))
            {
                throw new IllegalStateException("测试等待超时");
            }
            return "内容";
        });
        GuideSheetAiContentService service = new GuideSheetAiContentService(gateway);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<GuideSheetAiContentVo>> futures = new ArrayList<>();
        try
        {
            for (long teacherId = 1L; teacherId <= 4L; teacherId++)
            {
                final long currentTeacherId = teacherId;
                futures.add(executor.submit(
                        () -> service.generate(currentTeacherId, request("generateObjectives"))));
            }
            assertTrue(entered.await(5, TimeUnit.SECONDS));

            ServiceException busy = assertThrows(ServiceException.class,
                    () -> service.generate(99L, request("generateObjectives")));
            assertTrue(busy.getMessage().contains("繁忙"));

            release.countDown();
            for (Future<GuideSheetAiContentVo> future : futures)
            {
                assertTrue(future.get(5, TimeUnit.SECONDS).isAvailable());
            }
            assertTrue(service.generate(99L, request("generateObjectives")).isAvailable());
        }
        finally
        {
            release.countDown();
            executor.shutdownNow();
        }

        verify(gateway, times(5)).chat(anyString(), anyInt(), anyInt());
    }

    private GuideSheetAiGenerateRequest request(String action)
    {
        GuideSheetAiGenerateRequest request = new GuideSheetAiGenerateRequest();
        request.setAction(action);
        request.setGrade(7);
        request.setLessonNum(3);
        request.setTopic("循环结构");
        request.setInput("这题咋写");
        return request;
    }
}
