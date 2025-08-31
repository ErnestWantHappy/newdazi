package com.ruoyi.business.utils;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * 文件转换工具类
 * 专注于将 DOCX 文件转换为 PDF
 */
public class FileConversionUtils {

    private static final Logger log = LoggerFactory.getLogger(FileConversionUtils.class);

    /**
     * 将 DOCX 输入流转换为 PDF 输出流
     * @param docxInputStream DOCX文件的输入流
     * @param pdfOutputStream 转换后PDF文件的输出流
     * @throws Exception 转换过程中可能发生的异常
     */
    public static void convertDocxToPdf(InputStream docxInputStream, OutputStream pdfOutputStream) throws Exception {
        XWPFDocument document = null;
        Document pdfDocument = null;
        try {
            // 1. 使用 POI 读取 Word 文档
            document = new XWPFDocument(docxInputStream);

            // 2. 使用 OpenPDF 创建一个新的 PDF 文档
            pdfDocument = new Document();
            PdfWriter.getInstance(pdfDocument, pdfOutputStream);
            pdfDocument.open();

            // 3. 设置中文字体
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font chineseFont = new Font(bfChinese, 12, Font.NORMAL);

            // 4. 遍历 Word 文档的段落并写入 PDF
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    pdfDocument.add(new Paragraph(text, chineseFont));
                }
            }
            log.info("DOCX to PDF conversion successful.");

        } catch (Exception e) {
            log.error("Error during DOCX to PDF conversion", e);
            throw new Exception("文件转换为PDF预览时发生错误", e);
        } finally {
            // 5. 关闭所有资源
            if (pdfDocument != null) {
                pdfDocument.close();
            }
            if (document != null) {
                document.close();
            }
        }
    }
}