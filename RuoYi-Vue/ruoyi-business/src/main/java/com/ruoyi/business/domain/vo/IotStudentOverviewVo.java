package com.ruoyi.business.domain.vo;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 学生端物联网实验概览数据对象（仅返回本班、本组信息，绝不暴露其他班级/小组或 EMQX 管理凭据）
 */
public class IotStudentOverviewVo
{
    private Boolean hasExperiment = false;
    /** 当前课程是否已开启物联网（课程设计器开关）；未开启时即使存在历史实验也不展示。 */
    private Boolean iotEnabled = false;
    private Long experimentId;
    private String experimentTitle;
    private String activityCode;
    private String description;

    private String entryYear;
    private String classCode;
    private String studentNo;
    private String studentName;

    private Long groupId;
    private Integer groupNo;
    private String groupName;
    private String groupCode;
    private String topic;

    private String brokerUrl;
    private Integer brokerPort;
    private String mqttUsername;
    private String passcode;
    private String pythonClientId;
    private String brokerSyncStatus;
    private String brokerSyncError;

    private List<StudentMemberVo> groupMembers;

    private String latestPayloadText;
    private String latestPayloadType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date latestReceivedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastSeenAt;

    private Boolean isOnline = false;
    private String isolationNotice;

    public static class StudentMemberVo
    {
        private String studentNo;
        private String studentName;
        private Boolean isSelf = false;

        public StudentMemberVo() { }

        public StudentMemberVo(String studentNo, String studentName, Boolean isSelf)
        {
            this.studentNo = studentNo;
            this.studentName = studentName;
            this.isSelf = isSelf;
        }

        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public Boolean getIsSelf() { return isSelf; }
        public void setIsSelf(Boolean isSelf) { this.isSelf = isSelf; }
    }

    public Boolean getHasExperiment() { return hasExperiment; }
    public void setHasExperiment(Boolean hasExperiment) { this.hasExperiment = hasExperiment; }

    public Boolean getIotEnabled() { return iotEnabled; }
    public void setIotEnabled(Boolean iotEnabled) { this.iotEnabled = iotEnabled; }

    public Long getExperimentId() { return experimentId; }
    public void setExperimentId(Long experimentId) { this.experimentId = experimentId; }

    public String getExperimentTitle() { return experimentTitle; }
    public void setExperimentTitle(String experimentTitle) { this.experimentTitle = experimentTitle; }

    public String getActivityCode() { return activityCode; }
    public void setActivityCode(String activityCode) { this.activityCode = activityCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Integer getGroupNo() { return groupNo; }
    public void setGroupNo(Integer groupNo) { this.groupNo = groupNo; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getBrokerUrl() { return brokerUrl; }
    public void setBrokerUrl(String brokerUrl) { this.brokerUrl = brokerUrl; }

    public Integer getBrokerPort() { return brokerPort; }
    public void setBrokerPort(Integer brokerPort) { this.brokerPort = brokerPort; }

    public String getMqttUsername() { return mqttUsername; }
    public void setMqttUsername(String mqttUsername) { this.mqttUsername = mqttUsername; }

    public String getPasscode() { return passcode; }
    public void setPasscode(String passcode) { this.passcode = passcode; }

    public String getPythonClientId() { return pythonClientId; }
    public void setPythonClientId(String pythonClientId) { this.pythonClientId = pythonClientId; }

    public String getBrokerSyncStatus() { return brokerSyncStatus; }
    public void setBrokerSyncStatus(String brokerSyncStatus) { this.brokerSyncStatus = brokerSyncStatus; }

    public String getBrokerSyncError() { return brokerSyncError; }
    public void setBrokerSyncError(String brokerSyncError) { this.brokerSyncError = brokerSyncError; }

    public List<StudentMemberVo> getGroupMembers() { return groupMembers; }
    public void setGroupMembers(List<StudentMemberVo> groupMembers) { this.groupMembers = groupMembers; }

    public String getLatestPayloadText() { return latestPayloadText; }
    public void setLatestPayloadText(String latestPayloadText) { this.latestPayloadText = latestPayloadText; }

    public String getLatestPayloadType() { return latestPayloadType; }
    public void setLatestPayloadType(String latestPayloadType) { this.latestPayloadType = latestPayloadType; }

    public Date getLatestReceivedAt() { return latestReceivedAt; }
    public void setLatestReceivedAt(Date latestReceivedAt) { this.latestReceivedAt = latestReceivedAt; }

    public Date getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Date lastSeenAt) { this.lastSeenAt = lastSeenAt; }

    public Boolean getIsOnline() { return isOnline; }
    public void setIsOnline(Boolean isOnline) { this.isOnline = isOnline; }

    public String getIsolationNotice() { return isolationNotice; }
    public void setIsolationNotice(String isolationNotice) { this.isolationNotice = isolationNotice; }
}
