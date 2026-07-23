package com.ruoyi.business.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/** 教研活动留言；课程资源同样以一条结构化留言存在。 */
public class BizResearchPost extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long postId;
    private Long topicId;
    private String postType;
    private String contentHtml;
    private String contentText;
    private String schoolType;
    private Integer grade;
    private String semester;
    private String lessonKind;
    private Integer lessonNo;
    private String courseTitle;
    private String isPinned;
    private Long authorId;
    private Long deptId;
    private String delFlag;

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public String getPostType() { return postType; }
    public void setPostType(String postType) { this.postType = postType; }
    public String getContentHtml() { return contentHtml; }
    public void setContentHtml(String contentHtml) { this.contentHtml = contentHtml; }
    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public String getSchoolType() { return schoolType; }
    public void setSchoolType(String schoolType) { this.schoolType = schoolType; }
    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getLessonKind() { return lessonKind; }
    public void setLessonKind(String lessonKind) { this.lessonKind = lessonKind; }
    public Integer getLessonNo() { return lessonNo; }
    public void setLessonNo(Integer lessonNo) { this.lessonNo = lessonNo; }
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public String getIsPinned() { return isPinned; }
    public void setIsPinned(String isPinned) { this.isPinned = isPinned; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
