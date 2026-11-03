package com.ruoyi.business.domain.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 课堂推进的班级选择与预览课程快照。 */
public class LessonAdvanceRequestVo
{
    private String entryYear;
    private List<String> classCodes;
    private String classCode;
    private Map<String, Long> expectedLessonIds;

    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }
    public List<String> getClassCodes() { return classCodes; }
    public void setClassCodes(List<String> classCodes) { this.classCodes = classCodes; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
    public Map<String, Long> getExpectedLessonIds() { return expectedLessonIds; }
    public void setExpectedLessonIds(Map<String, Long> expectedLessonIds) { this.expectedLessonIds = expectedLessonIds; }

    /** 兼容旧页面只传一个班号的请求。 */
    public List<String> selectedClasses()
    {
        if (classCodes != null && !classCodes.isEmpty())
        {
            return classCodes;
        }
        List<String> selected = new ArrayList<>();
        if (classCode != null)
        {
            selected.add(classCode);
        }
        return selected;
    }
}
