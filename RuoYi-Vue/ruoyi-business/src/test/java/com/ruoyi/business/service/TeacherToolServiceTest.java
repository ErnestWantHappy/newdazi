package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.business.domain.dto.TeacherToolSaveRequest;
import com.ruoyi.business.domain.BizTeacherTool;
import com.ruoyi.business.domain.vo.TeacherToolCatalogVo;
import com.ruoyi.business.domain.vo.TeacherToolCategoryVo;
import com.ruoyi.business.domain.vo.TeacherToolVo;
import com.ruoyi.business.mapper.TeacherToolMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.core.redis.RedisCache;

@ExtendWith(MockitoExtension.class)
class TeacherToolServiceTest
{
    @Mock private TeacherToolMapper mapper;
    @Mock private RedisCache redisCache;
    @InjectMocks private TeacherToolService service;

    @Test
    void catalogGroupsToolsAndRemovesEmptyCategories()
    {
        TeacherToolCategoryVo first = category(1L, "小学实用工具");
        TeacherToolCategoryVo empty = category(2L, "空分类");
        TeacherToolVo tool = new TeacherToolVo();
        tool.setToolId(11L);
        tool.setCategoryId(1L);
        tool.setTitle("打字平台");
        when(mapper.selectCatalogCategories()).thenReturn(Arrays.asList(first, empty));
        when(mapper.selectCatalogTools()).thenReturn(Collections.singletonList(tool));
        when(mapper.selectRecommendedTools()).thenReturn(Collections.singletonList(tool));

        TeacherToolCatalogVo result = service.getCatalog();

        assertEquals(1, result.getCategories().size());
        assertEquals("打字平台", result.getCategories().get(0).getTools().get(0).getTitle());
        assertEquals(1, result.getRecommended().size());
    }

    @Test
    void catalogCacheHitDoesNotQueryDatabase()
    {
        TeacherToolCatalogVo cached = new TeacherToolCatalogVo();
        when(redisCache.getCacheObject(anyString())).thenReturn(cached);

        TeacherToolCatalogVo result = service.getCatalog();

        assertSame(cached, result);
        verify(mapper, never()).selectCatalogCategories();
        verify(mapper, never()).selectCatalogTools();
        verify(mapper, never()).selectRecommendedTools();
    }

    @Test
    void rejectsUnsafeOrCredentialBearingUrls()
    {
        assertInvalidUrl("javascript:alert(1)");
        assertInvalidUrl("file:///C:/secret.txt");
        assertInvalidUrl("https://user:password@example.com/tool");
        assertInvalidUrl("/relative/tool");
    }

    @Test
    void blankManualSourceReferenceIsStoredAsNull()
    {
        TeacherToolSaveRequest request = new TeacherToolSaveRequest();
        request.setTitle("手工工具");
        request.setDescription("允许不填写内部来源标识");
        request.setUrl("https://example.com/tool");
        request.setSourceType("MANUAL");
        request.setSourceRef("  ");
        request.setCategoryIds(Collections.singletonList(1L));
        BizTeacherTool tool = ReflectionTestUtils.invokeMethod(service, "toTool", request);

        assertEquals(null, tool.getSourceRef());
    }

    private void assertInvalidUrl(String url)
    {
        TeacherToolSaveRequest request = new TeacherToolSaveRequest();
        request.setTitle("测试工具");
        request.setDescription("测试工具说明");
        request.setUrl(url);
        request.setCategoryIds(Collections.singletonList(1L));
        assertThrows(ServiceException.class, () -> service.createTool(request));
    }

    private TeacherToolCategoryVo category(Long id, String name)
    {
        TeacherToolCategoryVo category = new TeacherToolCategoryVo();
        category.setCategoryId(id);
        category.setCategoryName(name);
        return category;
    }
}
