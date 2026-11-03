package com.ruoyi.business.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.BizTextbookBook;
import com.ruoyi.business.domain.BizTextbookPage;
import com.ruoyi.business.domain.BizTextbookSection;
import com.ruoyi.business.mapper.BizTextbookMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 电子教材 PDF 合成。
 *
 * 教材只有按页渲染的 JPEG，没有服务端原始 PDF；下载文件必须由页图合成。
 * 整册 PDF 生成耗时可观，缓存到教材根目录的 .pdf-cache（不在静态映射内），
 * 课次 PDF 页数少，也复用同一套缓存与并发锁。
 */
@Service
public class TextbookPdfService
{
    private static final Logger log = LoggerFactory.getLogger(TextbookPdfService.class);

    private static final float A4_WIDTH = 595.28f;
    private static final float A4_HEIGHT = 841.89f;
    private static final String CACHE_DIR = ".pdf-cache";
    /** 整册 PDF 每页都要解码一张大图，同时构建多本会叠加内存和 CPU，全局限制并发数。 */
    private static final int MAX_CONCURRENT_BUILDS = 2;
    private static final int CACHE_RETENTION_DAYS = 14;
    private static final int CACHE_MAX_FILES = 40;
    private static final long CACHE_CLEAN_INTERVAL_MS = TimeUnit.HOURS.toMillis(1);

    @Autowired
    private BizTextbookMapper textbookMapper;

    @Autowired
    private TextbookStorageService storageService;

    private final ConcurrentHashMap<String, Object> buildLocks = new ConcurrentHashMap<>();

    private final Semaphore buildPermits = new Semaphore(MAX_CONCURRENT_BUILDS);

    private final AtomicLong lastCacheClean = new AtomicLong(0L);

    public PdfDownload buildBookPdf(Long bookId)
    {
        BizTextbookBook book = requireBook(bookId);
        List<BizTextbookPage> pages = textbookMapper.selectPagesByBookId(bookId, null, null);
        if (pages == null || pages.isEmpty())
        {
            throw new ServiceException("教材页不存在");
        }
        // 页行缺失或页号不连续时宁可直接失败，也不能悄悄产出一份少页讲义。
        int totalPages = book.getTotalPages() == null ? 0 : book.getTotalPages();
        if (pages.size() != totalPages)
        {
            throw new ServiceException("教材页数据不完整，请联系管理员重新导入后再下载");
        }
        assertContinuous(pages, 1, totalPages);
        String filename = safeName(book.getTitle()) + ".pdf";
        return buildCached("book-" + bookId + "-" + versionOf(pages), pages, book.getTitle(), filename);
    }

    public PdfDownload buildSectionPdf(Long bookId, String sectionKey)
    {
        BizTextbookBook book = requireBook(bookId);
        String key = StringUtils.trimToNull(sectionKey);
        if (key == null)
        {
            throw new ServiceException("教材课次不存在");
        }
        BizTextbookSection section = findLesson(bookId, key);
        if (section.getPdfStart() == null || section.getPdfEnd() == null)
        {
            throw new ServiceException("教材课次页码不完整");
        }
        List<BizTextbookPage> pages = textbookMapper.selectPagesByBookId(
                bookId, section.getPdfStart(), section.getPdfEnd());
        if (pages == null || pages.isEmpty())
        {
            throw new ServiceException("教材页不存在");
        }
        int expected = section.getPdfEnd() - section.getPdfStart() + 1;
        if (pages.size() != expected)
        {
            throw new ServiceException("教材页数据不完整，请联系管理员重新导入后再下载");
        }
        assertContinuous(pages, section.getPdfStart(), section.getPdfEnd());
        String label = (section.getLessonNo() == null ? "" : "第" + section.getLessonNo() + "课 ")
                + StringUtils.defaultString(section.getTitle());
        String title = book.getTitle() + " " + label.trim();
        String filename = safeName(title) + ".pdf";
        return buildCached("lesson-" + bookId + "-" + safeKey(key) + "-" + versionOf(pages),
                pages, title, filename);
    }

    /** 页号必须按 from..to 连续且升序，否则 PDF 会缺页或错序。 */
    private void assertContinuous(List<BizTextbookPage> pages, int from, int to)
    {
        int expected = from;
        for (BizTextbookPage page : pages)
        {
            if (page.getPageNo() == null || page.getPageNo().intValue() != expected || expected > to)
            {
                throw new ServiceException("教材页数据不完整，请联系管理员重新导入后再下载");
            }
            expected++;
        }
    }

    private BizTextbookBook requireBook(Long bookId)
    {
        BizTextbookBook book = textbookMapper.selectBookById(bookId);
        if (book == null)
        {
            throw new ServiceException("教材不存在或未上架");
        }
        return book;
    }

    private BizTextbookSection findLesson(Long bookId, String sectionKey)
    {
        List<BizTextbookSection> sections = textbookMapper.selectSectionsByBookId(bookId);
        if (sections != null)
        {
            for (BizTextbookSection section : sections)
            {
                if (sectionKey.equals(section.getSectionKey()) && "LESSON".equals(section.getKind()))
                {
                    return section;
                }
            }
        }
        throw new ServiceException("教材课次不存在");
    }

    /**
     * 缓存键取全部页的页码+内容哈希：只改中间某页时旧缓存也必须失效，不能把旧 PDF 继续发给老师。
     */
    private String versionOf(List<BizTextbookPage> pages)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (BizTextbookPage page : pages)
            {
                digest.update(String.valueOf(page.getPageNo()).getBytes(StandardCharsets.UTF_8));
                digest.update((byte) ':');
                digest.update(StringUtils.defaultString(page.getSha256()).getBytes(StandardCharsets.UTF_8));
                digest.update((byte) '\n');
            }
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++)
            {
                sb.append(String.format("%02x", hash[i] & 0xff));
            }
            return sb + "-" + pages.size();
        }
        catch (NoSuchAlgorithmException e)
        {
            // SHA-256 是 JRE 必备算法，退化路径只保证不阻断下载。
            return shortHash(pages.get(0).getSha256()) + "-" + pages.size();
        }
    }

    private String shortHash(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return "0";
        }
        return value.substring(0, Math.min(12, value.length()));
    }

    private PdfDownload buildCached(String cacheKey, List<BizTextbookPage> pages,
                                    String title, String filename)
    {
        Path cacheDir = cacheDirectory();
        if (cacheDir == null)
        {
            Path temp = null;
            try
            {
                temp = Files.createTempFile("textbook-pdf-", ".pdf");
                buildWithPermit(pages, temp, title);
                return new PdfDownload(temp, filename, true);
            }
            catch (IOException e)
            {
                deleteQuietly(temp);
                throw new ServiceException("PDF 生成失败，请稍后重试");
            }
        }

        Path target = cacheDir.resolve(cacheKey + ".pdf");
        if (isUsable(target))
        {
            return new PdfDownload(target, filename, false);
        }

        Object lock = buildLocks.computeIfAbsent(cacheKey, key -> new Object());
        synchronized (lock)
        {
            try
            {
                if (isUsable(target))
                {
                    return new PdfDownload(target, filename, false);
                }
                Path temp = Files.createTempFile(cacheDir, "build-", ".pdf");
                try
                {
                    buildWithPermit(pages, temp, title);
                    moveIntoPlace(temp, target);
                    cleanCacheQuietly(cacheDir);
                    return new PdfDownload(target, filename, false);
                }
                finally
                {
                    deleteQuietly(temp);
                }
            }
            catch (IOException e)
            {
                log.error("教材 PDF 生成失败 cacheKey={}", cacheKey, e);
                throw new ServiceException("PDF 生成失败，请稍后重试");
            }
            finally
            {
                buildLocks.remove(cacheKey, lock);
            }
        }
    }

    /** 并发上限用信号量兜底：拿不到构建许可时直接提示稍后重试，不排队占满请求线程。 */
    private void buildWithPermit(List<BizTextbookPage> pages, Path target, String title)
    {
        boolean acquired = false;
        try
        {
            acquired = buildPermits.tryAcquire(30, TimeUnit.SECONDS);
            if (!acquired)
            {
                throw new ServiceException("教材 PDF 正在生成，请稍后重试");
            }
            writePdf(pages, target, title);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("PDF 生成失败，请稍后重试");
        }
        finally
        {
            if (acquired)
            {
                buildPermits.release();
            }
        }
    }

    /**
     * 缓存只保留最近使用的 40 份且超过 14 天的自动删除；每小时最多清理一次，避免下载路径反复扫目录。
     */
    private void cleanCacheQuietly(Path cacheDir)
    {
        long now = System.currentTimeMillis();
        long last = lastCacheClean.get();
        if (now - last < CACHE_CLEAN_INTERVAL_MS || !lastCacheClean.compareAndSet(last, now))
        {
            return;
        }
        try
        {
            List<Path> files = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheDir, "*.pdf"))
            {
                for (Path file : stream)
                {
                    files.add(file);
                }
            }
            long cutoff = now - TimeUnit.DAYS.toMillis(CACHE_RETENTION_DAYS);
            files.removeIf(file -> {
                try
                {
                    return Files.getLastModifiedTime(file).toMillis() < cutoff && Files.deleteIfExists(file);
                }
                catch (IOException e)
                {
                    return false;
                }
            });
            if (files.size() > CACHE_MAX_FILES)
            {
                files.sort(Comparator.comparingLong(TextbookPdfService::lastModified));
                for (int i = 0; i < files.size() - CACHE_MAX_FILES; i++)
                {
                    deleteQuietly(files.get(i));
                }
            }
        }
        catch (IOException e)
        {
            log.warn("教材 PDF 缓存清理失败 dir={}", cacheDir, e);
        }
    }

    private static long lastModified(Path file)
    {
        try
        {
            BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
            return attributes.lastModifiedTime().toMillis();
        }
        catch (IOException e)
        {
            return Long.MAX_VALUE;
        }
    }

    private Path cacheDirectory()
    {
        try
        {
            Path dir = storageService.getRoot().resolve(CACHE_DIR);
            Files.createDirectories(dir);
            return dir;
        }
        catch (IOException e)
        {
            // 只读共享目录时退回系统临时文件，功能可用只是每次重新生成。
            log.warn("教材 PDF 缓存目录不可用，改用临时文件", e);
            return null;
        }
    }

    private boolean isUsable(Path path)
    {
        try
        {
            return Files.isRegularFile(path) && Files.size(path) > 0;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    private void moveIntoPlace(Path source, Path target) throws IOException
    {
        try
        {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (AtomicMoveNotSupportedException e)
        {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void writePdf(List<BizTextbookPage> pages, Path target, String title)
    {
        try (PDDocument document = new PDDocument())
        {
            document.getDocumentInformation().setTitle(StringUtils.defaultString(title));
            for (BizTextbookPage page : pages)
            {
                Path imagePath = storageService.resolve(page.getImagePath());
                PDPage pdfPage = new PDPage(new PDRectangle(A4_WIDTH, A4_HEIGHT));
                document.addPage(pdfPage);
                PDImageXObject image = PDImageXObject.createFromFile(imagePath.toString(), document);
                try (PDPageContentStream content = new PDPageContentStream(document, pdfPage))
                {
                    content.drawImage(image, 0, 0, A4_WIDTH, A4_HEIGHT);
                }
            }
            document.save(target.toFile());
        }
        catch (IOException e)
        {
            throw new ServiceException("PDF 生成失败，请稍后重试");
        }
    }

    private void deleteQuietly(Path path)
    {
        if (path == null)
        {
            return;
        }
        try
        {
            Files.deleteIfExists(path);
        }
        catch (IOException ignored)
        {
            // 临时文件清理失败不影响本次下载。
        }
    }

    private String safeKey(String value)
    {
        return StringUtils.defaultString(value).replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private String safeName(String value)
    {
        String name = StringUtils.defaultIfBlank(value, "电子教材")
                .replaceAll("[\\\\/:*?\"<>|\\r\\n\\t]", "_").trim();
        return name.length() > 120 ? name.substring(0, 120) : name;
    }

    public static class PdfDownload
    {
        private final Path path;
        private final String filename;
        private final boolean temporary;

        public PdfDownload(Path path, String filename, boolean temporary)
        {
            this.path = path;
            this.filename = filename;
            this.temporary = temporary;
        }

        public Path getPath()
        {
            return path;
        }

        public String getFilename()
        {
            return filename;
        }

        public boolean isTemporary()
        {
            return temporary;
        }
    }
}
