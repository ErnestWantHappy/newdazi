package com.ruoyi.business.service;

import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.ruoyi.business.domain.BizGuideSheetAnswer;
import com.ruoyi.business.domain.BizGuideSheetProgress;
import com.ruoyi.business.domain.BizGuideSheetUpload;
import com.ruoyi.business.domain.BizLessonGuideSheetBinding;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.GuideSheetUploadMapper;
import com.ruoyi.business.mapper.GuideSheetProgressMapper;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 导学单文件统一落到平台受控目录，客户端只提供文件和幂等键。
 */
@Service
public class GuideSheetUploadService
{
    private static final long MAX_FILE_SIZE = 50L * 1024L * 1024L;
    private static final String[] ALLOWED_EXTENSIONS = {
            "png", "jpg", "jpeg", "gif", "webp", "pdf", "doc", "docx",
            "xls", "xlsx", "ppt", "pptx", "zip", "txt", "mp4"
    };
    private static final Map<String, Set<String>> ALLOWED_MIME_TYPES = buildMimeTypes();

    @Autowired
    private GuideSheetUploadMapper uploadMapper;

    @Autowired
    private GuideSheetProgressMapper progressMapper;

    @Autowired
    private GuideSheetAccessService accessService;

    @Autowired
    private GuideSheetStudentViewService studentViewService;

    @Autowired
    private IGuideSheetAnswerService answerService;

    public BizGuideSheetUpload upload(Long bindingId, String questionName,
                                      String clientUploadId, MultipartFile file)
    {
        BizStudent student = accessService.requireCurrentStudent();
        BizLessonGuideSheetBinding binding = accessService.requireStudentBinding(student, bindingId);
        validateIdentifiers(questionName, clientUploadId);
        if (!studentViewService.isUploadField(binding.getSnapshotFormJson(), questionName))
        {
            throw new ServiceException("上传字段不属于当前课程导学单");
        }

        BizGuideSheetUpload existing = uploadMapper.selectByClientUploadId(
                bindingId, student.getStudentId(), clientUploadId);
        if (existing != null)
        {
            recordUploadActivity(student, bindingId);
            existing.setAccessUrl(buildStudentAccessUrl(existing));
            return existing;
        }
        validateFile(file);
        recordUploadActivity(student, bindingId);

        String storedPath = null;
        try
        {
            storedPath = storePrivateFile(bindingId, student.getStudentId(), file);
            BizGuideSheetAnswer answer = answerService.getByStudentAndBinding(
                    student.getStudentId(), bindingId);
            BizGuideSheetUpload upload = new BizGuideSheetUpload();
            upload.setAnswerId(answer == null ? null : answer.getAnswerId());
            upload.setBindingId(bindingId);
            upload.setSourceSheetId(binding.getSourceSheetId());
            upload.setStudentId(student.getStudentId());
            upload.setQuestionName(questionName);
            upload.setFileName(safeOriginalFilename(file));
            upload.setFileSize(file.getSize());
            upload.setMimeType(normalizeMime(file.getContentType()));
            upload.setTeacherMachineIp(null);
            upload.setStoredPath(storedPath);
            upload.setClientUploadId(clientUploadId);
            upload.setAccessUrl(buildStudentAccessUrl(upload));
            upload.setUploadTime(new Date());
            try
            {
                uploadMapper.insertBizGuideSheetUpload(upload);
                return upload;
            }
            catch (DuplicateKeyException e)
            {
                BizGuideSheetUpload concurrent = uploadMapper.selectByClientUploadId(
                        bindingId, student.getStudentId(), clientUploadId);
                if (concurrent == null)
                {
                    throw e;
                }
                deleteOrphan(storedPath);
                concurrent.setAccessUrl(buildStudentAccessUrl(concurrent));
                return concurrent;
            }
        }
        catch (ServiceException e)
        {
            deleteOrphan(storedPath);
            throw e;
        }
        catch (Exception e)
        {
            deleteOrphan(storedPath);
            throw new ServiceException("导学单文件上传失败，请检查文件后重试");
        }
    }

    private void recordUploadActivity(BizStudent student, Long bindingId)
    {
        BizGuideSheetProgress progress = new BizGuideSheetProgress();
        progress.setBindingId(bindingId);
        progress.setStudentId(student.getStudentId());
        progress.setDeptId(student.getDeptId() != null
                ? student.getDeptId() : com.ruoyi.common.utils.SecurityUtils.getDeptId());
        progress.setEntryYear(student.getEntryYear());
        progress.setClassCode(student.getClassCode());
        progress.setLastHeartbeat(new Date());
        progressMapper.insertStartedIfAbsent(progress);
    }

    public DownloadResource requireStudentDownload(Long bindingId, String clientUploadId)
    {
        BizStudent student = accessService.requireCurrentStudent();
        accessService.requireStudentBinding(student, bindingId);
        BizGuideSheetUpload upload = uploadMapper.selectByClientUploadId(
                bindingId, student.getStudentId(), clientUploadId);
        if (upload == null)
        {
            throw new ServiceException("上传文件不存在或不属于当前学生");
        }
        return toDownloadResource(upload);
    }

    public DownloadResource requireTeacherDownload(Long uploadId, String entryYear, String classCode)
    {
        BizGuideSheetUpload upload = uploadMapper.selectByUploadId(uploadId);
        if (upload == null)
        {
            throw new ServiceException("上传文件不存在");
        }
        accessService.requireBindingClassAccess(upload.getBindingId(), entryYear, classCode);
        accessService.assertStudentInBindingClass(upload.getBindingId(), upload.getStudentId(),
                com.ruoyi.common.utils.SecurityUtils.getDeptId(), entryYear, classCode);
        return toDownloadResource(upload);
    }

    private String storePrivateFile(Long bindingId, Long studentId, MultipartFile file) throws Exception
    {
        String extension = FileUploadUtils.getExtension(file).toLowerCase(Locale.ROOT);
        String relativePath = bindingId + "/" + studentId + "/" + DateUtils.datePath()
                + "/" + IdUtils.fastSimpleUUID() + "." + extension;
        Path root = privateStorageRoot();
        Path target = root.resolve(relativePath).toAbsolutePath().normalize();
        if (!target.startsWith(root))
        {
            throw new ServiceException("导学单文件存储路径无效");
        }
        Files.createDirectories(target.getParent());
        file.transferTo(target.toFile());
        return relativePath.replace(File.separatorChar, '/');
    }

    private String buildStudentAccessUrl(BizGuideSheetUpload upload)
    {
        return "/business/guide-sheet/student/uploads/" + upload.getBindingId()
                + "/" + upload.getClientUploadId();
    }

    private DownloadResource toDownloadResource(BizGuideSheetUpload upload)
    {
        Path file = resolveStoredFile(upload.getStoredPath());
        if (!Files.isRegularFile(file))
        {
            throw new ServiceException("上传文件已不存在，请联系管理员");
        }
        return new DownloadResource(file, upload.getFileName(), upload.getMimeType());
    }

    private Path resolveStoredFile(String storedPath)
    {
        if (storedPath == null || storedPath.trim().isEmpty())
        {
            throw new ServiceException("上传文件存储记录无效");
        }
        String normalizedPath = storedPath.replace('\\', '/');
        Path root;
        String relativePath;
        if (normalizedPath.startsWith("/profile/upload/guide-sheet/"))
        {
            root = Paths.get(RuoYiConfig.getProfile(), "upload", "guide-sheet")
                    .toAbsolutePath().normalize();
            relativePath = normalizedPath.substring("/profile/upload/guide-sheet/".length());
        }
        else
        {
            root = privateStorageRoot();
            relativePath = normalizedPath;
        }
        Path target = root.resolve(relativePath).toAbsolutePath().normalize();
        if (!target.startsWith(root))
        {
            throw new ServiceException("上传文件存储路径无效");
        }
        return target;
    }

    private Path privateStorageRoot()
    {
        Path profile = Paths.get(RuoYiConfig.getProfile()).toAbsolutePath().normalize();
        return Paths.get(profile.toString() + "-private", "guide-sheet")
                .toAbsolutePath().normalize();
    }

    private void validateIdentifiers(String questionName, String clientUploadId)
    {
        if (questionName == null || !questionName.matches("[\\p{L}\\p{N}_-]{1,128}"))
        {
            throw new ServiceException("上传字段标识无效");
        }
        if (clientUploadId == null || !clientUploadId.matches("[A-Za-z0-9_-]{8,64}"))
        {
            throw new ServiceException("上传幂等标识无效");
        }
    }

    private void validateFile(MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("请选择要上传的文件");
        }
        if (file.getSize() > MAX_FILE_SIZE)
        {
            throw new ServiceException("上传文件不能超过50MB");
        }
        String extension = FileUploadUtils.getExtension(file).toLowerCase(Locale.ROOT);
        Set<String> allowedMimeTypes = ALLOWED_MIME_TYPES.get(extension);
        String mimeType = normalizeMime(file.getContentType());
        if (allowedMimeTypes == null || !allowedMimeTypes.contains(mimeType))
        {
            throw new ServiceException("文件类型与扩展名不匹配或不受支持");
        }
        if (!hasExpectedSignature(file, extension))
        {
            throw new ServiceException("文件内容与扩展名不匹配");
        }
    }

    private boolean hasExpectedSignature(MultipartFile file, String extension)
    {
        byte[] header = new byte[12];
        int length;
        try (InputStream input = file.getInputStream())
        {
            length = input.read(header);
        }
        catch (Exception e)
        {
            return false;
        }
        if ("txt".equals(extension)) return !containsZeroByte(header, length);
        if ("pdf".equals(extension)) return startsWith(header, length, "%PDF".getBytes());
        if ("png".equals(extension)) return startsWith(header, length,
                new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47});
        if ("jpg".equals(extension) || "jpeg".equals(extension)) return startsWith(header, length,
                new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        if ("gif".equals(extension)) return startsWith(header, length, "GIF8".getBytes());
        if ("webp".equals(extension)) return startsWith(header, length, "RIFF".getBytes())
                && length >= 12 && header[8] == 'W' && header[9] == 'E'
                && header[10] == 'B' && header[11] == 'P';
        if ("doc".equals(extension) || "xls".equals(extension) || "ppt".equals(extension))
        {
            return startsWith(header, length,
                    new byte[] {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0});
        }
        if ("docx".equals(extension) || "xlsx".equals(extension)
                || "pptx".equals(extension) || "zip".equals(extension))
        {
            return startsWith(header, length, new byte[] {0x50, 0x4B});
        }
        return "mp4".equals(extension) && length >= 8
                && header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p';
    }

    private boolean startsWith(byte[] source, int length, byte[] prefix)
    {
        if (length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++)
        {
            if (source[i] != prefix[i]) return false;
        }
        return true;
    }

    private boolean containsZeroByte(byte[] source, int length)
    {
        for (int i = 0; i < length; i++)
        {
            if (source[i] == 0) return true;
        }
        return false;
    }

    private String safeOriginalFilename(MultipartFile file)
    {
        String name = FilenameUtils.getName(file.getOriginalFilename());
        return name == null ? "file" : name.replaceAll("[\\r\\n\\t]", "_");
    }

    private String normalizeMime(String mimeType)
    {
        return mimeType == null ? "" : mimeType.trim().toLowerCase(Locale.ROOT);
    }

    private void deleteOrphan(String storedPath)
    {
        if (storedPath == null) return;
        try
        {
            Path target = resolveStoredFile(storedPath);
            Files.deleteIfExists(target);
        }
        catch (Exception ignored)
        {
            // 数据库已拒绝记录时，残余文件由运维清理，不覆盖原始业务异常。
        }
    }

    private static Map<String, Set<String>> buildMimeTypes()
    {
        Map<String, Set<String>> result = new HashMap<>();
        result.put("png", set("image/png"));
        result.put("jpg", set("image/jpeg", "image/jpg"));
        result.put("jpeg", set("image/jpeg", "image/jpg"));
        result.put("gif", set("image/gif"));
        result.put("webp", set("image/webp"));
        result.put("pdf", set("application/pdf"));
        result.put("doc", set("application/msword", "application/octet-stream"));
        result.put("docx", set("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/zip", "application/octet-stream"));
        result.put("xls", set("application/vnd.ms-excel", "application/octet-stream"));
        result.put("xlsx", set("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/zip", "application/octet-stream"));
        result.put("ppt", set("application/vnd.ms-powerpoint", "application/octet-stream"));
        result.put("pptx", set("application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/zip", "application/octet-stream"));
        result.put("zip", set("application/zip", "application/x-zip-compressed", "application/octet-stream"));
        result.put("txt", set("text/plain"));
        result.put("mp4", set("video/mp4"));
        return Collections.unmodifiableMap(result);
    }

    private static Set<String> set(String... values)
    {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(values)));
    }

    public static class DownloadResource
    {
        private final Path path;
        private final String fileName;
        private final String mimeType;

        DownloadResource(Path path, String fileName, String mimeType)
        {
            this.path = path;
            this.fileName = fileName;
            this.mimeType = mimeType;
        }

        public Path getPath() { return path; }
        public String getFileName() { return fileName; }
        public String getMimeType() { return mimeType; }
    }
}
