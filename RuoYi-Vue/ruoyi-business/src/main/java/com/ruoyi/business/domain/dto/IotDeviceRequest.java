package com.ruoyi.business.domain.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 创建物联网设备请求。 */
public class IotDeviceRequest
{
    @NotNull
    private Long groupId;
    @NotBlank
    @Size(max = 64)
    private String deviceCode;
    @NotBlank
    @Size(max = 128)
    private String deviceName;

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long value) { groupId = value; }
    public String getDeviceCode() { return deviceCode; }
    public void setDeviceCode(String value) { deviceCode = value; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String value) { deviceName = value; }
}
