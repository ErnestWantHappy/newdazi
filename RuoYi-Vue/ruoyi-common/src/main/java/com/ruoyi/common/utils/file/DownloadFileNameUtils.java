package com.ruoyi.common.utils.file;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * 下载文件名规范化工具。
 *
 * <p>磁盘存储名继续保持 UUID/序列号，只有返回给用户的名称在这里统一处理，
 * 避免为了显示名称改动既有文件路径和历史数据。</p>
 */
public final class DownloadFileNameUtils
{
    private static final int MAX_FILE_NAME_LENGTH = 120;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Pattern INVALID_CHARACTERS = Pattern.compile("[\\\\/:*?\"<>|\\p{Cntrl}]");
    private static final Pattern UPLOAD_SEQUENCE = Pattern.compile("_[0-9]{14}[A-Za-z][0-9]{3}$");
    private static final Pattern UUID_PREFIX = Pattern.compile("^[0-9a-fA-F-]{32,36}_");
    private static final Pattern UUID_ONLY = Pattern.compile("^[0-9a-fA-F-]{32,36}$");

    private DownloadFileNameUtils()
    {
    }

    /**
     * 给业务文件名追加统一时间，扩展名保持在最后。
     */
    public static String withTimestamp(String fileName)
    {
        return withTimestamp(fileName, LocalDateTime.now());
    }

    public static String withTimestamp(String fileName, LocalDateTime now)
    {
        String normalized = sanitize(fileName, "下载文件");
        String extension = extension(normalized);
        String baseName = baseName(normalized);
        return sanitize(baseName + "_" + TIMESTAMP_FORMAT.format(now) + extension, "下载文件");
    }

    /**
     * 组合业务前缀和原文件名，保留原扩展名。
     */
    public static String withBusinessPrefix(String prefix, String originalFileName)
    {
        String original = sanitize(originalFileName, "附件");
        return sanitize(sanitize(prefix, "附件") + "_" + original, "附件");
    }

    /**
     * 从若依上传路径恢复原始展示名；UUID 存储名无法恢复时使用明确的附件名称。
     */
    public static String fromStoredPath(String storedPath)
    {
        return fromStoredPath(storedPath, LocalDateTime.now());
    }

    public static String fromStoredPath(String storedPath, LocalDateTime now)
    {
        String leaf = decodeLeaf(storedPath);
        String extension = extension(leaf);
        String baseName = baseName(leaf);
        baseName = UPLOAD_SEQUENCE.matcher(baseName).replaceFirst("");
        baseName = UUID_PREFIX.matcher(baseName).replaceFirst("");
        if (baseName.trim().isEmpty() || UUID_ONLY.matcher(baseName).matches())
        {
            return sanitize("附件_" + TIMESTAMP_FORMAT.format(now) + extension, "附件");
        }
        return sanitize(baseName + extension, "附件");
    }

    /**
     * 清除路径、控制字符和 Windows 非法字符，并限制最终名称长度。
     */
    public static String sanitize(String fileName, String fallback)
    {
        String safeFallback = simpleFallback(fallback);
        String value = decodeLeaf(fileName);
        value = Normalizer.normalize(value, Normalizer.Form.NFC);
        value = INVALID_CHARACTERS.matcher(value).replaceAll("_");
        value = value.replaceAll("\\s+", " ").replaceAll("[. ]+$", "").trim();
        if (value.isEmpty() || ".".equals(value) || "..".equals(value))
        {
            value = safeFallback;
        }
        if (value.length() <= MAX_FILE_NAME_LENGTH)
        {
            return value;
        }
        String extension = extension(value);
        String baseName = baseName(value);
        int baseLimit = Math.max(1, MAX_FILE_NAME_LENGTH - extension.length());
        return baseName.substring(0, Math.min(baseName.length(), baseLimit)) + extension;
    }

    private static String simpleFallback(String fallback)
    {
        String value = fallback == null ? "下载文件" : fallback;
        value = INVALID_CHARACTERS.matcher(value).replaceAll("_").trim();
        return value.isEmpty() ? "下载文件" : value;
    }

    private static String decodeLeaf(String path)
    {
        if (path == null)
        {
            return "";
        }
        String value = path.trim().replace('\\', '/');
        int slashIndex = value.lastIndexOf('/');
        if (slashIndex >= 0)
        {
            value = value.substring(slashIndex + 1);
        }
        try
        {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        }
        catch (Exception ignored)
        {
            return value;
        }
    }

    private static String extension(String fileName)
    {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 && dotIndex < fileName.length() - 1 ? fileName.substring(dotIndex) : "";
    }

    private static String baseName(String fileName)
    {
        String extension = extension(fileName);
        return extension.isEmpty() ? fileName : fileName.substring(0, fileName.length() - extension.length());
    }
}
