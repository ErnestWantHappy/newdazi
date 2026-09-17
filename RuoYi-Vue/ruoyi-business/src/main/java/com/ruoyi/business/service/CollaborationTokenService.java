package com.ruoyi.business.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.config.WpsWebOfficeProperties;
import com.ruoyi.common.exception.ServiceException;

/** 给 WPS 透传的短期令牌，不复用浏览器 JWT，避免扩大平台登录态。 */
@Service
public class CollaborationTokenService
{
    @Autowired
    private WpsWebOfficeProperties properties;

    public String issue(Long userId, Long roomId, String scope)
    {
        long expiresAt = System.currentTimeMillis() + properties.getTokenMinutes() * 60_000L;
        String payload = "1|" + userId + "|" + roomId + "|" + scope + "|" + expiresAt;
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encoded + "." + sign(encoded);
    }

    public Claims verify(String token)
    {
        if (StringUtils.isBlank(token) || !token.contains("."))
        {
            throw new ServiceException("WebOffice 身份令牌缺失");
        }
        String[] parts = token.split("\\.", 2);
        if (!constantTimeEquals(sign(parts[0]), parts[1]))
        {
            throw new ServiceException("WebOffice 身份令牌签名无效");
        }
        try
        {
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] values = payload.split("\\|", -1);
            if (values.length != 5 || !"1".equals(values[0]))
            {
                throw new IllegalArgumentException("版本不支持");
            }
            Claims claims = new Claims(Long.valueOf(values[1]), Long.valueOf(values[2]),
                    values[3], Long.parseLong(values[4]));
            if (claims.getExpiresAt() < System.currentTimeMillis())
            {
                throw new ServiceException("WebOffice 身份令牌已过期，请重新进入房间");
            }
            return claims;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("WebOffice 身份令牌格式无效");
        }
    }

    private String sign(String value)
    {
        if (StringUtils.isBlank(properties.getTokenSecret()))
        {
            throw new ServiceException("未配置 WPS_WEBOFFICE_TOKEN_SECRET");
        }
        try
        {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getTokenSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        }
        catch (Exception e)
        {
            throw new ServiceException("WebOffice 身份令牌生成失败");
        }
    }

    private boolean constantTimeEquals(String left, String right)
    {
        if (left == null || right == null) return false;
        byte[] a = left.getBytes(StandardCharsets.UTF_8);
        byte[] b = right.getBytes(StandardCharsets.UTF_8);
        int diff = a.length ^ b.length;
        int max = Math.max(a.length, b.length);
        for (int i = 0; i < max; i++)
        {
            diff |= (i < a.length ? a[i] : 0) ^ (i < b.length ? b[i] : 0);
        }
        return diff == 0;
    }

    public static class Claims
    {
        private final Long userId;
        private final Long roomId;
        private final String scope;
        private final long expiresAt;

        public Claims(Long userId, Long roomId, String scope, long expiresAt)
        {
            this.userId = userId;
            this.roomId = roomId;
            this.scope = scope;
            this.expiresAt = expiresAt;
        }

        public Long getUserId() { return userId; }
        public Long getRoomId() { return roomId; }
        public String getScope() { return scope; }
        public long getExpiresAt() { return expiresAt; }
    }
}

