package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 物联网实验班级配置与口令快照 biz_iot_class_config
 */
public class IotClassConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long configId;
    private Long experimentId;
    private Long deptId;
    private String entryYear;
    private String classCode;
    private Integer groupSize;
    private String mqttUsername;
    private String passcodeCiphertext;
    private String passcodeHash;
    private Integer passcodeVersion;
    private Integer groupVersion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date passcodeUpdatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date groupedAt;

    /** Broker 同步状态：PENDING / SYNCED / FAILED。 */
    private String brokerSyncStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date brokerSyncedAt;

    /** 仅保存脱敏后的故障提示，不保存管理凭据或原始响应。 */
    private String brokerSyncError;

    private String status;

    // 非数据库持久化字段（用于向已授权教师/学生返回当前有效解密口令与统计数据）
    private String passcode;
    private Integer studentCount;
    private Integer groupCount;

    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }

    public Long getExperimentId() { return experimentId; }
    public void setExperimentId(Long experimentId) { this.experimentId = experimentId; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getEntryYear() { return entryYear; }
    public void setEntryYear(String entryYear) { this.entryYear = entryYear; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public Integer getGroupSize() { return groupSize; }
    public void setGroupSize(Integer groupSize) { this.groupSize = groupSize; }

    public String getMqttUsername() { return mqttUsername; }
    public void setMqttUsername(String mqttUsername) { this.mqttUsername = mqttUsername; }

    public String getPasscodeCiphertext() { return passcodeCiphertext; }
    public void setPasscodeCiphertext(String passcodeCiphertext) { this.passcodeCiphertext = passcodeCiphertext; }

    public String getPasscodeHash() { return passcodeHash; }
    public void setPasscodeHash(String passcodeHash) { this.passcodeHash = passcodeHash; }

    public Integer getPasscodeVersion() { return passcodeVersion; }
    public void setPasscodeVersion(Integer passcodeVersion) { this.passcodeVersion = passcodeVersion; }

    public Integer getGroupVersion() { return groupVersion; }
    public void setGroupVersion(Integer groupVersion) { this.groupVersion = groupVersion; }

    public Date getPasscodeUpdatedAt() { return passcodeUpdatedAt; }
    public void setPasscodeUpdatedAt(Date passcodeUpdatedAt) { this.passcodeUpdatedAt = passcodeUpdatedAt; }

    public Date getGroupedAt() { return groupedAt; }
    public void setGroupedAt(Date groupedAt) { this.groupedAt = groupedAt; }

    public String getBrokerSyncStatus() { return brokerSyncStatus; }
    public void setBrokerSyncStatus(String brokerSyncStatus) { this.brokerSyncStatus = brokerSyncStatus; }

    public Date getBrokerSyncedAt() { return brokerSyncedAt; }
    public void setBrokerSyncedAt(Date brokerSyncedAt) { this.brokerSyncedAt = brokerSyncedAt; }

    public String getBrokerSyncError() { return brokerSyncError; }
    public void setBrokerSyncError(String brokerSyncError) { this.brokerSyncError = brokerSyncError; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPasscode() { return passcode; }
    public void setPasscode(String passcode) { this.passcode = passcode; }

    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }

    public Integer getGroupCount() { return groupCount; }
    public void setGroupCount(Integer groupCount) { this.groupCount = groupCount; }
}
