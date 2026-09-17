package com.ruoyi.business.controller;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BizStudentControllerTemplateTest
{
    @Test
    void templateContainsTwoUsefulExampleRows() throws Exception
    {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new BizStudentController().importTemplate(response);
        byte[] workbookBytes = response.getContentAsByteArray();
        assertTrue(workbookBytes.length > 0);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(workbookBytes)))
        {
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);
            Map<String, Integer> columns = new HashMap<>();
            for (Cell cell : header)
            {
                columns.put(cell.getStringCellValue(), cell.getColumnIndex());
            }
            assertEquals("01", sheet.getRow(1).getCell(columns.get("学号")).getStringCellValue());
            assertEquals("2025", sheet.getRow(1).getCell(columns.get("入学年份")).getStringCellValue());
            assertEquals("01", sheet.getRow(1).getCell(columns.get("班级编号")).getStringCellValue());
            assertEquals("02", sheet.getRow(2).getCell(columns.get("班级编号")).getStringCellValue());
        }

        String outputPath = System.getProperty("student.template.output");
        if (outputPath != null && !outputPath.trim().isEmpty())
        {
            Path output = Paths.get(outputPath);
            Files.createDirectories(output.getParent());
            Files.write(output, workbookBytes);
        }
    }
}
