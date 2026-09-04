package com.ruoyi.business.domain.dto;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 课程资源连续表单的 JSON payload。 */
public class ResearchResourcePostSaveRequest
{
    @NotBlank(message = "请选择学段")
    private String schoolType;
    @NotNull(message = "请选择年级")
    @Min(value = 1, message = "年级必须在1到12之间")
    @Max(value = 12, message = "年级必须在1到12之间")
    private Integer grade;
    @NotBlank(message = "请选择学期")
    private String semester;
    @NotBlank(message = "请选择课次类型")
    private String lessonKind;
    private Integer lessonNo;
    @NotBlank(message = "请输入课程标题")
    @Size(max = 200, message = "课程标题不能超过200个字符")
    private String courseTitle;
    @NotBlank(message = "请输入课后反思与资源说明")
    private String contentHtml;
    private String fileAction = "KEEP";
    @Valid
    @Size(max = 3, message = "每条课程资源最多添加3个云盘链接")
    private List<ResearchResourceLinkRequest> links = new ArrayList<>();

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
    public String getContentHtml() { return contentHtml; }
    public void setContentHtml(String contentHtml) { this.contentHtml = contentHtml; }
    public String getFileAction() { return fileAction; }
    public void setFileAction(String fileAction) { this.fileAction = fileAction; }
    public List<ResearchResourceLinkRequest> getLinks() { return links; }
    public void setLinks(List<ResearchResourceLinkRequest> links) { this.links = links; }
}
