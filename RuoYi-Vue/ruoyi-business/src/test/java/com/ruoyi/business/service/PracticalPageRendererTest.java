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

    private File resourceFile(String resource)
    {
        return new File(RuoYiConfig.getProfile() + resource.replaceFirst("/profile", ""));
    }
}
