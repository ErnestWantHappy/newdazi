package com.ruoyi.business.domain.dto;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 三类留言共用附件表单；课程字段由服务端按类型校验。 */
public class ResearchAttachmentPostRequest
{
    @NotBlank private String postType;
    @NotBlank private String contentHtml;
    @NotNull @Size(max = 10) private List<Long> retainedResourceIds = new ArrayList<>();
    private ResearchResourcePostSaveRequest course;
    public String getPostType() { return postType; }
    public void setPostType(String value) { postType = value; }
    public String getContentHtml() { return contentHtml; }
    public void setContentHtml(String value) { contentHtml = value; }
    public List<Long> getRetainedResourceIds() { return retainedResourceIds; }
    public void setRetainedResourceIds(List<Long> value) { retainedResourceIds = value; }
    public ResearchResourcePostSaveRequest getCourse() { return course; }
    public void setCourse(ResearchResourcePostSaveRequest value) { course = value; }
}
