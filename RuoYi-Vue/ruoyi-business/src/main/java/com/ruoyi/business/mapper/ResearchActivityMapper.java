package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizResearchNoticeRecipient;
import com.ruoyi.business.domain.BizResearchPost;
import com.ruoyi.business.domain.BizResearchResource;
import com.ruoyi.business.domain.BizResearchTopic;
import com.ruoyi.business.domain.dto.ResearchResourceQuery;
import com.ruoyi.business.domain.dto.ResearchTopicQuery;
import com.ruoyi.business.domain.vo.ResearchNotificationVo;
import com.ruoyi.business.domain.vo.ResearchPostVo;
import com.ruoyi.business.domain.vo.ResearchResourceVo;
import com.ruoyi.business.domain.vo.ResearchTeacherOptionVo;
import com.ruoyi.business.domain.vo.ResearchTopicVo;

/** 教研活动主题、留言、资源和通知的数据访问。 */
public interface ResearchActivityMapper
{
    int insertTopic(BizResearchTopic topic);
    int updateTopic(BizResearchTopic topic);
    ResearchTopicVo selectTopicById(Long topicId);
    BizResearchTopic selectTopicByIdAny(Long topicId);
    BizResearchTopic selectTopicForUpdate(Long topicId);
    List<ResearchTopicVo> selectTopicList(ResearchTopicQuery query);
    List<ResearchTopicVo> selectHiddenTopicList();
    int updateTopicDeleteFlag(@Param("topicId") Long topicId, @Param("delFlag") String delFlag,
                              @Param("updateBy") String updateBy);
    int updateTopicPinned(@Param("topicId") Long topicId, @Param("isPinned") String isPinned,
                          @Param("updateBy") String updateBy);
    int incrementTopicView(Long topicId);
    int incrementTopicReply(Long topicId);
    int decrementTopicReply(Long topicId);
    int refreshTopicLastActivity(Long topicId);
    int incrementTopicDownload(Long topicId);

    int insertPost(BizResearchPost post);
    int updatePost(BizResearchPost post);
    ResearchPostVo selectPostById(Long postId);
    BizResearchPost selectPostByIdAny(Long postId);
    BizResearchPost selectPostForUpdate(Long postId);
    List<ResearchPostVo> selectPostList(@Param("topicId") Long topicId, @Param("postType") String postType);
    List<ResearchPostVo> selectHiddenPostList();
    List<ResearchPostVo> selectResourcePostList(ResearchResourceQuery query);
    int updatePostDeleteFlag(@Param("postId") Long postId, @Param("delFlag") String delFlag,
                             @Param("updateBy") String updateBy);
    int updatePostPinned(@Param("postId") Long postId, @Param("isPinned") String isPinned,
                         @Param("updateBy") String updateBy);

    int insertResource(BizResearchResource resource);
    ResearchResourceVo selectResourceById(Long resourceId);
    List<ResearchResourceVo> selectResourcesByPostId(Long postId);
    List<ResearchResourceVo> selectResourcesByPostIds(@Param("postIds") List<Long> postIds);
    int softDeleteResourcesByPost(@Param("postId") Long postId, @Param("resourceType") String resourceType,
                                  @Param("updateBy") String updateBy);
    int incrementResourceAccess(Long resourceId);

    List<ResearchTeacherOptionVo> selectTeacherOptions(@Param("keywordLike") String keywordLike,
                                                        @Param("schoolType") String schoolType);
    List<ResearchTeacherOptionVo> selectTeachersByStages(@Param("stageCodes") List<String> stageCodes);
    List<ResearchTeacherOptionVo> selectEnabledTeachersByIds(@Param("userIds") List<Long> userIds);
    int upsertNoticeRecipients(@Param("items") List<BizResearchNoticeRecipient> items);
    long countUnreadNotifications(Long userId);
    List<ResearchNotificationVo> selectUnreadNotifications(@Param("userId") Long userId,
                                                            @Param("limit") int limit);
    List<ResearchNotificationVo> selectNotificationList(Long userId);
    int markNotificationRead(@Param("recipientId") Long recipientId, @Param("userId") Long userId);
    int markAllNotificationsRead(Long userId);
}
