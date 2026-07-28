package com.ruoyi.business.domain.dto;

import javax.validation.constraints.NotBlank;

/** 课堂反思或活动纪实请求。 */
public class ResearchPostSaveRequest
{
    @NotBlank(message = "请选择留言类型")
    private String postType;
    @NotBlank(message = "请输入留言内容")
    private String contentHtml;

    public String getPostType() { return postType; }
    public void setPostType(String postType) { this.postType = postType; }
    public String getContentHtml() { return contentHtml; }
    public void setContentHtml(String contentHtml) { this.contentHtml = contentHtml; }
}
