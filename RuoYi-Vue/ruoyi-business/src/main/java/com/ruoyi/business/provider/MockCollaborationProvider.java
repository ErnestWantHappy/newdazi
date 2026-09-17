package com.ruoyi.business.provider;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.ruoyi.business.domain.CollaborationRoom;

/** 本机接口和权限测试使用的 Provider，不依赖外部 CryptPad。 */
@Component
public class MockCollaborationProvider implements CollaborationProvider
{
    @Override
    public String id() { return "MOCK"; }

    @Override
    public Map<String, Object> health()
    {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("provider", id());
        result.put("ready", true);
        return result;
    }

    @Override
    public Map<String, Object> session(CollaborationRoom room, Long userId, String scope)
    {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("provider", id());
        result.put("roomId", room.getRoomId());
        result.put("scope", scope);
        result.put("documentType", "mock");
        result.put("mode", "READ_ONLY".equals(room.getStatus()) ? "view" : "edit");
        return result;
    }
}
