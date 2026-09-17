package com.ruoyi.business.service;

import com.ruoyi.business.domain.BizGuideSheet;
import com.ruoyi.business.domain.BizGuideSheetAnswer;
import com.ruoyi.business.domain.BizLessonGuideSheetBinding;
import com.ruoyi.business.domain.vo.LessonDetailVo;
import com.ruoyi.business.mapper.GuideSheetBindingMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonGuideSheetBindingServiceTest
{
    @Mock
    private GuideSheetBindingMapper bindingMapper;
    @Mock
    private GuideSheetAccessService accessService;
    @InjectMocks
    private LessonGuideSheetBindingService service;

    @Test
    void closeKeepsBindingAndReopenRestoresIt()
    {
        BizLessonGuideSheetBinding current = binding("Y", "old");
        when(bindingMapper.selectCurrentByLessonId(3L)).thenReturn(current);
        when(bindingMapper.updateEnabled(eq(7L), any(), eq("teacher"), any())).thenReturn(1);
        LessonDetailVo close = detail(false, 5L, false);
        assertSame(current, service.synchronize(close, 3L, 8L, "teacher"));
        assertEquals("N", current.getEnabled());
        verify(bindingMapper).updateEnabled(eq(7L), eq("N"), eq("teacher"), any());

        LessonDetailVo reopen = detail(true, 5L, false);
        assertSame(current, service.synchronize(reopen, 3L, 8L, "teacher"));
        assertEquals("Y", current.getEnabled());
        verify(bindingMapper).updateEnabled(eq(7L), eq("Y"), eq("teacher"), any());
        verify(bindingMapper, never()).archiveCurrentByLessonId(eq(3L), any(), any());
    }

    @Test
    void ordinarySaveDoesNotRefreshExistingSnapshot()
    {
        BizLessonGuideSheetBinding current = binding("Y", "old");
        when(bindingMapper.selectCurrentByLessonId(3L)).thenReturn(current);

        BizLessonGuideSheetBinding result = service.synchronize(
                detail(true, 5L, false), 3L, 8L, "teacher");

        assertSame(current, result);
        assertEquals("old", result.getSnapshotFormJson());
        verify(accessService, never()).requireSelectableTemplate(5L);
        verify(bindingMapper, never()).insertBinding(any());
    }

    @Test
    void selectingOriginalTemplateAfterChangingMindDoesNotCreateSnapshot()
    {
        BizLessonGuideSheetBinding current = binding("Y", "old");
        when(bindingMapper.selectCurrentByLessonId(3L)).thenReturn(current);

        BizLessonGuideSheetBinding result = service.synchronize(
                detail(true, 5L, true), 3L, 8L, "teacher");

        assertSame(current, result);
        verify(accessService, never()).requireSelectableTemplate(any());
        verify(bindingMapper, never()).archiveCurrentByLessonId(eq(3L), any(), any());
        verify(bindingMapper, never()).insertBinding(any());
    }

    @Test
    void explicitReplaceCreatesLatestTemplateSnapshotAndKeepsHistoricalAnswer()
    {
        BizLessonGuideSheetBinding previous = binding("Y", "old");
        BizGuideSheetAnswer historicalAnswer = new BizGuideSheetAnswer();
        historicalAnswer.setAnswerId(11L);
        historicalAnswer.setBindingId(previous.getBindingId());
        historicalAnswer.setStudentId(9L);
        historicalAnswer.setAnswerJson("{\"q1\":\"历史答案\"}");
        when(bindingMapper.selectCurrentByLessonId(3L)).thenReturn(previous);
        BizGuideSheet source = new BizGuideSheet();
        source.setSheetId(6L);
        source.setVersionNo(2);
        source.setSheetTitle("新版");
        source.setFormJson("{\"widgetList\":[{\"type\":\"input\"}]}");
        when(accessService.requireSelectableTemplate(6L)).thenReturn(source);
        doAnswer(invocation -> {
            previous.setIsCurrent("N");
            return 1;
        }).when(bindingMapper).archiveCurrentByLessonId(eq(3L), eq("teacher"), any());
        when(bindingMapper.insertBinding(any())).thenReturn(1);
        when(bindingMapper.countCurrentByLessonId(3L)).thenReturn(1);

        service.synchronize(detail(true, 6L, true), 3L, 8L, "teacher");

        ArgumentCaptor<BizLessonGuideSheetBinding> captor =
                ArgumentCaptor.forClass(BizLessonGuideSheetBinding.class);
        verify(bindingMapper).archiveCurrentByLessonId(eq(3L), eq("teacher"), any());
        verify(bindingMapper).insertBinding(captor.capture());
        assertEquals(2, captor.getValue().getSourceVersion());
        assertEquals(source.getFormJson(), captor.getValue().getSnapshotFormJson());
        assertEquals("N", previous.getIsCurrent());
        // 更换只归档旧绑定，答卷继续通过原 bindingId 保持历史归属。
        assertEquals(previous.getBindingId(), historicalAnswer.getBindingId());
        assertEquals("{\"q1\":\"历史答案\"}", historicalAnswer.getAnswerJson());
    }

    @Test
    void changingTemplateWithoutExplicitReplaceIsRejected()
    {
        when(bindingMapper.selectCurrentByLessonId(3L)).thenReturn(binding("Y", "old"));

        com.ruoyi.common.exception.ServiceException error = assertThrows(
                com.ruoyi.common.exception.ServiceException.class,
                () -> service.synchronize(detail(true, 6L, false), 3L, 8L, "teacher"));

        assertTrue(error.getMessage().contains("明确更换"));
        verify(bindingMapper, never()).archiveCurrentByLessonId(eq(3L), any(), any());
    }

    @Test
    void emptyTemplateCannotCreateCourseSnapshot()
    {
        when(bindingMapper.selectCurrentByLessonId(3L)).thenReturn(null);
        BizGuideSheet source = new BizGuideSheet();
        source.setSheetId(5L);
        source.setFormJson("{\"widgetList\":[]}");
        when(accessService.requireSelectableTemplate(5L)).thenReturn(source);

        assertThrows(com.ruoyi.common.exception.ServiceException.class,
                () -> service.synchronize(detail(true, 5L, false), 3L, 8L, "teacher"));

        verify(bindingMapper, never()).insertBinding(any());
    }

    @Test
    void anyHistoricalBindingPreventsPhysicalLessonDeletion()
    {
        when(bindingMapper.countByLessonId(3L)).thenReturn(1);

        assertThrows(com.ruoyi.common.exception.ServiceException.class,
                () -> service.assertLessonHasNoHistory(3L));
    }

    private LessonDetailVo detail(boolean enabled, Long sourceSheetId, boolean replace)
    {
        LessonDetailVo detail = new LessonDetailVo();
        detail.setGuideSheetEnabled(enabled);
        detail.setSourceSheetId(sourceSheetId);
        detail.setGuideSheetReplaceRequested(replace);
        return detail;
    }

    private BizLessonGuideSheetBinding binding(String enabled, String formJson)
    {
        BizLessonGuideSheetBinding binding = new BizLessonGuideSheetBinding();
        binding.setBindingId(7L);
        binding.setLessonId(3L);
        binding.setSourceSheetId(5L);
        binding.setIsCurrent("Y");
        binding.setEnabled(enabled);
        binding.setSnapshotFormJson(formJson);
        return binding;
    }
}
