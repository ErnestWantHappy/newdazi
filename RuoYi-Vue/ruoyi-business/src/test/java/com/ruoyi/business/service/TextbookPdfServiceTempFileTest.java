package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ruoyi.business.domain.BizTextbookBook;
import com.ruoyi.business.domain.BizTextbookPage;
import com.ruoyi.business.mapper.BizTextbookMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 缓存目录不可写时会退回系统临时文件；生成中途失败必须清掉半成品，不能把 PDF 垃圾留在系统盘。
 */
class TextbookPdfServiceTempFileTest {

    @TempDir
    Path tempDir;

    @Test
    void failedBuildRemovesTemporaryPdf() throws Exception {
        // 用一个普通文件充当教材根目录，让 .pdf-cache 创建必然失败，从而走系统临时文件分支。
        Path fakeRoot = Files.createFile(tempDir.resolve("readonly-root"));
        TextbookStorageService storage = mock(TextbookStorageService.class);
        when(storage.getRoot()).thenReturn(fakeRoot);
        when(storage.resolve("missing-page.jpg")).thenThrow(new ServiceException("页图缺失"));

        BizTextbookBook book = new BizTextbookBook();
        book.setBookId(1L);
        book.setTitle("测试教材");
        book.setTotalPages(1);
        BizTextbookPage page = new BizTextbookPage();
        page.setPageNo(1);
        page.setSha256("abc");
        page.setImagePath("missing-page.jpg");
        BizTextbookMapper mapper = mock(BizTextbookMapper.class);
        when(mapper.selectBookById(anyLong())).thenReturn(book);
        when(mapper.selectPagesByBookId(anyLong(), org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull())).thenReturn(Collections.singletonList(page));

        TextbookPdfService service = new TextbookPdfService();
        org.springframework.test.util.ReflectionTestUtils.setField(service, "textbookMapper", mapper);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "storageService", storage);

        Path systemTemp = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"));
        Set<Path> before = pdfTemps(systemTemp);
        assertThrows(ServiceException.class, () -> service.buildBookPdf(1L));
        Set<Path> after = pdfTemps(systemTemp);
        after.removeAll(before);
        assertTrue(after.isEmpty(), "失败后仍残留临时文件: " + after);
        assertEquals(0, after.size());
    }

    private Set<Path> pdfTemps(Path dir) throws Exception {
        try (Stream<Path> stream = Files.list(dir)) {
            List<Path> files = stream
                    .filter(path -> path.getFileName().toString().startsWith("textbook-pdf-"))
                    .collect(Collectors.toList());
            return files.stream().collect(Collectors.toSet());
        }
    }
}
