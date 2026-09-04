package com.ruoyi.business.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.CollaborationRoom;
import com.ruoyi.business.mapper.CollaborationMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/** CryptPad onSave 的平台落盘实现，使用版本号 CAS 防止并发保存互相覆盖。 */
@Service
public class CryptPadDocumentService
{
    @Autowired private CollaborationRoomService roomService;
    @Autowired private CollaborationMapper mapper;
    @Autowired private CollaborationSecretService secretService;
    @Autowired private CollaborationRevisionDiffService revisionDiffService;

    @Value("${collaboration.provider:}")
    private String provider;

    @Value("${collaboration.cryptpad.max-file-bytes:52428800}")
    private long maxFileBytes;

    public Path currentFile(Long roomId)
    {
        CollaborationRoom room = authorizedRoom(roomId);
        Path file = roomService.resolveStoredFile(room.getCurrentFilePath());
        if (!Files.isRegularFile(file)) throw new ServiceException("协作文档文件不存在");
        return file;
    }

    public String currentFileName(Long roomId)
    {
        return authorizedRoom(roomId).getCurrentFileName();
    }

    public Map<String, Object> save(Long roomId, MultipartFile upload, Integer expectedVersion) throws IOException
    {
        CollaborationRoom room = authorizedRoom(roomId);
        mapper.insertOperationEvent(roomId, SecurityUtils.getUserId(), mapper.selectStudentIdByUserId(SecurityUtils.getUserId()),
                "SAVE_TRIGGER", null, new Date());
        if (!"OPEN".equals(room.getStatus())) throw new ServiceException("协作文档当前不可编辑");
        if (upload == null || upload.isEmpty()) throw new ServiceException("保存文件不能为空");
        if (upload.getSize() > maxFileBytes) throw new ServiceException("保存文件超过在线协作大小限制");
        if (expectedVersion == null || !expectedVersion.equals(room.getCurrentVersion()))
            throw new ServiceException("协作文档版本已变化，请刷新后重试");
        String extension = extension(upload.getOriginalFilename());
        if (!StringUtils.equalsIgnoreCase(extension, room.getCurrentFileExtension()))
            throw new ServiceException("保存文件类型与房间起始文件不一致");

        String token = UUID.randomUUID().toString().replace("-", "");
        String path = "collaboration/rooms/" + room.getPublicFileId() + "/v"
                + (room.getCurrentVersion() + 1) + "-" + token.substring(0, 8) + "." + extension;
        Path target = roomService.resolveStoredFile(path);
        Files.createDirectories(target.getParent());
        Path temp = roomService.resolveStoredFile("collaboration/temp/cryptpad-" + token + ".upload");
        Files.createDirectories(temp.getParent());
        try
        {
            try (InputStream input = upload.getInputStream())
            {
                Files.copy(input, temp, StandardCopyOption.REPLACE_EXISTING);
            }
            String sha256 = digest(temp);
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            Date now = new Date();
            int nextVersion = room.getCurrentVersion() + 1;
            if (mapper.commitRoomVersion(roomId, room.getCurrentVersion(), nextVersion,
                    safeName(upload.getOriginalFilename(), room.getCurrentFileName()), path, extension,
                    upload.getContentType(), Files.size(target), sha256, SecurityUtils.getUserId(), now) != 1)
                throw new ServiceException("协作文档发生并发保存冲突，请刷新后重试");
            mapper.insertRevision(roomId, nextVersion, safeName(upload.getOriginalFilename(), room.getCurrentFileName()),
                    path, Files.size(target), sha256, "sha256", sha256, false, SecurityUtils.getUserId(), now);
            mapper.insertOperationEvent(roomId, SecurityUtils.getUserId(), mapper.selectStudentIdByUserId(SecurityUtils.getUserId()),
                    "SAVE_SUCCESS", "version=" + nextVersion, now);
            // 差异提取只能丰富课堂审计，不能拖慢或否决已成功的协作文档保存。
            try
            {
                revisionDiffService.pending(roomId, nextVersion);
                revisionDiffService.extract(roomId, nextVersion);
            }
            catch (Exception ignored)
            {
                // 审计差异任务不可用时仍以已保存的协作文档版本为准。
            }
            CollaborationRoom updated = roomService.requireRoom(roomId);
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("roomId", roomId);
            result.put("version", updated.getCurrentVersion());
            result.put("fileName", updated.getCurrentFileName());
            result.put("fileSize", updated.getCurrentFileSize());
            result.put("lastSaveTime", updated.getLastSaveTime());
            return result;
        }
        finally
        {
            Files.deleteIfExists(temp);
            if (Files.exists(target) && room.getCurrentVersion().equals(expectedVersion)
                    && mapperRoomStillAtVersion(roomId, expectedVersion)) Files.deleteIfExists(target);
        }
    }

    public void rotateKey(Long roomId)
    {
        CollaborationRoom room = roomService.requireRoom(roomId);
        roomService.assertRoomAccess(roomId);
        if (!"CRYPTPAD".equalsIgnoreCase(room.getProvider())) throw new ServiceException("当前房间不是 CryptPad 房间");
        // 只有教师或管理员可以轮换；学生的 assertRoomAccess 只负责班级事实。
        if (!SecurityUtils.isAdmin(SecurityUtils.getUserId())) roomService.teacherSettings(room.getLessonId());
        mapper.updateRoomProvider(roomId, "CRYPTPAD", secretService.encrypt(secretService.generateKey()));
    }

    private CollaborationRoom authorizedRoom(Long roomId)
    {
        CollaborationRoom room = roomService.requireRoom(roomId);
        roomService.assertRoomAccess(roomId);
        if (!"CRYPTPAD".equalsIgnoreCase(StringUtils.defaultString(room.getProvider()))
                && !"MOCK".equalsIgnoreCase(StringUtils.defaultString(provider)))
            throw new ServiceException("当前房间未使用 CryptPad");
        return room;
    }

    private boolean mapperRoomStillAtVersion(Long roomId, Integer expectedVersion)
    {
        CollaborationRoom current = roomService.requireRoom(roomId);
        return current != null && expectedVersion.equals(current.getCurrentVersion());
    }

    private String extension(String name)
    {
        String value = StringUtils.defaultString(name).toLowerCase();
        int dot = value.lastIndexOf('.');
        if (dot < 1 || dot == value.length() - 1) throw new ServiceException("保存文件缺少扩展名");
        return value.substring(dot + 1).replaceAll("[^a-z0-9]", "");
    }

    private String safeName(String name, String fallback)
    {
        String value = StringUtils.defaultIfBlank(name, fallback).replace('\\', '_').replace('/', '_');
        return value.length() > 240 ? value.substring(value.length() - 240) : value;
    }

    private String digest(Path file) throws IOException
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(file); DigestInputStream digest = new DigestInputStream(input, md))
            {
                byte[] buffer = new byte[8192];
                while (digest.read(buffer) >= 0) { }
            }
            return Hex.encodeHexString(md.digest());
        }
        catch (IOException e) { throw e; }
        catch (Exception e) { throw new IOException("计算协作文档摘要失败", e); }
    }
}
