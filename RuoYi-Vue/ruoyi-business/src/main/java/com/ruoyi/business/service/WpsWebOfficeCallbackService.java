package com.ruoyi.business.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.CollaborationRoom;
import com.ruoyi.business.domain.CollaborationUploadTicket;
import com.ruoyi.business.mapper.CollaborationMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.mapper.SysUserMapper;

/** WPS WebOffice 文件系统回调实现。 */
@Service
public class WpsWebOfficeCallbackService
{
    @Autowired private CollaborationMapper mapper;
    @Autowired private CollaborationRoomService roomService;
    @Autowired private CollaborationTokenService tokenService;
    @Autowired private SysUserMapper userMapper;

    public Map<String, Object> fileInfo(String fileId, CollaborationTokenService.Claims claims)
    {
        CollaborationRoom room = requireClaimsRoom(fileId, claims);
        return success(fileData(room));
    }

    public Map<String, Object> downloadAddress(String fileId, CollaborationTokenService.Claims claims)
    {
        CollaborationRoom room = requireClaimsRoom(fileId, claims);
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        String ticket = tokenService.issue(claims.getUserId(), room.getRoomId(), "DOWNLOAD");
        data.put("url", roomService.publicBaseUrl() + "/weboffice/storage/download/"
                + fileId + "?ticket=" + ticket);
        data.put("digest", room.getCurrentSha256());
        data.put("digest_type", "sha256");
        return success(data);
    }

    public Map<String, Object> permission(String fileId, CollaborationTokenService.Claims claims)
    {
        CollaborationRoom room = requireClaimsRoom(fileId, claims);
        boolean readable = !"CLOSED".equals(room.getStatus());
        boolean editable = "OPEN".equals(room.getStatus());
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("user_id", wpsUserId(claims.getUserId()));
        data.put("read", readable ? 1 : 0);
        data.put("update", editable ? 1 : 0);
        data.put("download", readable ? 1 : 0);
        data.put("rename", 0);
        data.put("history", 0);
        data.put("copy", readable ? 1 : 0);
        data.put("print", readable ? 1 : 0);
        data.put("saveas", 0);
        data.put("comment", editable ? 1 : 0);
        return success(data);
    }

    public Map<String, Object> users(List<String> userIds, CollaborationTokenService.Claims claims)
    {
        // 先确认 Token 仍指向有效房间，避免把该接口变成匿名用户目录。
        roomService.requireRoom(claims.getRoomId());
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        if (userIds != null)
        {
            for (String wpsId : userIds)
            {
                Long userId = parseWpsUserId(wpsId);
                SysUser user = userMapper.selectUserById(userId);
                if (user == null) continue;
                Map<String, Object> item = new LinkedHashMap<String, Object>();
                item.put("id", wpsUserId(userId));
                item.put("name", StringUtils.defaultIfBlank(user.getNickName(), user.getUserName()));
                data.add(item);
            }
        }
        return success(data);
    }

    public Map<String, Object> uploadPrepare(String fileId, CollaborationTokenService.Claims claims)
    {
        CollaborationRoom room = requireClaimsRoom(fileId, claims);
        requireEditable(room);
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        List<String> types = new ArrayList<String>();
        types.add("sha256");
        data.put("digest_types", types);
        return success(data);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadAddress(String fileId, Map<String, Object> body,
                                             CollaborationTokenService.Claims claims) throws IOException
    {
        CollaborationRoom room = requireClaimsRoom(fileId, claims);
        requireEditable(room);
        String name = stringValue(body.get("name"));
        long size = longValue(body.get("size"));
        if (size <= 0) throw new ServiceException("WPS 声明的保存文件大小无效");
        @SuppressWarnings("unchecked")
        Map<String, Object> digests = body.get("digest") instanceof Map
                ? (Map<String, Object>) body.get("digest") : new LinkedHashMap<String, Object>();
        String digestType = digests.containsKey("sha256") ? "sha256"
                : (digests.containsKey("sha1") ? "sha1" : (digests.containsKey("md5") ? "md5" : null));
        if (digestType == null) throw new ServiceException("WPS 保存请求未提供可校验摘要");
        String digest = stringValue(digests.get(digestType)).toLowerCase();
        boolean manual = Boolean.TRUE.equals(body.get("is_manual"));
        String ticketToken = UUID.randomUUID().toString().replace("-", "");
        String tempPath = "collaboration/temp/" + ticketToken + ".upload";
        Path temp = roomService.resolveStoredFile(tempPath);
        Files.createDirectories(temp.getParent());
        CollaborationUploadTicket ticket = new CollaborationUploadTicket();
        Date now = new Date();
        ticket.setTicketToken(ticketToken);
        ticket.setRoomId(room.getRoomId());
        ticket.setExpectedVersion(room.getCurrentVersion());
        ticket.setExpectedFileName(name);
        ticket.setExpectedFileSize(size);
        ticket.setExpectedDigestType(digestType);
        ticket.setExpectedDigest(digest);
        ticket.setManualSave(manual);
        ticket.setTempFilePath(tempPath);
        ticket.setStatus("PREPARED");
        ticket.setRequesterUserId(claims.getUserId());
        ticket.setExpiresTime(new Date(now.getTime() + 15L * 60L * 1000L));
        ticket.setCreateTime(now);
        ticket.setUpdateTime(now);
        mapper.insertUploadTicket(ticket);
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("url", roomService.publicBaseUrl() + "/weboffice/storage/upload/" + ticketToken);
        data.put("method", "PUT");
        Map<String, String> sendBack = new LinkedHashMap<String, String>();
        sendBack.put("ticket", ticketToken);
        data.put("send_back_params", sendBack);
        return success(data);
    }

    public void receiveUpload(String ticketToken, InputStream input) throws IOException
    {
        CollaborationUploadTicket ticket = mapper.selectUploadTicket(ticketToken);
        Date now = new Date();
        if (ticket == null || !"PREPARED".equals(ticket.getStatus()) || ticket.getExpiresTime().before(now))
            throw new ServiceException("WPS 上传地址已失效，请重新保存");
        Path target = roomService.resolveStoredFile(ticket.getTempFilePath());
        Files.createDirectories(target.getParent());
        MessageDigest sha256;
        try { sha256 = MessageDigest.getInstance("SHA-256"); }
        catch (Exception e) { throw new IOException("初始化摘要失败", e); }
        long total = 0L;
        try (OutputStream output = Files.newOutputStream(target); DigestInputStream digestInput = new DigestInputStream(input, sha256))
        {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = digestInput.read(buffer)) >= 0)
            {
                total += read;
                if (total > ticket.getExpectedFileSize() + 1024L)
                    throw new ServiceException("WPS 实际上传文件大于声明大小");
                output.write(buffer, 0, read);
            }
        }
        if (total != ticket.getExpectedFileSize())
            throw new ServiceException("WPS 实际上传大小与声明不一致");
        String sha = Hex.encodeHexString(sha256.digest());
        if (mapper.markTicketUploaded(ticketToken, total, sha, now) != 1)
            throw new ServiceException("WPS 上传票据状态已变化，请重新保存");
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadComplete(String fileId, Map<String, Object> body,
                                              CollaborationTokenService.Claims claims) throws IOException
    {
        CollaborationRoom room = requireClaimsRoom(fileId, claims);
        requireEditable(room);
        @SuppressWarnings("unchecked")
        Map<String, Object> sendBack = body.get("send_back_params") instanceof Map
                ? (Map<String, Object>) body.get("send_back_params") : new LinkedHashMap<String, Object>();
        String token = stringValue(sendBack.get("ticket"));
        CollaborationUploadTicket ticket = mapper.selectUploadTicket(token);
        if (ticket == null || !room.getRoomId().equals(ticket.getRoomId())) throw new ServiceException("WPS 保存票据不存在");
        if (!"UPLOADED".equals(ticket.getStatus())) throw new ServiceException("WPS 文件尚未成功上传");
        @SuppressWarnings("unchecked")
        Map<String, Object> response = body.get("response") instanceof Map
                ? (Map<String, Object>) body.get("response") : new LinkedHashMap<String, Object>();
        long statusCode = longValue(response.get("status_code"));
        if (statusCode < 200 || statusCode >= 300) throw new ServiceException("WPS 上传存储返回状态：" + statusCode);
        if (!room.getCurrentVersion().equals(ticket.getExpectedVersion())) throw new ServiceException("协作文档版本已变化，请重新保存");
        Path temp = roomService.resolveStoredFile(ticket.getTempFilePath());
        if (!Files.isRegularFile(temp)) throw new ServiceException("WPS 上传临时文件不存在");
        String actualDigest = digest(temp, ticket.getExpectedDigestType());
        if (!actualDigest.equalsIgnoreCase(ticket.getExpectedDigest())) throw new ServiceException("WPS 上传文件摘要不一致");
        int nextVersion = room.getCurrentVersion() + 1;
        String extension = room.getCurrentFileExtension();
        String finalPath = "collaboration/rooms/" + room.getPublicFileId() + "/v" + nextVersion
                + "-" + token.substring(0, 8) + "." + extension;
        Path target = roomService.resolveStoredFile(finalPath);
        Files.createDirectories(target.getParent());
        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        String sha256 = digest(target, "sha256");
        Date now = new Date();
        String contentType = null;
        @SuppressWarnings("unchecked")
        Map<String, Object> request = body.get("request") instanceof Map
                ? (Map<String, Object>) body.get("request") : new LinkedHashMap<String, Object>();
        if (request.get("content_type") != null) contentType = stringValue(request.get("content_type"));
        if (mapper.commitRoomVersion(room.getRoomId(), room.getCurrentVersion(), nextVersion,
                ticket.getExpectedFileName(), finalPath, extension, contentType, ticket.getExpectedFileSize(),
                sha256, claims.getUserId(), now) != 1)
            throw new ServiceException("协作文档发生并发保存冲突，请重新保存");
        mapper.insertRevision(room.getRoomId(), nextVersion, ticket.getExpectedFileName(), finalPath,
                ticket.getExpectedFileSize(), sha256, ticket.getExpectedDigestType(), ticket.getExpectedDigest(),
                ticket.getManualSave(), claims.getUserId(), now);
        mapper.markTicketCompleted(ticket.getTicketId(), "COMPLETED", now, null);
        CollaborationRoom updated = roomService.requireRoom(room.getRoomId());
        return success(fileData(updated));
    }

    public Path downloadFile(String fileId, String downloadToken)
    {
        CollaborationTokenService.Claims claims = tokenService.verify(downloadToken);
        if (!"DOWNLOAD".equals(claims.getScope())) throw new ServiceException("下载令牌用途无效");
        CollaborationRoom room = requireClaimsRoom(fileId, claims);
        return roomService.resolveStoredFile(room.getCurrentFilePath());
    }

    public String downloadFileName(String fileId)
    {
        return roomService.requireRoomByFileId(fileId).getCurrentFileName();
    }

    public void record(String fileId, String type, boolean success, String requestId, Long userId,
                       String remoteIp, long startedAt, String errorCode, String errorMessage)
    {
        CollaborationRoom room = null;
        try { room = StringUtils.isBlank(fileId) ? null : roomService.requireRoomByFileId(fileId); }
        catch (Exception ignored) { }
        Date now = new Date();
        if (room != null)
            mapper.updateRoomCallback(room.getRoomId(), type, success ? "SUCCESS" : "FAILED",
                    requestId, truncate(errorMessage, 1000), now);
        mapper.insertCallbackEvent(room == null ? null : room.getRoomId(), fileId, type,
                success ? "SUCCESS" : "FAILED", requestId, userId, remoteIp,
                System.currentTimeMillis() - startedAt, errorCode, truncate(errorMessage, 1000), now);
    }

    private CollaborationRoom requireClaimsRoom(String fileId, CollaborationTokenService.Claims claims)
    {
        CollaborationRoom room = roomService.requireRoomByFileId(fileId);
        if (!room.getRoomId().equals(claims.getRoomId())) throw new ServiceException("Token 与协作文档不匹配");
        return room;
    }

    private void requireEditable(CollaborationRoom room)
    {
        if (!"OPEN".equals(room.getStatus())) throw new ServiceException("协作文档当前不可编辑");
    }

    private Map<String, Object> fileData(CollaborationRoom room)
    {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("id", room.getPublicFileId());
        data.put("name", room.getCurrentFileName());
        data.put("version", room.getCurrentVersion());
        data.put("size", room.getCurrentFileSize());
        data.put("create_time", epoch(room.getCreateTime()));
        data.put("modify_time", epoch(room.getLastSaveTime() == null ? room.getUpdateTime() : room.getLastSaveTime()));
        data.put("creator_id", wpsUserId(room.getCreatorUserId()));
        data.put("modifier_id", wpsUserId(room.getModifierUserId()));
        return data;
    }

    private Map<String, Object> success(Object data)
    {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("code", 0);
        result.put("message", "");
        result.put("data", data);
        return result;
    }

    private long epoch(Date date) { return (date == null ? System.currentTimeMillis() : date.getTime()) / 1000L; }
    private String wpsUserId(Long userId) { return "u" + userId; }
    private Long parseWpsUserId(String value)
    {
        if (StringUtils.isBlank(value) || !value.matches("u\\d+")) throw new ServiceException("WPS 用户ID格式无效");
        return Long.valueOf(value.substring(1));
    }
    private String stringValue(Object value)
    {
        String result = value == null ? "" : String.valueOf(value).trim();
        if (result.isEmpty()) throw new ServiceException("WPS 回调缺少必要字段");
        return result;
    }
    private long longValue(Object value)
    {
        try { return Long.parseLong(stringValue(value)); }
        catch (NumberFormatException e) { throw new ServiceException("WPS 回调数字字段格式无效"); }
    }
    private String digest(Path file, String type) throws IOException
    {
        String algorithm = "sha256".equalsIgnoreCase(type) ? "SHA-256"
                : ("sha1".equalsIgnoreCase(type) ? "SHA-1" : ("md5".equalsIgnoreCase(type) ? "MD5" : null));
        if (algorithm == null) throw new ServiceException("不支持的文件摘要算法：" + type);
        try
        {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            try (InputStream in = Files.newInputStream(file); DigestInputStream digestIn = new DigestInputStream(in, md))
            {
                byte[] buffer = new byte[8192];
                while (digestIn.read(buffer) >= 0) { }
            }
            return Hex.encodeHexString(md.digest());
        }
        catch (IOException e) { throw e; }
        catch (Exception e) { throw new IOException("计算文件摘要失败", e); }
    }
    private String truncate(String value, int max) { return value != null && value.length() > max ? value.substring(0, max) : value; }
}

