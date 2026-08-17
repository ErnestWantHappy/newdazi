package com.ruoyi.business.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ruoyi.business.config.WpsWebOfficeProperties;
import com.ruoyi.common.exception.ServiceException;

/** 严格校验 WPS-2 服务端回调签名和平台透传 Token。 */
@Service
public class WpsCallbackSecurityService
{
    private static final long MAX_CLOCK_SKEW_MINUTES = 10L;

    @Autowired
    private WpsWebOfficeProperties properties;

    @Autowired
    private CollaborationTokenService tokenService;

    @Value("${collaboration.enabled:false}")
    private boolean enabled;

    public CollaborationTokenService.Claims verify(HttpServletRequest request, byte[] body)
    {
        // 供应商停用后即使旧地址仍被访问，也不再进入文件读取或保存流程。
        if (!enabled)
        {
            throw new ServiceException("在线协作服务已停用");
        }
        if (StringUtils.isAnyBlank(properties.getAppId(), properties.getAppSecret()))
        {
            throw new ServiceException("WPS AppID/AppSecret 尚未配置");
        }
        String appId = request.getHeader("X-App-Id");
        if (!properties.getAppId().equals(appId))
        {
            throw new ServiceException("WPS 回调 AppID 不匹配");
        }
        String date = request.getHeader("Date");
        validateDate(date);
        String contentType = "GET".equalsIgnoreCase(request.getMethod())
                ? "" : StringUtils.defaultString(request.getContentType());
        String contentMd5;
        if (body != null && body.length > 0)
        {
            contentMd5 = DigestUtils.md5Hex(body);
        }
        else
        {
            contentMd5 = DigestUtils.md5Hex(request.getRequestURI().getBytes(StandardCharsets.UTF_8));
        }
        String sha1 = DigestUtils.sha1Hex(properties.getAppSecret() + contentMd5 + contentType + date);
        String expected = "WPS-2:" + properties.getAppId() + ":" + sha1;
        String actual = request.getHeader("Authorization");
        if (!MessageDigest.isEqual(expected.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8),
                StringUtils.defaultString(actual).toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8)))
        {
            throw new ServiceException("WPS-2 回调签名校验失败");
        }
        return tokenService.verify(request.getHeader("X-Weboffice-Token"));
    }

    private void validateDate(String date)
    {
        try
        {
            ZonedDateTime requestTime = ZonedDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME);
            long minutes = Math.abs(ChronoUnit.MINUTES.between(requestTime, ZonedDateTime.now(requestTime.getZone())));
            if (minutes > MAX_CLOCK_SKEW_MINUTES)
            {
                throw new ServiceException("WPS 回调时间超过允许窗口");
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("WPS 回调 Date 头无效");
        }
    }
}
