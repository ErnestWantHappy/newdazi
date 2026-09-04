package com.ruoyi.business.domain.vo;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 班级物联配置卡数据对象（用于课堂投屏/打印，不含任何管理凭据）
 */
public class IotClassCardVo
{
    private Long configId;
    private String brokerUrl;
    private Integer brokerPort;
    private String mqttUsername;
    private String passcode;
    private String experimentTitle;
    private String entryYear;
    private String classCode;
    private Integer groupSize;
    private Integer studentCount;
    private Integer groupCount;
    private String brokerSyncStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date brokerSyncedAt;

    private String brokerSyncError;
    private List<GroupItem> groups;

    public static class GroupItem
    {
        private Long groupId;
        private Integer groupNo;
        private String groupCode;
        private String groupName;
        private String topic;
        private String pythonClientId;
        private List<String> memberNames;

        public Long getGroupId() { return groupId; }
        public void setGroupId(Long groupId) { this.groupId = groupId; }

        public Integer getGroupNo() { return groupNo; }
        public void setGroupNo(Integer groupNo) { this.groupNo = groupNo; }

        public String getGroupCode() { return groupCode; }
        public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }

        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }

        public String getPythonClientId() { return pythonClientId; }
        public void setPythonClientId(String pythonClientId) { this.pythonClientId = pythonClientId; }

        public List<String> getMemberNames() { return memberNames; }
        public void setMemberNames(List<String> memberNames) { this.memberNames = memberNames; }
    }

    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }

    public String getBrokerUrl() { return brokerUrl; }
    public void setBrokerUrl(String brokerUrl) { this.brokerUrl = brokerUrl; }

    public Integer getBrokerPort() { return brokerPort; }
    public void setBrokerPort(Integer brokerPort) { this.brokerPort = brokerPort; }

    public String getMqttUsername() { return mqttUsername; }
    public void setMqttUsername(String mqttUsername) { this.mqttUsername = mqttUsername; }

    public String getPasscode() { return passcode; }
    public void setPasscode(String passcode) { this.passcode = passcode; }

    public String getExperimentTitle() { return experimentTitle; }
    public void setExperimentTitle(String experimentTitle) { this.experimentTitle = experimentTitle; }

    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public Integer getGroupSize() { return groupSize; }
    public void setGroupSize(Integer groupSize) { this.groupSize = groupSize; }

    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }

    public Integer getGroupCount() { return groupCount; }
    public void setGroupCount(Integer groupCount) { this.groupCount = groupCount; }

    public String getBrokerSyncStatus() { return brokerSyncStatus; }
    public void setBrokerSyncStatus(String brokerSyncStatus) { this.brokerSyncStatus = brokerSyncStatus; }

    public Date getBrokerSyncedAt() { return brokerSyncedAt; }
    public void setBrokerSyncedAt(Date brokerSyncedAt) { this.brokerSyncedAt = brokerSyncedAt; }

    public String getBrokerSyncError() { return brokerSyncError; }
    public void setBrokerSyncError(String brokerSyncError) { this.brokerSyncError = brokerSyncError; }

    public List<GroupItem> getGroups() { return groups; }
    public void setGroups(List<GroupItem> groups) { this.groups = groups; }
}
