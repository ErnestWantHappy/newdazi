package com.ruoyi.framework.web.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.AjaxResult;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.web.bind.MissingServletRequestParameterException;

class GlobalExceptionHandlerTest
{
    @Test
    void unsupportedHttpMethodReturnsBusiness405()
    {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/business/teacher/grading/grade");

        AjaxResult result = handler.handleHttpRequestMethodNotSupported(
                new HttpRequestMethodNotSupportedException("GET"), request);

        assertEquals(HttpStatus.BAD_METHOD, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    void missingParameterReturns400()
    {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        AjaxResult result = handler.handleMissingServletRequestParameterException(
                new MissingServletRequestParameterException("lessonId", "Long"),
                new MockHttpServletRequest("GET", "/business/teacher/grading/deadline-status"));
        assertEquals(HttpStatus.BAD_REQUEST, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    void databaseFailureDoesNotExposeSqlOrPath()
    {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        AjaxResult result = handler.handleDataAccessException(
                new DataAccessResourceFailureException("Unknown column in jar:file:/secret/app.jar SQL select *"),
                new MockHttpServletRequest("GET", "/business/collaboration/lesson/1"));
        String message = String.valueOf(result.get(AjaxResult.MSG_TAG));
        org.junit.jupiter.api.Assertions.assertFalse(message.contains("Unknown column"));
        org.junit.jupiter.api.Assertions.assertFalse(message.contains("jar:file"));
    }

    @Test
    void runtimeFailureDoesNotExposeOriginalMessage()
    {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        AjaxResult result = handler.handleRuntimeException(
                new IllegalStateException("secret SQL /opt/application.jar"),
                new MockHttpServletRequest("GET", "/business/test"));
        assertEquals("系统繁忙，请稍后重试", result.get(AjaxResult.MSG_TAG));
    }
}
