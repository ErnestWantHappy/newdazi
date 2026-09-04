package com.ruoyi.business.service;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Base64;
import java.util.Calendar;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.constant.ResearchActivityConstants;
import com.ruoyi.business.domain.BizResearchNoticeRecipient;
import com.ruoyi.business.domain.BizResearchPost;
import com.ruoyi.business.domain.BizResearchResource;
import com.ruoyi.business.domain.BizResearchTopic;
import com.ruoyi.business.domain.dto.ResearchNotificationSendRequest;
import com.ruoyi.business.domain.dto.ResearchPostSaveRequest;
import com.ruoyi.business.domain.dto.ResearchResourceLinkRequest;
import com.ruoyi.business.domain.dto.ResearchResourcePostSaveRequest;
import com.ruoyi.business.domain.dto.ResearchResourceQuery;
import com.ruoyi.business.domain.dto.ResearchTopicQuery;
import com.ruoyi.business.domain.dto.ResearchTopicSaveRequest;
import com.ruoyi.business.domain.vo.ResearchNotificationSummaryVo;
import com.ruoyi.business.domain.vo.ResearchNotificationVo;
import com.ruoyi.business.domain.vo.ResearchPostVo;
import com.ruoyi.business.domain.vo.ResearchPublicNoticeVo;
import com.ruoyi.business.domain.vo.ResearchPublicShareVo;
import com.ruoyi.business.domain.vo.ResearchResourceVo;
import com.ruoyi.business.domain.vo.ResearchTeacherOptionVo;
import com.ruoyi.business.domain.vo.ResearchTopicVo;
import com.ruoyi.business.mapper.ResearchActivityMapper;
import com.ruoyi.business.service.ResearchActivityHtmlSanitizer.SanitizedHtml;
import com.ruoyi.business.service.ResearchActivityUploadService.StoredFile;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/** 教研活动主题、留言、课程资源、搜索和通知的事务服务。 */
@Service
public class ResearchActivityService
{
    private static final Logger log = LoggerFactory.getLogger(ResearchActivityService.class);
    private static final SecureRandom PUBLIC_SHARE_RANDOM = new SecureRandom();
    private static final Pattern PUBLIC_SHARE_TOKEN = Pattern.compile("^[A-Za-z0-9_-]{43}$");

    @Autowired private ResearchActivityMapper mapper;
    @Autowired private ResearchActivityAccessService accessService;
    @Autowired private ResearchActivityHtmlSanitizer sanitizer;
    @Autowired private ResearchActivityUploadService uploadService;

    public List<ResearchTopicVo> listTopics(ResearchTopicQuery query)
    {
        accessService.requireReadableRole();
        prepareTopicQuery(query);
        List<ResearchTopicVo> list = mapper.selectTopicList(query);
        markTopicOwners(list);
        return list;
    }

    public ResearchTopicVo getTopic(Long topicId)
    {
        accessService.requireReadableRole();
        accessService.requireActiveTopic(topicId);
        try
        {
            mapper.incrementTopicView(topicId);
        }
        catch (RuntimeException e)
        {
            log.warn("教研活动主题浏览计数失败 topicId={}", topicId);
        }
        ResearchTopicVo topic = mapper.selectTopicById(topicId);
        if (topic == null) throw new ServiceException("内容已不存在");
        topic.setOwner(SecurityUtils.getUserId().equals(topic.getCreatorId()));
        return topic;
    }

    @Transactional
    public ResearchTopicVo createTopic(ResearchTopicSaveRequest request)
    {
        accessService.requireReadableRole();
        boolean manager = accessService.isManager();
        validateTopicRequest(request, manager);
        SanitizedHtml clean = sanitizer.sanitize(request.getContentHtml());
        Date now = new Date();
        BizResearchTopic topic = new BizResearchTopic();
        topic.setTopicType(request.getTopicType());
        topic.setTitle(request.getTitle().trim());
        topic.setContentHtml(clean.getHtml());
        topic.setContentText(clean.getText());
        topic.setNoticeLevel(request.getNoticeLevel());
        topic.setNoticeScope(request.getNoticeScope());
        topic.setNoticeStages(joinStages(request.getStageCodes()));
        topic.setActivityTime(request.getActivityTime());
        topic.setIsPinned(ResearchActivityConstants.NO);
        topic.setLastActivityTime(now);
        topic.setCreatorId(SecurityUtils.getUserId());
        topic.setDeptId(SecurityUtils.getDeptId());
        topic.setDelFlag(ResearchActivityConstants.DEL_NORMAL);
        topic.setCreateBy(SecurityUtils.getUsername());
        topic.setCreateTime(now);
        // 新建内容尚未被编辑，更新时间留空才能可靠地区分“刚创建”和“已编辑”。
        topic.setUpdateBy("");
        topic.setUpdateTime(null);
        mapper.insertTopic(topic);

        if (!ResearchActivityConstants.NOTICE_NONE.equals(request.getNoticeLevel()))
        {
            ResearchNotificationSendRequest notice = new ResearchNotificationSendRequest();
            notice.setNoticeLevel(request.getNoticeLevel());
            notice.setNoticeScope(request.getNoticeScope());
            notice.setStageCodes(request.getStageCodes());
            notice.setTeacherUserIds(request.getTeacherUserIds());
            sendNotificationInternal(topic.getTopicId(), notice);
        }
        return mapper.selectTopicById(topic.getTopicId());
    }

    @Transactional
    public ResearchTopicVo updateTopic(Long topicId, ResearchTopicSaveRequest request)
    {
        accessService.requireReadableRole();
        BizResearchTopic existing = accessService.requireActiveTopic(topicId);
        accessService.requireTopicAuthor(existing);
        if (!existing.getTopicType().equals(request.getTopicType()))
            throw new ServiceException("主题发布后不能修改类型");
        SanitizedHtml clean = sanitizer.sanitize(request.getContentHtml());
        existing.setTitle(request.getTitle().trim());
        existing.setContentHtml(clean.getHtml());
        existing.setContentText(clean.getText());
        if (ResearchActivityConstants.TOPIC_NOTICE.equals(existing.getTopicType()))
        {
            existing.setNoticeLevel(ResearchActivityConstants.NOTICE_NORMAL);
            if (request.getActivityTime() != null && !request.getActivityTime().equals(existing.getActivityTime()))
                validateFutureActivityTime(request.getActivityTime());
            existing.setActivityTime(request.getActivityTime());
        }
        existing.setUpdateBy(SecurityUtils.getUsername());
        existing.setUpdateTime(new Date());
        mapper.updateTopic(existing);
        // 编辑正文绝不自动重发通知；只能走显式 notify 接口。
        ResearchTopicVo result = mapper.selectTopicById(topicId);
        result.setOwner(Boolean.TRUE);
        return result;
    }

    @Transactional
    public void deleteTopic(Long topicId)
    {
        BizResearchTopic topic = accessService.requireActiveTopic(topicId);
        if (!SecurityUtils.getUserId().equals(topic.getCreatorId())) accessService.requireManager();
        mapper.updateTopicDeleteFlag(topicId, ResearchActivityConstants.DEL_DELETED, SecurityUtils.getUsername());
    }

    @Transactional
    public void restoreTopic(Long topicId)
    {
        accessService.requireManager();
        BizResearchTopic topic = mapper.selectTopicByIdAny(topicId);
        if (topic == null) throw new ServiceException("主题不存在");
        mapper.updateTopicDeleteFlag(topicId, ResearchActivityConstants.DEL_NORMAL, SecurityUtils.getUsername());
    }

    public List<ResearchTopicVo> listHiddenTopics()
    {
        accessService.requireManager();
        List<ResearchTopicVo> topics = mapper.selectHiddenTopicList();
        markTopicOwners(topics);
        return topics;
    }

    public void pinTopic(Long topicId, boolean pinned)
    {
        accessService.requireManager();
        accessService.requireActiveTopic(topicId);
        mapper.updateTopicPinned(topicId, pinned ? ResearchActivityConstants.YES : ResearchActivityConstants.NO,
                SecurityUtils.getUsername());
    }

    public List<ResearchPostVo> listPosts(Long topicId, String postType)
    {
        accessService.requireReadableRole();
        accessService.requireActiveTopic(topicId);
        if (StringUtils.isNotBlank(postType) && !isPostType(postType)) throw new ServiceException("留言类型不正确");
        List<ResearchPostVo> posts = mapper.selectPostList(topicId, postType);
        attachResources(posts);
        markPostOwners(posts);
        return posts;
    }

    @Transactional
    public ResearchPostVo createPost(Long topicId, ResearchPostSaveRequest request)
    {
        accessService.requireReadableRole();
        accessService.requireActiveTopic(topicId);
        if (!ResearchActivityConstants.POST_COMMENT.equals(request.getPostType())
                && !ResearchActivityConstants.POST_MOMENT.equals(request.getPostType()))
        {
            throw new ServiceException("课堂反思接口只支持课堂反思或活动纪实");
        }
        SanitizedHtml clean = sanitizer.sanitize(request.getContentHtml());
        BizResearchPost post = basePost(topicId, request.getPostType(), clean);
        mapper.insertPost(post);
        mapper.incrementTopicReply(topicId);
        ResearchPostVo result = mapper.selectPostById(post.getPostId());
        result.setOwner(Boolean.TRUE);
        return result;
    }

    @Transactional
    public ResearchPostVo updatePost(Long postId, ResearchPostSaveRequest request)
    {
        BizResearchPost post = accessService.requireActivePost(postId);
        accessService.requirePostAuthor(post);
        if (ResearchActivityConstants.POST_RESOURCE.equals(post.getPostType()))
            throw new ServiceException("课程资源请使用资源编辑接口");
        if (!post.getPostType().equals(request.getPostType())) throw new ServiceException("不能修改留言类型");
        SanitizedHtml clean = sanitizer.sanitize(request.getContentHtml());
        post.setContentHtml(clean.getHtml());
        post.setContentText(clean.getText());
        post.setUpdateBy(SecurityUtils.getUsername());
        post.setUpdateTime(new Date());
        mapper.updatePost(post);
        mapper.refreshTopicLastActivity(post.getTopicId());
        ResearchPostVo result = mapper.selectPostById(postId);
        result.setOwner(Boolean.TRUE);
        return result;
    }

    @Transactional
    public void deletePost(Long postId)
    {
        BizResearchPost post = accessService.requireActivePost(postId);
        if (!SecurityUtils.getUserId().equals(post.getAuthorId())) accessService.requireManager();
        mapper.updatePostDeleteFlag(postId, ResearchActivityConstants.DEL_DELETED, SecurityUtils.getUsername());
        // 资源通过父留言状态统一隐藏；不改资源自身 del_flag，避免恢复时把历史替换版本误激活。
        mapper.decrementTopicReply(post.getTopicId());
    }

    @Transactional
    public void restorePost(Long postId)
    {
        accessService.requireManager();
        BizResearchPost post = mapper.selectPostByIdAny(postId);
        if (post == null) throw new ServiceException("留言不存在");
        accessService.requireActiveTopic(post.getTopicId());
        if (ResearchActivityConstants.DEL_NORMAL.equals(post.getDelFlag())) return;
        mapper.updatePostDeleteFlag(postId, ResearchActivityConstants.DEL_NORMAL, SecurityUtils.getUsername());
        mapper.incrementTopicReply(post.getTopicId());
    }

    public List<ResearchPostVo> listHiddenPosts()
    {
        accessService.requireManager();
        List<ResearchPostVo> posts = mapper.selectHiddenPostList();
        attachResources(posts);
        markPostOwners(posts);
        return posts;
    }

    public void pinPost(Long postId, boolean pinned)
    {
        accessService.requireManager();
        BizResearchPost post = accessService.requireActivePost(postId);
        if (!ResearchActivityConstants.POST_RESOURCE.equals(post.getPostType()))
            throw new ServiceException("只有课程资源可以置顶");
        mapper.updatePostPinned(postId, pinned ? ResearchActivityConstants.YES : ResearchActivityConstants.NO,
                SecurityUtils.getUsername());
    }

    @Transactional
    public ResearchPostVo createResourcePost(Long topicId, ResearchResourcePostSaveRequest request, MultipartFile file)
    {
        accessService.requireReadableRole();
        accessService.requireActiveTopic(topicId);
        validateResourceRequest(request, file, false, false);
        SanitizedHtml clean = sanitizer.sanitize(request.getContentHtml());
        BizResearchPost post = basePost(topicId, ResearchActivityConstants.POST_RESOURCE, clean);
        applyCourseFields(post, request);
        mapper.insertPost(post);

        StoredFile stored = null;
        if (hasFile(file))
        {
            stored = uploadService.storePackage(file, topicId, post.getPostId());
            mapper.insertResource(fileResource(post.getPostId(), stored));
            registerFileLifecycle(stored.getRelativePath(), Collections.emptyList());
        }
        insertLinks(post.getPostId(), request.getLinks());
        mapper.incrementTopicReply(topicId);
        ResearchPostVo result = mapper.selectPostById(post.getPostId());
        result.setResources(withLinkStatus(mapper.selectResourcesByPostId(post.getPostId())));
        result.setOwner(Boolean.TRUE);
        return result;
    }

    @Transactional
    public ResearchPostVo updateResourcePost(Long postId, ResearchResourcePostSaveRequest request, MultipartFile file)
    {
        BizResearchPost post = mapper.selectPostForUpdate(postId);
        if (post == null || !ResearchActivityConstants.DEL_NORMAL.equals(post.getDelFlag()))
            throw new ServiceException("内容已不存在");
        accessService.requireActiveTopic(post.getTopicId());
        accessService.requirePostAuthor(post);
        if (!ResearchActivityConstants.POST_RESOURCE.equals(post.getPostType()))
            throw new ServiceException("该留言不是课程资源");

        List<ResearchResourceVo> existing = mapper.selectResourcesByPostId(postId);
        ResearchResourceVo existingFile = existing.stream()
                .filter(r -> ResearchActivityConstants.RESOURCE_FILE.equals(r.getResourceType())).findFirst().orElse(null);
        boolean keepExisting = ResearchActivityConstants.FILE_KEEP.equals(request.getFileAction()) && existingFile != null;
        validateResourceRequest(request, file, true, keepExisting);
        SanitizedHtml clean = sanitizer.sanitize(request.getContentHtml());
        post.setContentHtml(clean.getHtml());
        post.setContentText(clean.getText());
        applyCourseFields(post, request);
        post.setUpdateBy(SecurityUtils.getUsername());
        post.setUpdateTime(new Date());
        mapper.updatePost(post);

        mapper.softDeleteResourcesByPost(postId, ResearchActivityConstants.RESOURCE_LINK, SecurityUtils.getUsername());
        insertLinks(postId, request.getLinks());

        if (ResearchActivityConstants.FILE_REMOVE.equals(request.getFileAction())
                || ResearchActivityConstants.FILE_REPLACE.equals(request.getFileAction()))
        {
            mapper.softDeleteResourcesByPost(postId, ResearchActivityConstants.RESOURCE_FILE, SecurityUtils.getUsername());
        }
        if (ResearchActivityConstants.FILE_REPLACE.equals(request.getFileAction()))
        {
            StoredFile stored = uploadService.storePackage(file, post.getTopicId(), postId);
            mapper.insertResource(fileResource(postId, stored));
            List<String> oldPaths = existingFile == null ? Collections.emptyList()
                    : Collections.singletonList(existingFile.getStoredPath());
            registerFileLifecycle(stored.getRelativePath(), oldPaths);
        }
        else if (ResearchActivityConstants.FILE_REMOVE.equals(request.getFileAction()) && existingFile != null)
        {
            registerFileLifecycle(null, Collections.singletonList(existingFile.getStoredPath()));
        }
        mapper.refreshTopicLastActivity(post.getTopicId());
        ResearchPostVo result = mapper.selectPostById(postId);
        result.setResources(withLinkStatus(mapper.selectResourcesByPostId(postId)));
        result.setOwner(Boolean.TRUE);
        return result;
    }

    public List<ResearchPostVo> searchResources(ResearchResourceQuery query)
    {
        accessService.requireReadableRole();
        prepareResourceQuery(query);
        List<ResearchPostVo> list = mapper.selectResourcePostList(query);
        attachResources(list);
        markPostOwners(list);
        return list;
    }

    public List<ResearchTeacherOptionVo> listTeacherOptions(String keyword, String schoolType)
    {
        accessService.requireManager();
        if (StringUtils.isNotBlank(schoolType) && !isStage(schoolType)) throw new ServiceException("学段不正确");
        return mapper.selectTeacherOptions(like(keyword), schoolType);
    }

    @Transactional
    public int sendNotification(Long topicId, ResearchNotificationSendRequest request)
    {
        accessService.requireManager();
        accessService.requireActiveTopic(topicId);
        return sendNotificationInternal(topicId, request);
    }

    public ResearchNotificationSummaryVo notificationSummary(int limit)
    {
        accessService.requireReadableRole();
        int safeLimit = Math.max(1, Math.min(limit, 10));
        Long userId = SecurityUtils.getUserId();
        ResearchNotificationSummaryVo summary = new ResearchNotificationSummaryVo();
        summary.setUnreadCount(mapper.countUnreadNotifications(userId));
        summary.setItems(mapper.selectUnreadNotifications(userId, safeLimit));
        return summary;
    }

    public List<ResearchNotificationVo> listNotifications()
    {
        accessService.requireReadableRole();
        return mapper.selectNotificationList(SecurityUtils.getUserId());
    }

    public void markNotificationRead(Long recipientId)
    {
        accessService.requireReadableRole();
        if (mapper.markNotificationRead(recipientId, SecurityUtils.getUserId()) <= 0)
            throw new ServiceException("通知不存在或无权操作", 403);
    }

    public int markAllNotificationsRead()
    {
        accessService.requireReadableRole();
        return mapper.markAllNotificationsRead(SecurityUtils.getUserId());
    }

    public DownloadResource prepareDownload(Long resourceId)
    {
        accessService.requireReadableRole();
        BizResearchResource resource = accessService.requireActiveResource(resourceId);
        if (!ResearchActivityConstants.RESOURCE_FILE.equals(resource.getResourceType()))
            throw new ServiceException("该资源不是课件文件");
        Path path = uploadService.resolvePackagePath(resource.getStoredPath());
        if (!Files.isRegularFile(path)) throw new ServiceException("资源文件已不存在，请联系管理员");
        BizResearchPost post = accessService.requireActivePost(resource.getPostId());
        return new DownloadResource(resourceId, post.getTopicId(), path, resource.getOriginalFileName());
    }

    public void recordFileAccess(DownloadResource download)
    {
        try
        {
            mapper.incrementResourceAccess(download.getResourceId());
            mapper.incrementTopicDownload(download.getTopicId());
        }
        catch (RuntimeException e)
        {
            log.warn("教研活动文件下载计数失败 resourceId={}", download.getResourceId());
        }
    }

    public ResearchResourceVo accessLink(Long resourceId)
    {
        accessService.requireReadableRole();
        BizResearchResource resource = accessService.requireActiveResource(resourceId);
        if (!ResearchActivityConstants.RESOURCE_LINK.equals(resource.getResourceType()))
            throw new ServiceException("该资源不是云盘链接");
        BizResearchPost post = accessService.requireActivePost(resource.getPostId());
        try
        {
            mapper.incrementResourceAccess(resourceId);
            mapper.incrementTopicDownload(post.getTopicId());
        }
        catch (RuntimeException e)
        {
            log.warn("教研活动云盘访问计数失败 resourceId={}", resourceId);
        }
        ResearchResourceVo vo = mapper.selectResourceById(resourceId);
        applyLinkStatus(vo);
        return vo;
    }

    public String uploadImage(MultipartFile file)
    {
        accessService.requireReadableRole();
        return uploadService.storeImage(file);
    }

    @Transactional
    public ResearchPublicShareVo createPublicShare(Long topicId, Integer expireDays)
    {
        accessService.requireManager();
        requireNoticeTopic(topicId);
        Date expireTime = publicShareExpireTime(expireDays);
        byte[] bytes = new byte[32];
        PUBLIC_SHARE_RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        int affected = mapper.updatePublicShare(topicId, DigestUtils.sha256Hex(token), expireTime, SecurityUtils.getUsername());
        if (affected != 1) throw new ServiceException("通知不存在或无法生成分享链接");

        ResearchPublicShareVo result = publicShareStatus(topicId);
        // 明文令牌只在本次生成响应中短暂返回，数据库永不保存。
        result.setShareUrl(token);
        return result;
    }

    public ResearchPublicShareVo getPublicShareStatus(Long topicId)
    {
        accessService.requireManager();
        requireNoticeTopic(topicId);
        return publicShareStatus(topicId);
    }

    @Transactional
    public void revokePublicShare(Long topicId)
    {
        accessService.requireManager();
        requireNoticeTopic(topicId);
        if (mapper.revokePublicShare(topicId, SecurityUtils.getUsername()) != 1)
        {
            throw new ServiceException("通知不存在或无法撤销分享链接");
        }
    }

    public ResearchPublicNoticeVo getPublicNotice(String token)
    {
        return requirePublicNotice(token);
    }

    public Path getPublicNoticeImage(String token, String imageUrl)
    {
        String tokenHash = validateAndHashPublicToken(token);
        String contentHtml = mapper.selectPublicNoticeHtmlByTokenHash(tokenHash);
        if (StringUtils.isBlank(contentHtml) || !containsImageSource(contentHtml, imageUrl))
        {
            throw new ServiceException("通知图片不存在或已失效", 404);
        }
        return uploadService.resolvePublicImagePath(imageUrl);
    }

    private ResearchPublicNoticeVo requirePublicNotice(String token)
    {
        ResearchPublicNoticeVo notice = mapper.selectPublicNoticeByTokenHash(validateAndHashPublicToken(token));
        if (notice == null) throw new ServiceException("该通知不存在或已失效", 404);
        return notice;
    }

    private String validateAndHashPublicToken(String token)
    {
        if (token == null || !PUBLIC_SHARE_TOKEN.matcher(token).matches())
        {
            throw new ServiceException("该通知不存在或已失效", 404);
        }
        return DigestUtils.sha256Hex(token);
    }

    private void requireNoticeTopic(Long topicId)
    {
        BizResearchTopic topic = accessService.requireActiveTopic(topicId);
        if (!ResearchActivityConstants.TOPIC_NOTICE.equals(topic.getTopicType()))
        {
            throw new ServiceException("只有活动通知可以生成公开分享链接");
        }
    }

    private ResearchPublicShareVo publicShareStatus(Long topicId)
    {
        BizResearchTopic topic = accessService.requireActiveTopic(topicId);
        ResearchPublicShareVo result = new ResearchPublicShareVo();
        boolean active = ResearchActivityConstants.YES.equals(topic.getPublicShareEnabled())
                && StringUtils.isNotBlank(topic.getPublicShareTokenHash())
                && (topic.getPublicShareExpireTime() == null || topic.getPublicShareExpireTime().after(new Date()));
        result.setEnabled(active);
        result.setExpireTime(topic.getPublicShareExpireTime());
        return result;
    }

    private Date publicShareExpireTime(Integer expireDays)
    {
        int days = expireDays == null ? ResearchActivityConstants.PUBLIC_SHARE_DEFAULT_DAYS : expireDays;
        if (days == 0) return null;
        if (days != ResearchActivityConstants.PUBLIC_SHARE_SHORT_DAYS
                && days != ResearchActivityConstants.PUBLIC_SHARE_MAX_DAYS)
        {
            throw new ServiceException("分享有效期仅支持7天、30天或永久");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    private boolean containsImageSource(String contentHtml, String imageUrl)
    {
        if (StringUtils.isBlank(imageUrl)) return false;
        return Jsoup.parseBodyFragment(contentHtml).select("img[src]").stream()
                .anyMatch(image -> imageUrl.equals(image.attr("src")));
    }

    private int sendNotificationInternal(Long topicId, ResearchNotificationSendRequest request)
    {
        validateNotification(request);
        List<ResearchTeacherOptionVo> targets;
        String sourceType;
        if (ResearchActivityConstants.SCOPE_STAGE.equals(request.getNoticeScope()))
        {
            List<String> stages = distinctStages(request.getStageCodes());
            targets = mapper.selectTeachersByStages(stages);
            sourceType = ResearchActivityConstants.SOURCE_STAGE;
        }
        else
        {
            Set<Long> requested = request.getTeacherUserIds() == null ? Collections.emptySet()
                    : request.getTeacherUserIds().stream().filter(id -> id != null).collect(Collectors.toCollection(LinkedHashSet::new));
            targets = mapper.selectEnabledTeachersByIds(new ArrayList<>(requested));
            Set<Long> actual = targets.stream().map(ResearchTeacherOptionVo::getUserId).collect(Collectors.toSet());
            if (!actual.equals(requested)) throw new ServiceException("部分指定教师已停用或不具备教师角色，请重新选择");
            sourceType = ResearchActivityConstants.SOURCE_USER;
        }
        if (targets.isEmpty()) throw new ServiceException("当前通知范围没有可接收的启用教师账号");

        Date now = new Date();
        List<BizResearchNoticeRecipient> items = new ArrayList<>();
        for (ResearchTeacherOptionVo target : targets)
        {
            BizResearchNoticeRecipient item = new BizResearchNoticeRecipient();
            item.setTopicId(topicId);
            item.setUserId(target.getUserId());
            item.setSourceType(sourceType);
            item.setSourceValue(ResearchActivityConstants.SOURCE_STAGE.equals(sourceType) ? target.getSchoolType() : null);
            item.setNoticeLevel(ResearchActivityConstants.NOTICE_NORMAL);
            item.setReadFlag(ResearchActivityConstants.NO);
            item.setNotifyTime(now);
            item.setCreateBy(SecurityUtils.getUsername());
            item.setCreateTime(now);
            item.setUpdateBy(SecurityUtils.getUsername());
            item.setUpdateTime(now);
            items.add(item);
        }
        for (int start = 0; start < items.size(); start += 500)
        {
            mapper.upsertNoticeRecipients(items.subList(start, Math.min(start + 500, items.size())));
        }
        return items.size();
    }

    private void validateTopicRequest(ResearchTopicSaveRequest request, boolean manager)
    {
        if (!ResearchActivityConstants.TOPIC_NOTICE.equals(request.getTopicType())
                && !ResearchActivityConstants.TOPIC_SHARE.equals(request.getTopicType()))
            throw new ServiceException("主题类型不正确");
        if (!manager && !ResearchActivityConstants.TOPIC_SHARE.equals(request.getTopicType()))
            throw new ServiceException("教师只能发布交流分享", 403);
        if (ResearchActivityConstants.TOPIC_SHARE.equals(request.getTopicType()))
        {
            request.setNoticeLevel(ResearchActivityConstants.NOTICE_NONE);
            request.setNoticeScope(ResearchActivityConstants.SCOPE_NONE);
            request.setActivityTime(null);
            request.setStageCodes(Collections.emptyList());
            request.setTeacherUserIds(Collections.emptyList());
        }
        else
        {
            // 活动通知统一生成首页提醒，不再区分普通或重要级别。
            request.setNoticeLevel(ResearchActivityConstants.NOTICE_NORMAL);
            validateFutureActivityTime(request.getActivityTime());
            ResearchNotificationSendRequest notice = new ResearchNotificationSendRequest();
            notice.setNoticeLevel(request.getNoticeLevel());
            notice.setNoticeScope(request.getNoticeScope());
            notice.setStageCodes(request.getStageCodes());
            notice.setTeacherUserIds(request.getTeacherUserIds());
            validateNotification(notice);
        }
    }

    private void validateFutureActivityTime(Date activityTime)
    {
        if (activityTime != null && !activityTime.after(new Date()))
            throw new ServiceException("活动时间必须晚于当前时间");
    }

    private void validateNotification(ResearchNotificationSendRequest request)
    {
        request.setNoticeLevel(ResearchActivityConstants.NOTICE_NORMAL);
        if (ResearchActivityConstants.SCOPE_STAGE.equals(request.getNoticeScope()))
        {
            distinctStages(request.getStageCodes());
        }
        else if (ResearchActivityConstants.SCOPE_USER.equals(request.getNoticeScope()))
        {
            if (request.getTeacherUserIds() == null || request.getTeacherUserIds().stream().noneMatch(id -> id != null))
                throw new ServiceException("请选择通知教师");
        }
        else throw new ServiceException("请选择通知学段或教师");
    }

    private List<String> distinctStages(List<String> stages)
    {
        if (stages == null || stages.isEmpty()) throw new ServiceException("请选择通知学段");
        List<String> distinct = stages.stream().filter(StringUtils::isNotBlank)
                .distinct().collect(Collectors.toList());
        if (distinct.isEmpty() || distinct.stream().anyMatch(stage -> !isStage(stage)))
            throw new ServiceException("通知学段不正确");
        return distinct;
    }

    private void validateResourceRequest(ResearchResourcePostSaveRequest request, MultipartFile file,
                                         boolean editing, boolean keepExistingFile)
    {
        if (!isStage(request.getSchoolType())) throw new ServiceException("学段不正确");
        int grade = request.getGrade() == null ? 0 : request.getGrade();
        if (("1".equals(request.getSchoolType()) && (grade < 1 || grade > 6))
                || ("2".equals(request.getSchoolType()) && (grade < 7 || grade > 9))
                || ("3".equals(request.getSchoolType()) && (grade < 10 || grade > 12)))
            throw new ServiceException("年级与所选学段不一致");
        if (!"1".equals(request.getSemester()) && !"2".equals(request.getSemester()))
            throw new ServiceException("学期不正确");
        if (!ResearchActivityConstants.LESSON_NUMBER.equals(request.getLessonKind())
                && !ResearchActivityConstants.LESSON_SPECIAL.equals(request.getLessonKind())
                && !ResearchActivityConstants.LESSON_REVIEW.equals(request.getLessonKind()))
            throw new ServiceException("课次类型不正确");
        if (ResearchActivityConstants.LESSON_NUMBER.equals(request.getLessonKind()))
        {
            if (request.getLessonNo() == null || request.getLessonNo() <= 0) throw new ServiceException("请输入大于0的课次");
        }
        else request.setLessonNo(null);

        List<ResearchResourceLinkRequest> links = request.getLinks() == null ? Collections.emptyList() : request.getLinks();
        if (links.size() > ResearchActivityConstants.MAX_LINKS) throw new ServiceException("每条课程资源最多添加3个云盘链接");
        links.forEach(this::validateLink);

        boolean uploaded = hasFile(file);
        if (uploaded) uploadService.validatePackage(file);
        if (!editing)
        {
            if (!uploaded && links.isEmpty()) throw new ServiceException("请上传主课件或至少添加一个云盘链接");
            request.setFileAction(uploaded ? ResearchActivityConstants.FILE_REPLACE : ResearchActivityConstants.FILE_KEEP);
            return;
        }
        if (!ResearchActivityConstants.FILE_KEEP.equals(request.getFileAction())
                && !ResearchActivityConstants.FILE_REMOVE.equals(request.getFileAction())
                && !ResearchActivityConstants.FILE_REPLACE.equals(request.getFileAction()))
            throw new ServiceException("文件处理方式不正确");
        if (ResearchActivityConstants.FILE_REPLACE.equals(request.getFileAction()) && !uploaded)
            throw new ServiceException("请选择要替换的主课件");
        if (!ResearchActivityConstants.FILE_REPLACE.equals(request.getFileAction()) && uploaded)
            throw new ServiceException("上传新文件时请选择替换原文件");
        boolean hasFileAfter = keepExistingFile || ResearchActivityConstants.FILE_REPLACE.equals(request.getFileAction());
        if (!hasFileAfter && links.isEmpty()) throw new ServiceException("课程资源至少要有一个主课件或云盘链接");
    }

    private void validateLink(ResearchResourceLinkRequest link)
    {
        if (link == null || StringUtils.isBlank(link.getResourceName())) throw new ServiceException("请输入云盘资源名称");
        if (StringUtils.isBlank(link.getLinkUrl()) || !isHttpUrl(link.getLinkUrl().trim()))
            throw new ServiceException("仅支持 http/https 链接");
        link.setResourceName(link.getResourceName().trim());
        link.setLinkUrl(link.getLinkUrl().trim());
        if (Boolean.TRUE.equals(link.getPermanent())) link.setExpireTime(null);
        else if (link.getExpireTime() == null) throw new ServiceException("请选择资源过期时间");
        else if (!link.getExpireTime().after(new Date())) throw new ServiceException("过期时间必须晚于当前时间");
    }

    private boolean isHttpUrl(String value)
    {
        try
        {
            URI uri = URI.create(value);
            return uri.isAbsolute() && uri.getHost() != null
                    && ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()));
        }
        catch (IllegalArgumentException e) { return false; }
    }

    private BizResearchPost basePost(Long topicId, String postType, SanitizedHtml clean)
    {
        Date now = new Date();
        BizResearchPost post = new BizResearchPost();
        post.setTopicId(topicId);
        post.setPostType(postType);
        post.setContentHtml(clean.getHtml());
        post.setContentText(clean.getText());
        post.setIsPinned(ResearchActivityConstants.NO);
        post.setAuthorId(SecurityUtils.getUserId());
        post.setDeptId(SecurityUtils.getDeptId());
        post.setDelFlag(ResearchActivityConstants.DEL_NORMAL);
        post.setCreateBy(SecurityUtils.getUsername());
        post.setCreateTime(now);
        // MySQL 时间精度不足以用大小关系判断同一秒内的编辑，因此新建时不写更新时间。
        post.setUpdateBy("");
        post.setUpdateTime(null);
        return post;
    }

    private void applyCourseFields(BizResearchPost post, ResearchResourcePostSaveRequest request)
    {
        post.setSchoolType(request.getSchoolType());
        post.setGrade(request.getGrade());
        post.setSemester(request.getSemester());
        post.setLessonKind(request.getLessonKind());
        post.setLessonNo(request.getLessonNo());
        post.setCourseTitle(request.getCourseTitle().trim());
    }

    private BizResearchResource fileResource(Long postId, StoredFile stored)
    {
        Date now = new Date();
        BizResearchResource resource = new BizResearchResource();
        resource.setPostId(postId);
        resource.setResourceType(ResearchActivityConstants.RESOURCE_FILE);
        resource.setResourceName(stored.getOriginalFileName());
        resource.setOriginalFileName(stored.getOriginalFileName());
        resource.setStoredPath(stored.getRelativePath());
        resource.setFileSize(stored.getFileSize());
        resource.setMimeType(stored.getMimeType());
        resource.setSortOrder(0);
        resource.setDelFlag(ResearchActivityConstants.DEL_NORMAL);
        resource.setCreateBy(SecurityUtils.getUsername());
        resource.setCreateTime(now);
        resource.setUpdateBy(SecurityUtils.getUsername());
        resource.setUpdateTime(now);
        return resource;
    }

    private void insertLinks(Long postId, List<ResearchResourceLinkRequest> links)
    {
        if (links == null) return;
        int order = 1;
        for (ResearchResourceLinkRequest link : links)
        {
            Date now = new Date();
            BizResearchResource resource = new BizResearchResource();
            resource.setPostId(postId);
            resource.setResourceType(ResearchActivityConstants.RESOURCE_LINK);
            resource.setResourceName(link.getResourceName());
            resource.setLinkUrl(link.getLinkUrl());
            resource.setExtractCode(StringUtils.trim(link.getExtractCode()));
            resource.setExpireTime(link.getExpireTime());
            resource.setDescription(StringUtils.trim(link.getDescription()));
            resource.setSortOrder(order++);
            resource.setDelFlag(ResearchActivityConstants.DEL_NORMAL);
            resource.setCreateBy(SecurityUtils.getUsername());
            resource.setCreateTime(now);
            resource.setUpdateBy(SecurityUtils.getUsername());
            resource.setUpdateTime(now);
            mapper.insertResource(resource);
        }
    }

    private void attachResources(List<ResearchPostVo> posts)
    {
        if (posts == null || posts.isEmpty()) return;
        List<Long> postIds = posts.stream().map(ResearchPostVo::getPostId).collect(Collectors.toList());
        Map<Long, List<ResearchResourceVo>> grouped = withLinkStatus(mapper.selectResourcesByPostIds(postIds)).stream()
                .collect(Collectors.groupingBy(ResearchResourceVo::getPostId, LinkedHashMap::new, Collectors.toList()));
        posts.forEach(post -> post.setResources(grouped.getOrDefault(post.getPostId(), Collections.emptyList())));
    }

    private List<ResearchResourceVo> withLinkStatus(List<ResearchResourceVo> resources)
    {
        if (resources == null) return Collections.emptyList();
        resources.forEach(this::applyLinkStatus);
        return resources;
    }

    void applyLinkStatus(ResearchResourceVo resource)
    {
        if (resource == null || !ResearchActivityConstants.RESOURCE_LINK.equals(resource.getResourceType())) return;
        if (resource.getExpireTime() == null)
        {
            resource.setLinkStatus("PERMANENT");
            resource.setLinkStatusText("永久有效");
        }
        else if (resource.getExpireTime().after(new Date()))
        {
            resource.setLinkStatus("VALID");
            resource.setLinkStatusText("有效至" + DateUtils.parseDateToStr("yyyy-MM-dd HH:mm", resource.getExpireTime()));
        }
        else
        {
            resource.setLinkStatus("EXPIRED");
            resource.setLinkStatusText("已过期");
        }
    }

    private void registerFileLifecycle(String newPath, List<String> oldPaths)
    {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
        {
            @Override
            public void afterCommit()
            {
                oldPaths.forEach(uploadService::deleteQuietly);
            }

            @Override
            public void afterCompletion(int status)
            {
                if (status != TransactionSynchronization.STATUS_COMMITTED && StringUtils.isNotBlank(newPath))
                    uploadService.deleteQuietly(newPath);
            }
        });
    }

    private void prepareTopicQuery(ResearchTopicQuery query)
    {
        if (query == null) return;
        query.setKeyword(trimKeyword(query.getKeyword()));
        query.setKeywordLike(like(query.getKeyword()));
    }

    private void prepareResourceQuery(ResearchResourceQuery query)
    {
        if (query == null) return;
        query.setKeyword(trimKeyword(query.getKeyword()));
        if (StringUtils.isNotBlank(query.getKeyword()))
        {
            String escaped = escapeLike(query.getKeyword());
            query.setKeywordLike("%" + escaped + "%");
            query.setKeywordPrefix(escaped + "%");
        }
    }

    private String trimKeyword(String keyword)
    {
        if (StringUtils.isBlank(keyword)) return null;
        String value = keyword.trim();
        if (value.length() > 100) throw new ServiceException("搜索关键词不能超过100个字符");
        return value;
    }

    private String like(String keyword)
    {
        String value = trimKeyword(keyword);
        return value == null ? null : "%" + escapeLike(value) + "%";
    }

    private String escapeLike(String value)
    {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private String joinStages(List<String> stages)
    {
        if (stages == null || stages.isEmpty()) return null;
        return String.join(",", distinctStages(stages));
    }

    private boolean isStage(String value) { return "1".equals(value) || "2".equals(value) || "3".equals(value); }
    private boolean isPostType(String value)
    {
        return ResearchActivityConstants.POST_COMMENT.equals(value)
                || ResearchActivityConstants.POST_MOMENT.equals(value)
                || ResearchActivityConstants.POST_RESOURCE.equals(value);
    }
    private boolean hasFile(MultipartFile file) { return file != null && !file.isEmpty(); }

    private void markTopicOwners(List<ResearchTopicVo> list)
    {
        Long userId = SecurityUtils.getUserId();
        if (list != null) list.forEach(item -> item.setOwner(userId.equals(item.getCreatorId())));
    }
    private void markPostOwners(List<ResearchPostVo> list)
    {
        Long userId = SecurityUtils.getUserId();
        if (list != null) list.forEach(item -> item.setOwner(userId.equals(item.getAuthorId())));
    }

    public static class DownloadResource
    {
        private final Long resourceId;
        private final Long topicId;
        private final Path path;
        private final String fileName;

        public DownloadResource(Long resourceId, Long topicId, Path path, String fileName)
        {
            this.resourceId = resourceId;
            this.topicId = topicId;
            this.path = path;
            this.fileName = fileName;
        }
        public Long getResourceId() { return resourceId; }
        public Long getTopicId() { return topicId; }
        public Path getPath() { return path; }
        public String getFileName() { return fileName; }
    }
}
