package com.ruoyi.business.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URLDecoder;
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
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
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
    private static final int LOGIN_PREVIEW_MAX_EDGE = 1600;
    private static final float LOGIN_PREVIEW_JPEG_QUALITY = 0.78f;
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
        mimes.put("pdf", new HashSet<>(Arrays.asList("application/pdf", "application/octet-stream")));
        mimes.put("doc", new HashSet<>(Arrays.asList("application/msword", "application/octet-stream")));
        mimes.put("docx", new HashSet<>(Arrays.asList("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/octet-stream")));
        mimes.put("xls", new HashSet<>(Arrays.asList("application/vnd.ms-excel", "application/octet-stream")));
        mimes.put("xlsx", new HashSet<>(Arrays.asList("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/octet-stream")));
        mimes.put("ppt", new HashSet<>(Arrays.asList("application/vnd.ms-powerpoint", "application/octet-stream")));
        mimes.put("pptx", new HashSet<>(Arrays.asList("application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/octet-stream")));
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
            throw new ServiceException("文件类型不正确，请上传支持的附件");
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
        // 原图用于教研正文和下载，登录展厅固定读取独立展示图，避免把高分辨率原图带入登录链路。
        resolveLoginPreviewImage(target);
        return "/profile/upload/research-activity/images/" + relative.toString().replace('\\', '/');
    }

    /**
     * 获取登录展厅使用的 JPEG 展示图。历史图片首次展示时补生成，新上传图片在上传后已生成；
     * 原图无法由 JDK 解码（例如未安装 WebP 解码器）时保留原图展示，不能影响教研图片上传。
     */
    public Path resolveLoginPreviewImage(Path source)
    {
        if (source == null || !Files.isRegularFile(source)) return source;
        Path imageRoot = imageRoot();
        Path normalizedSource = source.toAbsolutePath().normalize();
        if (!normalizedSource.startsWith(imageRoot)) return source;

        Path relative = imageRoot.relativize(normalizedSource);
        String previewName = FilenameUtils.getBaseName(relative.getFileName().toString()) + ".jpg";
        Path preview = loginPreviewRoot().resolve(relative).resolveSibling(previewName).normalize();
        if (!preview.startsWith(loginPreviewRoot())) return source;
        if (Files.isRegularFile(preview)) return preview;

        synchronized (this)
        {
            if (Files.isRegularFile(preview)) return preview;
            try
            {
                writeLoginPreview(normalizedSource, preview);
                return preview;
            }
            catch (Exception e)
            {
                // 展示图生成失败不得让正常教研上传或已有内容失效，接口会临时回退读取原图。
                log.warn("教研登录展示图生成失败 file={}", relative);
                return source;
            }
        }
    }

    public Path resolvePackagePath(String relativePath)
    {
        if (StringUtils.isBlank(relativePath)) throw new ServiceException("资源路径为空");
        Path root = packageRoot();
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) throw new ServiceException("资源路径非法");
        return target;
    }

    /**
     * 将富文本中允许的教研图片 URL 映射到本地文件。
     * 公开通知只能借此读取正文已经引用的图片，不能把整个上传目录暴露为匿名资源。
     */
    public Path resolvePublicImagePath(String imageUrl)
    {
        String resourcePath = unwrapResourceViewUrl(imageUrl);
        if (StringUtils.isBlank(resourcePath) || resourcePath.indexOf('?') >= 0 || resourcePath.indexOf('#') >= 0)
        {
            throw new ServiceException("通知图片不存在或已失效", 404);
        }
        String relative = resourcePath;
        String[] prefixes = {
                "/profile/upload/research-activity/images/",
                "/dev-api/profile/upload/research-activity/images/",
                "/prod-api/profile/upload/research-activity/images/" };
        boolean matched = false;
        for (String prefix : prefixes)
        {
            if (relative.startsWith(prefix))
            {
                relative = relative.substring(prefix.length());
                matched = true;
                break;
            }
        }
        if (!matched || StringUtils.isBlank(relative)) throw new ServiceException("通知图片不存在或已失效", 404);

        Path root = imageRoot();
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root) || !Files.isRegularFile(target) || !isPublicImage(target))
        {
            throw new ServiceException("通知图片不存在或已失效", 404);
        }
        return target;
    }

    private String unwrapResourceViewUrl(String imageUrl)
    {
        if (StringUtils.isBlank(imageUrl)) return imageUrl;
        if (imageUrl.startsWith("/profile/upload/research-activity/images/")
                || imageUrl.startsWith("/dev-api/profile/upload/research-activity/images/")
                || imageUrl.startsWith("/prod-api/profile/upload/research-activity/images/"))
        {
            return imageUrl;
        }

        try
        {
            URI uri = URI.create(imageUrl);
            String path = uri.getPath();
            if (!"/common/resource/view".equals(path)
                    && !"/dev-api/common/resource/view".equals(path)
                    && !"/prod-api/common/resource/view".equals(path))
            {
                return imageUrl;
            }
            String rawQuery = uri.getRawQuery();
            if (StringUtils.isBlank(rawQuery)) return null;
            String resource = null;
            for (String item : rawQuery.split("&"))
            {
                int separator = item.indexOf('=');
                String name = separator < 0 ? item : item.substring(0, separator);
                if (!"resource".equals(URLDecoder.decode(name, "UTF-8"))) continue;
                if (resource != null || separator < 0) return null;
                resource = URLDecoder.decode(item.substring(separator + 1), "UTF-8");
            }
            return resource;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public void deleteQuietly(String relativePath)
    {
        if (StringUtils.isBlank(relativePath)) return;
        try
        {
            Path source = resolvePackagePath(relativePath);
            Files.deleteIfExists(source);
            Path preview = source.resolveSibling(source.getFileName() + ".preview");
            Files.deleteIfExists(preview.resolve(FilenameUtils.getBaseName(source.toString()) + ".pdf"));
            Files.deleteIfExists(preview);
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
        if ("docx".equals(extension) || "xlsx".equals(extension) || "pptx".equals(extension))
            return startsWith(head, new int[]{0x50, 0x4B}) && hasOfficePackage(file, extension);
        if ("pdf".equals(extension)) return startsWith(head, new int[]{0x25, 0x50, 0x44, 0x46});
        if ("doc".equals(extension) || "xls".equals(extension) || "ppt".equals(extension)) return startsWith(head, new int[]{0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1});
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

    private boolean hasOfficePackage(MultipartFile file, String extension)
    {
        String expected = "docx".equals(extension) ? "word/document.xml"
                : "xlsx".equals(extension) ? "xl/workbook.xml" : "ppt/presentation.xml";
        boolean contentTypes = false;
        boolean document = false;
        // 有界读取 ZIP 目录与内容，避免把改后缀的压缩包或解压炸弹送去 Office 转换。
        try (java.util.zip.ZipInputStream zip = new java.util.zip.ZipInputStream(file.getInputStream()))
        {
            java.util.zip.ZipEntry entry;
            byte[] buffer = new byte[8192];
            long bytes = 0;
            int count = 0;
            while ((entry = zip.getNextEntry()) != null)
            {
                if (++count > 2000) return false;
                contentTypes |= "[Content_Types].xml".equals(entry.getName());
                document |= expected.equals(entry.getName());
                int read;
                while ((read = zip.read(buffer)) != -1)
                    if ((bytes += read) > 100L * 1024 * 1024) return false;
            }
            return contentTypes && document;
        }
        catch (IOException e) { return false; }
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
            try { Files.deleteIfExists(target); } catch (IOException ignored) { /* 保留原始上传失败原因。 */ }
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

    private Path loginPreviewRoot()
    {
        return Paths.get(profile, "upload", "research-activity", "login-previews").toAbsolutePath().normalize();
    }

    private void writeLoginPreview(Path source, Path target) throws IOException
    {
        BufferedImage original = ImageIO.read(source.toFile());
        if (original == null) throw new IOException("不支持的图片解码格式");

        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        int longestEdge = Math.max(originalWidth, originalHeight);
        double scale = longestEdge > LOGIN_PREVIEW_MAX_EDGE
                ? (double) LOGIN_PREVIEW_MAX_EDGE / longestEdge : 1D;
        int width = Math.max(1, (int) Math.round(originalWidth * scale));
        int height = Math.max(1, (int) Math.round(originalHeight * scale));
        BufferedImage preview = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = preview.createGraphics();
        try
        {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(original, 0, 0, width, height, null);
        }
        finally
        {
            graphics.dispose();
        }

        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
        try
        {
            writeJpeg(preview, temporary);
            try
            {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (java.nio.file.AtomicMoveNotSupportedException ignored)
            {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        finally
        {
            Files.deleteIfExists(temporary);
        }
    }

    private void writeJpeg(BufferedImage image, Path target) throws IOException
    {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) throw new IOException("缺少 JPEG 编码器");
        ImageWriter writer = writers.next();
        try (OutputStream output = Files.newOutputStream(target);
             ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output))
        {
            writer.setOutput(imageOutput);
            ImageWriteParam parameter = writer.getDefaultWriteParam();
            if (parameter.canWriteCompressed())
            {
                parameter.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                parameter.setCompressionQuality(LOGIN_PREVIEW_JPEG_QUALITY);
            }
            writer.write(null, new IIOImage(image, null, null), parameter);
        }
        finally
        {
            writer.dispose();
        }
    }

    private String extension(MultipartFile file)
    {
        return FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
    }

    private String normalizeMime(String mime)
    {
        return mime == null ? "" : mime.toLowerCase(Locale.ROOT).trim();
    }

    private boolean isPublicImage(Path path)
    {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp");
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
