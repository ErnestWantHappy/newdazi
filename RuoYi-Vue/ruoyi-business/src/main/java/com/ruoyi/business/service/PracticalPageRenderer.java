package com.ruoyi.business.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(PracticalPageRenderer.class);

    // v4：Linux 字体度量与 WPS 不同，浮动图形可能被推到页框外而被整段裁掉；
    //     本版渲染时会自动把越界内容纳入页图，故必须失效 v3 页图缓存重算。
    public static final String RENDERER_VERSION = "page-jpeg-v4";
    private static final int MAX_PAGES = 50;
    private static final int MAX_EDGE = 2000;
    private static final int RENDER_DPI = 150;
    // 渲染时向外多取一圈，给“内容溢出页框”留出被看见的机会；过大只会多出白边。
    private static final float OVERFLOW_PADDING_RATIO = 0.18F;
    private static final float OVERFLOW_MIN_PADDING = 24F;
    // 内容边界外再留一点余量，避免文字紧贴裁切边缘。
    private static final float INK_MARGIN_RATIO = 0.01F;
    // 低于该灰度即视为“有内容”。抗锯齿产生的浅灰不会被误判。
    private static final int INK_THRESHOLD = 245;

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
            // 只有平台自己生成的转换副本（OFFICE 的 previewPath）才允许回写页框；
            // 学生原始 PDF 必须保持字节不变，否则会被当成新文件而失去缓存身份。
            pages = renderPdf(visualSource, outputDir, isDerivedPreview(attachment, visualSource));
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
                "normalized-v1" + File.separator + ownerKey + "-" + RENDERER_VERSION);
        if (!outputDir.exists() && !outputDir.mkdirs())
        {
            throw new ServiceException("无法创建规范化页图目录");
        }
        List<File> pages = "IMAGE".equals(fileKind)
                ? renderImage(visualSource, outputDir)
                : renderPdf(visualSource, outputDir, false);
        return toResources(pages);
    }

    /**
     * 判断待渲染的 PDF 是否为平台生成的转换副本。
     * 只有 OFFICE 附件会把 PDF 写进 previewPath；学生原始 PDF/图片不会被改写。
     */
    private boolean isDerivedPreview(PracticalAttachment attachment, File visualSource)
    {
        String previewPath = attachment.getPreviewPath();
        if (previewPath == null || previewPath.trim().isEmpty())
        {
            return false;
        }
        try
        {
            return resourceFile(previewPath).getCanonicalFile()
                    .equals(visualSource.getCanonicalFile());
        }
        catch (Exception e)
        {
            // 路径无法比对时保守视为原件，绝不动源文件。
            return false;
        }
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
        // 版本升级使用新资源地址，避免浏览器仍显示旧页图，并保留旧文件供精确回退。
        String cacheKey = hash + "-attachment-" + attachment.getAttachmentId() + "-" + RENDERER_VERSION;
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
          String physicalProfile = new File(RuoYiConfig.getProfile()).toPath().toAbsolutePath().normalize().toString().replace('\\', '/');
        for (File page : pages)
        {
              String absolute = page.toPath().toAbsolutePath().normalize().toString().replace('\\', '/');
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

    /**
     * 渲染 PDF 页图。
     *
     * <p>Linux 与 WPS/Word 的字体度量不同，浮动图形（wrapNone 锚定）在 LibreOffice 里
     * 可能被推到页框之外。这类图形不参与正文流，不会被分页到下一页，只会静默丢在页外。
     * 因此先测出每页内容的真实边界，越界时把取景框扩到内容处再渲染，避免整段内容被裁掉。
     *
     * <p>只有平台生成的转换副本才允许回写页框（{@code allowFramePersist}）；学生原始
     * PDF 必须保持字节不变，否则哈希身份变化会破坏页图缓存与其他业务判断。
     */
    private List<File> renderPdf(File source, File outputDir, boolean allowFramePersist)
    {
        List<File> result = new ArrayList<File>();
        boolean frameChanged = false;
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

            for (int i = 0; i < pageCount; i++)
            {
                if (expandFrameToContent(document.getPage(i)))
                {
                    frameChanged = true;
                }
            }

            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < pageCount; i++)
            {
                BufferedImage rendered = renderer.renderImageWithDPI(i, RENDER_DPI, ImageType.RGB);
                File target = new File(outputDir, String.format("page-%03d.jpg", i + 1));
                writeJpeg(scale(rendered), target);
                result.add(target);
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            LOGGER.warn("PDF页图生成失败 source={}", source == null ? null : source.getName(), e);
            throw new ServiceException("PDF页图生成失败");
        }

        // 回写必须等文档关闭后再做：文档句柄仍占用源文件时，Windows 无法原子替换。
        // 扩大规则是幂等的（已放大的页框再测不再越界），失败也只影响「PDF 原图」视图。
        if (frameChanged && allowFramePersist)
        {
            persistExpandedFrame(source);
        }
        return result;
    }

    /**
     * 把页面取景框扩到「页框 ∪ 内容真实边界」。内容没越界时原样返回，保证正常文档字节不变。
     *
     * @return 是否真的改过页框
     */
    private boolean expandFrameToContent(PDPage page)
    {
        PDRectangle box = page.getCropBox();
        if (box == null)
        {
            return false;
        }
        Bounds content;
        try
        {
            content = measureContentBounds(page);
        }
        catch (Exception e)
        {
            // 探测失败只影响“能否多看到一点”，不能让它拖垮整次转换。
            LOGGER.warn("PDF内容边界探测失败 page={}", page.getCOSObject(), e);
            return false;
        }
        if (content == null || content.isEmpty() || content.inside(box))
        {
            return false;
        }

        float minX = Math.min(box.getLowerLeftX(), content.minX);
        float minY = Math.min(box.getLowerLeftY(), content.minY);
        float maxX = Math.max(box.getUpperRightX(), content.maxX);
        float maxY = Math.max(box.getUpperRightY(), content.maxY);

        // 留一点余量，避免文字正好贴在裁切边缘上。
        float margin = Math.max(Math.abs(maxY - minY), Math.abs(maxX - minX)) * INK_MARGIN_RATIO;
        minX -= margin;
        minY -= margin;
        maxX += margin;
        maxY += margin;

        PDRectangle expanded = new PDRectangle(minX, minY, maxX - minX, maxY - minY);
        page.setCropBox(expanded);
        // MediaBox 同步放大：部分渲染与查看器按 MediaBox 裁剪，只改 CropBox 会被再次切掉。
        PDRectangle media = page.getMediaBox();
        if (media != null)
        {
            page.setMediaBox(new PDRectangle(
                    Math.min(media.getLowerLeftX(), expanded.getLowerLeftX()),
                    Math.min(media.getLowerLeftY(), expanded.getLowerLeftY()),
                    Math.max(media.getUpperRightX(), expanded.getUpperRightX()) - Math.min(media.getLowerLeftX(), expanded.getLowerLeftX()),
                    Math.max(media.getUpperRightY(), expanded.getUpperRightY()) - Math.min(media.getLowerLeftY(), expanded.getLowerLeftY())));
        }
        return true;
    }

    /** 测量一页里所有可见内容（路径、文本、图像、底纹）的包围盒，使用页面用户空间坐标。 */
    private Bounds measureContentBounds(PDPage page) throws IOException
    {
        Bounds bounds = new Bounds();
        ContentBoundsEngine engine = new ContentBoundsEngine(page, bounds);
        engine.processPage(page);
        return bounds.isEmpty() ? null : bounds;
    }

    /**
     * 把扩大后的页框写回转换副本，使「PDF 原图」视图与页图一致。
     * 先写临时文件再原子替换，避免失败时损坏既有预览。
     */
    private void persistExpandedFrame(File source)
    {
        File temp = new File(source.getParentFile(), source.getName() + ".reframe.tmp");
        try
        {
            // 重新打开一次而不是复用渲染时的文档：渲染用的文档已经关闭，
            // 且关闭前保存会把页框写回原始字节流，这里只需要确定性地产出扩大后的页框。
            try (PDDocument document = PDDocument.load(source))
            {
                for (int i = 0; i < document.getNumberOfPages(); i++)
                {
                    expandFrameToContent(document.getPage(i));
                }
                document.save(temp);
            }
            Files.move(temp.toPath(), source.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (Exception e)
        {
            LOGGER.warn("转换副本页框回写失败，页图仍按内存中扩大后的页框渲染 source={}",
                    source.getName(), e);
        }
        finally
        {
            if (temp.exists() && !temp.delete())
            {
                LOGGER.warn("临时页框文件未清理：{}", temp.getName());
            }
        }
    }

    /** 内容包围盒累加器。 */
    private static class Bounds
    {
        private float minX = Float.MAX_VALUE;
        private float minY = Float.MAX_VALUE;
        private float maxX = -Float.MAX_VALUE;
        private float maxY = -Float.MAX_VALUE;

        void add(float x, float y)
        {
            if (Float.isNaN(x) || Float.isNaN(y) || Float.isInfinite(x) || Float.isInfinite(y))
            {
                return;
            }
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        void add(Point2D point)
        {
            if (point != null)
            {
                add((float) point.getX(), (float) point.getY());
            }
        }

        boolean isEmpty()
        {
            return minX > maxX || minY > maxY;
        }

        /** 内容是否完全落在给定页框内。 */
        boolean inside(PDRectangle box)
        {
            // 允许 0.5pt 的浮点误差，避免正常文档被反复重写。
            float tolerance = 0.5F;
            return minX >= box.getLowerLeftX() - tolerance
                    && minY >= box.getLowerLeftY() - tolerance
                    && maxX <= box.getUpperRightX() + tolerance
                    && maxY <= box.getUpperRightY() + tolerance;
        }
    }

    /**
     * 遍历页面内容流，累计所有图形、图像与字形原点的包围盒。
     * PDFStreamEngine 会自动下钻 Form XObject，因此嵌套的虚线框、组合图形同样被计入。
     */
    private static class ContentBoundsEngine extends PDFGraphicsStreamEngine
    {
        private final Bounds bounds;

        ContentBoundsEngine(PDPage page, Bounds bounds)
        {
            super(page);
            this.bounds = bounds;
        }

        /** 把用户空间坐标经当前变换矩阵转到设备空间后计入。 */
        private void addTransformed(float x, float y)
        {
            Point2D.Float point = new Point2D.Float(x, y);
            transformInPlace(point);
            bounds.add(point);
        }

        /** Matrix.transform 是原地变换，没有返回值，这里统一包一层避免误用。 */
        private void transformInPlace(Point2D.Float point)
        {
            Matrix matrix = getGraphicsState().getCurrentTransformationMatrix();
            if (matrix != null)
            {
                matrix.transform(point);
            }
        }

        @Override
        public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException
        {
            // PDFBox 路径回调已应用当前矩阵，重复变换会误判正常的缩放或平移图形。
            bounds.add(p0);
            bounds.add(p1);
            bounds.add(p2);
            bounds.add(p3);
        }

        @Override
        public void drawImage(PDImage pdImage) throws IOException
        {
            // 图像按 1×1 单位方框放置，四个角都要计入。
            addTransformed(0F, 0F);
            addTransformed(1F, 0F);
            addTransformed(0F, 1F);
            addTransformed(1F, 1F);
        }

        @Override
        public void clip(int windingRule) throws IOException
        {
            // 仅设置裁剪区，不产生新内容。
        }

        @Override
        public void moveTo(float x, float y) throws IOException
        {
            bounds.add(x, y);
        }

        @Override
        public void lineTo(float x, float y) throws IOException
        {
            bounds.add(x, y);
        }

        @Override
        public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException
        {
            bounds.add(x1, y1);
            bounds.add(x2, y2);
            bounds.add(x3, y3);
        }

        @Override
        public Point2D getCurrentPoint() throws IOException
        {
            return new Point2D.Float(0F, 0F);
        }

        @Override
        public void closePath() throws IOException
        {
            // 闭合回起点，起点已计入。
        }

        @Override
        public void endPath() throws IOException
        {
            // 无新坐标。
        }

        @Override
        public void strokePath() throws IOException
        {
            // 路径坐标已在 moveTo/lineTo/curveTo 计入。
        }

        @Override
        public void fillPath(int windingRule) throws IOException
        {
            // 同上。
        }

        @Override
        public void fillAndStrokePath(int windingRule) throws IOException
        {
            // 同上。
        }

        @Override
        public void shadingFill(COSName shadingName) throws IOException
        {
            // 底纹按当前裁剪区绘制，裁剪区已由 clip 记录。
        }

        @Override
        protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode,
                Vector displacement) throws IOException
        {
            addGlyphOrigin(textRenderingMatrix);
            super.showGlyph(textRenderingMatrix, font, code, unicode, displacement);
        }

        @Override
        protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, Vector displacement)
                throws IOException
        {
            addGlyphOrigin(textRenderingMatrix);
            super.showGlyph(textRenderingMatrix, font, code, displacement);
        }

        /** 只取字形原点：字宽由页框余量覆盖，不必逐字测宽。 */
        private void addGlyphOrigin(Matrix textRenderingMatrix)
        {
            if (textRenderingMatrix == null)
            {
                return;
            }
            Point2D.Float origin = new Point2D.Float(0F, 0F);
            textRenderingMatrix.transform(origin);
            bounds.add(origin);
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
            params.setCompressionQuality(0.92F);
            writer.write(null, new IIOImage(image, null, null), params);
        }
        finally
        {
            writer.dispose();
        }
    }
}
