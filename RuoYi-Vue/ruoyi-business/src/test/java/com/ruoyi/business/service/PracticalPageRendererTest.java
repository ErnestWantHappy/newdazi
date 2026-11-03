package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.ruoyi.business.domain.PracticalAttachment;
import com.ruoyi.common.config.RuoYiConfig;

class PracticalPageRendererTest
{
    @TempDir
    Path tempDir;
    private PracticalPageRenderer renderer;

    @BeforeEach
    void setUp()
    {
        new RuoYiConfig().setProfile(tempDir.toString());
        renderer = new PracticalPageRenderer();
    }

    @Test
    void shouldNormalizeImageToSingleJpegPage() throws Exception
    {
        File source = tempDir.resolve("upload/work.png").toFile();
        assertTrue(source.getParentFile().mkdirs());
        BufferedImage image = new BufferedImage(2400, 1200, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, Color.RED.getRGB());
        ImageIO.write(image, "png", source);

        List<String> pages = renderer.render(attachment(1L, "IMAGE", "abc"), source);

        assertEquals(1, pages.size());
        assertTrue(pages.get(0).endsWith("page-001.jpg"));
        File rendered = resourceFile(pages.get(0));
        assertTrue(rendered.isFile());
        assertEquals(2000, ImageIO.read(rendered).getWidth());
    }

    @Test
    void shouldRenderEveryPdfPageInOrder() throws Exception
    {
        File source = tempDir.resolve("upload/work.pdf").toFile();
        assertTrue(source.getParentFile().mkdirs());
        try (PDDocument document = new PDDocument())
        {
            document.addPage(new PDPage());
            document.addPage(new PDPage());
            document.save(source);
        }

        List<String> pages = renderer.render(attachment(2L, "PDF", "def"), source);

        assertEquals(2, pages.size());
        assertTrue(pages.get(0).endsWith("page-001.jpg"));
        assertTrue(pages.get(1).endsWith("page-002.jpg"));
        assertTrue(resourceFile(pages.get(1)).isFile());
    }

    @Test
    void shouldCopyCachedPagesIntoCurrentAttachmentDirectory() throws Exception
    {
        File firstSource = tempDir.resolve("upload/first/work.png").toFile();
        File secondSource = tempDir.resolve("upload/second/work.png").toFile();
        assertTrue(firstSource.getParentFile().mkdirs());
        assertTrue(secondSource.getParentFile().mkdirs());
        BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", firstSource);
        ImageIO.write(image, "png", secondSource);

        List<String> cached = renderer.render(attachment(3L, "IMAGE", "same"), firstSource);
        List<String> copied = renderer.copyCachedPages(
                attachment(4L, "IMAGE", "same"), secondSource, cached);

        assertEquals(1, copied.size());
        assertTrue(resourceFile(copied.get(0)).isFile());
        assertTrue(copied.get(0).contains("attachment-4"));
        assertTrue(!cached.get(0).equals(copied.get(0)));
    }

    @Test
    void versionedPagesKeepLegacyFilesAndUseNewResourceAddresses() throws Exception
    {
        File source = tempDir.resolve("upload/work.png").toFile();
        assertTrue(source.getParentFile().mkdirs());
        ImageIO.write(new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB), "png", source);
        File old = tempDir.resolve("upload/normalized-v1/same-attachment-7/page-001.jpg").toFile();
        assertTrue(old.getParentFile().mkdirs());
        java.nio.file.Files.write(old.toPath(), new byte[] {1, 2, 3});
        List<String> pages = renderer.render(attachment(7L, "IMAGE", "same"), source);
        assertTrue(pages.get(0).contains("attachment-7-" + PracticalPageRenderer.RENDERER_VERSION));
        org.junit.jupiter.api.Assertions.assertArrayEquals(new byte[] {1, 2, 3}, java.nio.file.Files.readAllBytes(old.toPath()));
        List<String> owner = renderer.renderForOwner("county-7", "IMAGE", source);
        assertTrue(owner.get(0).contains("county-7-" + PracticalPageRenderer.RENDERER_VERSION));
    }

    private PracticalAttachment attachment(Long id, String kind, String sha)
    {
        PracticalAttachment attachment = new PracticalAttachment();
        attachment.setAttachmentId(id);
        attachment.setFileKind(kind);
        attachment.setSha256(sha);
        return attachment;
    }

    @Test
    void shouldKeepA3A4PortraitLandscapeCornersAndPageOrder() throws Exception
    {
        File source = tempDir.resolve("papers.pdf").toFile();
        org.apache.pdfbox.pdmodel.common.PDRectangle[] sizes = {
            org.apache.pdfbox.pdmodel.common.PDRectangle.A3,
            org.apache.pdfbox.pdmodel.common.PDRectangle.A4
        };
        try (PDDocument document = new PDDocument())
        {
            for (org.apache.pdfbox.pdmodel.common.PDRectangle size : sizes)
                for (int rotation : new int[] {0, 90})
                {
                    PDPage page = new PDPage(size); page.setRotation(rotation); document.addPage(page);
                    try (org.apache.pdfbox.pdmodel.PDPageContentStream content = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page))
                    {
                        content.setNonStrokingColor(Color.RED);
                        for (float x : new float[] {0, size.getWidth() - 35})
                            for (float y : new float[] {0, size.getHeight() - 35})
                            { content.addRect(x, y, 35, 35); content.fill(); }
                    }
                }
            document.save(source);
        }
        List<String> pages = renderer.render(attachment(8L, "PDF", "paper-sizes"), source);
        assertEquals(4, pages.size());
        for (int i = 0; i < pages.size(); i++)
        {
            assertTrue(pages.get(i).endsWith(String.format("page-%03d.jpg", i + 1)));
            BufferedImage image = ImageIO.read(resourceFile(pages.get(i)));
            assertEquals(i % 2 == 0, image.getHeight() > image.getWidth());
            assertEquals(Math.sqrt(2), (double) Math.max(image.getWidth(), image.getHeight()) / Math.min(image.getWidth(), image.getHeight()), 0.02);
            for (int x : new int[] {10, image.getWidth() - 11})
                for (int y : new int[] {10, image.getHeight() - 11})
                {
                    Color corner = new Color(image.getRGB(x, y));
                    assertTrue(corner.getRed() > 180 && corner.getGreen() < 80, "纸张四角不得裁掉");
                }
        }
    }

    /**
     * 内容画到页框外（LibreOffice 字体度量偏大时浮动图形的真实表现）时，
     * 页图必须把越界部分一并算进去，否则教师只能看到被裁掉一半的作品。
     */
    @Test
    void shouldIncludeContentDrawnOutsidePageBox() throws Exception
    {
        File source = tempDir.resolve("upload/overflow.pdf").toFile();
        assertTrue(source.getParentFile().mkdirs());
        writeA4WithRect(source, -40F);

        byte[] before = java.nio.file.Files.readAllBytes(source.toPath());
        List<String> pages = renderer.render(attachment(9L, "PDF", "overflow"), source);

        BufferedImage image = ImageIO.read(resourceFile(pages.get(0)));
        // A4 在 150 DPI 下高约 1754px；越界 40pt 加余量后应明显更高。
        assertTrue(image.getHeight() > 1800, "越界内容应纳入页图，实际高度 " + image.getHeight());
        // 学生原始 PDF 是缓存身份的一部分，渲染不得改动它。
        org.junit.jupiter.api.Assertions.assertArrayEquals(before,
                java.nio.file.Files.readAllBytes(source.toPath()), "学生原件不得被改写");
    }

    /** 内容全在页框内时必须与旧行为一致，不能因为新增探测而缩放或偏移正常文档。 */
    @Test
    void shouldKeepInBoxPageImageUnchanged() throws Exception
    {
        File source = tempDir.resolve("upload/normal.pdf").toFile();
        assertTrue(source.getParentFile().mkdirs());
        writeA4WithRect(source, 50F);

        List<String> pages = renderer.render(attachment(10L, "PDF", "normal"), source);

        BufferedImage image = ImageIO.read(resourceFile(pages.get(0)));
        assertEquals(1240, image.getWidth());
        assertTrue(image.getHeight() >= 1750 && image.getHeight() <= 1755,
                "正常 A4 页高应保持约 1754px，实际 " + image.getHeight());
    }

    /** 只有平台生成的转换副本才回写页框，让「PDF 原图」视图与页图看到同一范围。 */
    @Test
    void shouldRewriteFrameOnlyForDerivedPreview() throws Exception
    {
        File source = tempDir.resolve("upload/derived.pdf").toFile();
        assertTrue(source.getParentFile().mkdirs());
        writeA4WithRect(source, -40F);
        byte[] before = java.nio.file.Files.readAllBytes(source.toPath());

        PracticalAttachment attachment = attachment(11L, "OFFICE", "derived");
        attachment.setPreviewPath("/profile/upload/derived.pdf");
        renderer.render(attachment, source);

        assertTrue(!java.util.Arrays.equals(before, java.nio.file.Files.readAllBytes(source.toPath())),
                "转换副本页框应被回写");
        try (PDDocument reloaded = PDDocument.load(source))
        {
            assertTrue(reloaded.getPage(0).getCropBox().getHeight() > 850F,
                    "回写后的取景框应覆盖越界内容，实际 " + reloaded.getPage(0).getCropBox().getHeight());
        }
    }

    /** 区域抽测走 renderForOwner，同样不得回写源文件。 */
    @Test
    void shouldNotRewriteSourceWhenRenderingForOwner() throws Exception
    {
        File source = tempDir.resolve("upload/owner.pdf").toFile();
        assertTrue(source.getParentFile().mkdirs());
        writeA4WithRect(source, -40F);
        byte[] before = java.nio.file.Files.readAllBytes(source.toPath());

        List<String> pages = renderer.renderForOwner("county-overflow", "PDF", source);

        assertTrue(ImageIO.read(resourceFile(pages.get(0))).getHeight() > 1800, "抽测页图也应看到越界内容");
        org.junit.jupiter.api.Assertions.assertArrayEquals(before,
                java.nio.file.Files.readAllBytes(source.toPath()), "抽测渲染不得改写源文件");
    }

    /** 路径回调已变换坐标，二次平移会把本来在页内的矩形和曲线误判为越界。 */
    @Test
    void shouldNotTransformPathCoordinatesTwice() throws Exception
    {
        File source = tempDir.resolve("upload/transformed.pdf").toFile();
        assertTrue(source.getParentFile().mkdirs());
        try (PDDocument document = new PDDocument())
        {
            PDPage page = new PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
            document.addPage(page);
            try (org.apache.pdfbox.pdmodel.PDPageContentStream content = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page))
            {
                content.transform(org.apache.pdfbox.util.Matrix.getTranslateInstance(400F, 600F));
                content.addRect(0F, 0F, 50F, 50F);
                content.fill();
                content.moveTo(0F, 60F);
                content.lineTo(50F, 60F);
                content.curveTo(60F, 70F, 70F, 80F, 80F, 90F);
                content.stroke();
            }
            document.save(source);
        }
        BufferedImage image = ImageIO.read(resourceFile(renderer.render(attachment(12L, "PDF", "transformed"), source).get(0)));
        assertEquals(1240, image.getWidth());
        assertTrue(image.getHeight() >= 1750 && image.getHeight() <= 1755,
                "正常平移图形不得触发页框扩大");
    }

    /** 生成一页 A4，在 x=50 处画一个 100×100 的方块；bottomY 为负即模拟内容溢出页框。 */
    private void writeA4WithRect(File target, float bottomY) throws Exception
    {
        try (PDDocument document = new PDDocument())
        {
            PDPage page = new PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
            document.addPage(page);
            try (org.apache.pdfbox.pdmodel.PDPageContentStream content = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page))
            {
                content.setNonStrokingColor(Color.RED);
                content.addRect(50F, bottomY, 100F, 100F);
                content.fill();
            }
            document.save(target);
        }
    }

    private File resourceFile(String resource)
    {
        return new File(RuoYiConfig.getProfile() + resource.replaceFirst("/profile", ""));
    }
}
