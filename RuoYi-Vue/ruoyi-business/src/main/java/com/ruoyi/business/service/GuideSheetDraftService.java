package com.ruoyi.business.service;

import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.BizGuideSheetDraft;
import com.ruoyi.business.domain.dto.GuideSheetDraftSaveRequest;
import com.ruoyi.business.domain.vo.GuideSheetDraftVo;
import com.ruoyi.business.mapper.GuideSheetDraftMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理教师端创作草稿，使用客户端草稿键和版本号消除重复保存与并发覆盖。
 */
@Service
public class GuideSheetDraftService
{
    private static final String STATUS_DRAFT = "D";
    private static final String STATUS_COMPLETED = "C";
    private static final int MAX_CONTENT_LENGTH = 5 * 1024 * 1024;
    private static final Pattern DRAFT_KEY_PATTERN =
            Pattern.compile("[A-Za-z0-9][A-Za-z0-9._:-]{0,63}");

    private final GuideSheetDraftMapper draftMapper;
    private final ObjectMapper objectMapper;

    public GuideSheetDraftService(GuideSheetDraftMapper draftMapper, ObjectMapper objectMapper)
    {
        this.draftMapper = draftMapper;
        this.objectMapper = objectMapper;
    }

    public GuideSheetDraftVo restore(Long ownerId, String draftKey)
    {
        validateOwnerAndKey(ownerId, draftKey);
        BizGuideSheetDraft draft = draftMapper.selectByOwnerAndKey(ownerId, draftKey);
        if (draft == null || !STATUS_DRAFT.equals(draft.getDraftStatus()))
        {
            return null;
        }
        return toVo(draft);
    }

    @Transactional(rollbackFor = Exception.class)
    public GuideSheetDraftVo save(Long ownerId, String username, GuideSheetDraftSaveRequest request)
    {
        if (request == null)
        {
            throw new ServiceException("草稿内容不能为空");
        }
        validateOwnerAndKey(ownerId, request.getDraftKey());
        String contentJson = serializeContent(request.getContent());
        BizGuideSheetDraft existing = draftMapper.selectByOwnerAndKey(ownerId, request.getDraftKey());
        if (existing == null)
        {
            return createDraft(ownerId, username, request, contentJson);
        }
        if (!STATUS_DRAFT.equals(existing.getDraftStatus()))
        {
            return reopenCompletedDraft(ownerId, username, request, contentJson, existing);
        }
        if (samePayload(existing, request.getSheetId(), request.getContent()))
        {
            return toVo(existing);
        }
        long expectedRevision = normalizeRevision(request.getRevision());
        if (!Long.valueOf(expectedRevision).equals(existing.getRevision()))
        {
            throw new ServiceException("草稿已在其他页面更新，请刷新后重试");
        }

        Date now = new Date();
        int updated = draftMapper.updateDraftCas(ownerId, request.getDraftKey(), expectedRevision,
                request.getSheetId(), contentJson, username, now);
        if (updated != 1)
        {
            throw new ServiceException("草稿已在其他页面更新，请刷新后重试");
        }
        existing.setSheetId(request.getSheetId());
        existing.setContentJson(contentJson);
        existing.setRevision(expectedRevision + 1);
        existing.setUpdateBy(username);
        existing.setUpdateTime(now);
        return toVo(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public GuideSheetDraftVo complete(Long ownerId, String username, String draftKey, Long revision)
    {
        validateOwnerAndKey(ownerId, draftKey);
        BizGuideSheetDraft existing = draftMapper.selectByOwnerAndKey(ownerId, draftKey);
        if (existing == null)
        {
            throw new ServiceException("草稿不存在或已清理");
        }
        if (STATUS_COMPLETED.equals(existing.getDraftStatus()))
        {
            return toVo(existing);
        }
        long expectedRevision = normalizeRevision(revision);
        if (!Long.valueOf(expectedRevision).equals(existing.getRevision()))
        {
            throw new ServiceException("草稿已在其他页面更新，请刷新后重试");
        }
        Date now = new Date();
        if (draftMapper.completeDraftCas(ownerId, draftKey, expectedRevision, username, now) != 1)
        {
            throw new ServiceException("草稿已在其他页面更新，请刷新后重试");
        }
        existing.setDraftStatus(STATUS_COMPLETED);
        existing.setRevision(expectedRevision + 1);
        existing.setCompletedTime(now);
        existing.setUpdateBy(username);
        existing.setUpdateTime(now);
        return toVo(existing);
    }

    private GuideSheetDraftVo createDraft(Long ownerId, String username,
                                          GuideSheetDraftSaveRequest request, String contentJson)
    {
        if (normalizeRevision(request.getRevision()) != 0L)
        {
            throw new ServiceException("草稿不存在或已清理，请刷新后重试");
        }
        Date now = new Date();
        BizGuideSheetDraft draft = new BizGuideSheetDraft();
        draft.setOwnerId(ownerId);
        draft.setClientDraftKey(request.getDraftKey());
        draft.setSheetId(request.getSheetId());
        draft.setContentJson(contentJson);
        draft.setRevision(1L);
        draft.setDraftStatus(STATUS_DRAFT);
        draft.setCreateBy(username);
        draft.setCreateTime(now);
        draft.setUpdateBy(username);
        draft.setUpdateTime(now);
        try
        {
            draftMapper.insertDraft(draft);
            return toVo(draft);
        }
        catch (DuplicateKeyException e)
        {
            // 同一个浏览器请求并发到达时，唯一键负责收敛为一份草稿。
            BizGuideSheetDraft concurrent = draftMapper.selectByOwnerAndKey(ownerId, request.getDraftKey());
            if (concurrent != null && samePayload(concurrent, request.getSheetId(), request.getContent()))
            {
                return toVo(concurrent);
            }
            throw new ServiceException("草稿已在其他页面创建，请刷新后重试");
        }
    }

    private GuideSheetDraftVo reopenCompletedDraft(Long ownerId, String username,
                                                   GuideSheetDraftSaveRequest request,
                                                   String contentJson,
                                                   BizGuideSheetDraft existing)
    {
        if (!STATUS_COMPLETED.equals(existing.getDraftStatus())
                || normalizeRevision(request.getRevision()) != 0L)
        {
            throw new ServiceException("该草稿已完成，请刷新后重新编辑");
        }
        Date now = new Date();
        int updated = draftMapper.reopenCompletedDraft(ownerId, request.getDraftKey(),
                request.getSheetId(), contentJson, username, now);
        if (updated != 1)
        {
            BizGuideSheetDraft concurrent = draftMapper.selectByOwnerAndKey(ownerId, request.getDraftKey());
            if (concurrent != null && STATUS_DRAFT.equals(concurrent.getDraftStatus())
                    && samePayload(concurrent, request.getSheetId(), request.getContent()))
            {
                return toVo(concurrent);
            }
            throw new ServiceException("草稿已在其他页面更新，请刷新后重试");
        }
        existing.setSheetId(request.getSheetId());
        existing.setContentJson(contentJson);
        existing.setRevision(existing.getRevision() + 1);
        existing.setDraftStatus(STATUS_DRAFT);
        existing.setCompletedTime(null);
        existing.setUpdateBy(username);
        existing.setUpdateTime(now);
        return toVo(existing);
    }

    private boolean samePayload(BizGuideSheetDraft existing, Long sheetId, JsonNode content)
    {
        if (!Objects.equals(existing.getSheetId(), sheetId))
        {
            return false;
        }
        try
        {
            return objectMapper.readTree(existing.getContentJson()).equals(normalizeContent(content));
        }
        catch (Exception e)
        {
            throw new ServiceException("草稿内容已损坏，请联系管理员处理");
        }
    }

    private String serializeContent(JsonNode content)
    {
        try
        {
            JsonNode normalized = normalizeContent(content);
            String value = objectMapper.writeValueAsString(normalized);
            if (value.getBytes(StandardCharsets.UTF_8).length > MAX_CONTENT_LENGTH)
            {
                throw new ServiceException("草稿内容过大，请精简后重试");
            }
            return value;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("草稿内容格式无效");
        }
    }

    private GuideSheetDraftVo toVo(BizGuideSheetDraft draft)
    {
        GuideSheetDraftVo vo = new GuideSheetDraftVo();
        vo.setDraftKey(draft.getClientDraftKey());
        vo.setSheetId(draft.getSheetId());
        vo.setRevision(draft.getRevision());
        vo.setStatus(draft.getDraftStatus());
        vo.setUpdateTime(draft.getUpdateTime());
        try
        {
            JsonNode content = objectMapper.readTree(draft.getContentJson());
            if (content == null || !content.isObject())
            {
                throw new ServiceException("草稿内容已损坏，请联系管理员处理");
            }
            vo.setContent(objectMapper.writeValueAsString(content));
            return vo;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("草稿内容已损坏，请联系管理员处理");
        }
    }

    private JsonNode normalizeContent(JsonNode content) throws Exception
    {
        if (content == null || content.isNull())
        {
            throw new ServiceException("草稿内容格式无效");
        }
        JsonNode normalized = content.isTextual() ? objectMapper.readTree(content.asText()) : content;
        if (normalized == null || !normalized.isObject())
        {
            throw new ServiceException("草稿内容格式无效");
        }
        return normalized;
    }

    private void validateOwnerAndKey(Long ownerId, String draftKey)
    {
        if (ownerId == null)
        {
            throw new ServiceException("未识别当前教师");
        }
        if (StringUtils.isBlank(draftKey) || !DRAFT_KEY_PATTERN.matcher(draftKey).matches())
        {
            throw new ServiceException("草稿标识格式无效");
        }
    }

    private long normalizeRevision(Long revision)
    {
        return revision == null ? 0L : revision;
    }
}
