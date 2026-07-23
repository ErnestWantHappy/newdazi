package com.ruoyi.business.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.exception.ServiceException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResearchActivityUploadServiceTest
{
    private final ResearchActivityUploadService service = new ResearchActivityUploadService();

    ResearchActivityUploadServiceTest()
    {
        ReflectionTestUtils.setField(service, "profile", "target/research-upload-test");
    }

    @Test
    void accepts49MiBAndExactly50MiBButRejectsOneByteMore() throws Exception
    {
        assertDoesNotThrow(() -> service.validatePackage(file("a.zip", "application/zip",
                49L * 1024L * 1024L, hex(0x50, 0x4B, 0x03, 0x04))));
        assertDoesNotThrow(() -> service.validatePackage(file("a.zip", "application/zip",
                50L * 1024L * 1024L, hex(0x50, 0x4B, 0x03, 0x04))));
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.validatePackage(file("a.zip", "application/zip",
                        50L * 1024L * 1024L + 1, hex(0x50, 0x4B, 0x03, 0x04))));
        assertTrue(error.getMessage().contains("超过50MB"));
    }

    @Test
    void validatesZipRar4Rar5And7zSignatures() throws Exception
    {
        assertDoesNotThrow(() -> service.validatePackage(file("a.zip", "application/octet-stream", 100,
                hex(0x50, 0x4B, 0x03, 0x04))));
        assertDoesNotThrow(() -> service.validatePackage(file("a.rar", "application/vnd.rar", 100,
                hex(0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00))));
        assertDoesNotThrow(() -> service.validatePackage(file("a.rar", "application/x-rar-compressed", 100,
                hex(0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00))));
        assertDoesNotThrow(() -> service.validatePackage(file("a.7z", "application/x-7z-compressed", 100,
                hex(0x37, 0x7A, 0xBC, 0xAF, 0x27, 0x1C))));
    }

    @Test
    void rejectsDisguisedArchiveByExtensionMimeOrHeader() throws Exception
    {
        assertThrows(ServiceException.class, () -> service.validatePackage(
                file("fake.zip", "application/zip", 100, "plain".getBytes())));
        assertThrows(ServiceException.class, () -> service.validatePackage(
                file("fake.exe", "application/zip", 100, hex(0x50, 0x4B))));
        assertThrows(ServiceException.class, () -> service.validatePackage(
                file("fake.zip", "text/plain", 100, hex(0x50, 0x4B))));
    }

    @Test
    void rejectsPackagePathTraversal()
    {
        assertThrows(ServiceException.class, () -> service.resolvePackagePath("../../outside.zip"));
        assertDoesNotThrow(() -> service.resolvePackagePath("1/2/2026/07/22/a.zip"));
    }

    @Test
    void validatesJpegPngAndWebpHeaders() throws Exception
    {
        assertDoesNotThrow(() -> service.validateImage(file("a.jpg", "image/jpeg", 100,
                hex(0xFF, 0xD8, 0xFF, 0xE0))));
        assertDoesNotThrow(() -> service.validateImage(file("a.png", "image/png", 100,
                hex(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))));
        assertDoesNotThrow(() -> service.validateImage(file("a.webp", "image/webp", 100,
                hex(0x52, 0x49, 0x46, 0x46, 0x00, 0x00, 0x00, 0x00, 0x57, 0x45, 0x42, 0x50))));
        assertThrows(ServiceException.class, () -> service.validateImage(
                file("fake.png", "image/png", 100, "plain".getBytes())));
    }

    private MultipartFile file(String name, String mime, long size, byte[] header) throws Exception
    {
        MultipartFile file = mock(MultipartFile.class);
        lenient().when(file.isEmpty()).thenReturn(false);
        lenient().when(file.getSize()).thenReturn(size);
        lenient().when(file.getOriginalFilename()).thenReturn(name);
        lenient().when(file.getContentType()).thenReturn(mime);
        lenient().when(file.getInputStream()).thenAnswer(invocation -> (InputStream) new ByteArrayInputStream(header));
        return file;
    }

    private byte[] hex(int... values)
    {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) bytes[i] = (byte) values[i];
        return bytes;
    }
}
