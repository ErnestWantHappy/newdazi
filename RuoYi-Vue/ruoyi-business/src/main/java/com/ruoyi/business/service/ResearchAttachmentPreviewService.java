package com.ruoyi.business.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import com.ruoyi.business.utils.FileConversionUtils;
import com.ruoyi.common.exception.ServiceException;

/** 私有附件预览缓存；入口先校验原资源权限，缓存不通过静态目录公开。 */
@Service
public class ResearchAttachmentPreviewService
{
    private final Semaphore conversion = new Semaphore(1);

    public Path preview(Path source) throws IOException
    {
        String extension = FilenameUtils.getExtension(source.toString()).toLowerCase(Locale.ROOT);
        if ("pdf".equals(extension)) return source;
        if (!Arrays.asList("doc", "docx", "xls", "xlsx", "ppt", "pptx").contains(extension))
            throw new ServiceException("该附件不支持预览，请下载查看");
        Path directory = source.resolveSibling(source.getFileName() + ".preview");
        Path pdf = directory.resolve(FilenameUtils.getBaseName(source.toString()) + ".pdf");
        if (Files.isRegularFile(pdf) && Files.size(pdf) > 0) return pdf;
        // 独立限流避免大量预览同时排队占用课堂文档转换资源。
        if (!conversion.tryAcquire()) throw new ServiceException("文档预览正在转换，请稍后重试或下载查看");
        try
        {
            if (Files.isRegularFile(pdf) && Files.size(pdf) > 0) return pdf;
            Files.createDirectories(directory);
            Path pending = Files.createTempDirectory(directory, "pending-");
            Path converted = pending.resolve(pdf.getFileName());
            try
            {
                String result = FileConversionUtils.convertOfficeToPdfWithLibreOffice(source.toString(), pending.toString());
                if (result == null || !Files.isRegularFile(converted) || Files.size(converted) == 0)
                    throw new ServiceException("文档转换失败，请下载查看或稍后重试");
                Files.move(converted, pdf, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            finally
            {
                Files.deleteIfExists(converted);
                Files.deleteIfExists(pending);
            }
            return pdf;
        }
        finally { conversion.release(); }
    }
}
