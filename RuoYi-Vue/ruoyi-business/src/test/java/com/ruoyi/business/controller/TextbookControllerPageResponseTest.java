package com.ruoyi.business.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.BizTextbookPage;
import com.ruoyi.business.service.TextbookCatalogService;
import com.ruoyi.business.service.TextbookStorageService;

/**
 * 教材页图响应头回归：教材受版权约束，教师机常为共用机，
 * 缓存策略必须是"每次回源校验"，不能让浏览器把图直接交给下一个登录账号。
 */
class TextbookControllerPageResponseTest
{
    private static final String PAGE_SHA = "0e4b702b37b6c0f5d1a5b7c9e1f2a3b4c5d6e7f8091a2b3c4d5e6f708192a3b4";

    private static final byte[] IMAGE_BYTES = new byte[] { (byte) 0xFF, (byte) 0xD8, 0x01, 0x02, 0x03 };

    private static final byte[] COVER_BYTES = new byte[] { (byte) 0xFF, (byte) 0xD8, 0x09, 0x08, 0x07 };

    @TempDir
    Path tempDir;

    private TextbookController controller;

    private Path imageFile;

    private Path coverFile;

    @BeforeEach
    void setUp() throws Exception
    {
        imageFile = tempDir.resolve("p012.jpg");
        Files.write(imageFile, IMAGE_BYTES);
        coverFile = tempDir.resolve("6.jpg");
        Files.write(coverFile, COVER_BYTES);

        BizTextbookPage page = new BizTextbookPage();
        page.setBookId(6L);
        page.setPageNo(12);
        page.setImagePath("xxkj-7-0-2026/p012.jpg");
        page.setWidth(1400);
        page.setHeight(1980);
        page.setBytes(IMAGE_BYTES.length);
        page.setSha256(PAGE_SHA);

        TextbookCatalogService catalogService = mock(TextbookCatalogService.class);
        when(catalogService.requirePage(anyLong(), anyInt())).thenReturn(page);

        TextbookStorageService storageService = mock(TextbookStorageService.class);
        when(storageService.resolve(anyString())).thenReturn(imageFile);
        when(storageService.resolveCover(6L)).thenReturn(coverFile);

        controller = new TextbookController();
        ReflectionTestUtils.setField(controller, "catalogService", catalogService);
        ReflectionTestUtils.setField(controller, "storageService", storageService);
    }

    @Test
    void pageIsRevalidatedEveryTimeInsteadOfCachedForAWeek() throws Exception
    {
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.page(6L, 12, new MockHttpServletRequest(), response);

        assertEquals(200, response.getStatus());
        assertEquals("private, no-cache", response.getHeader("Cache-Control"));
        assertEquals("Authorization", response.getHeader("Vary"));
        assertEquals("\"" + PAGE_SHA + "\"", response.getHeader("ETag"));
        assertEquals("image/jpeg", response.getContentType());
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertEquals(IMAGE_BYTES.length, response.getContentLength());
        assertArrayEquals(IMAGE_BYTES, response.getContentAsByteArray());
    }

    @Test
    void matchingIfNoneMatchReturnsNotModifiedWithoutBody() throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("If-None-Match", "\"" + PAGE_SHA + "\"");
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.page(6L, 12, request, response);

        assertEquals(304, response.getStatus());
        assertEquals(0, response.getContentAsByteArray().length);
        // 304 仍要带 ETag 与 no-cache，否则浏览器会退回"没有缓存信息"的状态
        assertEquals("\"" + PAGE_SHA + "\"", response.getHeader("ETag"));
        assertEquals("private, no-cache", response.getHeader("Cache-Control"));
    }

    @Test
    void staleIfNoneMatchStillReturnsFullImage() throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("If-None-Match", "\"old-sha\"");
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.page(6L, 12, request, response);

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsByteArray().length > 0);
    }

    @Test
    void coverUsesThumbnailWithShortPrivateCache() throws Exception
    {
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.cover(6L, new MockHttpServletRequest(), response);

        assertEquals(200, response.getStatus());
        assertEquals("private, max-age=3600", response.getHeader("Cache-Control"));
        assertEquals("Authorization", response.getHeader("Vary"));
        assertTrue(response.getHeader("ETag").startsWith("\"cover-6-"));
        assertEquals("image/jpeg", response.getContentType());
        assertArrayEquals(COVER_BYTES, response.getContentAsByteArray());
    }

    @Test
    void coverFallsBackToFirstPageWhenThumbnailMissing() throws Exception
    {
        TextbookStorageService storageService = mock(TextbookStorageService.class);
        when(storageService.resolve(anyString())).thenReturn(imageFile);
        when(storageService.resolveCover(6L)).thenReturn(null);
        ReflectionTestUtils.setField(controller, "storageService", storageService);

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.cover(6L, new MockHttpServletRequest(), response);

        assertEquals(200, response.getStatus());
        assertArrayEquals(IMAGE_BYTES, response.getContentAsByteArray());
    }

    @Test
    void matchingCoverEtagReturnsNotModified() throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse first = new MockHttpServletResponse();
        controller.cover(6L, request, first);
        request.addHeader("If-None-Match", first.getHeader("ETag"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.cover(6L, request, response);

        assertEquals(304, response.getStatus());
        assertEquals(0, response.getContentAsByteArray().length);
    }
}
