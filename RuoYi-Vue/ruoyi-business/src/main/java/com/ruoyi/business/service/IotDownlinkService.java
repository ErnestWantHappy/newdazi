package com.ruoyi.business.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 原下发接口的兼容响应；消息内容由课堂程序处理，平台不再判定或自动下发。 */
@Service
public class IotDownlinkService
{
    @Autowired private IotExperimentService experimentService;

    public Map<String, Object> manualSend(Long groupId, String text, String forcedCommand)
    {
        // 旧页面仍可能请求此接口，保留原权限校验且明确返回未发送。
        experimentService.requireManageableGroup(groupId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("published", false);
        result.put("downlinkEnabled", false);
        result.put("reason", "平台已取消自动判定下发，请在设备程序中订阅原 Topic");
        return result;
    }
}
