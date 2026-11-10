package com.ruoyi.business.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.business.mapper.TeachingResourceMapper;
import com.ruoyi.business.service.TextbookStorageService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.framework.web.service.TokenService;

/**
 * 整册教学设计汇编、课程指南与配套课件。
 *
 * 与教材阅读分开鉴权：学生可以继续看教材，但不能取得这些备课资源。
 * 所有文件仍放在教材根目录之外，由后端鉴权后按页回图或按原文件下载。
 */
@RestController
@RequestMapping("/business/textbook/resources")
@PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")
public class TeachingResourceController
{
    @Autowired private TeachingResourceMapper mapper;
    @Autowired private TextbookStorageService storage;
    @Autowired private TokenService tokenService;
    private final Semaphore previewSlots = new Semaphore(2);

    // ------------------------------------------------------------------ 册级汇编

    @GetMapping("/book-resources")
    public AjaxResult allBookResources()
    {
        return AjaxResult.success(mapper.allBookResources());
    }

    @GetMapping("/books/{bookId}/book-resources")
    public AjaxResult bookResources(@PathVariable Long bookId)
    {
        return AjaxResult.success(mapper.bookResources(bookId));
    }

    @GetMapping("/book-files/{id}/preview-info")
    public AjaxResult bookPreviewInfo(@PathVariable Long id) throws IOException
    {
        return previewInfo(mapper.bookFile(id));
    }

    @GetMapping("/book-files/{id}/pages/{pageNo}")
    public void bookPreviewPage(@PathVariable Long id, @PathVariable int pageNo,
        @RequestParam(defaultValue = "false") boolean thumbnail, HttpServletResponse response) throws IOException
    {
        renderPage(mapper.bookFile(id), pageNo, thumbnail, response);
    }

    @Anonymous
    @PreAuthorize("permitAll()")
    @GetMapping("/book-files/{id}/download")
    public void bookDownload(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        if (!requireBrowserTeacher(request, response)) return;
        download(mapper.bookFile(id), false, response);
    }

    // ------------------------------------------------------------------ 课程指南

    @GetMapping("/guides")
    public AjaxResult guides()
    {
        return AjaxResult.success(mapper.guides());
    }

    @GetMapping("/standards")
    public AjaxResult standards()
    {
        return AjaxResult.success(mapper.standards());
    }

    @GetMapping("/standards/{key}/preview-info")
    public AjaxResult standardPreviewInfo(@PathVariable String key) throws IOException
    {
        return previewInfo(mapper.standardFile(key));
    }

    @GetMapping("/standards/{key}/pages/{pageNo}")
    public void standardPreviewPage(@PathVariable String key, @PathVariable int pageNo,
        @RequestParam(defaultValue = "false") boolean thumbnail, HttpServletResponse response) throws IOException
    {
        renderPage(mapper.standardFile(key), pageNo, thumbnail, response);
    }

    @Anonymous
    @PreAuthorize("permitAll()")
    @GetMapping("/standards/{key}/download")
    public void standardDownload(@PathVariable String key, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        if (!requireBrowserTeacher(request, response)) return;
        download(mapper.standardFile(key), false, response);
    }

    @GetMapping("/guides/{grade}")
    public AjaxResult guide(@PathVariable Integer grade)
    {
        Map<String, Object> guide = mapper.guide(grade);
        if (guide == null) throw new ServiceException("该年级指南暂未上架");
        guide.put("lessons", mapper.guideLessons(grade));
        return AjaxResult.success(guide);
    }

    @GetMapping("/guides/{grade}/preview-info")
    public AjaxResult guidePreviewInfo(@PathVariable Integer grade) throws IOException
    {
        return previewInfo(guideFile(grade));
    }

    @GetMapping("/guides/{grade}/pages/{pageNo}")
    public void guidePreviewPage(@PathVariable Integer grade, @PathVariable int pageNo,
        @RequestParam(defaultValue = "false") boolean thumbnail, HttpServletResponse response) throws IOException
    {
        renderPage(guideFile(grade), pageNo, thumbnail, response);
    }

    @Anonymous
    @PreAuthorize("permitAll()")
    @GetMapping("/guides/{grade}/download")
    public void guideDownload(@PathVariable Integer grade, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        if (!requireBrowserTeacher(request, response)) return;
        download(guideFile(grade), false, response);
    }

    /** 使用说明各年级同源，按年级入口下载同一个文件，避免重复占空间。 */
    @Anonymous
    @PreAuthorize("permitAll()")
    @GetMapping("/guides/{grade}/usage")
    public void guideUsage(@PathVariable Integer grade, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        if (!requireBrowserTeacher(request, response)) return;
        Map<String, Object> guide = mapper.guide(grade);
        if (guide == null || guide.get("usagePath") == null) throw new ServiceException("使用说明暂未提供");
        Map<String, Object> file = new java.util.HashMap<>();
        file.put("filename", guide.get("usageFilename"));
        file.put("originalPath", guide.get("usagePath"));
        download(file, false, response);
    }

    @GetMapping("/guide-lessons/{id}/preview-info")
    public AjaxResult lessonPreviewInfo(@PathVariable Long id) throws IOException
    {
        return previewInfo(lessonFile(id));
    }

    @GetMapping("/guide-lessons/{id}/pages/{pageNo}")
    public void lessonPreviewPage(@PathVariable Long id, @PathVariable int pageNo,
        @RequestParam(defaultValue = "false") boolean thumbnail, HttpServletResponse response) throws IOException
    {
        renderPage(lessonFile(id), pageNo, thumbnail, response);
    }

    @Anonymous
    @PreAuthorize("permitAll()")
    @GetMapping("/guide-lessons/{id}/download")
    public void lessonDownload(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        if (!requireBrowserTeacher(request, response)) return;
        download(lessonFile(id), false, response);
    }

    // ------------------------------------------------------------------ 公共

    private Map<String, Object> guideFile(Integer grade) throws IOException
    {
        Map<String, Object> guide = mapper.guide(grade);
        if (guide == null) throw new ServiceException("该年级指南暂未上架");
        Map<String, Object> file = new java.util.HashMap<>();
        file.put("filename", guide.get("pdfFilename"));
        file.put("originalPath", guide.get("pdfPath"));
        file.put("previewPath", guide.get("pdfPath"));
        return file;
    }

    private Map<String, Object> lessonFile(Long id)
    {
        Map<String, Object> file = mapper.guideLessonFile(id);
        if (file == null) throw new ServiceException("该课件暂未提供或已下架");
        return file;
    }

    private AjaxResult previewInfo(Map<String, Object> file) throws IOException
    {
        Path path = resolveFile(file, true);
        acquirePreviewSlot();
        try (PDDocument document = PDDocument.load(path.toFile(), MemoryUsageSetting.setupTempFileOnly()))
        {
            return AjaxResult.success(Collections.singletonMap("pages", document.getNumberOfPages()));
        }
        finally { previewSlots.release(); }
    }

    private void renderPage(Map<String, Object> file, int pageNo, boolean thumbnail, HttpServletResponse response) throws IOException
    {
        if (file == null) throw new ServiceException("资源不存在或已下架");
        Path path = resolveFile(file, true);
        acquirePreviewSlot();
        try (PDDocument document = PDDocument.load(path.toFile(), MemoryUsageSetting.setupTempFileOnly()))
        {
            if (pageNo < 1 || pageNo > document.getNumberOfPages()) throw new ServiceException("预览页码不存在");
            org.apache.pdfbox.pdmodel.common.PDRectangle size = document.getPage(pageNo - 1).getCropBox();
            float scale = Math.min((thumbnail ? 180f : 1200f) / size.getWidth(), (thumbnail ? 240f : 1600f) / size.getHeight());
            if (!Float.isFinite(scale) || scale <= 0) throw new ServiceException("文件页面尺寸异常");
            BufferedImage image = new PDFRenderer(document).renderImage(pageNo - 1, scale, ImageType.RGB);
            response.setContentType("image/jpeg");
            response.setHeader("Cache-Control", "private, no-store");
            response.setHeader("X-Content-Type-Options", "nosniff");
            ImageIO.write(image, "jpg", response.getOutputStream());
        }
        finally { previewSlots.release(); }
    }

    private void download(Map<String, Object> file, boolean preview, HttpServletResponse response) throws IOException
    {
        if (file == null) throw new ServiceException("资源不存在或已下架");
        Path path = resolveFile(file, false);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", ContentDisposition.attachment()
            .filename(String.valueOf(file.get("filename")), StandardCharsets.UTF_8).build().toString());
        response.setHeader("Cache-Control", "private, no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLengthLong(Files.size(path));
        Files.copy(path, response.getOutputStream());
    }

    private Path resolveFile(Map<String, Object> file, boolean preview) throws IOException
    {
        if (file == null) throw new ServiceException("资源不存在或已下架");
        Object relative = file.get(preview ? "previewPath" : "originalPath");
        if (relative == null || String.valueOf(relative).isEmpty()) throw new ServiceException("该资源暂不支持在线预览，请下载原件");
        Path root = storage.getRoot().resolve("cases").toRealPath();
        Path target = root.resolve(String.valueOf(relative)).normalize();
        if (!target.startsWith(root) || !Files.isRegularFile(target) || !target.toRealPath().startsWith(root))
            throw new ServiceException("资源文件不存在");
        if (preview && !target.toString().toLowerCase(java.util.Locale.ROOT).endsWith(".pdf"))
            throw new ServiceException("该格式暂不支持在线预览");
        if (preview && Files.size(target) > 100L * 1024 * 1024) throw new ServiceException("该预览文件较大，请下载原件查看");
        return target;
    }

    private void acquirePreviewSlot()
    {
        if (!previewSlots.tryAcquire()) throw new ServiceException("预览服务繁忙，请稍后重试");
    }

    /** 浏览器原生下载无法带 Authorization，这里只兼容既有登录 Cookie。 */
    private boolean requireBrowserTeacher(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        LoginUser user = tokenService.getLoginUser(request);
        if (user == null && request.getCookies() != null)
        {
            for (Cookie cookie : request.getCookies())
            {
                if ("Admin-Token".equals(cookie.getName()))
                {
                    user = tokenService.getLoginUser(cookie.getValue());
                    break;
                }
            }
        }
        if (user == null)
        {
            response.sendError(401, "请先登录");
            return false;
        }
        boolean allowed = user.getUser() != null && user.getUser().getRoles() != null
            && user.getUser().getRoles().stream().anyMatch(role -> "admin".equals(role.getRoleKey())
                || "teacher".equals(role.getRoleKey()) || "researcher".equals(role.getRoleKey()));
        if (!allowed)
        {
            response.sendError(403, "仅教师可下载教学资源");
            return false;
        }
        return true;
    }
}
