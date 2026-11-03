package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.BizTextbookBook;
import com.ruoyi.business.domain.BizTextbookPage;
import com.ruoyi.business.mapper.BizTextbookMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 种子 SQL 只增不删，目录修正后可能残留旧页行；
 * 页图接口必须以册的总页数为上界，不能让已下架的越界页继续回图。
 */
class TextbookCatalogServicePageBoundsTest
{
    private TextbookCatalogService service;

    private BizTextbookMapper mapper;

    @BeforeEach
    void setUp()
    {
        mapper = mock(BizTextbookMapper.class);
        service = new TextbookCatalogService();
        ReflectionTestUtils.setField(service, "textbookMapper", mapper);

        BizTextbookBook book = new BizTextbookBook();
        book.setBookId(6L);
        book.setTotalPages(125);
        when(mapper.selectBookById(6L)).thenReturn(book);
    }

    @Test
    void pageAboveTotalPagesIsRejectedEvenIfStaleRowExists()
    {
        BizTextbookPage stale = new BizTextbookPage();
        stale.setPageNo(126);
        when(mapper.selectPage(6L, 126)).thenReturn(stale);

        assertThrows(ServiceException.class, () -> service.requirePage(6L, 126));
        verify(mapper, never()).selectPage(anyLong(), anyInt());
    }

    @Test
    void pageWithinRangeStillLoads()
    {
        BizTextbookPage page = new BizTextbookPage();
        page.setPageNo(125);
        when(mapper.selectPage(6L, 125)).thenReturn(page);

        assertSame(page, service.requirePage(6L, 125));
    }

    @Test
    void nonPositiveOrMissingPageIsRejected()
    {
        assertThrows(ServiceException.class, () -> service.requirePage(6L, 0));
        assertThrows(ServiceException.class, () -> service.requirePage(6L, -1));
        assertThrows(ServiceException.class, () -> service.requirePage(6L, null));
        verify(mapper, never()).selectPage(anyLong(), anyInt());
    }
}
