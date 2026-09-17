package com.ruoyi.business.controller;

import com.ruoyi.business.domain.BizGuideSheetAnswer;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.business.service.IGuideSheetAnswerService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GuideSheetControllerTeacherAnswerTest
{
    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void teacherAnswerDetailChecksBindingClassAndStudentScope()
    {
        GuideSheetController controller = new GuideSheetController();
        GuideSheetAccessService accessService = mock(GuideSheetAccessService.class);
        IGuideSheetAnswerService answerService = mock(IGuideSheetAnswerService.class);
        ReflectionTestUtils.setField(controller, "accessService", accessService);
        ReflectionTestUtils.setField(controller, "answerService", answerService);
        authenticateTeacher();

        BizGuideSheetAnswer answer = new BizGuideSheetAnswer();
        answer.setAnswerJson("{\"q1\":\"学生回答\"}");
        answer.setStatus("2");
        when(answerService.getByStudentAndBinding(9L, 7L)).thenReturn(answer);

        AjaxResult result = controller.getTeacherStudentAnswer(7L, 9L, "2025", "1");

        verify(accessService).requireBindingClassAccess(7L, "2025", "1");
        verify(accessService).assertStudentInBindingClass(7L, 9L, 10L, "2025", "1");
        assertEquals(Boolean.TRUE, result.get("hasAnswer"));
        assertEquals("{\"q1\":\"学生回答\"}", result.get("answerJson"));
    }

    private void authenticateTeacher()
    {
        SysUser user = new SysUser();
        user.setUserName("teacher");
        LoginUser loginUser = new LoginUser(8L, 10L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }
}
