package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.business.domain.dto.TeacherToolQuery;
import com.ruoyi.business.domain.dto.TeacherToolSaveRequest;
import com.ruoyi.business.domain.BizTeacherTool;
import com.ruoyi.business.domain.vo.TeacherToolCatalogVo;
import com.ruoyi.business.domain.vo.TeacherToolCategoryVo;
import com.ruoyi.business.domain.vo.TeacherToolTaskVo;
import com.ruoyi.business.domain.vo.TeacherToolTermVo;
import com.ruoyi.business.domain.vo.TeacherToolVo;
import com.ruoyi.business.mapper.TeacherToolMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
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
    void catalogCacheInvalidationFailureDoesNotFailCommittedWrite()
    {
        when(redisCache.deleteObject(anyString())).thenThrow(new RuntimeException("redis down"));
        // 数据库写入已经提交，清缓存失败只能记录告警，不能把成功结果改写成前端失败。
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(service, "invalidateCatalog"));
        verify(redisCache).deleteObject(anyString());
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

    @Test
    void catalogIncludesTasksAndTerms()
    {
        TeacherToolTaskVo task = new TeacherToolTaskVo();
        task.setTaskCode("material");
        task.setTaskName("制作教学素材");
        task.setToolCount(16);
        TeacherToolTermVo term = new TeacherToolTermVo();
        term.setToolId(47L);
        term.setTerm("压缩图片");
        when(mapper.selectCatalogCategories()).thenReturn(Collections.emptyList());
        when(mapper.selectCatalogTools()).thenReturn(Collections.emptyList());
        when(mapper.selectRecommendedTools()).thenReturn(Collections.emptyList());
        when(mapper.selectCatalogTasks()).thenReturn(Collections.singletonList(task));
        when(mapper.selectCatalogTerms()).thenReturn(Collections.singletonList(term));

        TeacherToolCatalogVo result = service.getCatalog();

        assertEquals("material", result.getTasks().get(0).getTaskCode());
        assertEquals(16, result.getTasks().get(0).getToolCount());
        assertEquals("压缩图片", result.getTerms().get(0).getTerm());
    }

    @Test
    void aliasesAndTaskCodesAreNormalizedBeforeSaving()
    {
        TeacherToolSaveRequest request = new TeacherToolSaveRequest();
        request.setTitle("Optimizilla 图片压缩");
        request.setDescription("在线批量压缩 JPG 和 PNG 图片");
        request.setUrl("https://example.com/tool");
        request.setAliases(" 压缩图片，图片压缩、压缩图片 ");
        request.setTaskTags("MATERIAL, material,resources");
        request.setCategoryIds(Collections.singletonList(1L));
        BizTeacherTool tool = ReflectionTestUtils.invokeMethod(service, "toTool", request);

        assertEquals("压缩图片、图片压缩", tool.getAliases());
        assertEquals("material,resources", tool.getTaskTags());
    }

    @Test
    void aliasesDeduplicateIgnoringCaseForCiCollation()
    {
        // MySQL 默认校对不区分大小写：PPT 与 ppt 应视为同一别名，避免入库撞唯一键。
        TeacherToolSaveRequest request = new TeacherToolSaveRequest();
        request.setTitle("测试工具");
        request.setDescription("测试工具说明");
        request.setUrl("https://example.com/tool");
        request.setAliases("PPT, ppt, 演示文稿");
        request.setCategoryIds(Collections.singletonList(1L));
        BizTeacherTool tool = ReflectionTestUtils.invokeMethod(service, "toTool", request);

        assertEquals("PPT、演示文稿", tool.getAliases());
    }

    @Test
    void createToolRejectsDisabledCategories()
    {
        TeacherToolSaveRequest request = new TeacherToolSaveRequest();
        request.setTitle("测试工具");
        request.setDescription("测试工具说明");
        request.setUrl("https://example.com/tool");
        request.setTaskTags("material");
        request.setCategoryIds(Collections.singletonList(1L));
        when(mapper.selectAllTaskList()).thenReturn(Collections.singletonList(task("material", "制作教学素材")));
        // 启用口径查不到：说明分类不存在、已删除或已停用，新增直接拒绝。
        when(mapper.countCategoriesByIds(anyList())).thenReturn(0);

        assertThrows(ServiceException.class, () -> service.createTool(request));
    }

    @Test
    void updateToolKeepsExistingDisabledCategoryButRejectsNewDisabledOne()
    {
        TeacherToolSaveRequest request = new TeacherToolSaveRequest();
        request.setTitle("测试工具");
        request.setDescription("测试工具说明");
        request.setUrl("https://example.com/tool");
        request.setTaskTags("material");
        request.setCategoryIds(Arrays.asList(1L, 2L));
        when(mapper.selectAllTaskList()).thenReturn(Collections.singletonList(task("material", "制作教学素材")));
        BizTeacherTool existing = new BizTeacherTool();
        existing.setToolId(9L);
        existing.setDelFlag("0");
        when(mapper.selectToolById(9L)).thenReturn(existing);
        // 已有归属含 1L：编辑保留它不算新增；2L 不在已有归属里，按新增停用拒绝。
        when(mapper.selectCategoryIdsByToolId(9L)).thenReturn(Collections.singletonList(1L));
        when(mapper.countExistingCategoriesByIds(anyList())).thenReturn(1);
        when(mapper.countCategoriesByIds(anyList())).thenReturn(0);

        ServiceException ex = org.junit.jupiter.api.Assertions.assertThrows(ServiceException.class,
                () -> service.updateTool(9L, request));
        assertEquals("新选择的分类已停用，请先启用或移除", ex.getMessage());
    }

    @Test
    void switchStatusRejectsBlankInsteadOfDefaultingToEnabled()
    {
        BizTeacherTool existing = new BizTeacherTool();
        existing.setToolId(9L);
        existing.setDelFlag("0");
        when(mapper.selectToolById(9L)).thenReturn(existing);

        // 上下架开关不再用空值默认上架：误触空状态必须显式拒绝。
        assertThrows(ServiceException.class, () -> service.updateToolStatus(9L, null));
        assertThrows(ServiceException.class, () -> service.updateToolStatus(9L, "  "));
        verify(mapper, never()).updateToolStatus(org.mockito.ArgumentMatchers.eq(9L), anyString(), anyString());
    }

    @Test
    void managementKeywordEscapesLikeWildcards()
    {
        TeacherToolQuery query = new TeacherToolQuery();
        query.setKeyword("100%_匹配\\原文");
        // 管理列表真正使用的是同一个 Query 对象：此处直接调用 listTools 完成转义。
        when(mapper.selectToolList(org.mockito.ArgumentMatchers.any())).thenReturn(Collections.emptyList());
        service.listTools(query);
        ArgumentCaptor<TeacherToolQuery> captor = ArgumentCaptor.forClass(TeacherToolQuery.class);
        verify(mapper).selectToolList(captor.capture());
        assertEquals("100\\%\\_匹配\\\\原文", captor.getValue().getKeyword());
    }

    @Test
    void nullOnlyCategoryIdsNeverReachDatabase()
    {
        TeacherToolSaveRequest request = new TeacherToolSaveRequest();
        request.setTitle("测试工具");
        request.setDescription("测试工具说明");
        request.setUrl("https://example.com/tool");
        List<Long> onlyNull = new ArrayList<>();
        onlyNull.add(null);
        request.setCategoryIds(onlyNull);
        // 请求里只有一个 null：业务层先拒绝，不允许拼出“IN ()”打到数据库。
        assertThrows(ServiceException.class, () -> service.createTool(request));
        verify(mapper, never()).countCategoriesByIds(anyList());
        verify(mapper, never()).insertTool(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void categoryIdsWithNullEntriesAreCleanedBeforeInsert()
    {
        TeacherToolSaveRequest request = new TeacherToolSaveRequest();
        request.setTitle("测试工具");
        request.setDescription("测试工具说明");
        request.setUrl("https://example.com/tool");
        List<Long> withNull = new ArrayList<>();
        withNull.add(1L);
        withNull.add(null);
        withNull.add(1L);
        request.setCategoryIds(withNull);
        when(mapper.selectAllTaskList()).thenReturn(Collections.emptyList());
        when(mapper.countCategoriesByIds(anyList())).thenReturn(1);
        BizTeacherTool inserted = new BizTeacherTool();
        inserted.setToolId(99L);
        org.mockito.Mockito.doAnswer(invocation -> {
            BizTeacherTool arg = invocation.getArgument(0);
            arg.setToolId(99L);
            return 1;
        }).when(mapper).insertTool(org.mockito.ArgumentMatchers.any(BizTeacherTool.class));
        SysUser user = new SysUser();
        user.setUserId(1L);
        user.setUserName("tester");
        LoginUser loginUser = new LoginUser(1L, 1L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, "n/a", Collections.emptyList()));
        try
        {
            service.createTool(request);
        }
        finally
        {
            SecurityContextHolder.clearContext();
        }
        // 去空去重后只剩一个 1L：真正入库的关系必须干净，不能带 null。
        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        verify(mapper).insertToolCategoryRelations(org.mockito.ArgumentMatchers.eq(99L), captor.capture());
        assertEquals(Collections.singletonList(1L), captor.getValue());
    }

    @Test
    void rejectsTooManyAliases()
    {
        TeacherToolSaveRequest request = new TeacherToolSaveRequest();
        request.setTitle("测试工具");
        request.setDescription("测试工具说明");
        request.setUrl("https://example.com/tool");
        request.setAliases("别名1、别名2、别名3、别名4、别名5、别名6、别名7、别名8、别名9、别名10、"
                + "别名11、别名12、别名13、别名14、别名15、别名16、别名17、别名18、别名19、别名20、别名21");
        request.setCategoryIds(Collections.singletonList(1L));

        assertThrows(ServiceException.class, () -> service.createTool(request));
    }

    @Test
    void rejectsUnknownTaskCodesBeforeSaving()
    {
        TeacherToolSaveRequest request = new TeacherToolSaveRequest();
        request.setTitle("测试工具");
        request.setDescription("测试工具说明");
        request.setUrl("https://example.com/tool");
        request.setTaskTags("material,not-a-task");
        request.setCategoryIds(Collections.singletonList(1L));

        assertThrows(ServiceException.class, () -> service.createTool(request));
    }

    @Test
    void nullCategoryIdsDoNotThrowNullPointer()
    {
        TeacherToolSaveRequest request = new TeacherToolSaveRequest();
        request.setTitle("测试工具");
        request.setDescription("测试工具说明");
        request.setUrl("https://example.com/tool");
        request.setCategoryIds(null);
        BizTeacherTool tool = ReflectionTestUtils.invokeMethod(service, "toTool", request);

        assertEquals(Collections.emptyList(), tool.getCategoryIds());
    }

    @Test
    void rejectsInvalidStatusValuesInsteadOfNormalizing()
    {
        TeacherToolSaveRequest request = new TeacherToolSaveRequest();
        request.setTitle("测试工具");
        request.setDescription("测试工具说明");
        request.setUrl("https://example.com/tool");
        request.setStatus("9");
        request.setCategoryIds(Collections.singletonList(1L));

        assertThrows(ServiceException.class, () -> service.createTool(request));
        assertThrows(ServiceException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "requireStatus", "wrong"));
        assertEquals("1", ReflectionTestUtils.invokeMethod(service, "requireStatus", "1"));
        assertEquals("0", ReflectionTestUtils.invokeMethod(service, "requireStatus", (String) null));
        assertEquals("0", ReflectionTestUtils.invokeMethod(service, "requireStatus", "  "));
    }

    @Test
    void taskCodesFollowTaskTableInsteadOfHardcodedList()
    {
        TeacherToolTaskVo extra = new TeacherToolTaskVo();
        extra.setTaskCode("homework");
        extra.setTaskName("作业");
        // 保存校验用的是“未删除全部任务”，停用任务也在校验口径内，这里必须 mock 同一来源。
        when(mapper.selectAllTaskList()).thenReturn(Collections.singletonList(extra));

        TeacherToolSaveRequest request = new TeacherToolSaveRequest();
        request.setTitle("测试工具");
        request.setDescription("测试工具说明");
        request.setUrl("https://example.com/tool");
        request.setTaskTags("homework");
        request.setCategoryIds(Collections.singletonList(1L));
        BizTeacherTool tool = ReflectionTestUtils.invokeMethod(service, "toTool", request);

        assertEquals("homework", tool.getTaskTags());
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

    private TeacherToolTaskVo task(String code, String name)
    {
        TeacherToolTaskVo task = new TeacherToolTaskVo();
        task.setTaskCode(code);
        task.setTaskName(name);
        task.setStatus("0");
        task.setDelFlag("0");
        return task;
    }
}
