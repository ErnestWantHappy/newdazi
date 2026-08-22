package com.ruoyi.business.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.business.domain.BizScoreAdjustment;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizScoreAdjustmentMapper;
import com.ruoyi.business.mapper.BizTeacherClassMapper;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizLessonGuideSheetBinding;
import com.ruoyi.business.domain.vo.LessonInfoVo;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.business.util.AcademicYearUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.file.DownloadFileNameUtils;
import com.ruoyi.common.utils.file.FileUtils;
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
@PreAuthorize("@ss.hasPermi('business:score:list')")
public class ScoreQueryController extends BaseController {
    private static final String ADJUST_ACTION = "ADJUST";
    private static final String CANCEL_ACTION = "CANCEL";

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

    @Autowired
    private BizScoreAdjustmentMapper scoreAdjustmentMapper;

    @Autowired
    private GuideSheetAccessService guideSheetAccessService;

    @Autowired
    private BizTeacherClassMapper teacherClassMapper;

    /**
     * 设置/取消某节课缺考请假
     */
    @PutMapping("/absent")
    public AjaxResult setAbsent(@RequestBody Map<String, Object> params) {
        // 前端可能传字符串或缺失字段，直接强转 Number 会 NPE/500，这里统一做类型校验
        Object lessonIdValue = params == null ? null : params.get("lessonId");
        Object studentIdValue = params == null ? null : params.get("studentId");
        if (!(lessonIdValue instanceof Number) || !(studentIdValue instanceof Number)) {
            return AjaxResult.error("参数不完整：lessonId 与 studentId 必须为数字");
        }
        Long lessonId = ((Number) lessonIdValue).longValue();
        Long studentId = ((Number) studentIdValue).longValue();
        Boolean isAbsent = params.get("isAbsent") instanceof Boolean ? (Boolean) params.get("isAbsent") : null;
        if (isAbsent == null) {
            return AjaxResult.error("参数不完整：isAbsent 必须为布尔值");
        }

        Long deptId = SecurityUtils.getDeptId();
        Long teacherId = SecurityUtils.getUserId();

        String scopeError = validateManualScoreScope(studentId, lessonId);
        if (scopeError != null) {
            return AjaxResult.error(scopeError);
        }

        // 原子 upsert：依赖 uk_student_lesson 唯一键，并发双击不再双插/500；
        // 标记缺考时清零表现分，取消缺考保留原表现分
        com.ruoyi.business.domain.BizClassroomPerformance performance =
                new com.ruoyi.business.domain.BizClassroomPerformance();
        performance.setStudentId(studentId);
        performance.setLessonId(lessonId);
        performance.setTeacherId(teacherId);
        performance.setDeptId(deptId);
        performance.setIsAbsent(isAbsent ? 1 : 0);
        performanceMapper.upsertAbsent(performance);
        return AjaxResult.success();
    }

    /**
     * 人工修正某学生本节课作业分。
     */
    @PutMapping("/manual-homework-score")
    public AjaxResult saveManualHomeworkScore(@RequestBody ManualHomeworkScoreRequest request) {
        String requestError = validateManualHomeworkRequest(request, true);
        if (requestError != null) {
            return AjaxResult.error(requestError);
        }

        String scopeError = validateManualScoreScope(request.getStudentId(), request.getLessonId());
        if (scopeError != null) {
            return AjaxResult.error(scopeError);
        }

        com.ruoyi.business.domain.BizClassroomPerformance performance =
                performanceMapper.selectByStudentAndLesson(request.getStudentId(), request.getLessonId());
        if (performance != null && performance.getIsAbsent() != null && performance.getIsAbsent() == 1) {
            return AjaxResult.error("该学生本节课已标记请假，请先取消请假后再改分");
        }

        // 幂等保护：同一 (student, lesson) 只允许一条生效的 ADJUST，重复提交/并发双击直接拒绝
        BizScoreAdjustment latestAdjustment = scoreAdjustmentMapper.selectLatestByStudentAndLesson(
                request.getStudentId(), request.getLessonId(), SecurityUtils.getDeptId());
        if (isActiveHomeworkAdjustment(latestAdjustment)) {
            return AjaxResult.error("该学生本节课已存在生效的人工修正，请先取消后再改分");
        }

        int originalScore = selectRawHomeworkScore(request.getStudentId(), request.getLessonId());
        BizScoreAdjustment adjustment = buildScoreAdjustment(
                request.getStudentId(),
                request.getLessonId(),
                originalScore,
                request.getAdjustedScore(),
                ADJUST_ACTION,
                request.getReason()
        );
        scoreAdjustmentMapper.insert(adjustment);
        return AjaxResult.success("改分成功");
    }

    /**
     * 取消某学生本节课作业分人工修正。
     */
    @PutMapping("/manual-homework-score/cancel")
    public AjaxResult cancelManualHomeworkScore(@RequestBody ManualHomeworkScoreRequest request) {
        String requestError = validateManualHomeworkRequest(request, false);
        if (requestError != null) {
            return AjaxResult.error(requestError);
        }

        String scopeError = validateManualScoreScope(request.getStudentId(), request.getLessonId());
        if (scopeError != null) {
            return AjaxResult.error(scopeError);
        }

        Long deptId = SecurityUtils.getDeptId();
        BizScoreAdjustment latest = scoreAdjustmentMapper.selectLatestByStudentAndLesson(
                request.getStudentId(), request.getLessonId(), deptId);
        if (!isActiveHomeworkAdjustment(latest)) {
            return AjaxResult.success("当前没有人工修正");
        }

        int originalScore = selectRawHomeworkScore(request.getStudentId(), request.getLessonId());
        BizScoreAdjustment adjustment = buildScoreAdjustment(
                request.getStudentId(),
                request.getLessonId(),
                originalScore,
                null,
                CANCEL_ACTION,
                request.getReason()
        );
        scoreAdjustmentMapper.insert(adjustment);
        return AjaxResult.success("已取消人工修正");
    }

    /**
     * 获取班级列表（用于筛选下拉框）
     */
    @GetMapping("/classes")
    public AjaxResult getClasses() {
        Long userId = SecurityUtils.getUserId();
        Long deptId = SecurityUtils.getDeptId();
        if (SecurityUtils.isAdmin(userId)) {
            return AjaxResult.success(teacherClassMapper.selectAvailableClasses(deptId));
        }
        return AjaxResult.success(teacherClassMapper.selectTeacherClassListWithCount(userId, deptId));
    }
    
    /**
     * 获取课程列表（按年级和学校类型）
     */
    @GetMapping("/lessons")
    public AjaxResult getLessons(@RequestParam String entryYear) {
        Long deptId = SecurityUtils.getDeptId();
        String creator = SecurityUtils.getUsername();
        List<?> lessons = lessonMapper.selectScoreLessons(
                entryYear, creator, SecurityUtils.getUserId(), deptId);
        return AjaxResult.success(lessons);
    }

    /**
     * 返回成绩页已锁定课程和班级的导学单上下文。
     */
    @GetMapping("/guide-sheet-context")
    public AjaxResult getGuideSheetContext(@RequestParam Long lessonId,
                                           @RequestParam String entryYear,
                                           @RequestParam String classCode) {
        BizLessonGuideSheetBinding binding = guideSheetAccessService.requireLessonClassBindingContext(
                lessonId, entryYear, classCode);
        if (binding == null) {
            return AjaxResult.success().put("enabled", false);
        }
        return AjaxResult.success()
                .put("enabled", "Y".equals(binding.getEnabled()))
                .put("bindingId", binding.getBindingId())
                .put("lessonId", binding.getLessonId())
                .put("sourceSheetId", binding.getSourceSheetId())
                .put("title", binding.getSnapshotTitle())
                .put("entryYear", entryYear)
                .put("classCode", classCode);
    }
    
    /**
     * 计算年级（基于入学年份和学校类型）
     * 小学: 入学对应1年级，到6年级
     * 初中: 入学对应7年级，到9年级
     * 高中: 入学对应10年级，到12年级
     */
    private int calculateGrade(String entryYear, String schoolType) {
        try {
            return AcademicYearUtils.resolveDisplayGrade(
                    Integer.parseInt(entryYear == null ? "" : entryYear.trim()),
                    schoolType, java.time.LocalDate.now());
        } catch (NumberFormatException e) {
            throw new ServiceException("入学年份必须是四位数字");
        } catch (IllegalArgumentException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 成绩汇总查询
     */
    @GetMapping("/summary")
    public AjaxResult getSummary(
            @RequestParam String entryYear,
            @RequestParam(required = false) String classCode,
            @RequestParam(required = false) Long lessonId,
            @RequestParam(required = false) String lessonIds,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword) {
        
        Long deptId = SecurityUtils.getDeptId();
        Long userId = SecurityUtils.getUserId();
        
        // 获取学校类型并计算年级
        SysDept dept = deptMapper.selectDeptById(deptId);
        String schoolType = dept != null ? dept.getSchoolType() : "1";
        int gradeNum = calculateGrade(entryYear, schoolType);
        
        List<Long> selectedLessonIds = parseLessonIds(lessonIds, lessonId);
        selectedLessonIds = validateSelectedLessonIds(entryYear, selectedLessonIds);

        List<BizStudent> students;
        long total = 0;
        if (pageNum != null && pageSize != null && pageNum > 0 && pageSize > 0) {
            PageHelper.startPage(pageNum, pageSize);
            students = studentMapper.selectScoreStudents(userId, deptId, entryYear, classCode, keyword);
            PageInfo<BizStudent> pageInfo = new PageInfo<>(students);
            total = pageInfo.getTotal();
        } else {
            students = studentMapper.selectScoreStudents(userId, deptId, entryYear, classCode, keyword);
            total = students.size();
        }
        
        List<Map<String, Object>> result = buildScoreSummaryRows(students, selectedLessonIds, deptId, gradeNum);
        return AjaxResult.success(result)
                .put("rows", result)
                .put("total", total)
                .put("pageNum", pageNum)
                .put("pageSize", pageSize);
    }

    private List<Long> parseLessonIds(String lessonIds, Long lessonId) {
        try {
            LinkedHashSet<Long> result = new LinkedHashSet<>();
            if (lessonIds != null && !lessonIds.trim().isEmpty()) {
                for (String id : lessonIds.split(",")) {
                    if (id != null && !id.trim().isEmpty()) {
                        result.add(Long.parseLong(id.trim()));
                    }
                }
            } else if (lessonId != null) {
                result.add(lessonId);
            }
            return new ArrayList<>(result);
        } catch (NumberFormatException e) {
            throw new ServiceException("课程参数格式错误");
        }
    }

    /** 只允许查询成绩页已返回给当前教师的常规课，空选择等价于全部可见课程。 */
    private List<Long> validateSelectedLessonIds(String entryYear, List<Long> selectedLessonIds) {
        Long userId = SecurityUtils.getUserId();
        Long deptId = SecurityUtils.getDeptId();
        List<LessonInfoVo> visibleLessons = lessonMapper.selectScoreLessons(
                entryYear, SecurityUtils.getUsername(), userId, deptId);
        LinkedHashSet<Long> visibleIds = visibleLessons == null ? new LinkedHashSet<>()
                : visibleLessons.stream()
                .map(LessonInfoVo::getLessonId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (selectedLessonIds == null || selectedLessonIds.isEmpty()) {
            return new ArrayList<>(visibleIds);
        }
        if (!visibleIds.containsAll(selectedLessonIds)) {
            throw new ServiceException("包含无权查看或不计成绩的课程");
        }
        return selectedLessonIds;
    }

    private List<Map<String, Object>> buildScoreSummaryRows(List<BizStudent> students, List<Long> lessonIds,
                                                            Long deptId, int gradeNum) {
        if (students == null || students.isEmpty() || lessonIds == null || lessonIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> studentIds = students.stream()
                .map(BizStudent::getStudentId)
                .collect(java.util.stream.Collectors.toList());

        List<Map<String, Object>> scoreRows = studentAnswerMapper.selectScoreSummaryByStudents(studentIds, lessonIds);
        List<com.ruoyi.business.domain.BizClassroomPerformance> performanceRows =
                performanceMapper.selectByStudentIdsAndLessons(studentIds, lessonIds, deptId);
        List<BizScoreAdjustment> adjustmentRows =
                scoreAdjustmentMapper.selectLatestByStudentIdsAndLessons(studentIds, lessonIds, deptId);

        Map<Long, List<Map<String, Object>>> scoresByStudent = new HashMap<>();
        for (Map<String, Object> score : scoreRows) {
            Long studentId = ((Number) score.get("studentId")).longValue();
            scoresByStudent.computeIfAbsent(studentId, k -> new ArrayList<>()).add(score);
        }

        Map<Long, Map<Long, com.ruoyi.business.domain.BizClassroomPerformance>> performanceByStudent = new HashMap<>();
        for (com.ruoyi.business.domain.BizClassroomPerformance performance : performanceRows) {
            performanceByStudent
                    .computeIfAbsent(performance.getStudentId(), k -> new HashMap<>())
                    .put(performance.getLessonId(), performance);
        }

        Map<Long, Map<Long, BizScoreAdjustment>> adjustmentByStudent = new HashMap<>();
        for (BizScoreAdjustment adjustment : adjustmentRows) {
            adjustmentByStudent
                    .computeIfAbsent(adjustment.getStudentId(), k -> new HashMap<>())
                    .put(adjustment.getLessonId(), adjustment);
        }

        Map<Long, BizLesson> lessonCache = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (BizStudent student : students) {
            Long studentId = student.getStudentId();
            Map<String, Object> row = new HashMap<>();
            row.put("studentId", studentId);
            row.put("userName", student.getUserName());
            row.put("studentName", student.getStudentName());
            row.put("studentNo", student.getStudentNo());
            row.put("classCode", student.getClassCode());
            row.put("grade", gradeNum);
            row.put("remark", student.getRemark());

            List<Map<String, Object>> scores = new ArrayList<>(scoresByStudent.getOrDefault(studentId, Collections.emptyList()));
            Map<Long, com.ruoyi.business.domain.BizClassroomPerformance> performanceMap =
                    performanceByStudent.getOrDefault(studentId, Collections.emptyMap());
            Map<Long, BizScoreAdjustment> adjustmentMap =
                    adjustmentByStudent.getOrDefault(studentId, Collections.emptyMap());

            List<Long> scoredLessonIds = new ArrayList<>();
            for (Map<String, Object> score : scores) {
                Long lid = ((Number) score.get("lessonId")).longValue();
                scoredLessonIds.add(lid);
                applyHomeworkAdjustment(score, adjustmentMap.get(lid));
                applyPerformanceToScore(score, performanceMap.get(lid));
            }

            Set<Long> extraLessonIds = new HashSet<>(performanceMap.keySet());
            for (BizScoreAdjustment adjustment : adjustmentMap.values()) {
                if (isActiveHomeworkAdjustment(adjustment)) {
                    extraLessonIds.add(adjustment.getLessonId());
                }
            }

            for (Long lid : extraLessonIds) {
                if (scoredLessonIds.contains(lid)) {
                    continue;
                }
                Map<String, Object> extraScore = new HashMap<>();
                extraScore.put("studentId", studentId);
                extraScore.put("lessonId", lid);
                extraScore.put("typingScore", 0);
                extraScore.put("theoryScore", 0);
                extraScore.put("practicalScore", 0);
                extraScore.put("totalScore", 0);
                BizLesson lesson = lessonCache.computeIfAbsent(lid, lessonMapper::selectBizLessonByLessonId);
                if (lesson != null) {
                    extraScore.put("lessonTitle", lesson.getLessonTitle());
                    extraScore.put("lessonNum", lesson.getLessonNum());
                }
                applyHomeworkAdjustment(extraScore, adjustmentMap.get(lid));
                applyPerformanceToScore(extraScore, performanceMap.get(lid));
                scores.add(extraScore);
            }

            scores.sort((a, b) -> {
                Number numA = (Number) a.get("lessonNum");
                Number numB = (Number) b.get("lessonNum");
                int lessonNumA = numA == null ? 0 : numA.intValue();
                int lessonNumB = numB == null ? 0 : numB.intValue();
                return lessonNumA - lessonNumB;
            });
            row.put("scores", scores);

            int homeworkTotal = 0;
            int performanceTotal = 0;
            int finalTotal = 0;
            int validScoreCount = 0;
            for (Map<String, Object> score : scores) {
                if (Boolean.TRUE.equals(score.get("isAbsent"))) {
                    continue;
                }
                // 考勤课不进入作业均分分母（selectScoreLessons 已排除；此处双保险）
                Long scoreLessonId = score.get("lessonId") == null ? null : ((Number) score.get("lessonId")).longValue();
                if (scoreLessonId != null) {
                    BizLesson scoreLesson = lessonCache.computeIfAbsent(scoreLessonId, lessonMapper::selectBizLessonByLessonId);
                    if (scoreLesson != null && "attendance".equalsIgnoreCase(scoreLesson.getLessonMode())) {
                        continue;
                    }
                }
                homeworkTotal += numberToInt(score.get("totalScore"));
                performanceTotal += numberToInt(score.get("performanceScore"));
                finalTotal += numberToInt(score.get("finalScore"));
                validScoreCount++;
            }
            row.put("totalScore", homeworkTotal);
            row.put("totalPerformance", performanceTotal);
            row.put("totalFinalScore", finalTotal);
            row.put("averageHomeworkScore", validScoreCount > 0 ? roundOne((double) homeworkTotal / validScoreCount) : 0D);
            row.put("averagePerformanceScore", validScoreCount > 0 ? roundOne((double) performanceTotal / validScoreCount) : 0D);
            row.put("averageFinalScore", validScoreCount > 0 ? roundOne((double) finalTotal / validScoreCount) : 0D);
            row.put("validScoreCount", validScoreCount);

            result.add(row);
        }

        return result;
    }

    private void applyHomeworkAdjustment(Map<String, Object> score, BizScoreAdjustment adjustment) {
        int originalScore = numberToInt(score.get("totalScore"));
        score.put("originalTotalScore", originalScore);
        score.put("manualAdjusted", false);
        if (!isActiveHomeworkAdjustment(adjustment)) {
            return;
        }

        int adjustedScore = clampScore(adjustment.getAdjustedHomeworkScore());
        score.put("totalScore", adjustedScore);
        score.put("manualAdjusted", true);
        score.put("adjustmentReason", adjustment.getReason());
        score.put("adjustedBy", adjustment.getCreateBy());
        score.put("adjustedTime", adjustment.getCreateTime());
    }

    private void applyPerformanceToScore(Map<String, Object> score,
                                         com.ruoyi.business.domain.BizClassroomPerformance performance) {
        int performanceScore = performance != null && performance.getScore() != null ? performance.getScore() : 0;
        boolean isAbsent = performance != null && performance.getIsAbsent() != null && performance.getIsAbsent() == 1;
        score.put("performanceScore", performanceScore);
        score.put("isAbsent", isAbsent);
        if (isAbsent) {
            score.put("finalScore", null);
        } else {
            score.put("finalScore", clampScore(numberToInt(score.get("totalScore")) + performanceScore));
        }
    }

    private String validateManualHomeworkRequest(ManualHomeworkScoreRequest request, boolean requireAdjustedScore) {
        if (request == null || request.getStudentId() == null || request.getLessonId() == null) {
            return "缺少学生或课程信息";
        }
        if (!requireAdjustedScore) {
            return null;
        }
        if (request.getAdjustedScore() == null) {
            return "请填写修正后的作业分";
        }
        if (request.getAdjustedScore() < 0 || request.getAdjustedScore() > 100) {
            return "作业分必须在0到100之间";
        }
        String reason = trimToEmpty(request.getReason());
        if (reason.isEmpty()) {
            return "请填写改分原因";
        }
        if (reason.length() > 255) {
            return "改分原因最多255个字符";
        }
        return null;
    }

    private String validateManualScoreScope(Long studentId, Long lessonId) {
        Long deptId = SecurityUtils.getDeptId();
        Long teacherId = SecurityUtils.getUserId();

        BizStudent student = studentMapper.selectBizStudentByStudentId(studentId);
        if (student == null) {
            return "学生不存在";
        }
        if (student.getDeptId() != null && !deptId.equals(student.getDeptId())) {
            return "不能修改其他学校的学生成绩";
        }
        if (studentMapper.countManagedScoreStudent(teacherId, deptId, studentId) <= 0) {
            return "只能修改自己管理班级的学生成绩";
        }

        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        if (lesson == null) {
            return "课程不存在";
        }
        if (lesson.getDeptId() != null && !deptId.equals(lesson.getDeptId())) {
            return "不能修改其他学校的课程成绩";
        }
        if ("attendance".equalsIgnoreCase(lesson.getLessonMode())) {
            return "课堂考勤不计作业分，不能在成绩页改分";
        }
        try {
            guideSheetAccessService.assertCanViewLessonClass(
                    lessonId, student.getEntryYear(), student.getClassCode());
        } catch (ServiceException e) {
            return e.getMessage();
        }
        return null;
    }

    private BizScoreAdjustment buildScoreAdjustment(Long studentId, Long lessonId, Integer originalScore,
                                                    Integer adjustedScore, String actionType, String reason) {
        BizScoreAdjustment adjustment = new BizScoreAdjustment();
        adjustment.setStudentId(studentId);
        adjustment.setLessonId(lessonId);
        adjustment.setDeptId(SecurityUtils.getDeptId());
        adjustment.setTeacherId(SecurityUtils.getUserId());
        adjustment.setOriginalHomeworkScore(originalScore == null ? 0 : originalScore);
        adjustment.setAdjustedHomeworkScore(adjustedScore);
        adjustment.setActionType(actionType);
        adjustment.setReason(limitLength(trimToEmpty(reason), 255));
        adjustment.setCreateBy(SecurityUtils.getUsername());
        adjustment.setCreateTime(new Date());
        return adjustment;
    }

    private int selectRawHomeworkScore(Long studentId, Long lessonId) {
        List<Map<String, Object>> rows = studentAnswerMapper.selectScoreSummaryByStudents(
                Collections.singletonList(studentId),
                Collections.singletonList(lessonId)
        );
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        return numberToInt(rows.get(0).get("totalScore"));
    }

    private boolean isActiveHomeworkAdjustment(BizScoreAdjustment adjustment) {
        return adjustment != null
                && ADJUST_ACTION.equals(adjustment.getActionType())
                && adjustment.getAdjustedHomeworkScore() != null;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String limitLength(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private int numberToInt(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private double numberToDouble(Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : 0D;
    }

    private int clampScore(int score) {
        return Math.min(Math.max(score, 0), 100);
    }

    private double roundOne(double value) {
        return Math.round(value * 10.0D) / 10.0D;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getScoreRows(Map<String, Object> student) {
        Object scores = student.get("scores");
        if (scores instanceof List) {
            return (List<Map<String, Object>>) scores;
        }
        return Collections.emptyList();
    }

    private void setOneDecimalCell(org.apache.poi.ss.usermodel.Row row, int colIdx, double value,
                                   org.apache.poi.ss.usermodel.CellStyle style) {
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(colIdx);
        cell.setCellValue(roundOne(value));
        cell.setCellStyle(style);
    }

    private int appendExportHeader(org.apache.poi.ss.usermodel.Row row, int colIdx, Set<String> selectedColumns,
                                   String key, String label, org.apache.poi.ss.usermodel.CellStyle style) {
        if (!selectedColumns.contains(key)) {
            return colIdx;
        }
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(colIdx++);
        cell.setCellValue(label);
        cell.setCellStyle(style);
        return colIdx;
    }

    private List<String> parseExportColumns(String columns) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (columns != null && !columns.trim().isEmpty()) {
            for (String key : columns.split(",")) {
                if (key != null && !key.trim().isEmpty()) {
                    result.add(key.trim());
                }
            }
        }
        if (result.isEmpty()) {
            Collections.addAll(result,
                    "userName", "className", "studentNo", "studentName",
                    "remark", "avgTyping", "overallTypingSpeed", "overallAccuracy", "overallCompletion",
                    "avgTheory", "avgPractical", "filteredTotal", "totalPerformance", "finalTotal",
                    "filteredAverage", "gradeLevel", "scaledScore");
        }
        return new ArrayList<>(result);
    }

    private String getExportSummaryHeader(String key, boolean singleLessonMode) {
        switch (key) {
            case "remark":
                return "备注";
            case "avgTyping":
                return "打字平均";
            case "overallTypingSpeed":
                return "打字速度";
            case "overallAccuracy":
                return "打字正确率";
            case "overallCompletion":
                return "打字完成率";
            case "avgTheory":
                return "理论平均";
            case "avgPractical":
                return "操作平均";
            case "filteredTotal":
                return singleLessonMode ? "作业分" : "作业平均";
            case "totalPerformance":
                return singleLessonMode ? "课堂表现分" : "课堂表现平均";
            case "finalTotal":
                return singleLessonMode ? "课程总分" : "课程平均分";
            case "filteredAverage":
                return "平均分";
            case "gradeLevel":
                return "等级";
            case "scaledScore":
                return "赋分";
            default:
                return null;
        }
    }

    private void setSummaryScoreCell(org.apache.poi.ss.usermodel.Row row, int colIdx, double value,
                                     boolean singleLessonMode, org.apache.poi.ss.usermodel.CellStyle style) {
        if (singleLessonMode) {
            row.createCell(colIdx).setCellValue(Math.round(value));
        } else {
            setOneDecimalCell(row, colIdx, value, style);
        }
    }

    private Map<Long, ExportRankInfo> buildExportRankInfo(List<Map<String, Object>> students, Set<Long> targetLessonIds) {
        Map<Long, ExportRankInfo> rankInfoMap = new HashMap<>();
        List<ExportRankInfo> validInfos = new ArrayList<>();

        for (Map<String, Object> student : students) {
            Object studentIdValue = student.get("studentId");
            if (!(studentIdValue instanceof Number)) {
                continue;
            }

            Long studentId = ((Number) studentIdValue).longValue();
            double finalTotal = 0D;
            int count = 0;
            for (Map<String, Object> score : getScoreRows(student)) {
                Object lessonIdValue = score.get("lessonId");
                if (!(lessonIdValue instanceof Number)) {
                    continue;
                }
                Long lessonId = ((Number) lessonIdValue).longValue();
                if (targetLessonIds != null && !targetLessonIds.isEmpty() && !targetLessonIds.contains(lessonId)) {
                    continue;
                }
                if (Boolean.TRUE.equals(score.get("isAbsent"))) {
                    continue;
                }
                finalTotal += numberToInt(score.get("finalScore"));
                count++;
            }

            ExportRankInfo info = new ExportRankInfo(studentId, count > 0 ? roundOne(finalTotal / count) : 0D);
            rankInfoMap.put(studentId, info);
            if (info.getRankScore() > 0) {
                validInfos.add(info);
            }
        }

        validInfos.sort((a, b) -> Double.compare(b.getRankScore(), a.getRankScore()));
        int totalCount = validInfos.size();
        if (totalCount == 0) {
            return rankInfoMap;
        }

        int excellentCount = (int) Math.ceil(totalCount * 25D / 100D);
        int goodCount = (int) Math.ceil(totalCount * 40D / 100D);
        int passCount = (int) Math.ceil(totalCount * 30D / 100D);
        for (int i = 0; i < validInfos.size(); i++) {
            ExportRankInfo info = validInfos.get(i);
            if (i < excellentCount) {
                info.setGradeLevel("优秀");
            } else if (i < excellentCount + goodCount) {
                info.setGradeLevel("良好");
            } else if (i < excellentCount + goodCount + passCount) {
                info.setGradeLevel("及格");
            } else {
                info.setGradeLevel("不及格");
            }
        }

        if (totalCount == 1) {
            validInfos.get(0).setScaledScore("100");
            return rankInfoMap;
        }

        int currentRank = 0;
        Double prevScore = null;
        for (int i = 0; i < validInfos.size(); i++) {
            ExportRankInfo info = validInfos.get(i);
            if (prevScore == null || Double.compare(info.getRankScore(), prevScore) != 0) {
                currentRank = i;
            }
            prevScore = info.getRankScore();
            int scaledScore = (int) Math.round(100D - (currentRank / (double) (totalCount - 1)) * 45D);
            info.setScaledScore(String.valueOf(scaledScore));
        }

        return rankInfoMap;
    }
    
    /**
     * 导出成绩 Excel
     */
    @GetMapping("/export")
    public void exportScoreExcel(
            @RequestParam String entryYear,
            @RequestParam(required = false) String classCode,
            @RequestParam(required = false) String lessonIds,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String columns,
            HttpServletResponse response) throws IOException {
        
        Long deptId = SecurityUtils.getDeptId();
        Long userId = SecurityUtils.getUserId();
        SysDept dept = deptMapper.selectDeptById(deptId);
        String schoolType = dept != null ? dept.getSchoolType() : "1";
        int gradeNum = calculateGrade(entryYear, schoolType);
        List<Long> selectedLessonIds = parseLessonIds(lessonIds, null);
        selectedLessonIds = validateSelectedLessonIds(entryYear, selectedLessonIds);
        List<String> selectedColumns = parseExportColumns(columns);
        Set<String> selectedColumnSet = new HashSet<>(selectedColumns);
        List<BizStudent> studentList = studentMapper.selectScoreStudents(userId, deptId, entryYear, classCode, keyword);
        List<Map<String, Object>> students = buildScoreSummaryRows(studentList, selectedLessonIds, deptId, gradeNum);
        
        // 3. 获取所有课程信息（用于表头）
        String creator = SecurityUtils.getUsername();
        List<LessonInfoVo> allLessons = lessonMapper.selectScoreLessons(
                entryYear, creator, userId, deptId);
        List<LessonInfoVo> targetLessons = new ArrayList<>();
        
        if (selectedLessonIds.isEmpty()) {
            targetLessons.addAll(allLessons);
        } else {
            for (LessonInfoVo lesson : allLessons) {
                Long id = lesson.getLessonId();
                if (selectedLessonIds.contains(id)) {
                    targetLessons.add(lesson);
                }
            }
        }
        
        // 按课次排序
        targetLessons.sort((a, b) -> {
            int numA = a.getLessonNum() == null ? 0 : a.getLessonNum();
            int numB = b.getLessonNum() == null ? 0 : b.getLessonNum();
            return numA - numB;
        });
        boolean singleLessonMode = selectedLessonIds.size() == 1;
        Set<Long> targetLessonIdSet = targetLessons.stream()
                .map(LessonInfoVo::getLessonId)
                .collect(java.util.stream.Collectors.toSet());
        Map<Long, ExportRankInfo> rankInfoMap = buildExportRankInfo(students, targetLessonIdSet);

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

        org.apache.poi.ss.usermodel.CellStyle oneDecimalStyle = wb.createCellStyle();
        oneDecimalStyle.setDataFormat(wb.createDataFormat().getFormat("0.0"));
        
        // 5. 表头
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        int colIdx = 0;
        colIdx = appendExportHeader(headerRow, colIdx, selectedColumnSet, "userName", "账号", headerStyle);
        colIdx = appendExportHeader(headerRow, colIdx, selectedColumnSet, "className", "班级", headerStyle);
        colIdx = appendExportHeader(headerRow, colIdx, selectedColumnSet, "studentNo", "学号", headerStyle);
        colIdx = appendExportHeader(headerRow, colIdx, selectedColumnSet, "studentName", "姓名", headerStyle);
        
        // 动态课程表头
        for (LessonInfoVo lesson : targetLessons) {
            String title = lesson.getLessonTitle();
            String[] lessonHeaders = {title + "-作业分", title + "-课堂表现分", title + "-课程总分"};
            for (String h : lessonHeaders) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(colIdx++);
                cell.setCellValue(h);
                cell.setCellStyle(headerStyle);
            }
        }
        
        // 统计表头
        for (String key : selectedColumns) {
            String label = getExportSummaryHeader(key, singleLessonMode);
            if (label != null) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(colIdx++);
                cell.setCellValue(label);
                cell.setCellStyle(headerStyle);
            }
        }
        
        // 6. 填充数据
        int rowIdx = 1;
        // 排序学生 (按学号)
        students.sort((a, b) -> {
            String noA = a.get("studentNo") == null ? "" : String.valueOf(a.get("studentNo"));
            String noB = b.get("studentNo") == null ? "" : String.valueOf(b.get("studentNo"));
            try {
                return Integer.parseInt(noA) - Integer.parseInt(noB);
            } catch (Exception e) {
                return noA.compareTo(noB);
            }
        });

        for (Map<String, Object> student : students) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
            colIdx = 0;
            
            Long studentId = ((Number) student.get("studentId")).longValue();

            if (selectedColumnSet.contains("userName")) {
                row.createCell(colIdx++).setCellValue((String) student.get("userName"));
            }
            
            String studentClassCode = (String) student.get("classCode");
            String classDisplay = "";
            if (studentClassCode != null) {
                try {
                    classDisplay = String.format("%d%02d", gradeNum, Integer.parseInt(studentClassCode));
                } catch (Exception e) {
                     classDisplay = gradeNum + studentClassCode;
                }
            }
            if (selectedColumnSet.contains("className")) {
                row.createCell(colIdx++).setCellValue(classDisplay);
            }
            
            if (selectedColumnSet.contains("studentNo")) {
                row.createCell(colIdx++).setCellValue((String) student.get("studentNo"));
            }
            if (selectedColumnSet.contains("studentName")) {
                row.createCell(colIdx++).setCellValue((String) student.get("studentName"));
            }
            
            // 课程成绩
            List<Map<String, Object>> scores = getScoreRows(student);
            double sumTyping = 0, sumTheory = 0, sumPractical = 0, sumTotal = 0;
            double sumPerformance = 0, sumFinal = 0;
            double typingSpeedSum = 0, accuracySum = 0, completionSum = 0;
            int typingCount = 0;
            int count = 0;
            
            for (LessonInfoVo lesson : targetLessons) {
                Long lessonId = lesson.getLessonId();
                Map<String, Object> targetScore = null;
                for (Map<String, Object> s : scores) {
                    if (((Number) s.get("lessonId")).longValue() == lessonId.longValue()) {
                        targetScore = s;
                        break;
                    }
                }
                
                if (targetScore == null) {
                    row.createCell(colIdx++).setCellValue("");
                    row.createCell(colIdx++).setCellValue("");
                    row.createCell(colIdx++).setCellValue("");
                } else if (Boolean.TRUE.equals(targetScore.get("isAbsent"))) {
                    row.createCell(colIdx++).setCellValue("请假");
                    row.createCell(colIdx++).setCellValue("请假");
                    row.createCell(colIdx++).setCellValue("请假");
                } else {
                    int scoreVal = numberToInt(targetScore.get("totalScore"));
                    int performanceScore = numberToInt(targetScore.get("performanceScore"));
                    int finalScore = numberToInt(targetScore.get("finalScore"));

                    sumTyping += numberToInt(targetScore.get("typingScore"));
                    sumTheory += numberToInt(targetScore.get("theoryScore"));
                    sumPractical += numberToInt(targetScore.get("practicalScore"));
                    sumTotal += scoreVal;
                    sumPerformance += performanceScore;
                    sumFinal += finalScore;
                    count++;

                    double typingSpeed = numberToDouble(targetScore.get("avgTypingSpeed"));
                    if (typingSpeed > 0) {
                        typingSpeedSum += typingSpeed;
                        accuracySum += numberToDouble(targetScore.get("avgAccuracyRate"));
                        completionSum += numberToDouble(targetScore.get("avgCompletionRate"));
                        typingCount++;
                    }

                    row.createCell(colIdx++).setCellValue(scoreVal);
                    row.createCell(colIdx++).setCellValue(performanceScore);
                    row.createCell(colIdx++).setCellValue(finalScore);
                }
            }
            
            // 统计列
            double avgTyping = count > 0 ? sumTyping / count : 0;
            double avgTheory = count > 0 ? sumTheory / count : 0;
            double avgPractical = count > 0 ? sumPractical / count : 0;
            double homeworkSummary = singleLessonMode ? sumTotal : (count > 0 ? sumTotal / count : 0);
            double performanceSummary = singleLessonMode ? sumPerformance : (count > 0 ? sumPerformance / count : 0);
            double finalSummary = singleLessonMode ? sumFinal : (count > 0 ? sumFinal / count : 0);
            double overallTypingSpeed = typingCount > 0 ? Math.round(typingSpeedSum / typingCount) : 0;
            double overallAccuracy = typingCount > 0 ? Math.round(accuracySum / typingCount) : 0;
            double overallCompletion = typingCount > 0 ? Math.round(completionSum / typingCount) : 0;
            ExportRankInfo rankInfo = rankInfoMap.get(studentId);

            for (String key : selectedColumns) {
                switch (key) {
                    case "remark":
                        row.createCell(colIdx++).setCellValue(student.get("remark") != null ? (String) student.get("remark") : "");
                        break;
                    case "avgTyping":
                        setOneDecimalCell(row, colIdx++, avgTyping, oneDecimalStyle);
                        break;
                    case "overallTypingSpeed":
                        row.createCell(colIdx++).setCellValue(typingCount > 0 ? overallTypingSpeed + " 字/分" : "");
                        break;
                    case "overallAccuracy":
                        row.createCell(colIdx++).setCellValue(typingCount > 0 ? overallAccuracy + "%" : "");
                        break;
                    case "overallCompletion":
                        row.createCell(colIdx++).setCellValue(typingCount > 0 ? overallCompletion + "%" : "");
                        break;
                    case "avgTheory":
                        setOneDecimalCell(row, colIdx++, avgTheory, oneDecimalStyle);
                        break;
                    case "avgPractical":
                        setOneDecimalCell(row, colIdx++, avgPractical, oneDecimalStyle);
                        break;
                    case "filteredTotal":
                        setSummaryScoreCell(row, colIdx++, homeworkSummary, singleLessonMode, oneDecimalStyle);
                        break;
                    case "totalPerformance":
                        setSummaryScoreCell(row, colIdx++, performanceSummary, singleLessonMode, oneDecimalStyle);
                        break;
                    case "finalTotal":
                    case "filteredAverage":
                        setSummaryScoreCell(row, colIdx++, finalSummary, singleLessonMode, oneDecimalStyle);
                        break;
                    case "gradeLevel":
                        row.createCell(colIdx++).setCellValue(rankInfo != null ? rankInfo.getGradeLevel() : "-");
                        break;
                    case "scaledScore":
                        row.createCell(colIdx++).setCellValue(rankInfo != null ? rankInfo.getScaledScore() : "-");
                        break;
                    default:
                        break;
                }
            }
        }
        
        // 输出
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String classScope = org.apache.commons.lang3.StringUtils.isBlank(classCode)
                ? "全部班级" : classCode.trim() + "班";
        FileUtils.setAttachmentResponseHeader(response, DownloadFileNameUtils.withTimestamp(
                "成绩汇总_" + entryYear.trim() + "级_" + classScope + ".xlsx"));
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
        if (org.apache.commons.lang3.StringUtils.isBlank(classCode)
                || org.apache.commons.lang3.StringUtils.isBlank(entryYear)) {
            throw new ServiceException("入学年份和班级编号必须同时提供");
        }
        guideSheetAccessService.assertCanViewLessonClass(lessonId, entryYear.trim(), classCode.trim());
        // 1. 查询课程的所有题目详情
        List<com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo> questions = lessonQuestionMapper.selectDetailsByLessonId(lessonId);
        
        Long deptId = SecurityUtils.getDeptId();
        
        // 2. 查询该课程的答题记录 (根据筛选条件)
        List<com.ruoyi.business.domain.BizStudentAnswer> allAnswers =
                studentAnswerMapper.selectByLessonAndClass(lessonId, classCode.trim(), entryYear.trim(), deptId);
        
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
        if (lessonId == null || org.apache.commons.lang3.StringUtils.isBlank(classCode)
                || org.apache.commons.lang3.StringUtils.isBlank(entryYear)) {
            throw new ServiceException("课程、入学年份和班级编号必须同时提供");
        }
        guideSheetAccessService.assertCanViewLessonClass(lessonId, entryYear.trim(), classCode.trim());
        // 获取学校类型并计算年级
        Long deptId = SecurityUtils.getDeptId();
        SysDept dept = deptMapper.selectDeptById(deptId);
        String schoolType = dept != null ? dept.getSchoolType() : "1";
        int gradeNum = calculateGrade(entryYear, schoolType);
        
        List<com.ruoyi.business.domain.vo.StudentAnswerMatrixVo> result = studentAnswerMapper.selectStudentAnswerMatrix(
                lessonId, classCode.trim(), entryYear.trim(), deptId);
        
        // 格式化 className 为 年级+班级号 (如 "601")
        for (com.ruoyi.business.domain.vo.StudentAnswerMatrixVo vo : result) {
            if (vo.getClassName() != null && !vo.getClassName().isEmpty()) {
                String code = String.format("%02d", Integer.parseInt(vo.getClassName()));
                vo.setClassName(gradeNum + code);
            }
        }
        
        return result;
    }

    public static class ManualHomeworkScoreRequest {
        private Long studentId;
        private Long lessonId;
        private Integer adjustedScore;
        private String reason;

        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(Long studentId) {
            this.studentId = studentId;
        }

        public Long getLessonId() {
            return lessonId;
        }

        public void setLessonId(Long lessonId) {
            this.lessonId = lessonId;
        }

        public Integer getAdjustedScore() {
            return adjustedScore;
        }

        public void setAdjustedScore(Integer adjustedScore) {
            this.adjustedScore = adjustedScore;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    private static class ExportRankInfo {
        private final Long studentId;
        private final double rankScore;
        private String gradeLevel = "-";
        private String scaledScore = "-";

        ExportRankInfo(Long studentId, double rankScore) {
            this.studentId = studentId;
            this.rankScore = rankScore;
        }

        public Long getStudentId() {
            return studentId;
        }

        public double getRankScore() {
            return rankScore;
        }

        public String getGradeLevel() {
            return gradeLevel;
        }

        public void setGradeLevel(String gradeLevel) {
            this.gradeLevel = gradeLevel;
        }

        public String getScaledScore() {
            return scaledScore;
        }

        public void setScaledScore(String scaledScore) {
            this.scaledScore = scaledScore;
        }
    }
}
