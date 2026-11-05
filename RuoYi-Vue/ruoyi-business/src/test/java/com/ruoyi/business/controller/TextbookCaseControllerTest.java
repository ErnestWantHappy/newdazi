package com.ruoyi.business.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import javax.servlet.http.Cookie;
import java.util.Collections;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.framework.web.service.TokenService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.mapper.TextbookCaseMapper;
import com.ruoyi.business.service.TextbookStorageService;
import com.ruoyi.common.exception.ServiceException;

class TextbookCaseControllerTest
{
    @Test void dualRoleTeacherMayVoteButPureResearcherAndAdminMayNot()
    {
        TextbookCaseController controller = new TextbookCaseController();
        com.ruoyi.business.service.TextbookCaseFeedbackService feedback = mock(com.ruoyi.business.service.TextbookCaseFeedbackService.class);
        ReflectionTestUtils.setField(controller, "feedbackService", feedback);
        LoginUser login = new LoginUser();
        login.setUserId(2L);
        SysUser user = new SysUser();
        login.setUser(user);
        SysRole teacher = new SysRole(); teacher.setRoleKey("teacher");
        SysRole researcher = new SysRole(); researcher.setRoleKey("researcher");
        SysRole admin = new SysRole(); admin.setRoleKey("admin");
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(login, null, Collections.emptyList()));
        try
        {
            user.setRoles(java.util.Arrays.asList(researcher, teacher));
            controller.feedback(1L, Collections.singletonMap("vote", 1));
            verify(feedback).vote(1L, 2L, 1, null);
            for (SysRole role : java.util.Arrays.asList(researcher, admin))
            {
                user.setRoles(Collections.singletonList(role));
                assertThrows(org.springframework.security.access.AccessDeniedException.class,
                    () -> controller.feedback(1L, Collections.singletonMap("vote", 1)));
            }
        }
        finally { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }
    }

    @TempDir Path root;

    private TextbookCaseController controller(String original, String preview) throws Exception
    {
        Files.createDirectories(root.resolve("cases"));
        TextbookStorageService storage = mock(TextbookStorageService.class);
        when(storage.getRoot()).thenReturn(root);
        Map<String, Object> row = new HashMap<>();
        row.put("filename", "教学设计.docx");
        row.put("originalPath", original);
        row.put("previewPath", preview);
        TextbookCaseMapper mapper = mock(TextbookCaseMapper.class);
        when(mapper.file(1L)).thenReturn(row);
        TextbookCaseController controller = new TextbookCaseController();
        ReflectionTestUtils.setField(controller, "storage", storage);
        ReflectionTestUtils.setField(controller, "mapper", mapper);
        return controller;
    }

    @Test void traversalCannotReadOutsideResourceRoot() throws Exception
    {
        Files.write(root.resolve("secret.pdf"), new byte[] {1});
        TextbookCaseController controller = controller("../secret.pdf", "../secret.pdf");
        assertThrows(ServiceException.class, () -> controller.file(1L, true, new MockHttpServletResponse()));
    }

    @Test void previewOnlyAllowsPdfAndPrivateResponse() throws Exception
    {
        TextbookCaseController controller = controller("file.html", "file.html");
        Files.write(root.resolve("cases/file.html"), new byte[] {1});
        assertThrows(ServiceException.class, () -> controller.file(1L, true, new MockHttpServletResponse()));
        TextbookCaseController pdfController = controller("file.pdf", "file.pdf");
        Files.write(root.resolve("cases/file.pdf"), new byte[] {1, 2});
        MockHttpServletResponse response = new MockHttpServletResponse();
        pdfController.file(1L, true, response);
        assertEquals("application/pdf", response.getContentType());
        assertEquals("private, no-store", response.getHeader("Cache-Control"));
        assertArrayEquals(new byte[] {1, 2}, response.getContentAsByteArray());
    }

    @Test void unpublishedFileIsRejected() throws Exception
    {
        TextbookCaseController controller = controller("file.pdf", null);
        assertThrows(ServiceException.class, () -> controller.file(2L, false, new MockHttpServletResponse()));
    }

    @Test void teacherRoleGuardIsExplicit()
    {
        assertEquals("@ss.hasAnyRoles('admin,teacher,researcher')",
            TextbookCaseController.class.getAnnotation(PreAuthorize.class).value());
    }

    @Test void browserDownloadRejectsMissingSession() throws Exception
    {
        TextbookCaseController controller = controller("file.pdf", null);
        TokenService tokens = mock(TokenService.class);
        ReflectionTestUtils.setField(controller, "tokenService", tokens);
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.download(1L, new MockHttpServletRequest(), response);
        assertEquals(401, response.getStatus());
    }

    @Test void browserDownloadChecksRoleBeforeOpeningFile() throws Exception
    {
        TextbookCaseController controller = controller("file.pdf", null);
        TokenService tokens = mock(TokenService.class);
        ReflectionTestUtils.setField(controller, "tokenService", tokens);
        LoginUser user = new LoginUser();
        SysUser account = new SysUser();
        SysRole role = new SysRole();
        role.setRoleKey("student");
        account.setRoles(Collections.singletonList(role));
        user.setUser(account);
        when(tokens.getLoginUser("local-test-session")).thenReturn(user);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("Admin-Token", "local-test-session"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.download(1L, request, response);
        assertEquals(403, response.getStatus());
        role.setRoleKey("teacher");
        Files.write(root.resolve("cases/file.pdf"), new byte[] {1, 2});
        response = new MockHttpServletResponse();
        controller.download(1L, request, response);
        assertEquals(200, response.getStatus());
        assertArrayEquals(new byte[] {1, 2}, response.getContentAsByteArray());
    }

    @Test void pdfPreviewReturnsCountAndActualJpegWithPageBounds() throws Exception
    {
        TextbookCaseController controller = controller("file.pdf", "file.pdf");
        try (org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument())
        {
            document.addPage(new org.apache.pdfbox.pdmodel.PDPage());
            document.save(root.resolve("cases/file.pdf").toFile());
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> info = (Map<String, Object>) controller.previewInfo(1L).get("data");
        assertEquals(1, info.get("pages"));
        assertThrows(ServiceException.class, () -> controller.previewPage(1L, 0, new MockHttpServletResponse()));
        assertThrows(ServiceException.class, () -> controller.previewPage(1L, 2, new MockHttpServletResponse()));
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.previewPage(1L, 1, response);
        assertEquals("image/jpeg", response.getContentType());
        assertNotNull(javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(response.getContentAsByteArray())));
    }

    @Test void htmlPreviewIsSandboxedAndOnlyForTeachers() throws Exception
    {
        TextbookCaseController controller = controller("demo.html", null);
        TokenService tokens = mock(TokenService.class);
        ReflectionTestUtils.setField(controller, "tokenService", tokens);
        LoginUser user = new LoginUser();
        SysUser account = new SysUser();
        SysRole role = new SysRole();
        role.setRoleKey("teacher");
        account.setRoles(Collections.singletonList(role));
        user.setUser(account);
        when(tokens.getLoginUser(any(javax.servlet.http.HttpServletRequest.class))).thenReturn(user);
        when(tokens.getLoginUser("local-test-session")).thenReturn(user);
        Files.write(root.resolve("cases/demo.html"), "<html><body>demo</body></html>".getBytes("UTF-8"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.html(1L, request, response);
        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=utf-8", response.getContentType());
        String policy = response.getHeader("Content-Security-Policy");
        assertTrue(policy.contains("sandbox"));
        assertTrue(policy.contains("allow-scripts"));
        assertFalse(policy.contains("allow-same-origin"));
        assertEquals("no-referrer", response.getHeader("Referrer-Policy"));
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertEquals("private, no-store", response.getHeader("Cache-Control"));

        // 学生只拿到 403，读不到作者网页内容。
        role.setRoleKey("student");
        response = new MockHttpServletResponse();
        controller.html(1L, request, response);
        assertEquals(403, response.getStatus());
        assertEquals(0, response.getContentAsByteArray().length);
    }

    @Test void htmlPreviewRejectsNonHtmlResource() throws Exception
    {
        TextbookCaseController controller = controller("file.pdf", null);
        TokenService tokens = mock(TokenService.class);
        ReflectionTestUtils.setField(controller, "tokenService", tokens);
        LoginUser user = new LoginUser();
        SysUser account = new SysUser();
        SysRole role = new SysRole();
        role.setRoleKey("teacher");
        account.setRoles(Collections.singletonList(role));
        user.setUser(account);
        when(tokens.getLoginUser(any(javax.servlet.http.HttpServletRequest.class))).thenReturn(user);
        Files.write(root.resolve("cases/file.pdf"), new byte[] {1});
        assertThrows(ServiceException.class, () -> controller.html(1L, new MockHttpServletRequest("GET", "/"),
            new MockHttpServletResponse()));
    }

    @Test void mediaSupportsBoundedRangesAndRejectsInvalidRanges() throws Exception
    {
        TextbookCaseController controller = controller("clip.mp4", null);
        TokenService tokens = mock(TokenService.class);
        ReflectionTestUtils.setField(controller, "tokenService", tokens);
        LoginUser user = new LoginUser();
        SysUser account = new SysUser();
        SysRole role = new SysRole();
        role.setRoleKey("teacher");
        account.setRoles(Collections.singletonList(role));
        user.setUser(account);
        when(tokens.getLoginUser(any(javax.servlet.http.HttpServletRequest.class))).thenReturn(user);
        Files.write(root.resolve("cases/clip.mp4"), new byte[] {0, 1, 2, 3, 4});
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        request.addHeader("Range", "bytes=1-3");
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.media(1L, request, response);
        assertEquals(206, response.getStatus());
        assertEquals("bytes 1-3/5", response.getHeader("Content-Range"));
        assertArrayEquals(new byte[] {1, 2, 3}, response.getContentAsByteArray());
        for (String invalid : new String[] {"bytes=-0", "bytes=3-1", "bytes=999-", "bytes=0-1,3-4"})
        {
            request = new MockHttpServletRequest("GET", "/");
            request.addHeader("Range", invalid);
            response = new MockHttpServletResponse();
            controller.media(1L, request, response);
            assertEquals(416, response.getStatus());
        }
    }
}
