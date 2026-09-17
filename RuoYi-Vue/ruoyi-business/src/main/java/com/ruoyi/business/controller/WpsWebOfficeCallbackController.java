package com.ruoyi.business.controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.service.CollaborationTokenService;
import com.ruoyi.business.service.WpsCallbackSecurityService;
import com.ruoyi.business.service.WpsWebOfficeCallbackService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.ip.IpUtils;

/**
 * WPS 服务端公网回调。匿名仅指不使用平台 JWT，所有业务回调仍强制 WPS-2 + 短期 Token。
 */
@Anonymous
@RestController
public class WpsWebOfficeCallbackController
{
    private static final Logger log = LoggerFactory.getLogger(WpsWebOfficeCallbackController.class);

    @Autowired private WpsCallbackSecurityService securityService;
    @Autowired private WpsWebOfficeCallbackService callbackService;
    @Autowired private ObjectMapper objectMapper;

    @GetMapping("/weboffice/callback/v3/3rd/files/{fileId}")
    public Map<String, Object> fileInfo(@PathVariable String fileId, HttpServletRequest request)
    {
        return process(fileId, "FILE_INFO", request, new byte[0],
                claims -> callbackService.fileInfo(fileId, claims));
    }

    @GetMapping("/weboffice/callback/v3/3rd/files/{fileId}/download")
    public Map<String, Object> download(@PathVariable String fileId, HttpServletRequest request)
    {
        return process(fileId, "DOWNLOAD_ADDRESS", request, new byte[0],
                claims -> callbackService.downloadAddress(fileId, claims));
    }

    @GetMapping("/weboffice/callback/v3/3rd/files/{fileId}/permission")
    public Map<String, Object> permission(@PathVariable String fileId, HttpServletRequest request)
    {
        return process(fileId, "PERMISSION", request, new byte[0],
                claims -> callbackService.permission(fileId, claims));
    }

    @GetMapping("/weboffice/callback/v3/3rd/users")
    public Map<String, Object> users(@RequestParam(name = "user_ids") List<String> userIds,
                                     HttpServletRequest request)
    {
        return process(null, "USERS", request, new byte[0],
                claims -> callbackService.users(userIds, claims));
    }

    @GetMapping("/weboffice/callback/v3/3rd/files/{fileId}/upload/prepare")
    public Map<String, Object> uploadPrepare(@PathVariable String fileId, HttpServletRequest request)
    {
        return process(fileId, "UPLOAD_PREPARE", request, new byte[0],
                claims -> callbackService.uploadPrepare(fileId, claims));
    }

    @PostMapping(value = "/weboffice/callback/v3/3rd/files/{fileId}/upload/address",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> uploadAddress(@PathVariable String fileId,
                                             @RequestBody byte[] body,
                                             HttpServletRequest request)
    {
        return process(fileId, "UPLOAD_ADDRESS", request, body,
                claims -> callbackService.uploadAddress(fileId, readJson(body), claims));
    }

    @PostMapping(value = "/weboffice/callback/v3/3rd/files/{fileId}/upload/complete",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> uploadComplete(@PathVariable String fileId,
                                              @RequestBody byte[] body,
                                              HttpServletRequest request)
    {
        return process(fileId, "UPLOAD_COMPLETE", request, body,
                claims -> callbackService.uploadComplete(fileId, readJson(body), claims));
    }

    @PutMapping("/weboffice/storage/upload/{ticket}")
    public void storageUpload(@PathVariable String ticket, HttpServletRequest request,
                              HttpServletResponse response) throws Exception
    {
        try (InputStream input = request.getInputStream())
        {
            callbackService.receiveUpload(ticket, input);
            response.setStatus(HttpServletResponse.SC_OK);
        }
        catch (ServiceException e)
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
    }

    @GetMapping("/weboffice/storage/download/{fileId}")
    public void storageDownload(@PathVariable String fileId, @RequestParam String ticket,
                                HttpServletResponse response) throws Exception
    {
        try
        {
            Path file = callbackService.downloadFile(fileId, ticket);
            if (!Files.isRegularFile(file))
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "协作文档不存在");
                return;
            }
            String name = callbackService.downloadFileName(fileId);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, name);
            response.setContentLengthLong(Files.size(file));
            Files.copy(file, response.getOutputStream());
        }
        catch (ServiceException e)
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
    }

    private Map<String, Object> process(String fileId, String type, HttpServletRequest request,
                                        byte[] body, CallbackAction action)
    {
        long startedAt = System.currentTimeMillis();
        String requestId = request.getHeader("X-Request-Id");
        CollaborationTokenService.Claims claims = null;
        try
        {
            claims = securityService.verify(request, body);
            Map<String, Object> result = action.run(claims);
            safeRecord(fileId, type, true, requestId, claims.getUserId(),
                    IpUtils.getIpAddr(request), startedAt, null, null);
            return result;
        }
        catch (Exception e)
        {
            String message = e instanceof ServiceException ? e.getMessage() : "回调处理异常";
            log.warn("【WPS WebOffice】{} 失败，fileId={}，requestId={}：{}", type, fileId, requestId, message);
            safeRecord(fileId, type, false, requestId,
                    claims == null ? null : claims.getUserId(), IpUtils.getIpAddr(request), startedAt,
                    e instanceof ServiceException ? "BUSINESS_ERROR" : "INTERNAL_ERROR", message);
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("code", e instanceof ServiceException ? 40003 : 50001);
            result.put("message", message);
            return result;
        }
    }

    private void safeRecord(String fileId, String type, boolean success, String requestId, Long userId,
                            String remoteIp, long startedAt, String errorCode, String errorMessage)
    {
        try
        {
            callbackService.record(fileId, type, success, requestId, userId, remoteIp,
                    startedAt, errorCode, errorMessage);
        }
        catch (Exception e)
        {
            // 诊断表故障不能覆盖原始 WPS 回调结果，否则课堂上只会看到二次错误。
            log.error("【WPS WebOffice】写入回调诊断失败，requestId={}", requestId, e);
        }
    }

    private Map<String, Object> readJson(byte[] body) throws Exception
    {
        return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() { });
    }

    private interface CallbackAction
    {
        Map<String, Object> run(CollaborationTokenService.Claims claims) throws Exception;
    }
}
