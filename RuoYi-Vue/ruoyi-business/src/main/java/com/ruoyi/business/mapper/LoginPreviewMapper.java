package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;

/** 登录展厅只查询摘要列，不复用包含正文、成绩和资源地址的内部对象。 */
public interface LoginPreviewMapper
{
    List<Map<String, Object>> selectActivities();
    List<Map<String, Object>> selectLessons();
    String selectTopicContentHtml(Long topicId);
    List<Map<String, Object>> selectLatestPostImages();
    String selectPostContentHtml(Long postId);
}

