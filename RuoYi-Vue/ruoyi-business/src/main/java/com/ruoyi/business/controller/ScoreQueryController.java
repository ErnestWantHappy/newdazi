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
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.system.mapper.SysDeptMapper;
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

    @Autowired
    private com.ruoyi.business.mapper.BizLessonQuestionMapper lessonQuestionMapper;
    
    @Autowired
    private SysDeptMapper deptMapper;
    
    @Autowired
    private com.ruoyi.business.mapper.BizClassroomPerformanceMapper performanceMapper;

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
     * 获取课程列表（按年级和学校类型）
     */
    @GetMapping("/lessons")
    public AjaxResult getLessons(@RequestParam String entryYear) {
        // 获取当前用户的学校类型
        Long deptId = SecurityUtils.getDeptId();
        SysDept dept = deptMapper.selectDeptById(deptId);
        String schoolType = dept != null ? dept.getSchoolType() : "1"; // 默认小学
        
        // 根据入学年份和学校类型计算年级
        int gradeNum = calculateGrade(Integer.parseInt(entryYear), schoolType);
        
        String creator = SecurityUtils.getUsername();
        
        System.out.println("[课程下拉DEBUG] entryYear: " + entryYear);
        System.out.println("[课程下拉DEBUG] schoolType: " + schoolType + " (1=小学, 2=初中, 3=高中)");
        System.out.println("[课程下拉DEBUG] 计算的年级 gradeNum: " + gradeNum);
        System.out.println("[课程下拉DEBUG] creator: " + creator);
        
        List<?> lessons = lessonMapper.selectLessonsByGradeAndCreator((long) gradeNum, creator);
        System.out.println("[课程下拉DEBUG] 查询结果数量: " + (lessons != null ? lessons.size() : "null"));
        if (lessons != null && !lessons.isEmpty()) {
            System.out.println("[课程下拉DEBUG] 第一个课程: " + lessons.get(0));
        }
        
        return AjaxResult.success(lessons);
    }
    
    /**
     * 计算年级（基于入学年份和学校类型）
     * 小学: 入学对应1年级，到6年级
     * 初中: 入学对应7年级，到9年级
     * 高中: 入学对应10年级，到12年级
     */
    private int calculateGrade(int entryYear, String schoolType) {
        java.time.LocalDate now = java.time.LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        int currentDay = now.getDayOfMonth();
        
        // 判断是否已过8月15日（学年分界）
        boolean afterAug15 = (currentMonth > 8) || (currentMonth == 8 && currentDay >= 15);
        
        // 计算在校年数（不含入学年）
        int yearsInSchool = currentYear - entryYear;
        if (afterAug15) {
            yearsInSchool += 1;
        }
        
        // 根据学校类型计算年级
        int gradeOffset;
        switch (schoolType) {
            case "1": // 小学
                gradeOffset = 0; // 1年级入学
                break;
            case "2": // 初中
                gradeOffset = 6; // 7年级入学
                break;
            case "3": // 高中
                gradeOffset = 9; // 10年级入学
                break;
            default:
                gradeOffset = 0;
        }
        
        return yearsInSchool + gradeOffset;
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
        
        // 获取学校类型并计算年级
        SysDept dept = deptMapper.selectDeptById(deptId);
        String schoolType = dept != null ? dept.getSchoolType() : "1";
        int gradeNum = calculateGrade(Integer.parseInt(entryYear), schoolType);
        
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
            row.put("userName", student.getUserName()); // 账号（唯一标识）
            row.put("studentName", student.getStudentName());
            row.put("studentNo", student.getStudentNo());
            row.put("classCode", student.getClassCode()); // P2: 添加班级代码用于显示
            row.put("grade", gradeNum); // P3: 添加计算好的年级
            row.put("remark", student.getRemark()); // 教师备注
            
            // 查询该学生的成绩汇总
            List<Map<String, Object>> scores = studentAnswerMapper.selectScoreSummaryByStudent(
                student.getStudentId(), lessonId);
            
            // 查询该学生的平时分（所有课程的平时分记录）
            List<com.ruoyi.business.domain.BizClassroomPerformance> performanceList = 
                performanceMapper.selectByStudentId(student.getStudentId());
            
            // 将平时分添加到每个课程成绩中
            Map<Long, Integer> performanceMap = new HashMap<>();
            for (com.ruoyi.business.domain.BizClassroomPerformance p : performanceList) {
                performanceMap.put(p.getLessonId(), p.getScore());
            }
            for (Map<String, Object> score : scores) {
                Long lid = ((Number) score.get("lessonId")).longValue();
                Integer performanceScore = performanceMap.getOrDefault(lid, 0);
                score.put("performanceScore", performanceScore);
                // 计算课程总分 = 作业分 + 平时分，上限100
                int homeworkScore = ((Number) score.get("totalScore")).intValue();
                int finalScore = Math.min(homeworkScore + performanceScore, 100);
                score.put("finalScore", finalScore);
            }
            row.put("scores", scores);
            
            // 计算总分
            int totalScore = 0;
            int totalPerformance = 0;
            for (Map<String, Object> score : scores) {
                Object ts = score.get("totalScore");
                if (ts != null) {
                    totalScore += ((Number) ts).intValue();
                }
                Object ps = score.get("performanceScore");
                if (ps != null) {
                    totalPerformance += ((Number) ps).intValue();
                }
            }
            row.put("totalScore", totalScore);
            row.put("totalPerformance", totalPerformance);
            
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
        String[] fixedHeaders = {"账号", "班级", "学号", "姓名"};
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
        String[] statHeaders = {"打字平均", "理论平均", "操作平均", "总分", "平均分", "备注"};
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
            
            // 账号
            row.createCell(colIdx++).setCellValue((String) student.get("userName"));
            
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
            row.createCell(colIdx++).setCellValue(student.get("remark") != null ? (String) student.get("remark") : ""); // 备注
        }
        
        // 输出
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=scores.xlsx");
        wb.write(response.getOutputStream());
        wb.close();
    }

    /**
     * 获取题目分析数据 (选择题和判断题)
     */
    @GetMapping("/analysis/{lessonId}")
    public AjaxResult getQuestionAnalysis(@PathVariable Long lessonId, 
                                        @RequestParam(required = false) String classCode,
                                        @RequestParam(required = false) String entryYear) {
        // 1. 查询课程的所有题目详情
        List<com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo> questions = lessonQuestionMapper.selectDetailsByLessonId(lessonId);
        
        // 2. 查询该课程的答题记录 (根据筛选条件)
        List<com.ruoyi.business.domain.BizStudentAnswer> allAnswers;
        if ((classCode != null && !classCode.isEmpty()) || (entryYear != null && !entryYear.isEmpty())) {
            allAnswers = studentAnswerMapper.selectByLessonAndClass(lessonId, classCode, entryYear);
        } else {
            allAnswers = studentAnswerMapper.selectByLessonId(lessonId);
        }
        
        // 3. 按题目ID分组答题记录
        Map<Long, List<com.ruoyi.business.domain.BizStudentAnswer>> answerMap = new HashMap<>();
        for (com.ruoyi.business.domain.BizStudentAnswer ans : allAnswers) {
            answerMap.computeIfAbsent(ans.getQuestionId(), k -> new ArrayList<>()).add(ans);
        }
        
        // 4. 计算每道题的统计数据
        List<com.ruoyi.business.domain.vo.QuestionAnalysisDetailVo> analysisList = new ArrayList<>();
        
        for (com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo q : questions) {
            // 只分析选择题和判断题
            if (!"choice".equals(q.getQuestionType()) && !"judgment".equals(q.getQuestionType())) {
                continue;
            }
            
            com.ruoyi.business.domain.vo.QuestionAnalysisDetailVo vo = new com.ruoyi.business.domain.vo.QuestionAnalysisDetailVo();
            vo.setQuestionId(q.getQuestionId());
            vo.setQuestionContent(q.getQuestionContent());
            vo.setQuestionType(q.getQuestionType());
            vo.setAnswer(q.getAnswer());
            
            // 设置选项内容
            Map<String, String> optionContents = new HashMap<>();
            if ("choice".equals(q.getQuestionType())) {
                optionContents.put("A", q.getOptionA());
                optionContents.put("B", q.getOptionB());
                optionContents.put("C", q.getOptionC());
                optionContents.put("D", q.getOptionD());
            } else {
                optionContents.put("T", "正确");
                optionContents.put("F", "错误");
            }
            vo.setOptionContents(optionContents);
            
            List<com.ruoyi.business.domain.BizStudentAnswer> qAnswers = answerMap.getOrDefault(q.getQuestionId(), new ArrayList<>());
            vo.setStudentCount(qAnswers.size());
            
            int correctCount = 0;
            Map<String, Integer> dist = new HashMap<>();
            
            // 初始化选项分布
            if ("choice".equals(q.getQuestionType())) {
                dist.put("A", 0); dist.put("B", 0); dist.put("C", 0); dist.put("D", 0);
            } else {
                dist.put("T", 0); dist.put("F", 0);
            }
            
            for (com.ruoyi.business.domain.BizStudentAnswer ans : qAnswers) {
                if (Boolean.TRUE.equals(ans.getIsCorrect())) {
                    correctCount++;
                }
                String userAns = ans.getStudentAnswer();
                if (userAns != null) {
                    dist.put(userAns, dist.getOrDefault(userAns, 0) + 1);
                }
            }
            
            vo.setCorrectCount(correctCount);
            vo.setAnswerDistribution(dist);
            
            double accuracy = qAnswers.isEmpty() ? 0.0 : (double) correctCount / qAnswers.size() * 100;
            vo.setAccuracy(Math.round(accuracy * 10.0) / 10.0); // 保留1位小数
            
            analysisList.add(vo);
        }
        
        return AjaxResult.success(analysisList);
    }

    /**
     * 获取学生答题详情矩阵
     */
    @GetMapping("/studentAnswerMatrix")
    public List<com.ruoyi.business.domain.vo.StudentAnswerMatrixVo> getStudentAnswerMatrix(Long lessonId, String classCode, String entryYear) {
        // 获取学校类型并计算年级
        Long deptId = SecurityUtils.getDeptId();
        SysDept dept = deptMapper.selectDeptById(deptId);
        String schoolType = dept != null ? dept.getSchoolType() : "1";
        int gradeNum = calculateGrade(Integer.parseInt(entryYear), schoolType);
        
        List<com.ruoyi.business.domain.vo.StudentAnswerMatrixVo> result = studentAnswerMapper.selectStudentAnswerMatrix(lessonId, classCode, entryYear);
        
        // 格式化 className 为 年级+班级号 (如 "601")
        for (com.ruoyi.business.domain.vo.StudentAnswerMatrixVo vo : result) {
            if (vo.getClassName() != null && !vo.getClassName().isEmpty()) {
                String code = String.format("%02d", Integer.parseInt(vo.getClassName()));
                vo.setClassName(gradeNum + code);
            }
        }
        
        return result;
    }
}
