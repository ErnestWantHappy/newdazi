package com.ruoyi.business.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.common.utils.SecurityUtils;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 成绩查询 Controller
 * 
 * @author ruoyi
 * @date 2026-01-08
 */
@RestController
@RequestMapping("/business/score")
public class ScoreQueryController extends BaseController {

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;
    
    @Autowired
    private BizStudentMapper studentMapper;
    
    @Autowired
    private BizLessonMapper lessonMapper;

    /**
     * 获取班级列表（用于筛选下拉框）
     */
    @GetMapping("/classes")
    public AjaxResult getClasses() {
        Long userId = SecurityUtils.getUserId();
        Long deptId = SecurityUtils.getDeptId();
        return AjaxResult.success(studentMapper.selectDistinctYearAndClassByDeptId(deptId));
    }
    
    /**
     * 获取课程列表（按年级）
     */
    @GetMapping("/lessons")
    public AjaxResult getLessons(@RequestParam String entryYear) {
        // 根据入学年份计算年级（以8月15日为学年分界）
        int gradeNum = calculateGrade(Integer.parseInt(entryYear));
        
        String creator = SecurityUtils.getUsername();
        return AjaxResult.success(lessonMapper.selectLessonsByGradeAndCreator((long) gradeNum, creator));
    }
    
    /**
     * 计算年级（以8月15日为分界点）
     * 2025年入学的学生在 2025-08-15 ~ 2026-08-15 期间为7年级
     */
    private int calculateGrade(int entryYear) {
        java.time.LocalDate now = java.time.LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        int currentDay = now.getDayOfMonth();
        
        // 判断是否已过8月15日
        boolean afterAug15 = (currentMonth > 8) || (currentMonth == 8 && currentDay >= 15);
        
        // 如果已过8月15日，学年为 currentYear，否则学年为 currentYear - 1
        int schoolYear = afterAug15 ? currentYear : currentYear - 1;
        
        // 年级 = 学年 - 入学年份 + 7
        return schoolYear - entryYear + 7;
    }

    /**
     * 成绩汇总查询
     */
    @GetMapping("/summary")
    public AjaxResult getSummary(
            @RequestParam String entryYear,
            @RequestParam(required = false) String classCode,
            @RequestParam(required = false) Long lessonId) {
        
        Long deptId = SecurityUtils.getDeptId();
        Long userId = SecurityUtils.getUserId();
        
        // 查询学生列表
        BizStudent query = new BizStudent();
        query.setEntryYear(entryYear);
        query.setClassCode(classCode);
        query.setTeacherUserId(userId);
        query.setDeptId(deptId);
        List<BizStudent> students = studentMapper.selectBizStudentList(query);
        
        // 查询每个学生的成绩
        List<Map<String, Object>> result = new ArrayList<>();
        for (BizStudent student : students) {
            Map<String, Object> row = new HashMap<>();
            row.put("studentId", student.getStudentId());
            row.put("studentName", student.getStudentName());
            row.put("studentNo", student.getStudentNo());
            row.put("classCode", student.getClassCode()); // P2: 添加班级代码用于显示
            
            // 查询该学生的成绩汇总
            List<Map<String, Object>> scores = studentAnswerMapper.selectScoreSummaryByStudent(
                student.getStudentId(), lessonId);
            row.put("scores", scores);
            
            // 计算总分
            int totalScore = 0;
            for (Map<String, Object> score : scores) {
                Object ts = score.get("totalScore");
                if (ts != null) {
                    totalScore += ((Number) ts).intValue();
                }
            }
            row.put("totalScore", totalScore);
            
            result.add(row);
        }
        
        return AjaxResult.success(result);
    }
    
    /**
     * 导出成绩 Excel
     */
    @GetMapping("/export")
    public void exportScoreExcel(
            @RequestParam String entryYear,
            @RequestParam(required = false) String classCode,
            @RequestParam(required = false) String lessonIds,
            HttpServletResponse response) throws IOException {
        
        // 1. 获取基础数据
        AjaxResult summaryResult = getSummary(entryYear, classCode, null); // 获取全部数据
        List<Map<String, Object>> students = (List<Map<String, Object>>) summaryResult.get("data");
        
        // 2. 解析选中的课程ID
        List<Long> selectedLessonIds = new ArrayList<>();
        if (com.ruoyi.common.utils.StringUtils.isNotEmpty(lessonIds)) {
            for (String id : lessonIds.split(",")) {
                selectedLessonIds.add(Long.parseLong(id));
            }
        }
        
        // 3. 获取所有课程信息（用于表头）
        AjaxResult lessonResult = getLessons(entryYear);
        List<Map<String, Object>> allLessons = (List<Map<String, Object>>) lessonResult.get("data");
        List<Map<String, Object>> targetLessons = new ArrayList<>();
        
        if (selectedLessonIds.isEmpty()) {
            targetLessons.addAll(allLessons);
        } else {
            for (Map<String, Object> lesson : allLessons) {
                Long id = ((Number) lesson.get("lessonId")).longValue();
                if (selectedLessonIds.contains(id)) {
                    targetLessons.add(lesson);
                }
            }
        }
        
        // 按课次排序
        targetLessons.sort((a, b) -> ((Number) a.get("lessonNum")).intValue() - ((Number) b.get("lessonNum")).intValue());

        // 4. 创建 Excel
        org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("成绩汇总");
        
        // 样式
        org.apache.poi.ss.usermodel.CellStyle headerStyle = wb.createCellStyle();
        headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        org.apache.poi.ss.usermodel.Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        // 5. 表头
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        int colIdx = 0;
        String[] fixedHeaders = {"班级", "学号", "姓名"};
        for (String h : fixedHeaders) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(colIdx++);
            cell.setCellValue(h);
            cell.setCellStyle(headerStyle);
        }
        
        // 动态课程表头
        for (Map<String, Object> lesson : targetLessons) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(colIdx++);
            cell.setCellValue((String) lesson.get("lessonTitle"));
            cell.setCellStyle(headerStyle);
        }
        
        // 统计表头
        String[] statHeaders = {"打字平均", "理论平均", "操作平均", "总分", "平均分"};
        for (String h : statHeaders) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(colIdx++);
            cell.setCellValue(h);
            cell.setCellStyle(headerStyle);
        }
        
        // 6. 填充数据
        int rowIdx = 1;
        // 计算年级
        int currentYear = java.time.Year.now().getValue();
        int entry = Integer.parseInt(entryYear);
        int gradeNum = currentYear - entry + 7;
        
        // 排序学生 (按学号)
        students.sort((a, b) -> {
            String noA = (String) a.get("studentNo");
            String noB = (String) b.get("studentNo");
            try {
                return Integer.parseInt(noA) - Integer.parseInt(noB);
            } catch (Exception e) {
                return noA.compareTo(noB);
            }
        });

        for (Map<String, Object> student : students) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
            colIdx = 0;
            
            // 班级
            String studentClassCode = (String) student.get("classCode");
            String classDisplay = "";
            if (studentClassCode != null) {
                try {
                    classDisplay = String.format("%d%02d", gradeNum, Integer.parseInt(studentClassCode));
                } catch (Exception e) {
                     classDisplay = gradeNum + studentClassCode;
                }
            }
            row.createCell(colIdx++).setCellValue(Integer.parseInt(classDisplay));
            
            // 学号、姓名
            row.createCell(colIdx++).setCellValue((String) student.get("studentNo"));
            row.createCell(colIdx++).setCellValue((String) student.get("studentName"));
            
            // 课程成绩
            List<Map<String, Object>> scores = (List<Map<String, Object>>) student.get("scores");
            double sumTyping = 0, sumTheory = 0, sumPractical = 0, sumTotal = 0;
            int count = 0;
            
            for (Map<String, Object> lesson : targetLessons) {
                Long lessonId = ((Number) lesson.get("lessonId")).longValue();
                Map<String, Object> targetScore = null;
                for (Map<String, Object> s : scores) {
                    if (((Number) s.get("lessonId")).longValue() == lessonId) {
                        targetScore = s;
                        break;
                    }
                }
                
                int scoreVal = 0;
                if (targetScore != null) {
                    scoreVal = ((Number) targetScore.get("totalScore")).intValue();
                    sumTyping += ((Number) targetScore.get("typingScore")).intValue();
                    sumTheory += ((Number) targetScore.get("theoryScore")).intValue();
                    sumPractical += ((Number) targetScore.get("practicalScore")).intValue();
                    sumTotal += scoreVal;
                    count++;
                }
                
                // 只有在该课程有成绩记录时才算入(或者只要选中了这门课就算分母?)
                // 前端逻辑：filter 后，length 为 count。如果 backend 返回的 scores 只包含有成绩的课程，那没问题。
                // 实际上 getSummary 返回的 scores 是 selectByStudent 查出来的，只有有记录的才会有。
                // 需求：选中了这门课，如果还没考，是显示0还是空？前端显示 tag 0。
                // 这里导出为 0 比较合适。
                
                row.createCell(colIdx++).setCellValue(scoreVal);
            }
            
            // 统计列
            double avgTyping = count > 0 ? sumTyping / count : 0;
            double avgTheory = count > 0 ? sumTheory / count : 0;
            double avgPractical = count > 0 ? sumPractical / count : 0;
            double avgTotal = count > 0 ? sumTotal / count : 0;
            
            row.createCell(colIdx++).setCellValue(String.format("%.1f", avgTyping));
            row.createCell(colIdx++).setCellValue(String.format("%.1f", avgTheory));
            row.createCell(colIdx++).setCellValue(String.format("%.1f", avgPractical)); // 操作平均
            row.createCell(colIdx++).setCellValue((int) sumTotal); // 总分
            row.createCell(colIdx++).setCellValue(String.format("%.1f", avgTotal)); // 平均分
        }
        
        // 输出
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=scores.xlsx");
        wb.write(response.getOutputStream());
        wb.close();
    }
}
