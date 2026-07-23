package com.ruoyi.business.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.constant.ResearchActivityConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/** 教研活动课件与富文本图片的专用校验、私有存储和安全路径解析。 */
@Service
public class ResearchActivityUploadService
{
    private static final Logger log = LoggerFactory.getLogger(ResearchActivityUploadService.class);
    private static final Map<String, Set<String>> PACKAGE_MIMES;
    private static final Set<String> IMAGE_MIMES = new HashSet<>(
            Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp"));

    static
    {
        Map<String, Set<String>> mimes = new HashMap<>();
        mimes.put("zip", new HashSet<>(Arrays.asList(
                "application/zip", "application/x-zip-compressed", "application/octet-stream")));
        mimes.put("rar", new HashSet<>(Arrays.asList(
                "application/vnd.rar", "application/x-rar-compressed", "application/octet-stream")));
        mimes.put("7z", new HashSet<>(Arrays.asList(
                "application/x-7z-compressed", "application/octet-stream")));
        PACKAGE_MIMES = Collections.unmodifiableMap(mimes);
    }

    @Value("${ruoyi.profile}")
    private String profile;

    public void validatePackage(MultipartFile file)
    {
        if (file == null || file.isEmpty()) throw new ServiceException("请选择主课件文件");
        if (file.getSize() > ResearchActivityConstants.MAX_PACKAGE_BYTES)
        {
            throw new ServiceException("文件超过50MB，请改用云盘链接");
        }
        String originalName = FilenameUtils.getName(file.getOriginalFilename());
        if (StringUtils.isBlank(originalName) || originalName.length() > 255)
        {
            throw new ServiceException("文件名不能为空且不能超过255个字符");
        }
        String extension = extension(file);
        Set<String> allowedMimes = PACKAGE_MIMES.get(extension);
        String contentType = normalizeMime(file.getContentType());
        if (allowedMimes == null || !allowedMimes.contains(contentType) || !hasPackageSignature(file, extension))
        {
            throw new ServiceException("文件类型不正确，请重新压缩后上传");
        }
    }

    public void validateImage(MultipartFile file)
    {
        if (file == null || file.isEmpty()) throw new ServiceException("请选择图片");
        if (file.getSize() > ResearchActivityConstants.MAX_IMAGE_BYTES)
        {
            throw new ServiceException("单张图片不能超过10MB");
        }
        String extension = extension(file);
        String mime = normalizeMime(file.getContentType());
        boolean extensionAllowed = "jpg".equals(extension) || "jpeg".equals(extension)
                || "png".equals(extension) || "webp".equals(extension);
        if (!extensionAllowed || !IMAGE_MIMES.contains(mime) || !hasImageSignature(file, extension))
        {
            throw new ServiceException("图片格式不正确，仅支持JPG、PNG和WebP");
        }
    }

    public StoredFile storePackage(MultipartFile file, Long topicId, Long postId)
    {
        validatePackage(file);
        String extension = extension(file);
        LocalDate today = LocalDate.now();
        Path relative = Paths.get(String.valueOf(topicId), String.valueOf(postId),
                String.valueOf(today.getYear()), two(today.getMonthValue()), two(today.getDayOfMonth()),
                UUID.randomUUID().toString().replace("-", "") + "." + extension);
        Path target = resolvePackagePath(relative.toString());
        copy(file, target);
        return new StoredFile(FilenameUtils.getName(file.getOriginalFilename()), relative.toString().replace('\\', '/'),
                file.getSize(), normalizeMime(file.getContentType()));
    }

    public String storeImage(MultipartFile file)
    {
        validateImage(file);
        String extension = extension(file);
        LocalDate today = LocalDate.now();
        Path relative = Paths.get(String.valueOf(today.getYear()), two(today.getMonthValue()), two(today.getDayOfMonth()),
                UUID.randomUUID().toString().replace("-", "") + "." + extension);
        Path root = imageRoot();
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root)) throw new ServiceException("图片存储路径非法");
        copy(file, target);
        return "/profile/upload/research-activity/images/" + relative.toString().replace('\\', '/');
    }

    public Path resolvePackagePath(String relativePath)
    {
        if (StringUtils.isBlank(relativePath)) throw new ServiceException("资源路径为空");
        Path root = packageRoot();
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) throw new ServiceException("资源路径非法");
        return target;
    }

    public void deleteQuietly(String relativePath)
    {
        if (StringUtils.isBlank(relativePath)) return;
        try
        {
            Files.deleteIfExists(resolvePackagePath(relativePath));
        }
        catch (Exception e)
        {
            // 只记录数据库相对标识，禁止把服务器绝对路径写入日志。
            log.warn("教研活动资源文件清理失败 resource={}", relativePath);
        }
    }

    private boolean hasPackageSignature(MultipartFile file, String extension)
    {
        byte[] head = readHead(file, 8);
        if ("zip".equals(extension)) return startsWith(head, new int[]{0x50, 0x4B});
        if ("7z".equals(extension)) return startsWith(head, new int[]{0x37, 0x7A, 0xBC, 0xAF, 0x27, 0x1C});
        if ("rar".equals(extension))
        {
            return startsWith(head, new int[]{0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00})
                    || startsWith(head, new int[]{0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00});
        }
        return false;
    }

    private boolean hasImageSignature(MultipartFile file, String extension)
    {
        byte[] head = readHead(file, 12);
        if ("jpg".equals(extension) || "jpeg".equals(extension))
            return startsWith(head, new int[]{0xFF, 0xD8, 0xFF});
        if ("png".equals(extension))
            return startsWith(head, new int[]{0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
        if ("webp".equals(extension))
            return startsWith(head, new int[]{0x52, 0x49, 0x46, 0x46})
                    && head.length >= 12 && head[8] == 0x57 && head[9] == 0x45 && head[10] == 0x42 && head[11] == 0x50;
        return false;
    }

    private byte[] readHead(MultipartFile file, int length)
    {
        try (InputStream input = file.getInputStream())
        {
            byte[] buffer = new byte[length];
            int read = input.read(buffer);
            return read <= 0 ? new byte[0] : Arrays.copyOf(buffer, read);
        }
        catch (IOException e)
        {
            throw new ServiceException("读取上传文件失败");
        }
    }

    private boolean startsWith(byte[] actual, int[] expected)
    {
        if (actual.length < expected.length) return false;
        for (int i = 0; i < expected.length; i++)
        {
            if ((actual[i] & 0xFF) != expected[i]) return false;
        }
        return true;
    }

    private void copy(MultipartFile file, Path target)
    {
        try
        {
            Files.createDirectories(target.getParent());
            try (InputStream input = file.getInputStream())
            {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e)
        {
            throw new ServiceException("保存上传文件失败");
        }
    }

    private Path packageRoot()
    {
        return Paths.get(profile + "-private", "research-activity").toAbsolutePath().normalize();
    }

    private Path imageRoot()
    {
        return Paths.get(profile, "upload", "research-activity", "images").toAbsolutePath().normalize();
    }

    private String extension(MultipartFile file)
    {
        return FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
    }

    private String normalizeMime(String mime)
    {
        return mime == null ? "" : mime.toLowerCase(Locale.ROOT).trim();
    }

    private String two(int value) { return value < 10 ? "0" + value : String.valueOf(value); }

    public static class StoredFile
    {
        private final String originalFileName;
        private final String relativePath;
        private final long fileSize;
        private final String mimeType;

        public StoredFile(String originalFileName, String relativePath, long fileSize, String mimeType)
        {
            this.originalFileName = originalFileName;
            this.relativePath = relativePath;
            this.fileSize = fileSize;
            this.mimeType = mimeType;
        }

        public String getOriginalFileName() { return originalFileName; }
        public String getRelativePath() { return relativePath; }
        public long getFileSize() { return fileSize; }
        public String getMimeType() { return mimeType; }
    }
}
