package com.ruoyi.business.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.constant.ResearchActivityConstants;
import com.ruoyi.business.domain.BizResearchPost;
import com.ruoyi.business.domain.BizResearchResource;
import com.ruoyi.business.domain.BizResearchTopic;
import com.ruoyi.business.domain.dto.ResearchNotificationSendRequest;
import com.ruoyi.business.domain.dto.ResearchResourceLinkRequest;
import com.ruoyi.business.domain.dto.ResearchResourcePostSaveRequest;
import com.ruoyi.business.domain.dto.ResearchResourceQuery;
import com.ruoyi.business.domain.dto.ResearchTopicSaveRequest;
import com.ruoyi.business.domain.vo.ResearchPostVo;
import com.ruoyi.business.domain.vo.ResearchResourceVo;
import com.ruoyi.business.domain.vo.ResearchTeacherOptionVo;
import com.ruoyi.business.domain.vo.ResearchTopicVo;
import com.ruoyi.business.mapper.ResearchActivityMapper;
import com.ruoyi.business.service.ResearchActivityHtmlSanitizer.SanitizedHtml;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResearchActivityServiceTest
{
    @Mock private ResearchActivityMapper mapper;
    @Mock private ResearchActivityAccessService accessService;
    @Mock private ResearchActivityHtmlSanitizer sanitizer;
    @Mock private ResearchActivityUploadService uploadService;
    @InjectMocks private ResearchActivityService service;

    @BeforeEach
    void loginTeacher()
    {
        login(20L, "teacher");
    }

    @AfterEach
    void clearSecurity()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void teacherCannotPublishNoticeTopic()
    {
        when(accessService.isManager()).thenReturn(false);
        ResearchTopicSaveRequest request = new ResearchTopicSaveRequest();
        request.setTopicType(ResearchActivityConstants.TOPIC_NOTICE);
        request.setTitle("活动");
        request.setContentHtml("<p>正文</p>");
        request.setNoticeLevel(ResearchActivityConstants.NOTICE_NORMAL);
        request.setNoticeScope(ResearchActivityConstants.SCOPE_STAGE);
        request.setStageCodes(Collections.singletonList("1"));

        ServiceException error = assertThrows(ServiceException.class, () -> service.createTopic(request));
        assertEquals(403, error.getCode());
        verify(mapper, never()).insertTopic(any());
    }

    @Test
    void noticeTopicAlwaysCreatesUnifiedHomeNotification()
    {
        when(accessService.isManager()).thenReturn(true);
        when(sanitizer.sanitize(anyString())).thenReturn(new SanitizedHtml("<p>正文</p>", "正文", 0));
        when(mapper.selectTeachersByStages(Collections.singletonList("1")))
                .thenReturn(Collections.singletonList(teacher(100L, "1")));
        doAnswer(invocation -> { ((BizResearchTopic) invocation.getArgument(0)).setTopicId(9L); return 1; })
                .when(mapper).insertTopic(any());
        when(mapper.selectTopicById(9L)).thenReturn(new ResearchTopicVo());

        ResearchTopicSaveRequest request = new ResearchTopicSaveRequest();
        request.setTopicType(ResearchActivityConstants.TOPIC_NOTICE);
        request.setTitle("活动");
        request.setContentHtml("<p>正文</p>");
        request.setNoticeScope(ResearchActivityConstants.SCOPE_STAGE);
        request.setStageCodes(Collections.singletonList("1"));

        service.createTopic(request);

        verify(mapper).insertTopic(argThat(topic -> "1".equals(topic.getNoticeLevel())
                && "".equals(topic.getUpdateBy()) && topic.getUpdateTime() == null));
        verify(mapper).upsertNoticeRecipients(argThat(items -> items.size() == 1
                && "1".equals(items.get(0).getNoticeLevel())));
    }

    @Test
    void noticeTopicSavesOptionalFutureActivityTime()
    {
        when(accessService.isManager()).thenReturn(true);
        when(sanitizer.sanitize(anyString())).thenReturn(new SanitizedHtml("<p>正文</p>", "正文", 0));
        when(mapper.selectTeachersByStages(Collections.singletonList("1")))
                .thenReturn(Collections.singletonList(teacher(100L, "1")));
        doAnswer(invocation -> { ((BizResearchTopic) invocation.getArgument(0)).setTopicId(9L); return 1; })
                .when(mapper).insertTopic(any());
        when(mapper.selectTopicById(9L)).thenReturn(new ResearchTopicVo());
        Date activityTime = new Date(System.currentTimeMillis() + 60_000);
        ResearchTopicSaveRequest request = new ResearchTopicSaveRequest();
        request.setTopicType(ResearchActivityConstants.TOPIC_NOTICE);
        request.setTitle("有时间的活动");
        request.setContentHtml("<p>正文</p>");
        request.setNoticeScope(ResearchActivityConstants.SCOPE_STAGE);
        request.setStageCodes(Collections.singletonList("1"));
        request.setActivityTime(activityTime);

        service.createTopic(request);

        verify(mapper).insertTopic(argThat(topic -> activityTime.equals(topic.getActivityTime())));
    }

    @Test
    void noticeTopicRejectsPastActivityTime()
    {
        when(accessService.isManager()).thenReturn(true);
        ResearchTopicSaveRequest request = new ResearchTopicSaveRequest();
        request.setTopicType(ResearchActivityConstants.TOPIC_NOTICE);
        request.setTitle("过期活动");
        request.setContentHtml("<p>正文</p>");
        request.setNoticeScope(ResearchActivityConstants.SCOPE_STAGE);
        request.setStageCodes(Collections.singletonList("1"));
        request.setActivityTime(new Date(System.currentTimeMillis() - 60_000));

        ServiceException error = assertThrows(ServiceException.class, () -> service.createTopic(request));

        assertEquals("活动时间必须晚于当前时间", error.getMessage());
        verify(mapper, never()).insertTopic(any());
    }

    @Test
    void validResourceAcceptsOneFileAndThreeLinks()
    {
        ResearchResourcePostSaveRequest request = validResource();
        request.setLinks(Arrays.asList(futureLink("一"), futureLink("二"), futureLink("三")));
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(sanitizer.sanitize(anyString())).thenReturn(new SanitizedHtml("<p>反思</p>", "反思", 0));
        when(uploadService.storePackage(eq(file), eq(9L), anyLong()))
                .thenReturn(new ResearchActivityUploadService.StoredFile("课件.zip", "9/1/a.zip", 100, "application/zip"));
        doAnswer(invocation -> { ((BizResearchPost) invocation.getArgument(0)).setPostId(30L); return 1; })
                .when(mapper).insertPost(any());
        when(mapper.selectPostById(30L)).thenReturn(new ResearchPostVo());
        when(mapper.selectResourcesByPostId(30L)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> service.createResourcePost(9L, request, file));
        verify(mapper).insertPost(argThat(post -> "".equals(post.getUpdateBy()) && post.getUpdateTime() == null));
        verify(mapper, times(4)).insertResource(any());
    }

    @Test
    void rejectsFourthLinkAndMismatchedStageGrade()
    {
        ResearchResourcePostSaveRequest fourLinks = validResource();
        fourLinks.setLinks(Arrays.asList(futureLink("一"), futureLink("二"), futureLink("三"), futureLink("四")));
        assertThrows(ServiceException.class, () -> service.createResourcePost(9L, fourLinks, null));

        ResearchResourcePostSaveRequest wrongGrade = validResource();
        wrongGrade.setGrade(8);
        wrongGrade.setLinks(Collections.singletonList(futureLink("一")));
        assertThrows(ServiceException.class, () -> service.createResourcePost(9L, wrongGrade, null));
    }

    @Test
    void numericLessonNeedsNumberButSpecialAndReviewClearIt()
    {
        ResearchResourcePostSaveRequest numeric = validResource();
        numeric.setLessonNo(null);
        numeric.setLinks(Collections.singletonList(futureLink("一")));
        assertThrows(ServiceException.class, () -> service.createResourcePost(9L, numeric, null));

        ResearchResourcePostSaveRequest special = validResource();
        special.setLessonKind(ResearchActivityConstants.LESSON_SPECIAL);
        special.setLessonNo(8);
        special.setLinks(Collections.singletonList(futureLink("一")));
        when(sanitizer.sanitize(anyString())).thenReturn(new SanitizedHtml("<p>反思</p>", "反思", 0));
        doAnswer(invocation -> { ((BizResearchPost) invocation.getArgument(0)).setPostId(31L); return 1; })
                .when(mapper).insertPost(any());
        when(mapper.selectPostById(31L)).thenReturn(new ResearchPostVo());
        when(mapper.selectResourcesByPostId(31L)).thenReturn(Collections.emptyList());
        service.createResourcePost(9L, special, null);
        assertNull(special.getLessonNo());
    }

    @Test
    void rejectsPastExpiryAndSupportsPermanentFutureAndExpiredDisplay()
    {
        ResearchResourcePostSaveRequest request = validResource();
        ResearchResourceLinkRequest past = futureLink("过去");
        past.setPermanent(false);
        past.setExpireTime(new Date(System.currentTimeMillis() - 1000));
        request.setLinks(Collections.singletonList(past));
        assertThrows(ServiceException.class, () -> service.createResourcePost(9L, request, null));

        ResearchResourceVo permanent = linkVo(null);
        service.applyLinkStatus(permanent);
        assertEquals("PERMANENT", permanent.getLinkStatus());
        ResearchResourceVo future = linkVo(new Date(System.currentTimeMillis() + 60_000));
        service.applyLinkStatus(future);
        assertEquals("VALID", future.getLinkStatus());
        ResearchResourceVo expired = linkVo(new Date(System.currentTimeMillis() - 60_000));
        service.applyLinkStatus(expired);
        assertEquals("EXPIRED", expired.getLinkStatus());
    }

    @Test
    void existingFileCannotBeSilentlyReplacedAsSecondFile()
    {
        ResearchResourcePostSaveRequest request = validResource();
        request.setFileAction(ResearchActivityConstants.FILE_KEEP);
        request.setLinks(Collections.emptyList());
        MultipartFile newFile = mock(MultipartFile.class);
        when(newFile.isEmpty()).thenReturn(false);
        BizResearchPost post = new BizResearchPost();
        post.setPostId(30L);
        post.setTopicId(9L);
        post.setPostType(ResearchActivityConstants.POST_RESOURCE);
        post.setAuthorId(20L);
        post.setDelFlag(ResearchActivityConstants.DEL_NORMAL);
        when(mapper.selectPostForUpdate(30L)).thenReturn(post);
        ResearchResourceVo existing = new ResearchResourceVo();
        existing.setResourceType(ResearchActivityConstants.RESOURCE_FILE);
        when(mapper.selectResourcesByPostId(30L)).thenReturn(Collections.singletonList(existing));

        assertThrows(ServiceException.class, () -> service.updateResourcePost(30L, request, newFile));
        verify(uploadService, never()).storePackage(any(), anyLong(), anyLong());
    }

    @Test
    void stageNotificationKeepsMultipleTeacherAccounts()
    {
        ResearchTeacherOptionVo first = teacher(100L, "1");
        ResearchTeacherOptionVo second = teacher(101L, "2");
        when(mapper.selectTeachersByStages(Arrays.asList("1", "2"))).thenReturn(Arrays.asList(first, second));
        ResearchNotificationSendRequest request = notificationByStages();

        assertEquals(2, service.sendNotification(9L, request));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<java.util.List<com.ruoyi.business.domain.BizResearchNoticeRecipient>> captor =
                ArgumentCaptor.forClass(java.util.List.class);
        verify(mapper).upsertNoticeRecipients(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    void specifiedTeachersDeduplicateAndRejectDisabledAccount()
    {
        ResearchNotificationSendRequest request = new ResearchNotificationSendRequest();
        request.setNoticeLevel("1");
        request.setNoticeScope("2");
        request.setTeacherUserIds(Arrays.asList(100L, 100L));
        when(mapper.selectEnabledTeachersByIds(Collections.singletonList(100L)))
                .thenReturn(Collections.singletonList(teacher(100L, "1")));
        assertEquals(1, service.sendNotification(9L, request));

        request.setTeacherUserIds(Arrays.asList(100L, 101L));
        when(mapper.selectEnabledTeachersByIds(Arrays.asList(100L, 101L)))
                .thenReturn(Collections.singletonList(teacher(100L, "1")));
        assertThrows(ServiceException.class, () -> service.sendNotification(9L, request));
    }

    @Test
    void editingTopicDoesNotResendNotification()
    {
        BizResearchTopic topic = new BizResearchTopic();
        topic.setTopicId(9L);
        topic.setCreatorId(20L);
        topic.setDelFlag(ResearchActivityConstants.DEL_NORMAL);
        topic.setTopicType("NOTICE");
        topic.setNoticeLevel("2");
        topic.setNoticeScope("2");
        when(accessService.requireActiveTopic(9L)).thenReturn(topic);
        when(sanitizer.sanitize(anyString())).thenReturn(new SanitizedHtml("<p>改后</p>", "改后", 0));
        when(mapper.selectTopicById(9L)).thenReturn(new ResearchTopicVo());
        ResearchTopicSaveRequest request = new ResearchTopicSaveRequest();
        request.setTopicType("NOTICE");
        request.setTitle("更新");
        request.setContentHtml("<p>更新</p>");
        request.setNoticeLevel("2");
        request.setNoticeScope("2");
        request.setTeacherUserIds(Collections.emptyList());

        service.updateTopic(9L, request);
        verify(mapper, never()).upsertNoticeRecipients(anyList());
        verify(mapper).updateTopic(argThat(updated -> "1".equals(updated.getNoticeLevel())
                && "2".equals(updated.getNoticeScope())));
    }

    @Test
    void publishedTopicTypeCannotChangeDuringEdit()
    {
        BizResearchTopic topic = new BizResearchTopic();
        topic.setTopicId(9L);
        topic.setCreatorId(20L);
        topic.setDelFlag("0");
        topic.setTopicType("SHARE");
        when(accessService.requireActiveTopic(9L)).thenReturn(topic);
        ResearchTopicSaveRequest request = new ResearchTopicSaveRequest();
        request.setTopicType("NOTICE");
        request.setTitle("不能改类型");
        request.setContentHtml("<p>正文</p>");
        assertThrows(ServiceException.class, () -> service.updateTopic(9L, request));
        verify(mapper, never()).updateTopic(any());
    }

    @Test
    void resendNotificationMaterializesUnreadSnapshotAndBatchesAccounts()
    {
        when(mapper.selectTeachersByStages(Collections.singletonList("1")))
                .thenReturn(Arrays.asList(teacher(100L, "1"), teacher(101L, "1")));
        ResearchNotificationSendRequest request = new ResearchNotificationSendRequest();
        request.setNoticeLevel("2");
        request.setNoticeScope("1");
        request.setStageCodes(Collections.singletonList("1"));

        assertEquals(2, service.sendNotification(9L, request));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<java.util.List<com.ruoyi.business.domain.BizResearchNoticeRecipient>> captor =
                ArgumentCaptor.forClass(java.util.List.class);
        verify(mapper).upsertNoticeRecipients(captor.capture());
        assertTrue(captor.getValue().stream().allMatch(item -> "N".equals(item.getReadFlag())));
        assertTrue(captor.getValue().stream().allMatch(item -> "1".equals(item.getNoticeLevel())));
        assertTrue(captor.getValue().stream().allMatch(item -> item.getNotifyTime() != null));
        assertEquals(2, captor.getValue().stream().map(item -> item.getUserId()).distinct().count());
    }

    @Test
    void notificationReadOnlyTouchesCurrentAccountAndIsRepeatable()
    {
        when(mapper.markNotificationRead(88L, 20L)).thenReturn(1);
        assertDoesNotThrow(() -> service.markNotificationRead(88L));
        assertDoesNotThrow(() -> service.markNotificationRead(88L));
        verify(mapper, times(2)).markNotificationRead(88L, 20L);

        when(mapper.markNotificationRead(99L, 20L)).thenReturn(0);
        assertEquals(403, assertThrows(ServiceException.class,
                () -> service.markNotificationRead(99L)).getCode());
        when(mapper.markAllNotificationsRead(20L)).thenReturn(3);
        assertEquals(3, service.markAllNotificationsRead());
    }

    @Test
    void deletingAndRestoringPostMaintainsVisibleReplyCount()
    {
        BizResearchPost post = new BizResearchPost();
        post.setPostId(30L);
        post.setTopicId(9L);
        post.setAuthorId(20L);
        post.setDelFlag("0");
        when(accessService.requireActivePost(30L)).thenReturn(post);

        service.deletePost(30L);
        verify(mapper).updatePostDeleteFlag(30L, "2", "teacher");
        verify(mapper).decrementTopicReply(9L);

        post.setDelFlag("2");
        when(mapper.selectPostByIdAny(30L)).thenReturn(post);
        service.restorePost(30L);
        verify(mapper).updatePostDeleteFlag(30L, "0", "teacher");
        verify(mapper).incrementTopicReply(9L);
    }

    @Test
    void resourceSearchEscapesWildcardsAndBatchesResourceRows()
    {
        ResearchResourceQuery query = new ResearchResourceQuery();
        query.setKeyword(" 课%_! ");
        ResearchPostVo post = new ResearchPostVo();
        post.setPostId(30L);
        when(mapper.selectResourcePostList(query)).thenReturn(Collections.singletonList(post));
        when(mapper.selectResourcesByPostIds(Collections.singletonList(30L))).thenReturn(Collections.emptyList());

        assertEquals(1, service.searchResources(query).size());
        assertEquals("课%_!", query.getKeyword());
        assertEquals("%课!%!_!!%", query.getKeywordLike());
        assertEquals("课!%!_!!%", query.getKeywordPrefix());
        verify(mapper).selectResourcesByPostIds(Collections.singletonList(30L));
        verify(mapper, never()).selectResourcesByPostId(anyLong());
    }

    private ResearchResourcePostSaveRequest validResource()
    {
        ResearchResourcePostSaveRequest request = new ResearchResourcePostSaveRequest();
        request.setSchoolType("1");
        request.setGrade(3);
        request.setSemester("1");
        request.setLessonKind("N");
        request.setLessonNo(5);
        request.setCourseTitle("在线学习真方便");
        request.setContentHtml("<p>课后反思</p>");
        return request;
    }

    private ResearchResourceLinkRequest futureLink(String name)
    {
        ResearchResourceLinkRequest link = new ResearchResourceLinkRequest();
        link.setResourceName(name);
        link.setLinkUrl("https://example.com/" + name);
        link.setPermanent(true);
        return link;
    }

    private ResearchResourceVo linkVo(Date expire)
    {
        ResearchResourceVo resource = new ResearchResourceVo();
        resource.setResourceType(ResearchActivityConstants.RESOURCE_LINK);
        resource.setExpireTime(expire);
        return resource;
    }

    private ResearchTeacherOptionVo teacher(Long id, String schoolType)
    {
        ResearchTeacherOptionVo teacher = new ResearchTeacherOptionVo();
        teacher.setUserId(id);
        teacher.setSchoolType(schoolType);
        return teacher;
    }

    private ResearchNotificationSendRequest notificationByStages()
    {
        ResearchNotificationSendRequest request = new ResearchNotificationSendRequest();
        request.setNoticeLevel("2");
        request.setNoticeScope("1");
        request.setStageCodes(Arrays.asList("1", "2"));
        return request;
    }

    private void login(Long userId, String roleKey)
    {
        SysRole role = new SysRole();
        role.setRoleKey(roleKey);
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setDeptId(10L);
        user.setUserName("teacher");
        user.setRoles(Collections.singletonList(role));
        LoginUser loginUser = new LoginUser(userId, 10L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }
}
