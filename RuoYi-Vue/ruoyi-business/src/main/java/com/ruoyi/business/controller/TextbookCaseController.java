package com.ruoyi.business.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.Semaphore;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.business.mapper.TextbookCaseMapper;
import com.ruoyi.business.service.TextbookStorageService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.business.service.TextbookCaseFeedbackService;
import com.ruoyi.common.utils.SecurityUtils;

/** 教师资源独立鉴权，避免学生共用教材目录接口时取得备课材料。 */
@RestController
@RequestMapping("/business/textbook/cases")
@PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")
public class TextbookCaseController
{
    @Autowired private TextbookCaseMapper mapper;
    @Autowired private TextbookStorageService storage;
    @Autowired private TokenService tokenService;
    @Autowired private TextbookCaseFeedbackService feedbackService;
    private final Semaphore previewSlots = new Semaphore(2);

    @GetMapping("/files/{id}/preview-info")
    public AjaxResult previewInfo(@PathVariable Long id) throws IOException
    {
        Path path = resolveFile(id, true);
        acquirePreviewSlot();
        try (PDDocument document = PDDocument.load(path.toFile(), MemoryUsageSetting.setupTempFileOnly()))
        {
            return AjaxResult.success(Collections.singletonMap("pages", document.getNumberOfPages()));
        }
        finally { previewSlots.release(); }
    }

    /** 逐页回图使手机无需依赖内置PDF插件，并限制同时渲染占用。 */
    @GetMapping("/files/{id}/pages/{pageNo}")
    public void previewPageSized(@PathVariable Long id, @PathVariable int pageNo,
        @RequestParam(defaultValue="false") boolean thumbnail, HttpServletResponse response) throws IOException
    {
        renderPage(id, pageNo, thumbnail, response);
    }

    public void previewPage(Long id, int pageNo, HttpServletResponse response) throws IOException
    {
        renderPage(id, pageNo, false, response);
    }

    private void renderPage(Long id, int pageNo, boolean thumbnail, HttpServletResponse response) throws IOException
    {
        Path path = resolveFile(id, true);
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

    private void acquirePreviewSlot()
    {
        if (!previewSlots.tryAcquire()) throw new ServiceException("预览服务繁忙，请稍后重试");
    }

    /** 浏览器原生下载无法设置Authorization头，仅此只读入口兼容既有登录Cookie。 */
    @Anonymous
    @PreAuthorize("permitAll()")
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        if (requireBrowserTeacher(request, response)) file(id, false, false, response);
    }

    /** 同一教学设计合并后的附加原件（如 Word 版），保持独立下载入口。 */
    @Anonymous
    @PreAuthorize("permitAll()")
    @GetMapping("/download/{id}/extra")
    public void downloadExtra(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        if (requireBrowserTeacher(request, response)) file(id, true, false, response);
    }

    @Anonymous
    @PreAuthorize("permitAll()")
    @GetMapping("/media/{id}")
    public void media(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        if (!requireBrowserTeacher(request, response)) return;
        Path path = resolveFile(id, false);
        String name = path.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        String extension = name.substring(name.lastIndexOf('.') + 1);
        Map<String, String> types = new java.util.HashMap<>();
        // 音频演示文件沿用教师鉴权和 Range 下载，只扩展明确的安全 MIME，不开放任意文件。
        types.put("mp3", "audio/mpeg"); types.put("wav", "audio/wav");
        types.put("m4a", "audio/mp4"); types.put("ogg", "audio/ogg");
        types.put("mp4", "video/mp4"); types.put("webm", "video/webm");
        types.put("jpg", "image/jpeg"); types.put("jpeg", "image/jpeg");
        types.put("png", "image/png"); types.put("webp", "image/webp");
        String type = types.get(extension);
        if (type == null) throw new ServiceException("该文件请下载后使用");
        long length = Files.size(path);
        long start = 0;
        long end = length - 1;
        String range = request.getHeader("Range");
        if (range != null)
        {
            try
            {
                if (!range.matches("bytes=\\d*-\\d*")) throw new IllegalArgumentException();
                String[] bounds = range.substring(6).split("-", -1);
                if (bounds[0].isEmpty())
                {
                    long suffix = Long.parseLong(bounds[1]);
                    if (suffix <= 0) throw new IllegalArgumentException();
                    start = Math.max(0, length - suffix);
                }
                else
                {
                    start = Long.parseLong(bounds[0]);
                    if (!bounds[1].isEmpty()) end = Math.min(end, Long.parseLong(bounds[1]));
                }
                if (start >= length || start > end) throw new IllegalArgumentException();
            }
            catch (IllegalArgumentException error)
            {
                response.setHeader("Content-Range", "bytes */" + length);
                response.setStatus(416);
                return;
            }
            response.setStatus(206);
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + length);
        }
        response.setContentType(type);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Cache-Control", "private, no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLengthLong(Math.max(0, end - start + 1));
        if ("HEAD".equals(request.getMethod())) return;
        try (java.io.RandomAccessFile input = new java.io.RandomAccessFile(path.toFile(), "r"))
        {
            input.seek(start);
            byte[] buffer = new byte[64 * 1024];
            long remaining = end - start + 1;
            while (remaining > 0)
            {
                int read = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (read < 0) break;
                response.getOutputStream().write(buffer, 0, read);
                remaining -= read;
            }
        }
    }

    /**
     * 作者网页一律作为沙箱文档打开：CSP sandbox 只给脚本/表单/弹窗权限，
     * 不给 allow-same-origin，因此读不到平台 Cookie、localStorage 与父页面。
     */
    @Anonymous
    @PreAuthorize("permitAll()")
    @GetMapping("/html/{id}")
    public void html(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        if (!requireBrowserTeacher(request, response)) return;
        Path path = resolveFile(id, false);
        String name = path.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        if (!name.endsWith(".html") && !name.endsWith(".htm")) throw new ServiceException("该资源不是网页");
        long size = Files.size(path);
        if (size > 16L * 1024 * 1024) throw new ServiceException("网页文件较大，请下载原件查看");
        response.setContentType("text/html; charset=utf-8");
        response.setHeader("Content-Security-Policy", "sandbox allow-scripts allow-forms allow-modals allow-popups");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "private, no-store");
        response.setContentLengthLong(size);
        Files.copy(path, response.getOutputStream());
    }

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
            response.sendError(403, "仅教师可下载课例资源");
            return false;
        }
        return true;
    }

    @GetMapping("/summary")
    public AjaxResult summary()
    {
        return AjaxResult.success(mapper.summary());
    }

    @GetMapping("/book/{bookId}/section/{sectionKey}")
    public AjaxResult cases(@PathVariable Long bookId, @PathVariable String sectionKey)
    {
        List<Map<String, Object>> cases = mapper.cases(bookId, sectionKey);
        for (Map<String, Object> item : cases)
        {
            item.put("resources", mapper.resources(((Number) item.get("caseId")).longValue()));
            item.putAll(feedbackService.summary(((Number) item.get("caseId")).longValue(), SecurityUtils.getUserId()));
        }
        return AjaxResult.success(cases);
    }

    @PreAuthorize("@ss.hasRole('teacher')")
    @PutMapping("/{caseId}/feedback")
    public AjaxResult feedback(@PathVariable Long caseId, @RequestBody Map<String, Object> body)
    {
        requireVotingTeacher();
        Object rawVote = body.get("vote");
        if (!(rawVote instanceof Number)) throw new ServiceException("请选择推荐或不推荐");
        int vote = ((Number) rawVote).intValue();
        if (((Number) rawVote).doubleValue() != vote) throw new ServiceException("请选择推荐或不推荐");
        String reason = body.get("reason") == null ? null : String.valueOf(body.get("reason"));
        return AjaxResult.success(feedbackService.vote(caseId, SecurityUtils.getUserId(), vote, reason));
    }

    @GetMapping("/{caseId}/feedback/comments")
    public AjaxResult comments(@PathVariable Long caseId)
    {
        return AjaxResult.success(feedbackService.comments(caseId));
    }

    @PreAuthorize("@ss.hasRole('teacher')")
    @PostMapping("/{caseId}/invitation")
    public AjaxResult invitation(@PathVariable Long caseId)
    {
        requireVotingTeacher();
        return AjaxResult.success(Collections.singletonMap("show", feedbackService.invitation(caseId, SecurityUtils.getUserId())));
    }

    private void requireVotingTeacher()
    {
        // 通用权限表达式会放行超级管理员，但课例评价只统计真实教师角色。
        LoginUser user = SecurityUtils.getLoginUser();
        if (user.getUser() == null || user.getUser().getRoles() == null
            || user.getUser().getRoles().stream().noneMatch(role -> "teacher".equals(role.getRoleKey())))
            throw new org.springframework.security.access.AccessDeniedException("仅教师可评价课例");
    }

    @GetMapping("/files/{id}")
    public void file(@PathVariable Long id, @RequestParam(defaultValue="false") boolean preview,
                     HttpServletResponse response) throws IOException
    {
        file(id, false, preview, response);
    }

    private void file(Long id, boolean extra, boolean preview, HttpServletResponse response) throws IOException
    {
        Map<String, Object> file = mapper.file(id);
        if (file == null) throw new ServiceException("资源已下架或不存在");
        Path target = resolveFile(id, preview, extra);
        String name = String.valueOf(extra ? file.get("extraFilename") : file.get("filename"));
        response.setContentType(preview ? "application/pdf" : "application/octet-stream");
        response.setHeader("Content-Disposition", (preview ? ContentDisposition.inline() : ContentDisposition.attachment())
            .filename(preview ? "preview.pdf" : name, StandardCharsets.UTF_8).build().toString());
        response.setHeader("Cache-Control", "private, no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLengthLong(Files.size(target));
        Files.copy(target, response.getOutputStream());
    }

    private Path resolveFile(Long id, boolean preview) throws IOException
    {
        return resolveFile(id, preview, false);
    }

    private Path resolveFile(Long id, boolean preview, boolean extra) throws IOException
    {
        Map<String, Object> file = mapper.file(id);
        if (file == null) throw new ServiceException("资源已下架或不存在");
        Object relative = file.get(extra ? "extraPath" : (preview ? "previewPath" : "originalPath"));
        if (relative == null) throw new ServiceException(extra ? "该资源没有附加原件" : "预览尚未准备好，请下载原件");
        // realpath 同时阻止越界路径和符号链接跳出资源目录。
        Path root = storage.getRoot().resolve("cases").toRealPath();
        Path target = root.resolve(relative.toString()).normalize();
        if (!target.startsWith(root) || !Files.isRegularFile(target) || !target.toRealPath().startsWith(root))
            throw new ServiceException("资源文件不存在");
        if (preview && !target.toString().toLowerCase(java.util.Locale.ROOT).endsWith(".pdf"))
            throw new ServiceException("该格式暂不支持在线预览");
        if (preview && Files.size(target) > 100L * 1024 * 1024) throw new ServiceException("该预览文件较大，请下载原件查看");
        return target;
    }
}
