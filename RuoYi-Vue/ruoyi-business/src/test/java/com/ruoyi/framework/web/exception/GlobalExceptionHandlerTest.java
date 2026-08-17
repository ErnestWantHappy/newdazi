package com.ruoyi.framework.web.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.AjaxResult;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;

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
}
