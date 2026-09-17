package com.ruoyi.business.provider;

import java.util.Map;
import com.ruoyi.business.domain.CollaborationRoom;

/**
 * 统一编辑器适配边界。课程、班级、文件和权限仍由平台负责。
 */
public interface CollaborationProvider
{
    String id();

    Map<String, Object> health();

    Map<String, Object> session(CollaborationRoom room, Long userId, String scope);

    /**
     * 创建会话时允许业务层传入展示名；旧 Provider 不需要身份信息时继续走旧实现。
     */
    default Map<String, Object> session(CollaborationRoom room, Long userId, String scope, String displayName)
    {
        return session(room, userId, scope);
    }
}
