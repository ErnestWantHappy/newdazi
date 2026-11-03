package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.BizTextbookBook;
import com.ruoyi.business.domain.BizTextbookPage;
import com.ruoyi.business.domain.BizTextbookSection;
import com.ruoyi.business.mapper.BizTextbookMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * PDF 由页图合成：页行缺失或页号不连续时必须直接失败，
 * 不能生成一份少页讲义却返回下载成功。
 */
class TextbookPdfServicePageValidationTest
{
    private TextbookPdfService service;

    private BizTextbookMapper mapper;

    @BeforeEach
    void setUp()
    {
        mapper = mock(BizTextbookMapper.class);
        service = new TextbookPdfService();
        ReflectionTestUtils.setField(service, "textbookMapper", mapper);
        ReflectionTestUtils.setField(service, "storageService", mock(TextbookStorageService.class));

        BizTextbookBook book = new BizTextbookBook();
        book.setBookId(6L);
        book.setTitle("六年级上册");
        book.setTotalPages(4);
        when(mapper.selectBookById(6L)).thenReturn(book);

        BizTextbookSection lesson = new BizTextbookSection();
        lesson.setSectionKey("six-1-lesson-1");
        lesson.setKind("LESSON");
        lesson.setLessonNo(1);
        lesson.setTitle("第1课");
        lesson.setPdfStart(2);
        lesson.setPdfEnd(4);
        when(mapper.selectSectionsByBookId(6L)).thenReturn(Collections.singletonList(lesson));
    }

    @Test
    void bookPdfRejectsMissingPageRows()
    {
        when(mapper.selectPagesByBookId(6L, null, null))
                .thenReturn(Arrays.asList(page(1), page(2), page(3)));

        assertThrows(ServiceException.class, () -> service.buildBookPdf(6L));
    }

    @Test
    void bookPdfRejectsNonContinuousPageNumbers()
    {
        when(mapper.selectPagesByBookId(6L, null, null))
                .thenReturn(Arrays.asList(page(1), page(2), page(4), page(5)));

        assertThrows(ServiceException.class, () -> service.buildBookPdf(6L));
    }

    @Test
    void sectionPdfRejectsShortRange()
    {
        when(mapper.selectPagesByBookId(6L, 2, 4)).thenReturn(Arrays.asList(page(3), page(4)));

        assertThrows(ServiceException.class, () -> service.buildSectionPdf(6L, "six-1-lesson-1"));
    }

    @Test
    void nonLessonSectionIsRejected()
    {
        assertThrows(ServiceException.class, () -> service.buildSectionPdf(6L, "unit-1-intro"));
        verify(mapper, never()).selectPagesByBookId(any(), any(), any());
    }

    private BizTextbookPage page(int pageNo)
    {
        BizTextbookPage page = new BizTextbookPage();
        page.setPageNo(pageNo);
        page.setSha256("sha-" + pageNo);
        return page;
    }
}
