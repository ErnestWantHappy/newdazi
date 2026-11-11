package com.ruoyi.business.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.QuestionMedia;
import com.ruoyi.business.utils.TypingContentNormalizer;
import com.ruoyi.common.exception.ServiceException;

/** 题库专用双通道读取：文字不被图片路径覆盖，歧义不自动猜。 */
@Component
public class QuestionWorkbookReader {
    public static final long MAX_FILE = 50L*1024*1024;
    private static final Map<String,String> HEADERS=new HashMap<>();
    private static final Map<String,String> IMAGE_HEADERS=new HashMap<>();
    private static final Pattern DISPIMG=Pattern.compile("(?i)DISPIMG\\s*\\(\\s*[\"']([^\"']+)[\"']");
    private static final String RICH_DATA_MESSAGE="检测到 Excel 新式单元格图片，暂不能自动读取；请改用浮动图片，或在预览中粘贴截图";
    static {
        for (String[] pair:new String[][]{{"题目类型","type"},{"题型","type"},{"题目内容","stem"},{"题干","stem"},
            {"选项A","A"},{"选项B","B"},{"选项C","C"},{"选项D","D"},{"A","A"},{"B","B"},{"C","C"},{"D","D"},
            {"标准答案","answer"},{"答案","answer"},{"题目解析","analysis"},{"解析","analysis"},{"年级","grade"},
            {"学期","semester"},{"第几课","lesson"},{"是否公开","public"}}) HEADERS.put(pair[0],pair[1]);
        for (String[] pair:new String[][]{
            {"题干图片","stem"},{"题干图片1","stem"},{"题干图片2","stem"},{"题干图片3","stem"},
            {"题干图1","stem"},{"题干图2","stem"},{"题干图3","stem"},
            {"题目图片","stem"},{"题目图片1","stem"},{"题目图片2","stem"},{"题目图片3","stem"},
            {"选项A图片","A"},{"选项B图片","B"},{"选项C图片","C"},{"选项D图片","D"},
            {"A图片","A"},{"B图片","B"},{"C图片","C"},{"D图片","D"}}) IMAGE_HEADERS.put(pair[0],pair[1]);
    }
    public boolean hasArtwork(byte[] data) {
        if(data==null || data.length==0 || data.length>MAX_FILE)throw new ServiceException("请选择不超过50MB的Excel文件");
        Result probe=new Result();checkZip(data,probe);
        if(probe.embeddedArtwork || !probe.globalIssues.isEmpty())return true;
        try(Workbook book=WorkbookFactory.create(new ByteArrayInputStream(data))){
            if(!book.getAllPictures().isEmpty())return true;
            // 旧导入接口不能把 DISPIMG/IMAGE 公式当普通文字导入，否则老师会以为图片已经保留。
            for(int sheetNo=0;sheetNo<book.getNumberOfSheets();sheetNo++) {
                Sheet sheet=book.getSheetAt(sheetNo);
                for(Row row:sheet) for(Cell cell:row) {
                    if(cell.getCellType()!=CellType.FORMULA)continue;
                    try {
                        String formula=cell.getCellFormula();
                        if(formula!=null && (DISPIMG.matcher(formula).find()
                            || formula.toUpperCase(Locale.ROOT).contains("IMAGE("))) return true;
                    }catch(Exception ignored){ }
                }
            }
            return false;
        }
        catch(Exception ex){throw new ServiceException("Excel无法读取，请取消加密或另存为xlsx后重试");}
    }
    public Result read(byte[] data) {
        if (data==null || data.length==0 || data.length>MAX_FILE) throw new ServiceException("请选择不超过50MB的Excel文件");
        Result result=new Result();
        checkZip(data,result);
        Map<String,byte[]> embeddedImages=readWpsCellImages(data,result);
        try (Workbook book=WorkbookFactory.create(new ByteArrayInputStream(data))) {
            for(int sheetNo=0;sheetNo<book.getNumberOfSheets();sheetNo++) {
                if(book.isSheetHidden(sheetNo)||book.isSheetVeryHidden(sheetNo)) continue;
                Sheet sheet=book.getSheetAt(sheetNo);
                Map<Integer,String> columns=null; int header=-1;
                for(int row=0;row<=Math.min(sheet.getLastRowNum(),20);row++) {
                    Map<Integer,String> c=headers(sheet.getRow(row));
                    if(c.containsValue("stem")&&c.containsValue("answer")){columns=c;header=row;break;}
                }
                if(columns==null) continue;
                Map<Integer,String> imageColumns=imageHeaders(sheet.getRow(header));
                Map<Integer,RowDraft> byRow=new LinkedHashMap<>();
                Map<Integer,Set<Integer>> unresolvedImageCells=new LinkedHashMap<>();
                for(int r=header+1;r<=sheet.getLastRowNum();r++) {
                    if(r>10000) throw new ServiceException("表格行数过多，请删除空白格式行后重试");
                    Row row=sheet.getRow(r); if(row==null) continue;
                    RowDraft draft=new RowDraft();draft.sourceKey=sheetNo+":"+r;draft.sheetName=sheet.getSheetName();draft.sourceRow=r+1;
                    Map<String,String> values=new HashMap<>();
                    for(Map.Entry<Integer,String> c:columns.entrySet()) {
                        Cell cell=row.getCell(c.getKey());String value=text(cell);
                        if(cell!=null && (cell.getCellType()==CellType.FORMULA || cell.getCellType()==CellType.ERROR)) {
                            draft.issues.add(new Issue(c.getValue(),"单元格公式或内嵌图片格式需核对，可在此题在线修正/粘贴截图"));value="";
                        }
                        values.put(c.getValue(),value);
                    }
                    boolean hasImageCell=false;
                    for(Map.Entry<Integer,String> c:imageColumns.entrySet()) {
                        Cell cell=row.getCell(c.getKey());
                        if(cell==null) continue;
                        hasImageCell=true;
                        if(cell.getCellType()==CellType.FORMULA || cell.getCellType()==CellType.ERROR) {
                            unresolvedImageCells.computeIfAbsent(r,key->new LinkedHashSet<>()).add(c.getKey());
                        } else if(!text(cell).isEmpty()) {
                            draft.issues.add(new Issue(c.getValue(),"图片列只放图片，请不要填写文字"));
                        }
                    }
                    draft.question=question(values,draft);
                    if(values.values().stream().anyMatch(v->!v.trim().isEmpty())||hasImageCell||!draft.issues.isEmpty()) byRow.put(r,draft);
                }
                Set<String> extractedCells=new HashSet<>();
                List<Picture> pictures=pictures(sheet,result,embeddedImages);
                for(Picture picture:pictures) {
                    if(++result.imageCount>1000) throw new ServiceException("单批最多1000个图片实例");
                    int row=picture.row, col=picture.col;
                    String imageSlot=imageColumns.get(col);
                    String slot=imageSlot!=null?imageSlot:columns.get(col);
                    if(row<=header || row>sheet.getLastRowNum() || !("stem".equals(slot)||Arrays.asList("A","B","C","D").contains(slot))) {
                        result.globalIssues.add("工作表“"+sheet.getSheetName()+"”有图片未落在题干、选项或对应图片列，请在预览中核对是否为装饰图或补到对应题目");
                        continue;
                    }
                    RowDraft draft=byRow.get(row);
                    if(draft==null) {
                        draft=new RowDraft();draft.sourceKey=sheetNo+":"+row;draft.sheetName=sheet.getSheetName();draft.sourceRow=row+1;
                        draft.question=new BizQuestion();draft.question.setQuestionType("choice");byRow.put(row,draft);
                    }
                    if(picture.embedded) extractedCells.add(row+":"+col);
                    picture.slot=slot;
                    if(picture.transformed) {
                        draft.issues.add(new Issue(slot,"该图片有裁剪/旋转/翻转，请粘贴其可见截图替换，避免显示被裁去的内容"));
                        continue;
                    }
                    if(imageSlot!=null) {
                        // 图片列按起始列直接归属，只检查是否跨到别的题目行，不再按覆盖面积猜列。
                        if(crossesQuestionRow(sheet,picture)) picture.reason="图片跨越了题目行，请确认归属位置或调整行高";
                    } else {
                        if(picture.embedded) draft.issues.add(new Issue(slot,"原单元格文字已被嵌入图替换，请核对文字内容"));
                        if(picture.ambiguous || crossesSemanticCell(sheet,columns,picture)) picture.reason="图片跨越不同题目或选项，请确认所属位置";
                    }
                    draft.pictures.add(picture);
                }
                for(Map.Entry<Integer,Set<Integer>> entry:unresolvedImageCells.entrySet()) {
                    RowDraft draft=byRow.get(entry.getKey());
                    if(draft==null) continue;
                    for(Integer col:entry.getValue()) {
                        if(!extractedCells.contains(entry.getKey()+":"+col)) {
                            draft.issues.add(new Issue(imageColumns.get(col),"该单元格图片未能读取，请改用浮动图片，或在此题粘贴截图"));
                        }
                    }
                }
                List<Integer> sorted=new ArrayList<>(byRow.keySet());java.util.Collections.sort(sorted);
                for(Integer r:sorted){result.rows.add(byRow.get(r));if(result.rows.size()>200)throw new ServiceException("单批最多导入200道题目");}
            }
            if(result.rows.isEmpty()) throw new ServiceException("没有找到可导入题目，请使用题库模板并保留题干、答案表头");
            return result;
        } catch(ServiceException ex){throw ex;}
        catch(Exception ex){throw new ServiceException("Excel无法读取，请取消加密或另存为xlsx后重试");}
    }
    private Map<Integer,String> headers(Row row) {
        Map<Integer,String> result=new LinkedHashMap<>();if(row==null)return result;
        for(Cell cell:row){String field=HEADERS.get(text(cell).trim());if(field!=null){if(result.containsValue(field))throw new ServiceException("同一字段存在重复表头，请保留一列");result.put(cell.getColumnIndex(),field);}}
        return result;
    }
    private Map<Integer,String> imageHeaders(Row row) {
        Map<Integer,String> result=new LinkedHashMap<>();if(row==null)return result;
        Map<String,Integer> seen=new HashMap<>();
        for(Cell cell:row){
            String field=IMAGE_HEADERS.get(text(cell).trim());
            if(field==null)continue;
            // 题干图允许分成多列，选项图每项只能有一列，否则预览无法判断归属。
            if(!"stem".equals(field)&&seen.containsKey(field))throw new ServiceException("同一选项图片存在重复表头，请保留一列");
            seen.put(field,cell.getColumnIndex());
            result.put(cell.getColumnIndex(),field);
        }
        return result;
    }
    private String text(Cell cell){return cell==null?"":new DataFormatter(Locale.CHINA).formatCellValue(cell).trim();}
    private BizQuestion question(Map<String,String> v,RowDraft draft) {
        BizQuestion q=new BizQuestion();String type=v.getOrDefault("type","choice");
        if(type.isEmpty()||Arrays.asList("choice","选择题","单选","单选题").contains(type)) type="choice";
        else if(Arrays.asList("judgment","判断题","判断").contains(type))type="judgment";
        else if(Arrays.asList("typing","打字题").contains(type))type="typing";
        q.setQuestionType(type);
        // Excel 课文同样可能带入弯单引号；打字题入库前与手工编辑保持同一归一化规则。
        q.setQuestionContent("typing".equals(type)?TypingContentNormalizer.normalize(v.get("stem")):v.get("stem"));q.setOptionA(v.get("A"));q.setOptionB(v.get("B"));q.setOptionC(v.get("C"));q.setOptionD(v.get("D"));
        q.setAnswer(v.getOrDefault("answer","").toUpperCase(Locale.ROOT));q.setAnalysis(v.get("analysis"));
        q.setIsPublic(Arrays.asList("N","0","否").contains(v.get("public"))?"N":"Y");
        String grade=v.getOrDefault("grade","");String[] cn={"一年级","二年级","三年级","四年级","五年级","六年级","七年级","八年级","九年级"};
        for(int i=0;i<cn.length;i++)if(cn[i].equals(grade))grade=String.valueOf(i+1);
        if(!grade.isEmpty())try{q.setGrade(Long.valueOf(grade));}catch(NumberFormatException ex){draft.issues.add(new Issue("grade","年级需核对"));}
        String semester=v.get("semester");q.setSemester("上册".equals(semester)?"0":"下册".equals(semester)?"1":semester);
        String lesson=v.getOrDefault("lesson","");if(!lesson.isEmpty())try{q.setLessonNum(Integer.valueOf(lesson));}catch(NumberFormatException ex){draft.issues.add(new Issue("lesson","第几课需核对"));}
        return q;
    }
    private List<Picture> pictures(Sheet sheet,Result result,Map<String,byte[]> embeddedImages) {
        List<Picture> list=new ArrayList<>();
        if(sheet instanceof XSSFSheet) {
            for(POIXMLDocumentPart part:((XSSFSheet)sheet).getRelations())if(part instanceof XSSFDrawing) {
                for(XSSFShape shape:((XSSFDrawing)part).getShapes()) {
                    if(shape instanceof XSSFPicture) {
                        XSSFPicture pic=(XSSFPicture)shape;Picture p=new Picture();p.bytes=pic.getPictureData().getData();
                        try {
                            XSSFClientAnchor a=pic.getClientAnchor();p.row=a.getRow1();p.col=a.getCol1();p.row2=a.getRow2();p.col2=a.getCol2();
                            p.dx1=a.getDx1()/9525.0;p.dy1=a.getDy1()/9525.0;p.dx2=a.getDx2()/9525.0;p.dy2=a.getDy2()/9525.0;
                            p.transformed=pic.getCTPicture().getBlipFill().isSetSrcRect();
                            org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D x=pic.getCTPicture().getSpPr().getXfrm();
                            if(x!=null&&(x.getRot()!=0||x.getFlipH()||x.getFlipV()))p.transformed=true;
                        }catch(Exception ex){result.globalIssues.add("存在无法定位的绘图图片，请核对题面并在线补图");continue;}
                        list.add(p);
                    } else result.globalIssues.add("存在组合图形或非位图对象，请核对是否需要截图补入题目");
                }
            }
        } else if(sheet instanceof HSSFSheet && ((HSSFSheet)sheet).getDrawingPatriarch()!=null) {
            for(HSSFShape shape:((HSSFSheet)sheet).getDrawingPatriarch().getChildren()) {
                if(shape instanceof HSSFPicture && shape.getAnchor() instanceof HSSFClientAnchor) {
                    HSSFPicture pic=(HSSFPicture)shape;HSSFClientAnchor a=(HSSFClientAnchor)pic.getAnchor();Picture p=new Picture();p.bytes=pic.getPictureData().getData();
                    p.row=a.getRow1();p.row2=a.getRow2();p.col=a.getCol1();p.col2=a.getCol2();
                    p.dx1=a.getDx1()/1024.0*sheet.getColumnWidthInPixels(p.col);p.dx2=a.getDx2()/1024.0*sheet.getColumnWidthInPixels(p.col2);
                    p.dy1=a.getDy1()/256.0*rowHeight(sheet,p.row);p.dy2=a.getDy2()/256.0*rowHeight(sheet,p.row2);
                    list.add(p);
                } else result.globalIssues.add("存在组合图形，请核对并截图补充");
            }
        }
        if(!embeddedImages.isEmpty()) {
            // WPS 的 DISPIMG 单元格图不在 drawing 层，必须按公式里的图片名回填到对应单元格。
            for(Row row:sheet) for(Cell cell:row) {
                if(cell.getCellType()!=CellType.FORMULA)continue;
                String formula;
                try { formula=cell.getCellFormula(); }
                catch(Exception ex){ continue; }
                Matcher matcher=DISPIMG.matcher(formula);
                if(!matcher.find())continue;
                byte[] bytes=embeddedImages.get(matcher.group(1));
                if(bytes==null) {
                    result.globalIssues.add("工作表“"+sheet.getSheetName()+"”第"+(cell.getRowIndex()+1)+"行有 WPS 单元格图缺少原图，请在预览中粘贴截图补充");
                    continue;
                }
                Picture picture=new Picture();picture.bytes=bytes;picture.embedded=true;
                picture.row=cell.getRowIndex();picture.col=cell.getColumnIndex();picture.row2=picture.row;picture.col2=picture.col;
                list.add(picture);
            }
        }
        list.sort((a,b)->{int y=Integer.compare(a.row,b.row);if(y!=0)return y;y=Double.compare(a.dy1,b.dy1);return y!=0?y:Double.compare(a.dx1,b.dx1);});
        return list;
    }
    private boolean crossesSemanticCell(Sheet sheet,Map<Integer,String> columns,Picture p) {
        if(p.row<0||p.col<0||p.row2>10000||p.col2>1000)return true;
        for(org.apache.poi.ss.util.CellRangeAddress range:sheet.getMergedRegions())
            if(range.isInRange(p.row,p.col)&&(range.getFirstRow()!=range.getLastRow()||range.getFirstColumn()!=range.getLastColumn()))return true;
        double w=-p.dx1+p.dx2,h=-p.dy1+p.dy2;
        for(int c=p.col;c<p.col2;c++)w+=sheet.getColumnWidthInPixels(c);
        for(int r=p.row;r<p.row2;r++)h+=rowHeight(sheet,r);
        if(w<=0||h<=0)return p.row2!=p.row||p.col2!=p.col;
        double firstW=Math.min(w,Math.max(0,sheet.getColumnWidthInPixels(p.col)-p.dx1));
        double firstH=Math.min(h,Math.max(0,rowHeight(sheet,p.row)-p.dy1));
        return firstW*firstH/(w*h)<0.85;
    }
    private boolean crossesQuestionRow(Sheet sheet,Picture p) {
        if(p.row<0||p.row2<0||p.row2>10000)return true;
        for(org.apache.poi.ss.util.CellRangeAddress range:sheet.getMergedRegions())
            if(range.isInRange(p.row,p.col)&&range.getFirstRow()!=range.getLastRow())return true;
        double h=-p.dy1+p.dy2;
        for(int r=p.row;r<p.row2;r++)h+=rowHeight(sheet,r);
        // row2等于row时也可能因dy2超出本行，不能只看end row判断。
        if(h<=0)return p.row2>p.row;
        double firstH=Math.min(h,Math.max(0,rowHeight(sheet,p.row)-p.dy1));
        return firstH/h<0.85;
    }
    private double rowHeight(Sheet sheet,int r){Row row=sheet.getRow(r);return (row==null?sheet.getDefaultRowHeightInPoints():row.getHeightInPoints())*96/72.0;}
    private void checkZip(byte[] bytes,Result result) {
        if(bytes[0]!='P'||bytes.length<4||bytes[1]!='K')return;
        long total=0;int entries=0;
        try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;byte[] buffer=new byte[8192];
            while((entry=zip.getNextEntry())!=null) {
                if(++entries>4000||entry.getName().contains("..")||entry.getName().startsWith("/"))throw new ServiceException("Excel压缩包结构异常");
                if(entry.getName().contains("cellimages"))result.embeddedArtwork=true;
                if(entry.getName().startsWith("xl/richData/")) {
                    result.embeddedArtwork=true;
                    if(!result.globalIssues.contains(RICH_DATA_MESSAGE))result.globalIssues.add(RICH_DATA_MESSAGE);
                }
                int n;while((n=zip.read(buffer))!=-1){total+=n;if(total>128L*1024*1024)throw new ServiceException("Excel解压后内容超过128MB，请分批导入");}
            }
        }catch(IOException ex){throw new ServiceException("Excel压缩包损坏，请重新保存");}
    }
    private Map<String,byte[]> readWpsCellImages(byte[] data,Result result) {
        if(data.length<2||data[0]!='P'||data[1]!='K')return new LinkedHashMap<>();
        byte[] cellImages=null, relationshipXml=null;
        try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while((entry=zip.getNextEntry())!=null) {
                if("xl/cellimages.xml".equals(entry.getName()))cellImages=readAll(zip,4L*1024*1024);
                else if("xl/_rels/cellimages.xml.rels".equals(entry.getName()))relationshipXml=readAll(zip,1024*1024);
            }
        }catch(IOException ex){result.globalIssues.add("WPS 单元格图片关系读取失败，请在预览中核对或粘贴截图");return new LinkedHashMap<>();}
        if(cellImages==null)return new LinkedHashMap<>();
        if(relationshipXml==null){result.globalIssues.add("检测到 WPS 单元格图片，但缺少图片关系文件，请在预览中粘贴截图补充");return new LinkedHashMap<>();}
        try {
            Map<String,String> targets=relationships(relationshipXml);
            Map<String,String> refs=cellImageRefs(cellImages);
            if(refs.isEmpty())return new LinkedHashMap<>();
            Set<String> mediaTargets=new HashSet<>();
            for(String rid:refs.values()){String target=targets.get(rid);if(target!=null)mediaTargets.add(target);}
            Map<String,byte[]> media=readZipEntries(data,mediaTargets,result);
            Map<String,byte[]> images=new LinkedHashMap<>();
            for(Map.Entry<String,String> ref:refs.entrySet()) {
                String target=targets.get(ref.getValue());
                byte[] bytes=target==null?null:media.get(target);
                if(bytes!=null)images.put(ref.getKey(),bytes);
            }
            if(images.size()<refs.size())result.globalIssues.add("部分 WPS 单元格图片未找到原图，请在预览中粘贴截图补充");
            return images;
        }catch(Exception ex) {
            result.globalIssues.add("WPS 单元格图片解析失败，请在预览中粘贴截图补充");
            return new LinkedHashMap<>();
        }
    }
    private Map<String,String> relationships(byte[] bytes) throws Exception {
        Document document=parseXml(bytes);
        Map<String,String> result=new HashMap<>();
        NodeList list=document.getElementsByTagNameNS("*","Relationship");
        for(int i=0;i<list.getLength();i++) {
            Element element=(Element)list.item(i);
            String id=element.getAttribute("Id"), target=element.getAttribute("Target");
            if(!id.isEmpty()&&!target.isEmpty())result.put(id,zipPath("xl",target));
        }
        return result;
    }
    private Map<String,String> cellImageRefs(byte[] bytes) throws Exception {
        Document document=parseXml(bytes);
        Map<String,String> result=new LinkedHashMap<>();
        NodeList pictures=document.getElementsByTagNameNS("*","pic");
        for(int i=0;i<pictures.getLength();i++) {
            Element picture=(Element)pictures.item(i);
            NodeList names=picture.getElementsByTagNameNS("*","cNvPr");
            NodeList blips=picture.getElementsByTagNameNS("*","blip");
            if(names.getLength()==0||blips.getLength()==0)continue;
            String name=((Element)names.item(0)).getAttribute("name");
            Element blip=(Element)blips.item(0);
            String embed=blip.getAttributeNS("http://schemas.openxmlformats.org/officeDocument/2006/relationships","embed");
            if(embed==null||embed.isEmpty())embed=blip.getAttribute("r:embed");
            if(!name.isEmpty()&&!embed.isEmpty())result.put(name,embed);
        }
        return result;
    }
    private Document parseXml(byte[] bytes) throws Exception {
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities",false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities",false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
    }
    private Map<String,byte[]> readZipEntries(byte[] data,Set<String> targets,Result result) throws IOException {
        Map<String,byte[]> resultMap=new LinkedHashMap<>();
        if(targets.isEmpty())return resultMap;
        try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while((entry=zip.getNextEntry())!=null) {
                if(!targets.contains(entry.getName()))continue;
                try { resultMap.put(entry.getName(),readAll(zip,QuestionImageService.MAX_BYTES+1)); }
                catch(IOException ex) { result.globalIssues.add("WPS 单元格图片超过单张5MB，请在预览中改用压缩截图"); }
            }
        }
        return resultMap;
    }
    private byte[] readAll(InputStream input,long max) throws IOException {
        ByteArrayOutputStream output=new ByteArrayOutputStream();
        byte[] buffer=new byte[8192];long total=0;int count;
        while((count=input.read(buffer))!=-1) {
            total+=count;
            if(total>max)throw new IOException("entry too large");
            output.write(buffer,0,count);
        }
        return output.toByteArray();
    }
    private String zipPath(String baseDir,String target) {
        if(target==null||target.isEmpty())return null;
        String value=target.replace('\\','/');
        if(value.startsWith("/"))return value.substring(1);
        return java.nio.file.Paths.get(baseDir,value).normalize().toString().replace('\\','/');
    }
    public static class Result { public List<RowDraft> rows=new ArrayList<>();public List<String> globalIssues=new ArrayList<>();public int imageCount;public boolean embeddedArtwork; }
    public static class RowDraft {
        public String sourceKey,sheetName;public int sourceRow;public BizQuestion question;
        public List<Issue> issues=new ArrayList<>();public List<PendingImage> pendingImages=new ArrayList<>();
        @JsonIgnore public List<Picture> pictures=new ArrayList<>();
    }
    public static class Issue { public String field,message;public Issue(){}public Issue(String field,String message){this.field=field;this.message=message;} }
    public static class PendingImage {
        public String pendingId,imageId,slot,reason;
        public PendingImage(){}
        public PendingImage(String id,String slot,String reason){this(null,id,slot,reason);}
        public PendingImage(String pendingId,String id,String slot,String reason){this.pendingId=pendingId;this.imageId=id;this.slot=slot;this.reason=reason;}
    }
    public static class Picture {
        public String slot,reason;public int row,col,row2,col2;public double dx1,dy1,dx2,dy2;public boolean ambiguous,transformed,embedded;
        @JsonIgnore public byte[] bytes;
    }
}
