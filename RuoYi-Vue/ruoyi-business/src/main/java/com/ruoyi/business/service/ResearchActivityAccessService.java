package com.ruoyi.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.constant.ResearchActivityConstants;
import com.ruoyi.business.domain.BizResearchPost;
import com.ruoyi.business.domain.BizResearchResource;
import com.ruoyi.business.domain.BizResearchTopic;
import com.ruoyi.business.mapper.ResearchActivityMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 教研活动访问控制。
 * Controller 权限只负责挡住明显越权，作者和资源归属必须在服务层再次校验。
 */
@Service
public class ResearchActivityAccessService
{
    @Autowired
    private ResearchActivityMapper mapper;

    public void requireReadableRole()
    {
        if (!isReader())
        {
            throw new ServiceException("无权访问教研活动", 403);
        }
    }

    public void requireManager()
    {
        if (!isManager())
        {
            throw new ServiceException("仅教研员或管理员可以执行该操作", 403);
        }
    }

    public void requireTopicAuthor(BizResearchTopic topic)
    {
        if (topic == null) throw new ServiceException("主题不存在");
        if (!SecurityUtils.getUserId().equals(topic.getCreatorId()))
        {
            throw new ServiceException("只能修改本人发布的主题", 403);
        }
    }

    public void requirePostAuthor(BizResearchPost post)
    {
        if (post == null) throw new ServiceException("留言不存在");
        if (!SecurityUtils.getUserId().equals(post.getAuthorId()))
        {
            // 管理角色只能隐藏和恢复，不能篡改他人的正文或课后反思。
            throw new ServiceException("只能修改本人发布的内容", 403);
        }
    }

    public BizResearchTopic requireActiveTopic(Long topicId)
    {
        BizResearchTopic topic = mapper.selectTopicByIdAny(topicId);
        if (topic == null || !ResearchActivityConstants.DEL_NORMAL.equals(topic.getDelFlag()))
        {
            throw new ServiceException("内容已不存在");
        }
        return topic;
    }

    public BizResearchPost requireActivePost(Long postId)
    {
        BizResearchPost post = mapper.selectPostByIdAny(postId);
        if (post == null || !ResearchActivityConstants.DEL_NORMAL.equals(post.getDelFlag()))
        {
            throw new ServiceException("内容已不存在");
        }
        requireActiveTopic(post.getTopicId());
        return post;
    }

    public BizResearchResource requireActiveResource(Long resourceId)
    {
        BizResearchResource resource = mapper.selectResourceById(resourceId);
        if (resource == null)
        {
            throw new ServiceException("资源已不存在");
        }
        requireActivePost(resource.getPostId());
        return resource;
    }

    public boolean isManager()
    {
        Long userId = SecurityUtils.getUserId();
        return SecurityUtils.isAdmin(userId)
                || SecurityUtils.hasRole(ResearchActivityConstants.ROLE_ADMIN)
                || SecurityUtils.hasRole(ResearchActivityConstants.ROLE_RESEARCHER);
    }

    public boolean isReader()
    {
        Long userId = SecurityUtils.getUserId();
        return SecurityUtils.isAdmin(userId)
                || SecurityUtils.hasRole(ResearchActivityConstants.ROLE_ADMIN)
                || SecurityUtils.hasRole(ResearchActivityConstants.ROLE_TEACHER)
                || SecurityUtils.hasRole(ResearchActivityConstants.ROLE_RESEARCHER);
    }
}
