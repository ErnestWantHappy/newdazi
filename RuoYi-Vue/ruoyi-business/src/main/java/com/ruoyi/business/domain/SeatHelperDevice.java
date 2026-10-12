package com.ruoyi.business.domain;

/** 设备凭据只用于认证，禁止整实体直接返回网页。 */
public class SeatHelperDevice {
    public String deviceId, roomName, seatName, credentialHash, fingerprint;
    public Long deptId;
    public boolean active;
}
