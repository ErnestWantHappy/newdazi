package com.ruoyi.business.service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.dto.PracticalUploadTicket;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;

/**
 * 操作题文件白名单、MIME 和文件头校验。
 */
@Service
public class PracticalFilePolicyService
{
    public static final String DEFAULT_ALLOWED_EXTENSIONS =
            "doc,docx,pdf,ppt,pptx,xls,xlsx,jpg,jpeg,png";

    private static final Set<String> SUPPORTED_EXTENSIONS = Collections.unmodifiableSet(
            new LinkedHashSet<String>(Arrays.asList(
                    "doc", "docx", "pdf", "ppt", "pptx", "xls", "xlsx", "jpg", "jpeg", "png")));

    private static final Set<String> IMAGE_EXTENSIONS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList("jpg", "jpeg", "png")));

    private static final Set<String> OFFICE_EXTENSIONS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList("doc", "docx", "ppt", "pptx", "xls", "xlsx")));

    private static final Map<String, Set<String>> ALLOWED_MIME_TYPES = buildMimeTypes();

    public PracticalUploadTicket inspect(MultipartFile file, String allowedExtensions)
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("上传文件不能为空");
        }
        if (file.getSize() > FileUploadUtils.DEFAULT_MAX_SIZE)
        {
            throw new ServiceException("单个操作题文件不能超过50 MiB");
        }

        String originalFileName = leafFileName(file.getOriginalFilename());
        if (StringUtils.isBlank(originalFileName) || originalFileName.length() > 255)
        {
            throw new ServiceException("文件名为空或过长");
        }
        String extension = FilenameUtils.getExtension(originalFileName).toLowerCase(Locale.ROOT);
        Set<String> questionAllowed = parseAllowedExtensions(allowedExtensions);
        if (!SUPPORTED_EXTENSIONS.contains(extension) || !questionAllowed.contains(extension))
        {
            throw new ServiceException("当前操作题不允许提交该文件格式");
        }

        validateMimeType(extension, file.getContentType());
        validateSignature(extension, file);

        PracticalUploadTicket ticket = new PracticalUploadTicket();
        ticket.setOriginalFileName(originalFileName);
        ticket.setFileExtension(extension);
        ticket.setFileKind(fileKind(extension));
        ticket.setMimeType(normalizeMimeType(file.getContentType()));
        ticket.setFileSize(file.getSize());
        ticket.setSha256(calculateSha256(file));
        return ticket;
    }

    public Set<String> parseAllowedExtensions(String value)
    {
        String source = StringUtils.isBlank(value) ? DEFAULT_ALLOWED_EXTENSIONS : value;
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        for (String item : source.split(","))
        {
            String extension = item == null ? "" : item.trim().toLowerCase(Locale.ROOT);
            if (SUPPORTED_EXTENSIONS.contains(extension))
            {
                result.add(extension);
            }
        }
        if (result.isEmpty())
        {
            throw new ServiceException("操作题允许提交格式配置无效");
        }
        return result;
    }

    public boolean isImage(String extension)
    {
        return extension != null && IMAGE_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));
    }

    public boolean isOffice(String extension)
    {
        return extension != null && OFFICE_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));
    }

    private void validateMimeType(String extension, String contentType)
    {
        String normalized = normalizeMimeType(contentType);
        if (StringUtils.isBlank(normalized))
        {
            return;
        }
        Set<String> allowed = ALLOWED_MIME_TYPES.get(extension);
        if (allowed == null || !allowed.contains(normalized))
        {
            throw new ServiceException("文件MIME类型与扩展名不匹配");
        }
    }

    private void validateSignature(String extension, MultipartFile file)
    {
        try
        {
            if ("pdf".equals(extension))
            {
                requirePrefix(file, new byte[] { '%', 'P', 'D', 'F', '-' });
                return;
            }
            if ("png".equals(extension))
            {
                requirePrefix(file, new byte[] {
                        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A });
                return;
            }
            if ("jpg".equals(extension) || "jpeg".equals(extension))
            {
                requirePrefix(file, new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF });
                return;
            }
            if ("doc".equals(extension) || "ppt".equals(extension) || "xls".equals(extension))
            {
                requirePrefix(file, new byte[] {
                        (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0,
                        (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1 });
                return;
            }
            validateOpenXmlContainer(extension, file);
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("文件内容无法识别，请重新导出后上传");
        }
    }

    private void requirePrefix(MultipartFile file, byte[] expected) throws Exception
    {
        byte[] actual = new byte[expected.length];
        try (InputStream input = new BufferedInputStream(file.getInputStream()))
        {
            int offset = 0;
            while (offset < actual.length)
            {
                int read = input.read(actual, offset, actual.length - offset);
                if (read < 0) break;
                offset += read;
            }
        }
        if (!Arrays.equals(expected, actual))
        {
            throw new ServiceException("文件内容与扩展名不匹配");
        }
    }

    private void validateOpenXmlContainer(String extension, MultipartFile file) throws Exception
    {
        String requiredEntry;
        if ("docx".equals(extension)) requiredEntry = "word/document.xml";
        else if ("pptx".equals(extension)) requiredEntry = "ppt/presentation.xml";
        else if ("xlsx".equals(extension)) requiredEntry = "xl/workbook.xml";
        else throw new ServiceException("文件格式不支持");

        boolean hasContentTypes = false;
        boolean hasRequiredEntry = false;
        int entryCount = 0;
        long uncompressedSize = 0L;
        byte[] buffer = new byte[8192];
        try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(file.getInputStream())))
        {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null)
            {
                entryCount++;
                if (entryCount > 5000)
                {
                    throw new ServiceException("Office文件内部条目过多");
                }
                int read;
                while ((read = zip.read(buffer)) >= 0)
                {
                    uncompressedSize += read;
                    if (uncompressedSize > 500L * 1024L * 1024L)
                    {
                        throw new ServiceException("Office文件解压后内容过大");
                    }
                }
                String name = entry.getName().replace('\\', '/');
                if ("[Content_Types].xml".equals(name)) hasContentTypes = true;
                if (requiredEntry.equals(name)) hasRequiredEntry = true;
            }
        }
        if (!hasContentTypes || !hasRequiredEntry)
        {
            throw new ServiceException("Office文件结构与扩展名不匹配");
        }
    }

    private String calculateSha256(MultipartFile file)
    {
        try (InputStream input = new BufferedInputStream(file.getInputStream()))
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0)
            {
                if (read > 0) digest.update(buffer, 0, read);
            }
            StringBuilder result = new StringBuilder(64);
            for (byte value : digest.digest())
            {
                result.append(String.format("%02x", value & 0xff));
            }
            return result.toString();
        }
        catch (Exception e)
        {
            throw new ServiceException("文件摘要计算失败，请重试");
        }
    }

    private String leafFileName(String value)
    {
        if (value == null) return "";
        return value.replace('\\', '/').substring(value.replace('\\', '/').lastIndexOf('/') + 1).trim();
    }

    private String fileKind(String extension)
    {
        if (IMAGE_EXTENSIONS.contains(extension)) return "IMAGE";
        if ("pdf".equals(extension)) return "PDF";
        return "OFFICE";
    }

    private String normalizeMimeType(String value)
    {
        if (value == null) return "";
        int separator = value.indexOf(';');
        String normalized = separator >= 0 ? value.substring(0, separator) : value;
        return normalized.trim().toLowerCase(Locale.ROOT);
    }

    private static Map<String, Set<String>> buildMimeTypes()
    {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        result.put("doc", mimeSet("application/msword", "application/vnd.ms-office", "application/octet-stream"));
        result.put("xls", mimeSet("application/vnd.ms-excel", "application/vnd.ms-office", "application/octet-stream"));
        result.put("ppt", mimeSet("application/vnd.ms-powerpoint", "application/vnd.ms-office", "application/octet-stream"));
        result.put("docx", mimeSet("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/zip", "application/octet-stream"));
        result.put("xlsx", mimeSet("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/zip", "application/octet-stream"));
        result.put("pptx", mimeSet("application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/zip", "application/octet-stream"));
        result.put("pdf", mimeSet("application/pdf", "application/octet-stream"));
        result.put("jpg", mimeSet("image/jpeg", "image/jpg"));
        result.put("jpeg", mimeSet("image/jpeg", "image/jpg"));
        result.put("png", mimeSet("image/png"));
        return Collections.unmodifiableMap(result);
    }

    private static Set<String> mimeSet(String... values)
    {
        return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(values)));
    }
}
