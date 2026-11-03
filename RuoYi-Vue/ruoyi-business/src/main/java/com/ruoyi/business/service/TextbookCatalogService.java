package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.BizTextbookBook;
import com.ruoyi.business.domain.BizTextbookPage;
import com.ruoyi.business.domain.BizTextbookSection;
import com.ruoyi.business.domain.BizTextbookUnit;
import com.ruoyi.business.mapper.BizTextbookMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 电子教材目录服务。
 *
 * 只读：册/单元/条目/页全部来自离线导入，运行期不提供编辑入口。
 * 不做"课程↔教材"自动匹配，课次由教师在阅读器手动选择。
 */
@Service
public class TextbookCatalogService
{
    @Autowired
    private BizTextbookMapper textbookMapper;

    /** 上架册列表，按小学/初中分组由前端按 stage 处理 */
    public List<BizTextbookBook> listBooks()
    {
        List<BizTextbookBook> books = textbookMapper.selectBookList();
        for (BizTextbookBook book : books)
        {
            book.setStage(stageOf(book.getGrade()));
        }
        return books;
    }

    /** 目录树：册 + 单元（导入/课次/小结）+ 前置 + 附录 + 页元数据 */
    public Map<String, Object> getToc(Long bookId)
    {
        BizTextbookBook book = requireBook(bookId);
        List<BizTextbookUnit> units = textbookMapper.selectUnitsByBookId(bookId);
        List<BizTextbookSection> sections = textbookMapper.selectSectionsByBookId(bookId);
        List<BizTextbookPage> pages = textbookMapper.selectPagesByBookId(bookId, null, null);

        Map<Long, Map<String, Object>> unitMap = new LinkedHashMap<>();
        List<Map<String, Object>> unitList = new ArrayList<>();
        for (BizTextbookUnit unit : units)
        {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("unitId", unit.getUnitId());
            node.put("unitNo", unit.getUnitNo());
            node.put("title", unit.getTitle());
            node.put("intro", null);
            node.put("lessons", new ArrayList<BizTextbookSection>());
            node.put("summary", null);
            unitMap.put(unit.getUnitId(), node);
            unitList.add(node);
        }

        BizTextbookSection front = null;
        BizTextbookSection appendix = null;
        for (BizTextbookSection section : sections)
        {
            String kind = section.getKind();
            if ("FRONT".equals(kind))
            {
                front = section;
                continue;
            }
            if ("APPENDIX".equals(kind))
            {
                appendix = section;
                continue;
            }
            Map<String, Object> node = section.getUnitId() == null ? null : unitMap.get(section.getUnitId());
            if (node == null)
            {
                // 单元被删但条目还在时跳过，避免整册目录打不开
                continue;
            }
            if ("UNIT_INTRO".equals(kind))
            {
                node.put("intro", section);
            }
            else if ("SUMMARY".equals(kind))
            {
                node.put("summary", section);
            }
            else
            {
                @SuppressWarnings("unchecked")
                List<BizTextbookSection> lessons = (List<BizTextbookSection>) node.get("lessons");
                lessons.add(section);
            }
        }

        List<Map<String, Object>> pageList = new ArrayList<>(pages.size());
        for (BizTextbookPage page : pages)
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("pageNo", page.getPageNo());
            item.put("printedNo", page.getPrintedNo());
            item.put("width", page.getWidth());
            item.put("height", page.getHeight());
            pageList.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("book", book);
        data.put("front", front);
        data.put("units", unitList);
        data.put("appendix", appendix);
        data.put("pages", pageList);
        return data;
    }

    /** 页元数据区间，from/to 为闭区间；越界自动收敛 */
    public List<BizTextbookPage> listPages(Long bookId, Integer from, Integer to)
    {
        BizTextbookBook book = requireBook(bookId);
        int max = book.getTotalPages() == null ? 0 : book.getTotalPages();
        int start = from == null ? 1 : Math.max(1, from);
        int end = to == null ? max : Math.min(max, to);
        if (start > end)
        {
            return new ArrayList<>();
        }
        return textbookMapper.selectPagesByBookId(bookId, start, end);
    }

    public BizTextbookPage requirePage(Long bookId, Integer pageNo)
    {
        BizTextbookBook book = requireBook(bookId);
        // 种子 SQL 只增不删，目录修正后可能残留旧页行；以册的总页数为准，越界页一律不再回图。
        int totalPages = book.getTotalPages() == null ? 0 : book.getTotalPages();
        if (pageNo == null || pageNo < 1 || pageNo > totalPages)
        {
            throw new ServiceException("教材页不存在");
        }
        BizTextbookPage page = textbookMapper.selectPage(bookId, pageNo);
        if (page == null)
        {
            throw new ServiceException("教材页不存在");
        }
        return page;
    }

    public BizTextbookBook requireBook(Long bookId)
    {
        if (bookId == null)
        {
            throw new ServiceException("教材不存在");
        }
        BizTextbookBook book = textbookMapper.selectBookById(bookId);
        if (book == null)
        {
            throw new ServiceException("教材不存在或已下架");
        }
        book.setStage(stageOf(book.getGrade()));
        return book;
    }

    private String stageOf(Integer grade)
    {
        return grade == null || grade <= 6 ? "primary" : "junior";
    }
}
