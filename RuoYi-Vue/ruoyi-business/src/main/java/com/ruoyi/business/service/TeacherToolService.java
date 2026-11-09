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
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.ruoyi.business.domain.vo.TeacherToolTaskVo;
import com.ruoyi.business.domain.vo.TeacherToolTermVo;
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
    private static final Logger log = LoggerFactory.getLogger(TeacherToolService.class);
    // 目录结构加入教学任务与检索别名后，缓存键升版避免读到旧结构。
    private static final String CATALOG_CACHE_KEY = "business:teacher-tools:catalog:v2";
    private static final int TERM_MAX_LENGTH = 50;
    private static final int TERMS_PER_TOOL_MAX = 20;
    private static final int ALIASES_MAX_LENGTH = 500;
    private static final int CATALOG_CACHE_MINUTES = 5;
    private static final Set<String> SECTION_LEVELS = new LinkedHashSet<>(Arrays.asList("PRIMARY", "SECONDARY"));
    private static final Set<String> ACCESS_TYPES = new LinkedHashSet<>(Arrays.asList("DIRECT", "LOGIN_REQUIRED", "INTRANET_ONLY", "DOWNLOAD"));
    private static final Set<String> SOURCE_TYPES = new LinkedHashSet<>(Arrays.asList("LOCAL_3005", "LOCAL_80", "ZJ_DISCIPLINE", "MANUAL"));
    // 任务编码兜底：正常以任务对照表为准；查不到（如事务中间态）才用这组兜底，避免脏编码污染 FIND_IN_SET。
    private static final Set<String> TASK_CODE_FALLBACK = new LinkedHashSet<>(Arrays.asList(
            "programming", "experiment", "resources", "material", "class-affairs"));

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
        // 目录的分类名汇总走 GROUP_CONCAT：先放大本会话上限，避免多分类工具被默认 1024 字节截断。
        mapper.increaseGroupConcatMaxLen();
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
        result.setTasks(mapper.selectCatalogTasks());
        result.setTerms(mapper.selectCatalogTerms());
        return result;
    }

    public List<TeacherToolTaskVo> listTasks()
    {
        return mapper.selectTaskList();
    }

    /**
     * 管理页只显示启用任务；保存校验用“未删除全部任务”，
     * 历史工具保留了已停用任务时仍能原样保存，不会被误判为未知编码。
     */
    public List<TeacherToolTaskVo> listAllTasksForValidation()
    {
        return mapper.selectAllTaskList();
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
        // 管理列表关键词会拼进 LIKE：先转义通配符，避免“_”“%”把整库都查出来。
        query.setKeyword(escapeLike(normalize(query.getKeyword())));
        query.setSourceType(normalize(query.getSourceType()));
        query.setStatus(normalize(query.getStatus()));
        query.setDelFlag("2".equals(query.getDelFlag()) ? "2" : "0");
        // 分组拼接默认上限只有 1024 字节：管理列表的分类名/任务名汇总在本会话内先放大，避免多分类工具被截断。
        mapper.increaseGroupConcatMaxLen();
        return mapper.selectToolList(query);
    }

    public BizTeacherTool getTool(Long toolId)
    {
        BizTeacherTool tool = requireTool(toolId);
        tool.setCategoryIds(mapper.selectCategoryIdsByToolId(toolId));
        // 详情回显以 aliases 列为准：管理列表关键词与搜索都走这一列，明细表只是同一数据的检索副本。
        // 列为空但明细表有时才用明细回填，避免两处口径分叉。
        if (StringUtils.isBlank(tool.getAliases()))
        {
            List<String> terms = new ArrayList<>();
            for (TeacherToolTermVo term : mapper.selectTermsByToolId(toolId))
            {
                terms.add(term.getTerm());
            }
            tool.setAliases(String.join("、", terms));
        }
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
        // 分类启停同样只接受显式 0/1；空值不再默认上架，避免误触切换可见性。
        mapper.updateCategoryStatus(categoryId, requireStrictStatus(status), SecurityUtils.getUsername());
        invalidateCatalog();
    }

    @Transactional
    public Long createTool(TeacherToolSaveRequest request)
    {
        BizTeacherTool tool = toTool(request);
        // 新增工具只接受启用分类；停用分类由编辑保留逻辑处理，这里直接拒绝。
        requireNonEmptyCategoryIds(tool.getCategoryIds(), "请至少选择一个分类");
        validateCategoriesForCreate(tool.getCategoryIds());
        tool.setCreateBy(SecurityUtils.getUsername());
        mapper.insertTool(tool);
        insertCategoryRelations(tool.getToolId(), tool.getCategoryIds());
        replaceTerms(tool.getToolId(), tool.getAliases());
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
        // 编辑允许保留已有归属里的停用分类，只拦截新勾选的停用分类，避免老工具无法保存。
        requireNonEmptyCategoryIds(tool.getCategoryIds(), "请至少选择一个分类");
        validateCategoriesForUpdate(toolId, tool.getCategoryIds());
        tool.setToolId(toolId);
        tool.setUpdateBy(SecurityUtils.getUsername());
        mapper.updateTool(tool);
        mapper.deleteToolCategoryRelations(toolId);
        insertCategoryRelations(toolId, tool.getCategoryIds());
        replaceTerms(toolId, tool.getAliases());
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
        // 上下架开关只接受显式 0/1；空值不再默认上架，避免误触切换工具可见性。
        mapper.updateToolStatus(toolId, requireStrictStatus(status), SecurityUtils.getUsername());
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
        category.setStatus(requireStatus(request.getStatus()));
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
        // 别名与任务段在保存时一次性规范化，检索配置只认这两处（管理页 + 任务对照表），不在代码里另立词表。
        tool.setAliases(normalizeTerms(request.getAliases()));
        tool.setTaskTags(normalizeTaskTags(request.getTaskTags()));
        tool.setAccessType(accessType);
        tool.setSourceType(sourceType);
        // 手工维护时来源标识可留空；使用 NULL 才能让唯一索引允许多条手工工具。
        tool.setSourceRef(StringUtils.isBlank(request.getSourceRef()) ? null : normalize(request.getSourceRef()));
        tool.setIsRecommended("Y".equalsIgnoreCase(request.getIsRecommended()) ? "Y" : "N");
        tool.setRecommendOrder(request.getRecommendOrder() == null ? 0 : request.getRecommendOrder());
        tool.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        tool.setStatus(requireStatus(request.getStatus()));
        // 管理页传入分类 id 列表；空列表会触发 @NotEmpty 校验，这里只做去空去重，避免旧数据直接 NPE。
        LinkedHashSet<Long> categoryIds = new LinkedHashSet<>(request.getCategoryIds() == null
                ? java.util.Collections.<Long>emptyList() : request.getCategoryIds());
        categoryIds.remove(null);
        tool.setCategoryIds(new ArrayList<>(categoryIds));
        return tool;
    }

    /** 别名全量替换：仅在新工具入库后或更新事务内调用，避免出现半套词条。 */
    private void replaceTerms(Long toolId, String aliases)
    {
        mapper.deleteTermsByToolId(toolId);
        List<String> terms = new ArrayList<>();
        for (String item : StringUtils.isEmpty(aliases) ? new String[0] : aliases.split("、"))
        {
            if (StringUtils.isNotBlank(item))
            {
                terms.add(item);
            }
        }
        if (!terms.isEmpty())
        {
            mapper.insertTerms(toolId, terms, SecurityUtils.getUsername());
        }
    }

    /** 别名：中英文逗号、顿号与分号都视为分隔符，去空、去重并限制数量与长度。 */
    private String normalizeTerms(String value)
    {
        // MySQL 默认校对不区分大小写：PPT 与 ppt 应视为同一别名，去重键统一小写，避免校验通过却撞唯一键 500。
        Map<String, String> termsByLower = new LinkedHashMap<>();
        for (String item : normalize(value).split("[,，、;；]"))
        {
            String term = item.trim();
            if (term.isEmpty())
            {
                continue;
            }
            if (term.length() > TERM_MAX_LENGTH)
            {
                throw new ServiceException("单个检索别名不能超过" + TERM_MAX_LENGTH + "个字符");
            }
            // 保留首次出现时的原文大小写，排序与展示更稳定。
            termsByLower.putIfAbsent(term.toLowerCase(), term);
        }
        if (termsByLower.size() > TERMS_PER_TOOL_MAX)
        {
            throw new ServiceException("单个工具最多设置" + TERMS_PER_TOOL_MAX + "个检索别名");
        }
        String joined = String.join("、", termsByLower.values());
        if (joined.length() > ALIASES_MAX_LENGTH)
        {
            throw new ServiceException("检索别名合计不能超过" + ALIASES_MAX_LENGTH + "个字符");
        }
        return joined;
    }

    /** 任务段按编码排序存储，便于 FIND_IN_SET 匹配和人工核对。 */
    private String normalizeTaskTags(String value)
    {
        // 任务编码以对照表为准；新增任务无需改代码发版。
        Set<String> allowed = allowedTaskCodes();
        Set<String> codes = new TreeSet<>();
        for (String item : normalize(value).split("[,，、;；]"))
        {
            String code = item.trim().toLowerCase();
            // 只接受对照表里的编码；未知编码直接拒绝，避免写脏数据后目录统计口径不一致。
            if (!code.isEmpty() && allowed.contains(code))
            {
                codes.add(code);
            }
            else if (!code.isEmpty())
            {
                throw new ServiceException("未知的教学任务编码：" + code);
            }
        }
        return String.join(",", codes);
    }

    /** 对照表为空或查询失败时用兜底编码，避免管理页因中间态写不进任务。 */
    private Set<String> allowedTaskCodes()
    {
        try
        {
            // 校验只要求“未删除”，已停用任务仍允许保留，不把历史归属误判成脏编码。
            List<TeacherToolTaskVo> tasks = mapper.selectAllTaskList();
            Set<String> codes = new LinkedHashSet<>();
            if (tasks != null)
            {
                for (TeacherToolTaskVo task : tasks)
                {
                    if (task != null && StringUtils.isNotBlank(task.getTaskCode()))
                    {
                        codes.add(task.getTaskCode().trim().toLowerCase());
                    }
                }
            }
            if (!codes.isEmpty())
            {
                return codes;
            }
        }
        catch (RuntimeException ex)
        {
            // 对照表查询失败时仍用兜底编码做校验，异常交给外层事务处理。
        }
        return TASK_CODE_FALLBACK;
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

    // 新增工具只允许挂启用分类：不存在、已删除与已停用统一拒绝，避免看不见的归属。
    private void validateCategoriesForCreate(List<Long> categoryIds)
    {
        if (categoryIds == null || categoryIds.isEmpty())
        {
            throw new ServiceException("请至少选择一个分类");
        }
        if (mapper.countCategoriesByIds(categoryIds) != categoryIds.size())
        {
            throw new ServiceException("所选分类不存在、已删除或已停用");
        }
    }

    // 编辑工具允许保留已有归属（停用或已删除都不拦），只拦截“新勾选”的停用或已删除分类，避免老工具无法保存。
    private void validateCategoriesForUpdate(Long toolId, List<Long> categoryIds)
    {
        if (categoryIds == null || categoryIds.isEmpty())
        {
            throw new ServiceException("请至少选择一个分类");
        }
        List<Long> existing = mapper.selectCategoryIdsByToolId(toolId);
        Set<Long> existingIds = new LinkedHashSet<>(existing == null
                ? java.util.Collections.<Long>emptyList() : existing);
        List<Long> newlyAdded = new ArrayList<>();
        for (Long categoryId : categoryIds)
        {
            // 已有归属视为保留：停用或已删除都不在这里拦截，只保证归属不断档。
            if (!existingIds.contains(categoryId))
            {
                newlyAdded.add(categoryId);
            }
        }
        if (newlyAdded.isEmpty())
        {
            return;
        }
        // 新勾选的分类必须存在且未删除；停用只提示移除，不阻断“原样保存”。
        if (mapper.countExistingCategoriesByIds(newlyAdded) != newlyAdded.size())
        {
            throw new ServiceException("新选择的分类不存在或已删除，请先移除");
        }
        if (mapper.countCategoriesByIds(newlyAdded) != newlyAdded.size())
        {
            throw new ServiceException("新选择的分类已停用，请先启用或移除");
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
        // 缓存只是数据库的派生数据：Redis 短暂不可用不能把已完成的业务写入回报成失败，
        // 否则教研发重试会重复新增。缓存未清时最多 5 分钟自然过期。
        try
        {
            redisCache.deleteObject(CATALOG_CACHE_KEY);
        }
        catch (RuntimeException ex)
        {
            log.warn("教师工具目录缓存失效失败，等待缓存自然过期：{}", CATALOG_CACHE_KEY, ex);
        }
    }

    /** 上下架状态只接受 0/1，未填按上架处理，其余非法值直接拒绝，避免误传被静默当作上架。 */
    private String requireStatus(String status)
    {
        String normalized = normalize(status);
        if (normalized.isEmpty())
        {
            return "0";
        }
        if ("0".equals(normalized) || "1".equals(normalized))
        {
            return normalized;
        }
        throw new ServiceException("状态无效");
    }

    /** 开关类状态只接受显式 0/1：上下架与分类启停都不再用空值默认上架，避免误触。 */
    private String requireStrictStatus(String status)
    {
        String normalized = normalize(status);
        if ("0".equals(normalized) || "1".equals(normalized))
        {
            return normalized;
        }
        throw new ServiceException("状态无效");
    }

    private String normalize(String value)
    {
        return value == null ? "" : value.trim();
    }

    /**
     * 管理列表偶发空分类 id 会拼出“IN ()”非法 SQL。
     * 这里做最后一道兜底：真正的空选择仍由上层非空校验拒绝，
     * 但绝不让非法 SQL 打到数据库。
     */
    private void requireNonEmptyCategoryIds(List<Long> categoryIds, String message)
    {
        if (categoryIds == null)
        {
            throw new ServiceException(message);
        }
        for (Long categoryId : categoryIds)
        {
            if (categoryId != null)
            {
                return;
            }
        }
        throw new ServiceException(message);
    }

    /** 管理列表关键词进 LIKE 前转义，避免用户输入的“%”“_”被当成通配符。 */
    private String escapeLike(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return value;
        }
        // 先转义反斜杠，再转义两个 LIKE 通配符；Mapper 侧用 ESCAPE '\\' 生效。
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /** 归属关系只在确实有分类 id 时才批量插入，避免拼出“VALUES”后无值的非法 SQL。 */
    private void insertCategoryRelations(Long toolId, List<Long> categoryIds)
    {
        List<Long> cleaned = new ArrayList<>();
        if (categoryIds != null)
        {
            for (Long categoryId : categoryIds)
            {
                if (categoryId != null)
                {
                    cleaned.add(categoryId);
                }
            }
        }
        if (cleaned.isEmpty())
        {
            return;
        }
        mapper.insertToolCategoryRelations(toolId, cleaned);
    }
}
