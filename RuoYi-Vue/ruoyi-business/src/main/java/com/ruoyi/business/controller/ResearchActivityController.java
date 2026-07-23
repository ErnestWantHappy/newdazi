package com.ruoyi.business.controller;

import java.util.List;
import javax.validation.Valid;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.constant.ResearchActivityConstants;
import com.ruoyi.business.domain.dto.ResearchNotificationSendRequest;
import com.ruoyi.business.domain.dto.ResearchPostSaveRequest;
import com.ruoyi.business.domain.dto.ResearchResourcePostSaveRequest;
import com.ruoyi.business.domain.dto.ResearchResourceQuery;
import com.ruoyi.business.domain.dto.ResearchTopicQuery;
import com.ruoyi.business.domain.dto.ResearchTopicSaveRequest;
import com.ruoyi.business.domain.vo.ResearchPostVo;
import com.ruoyi.business.domain.vo.ResearchTeacherOptionVo;
import com.ruoyi.business.domain.vo.ResearchTopicVo;
import com.ruoyi.business.service.ResearchActivityService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.enums.BusinessType;

/** 教研活动主题、留言、搜索和通知接口。 */
@RestController
@RequestMapping("/business/research-activity")
public class ResearchActivityController extends BaseController
{
    private static final String ROLE_GUARD = "(@ss.hasRole('teacher') or @ss.hasRole('researcher') or @ss.hasRole('admin'))";

    @Autowired private ResearchActivityService service;

    @GetMapping("/topics")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:list') and " + ROLE_GUARD)
    public TableDataInfo topics(ResearchTopicQuery query)
    {
        safeStartPage();
        List<ResearchTopicVo> list = service.listTopics(query);
        return getDataTable(list);
    }

    @GetMapping("/search/topics")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:list') and " + ROLE_GUARD)
    public TableDataInfo searchTopics(ResearchTopicQuery query)
    {
        return topics(query);
    }

    @GetMapping("/topics/{topicId}")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:list') and " + ROLE_GUARD)
    public AjaxResult topic(@PathVariable Long topicId)
    {
        return success(service.getTopic(topicId));
    }

    @PostMapping("/topics")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:add') and " + ROLE_GUARD)
    @Log(title="教研活动主题", businessType=BusinessType.INSERT, isSaveRequestData=false, isSaveResponseData=false)
    public AjaxResult createTopic(@Valid @RequestBody ResearchTopicSaveRequest request)
    {
        return success(service.createTopic(request));
    }

    @PutMapping("/topics/{topicId}")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:edit') and " + ROLE_GUARD)
    @Log(title="教研活动主题", businessType=BusinessType.UPDATE, isSaveRequestData=false, isSaveResponseData=false)
    public AjaxResult updateTopic(@PathVariable Long topicId, @Valid @RequestBody ResearchTopicSaveRequest request)
    {
        return success(service.updateTopic(topicId, request));
    }

    @DeleteMapping("/topics/{topicId}")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:remove') and " + ROLE_GUARD)
    @Log(title="教研活动主题", businessType=BusinessType.DELETE)
    public AjaxResult deleteTopic(@PathVariable Long topicId)
    {
        service.deleteTopic(topicId);
        return success();
    }

    @PutMapping("/topics/{topicId}/restore")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:manage') and (@ss.hasRole('researcher') or @ss.hasRole('admin'))")
    public AjaxResult restoreTopic(@PathVariable Long topicId)
    {
        service.restoreTopic(topicId);
        return success();
    }

    @GetMapping("/hidden/topics")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:manage') and (@ss.hasRole('researcher') or @ss.hasRole('admin'))")
    public TableDataInfo hiddenTopics()
    {
        safeStartPage();
        return getDataTable(service.listHiddenTopics());
    }

    @PutMapping("/topics/{topicId}/pin")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:pin') and (@ss.hasRole('researcher') or @ss.hasRole('admin'))")
    public AjaxResult pinTopic(@PathVariable Long topicId, @RequestParam boolean pinned)
    {
        service.pinTopic(topicId, pinned);
        return success();
    }

    @GetMapping("/topics/{topicId}/posts")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:list') and " + ROLE_GUARD)
    public TableDataInfo posts(@PathVariable Long topicId, @RequestParam(required=false) String postType)
    {
        safeStartPage();
        return getDataTable(service.listPosts(topicId, postType));
    }

    @PostMapping("/topics/{topicId}/posts")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:add') and " + ROLE_GUARD)
    @Log(title="教研活动留言", businessType=BusinessType.INSERT, isSaveRequestData=false, isSaveResponseData=false)
    public AjaxResult createPost(@PathVariable Long topicId, @Valid @RequestBody ResearchPostSaveRequest request)
    {
        return success(service.createPost(topicId, request));
    }

    @PutMapping("/posts/{postId}")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:edit') and " + ROLE_GUARD)
    @Log(title="教研活动留言", businessType=BusinessType.UPDATE, isSaveRequestData=false, isSaveResponseData=false)
    public AjaxResult updatePost(@PathVariable Long postId, @Valid @RequestBody ResearchPostSaveRequest request)
    {
        return success(service.updatePost(postId, request));
    }

    @PostMapping(value="/topics/{topicId}/resource-posts", consumes="multipart/form-data")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:add') and " + ROLE_GUARD)
    @Log(title="教研活动课程资源", businessType=BusinessType.INSERT, isSaveRequestData=false, isSaveResponseData=false)
    public AjaxResult createResourcePost(@PathVariable Long topicId,
            @Valid @RequestPart("payload") ResearchResourcePostSaveRequest payload,
            @RequestPart(value="file", required=false) MultipartFile file)
    {
        return success(service.createResourcePost(topicId, payload, file));
    }

    @PutMapping(value="/resource-posts/{postId}", consumes="multipart/form-data")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:edit') and " + ROLE_GUARD)
    @Log(title="教研活动课程资源", businessType=BusinessType.UPDATE, isSaveRequestData=false, isSaveResponseData=false)
    public AjaxResult updateResourcePost(@PathVariable Long postId,
            @Valid @RequestPart("payload") ResearchResourcePostSaveRequest payload,
            @RequestPart(value="file", required=false) MultipartFile file)
    {
        return success(service.updateResourcePost(postId, payload, file));
    }

    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:remove') and " + ROLE_GUARD)
    public AjaxResult deletePost(@PathVariable Long postId)
    {
        service.deletePost(postId);
        return success();
    }

    @PutMapping("/posts/{postId}/restore")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:manage') and (@ss.hasRole('researcher') or @ss.hasRole('admin'))")
    public AjaxResult restorePost(@PathVariable Long postId)
    {
        service.restorePost(postId);
        return success();
    }

    @GetMapping("/hidden/posts")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:manage') and (@ss.hasRole('researcher') or @ss.hasRole('admin'))")
    public TableDataInfo hiddenPosts()
    {
        safeStartPage();
        return getDataTable(service.listHiddenPosts());
    }

    @PutMapping("/posts/{postId}/pin")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:pin') and (@ss.hasRole('researcher') or @ss.hasRole('admin'))")
    public AjaxResult pinPost(@PathVariable Long postId, @RequestParam boolean pinned)
    {
        service.pinPost(postId, pinned);
        return success();
    }

    @GetMapping("/search/resources")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:list') and " + ROLE_GUARD)
    public TableDataInfo searchResources(ResearchResourceQuery query)
    {
        safeStartPage();
        List<ResearchPostVo> list = service.searchResources(query);
        return getDataTable(list);
    }

    @GetMapping("/notification-targets/teachers")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:notify') and (@ss.hasRole('researcher') or @ss.hasRole('admin'))")
    public TableDataInfo teacherTargets(@RequestParam(required=false) String keyword,
                                        @RequestParam(required=false) String schoolType)
    {
        safeStartPage();
        List<ResearchTeacherOptionVo> list = service.listTeacherOptions(keyword, schoolType);
        return getDataTable(list);
    }

    @PostMapping("/topics/{topicId}/notify")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:notify') and (@ss.hasRole('researcher') or @ss.hasRole('admin'))")
    @Log(title="教研活动再次通知", businessType=BusinessType.OTHER, isSaveRequestData=false, isSaveResponseData=false)
    public AjaxResult notifyTopic(@PathVariable Long topicId, @Valid @RequestBody ResearchNotificationSendRequest request)
    {
        return success(service.sendNotification(topicId, request));
    }

    @GetMapping("/notifications/summary")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:list') and " + ROLE_GUARD)
    public AjaxResult notificationSummary(@RequestParam(defaultValue="5") int limit)
    {
        return success(service.notificationSummary(limit));
    }

    @GetMapping("/notifications")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:list') and " + ROLE_GUARD)
    public TableDataInfo notifications()
    {
        safeStartPage();
        return getDataTable(service.listNotifications());
    }

    @PutMapping("/notifications/{recipientId}/read")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:list') and " + ROLE_GUARD)
    public AjaxResult readNotification(@PathVariable Long recipientId)
    {
        service.markNotificationRead(recipientId);
        return success();
    }

    @PutMapping("/notifications/read-all")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:list') and " + ROLE_GUARD)
    public AjaxResult readAllNotifications()
    {
        return success(service.markAllNotificationsRead());
    }

    private void safeStartPage()
    {
        PageDomain page = TableSupport.buildPageRequest();
        int pageNum = page.getPageNum() == null ? 1 : Math.max(1, page.getPageNum());
        int requested = page.getPageSize() == null ? ResearchActivityConstants.DEFAULT_PAGE_SIZE : page.getPageSize();
        int pageSize = Math.max(1, Math.min(requested, ResearchActivityConstants.MAX_PAGE_SIZE));
        PageHelper.startPage(pageNum, pageSize);
    }
}
