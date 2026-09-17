package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.business.domain.BizLessonTool;
import com.ruoyi.business.mapper.StudentToolMapper;
import com.ruoyi.business.service.impl.StudentToolServiceImpl;
import com.ruoyi.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class StudentLessonToolSaveTest {
    @Mock StudentToolMapper mapper;
    @InjectMocks StudentToolServiceImpl service;

    @Test void omittedToolsPreserveExistingConfiguration() {
        service.replaceLessonTools(7L, null);
        verifyNoInteractions(mapper);
    }

    @Test void explicitEmptyListClearsTools() {
        service.replaceLessonTools(7L, Collections.emptyList());
        verify(mapper).deleteLessonToolsByLessonId(7L);
        verifyNoMoreInteractions(mapper);
    }

    @Test void invalidStructureDoesNotDeleteExistingTools() {
        assertThrows(ServiceException.class, () -> service.replaceLessonTools(7L,
            Arrays.asList(tool("https://example.com/a?x=1&y=2"), null)));
        verifyNoInteractions(mapper);
    }

    @Test void incompleteAndNonWebUrlsArePreservedForLaterEditing() {
        for (String url : Arrays.asList("example.com/tool", "javascript:alert(1)", "https://user:pass@example.com", "")) {
            BizLessonTool tool = tool(url);
            service.replaceLessonTools(7L, Collections.singletonList(tool));
            assertEquals(url, tool.getToolUrl());
            verify(mapper).insertLessonTool(tool);
        }
    }

    @Test void missingNameOrUrlDoesNotPreventSavingCourse() {
        BizLessonTool tool = tool(null);
        tool.setToolName(null);
        service.replaceLessonTools(7L, Collections.singletonList(tool));
        assertEquals("", tool.getToolName());
        assertEquals("", tool.getToolUrl());
        verify(mapper).insertLessonTool(tool);
    }

    @Test void savedUrlRetainsQueryAndFragmentAndTrimsSpaces() {
        BizLessonTool tool = tool(" https://example.com/a?x=1&y=2#page ");
        service.replaceLessonTools(7L, Collections.singletonList(tool));
        assertEquals("https://example.com/a?x=1&y=2#page", tool.getToolUrl());
        assertEquals("实验工具", tool.getToolName());
        verify(mapper).insertLessonTool(tool);
    }

    private BizLessonTool tool(String url) {
        BizLessonTool tool = new BizLessonTool();
        tool.setToolName(" 实验工具 ");
        tool.setToolUrl(url);
        return tool;
    }
}
