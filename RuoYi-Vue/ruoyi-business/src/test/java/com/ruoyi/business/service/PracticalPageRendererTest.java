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
        assertEquals(1800, ImageIO.read(rendered).getWidth());
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

    private File resourceFile(String resource)
    {
        return new File(RuoYiConfig.getProfile() + resource.replaceFirst("/profile", ""));
    }
}
