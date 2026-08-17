package com.ruoyi.business.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizTeacherTool;
import com.ruoyi.business.domain.BizTeacherToolCategory;
import com.ruoyi.business.domain.dto.TeacherToolCategorySaveRequest;
import com.ruoyi.business.domain.dto.TeacherToolQuery;
import com.ruoyi.business.domain.dto.TeacherToolSaveRequest;
import com.ruoyi.business.domain.vo.TeacherToolCatalogVo;
import com.ruoyi.business.domain.vo.TeacherToolCategoryVo;
import com.ruoyi.business.domain.vo.TeacherToolVo;
import com.ruoyi.business.mapper.TeacherToolMapper;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/** 教师工具目录与后台维护。 */
@Service
public class TeacherToolService
{
    private static final String CATALOG_CACHE_KEY = "business:teacher-tools:catalog:v1";
    private static final int CATALOG_CACHE_MINUTES = 5;
    private static final Set<String> SECTION_LEVELS = new LinkedHashSet<>(Arrays.asList("PRIMARY", "SECONDARY"));
    private static final Set<String> ACCESS_TYPES = new LinkedHashSet<>(Arrays.asList("DIRECT", "LOGIN_REQUIRED", "INTRANET_ONLY", "DOWNLOAD"));
    private static final Set<String> SOURCE_TYPES = new LinkedHashSet<>(Arrays.asList("LOCAL_3005", "LOCAL_80", "ZJ_DISCIPLINE", "MANUAL"));

    @Autowired
    private TeacherToolMapper mapper;

    @Autowired
    private RedisCache redisCache;

    private final Object catalogCacheLock = new Object();

    public TeacherToolCatalogVo getCatalog()
    {
        TeacherToolCatalogVo cached = redisCache.getCacheObject(CATALOG_CACHE_KEY);
        if (cached != null)
        {
            return cached;
        }
        // 同一实例只允许一个请求回源，避免上课瞬间缓存击穿再次占满数据库连接。
        synchronized (catalogCacheLock)
        {
            cached = redisCache.getCacheObject(CATALOG_CACHE_KEY);
            if (cached != null)
            {
                return cached;
            }
            TeacherToolCatalogVo result = loadCatalog();
            redisCache.setCacheObject(CATALOG_CACHE_KEY, result,
                    CATALOG_CACHE_MINUTES, TimeUnit.MINUTES);
            return result;
        }
    }

    private TeacherToolCatalogVo loadCatalog()
    {
        List<TeacherToolCategoryVo> categories = mapper.selectCatalogCategories();
        Map<Long, TeacherToolCategoryVo> categoryMap = new LinkedHashMap<>();
        for (TeacherToolCategoryVo category : categories)
        {
            category.setTools(new ArrayList<>());
            categoryMap.put(category.getCategoryId(), category);
        }
        for (TeacherToolVo tool : mapper.selectCatalogTools())
        {
            TeacherToolCategoryVo category = categoryMap.get(tool.getCategoryId());
            if (category != null)
            {
                category.getTools().add(tool);
            }
        }
        TeacherToolCatalogVo result = new TeacherToolCatalogVo();
        result.setCategories(categories.stream()
                .filter(category -> !category.getTools().isEmpty())
                .collect(Collectors.toList()));
        result.setRecommended(mapper.selectRecommendedTools());
        return result;
    }

    public List<BizTeacherToolCategory> listCategories()
    {
        return mapper.selectCategoryList();
    }

    public List<TeacherToolVo> listTools(TeacherToolQuery query)
    {
        if (query == null)
        {
            query = new TeacherToolQuery();
        }
        query.setKeyword(normalize(query.getKeyword()));
        query.setSourceType(normalize(query.getSourceType()));
        query.setStatus(normalize(query.getStatus()));
        query.setDelFlag("2".equals(query.getDelFlag()) ? "2" : "0");
        return mapper.selectToolList(query);
    }

    public BizTeacherTool getTool(Long toolId)
    {
        BizTeacherTool tool = requireTool(toolId);
        tool.setCategoryIds(mapper.selectCategoryIdsByToolId(toolId));
        return tool;
    }

    @Transactional
    public Long createCategory(TeacherToolCategorySaveRequest request)
    {
        BizTeacherToolCategory category = toCategory(request);
        if (mapper.countCategoryCode(category.getCategoryCode(), null) > 0)
        {
            throw new ServiceException("分类编码已存在");
        }
        category.setCreateBy(SecurityUtils.getUsername());
        mapper.insertCategory(category);
        invalidateCatalog();
        return category.getCategoryId();
    }

    @Transactional
    public Long updateCategory(Long categoryId, TeacherToolCategorySaveRequest request)
    {
        requireCategory(categoryId);
        BizTeacherToolCategory category = toCategory(request);
        category.setCategoryId(categoryId);
        if (mapper.countCategoryCode(category.getCategoryCode(), categoryId) > 0)
        {
            throw new ServiceException("分类编码已存在");
        }
        category.setUpdateBy(SecurityUtils.getUsername());
        mapper.updateCategory(category);
        invalidateCatalog();
        return categoryId;
    }

    public void updateCategoryStatus(Long categoryId, String status)
    {
        requireCategory(categoryId);
        mapper.updateCategoryStatus(categoryId, normalizeStatus(status), SecurityUtils.getUsername());
        invalidateCatalog();
    }

    @Transactional
    public Long createTool(TeacherToolSaveRequest request)
    {
        BizTeacherTool tool = toTool(request);
        validateCategories(tool.getCategoryIds());
        tool.setCreateBy(SecurityUtils.getUsername());
        mapper.insertTool(tool);
        mapper.insertToolCategoryRelations(tool.getToolId(), tool.getCategoryIds());
        invalidateCatalog();
        return tool.getToolId();
    }

    @Transactional
    public Long updateTool(Long toolId, TeacherToolSaveRequest request)
    {
        BizTeacherTool existing = requireTool(toolId);
        if ("2".equals(existing.getDelFlag()))
        {
            throw new ServiceException("工具已删除，请先恢复");
        }
        BizTeacherTool tool = toTool(request);
        validateCategories(tool.getCategoryIds());
        tool.setToolId(toolId);
        tool.setUpdateBy(SecurityUtils.getUsername());
        mapper.updateTool(tool);
        mapper.deleteToolCategoryRelations(toolId);
        mapper.insertToolCategoryRelations(toolId, tool.getCategoryIds());
        invalidateCatalog();
        return toolId;
    }

    public void updateToolStatus(Long toolId, String status)
    {
        BizTeacherTool tool = requireTool(toolId);
        if ("2".equals(tool.getDelFlag()))
        {
            throw new ServiceException("工具已删除，请先恢复");
        }
        mapper.updateToolStatus(toolId, normalizeStatus(status), SecurityUtils.getUsername());
        invalidateCatalog();
    }

    public void deleteTool(Long toolId)
    {
        requireTool(toolId);
        mapper.updateToolDeleteFlag(toolId, "2", SecurityUtils.getUsername());
        invalidateCatalog();
    }

    public void restoreTool(Long toolId)
    {
        requireTool(toolId);
        mapper.updateToolDeleteFlag(toolId, "0", SecurityUtils.getUsername());
        invalidateCatalog();
    }

    private BizTeacherToolCategory toCategory(TeacherToolCategorySaveRequest request)
    {
        String sectionLevel = normalize(request.getSectionLevel()).toUpperCase();
        if (!SECTION_LEVELS.contains(sectionLevel))
        {
            throw new ServiceException("分类层级无效");
        }
        BizTeacherToolCategory category = new BizTeacherToolCategory();
        category.setCategoryCode(normalize(request.getCategoryCode()).toLowerCase());
        category.setCategoryName(normalize(request.getCategoryName()));
        category.setDescription(normalize(request.getDescription()));
        category.setIcon(StringUtils.isBlank(request.getIcon()) ? "tool" : normalize(request.getIcon()));
        category.setSectionLevel(sectionLevel);
        category.setDefaultExpanded("Y".equalsIgnoreCase(request.getDefaultExpanded()) ? "Y" : "N");
        category.setPreviewLimit(request.getPreviewLimit() == null ? 4 : request.getPreviewLimit());
        category.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        category.setStatus(normalizeStatus(request.getStatus()));
        return category;
    }

    private BizTeacherTool toTool(TeacherToolSaveRequest request)
    {
        String accessType = normalize(request.getAccessType()).toUpperCase();
        String sourceType = normalize(request.getSourceType()).toUpperCase();
        if (StringUtils.isBlank(accessType)) accessType = "DIRECT";
        if (StringUtils.isBlank(sourceType)) sourceType = "MANUAL";
        if (!ACCESS_TYPES.contains(accessType)) throw new ServiceException("访问类型无效");
        if (!SOURCE_TYPES.contains(sourceType)) throw new ServiceException("来源类型无效");

        BizTeacherTool tool = new BizTeacherTool();
        tool.setTitle(normalize(request.getTitle()));
        tool.setDescription(normalize(request.getDescription()));
        tool.setUrl(validateHttpUrl(request.getUrl(), "工具地址"));
        tool.setIconUrl(StringUtils.isBlank(request.getIconUrl()) ? null : validateHttpUrl(request.getIconUrl(), "图标地址"));
        tool.setTags(normalize(request.getTags()));
        tool.setAccessType(accessType);
        tool.setSourceType(sourceType);
        // 手工维护时来源标识可留空；使用 NULL 才能让唯一索引允许多条手工工具。
        tool.setSourceRef(StringUtils.isBlank(request.getSourceRef()) ? null : normalize(request.getSourceRef()));
        tool.setIsRecommended("Y".equalsIgnoreCase(request.getIsRecommended()) ? "Y" : "N");
        tool.setRecommendOrder(request.getRecommendOrder() == null ? 0 : request.getRecommendOrder());
        tool.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        tool.setStatus(normalizeStatus(request.getStatus()));
        LinkedHashSet<Long> categoryIds = new LinkedHashSet<>(request.getCategoryIds());
        categoryIds.remove(null);
        tool.setCategoryIds(new ArrayList<>(categoryIds));
        return tool;
    }

    private String validateHttpUrl(String value, String label)
    {
        String normalized = normalize(value);
        try
        {
            URI uri = new URI(normalized);
            String scheme = uri.getScheme();
            if (scheme == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    || StringUtils.isBlank(uri.getHost()) || uri.getUserInfo() != null)
            {
                throw new ServiceException(label + "仅支持不含账号信息的HTTP或HTTPS地址");
            }
            return uri.toASCIIString();
        }
        catch (URISyntaxException ex)
        {
            throw new ServiceException(label + "格式无效");
        }
    }

    private void validateCategories(List<Long> categoryIds)
    {
        if (categoryIds == null || categoryIds.isEmpty())
        {
            throw new ServiceException("请至少选择一个分类");
        }
        if (mapper.countCategoriesByIds(categoryIds) != categoryIds.size())
        {
            throw new ServiceException("所选分类不存在或已删除");
        }
    }

    private BizTeacherToolCategory requireCategory(Long categoryId)
    {
        BizTeacherToolCategory category = mapper.selectCategoryById(categoryId);
        if (category == null) throw new ServiceException("分类不存在");
        return category;
    }

    private BizTeacherTool requireTool(Long toolId)
    {
        BizTeacherTool tool = mapper.selectToolById(toolId);
        if (tool == null) throw new ServiceException("工具不存在");
        return tool;
    }

    private void invalidateCatalog()
    {
        redisCache.deleteObject(CATALOG_CACHE_KEY);
    }

    private String normalizeStatus(String status)
    {
        return "1".equals(normalize(status)) ? "1" : "0";
    }

    private String normalize(String value)
    {
        return value == null ? "" : value.trim();
    }
}
