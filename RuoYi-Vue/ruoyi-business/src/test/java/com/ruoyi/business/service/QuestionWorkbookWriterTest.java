package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.QuestionMedia;

class QuestionWorkbookWriterTest {
    @Test
    void templateContainsDedicatedImageColumns() throws Exception {
        QuestionWorkbookWriter writer=new QuestionWorkbookWriter();
        MockHttpServletResponse response=new MockHttpServletResponse();
        writer.template(response);
        try(Workbook book=new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            Sheet sheet=book.getSheet("题库");
            assertNotNull(sheet);
            assertEquals("题干图片1",sheet.getRow(0).getCell(13).getStringCellValue());
            assertEquals("选项D图片",sheet.getRow(0).getCell(19).getStringCellValue());
            assertNotNull(book.getSheet("填写说明"));
        }
    }

    @Test
    void exportPlacesImagesInDedicatedColumns() throws Exception {
        QuestionImageService images=mock(QuestionImageService.class);
        byte[] png=png();
        when(images.bytesForExport(anyLong(),nullable(Long.class))).thenReturn(png);
        QuestionWorkbookWriter writer=new QuestionWorkbookWriter();
        ReflectionTestUtils.setField(writer,"images",images);

        BizQuestion question=new BizQuestion();
        question.setQuestionType("choice");question.setQuestionContent("图文题");
        question.setGrade(6L);question.setSemester("0");question.setAnswer("A");
        question.setOptionA("甲");question.setOptionB("乙");question.setOptionC("丙");question.setOptionD("丁");
        question.setIsPublic("Y");
        QuestionMedia media=new QuestionMedia();
        media.stem.add(new QuestionMedia.ImageRef("11"));
        media.options.get("B").add(new QuestionMedia.ImageRef("12"));
        question.setMedia(media);

        MockHttpServletResponse response=new MockHttpServletResponse();
        writer.export(response,Arrays.asList(question));
        byte[] exported=response.getContentAsByteArray();
        try(Workbook book=new XSSFWorkbook(new ByteArrayInputStream(exported))) {
            Sheet sheet=book.getSheet("题库");
            assertEquals("题干图片1",sheet.getRow(0).getCell(13).getStringCellValue());
            assertEquals("选项B图片",sheet.getRow(0).getCell(17).getStringCellValue());
            XSSFDrawing drawing=(XSSFDrawing)sheet.getDrawingPatriarch();
            boolean stem=false,optionB=false;
            for(XSSFShape shape:drawing.getShapes()) {
                if(!(shape instanceof XSSFPicture))continue;
                int col=((XSSFPicture)shape).getClientAnchor().getCol1();
                int row=((XSSFPicture)shape).getClientAnchor().getRow1();
                if(row==1&&col==13)stem=true;
                if(row==1&&col==17)optionB=true;
            }
            assertTrue(stem);
            assertTrue(optionB);
        }
        QuestionWorkbookReader reader=new QuestionWorkbookReader();
        QuestionWorkbookReader.Result result=reader.read(exported);
        assertEquals(1,result.rows.size());
        assertEquals(1,result.rows.get(0).pictures.stream().filter(p->"stem".equals(p.slot)).count());
        assertEquals(1,result.rows.get(0).pictures.stream().filter(p->"B".equals(p.slot)).count());
        assertTrue(result.rows.get(0).pictures.stream().allMatch(p->p.reason==null));
    }

    private byte[] png() throws Exception {
        ByteArrayOutputStream output=new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(100,60,BufferedImage.TYPE_INT_RGB),"png",output);
        return output.toByteArray();
    }
}
