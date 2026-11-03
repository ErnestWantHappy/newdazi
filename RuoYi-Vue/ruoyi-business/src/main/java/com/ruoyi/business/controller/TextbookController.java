package com.ruoyi.business.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizTextbookBook;
import com.ruoyi.business.domain.BizTextbookPage;
import com.ruoyi.business.service.TextbookCatalogService;
import com.ruoyi.business.service.TextbookPdfService;
import com.ruoyi.business.service.TextbookStorageService;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 电子教材。
 *
 * 书架仍只对教师/教研员/管理员开放；学生凭本课绑定直达阅读器，只放行目录、页元数据与页图。
 * 整册/本课 PDF 仅教师侧可用，由页图在后端合成。
 */
@RestController
@RequestMapping("/business/textbook")
@PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")
public class TextbookController
{
    @Autowired
    private TextbookCatalogService catalogService;

    @Autowired
    private TextbookStorageService storageService;

    @Autowired
    private TextbookPdfService pdfService;

    /** 书架：上架册列表 */
    @GetMapping("/books")
    public AjaxResult books()
    {
        List<BizTextbookBook> books = catalogService.listBooks();
        return AjaxResult.success(books);
    }

    /** 目录树：册 + 单元（导入/课次/小结）+ 前置 + 附录 + 页元数据 */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher') or @studentSs.isStudent()")
    @GetMapping("/books/{bookId}/toc")
    public AjaxResult toc(@PathVariable Long bookId)
    {
        return AjaxResult.success(catalogService.getToc(bookId));
    }

    /** 页元数据（页码、印刷页码、尺寸），供阅读器渲染页脚和预加载 */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher') or @studentSs.isStudent()")
    @GetMapping("/books/{bookId}/pages")
    public AjaxResult pages(@PathVariable Long bookId,
                            @RequestParam(required = false) Integer from,
                            @RequestParam(required = false) Integer to)
    {
        List<BizTextbookPage> pages = catalogService.listPages(bookId, from, to);
        return AjaxResult.success(pages);
    }

    /** 整册 PDF：教师端下载；由页图合成，学生和匿名请求均不可用。 */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")
    @GetMapping("/books/{bookId}/pdf")
    public void bookPdf(@PathVariable Long bookId, HttpServletResponse response) throws IOException
    {
        writePdf(pdfService.buildBookPdf(bookId), response);
    }

    /** 本课 PDF：按课次起止页合成，教师端下载。 */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")
    @GetMapping("/books/{bookId}/sections/{sectionKey}/pdf")
    public void sectionPdf(@PathVariable Long bookId, @PathVariable String sectionKey,
                           HttpServletResponse response) throws IOException
    {
        writePdf(pdfService.buildSectionPdf(bookId, sectionKey), response);
    }

    private void writePdf(TextbookPdfService.PdfDownload pdf, HttpServletResponse response) throws IOException
    {
        try
        {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", ContentDisposition.attachment()
                    .filename(pdf.getFilename(), StandardCharsets.UTF_8).build().toString());
            response.setHeader("Cache-Control", "private, no-cache");
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setContentLengthLong(Files.size(pdf.getPath()));
            Files.copy(pdf.getPath(), response.getOutputStream());
        }
        finally
        {
            if (pdf.isTemporary())
            {
                try
                {
                    Files.deleteIfExists(pdf.getPath());
                }
                catch (IOException ignored)
                {
                    // 临时文件清理失败不能覆盖已经完成的下载响应。
                }
            }
        }
    }

    /** 书架封面：优先使用预生成的 360px 缩略图，缺失时回退第 1 页原图，避免书架一次拉 2MB+。 */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher') or @studentSs.isStudent()")
    @GetMapping("/books/{bookId}/cover")
    public void cover(@PathVariable Long bookId, HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        BizTextbookPage page = catalogService.requirePage(bookId, 1);
        Path cover = storageService.resolveCover(bookId);
        Path file = cover != null ? cover : storageService.resolve(page.getImagePath());

        String etag = "\"cover-" + bookId + "-" + Files.size(file) + "-" + Files.getLastModifiedTime(file).toMillis() + "\"";
        response.setHeader("ETag", etag);
        // 封面是低分辨率派生图，按账号缓存 1 小时；Vary 保证共用机换账号时不复用上一个人的缓存。
        response.setHeader("Cache-Control", "private, max-age=3600");
        response.setHeader("Vary", "Authorization");
        response.setHeader("X-Content-Type-Options", "nosniff");

        if (etag.equals(request.getHeader("If-None-Match")))
        {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        response.setContentType("image/jpeg");
        response.setContentLengthLong(Files.size(file));
        Files.copy(file, response.getOutputStream());
    }

    /** 单页图片：鉴权 + ETag + 私有长缓存 */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher') or @studentSs.isStudent()")
    @GetMapping("/page/{bookId}/{pageNo}")
    public void page(@PathVariable Long bookId, @PathVariable Integer pageNo,
                     HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        BizTextbookPage page = catalogService.requirePage(bookId, pageNo);
        Path file = storageService.resolve(page.getImagePath());

        String etag = "\"" + page.getSha256() + "\"";
        response.setHeader("ETag", etag);
        // 教材受版权约束，教师机又常是共用机：不能让浏览器在新鲜期内把图直接交给下一个登录账号，
        // 因此每次回源校验（no-cache），ETag 命中仍返回 304，只省传输不省鉴权。
        response.setHeader("Cache-Control", "private, no-cache");
        response.setHeader("Vary", "Authorization");
        response.setHeader("X-Content-Type-Options", "nosniff");

        // 内容哈希稳定，浏览器带回来的 ETag 命中即可省掉整页传输
        if (etag.equals(request.getHeader("If-None-Match")))
        {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        response.setContentType("image/jpeg");
        response.setContentLengthLong(Files.size(file));
        Files.copy(file, response.getOutputStream());
    }
}
