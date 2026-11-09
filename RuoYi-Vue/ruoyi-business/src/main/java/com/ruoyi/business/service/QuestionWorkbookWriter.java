package com.ruoyi.business.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.QuestionMedia;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.file.FileUtils;

/** 模板和导出统一使用图片专用列，文字与图片不再争抢同一个单元格。 */
@Component
public class QuestionWorkbookWriter {
    private static final int STEM_IMAGE_START=13;
    private static final int OPTION_IMAGE_START=16;
    private static final String[] HEADERS={
        "题目类型","题目内容","年级","学期","第几课","选项A","选项B","选项C","选项D",
        "标准答案","题目解析","是否公开","创建人",
        "题干图片1","题干图片2","题干图片3","选项A图片","选项B图片","选项C图片","选项D图片"
    };
    @Autowired private QuestionImageService images;

    /** 下载模板：文字列保持原顺序，右侧新增图片专用列和填写说明。 */
    public void template(HttpServletResponse response) throws IOException {
        try(XSSFWorkbook book=new XSSFWorkbook()) {
            Sheet sheet=book.createSheet("题库");
            CellStyle head=headerStyle(book);
            CellStyle body=bodyStyle(book);
            Row header=sheet.createRow(0);header.setHeightInPoints(28);
            for(int c=0;c<HEADERS.length;c++) {
                header.createCell(c).setCellValue(HEADERS[c]);
                header.getCell(c).setCellStyle(head);
                sheet.setColumnWidth(c,columnWidth(c)*256);
            }
            String[][] examples={
                {"选择题","【选择题】中国的首都是哪里？","6","0","1","北京","上海","广州","深圳","A","","Y",""},
                {"判断题","【判断题】计算机病毒可以通过网络传播。","5","0","1","","","","","T","","Y",""},
                {"打字题","【打字题】春眠不觉晓，处处闻啼鸟。夜来风雨声，花落知多少。","4","0","1","","","","","","","Y",""}
            };
            for(int i=0;i<examples.length;i++) {
                Row row=sheet.createRow(i+1);row.setHeightInPoints(120);
                for(int c=0;c<examples[i].length;c++) {
                    Cell cell=row.createCell(c,CellType.STRING);
                    cell.setCellValue(examples[i][c]==null?"":examples[i][c]);
                    cell.setCellStyle(body);
                }
            }
            sheet.createFreezePane(2,1);

            Sheet guide=book.createSheet("填写说明");
            guide.setColumnWidth(0,110*256);
            String[] notes={
                "Excel 图文题库导入说明",
                "1. 文字写在“题目内容、选项A-D”原列；图片放在右侧“题干图片1-3、选项A-D图片”列。",
                "2. 图片推荐使用浮动图片（浮在单元格上方）。系统按图片左上角所在列判断它属于题干还是选项；图片不要跨到下一题行。",
                "3. 一道选择题最多3张题干图，每个选项1张图；只有图片、没有文字的题目也可以导入。",
                "4. 图片列只放图片，不要填写文字；不要合并数据行；图片尽量放在本行内，避免跨到下一题。",
                "5. 图片跨行或位置无法确定时，会进入“待确认图片”，不会自动猜；WPS“嵌入单元格”读不到时可在预览中粘贴截图。",
                "6. 旧模板仍可导入；新模板把文字和图片分开，更不容易放错位置。"
            };
            CellStyle guideTitle=bodyStyle(book);guideTitle.setFont(titleFont(book));
            for(int i=0;i<notes.length;i++) {
                Row row=guide.createRow(i);
                row.setHeightInPoints(i==0?28:42);
                Cell cell=row.createCell(0);
                cell.setCellValue(notes[i]);
                cell.setCellStyle(i==0?guideTitle:body);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            FileUtils.setAttachmentResponseHeader(response,"题库导入模板.xlsx");
            book.write(response.getOutputStream());
        }
    }

    public void export(HttpServletResponse response,List<BizQuestion> questions) throws IOException {
        if(questions.size()>200)throw new ServiceException("图文导出每次最多200题，请筛选年级、学期或课次后重试");
        try(XSSFWorkbook book=new XSSFWorkbook()) {
            Sheet sheet=book.createSheet("题库");
            CellStyle style=bodyStyle(book);
            CellStyle head=headerStyle(book);
            Row header=sheet.createRow(0);header.setHeightInPoints(24);
            for(int c=0;c<HEADERS.length;c++) {
                header.createCell(c).setCellValue(HEADERS[c]);
                header.getCell(c).setCellStyle(head);
                sheet.setColumnWidth(c,columnWidth(c)*256);
            }
            Drawing<?> drawing=sheet.createDrawingPatriarch();
            Map<Long,Integer> cache=new HashMap<>();
            Map<Long,QuestionImageService.ImageInfo> dimensions=new HashMap<>();
            long total=0;
            for(int i=0;i<questions.size();i++) {
                BizQuestion q=questions.get(i);Row row=sheet.createRow(i+1);
                String[] values={q.getQuestionType(),q.getQuestionContent(),q.getGrade()==null?"":q.getGrade().toString(),q.getSemester(),q.getLessonNum()==null?"":q.getLessonNum().toString(),q.getOptionA(),q.getOptionB(),q.getOptionC(),q.getOptionD(),q.getAnswer(),q.getAnalysis(),q.getIsPublic(),q.getNickName()};
                for(int c=0;c<values.length;c++){Cell cell=row.createCell(c,CellType.STRING);cell.setCellValue(values[c]==null?"":values[c]);cell.setCellStyle(style);}
                QuestionMedia media=q.getMedia();
                List<ImagePlacement> placements=new ArrayList<>();
                for(int n=0;n<media.stem.size()&&n<3;n++)placements.add(new ImagePlacement(media.stem.get(n),STEM_IMAGE_START+n));
                for(int n=0;n<4;n++) {
                    List<QuestionMedia.ImageRef> refs=media.options.get(String.valueOf((char)('A'+n)));
                    if(refs!=null&&!refs.isEmpty())placements.add(new ImagePlacement(refs.get(0),OPTION_IMAGE_START+n));
                }
                row.setHeightInPoints(placements.isEmpty()?42:220);
                for(ImagePlacement placement:placements) {
                    long id=Long.parseLong(placement.ref.imageId);Integer index=cache.get(id);
                    if(index==null) {
                        byte[] bytes=images.bytesForExport(id,q.getQuestionId());
                        total+=bytes.length;
                        if(total>100L*1024*1024)throw new ServiceException("图片总量超过100MB，请减少每次导出题目数量");
                        QuestionImageService.ImageInfo info=QuestionImageService.inspect(bytes);
                        dimensions.put(id,info);
                        index=book.addPicture(bytes,"png".equals(info.extension)?Workbook.PICTURE_TYPE_PNG:Workbook.PICTURE_TYPE_JPEG);
                        cache.put(id,index);
                    }
                    ClientAnchor anchor=book.getCreationHelper().createClientAnchor();
                    anchor.setCol1(placement.col);anchor.setCol2(placement.col);
                    anchor.setRow1(i+1);anchor.setRow2(i+1);
                    QuestionImageService.ImageInfo size=dimensions.get(id);
                    double width=sheet.getColumnWidthInPixels(placement.col),slotWidth=Math.max(1,width-12);
                    double scale=Math.min((slotWidth-5)/size.width,223.0/size.height);
                    double x=6;
                    anchor.setDx1((int)(x*9525));anchor.setDx2((int)((x+size.width*scale)*9525));
                    anchor.setDy1(55*9525);anchor.setDy2((int)((55+size.height*scale)*9525));
                    anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                    drawing.createPicture(anchor,index);
                }
            }
            sheet.createFreezePane(2,1);
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0,questions.size(),0,HEADERS.length-1));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            FileUtils.setAttachmentResponseHeader(response,"图文选择题库.xlsx");
            book.write(response.getOutputStream());
        }
    }

    private CellStyle headerStyle(XSSFWorkbook book) {
        CellStyle style=book.createCellStyle();
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }
    private CellStyle bodyStyle(XSSFWorkbook book) {
        CellStyle style=book.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }
    private Font titleFont(XSSFWorkbook book) {
        Font font=book.createFont();font.setBold(true);font.setFontHeightInPoints((short)14);return font;
    }
    private int columnWidth(int column) {
        if(column==1)return 64;
        if(column>=5&&column<=8)return 32;
        if(column>=STEM_IMAGE_START)return 30;
        return 18;
    }
    private static class ImagePlacement {
        final QuestionMedia.ImageRef ref;final int col;
        ImagePlacement(QuestionMedia.ImageRef ref,int col){this.ref=ref;this.col=col;}
    }
}
