package com.ruoyi.business.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.CollaborationMapper;

/** 版本保存后异步提取结构化摘要，失败只影响审计摘要，不回滚已保存的协作文档。 */
@Service
public class CollaborationRevisionDiffService
{
    @Autowired private CollaborationMapper mapper;
    @Autowired private CollaborationRoomService roomService;

    @Async("conversionExecutor")
    public void extract(Long roomId, Integer versionNo)
    {
        Map<String, Object> pair = mapper.selectRevisionPair(roomId, versionNo);
        if (pair == null) return;
        Long revisionId = number(pair.get("revisionId"));
        try
        {
            String current = text(roomService.resolveStoredFile(String.valueOf(pair.get("filePath"))));
            String previousPath = pair.get("previousFilePath") == null ? null : String.valueOf(pair.get("previousFilePath"));
            String previous = previousPath == null ? "" : text(roomService.resolveStoredFile(previousPath));
            String summary = previousPath == null ? "初始版本，已提取文档结构摘要" : summarize(previous, current);
            mapper.updateRevisionDiff(revisionId, "SUCCESS", summary, null, new Date());
        }
        catch (Exception e)
        {
            mapper.updateRevisionDiff(revisionId, "FAILED", "文档已变化，暂无法结构化描述", safe(e), new Date());
        }
    }

    public void pending(Long roomId, Integer versionNo)
    {
        Map<String, Object> revision = mapper.selectRevisionByRoomVersion(roomId, versionNo);
        if (revision != null) mapper.insertRevisionDiff(number(revision.get("revisionId")), "PENDING", null, null, null);
    }

    private String text(Path file) throws Exception
    {
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".docx")) try (InputStream in = Files.newInputStream(file)) { XWPFDocument d = new XWPFDocument(in); return "段落" + d.getParagraphs().size() + "，表格" + d.getTables().size(); }
        if (name.endsWith(".xlsx") || name.endsWith(".xls")) try (InputStream in = Files.newInputStream(file)) { Workbook w = WorkbookFactory.create(in); int cells = 0, formulas = 0; for (int i=0;i<w.getNumberOfSheets();i++) for (Row r : w.getSheetAt(i)) for (Cell c : r) { cells++; if (c.getCellType() == CellType.FORMULA) formulas++; } return "工作表" + w.getNumberOfSheets() + "，单元格" + cells + "，公式" + formulas; }
        if (name.endsWith(".pptx")) try (InputStream in = Files.newInputStream(file)) { XMLSlideShow p = new XMLSlideShow(in); int chars = 0; for (XSLFSlide s : p.getSlides()) for (XSLFShape shape : s.getShapes()) if (shape instanceof XSLFTextShape) chars += ((XSLFTextShape) shape).getText().length(); return "幻灯片" + p.getSlides().size() + "，文本长度" + chars; }
        if (name.endsWith(".doc")) try (InputStream in = Files.newInputStream(file)) { HWPFDocument d = new HWPFDocument(in); return "旧版 Word 文本长度" + d.getRange().text().length(); }
        if (name.endsWith(".ppt")) try (InputStream in = Files.newInputStream(file)) { HSLFSlideShow p = new HSLFSlideShow(in); int count = 0; for (HSLFSlide ignored : p.getSlides()) count++; return "旧版 PPT 幻灯片" + count; }
        return "文档已变化，当前格式仅记录版本变化";
    }
    private String summarize(String before, String after) { return before.equals(after) ? "结构化内容计数未变化；格式、图片和复杂对象不作作者归属" : "结构化摘要由“" + before + "”变为“" + after + "”；仅表示小组版本变化，不代表保存触发者是全部内容作者"; }
    private static String safe(Exception e) { return StringUtils.left(StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName()).replace('\r',' ').replace('\n',' '), 500); }
    private static Long number(Object value) { return value instanceof Number ? ((Number)value).longValue() : Long.valueOf(String.valueOf(value)); }
}
