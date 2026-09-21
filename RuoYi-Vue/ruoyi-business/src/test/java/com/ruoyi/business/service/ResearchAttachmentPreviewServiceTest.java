package com.ruoyi.business.service;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.ruoyi.common.exception.ServiceException;
import static org.junit.jupiter.api.Assertions.*;

class ResearchAttachmentPreviewServiceTest
{
    @TempDir Path directory;
    private final ResearchAttachmentPreviewService service = new ResearchAttachmentPreviewService();

    @Test void pdfIsReadDirectlyAndArchivesAreDownloadOnly() throws Exception
    {
        Path pdf = Files.write(directory.resolve("example.pdf"), "%PDF-1.7".getBytes("US-ASCII"));
        assertEquals(pdf, service.preview(pdf));
        assertThrows(ServiceException.class, () -> service.preview(directory.resolve("archive.zip")));
    }

    @Test void reusesPrivateOfficePreviewCache() throws Exception
    {
        Path word = directory.resolve("example.docx");
        Path cache = Files.createDirectories(directory.resolve("example.docx.preview"));
        Path pdf = Files.write(cache.resolve("example.pdf"), "%PDF-1.7".getBytes("US-ASCII"));
        assertEquals(pdf, service.preview(word));
    }
}
