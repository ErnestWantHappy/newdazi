package com.ruoyi.business.service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.BizGuideSheet;
import com.ruoyi.business.domain.BizLessonGuideSheetBinding;
import com.ruoyi.business.domain.vo.LessonDetailVo;
import com.ruoyi.business.mapper.GuideSheetBindingMapper;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BeginnerGuideSheetAssemblerServiceTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();
    private BeginnerGuideSheetAssemblerService service;

    @BeforeEach
    void setUp()
    {
        service = new BeginnerGuideSheetAssemblerService(objectMapper);
    }

    @Test
    void exposesExactlyNineBeginnerModulesAndSixPresets()
    {
        assertEquals(9, service.getSupportedModuleTypes().size());
        assertEquals(6, service.getSupportedPresetCodes().size());
    }

    @Test
    void presetProducesCanonicalFormJsonAcceptedByBindingService() throws Exception
    {
        String formJson = service.assemble("classroomTask", Collections.emptyList(), null);
        JsonNode root = objectMapper.readTree(formJson);
        JsonNode homeTab = root.path("widgetList").path(0);
        assertEquals("tab", homeTab.path("type").asText());
        assertEquals("HomeTab", homeTab.path("options").path("name").asText());
        assertTrue(homeTab.path("tabs").path(0).path("widgetList").size() > 0);

        GuideSheetBindingMapper bindingMapper = mock(GuideSheetBindingMapper.class);
        GuideSheetAccessService accessService = mock(GuideSheetAccessService.class);
        when(bindingMapper.selectCurrentByLessonId(3L)).thenReturn(null);
        when(bindingMapper.insertBinding(any())).thenReturn(1);
        when(bindingMapper.countCurrentByLessonId(3L)).thenReturn(1);
        BizGuideSheet template = new BizGuideSheet();
        template.setSheetId(5L);
        template.setVersionNo(1);
        template.setSheetTitle("课堂任务单");
        template.setFormJson(formJson);
        when(accessService.requireSelectableTemplate(5L)).thenReturn(template);
        LessonGuideSheetBindingService bindingService = new LessonGuideSheetBindingService();
        ReflectionTestUtils.setField(bindingService, "bindingMapper", bindingMapper);
        ReflectionTestUtils.setField(bindingService, "accessService", accessService);
        LessonDetailVo detail = new LessonDetailVo();
        detail.setGuideSheetEnabled(true);
        detail.setSourceSheetId(5L);

        BizLessonGuideSheetBinding binding = bindingService.synchronize(detail, 3L, 8L, "teacher");

        assertEquals(formJson, binding.getSnapshotFormJson());
        verify(bindingMapper).insertBinding(any());
    }

    @Test
    void advancedComponentsSurviveBeginnerRoundTripWithoutChanges() throws Exception
    {
        String advanced = "{\"widgetList\":[{\"id\":\"advanced-1\",\"type\":\"custom-widget\","
                + "\"name\":\"advanced_field\",\"options\":{\"customFlag\":true},"
                + "\"children\":[{\"type\":\"unknown-child\",\"payload\":{\"x\":1}}]}],"
                + "\"customRoot\":{\"keep\":true}}";
        JsonNode before = objectMapper.readTree(advanced).path("widgetList").path(0);

        String result = service.assemble(null, Collections.emptyList(), advanced);
        JsonNode root = objectMapper.readTree(result);
        JsonNode paneWidgets = root.path("widgetList").path(0).path("tabs").path(0).path("widgetList");

        assertTrue(containsNode(paneWidgets, before));
        assertTrue(root.path("customRoot").path("keep").asBoolean());
    }

    @Test
    void generatedNamesAvoidExistingFieldsAndRemainUnique() throws Exception
    {
        String existing = "{\"widgetList\":[{\"type\":\"input\","
                + "\"name\":\"bg_singleChoice_1\",\"options\":{\"name\":\"bg_singleChoice_1\"}}]}";

        String result = service.assemble(null,
                Arrays.asList("singleChoice", "singleChoice"), existing);
        JsonNode widgets = objectMapper.readTree(result).path("widgetList").path(0)
                .path("tabs").path(0).path("widgetList");

        assertTrue(containsName(widgets, "bg_singleChoice_1"));
        assertTrue(containsName(widgets, "bg_singleChoice_2"));
        assertTrue(containsName(widgets, "bg_singleChoice_3"));
    }

    @Test
    void textTasksDefaultToManualScoring() throws Exception
    {
        String result = service.assemble(null,
                Arrays.asList("preClassCheck", "shortAnswer"), null);
        JsonNode widgets = objectMapper.readTree(result).path("widgetList").path(0)
                .path("tabs").path(0).path("widgetList");

        assertEquals(2, widgets.size());
        for (JsonNode widget : widgets)
        {
            assertEquals("manual", widget.path("scoring").path("type").asText());
            assertEquals("", widget.path("scoring").path("answer").asText());
        }
    }

    @Test
    void utf8ContentBeyondTwoMegabytesIsRejected()
    {
        char[] padding = new char[700_000];
        Arrays.fill(padding, '测');
        String existing = "{\"widgetList\":[],\"padding\":\"" + new String(padding) + "\"}";

        assertTrue(existing.length() < 2 * 1024 * 1024);
        assertTrue(existing.getBytes(StandardCharsets.UTF_8).length > 2 * 1024 * 1024);
        assertThrows(ServiceException.class,
                () -> service.assemble(null, Collections.emptyList(), existing));
    }

    @Test
    void malformedLegacyFormIsRejectedInsteadOfSilentlyDiscarded()
    {
        assertThrows(ServiceException.class,
                () -> service.assemble(null, Collections.emptyList(), "{broken"));
    }

    private boolean containsNode(JsonNode array, JsonNode expected)
    {
        for (JsonNode item : array)
        {
            if (item.equals(expected))
            {
                return true;
            }
        }
        return false;
    }

    private boolean containsName(JsonNode array, String name)
    {
        for (JsonNode item : array)
        {
            if (name.equals(item.path("name").asText()))
            {
                return true;
            }
        }
        return false;
    }
}
