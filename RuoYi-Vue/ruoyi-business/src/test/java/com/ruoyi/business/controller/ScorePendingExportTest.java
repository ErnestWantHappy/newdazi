package com.ruoyi.business.controller;

import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.vo.LessonInfoVo;
import com.ruoyi.business.mapper.*;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.system.mapper.SysDeptMapper;
import java.io.ByteArrayInputStream;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** 经完整导出入口验证汇总分母和工作簿单元格，避免只测局部空值转换。 */
class ScorePendingExportTest
{
    @Test
    void pendingOnlyExportContainsLabelInsteadOfZero() throws Exception
    {
        try (Workbook book = export(null)) {
            assertEquals("待批改", cell(book, "操作平均").getStringCellValue());
            assertEquals("待批改", cell(book, "作业分").getStringCellValue());
            assertEquals("待批改", cell(book, "课程总分").getStringCellValue());
        }
    }

    @Test
    void pendingLessonDoesNotHalveGradedAverage() throws Exception
    {
        try (Workbook book = export(80)) {
            assertEquals(80D, cell(book, "操作平均").getNumericCellValue());
            assertEquals(80D, cell(book, "作业平均").getNumericCellValue());
            assertEquals(80D, cell(book, "课程平均分").getNumericCellValue());
        }
    }

    @Test
    void realZeroRemainsNumeric() throws Exception
    {
        try (Workbook book = export(0)) {
            assertEquals(0D, cell(book, "操作平均").getNumericCellValue());
            assertEquals(0D, cell(book, "课程平均分").getNumericCellValue());
        }
    }

    private Cell cell(Workbook book, String title) {
        Sheet sheet = book.getSheetAt(0);
        for (Cell header : sheet.getRow(0)) {
            if (title.equals(header.getStringCellValue())) return sheet.getRow(1).getCell(header.getColumnIndex());
        }
        throw new AssertionError("缺少导出列：" + title);
    }

    private Workbook export(Integer graded) throws Exception {
        ScoreQueryController controller = new ScoreQueryController();
        BizStudentMapper students = mock(BizStudentMapper.class);
        BizStudentAnswerMapper answers = mock(BizStudentAnswerMapper.class);
        BizLessonMapper lessons = mock(BizLessonMapper.class);
        ReflectionTestUtils.setField(controller, "studentMapper", students);
        ReflectionTestUtils.setField(controller, "studentAnswerMapper", answers);
        ReflectionTestUtils.setField(controller, "lessonMapper", lessons);
        ReflectionTestUtils.setField(controller, "deptMapper", mock(SysDeptMapper.class));
        ReflectionTestUtils.setField(controller, "performanceMapper", mock(BizClassroomPerformanceMapper.class));
        ReflectionTestUtils.setField(controller, "scoreAdjustmentMapper", mock(BizScoreAdjustmentMapper.class));
        BizStudent student = new BizStudent();
        student.setStudentId(1L); student.setStudentNo("1"); student.setStudentName("测试学生"); student.setClassCode("1");
        when(students.selectScoreStudents(anyLong(), anyLong(), anyString(), anyString(), isNull())).thenReturn(Collections.singletonList(student));
        List<LessonInfoVo> visible = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 1; i <= (graded == null ? 1 : 2); i++) {
            LessonInfoVo lesson = new LessonInfoVo(); lesson.setLessonId((long) i); lesson.setLessonNum(i); lesson.setLessonTitle("测试课" + i); visible.add(lesson);
            Map<String, Object> row = new HashMap<>();
            row.put("studentId", 1L); row.put("lessonId", (long) i); row.put("lessonNum", i);
            row.put("totalScore", i == 1 ? null : graded); row.put("practicalScore", i == 1 ? null : graded);
            row.put("practicalPending", i == 1 ? 1 : 0); rows.add(row);
        }
        when(lessons.selectScoreLessons(anyString(), anyString(), anyLong(), anyLong())).thenReturn(visible);
        when(answers.selectScoreSummaryByStudents(anyList(), anyList())).thenReturn(rows);
        SysUser user = new SysUser(); user.setUserName("测试教师");
        LoginUser login = new LoginUser(200L, 9L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login, null, Collections.emptyList()));
        try {
            MockHttpServletResponse response = new MockHttpServletResponse();
            controller.exportScoreExcel("2025", "1", graded == null ? "1" : "1,2", null,
                    "studentName,lessonDetails,avgPractical,filteredTotal,finalTotal", response);
            return new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
