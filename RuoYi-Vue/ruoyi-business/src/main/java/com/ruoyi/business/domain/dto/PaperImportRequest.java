package com.ruoyi.business.domain.dto;

/** 导入范围由服务端重新校验，不接受客户端指定教师或学校。 */
public class PaperImportRequest
{
    public Long lessonId;
    public Long questionId;
    public String entryYear;
    public String classCode;
    public String token;
    public Long studentId;
    public Long expectedVersionId;
    public Integer expectedScore;
    public boolean replace;
    public Integer page;
    public java.util.List<Segment> segments;
    public static class Segment
    {
        public int startPage, endPage;
        public Long studentId;
        public boolean confirmed;
    }
}
