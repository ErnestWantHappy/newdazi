package com.ruoyi.business.provider;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.business.config.CryptPadProperties;
import com.ruoyi.business.domain.CollaborationRoom;
import com.ruoyi.business.service.CollaborationSecretService;
import com.ruoyi.common.exception.ServiceException;

/** CryptPad Integration API 适配器。CryptPad 不持有平台权限，平台只交给它短期会话配置。 */
@Component
public class CryptPadAdapter implements CollaborationProvider
{
    @Autowired private CryptPadProperties properties;
    @Autowired private CollaborationSecretService secretService;

    @Override
    public String id() { return "CRYPTPAD"; }

    @Override
    public Map<String, Object> health()
    {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("provider", id());
        result.put("baseUrlConfigured", StringUtils.isNotBlank(properties.getBaseUrl()));
        result.put("apiUrlConfigured", StringUtils.isNotBlank(properties.getApiUrl()));
        result.put("keySecretConfigured", StringUtils.isNotBlank(properties.getKeySecret())
                && properties.getKeySecret().length() >= 32);
        result.put("remoteEmbedding", properties.isRemoteEmbedding());
        result.put("ready", ready());
        return result;
    }

    @Override
    public Map<String, Object> session(CollaborationRoom room, Long userId, String scope)
    {
        return session(room, userId, scope, null);
    }

    @Override
    public Map<String, Object> session(CollaborationRoom room, Long userId, String scope, String displayName)
    {
        if (!ready()) throw new ServiceException("CryptPad 尚未配置完成");
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("provider", id());
        result.put("apiUrl", properties.getApiUrl());
        result.put("baseUrl", StringUtils.removeEnd(properties.getBaseUrl(), "/"));
        result.put("documentKey", secretService.decrypt(room.getProviderSessionKey()));
        result.put("documentType", documentType(room.getCurrentFileExtension()));
        result.put("fileType", room.getCurrentFileExtension());
        result.put("title", room.getCurrentFileName());
        result.put("autosave", properties.getAutosaveSeconds());
        result.put("mode", "READ_ONLY".equals(room.getStatus()) ? "view" : "edit");
        result.put("user", StringUtils.defaultIfBlank(displayName,
                userId == null ? "平台用户" : "协作用户"));
        // CryptPad/OnlyOffice 用稳定参与者 ID 区分同时编辑者；只传姓名会被当作匿名会话复用。
        result.put("participantId", "p-" + DigestUtils.sha256Hex(
                String.valueOf(room.getRoomId()) + ":" + String.valueOf(userId) + ":" + properties.getKeySecret()).substring(0, 24));
        result.put("scope", scope);
        result.put("roomId", room.getRoomId());
        result.put("version", room.getCurrentVersion());
        return result;
    }

    public boolean ready()
    {
        if (StringUtils.isBlank(properties.getBaseUrl()) || StringUtils.isBlank(properties.getApiUrl())
                || StringUtils.isBlank(properties.getKeySecret()) || properties.getKeySecret().length() < 32)
            return false;
        try
        {
            URI base = URI.create(properties.getBaseUrl());
            return ("http".equalsIgnoreCase(base.getScheme()) || "https".equalsIgnoreCase(base.getScheme()))
                    && StringUtils.isNotBlank(base.getHost()) && properties.isRemoteEmbedding();
        }
        catch (Exception e) { return false; }
    }

    private String documentType(String extension)
    {
        String ext = StringUtils.lowerCase(StringUtils.defaultString(extension));
        if ("xlsx".equals(ext) || "xls".equals(ext) || "csv".equals(ext)) return "sheet";
        if ("pptx".equals(ext) || "ppt".equals(ext) || "ppsx".equals(ext)) return "presentation";
        return "doc";
    }
}
