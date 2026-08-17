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
}
