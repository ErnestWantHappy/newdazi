package com.ruoyi.business.domain.dto;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/** 课程资源结构化检索条件。 */
public class ResearchResourceQuery
{
    private String keyword;
    private String keywordLike;
    private String keywordPrefix;
    private String schoolType;
    private Integer grade;
    private String semester;
    private String lessonKind;
    private Integer lessonNo;
    private Long authorId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getKeywordLike() { return keywordLike; }
    public void setKeywordLike(String keywordLike) { this.keywordLike = keywordLike; }
    public String getKeywordPrefix() { return keywordPrefix; }
    public void setKeywordPrefix(String keywordPrefix) { this.keywordPrefix = keywordPrefix; }
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
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public Date getBeginTime() { return beginTime; }
    public void setBeginTime(Date beginTime) { this.beginTime = beginTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
}
