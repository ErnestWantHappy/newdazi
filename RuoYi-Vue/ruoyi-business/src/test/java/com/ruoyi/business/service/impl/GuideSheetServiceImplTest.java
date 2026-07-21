package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizGuideSheet;
import com.ruoyi.business.domain.dto.GuideSheetAiGenerateRequest;
import com.ruoyi.business.domain.vo.GuideSheetVo;
import com.ruoyi.business.domain.vo.GuideSheetProgressVo;
import com.ruoyi.business.mapper.GuideSheetMapper;
import com.ruoyi.business.mapper.GuideSheetProgressMapper;
import com.ruoyi.business.service.AiChatGateway;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.business.service.GuideSheetAiContentService;
import com.ruoyi.business.service.OrganizationBoundaryService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GuideSheetServiceImplTest
{
    private final GuideSheetServiceImpl service = new GuideSheetServiceImpl();

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void emptyTemplateCannotBeSaved()
    {
        GuideSheetVo template = validTemplate();
        template.setFormJson("{\"widgetList\":[]}");

        assertThrows(ServiceException.class, () -> service.saveGuideSheetDetail(template));
    }

    @Test
    void malformedTemplateCannotBeSaved()
    {
        GuideSheetVo template = validTemplate();
        template.setFormJson("{broken");

        assertThrows(ServiceException.class, () -> service.saveGuideSheetDetail(template));
    }

    @Test
    void aiFailureDoesNotBlockOrdinaryTemplateSaving()
    {
        AiChatGateway gateway = mock(AiChatGateway.class);
        when(gateway.isConfigured()).thenReturn(true);
        when(gateway.chat(anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("provider unavailable"));
        GuideSheetAiContentService aiService = new GuideSheetAiContentService(gateway);
        GuideSheetAiGenerateRequest aiRequest = new GuideSheetAiGenerateRequest();
        aiRequest.setAction("generateObjectives");
        aiRequest.setGrade(6);
        aiRequest.setLessonNum(1);
        aiRequest.setTopic("网络安全");
        assertFalse(aiService.generate(8L, aiRequest).isAvailable());

        GuideSheetMapper mapper = mock(GuideSheetMapper.class);
        OrganizationBoundaryService boundaryService = mock(OrganizationBoundaryService.class);
        ReflectionTestUtils.setField(service, "guideSheetMapper", mapper);
        ReflectionTestUtils.setField(service, "organizationBoundaryService", boundaryService);
        when(boundaryService.resolveCountyDeptId(10L)).thenReturn(100L);
        when(mapper.insertBizGuideSheet(any())).thenAnswer(invocation -> {
            BizGuideSheet sheet = invocation.getArgument(0);
            sheet.setSheetId(21L);
            return 1;
        });
        SysUser user = new SysUser();
        user.setUserName("teacher");
        LoginUser loginUser = new LoginUser(8L, 10L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
        GuideSheetVo template = validTemplate();
        template.setFormJson("{\"widgetList\":[{\"type\":\"input\"}]}");

        GuideSheetVo saved = service.saveGuideSheetDetail(template);

        assertEquals(21L, saved.getSheetId());
        verify(mapper).insertBizGuideSheet(any());
    }

    @Test
    void staleTemplateVersionCannotOverwriteNewerContent()
    {
        GuideSheetMapper mapper = mock(GuideSheetMapper.class);
        GuideSheetAccessService accessService = mock(GuideSheetAccessService.class);
        ReflectionTestUtils.setField(service, "guideSheetMapper", mapper);
        ReflectionTestUtils.setField(service, "accessService", accessService);
        authenticateTeacher();

        BizGuideSheet existing = existingTemplate(31L, 3);
        when(mapper.selectBizGuideSheetBySheetId(31L)).thenReturn(existing);
        GuideSheetVo update = validTemplate();
        update.setSheetId(31L);
        update.setVersionNo(2);
        update.setFormJson("{\"widgetList\":[{\"type\":\"input\"}]}");

        assertThrows(ServiceException.class, () -> service.saveGuideSheetDetail(update));

        verify(accessService).assertCanManageTemplate(31L);
        verify(mapper, never()).updateBizGuideSheet(any());
    }

    @Test
    void matchingTemplateVersionIsUsedForCompareAndSwap()
    {
        GuideSheetMapper mapper = mock(GuideSheetMapper.class);
        GuideSheetAccessService accessService = mock(GuideSheetAccessService.class);
        ReflectionTestUtils.setField(service, "guideSheetMapper", mapper);
        ReflectionTestUtils.setField(service, "accessService", accessService);
        authenticateTeacher();

        BizGuideSheet existing = existingTemplate(32L, 4);
        BizGuideSheet saved = existingTemplate(32L, 5);
        when(mapper.selectBizGuideSheetBySheetId(32L)).thenReturn(existing, saved);
        when(mapper.updateBizGuideSheet(any())).thenReturn(1);
        GuideSheetVo update = validTemplate();
        update.setSheetId(32L);
        update.setVersionNo(4);
        update.setFormJson("{\"widgetList\":[{\"type\":\"input\"}]}");

        GuideSheetVo result = service.saveGuideSheetDetail(update);

        ArgumentCaptor<BizGuideSheet> captor = ArgumentCaptor.forClass(BizGuideSheet.class);
        verify(mapper).updateBizGuideSheet(captor.capture());
        assertEquals(Integer.valueOf(4), captor.getValue().getVersionNo());
        assertEquals(Integer.valueOf(5), result.getVersionNo());
    }

    @Test
    void currentRosterAndHistoricalStudentsAreMerged()
    {
        GuideSheetProgressMapper mapper = mock(GuideSheetProgressMapper.class);
        ReflectionTestUtils.setField(service, "guideSheetProgressMapper", mapper);
        GuideSheetProgressVo current = progress(1L);
        GuideSheetProgressVo historicalCurrent = progress(1L);
        GuideSheetProgressVo transferred = progress(2L);
        when(mapper.selectFullProgressByBindingAndClass(7L, 10L, "2025", "1"))
                .thenReturn(Collections.singletonList(current));
        when(mapper.selectByBindingAndClass(7L, 10L, "2025", "1"))
                .thenReturn(Arrays.asList(historicalCurrent, transferred));

        List<GuideSheetProgressVo> result = service.getProgress(7L, 10L, "2025", "1");

        assertEquals(2, result.size());
        assertEquals(Long.valueOf(1L), result.get(0).getStudentId());
        assertEquals(Long.valueOf(2L), result.get(1).getStudentId());
    }

    private GuideSheetVo validTemplate()
    {
        GuideSheetVo template = new GuideSheetVo();
        template.setSheetTitle("课堂任务单");
        template.setGrade(6);
        template.setSemester("0");
        template.setLessonNum(1);
        template.setIsPublic("N");
        return template;
    }

    private BizGuideSheet existingTemplate(Long sheetId, Integer versionNo)
    {
        BizGuideSheet template = new BizGuideSheet();
        template.setSheetId(sheetId);
        template.setVersionNo(versionNo);
        template.setDelFlag("0");
        template.setSheetTitle("课堂任务单");
        template.setGrade(6);
        template.setSemester("0");
        template.setLessonNum(1);
        template.setIsPublic("N");
        template.setFormJson("{\"widgetList\":[{\"type\":\"input\"}]}");
        return template;
    }

    private void authenticateTeacher()
    {
        SysUser user = new SysUser();
        user.setUserName("teacher");
        LoginUser loginUser = new LoginUser(8L, 10L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    private GuideSheetProgressVo progress(Long studentId)
    {
        GuideSheetProgressVo row = new GuideSheetProgressVo();
        row.setStudentId(studentId);
        return row;
    }
}
