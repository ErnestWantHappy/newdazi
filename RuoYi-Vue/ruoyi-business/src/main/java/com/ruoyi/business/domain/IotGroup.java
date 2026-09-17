package com.ruoyi.business.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

/** 物联网实验小组。 */
public class IotGroup
{
    private Long groupId;
    private Long experimentId;
    private String entryYear;
    private String classCode;
    private String groupCode;
    private Integer groupNo;
    private String groupName;
    private String topic;
    private String status;
    private Integer deviceCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastSeenAt;

    private String createBy;

    // 关联与统计字段
    private List<IotGroupStudent> studentList;
    private Integer studentCount;

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long value) { groupId = value; }

    public Long getExperimentId() { return experimentId; }
    public void setExperimentId(Long value) { experimentId = value; }

    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String value) { entryYear = value; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String value) { classCode = value; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String value) { groupCode = value; }

    public Integer getGroupNo() { return groupNo; }
    public void setGroupNo(Integer value) { groupNo = value; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String value) { groupName = value; }

    public String getTopic() { return topic; }
    public void setTopic(String value) { topic = value; }

    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }

    public Integer getDeviceCount() { return deviceCount; }
    public void setDeviceCount(Integer value) { deviceCount = value; }

    public Date getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Date value) { lastSeenAt = value; }

    public String getCreateBy() { return createBy; }
    public void setCreateBy(String value) { createBy = value; }

    public List<IotGroupStudent> getStudentList() { return studentList; }
    public void setStudentList(List<IotGroupStudent> studentList) { this.studentList = studentList; }

    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }
}
