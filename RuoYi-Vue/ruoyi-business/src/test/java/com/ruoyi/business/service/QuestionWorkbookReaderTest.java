package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;
import com.ruoyi.business.service.QuestionWorkbookReader.*;

class QuestionWorkbookReaderTest {
    private final QuestionWorkbookReader reader=new QuestionWorkbookReader();
    private static final String[] HEAD={"题目类型","题目内容","选项A","选项B","选项C","选项D","标准答案","年级","学期"};
    private Workbook book(boolean old) {
        return book(old,HEAD);
    }
    private Workbook book(boolean old,String[] headers) {
        Workbook b=old?new HSSFWorkbook():new XSSFWorkbook();Sheet s=b.createSheet("题库");Row h=s.createRow(0);for(int i=0;i<headers.length;i++){h.createCell(i).setCellValue(headers[i]);s.setColumnWidth(i,30*256);}return b;
    }
    private void row(Workbook b,int n,String stem) {
        Row r=b.getSheetAt(0).createRow(n);r.setHeightInPoints(160);String[] values={"选择题",stem,"甲","乙","丙","丁","A","六年级","上册"};for(int i=0;i<values.length;i++)r.createCell(i).setCellValue(values[i]);
    }
    private void pic(Workbook b,int r,int c,int endRow,int endCol) throws Exception {
        int index=b.addPicture(png(),Workbook.PICTURE_TYPE_PNG);
        ClientAnchor a=b.getCreationHelper().createClientAnchor();a.setRow1(r);a.setRow2(endRow);a.setCol1(c);a.setCol2(endCol);b.getSheetAt(0).createDrawingPatriarch().createPicture(a,index);
    }
    private byte[] png() throws Exception {
        ByteArrayOutputStream png=new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(100,60,BufferedImage.TYPE_INT_RGB),"png",png);
        return png.toByteArray();
    }
    private byte[] withZipEntries(byte[] source,Map<String,byte[]> extra) throws Exception {
        ByteArrayOutputStream output=new ByteArrayOutputStream();
        try(ZipInputStream input=new ZipInputStream(new java.io.ByteArrayInputStream(source));ZipOutputStream zip=new ZipOutputStream(output)) {
            ZipEntry entry;byte[] buffer=new byte[8192];
            while((entry=input.getNextEntry())!=null) {
                zip.putNextEntry(new ZipEntry(entry.getName()));
                if("[Content_Types].xml".equals(entry.getName())) {
                    byte[] original=readAll(input);
                    StringBuilder overrides=new StringBuilder();
                    for(String name:extra.keySet())overrides.append("<Override PartName=\"/").append(name).append("\" ContentType=\"")
                        .append(name.endsWith(".png")?"image/png":name.contains("cellimages")?"application/vnd.wps-officedocument.cellimages+xml":"application/octet-stream").append("\"/>");
                    String xml=new String(original,StandardCharsets.UTF_8).replace("</Types>",overrides+"</Types>");
                    zip.write(xml.getBytes(StandardCharsets.UTF_8));
                } else {
                    int count;while((count=input.read(buffer))!=-1)zip.write(buffer,0,count);
                }
                zip.closeEntry();
            }
            for(Map.Entry<String,byte[]> item:extra.entrySet()) {
                zip.putNextEntry(new ZipEntry(item.getKey()));
                zip.write(item.getValue());
                zip.closeEntry();
            }
        }
        return output.toByteArray();
    }
    private byte[] readAll(ZipInputStream input) throws Exception {
        ByteArrayOutputStream output=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int count;
        while((count=input.read(buffer))!=-1)output.write(buffer,0,count);
        return output.toByteArray();
    }
    private Map<String,byte[]> wpsEntries() throws Exception {
        String cellImages="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            +"<etc:cellImages xmlns:etc=\"http://www.wps.cn/officeDocument/2017/etCustomData\" "
            +"xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\" "
            +"xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" "
            +"xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
            +"<etc:cellImage><xdr:pic><xdr:nvPicPr><xdr:cNvPr id=\"1\" name=\"ID_TEST\"/><xdr:cNvPicPr/></xdr:nvPicPr>"
            +"<xdr:blipFill><a:blip r:embed=\"rId1\"/></xdr:blipFill></xdr:pic></etc:cellImage></etc:cellImages>";
        String rels="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            +"<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            +"<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"media/wps-test.png\"/>"
            +"</Relationships>";
        Map<String,byte[]> extra=new LinkedHashMap<>();
        extra.put("xl/cellimages.xml",cellImages.getBytes(StandardCharsets.UTF_8));
        extra.put("xl/_rels/cellimages.xml.rels",rels.getBytes(StandardCharsets.UTF_8));
        extra.put("xl/media/wps-test.png",png());
        return extra;
    }
    private byte[] bytes(Workbook b) throws Exception {ByteArrayOutputStream out=new ByteArrayOutputStream();b.write(out);return out.toByteArray();}
    @Test void sameCellTextAndPictureBothSurvive() throws Exception {
        try(Workbook b=book(false)){row(b,1,"观察图片");pic(b,1,1,2,2);Result result=reader.read(bytes(b));assertEquals("观察图片",result.rows.get(0).question.getQuestionContent());assertEquals(1,result.rows.get(0).pictures.size());assertNull(result.rows.get(0).pictures.get(0).reason);}
    }
    @Test void imageOnlyOptionAndEmptyStemNotSkipped() throws Exception {
        try(Workbook b=book(false)){row(b,1,"");b.getSheetAt(0).getRow(1).getCell(2).setCellValue("");pic(b,1,1,2,2);pic(b,1,2,2,3);Result r=reader.read(bytes(b));assertEquals(1,r.rows.size());assertEquals(2,r.rows.get(0).pictures.size());}
    }
    @Test void crossingAnotherQuestionNeedsReview() throws Exception {
        try(Workbook b=book(false)){row(b,1,"第一题");row(b,2,"第二题");pic(b,1,1,3,2);Result r=reader.read(bytes(b));assertNotNull(r.rows.get(0).pictures.get(0).reason);}
    }
    @Test void mergedAreaNeedsReview() throws Exception {
        try(Workbook b=book(false)){row(b,1,"合并格");row(b,2,"另题");b.getSheetAt(0).addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1,2,1,1));pic(b,1,1,2,2);assertNotNull(reader.read(bytes(b)).rows.get(0).pictures.get(0).reason);}
    }
    @Test void formulaIsVisibleIssueNotRemoteRequest() throws Exception {
        try(Workbook b=book(false)){row(b,1,"题干");b.getSheetAt(0).getRow(1).getCell(2).setCellFormula("1+1");Result r=reader.read(bytes(b));assertFalse(r.rows.get(0).issues.isEmpty());assertEquals("",r.rows.get(0).question.getOptionA());}
    }
    @Test void legacyXlsPicturesSupported() throws Exception {
        try(Workbook b=book(true)){row(b,1,"旧模板图片");pic(b,1,1,2,2);Result r=reader.read(bytes(b));assertEquals(1,r.rows.get(0).pictures.size());assertTrue(r.globalIssues.isEmpty());}
    }
    @Test void dedicatedImageColumnsMapToSlots() throws Exception {
        String[] headers={"题目类型","题目内容","选项A","选项B","选项C","选项D","标准答案","题干图片1","题干图片2","选项A图片"};
        try(Workbook b=book(false,headers)) {
            Row row=b.getSheetAt(0).createRow(1);row.setHeightInPoints(160);
            String[] values={"选择题","观察图片","甲","乙","丙","丁","A"};
            for(int i=0;i<values.length;i++)row.createCell(i).setCellValue(values[i]);
            pic(b,1,7,2,8);pic(b,1,8,2,9);pic(b,1,9,2,10);
            Result result=reader.read(bytes(b));
            assertEquals(1,result.rows.size());
            assertEquals("观察图片",result.rows.get(0).question.getQuestionContent());
            assertEquals(3,result.rows.get(0).pictures.size());
            assertEquals(2,result.rows.get(0).pictures.stream().filter(p->"stem".equals(p.slot)).count());
            assertEquals(1,result.rows.get(0).pictures.stream().filter(p->"A".equals(p.slot)).count());
            assertTrue(result.rows.get(0).pictures.stream().allMatch(p->p.reason==null));
        }
    }
    @Test void imageOnlyQuestionInDedicatedColumnIsKept() throws Exception {
        String[] headers={"题目类型","题目内容","选项A","选项B","标准答案","题干图片1"};
        try(Workbook b=book(false,headers)) {
            Row row=b.getSheetAt(0).createRow(1);row.setHeightInPoints(160);
            row.createCell(0).setCellValue("选择题");
            pic(b,1,5,2,6);
            Result result=reader.read(bytes(b));
            assertEquals(1,result.rows.size());
            assertEquals(1,result.rows.get(0).pictures.size());
            assertEquals("stem",result.rows.get(0).pictures.get(0).slot);
        }
    }
    @Test void dedicatedImageColumnCrossingQuestionRowNeedsReview() throws Exception {
        String[] headers={"题目类型","题目内容","选项A","选项B","标准答案","题干图片1"};
        try(Workbook b=book(false,headers)) {
            Row first=b.getSheetAt(0).createRow(1);first.setHeightInPoints(160);
            first.createCell(0).setCellValue("选择题");
            first.createCell(1).setCellValue("第一题");
            Row second=b.getSheetAt(0).createRow(2);second.setHeightInPoints(160);
            second.createCell(0).setCellValue("选择题");
            second.createCell(1).setCellValue("第二题");
            pic(b,1,5,3,6);
            Result result=reader.read(bytes(b));
            assertEquals(1,result.rows.get(0).pictures.size());
            assertNotNull(result.rows.get(0).pictures.get(0).reason);
        }
    }
    @Test void wpsCellImageFormulaIsExtracted() throws Exception {
        String[] headers={"题目类型","题目内容","选项A","选项B","选项C","选项D","标准答案","题干图片1"};
        try(Workbook b=book(false,headers)) {
            Row row=b.getSheetAt(0).createRow(1);row.setHeightInPoints(160);
            String[] values={"选择题","观察图片","甲","乙","丙","丁","A"};
            for(int i=0;i<values.length;i++)row.createCell(i).setCellValue(values[i]);
            row.createCell(7).setCellFormula("DISPIMG(\"ID_TEST\",1)");
            Result result=reader.read(withZipEntries(bytes(b),wpsEntries()));
            assertEquals(1,result.rows.size());
            assertEquals("观察图片",result.rows.get(0).question.getQuestionContent());
            assertEquals(1,result.rows.get(0).pictures.size());
            assertTrue(result.rows.get(0).pictures.get(0).embedded);
            assertEquals("stem",result.rows.get(0).pictures.get(0).slot);
            assertTrue(result.rows.get(0).issues.stream().noneMatch(i->i.message.contains("未能读取")));
        }
    }
    @Test void unsupportedImageColumnFormulaNeedsReview() throws Exception {
        String[] headers={"题目类型","题目内容","选项A","选项B","标准答案","题干图片1"};
        try(Workbook b=book(false,headers)) {
            Row row=b.getSheetAt(0).createRow(1);row.setHeightInPoints(160);
            row.createCell(0).setCellValue("选择题");
            row.createCell(1).setCellValue("观察图片");
            row.createCell(5).setCellFormula("1+1");
            Result result=reader.read(bytes(b));
            assertTrue(result.rows.get(0).issues.stream().anyMatch(i->i.message.contains("未能读取")));
        }
    }
    @Test void richDataWorkbookGetsClearFallback() throws Exception {
        try(Workbook b=book(false)) {
            row(b,1,"新式单元格图");
            Map<String,byte[]> extra=new LinkedHashMap<>();
            extra.put("xl/richData/rdrichvalue.xml","<root/>".getBytes(StandardCharsets.UTF_8));
            Result result=reader.read(withZipEntries(bytes(b),extra));
            assertTrue(result.globalIssues.stream().anyMatch(message->message.contains("新式单元格图片")));
        }
    }
    @Test void legacyGuardRejectsImageFormulaWithoutDrawing() throws Exception {
        try(Workbook b=book(false)) {
            row(b,1,"图片公式");
            b.getSheetAt(0).getRow(1).getCell(1).setCellFormula("DISPIMG(\"ID_MISSING\",1)");
            assertTrue(reader.hasArtwork(bytes(b)));
        }
    }
    @Test void fiftyQuestionsSeventySixImagesAndFixture() throws Exception {
        try(Workbook b=book(false)) {
            for(int r=1;r<=50;r++){row(b,r,"图片功能验收题"+r+"：观察图片，选择正确答案。");pic(b,r,1,r+1,2);if(r<=26)pic(b,r,2,r+1,3);}
            byte[] data=bytes(b);Result result=reader.read(data);assertEquals(50,result.rows.size());assertEquals(76,result.imageCount);assertEquals(76,result.rows.stream().mapToInt(r->r.pictures.size()).sum());assertEquals(51,result.rows.get(49).sourceRow);
            Path dir=Paths.get(System.getProperty("basedir"),"..","..","output","choice-media","fixtures").toAbsolutePath().normalize();Files.createDirectories(dir);Files.write(dir.resolve("50-text-images.xlsx"),data);
        }
    }
}
