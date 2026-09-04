package com.ruoyi.business.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.PracticalAttachment;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.ServiceException;

/**
 * 将 PDF 或图片规范化为有序 JPEG 页图，供教师连续查看和 AI 视觉输入复用。
 */
@Service
public class PracticalPageRenderer
{
    public static final String RENDERER_VERSION = "page-jpeg-v1";
    private static final int MAX_PAGES = 50;
    private static final int MAX_EDGE = 1800;

    public List<String> render(PracticalAttachment attachment, File visualSource)
    {
        if (attachment == null || visualSource == null || !visualSource.isFile())
        {
            throw new ServiceException("规范化渲染源文件不存在");
        }
        File outputDir = outputDirectory(attachment, visualSource);

        List<File> pages;
        if ("IMAGE".equals(attachment.getFileKind()))
        {
            pages = renderImage(visualSource, outputDir);
        }
        else
        {
            pages = renderPdf(visualSource, outputDir);
        }
        return toResources(pages);
    }

    /**
     * 区域抽测等单文件业务复用同一页图算法，ownerKey 只用于隔离输出目录。
     */
    public List<String> renderForOwner(String ownerKey, String fileKind, File visualSource)
    {
        if (ownerKey == null || !ownerKey.matches("[A-Za-z0-9_-]+"))
        {
            throw new ServiceException("页图资源归属标识非法");
        }
        if (visualSource == null || !visualSource.isFile())
        {
            throw new ServiceException("规范化渲染源文件不存在");
        }
        File outputDir = new File(visualSource.getParentFile(),
                "normalized-v1" + File.separator + ownerKey);
        if (!outputDir.exists() && !outputDir.mkdirs())
        {
            throw new ServiceException("无法创建规范化页图目录");
        }
        List<File> pages = "IMAGE".equals(fileKind)
                ? renderImage(visualSource, outputDir)
                : renderPdf(visualSource, outputDir);
        return toResources(pages);
    }

    /**
     * 命中哈希缓存时只复制渲染结果，不共享其他学生作品的资源地址，避免资源归属歧义。
     */
    public List<String> copyCachedPages(PracticalAttachment attachment, File visualSource,
            List<String> cachedPages)
    {
        if (cachedPages == null || cachedPages.isEmpty())
        {
            throw new ServiceException("规范化缓存为空");
        }
        File outputDir = outputDirectory(attachment, visualSource);
        List<File> copied = new ArrayList<File>();
        try
        {
            for (int i = 0; i < cachedPages.size(); i++)
            {
                File cached = resourceFile(cachedPages.get(i));
                if (!cached.isFile()) throw new ServiceException("规范化缓存文件不存在");
                File target = new File(outputDir, String.format("page-%03d.jpg", i + 1));
                Files.copy(cached.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                copied.add(target);
            }
            return toResources(copied);
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("规范化缓存复制失败");
        }
    }

    private File outputDirectory(PracticalAttachment attachment, File visualSource)
    {
        String hash = attachment.getSha256() == null || attachment.getSha256().trim().isEmpty()
                ? "nohash" : attachment.getSha256();
        String cacheKey = hash + "-attachment-" + attachment.getAttachmentId();
        File outputDir = new File(visualSource.getParentFile(),
                "normalized-v1" + File.separator + cacheKey);
        if (!outputDir.exists() && !outputDir.mkdirs())
        {
            throw new ServiceException("无法创建规范化页图目录");
        }
        return outputDir;
    }

    private List<String> toResources(List<File> pages)
    {
        List<String> resources = new ArrayList<String>();
        String physicalProfile = new File(RuoYiConfig.getProfile()).getAbsolutePath().replace('\\', '/');
        for (File page : pages)
        {
            String absolute = page.getAbsolutePath().replace('\\', '/');
            if (!absolute.startsWith(physicalProfile + "/"))
            {
                throw new ServiceException("规范化页图路径越界");
            }
            resources.add(Constants.RESOURCE_PREFIX + absolute.substring(physicalProfile.length()));
        }
        return resources;
    }

    private File resourceFile(String resource)
    {
        String relative = resource.replaceFirst(Constants.RESOURCE_PREFIX, "");
        return new File(RuoYiConfig.getProfile() + relative);
    }

    private List<File> renderPdf(File source, File outputDir)
    {
        List<File> result = new ArrayList<File>();
        try (PDDocument document = PDDocument.load(source))
        {
            if (document.isEncrypted())
            {
                throw new ServiceException("加密 PDF 无法生成页图");
            }
            int pageCount = document.getNumberOfPages();
            if (pageCount < 1 || pageCount > MAX_PAGES)
            {
                throw new ServiceException("作品页数必须在1至50页之间");
            }
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < pageCount; i++)
            {
                BufferedImage rendered = renderer.renderImageWithDPI(i, 120, ImageType.RGB);
                File target = new File(outputDir, String.format("page-%03d.jpg", i + 1));
                writeJpeg(scale(rendered), target);
                result.add(target);
            }
            return result;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("PDF页图生成失败");
        }
    }

    private List<File> renderImage(File source, File outputDir)
    {
        try
        {
            BufferedImage image = ImageIO.read(source);
            if (image == null)
            {
                throw new ServiceException("图片无法安全解码");
            }
            File target = new File(outputDir, "page-001.jpg");
            writeJpeg(scale(image), target);
            List<File> result = new ArrayList<File>();
            result.add(target);
            return result;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("图片规范化失败");
        }
    }

    private BufferedImage scale(BufferedImage source)
    {
        double ratio = Math.min(1D, (double) MAX_EDGE / Math.max(source.getWidth(), source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * ratio));
        int height = Math.max(1, (int) Math.round(source.getHeight() * ratio));
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try
        {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(source, 0, 0, width, height, null);
        }
        finally
        {
            graphics.dispose();
        }
        return target;
    }

    private void writeJpeg(BufferedImage image, File target) throws Exception
    {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) throw new ServiceException("系统缺少 JPEG 编码器");
        ImageWriter writer = writers.next();
        try (ImageOutputStream output = ImageIO.createImageOutputStream(target))
        {
            writer.setOutput(output);
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(0.85F);
            writer.write(null, new IIOImage(image, null, null), params);
        }
        finally
        {
            writer.dispose();
        }
    }
}
