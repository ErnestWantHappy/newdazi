package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import com.ruoyi.business.domain.dto.PracticalUploadTicket;
import com.ruoyi.common.exception.ServiceException;

class PracticalFilePolicyServiceTest
{
    private final PracticalFilePolicyService service = new PracticalFilePolicyService();

    @Test
    void shouldAcceptPngAndCalculateDigest()
    {
        byte[] content = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3 };
        PracticalUploadTicket result = service.inspect(new MockMultipartFile(
                "file", "作品.PNG", "image/png", content), "png,jpg");

        assertEquals("png", result.getFileExtension());
        assertEquals("IMAGE", result.getFileKind());
        assertEquals(64, result.getSha256().length());
    }

    @Test
    void shouldRejectSpoofedExtensionOrMime()
    {
        assertThrows(ServiceException.class, () -> service.inspect(new MockMultipartFile(
                "file", "伪装.pdf", "application/pdf", new byte[] { 1, 2, 3 }), "pdf"));
        assertThrows(ServiceException.class, () -> service.inspect(new MockMultipartFile(
                "file", "图片.png", "application/pdf",
                new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A }), "png"));
    }

    @Test
    void shouldRecognizeOpenXmlFamilyByInternalStructure() throws Exception
    {
        PracticalUploadTicket pptx = service.inspect(new MockMultipartFile(
                "file", "演示文稿.pptx",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                openXml("ppt/presentation.xml")), "pptx");
        assertEquals("OFFICE", pptx.getFileKind());

        assertThrows(ServiceException.class, () -> service.inspect(new MockMultipartFile(
                "file", "伪装表格.xlsx", "application/zip",
                openXml("word/document.xml")), "xlsx"));
    }

    private byte[] openXml(String requiredEntry) throws Exception
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output))
        {
            zip.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zip.write("<Types/>".getBytes("UTF-8"));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry(requiredEntry));
            zip.write("<root/>".getBytes("UTF-8"));
            zip.closeEntry();
        }
        return output.toByteArray();
    }
}
