package com.ruoyi.business.controller;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.business.utils.FileConversionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 学生首页Controller - 显示当前被指派课程的题目
 * 
 * @author zdx
 * @date 2025-12-30
 */
@RestController
@RequestMapping("/business/student-home")
public class StudentHomeController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(StudentHomeController.class);

    @Autowired
    private BizStudentMapper bizStudentMapper;

    @Autowired
    private BizLessonAssignmentMapper lessonAssignmentMapper;

    @Autowired
    private BizLessonQuestionMapper lessonQuestionMapper;

    @Autowired
    private BizLessonMapper lessonMapper;

    @Autowired
    private SysDeptMapper deptMapper;

    /**
     * 获取学生当前课程信息
     * 根据学生的入学年份和班级，查询被指派的当前课程及其题目
     */
    @GetMapping("/current-lesson")
    public AjaxResult getCurrentLesson()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return AjaxResult.error("用户未登录");
        }

        Long userId = loginUser.getUserId();
        Long deptId = loginUser.getDeptId();
        log.info("【学生首页】用户ID: {} 请求当前课程", userId);

        // 1. 查询学生信息（入学年份、班级编号）
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(userId);
        if (student == null) {
            log.warn("【学生首页】用户 {} 不是学生", userId);
            return AjaxResult.error("您不是学生用户");
        }

        String entryYear = student.getEntryYear();
        String classCode = student.getClassCode();
        log.info("【学生首页】学生入学年份: {}, 班级: {}", entryYear, classCode);

        // 获取学校信息
        SysDept dept = deptMapper.selectDeptById(deptId);
        String deptName = dept != null ? dept.getDeptName() : "";
        String schoolType = dept != null ? dept.getSchoolType() : "1";

        // 计算年级名称
        String gradeName = calculateGradeName(entryYear, schoolType);

        // 构建学生信息
        Map<String, Object> studentInfo = new HashMap<>();
        studentInfo.put("studentName", student.getStudentName());
        studentInfo.put("entryYear", entryYear);
        studentInfo.put("classCode", classCode);
        studentInfo.put("deptName", deptName);
        studentInfo.put("gradeName", gradeName);

        // 2. 查询当前被指派的课程ID
        Long lessonId = lessonAssignmentMapper.selectCurrentLessonByClass(entryYear, classCode);
        if (lessonId == null) {
            log.info("【学生首页】学生 {} 当前没有被指派课程", userId);
            return AjaxResult.success()
                    .put("hasLesson", false)
                    .put("message", "暂无课程")
                    .put("studentInfo", studentInfo);
        }

        log.info("【学生首页】当前课程ID: {}", lessonId);

        // 3. 查询课程基本信息
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);

        // 4. 查询课程题目列表
        List<BizLessonQuestionDetailVo> questions = lessonQuestionMapper.selectDetailsByLessonId(lessonId);

        log.info("【学生首页】课程 {} 包含 {} 道题目", lessonId, questions.size());

        // 5. 查询学生已提交的答题记录
        List<BizStudentAnswer> submittedAnswers = studentAnswerMapper.selectByStudentAndLesson(student.getStudentId(), lessonId);
        // 转换为Map: { questionId: { answer, score } }
        java.util.Map<Long, java.util.Map<String, Object>> answersMap = new java.util.HashMap<>();
        for (BizStudentAnswer sa : submittedAnswers) {
            java.util.Map<String, Object> info = new java.util.HashMap<>();
            info.put("answer", sa.getStudentAnswer());
            info.put("score", sa.getScore());
            info.put("submitTime", sa.getSubmitTime());
            answersMap.put(sa.getQuestionId(), info);
        }

        return AjaxResult.success()
                .put("hasLesson", true)
                .put("lessonId", lessonId)
                .put("lessonTitle", lesson != null ? lesson.getLessonTitle() : "")
                .put("questions", questions)
                .put("submittedAnswers", answersMap)  // 新增：学生已提交的答案
                .put("studentInfo", studentInfo);
    }

    /**
     * 根据入学年份和学校类型计算年级名称
     */
    private String calculateGradeName(String entryYear, String schoolType) {
        if (entryYear == null) return "未知年级";
        
        java.util.Calendar now = java.util.Calendar.getInstance();
        int currentYear = now.get(java.util.Calendar.YEAR);
        int currentMonth = now.get(java.util.Calendar.MONTH) + 1;
        
        int academicStartYear = currentYear;
        if (currentMonth < 7) {
            academicStartYear = currentYear - 1;
        }
        
        int yearsInSchool = academicStartYear - Integer.parseInt(entryYear) + 1;
        
        String[] gradeNames;
        if ("1".equals(schoolType)) { // 小学
            gradeNames = new String[]{"一年级", "二年级", "三年级", "四年级", "五年级", "六年级"};
        } else if ("2".equals(schoolType)) { // 初中
            gradeNames = new String[]{"七年级", "八年级", "九年级"};
        } else { // 高中
            gradeNames = new String[]{"高一", "高二", "高三"};
        }
        
        if (yearsInSchool >= 1 && yearsInSchool <= gradeNames.length) {
            return gradeNames[yearsInSchool - 1];
        }
        return "未知年级";
    }

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    @Autowired
    private BizQuestionMapper questionMapper;

    /**
     * 提交学生答案
     */
    @PostMapping("/submit-answers")
    public AjaxResult submitAnswers(@RequestBody SubmitAnswerRequest request)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return AjaxResult.error("用户未登录");
        }

        Long userId = loginUser.getUserId();
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(userId);
        if (student == null) {
            return AjaxResult.error("您不是学生用户");
        }

        Long lessonId = request.getLessonId();
        Map<Long, String> answers = request.getAnswers();
        Long studentId = student.getStudentId();
        Long deptId = loginUser.getDeptId();
        if (lessonId == null || answers == null || answers.isEmpty()) {
            return AjaxResult.error("参数错误");
        }

        log.info("【学生答题】学生 {} 开始提交课程 {} 的答案，共 {} 道题", studentId, lessonId, answers.size());

        // 获取学校信息，计算学生年级
        SysDept dept = deptMapper.selectDeptById(deptId);
        String schoolType = dept != null ? dept.getSchoolType() : "1";
        String gradeName = calculateGradeName(student.getEntryYear(), schoolType);
        
        // 根据年级确定打字基准速度
        int baseSpeed = determineBaseSpeed(gradeName);
        log.info("【学生答题】学生年级: {}, 打字基准速度: {} 字/分", gradeName, baseSpeed);

        // 删除旧答案（只删除本次提交的题目ID）
        List<Long> questionIds = new java.util.ArrayList<>(answers.keySet());
        if (!questionIds.isEmpty()) {
            studentAnswerMapper.deleteByStudentLessonAndQuestions(student.getStudentId(), lessonId, questionIds);
        }

        // 获取题目列表用于判断正确答案
        List<BizLessonQuestionDetailVo> questions = lessonQuestionMapper.selectDetailsByLessonId(lessonId);
        Map<Long, BizLessonQuestionDetailVo> questionMap = questions.stream()
                .collect(java.util.stream.Collectors.toMap(BizLessonQuestionDetailVo::getQuestionId, q -> q));

        List<BizStudentAnswer> answerList = new java.util.ArrayList<>();
        Date now = new Date();
        int totalScore = 0;

        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            Long questionId = entry.getKey();
            String studentAnswer = entry.getValue();
            
            BizLessonQuestionDetailVo question = questionMap.get(questionId);
            if (question == null) continue;

            BizStudentAnswer answer = new BizStudentAnswer();
            answer.setStudentId(student.getStudentId());
            answer.setLessonId(lessonId);
            answer.setQuestionId(questionId);
            answer.setStudentAnswer(studentAnswer);
            answer.setSubmitTime(now);
            answer.setAnswerTime(0); // TODO: 前端传递答题时间

            int score = 0;
            boolean isCorrect = false;

            if ("choice".equals(question.getQuestionType())) {
                // 选择题：直接比较（忽略大小写）
                isCorrect = studentAnswer != null && studentAnswer.equalsIgnoreCase(question.getAnswer());
                if (isCorrect && question.getQuestionScore() != null) {
                    score = question.getQuestionScore().intValue();
                }
            } else if ("judgment".equals(question.getQuestionType())) {
                // 判断题：将中文答案转换为T/F后比较
                String normalizedAnswer = normalizeJudgmentAnswer(studentAnswer);
                isCorrect = normalizedAnswer != null && normalizedAnswer.equalsIgnoreCase(question.getAnswer());
                if (isCorrect && question.getQuestionScore() != null) {
                    score = question.getQuestionScore().intValue();
                }
            } else if ("typing".equals(question.getQuestionType())) {
                String original = question.getQuestionContent();
                if (original != null && !original.isEmpty() && studentAnswer != null) {
                    int completedCount = studentAnswer.length(); // 完成字数
                    int originalLength = original.length(); // 原文字数
                    int correctCount = 0;
                    
                    // 逐字比对
                    int compLen = Math.min(completedCount, originalLength);
                    for (int i = 0; i < compLen; i++) {
                        if (original.charAt(i) == studentAnswer.charAt(i)) {
                            correctCount++;
                        }
                    }
                    
                    // 计算完成率和正确率
                    double completionRate = (double) correctCount / originalLength;  // 完成率 = 正确字数/原文字数（与前端progress一致）
                    double accuracyRate = completedCount > 0 ? (double) correctCount / completedCount : 0;  // 正确率 = 正确字数/完成字数
                    
                    Integer duration = question.getTypingDuration();

                    // 获取实际答题用时（秒），如果前端未传则默认使用1秒防止除以零，或者使用题目设定时长作为兜底
                    Integer timeSpent = request.getAnswerTimes() != null ? request.getAnswerTimes().get(questionId) : null;
                    if (timeSpent == null || timeSpent <= 0) {
                        // 兜底：如果没有传时间，可能是在后端逻辑修改前的前端提交，暂且用设定时长
                         timeSpent = duration != null ? duration : 60;
                    }
                    
                    // 记录答题时间到数据库
                    answer.setAnswerTime(timeSpent);
                    
                    // 应用新公式计算得分
                    if (question.getQuestionScore() != null) {
                        // 将实际用时转换为分钟
                        double durationInMinutes = (double) timeSpent / 60.0;
                        if (durationInMinutes <= 0) durationInMinutes = 0.016; // 防止除以0（1秒）
                        
                        // 实际速度 = 正确字数 / 用时（分钟）
                        double actualSpeed = (double) correctCount / durationInMinutes;
                        
                        // 速度系数 = 实际速度 / 基准速度（不封顶，支持超速加分）
                        double speedFactor = actualSpeed / baseSpeed;
                        
                        // 得分 = 满分 × 速度系数 × 正确率 × 完成率
                        double rawScore = question.getQuestionScore() 
                                        * speedFactor 
                                        * accuracyRate 
                                        * completionRate;
                        
                        score = (int) Math.round(rawScore);
                        
                        // 打印调试日志
                        log.info("=== 打字评分调试 Start ===");
                        log.info("题目ID: {}, 满分: {}, 实际用时: {}秒 ({}分), 基准速度: {}字/分", 
                                question.getQuestionId(), question.getQuestionScore(), timeSpent, String.format("%.2f", durationInMinutes), baseSpeed);
                        log.info("数据统计 - 原文: {}, 完成: {}, 正确: {}", 
                                originalLength, completedCount, correctCount);
                        log.info("比率计算 - 完成率: {}, 正确率: {}", completionRate, accuracyRate);
                        log.info("速度计算 - 实际速度(正确/实际用时): {}, 速度系数: {}", actualSpeed, speedFactor);
                        log.info("得分计算 - 原始得分: {}, 取整后: {}, 最终得分(封顶): {}", 
                                rawScore, score, Math.min(score, question.getQuestionScore().intValue()));
                        log.info("=== 打字评分调试 End ===");
                        
                        // 最终得分封顶在满分
                        score = Math.min(score, question.getQuestionScore().intValue());
                    }
                    
                    // isCorrect 标记（60%及格线）
                    isCorrect = question.getQuestionScore() != null 
                             && score >= question.getQuestionScore() * 0.6;
                }
            } else if ("practical".equals(question.getQuestionType())) {
                // 操作题：保存文件路径，如果是docx则转换为PDF供预览
                if (studentAnswer != null && !studentAnswer.trim().isEmpty()) {
                    // 检查是否为docx文件，如果是则进行PDF转换
                    if (studentAnswer.toLowerCase().endsWith(".docx") || studentAnswer.toLowerCase().endsWith(".doc")) {
                        try {
                            // 构建文件绝对路径
                            String fileSystemRelativePath = studentAnswer.replaceFirst(com.ruoyi.common.constant.Constants.RESOURCE_PREFIX, "");
                            String docxFullPath = RuoYiConfig.getProfile() + fileSystemRelativePath;
                            java.io.File docxFile = new java.io.File(docxFullPath);
                            
                            if (docxFile.exists() && FileConversionUtils.isLibreOfficeInstalled()) {
                                String outputDir = docxFile.getParent();
                                String pdfFullPath = FileConversionUtils.convertDocxToPdfWithLibreOffice(docxFullPath, outputDir);
                                if (pdfFullPath != null) {
                                    java.io.File pdfFile = new java.io.File(pdfFullPath);
                                    if (pdfFile.exists()) {
                                        log.info("【操作题】PDF转换成功 | 文件大小: {} 字节 | 绝对路径: {}", pdfFile.length(), pdfFile.getAbsolutePath());
                                    } else {
                                        log.error("【操作题】转换命令执行完成但文件不存在: {}", pdfFullPath);
                                    }
                                }
                            } else {
                                log.warn("【操作题】无法转换: 源文件不存在({}) 或 LibreOffice未安装", docxFullPath);
                            }
                        } catch (Exception e) {
                            log.error("【操作题】学生作品PDF转换异常", e);
                        }
                    }
                    // 操作题不自动评分，score保持null由教师批改
                    answer.setScore(null);
                }
            }
            
            answer.setIsCorrect(isCorrect);
            answer.setScore(score);
            totalScore += answer.getScore();
            
            answerList.add(answer);
        }

        if (!answerList.isEmpty()) {
            studentAnswerMapper.batchInsert(answerList);
        }

        log.info("【学生答题】学生 {} 提交课程 {} 答案，得分: {}", student.getStudentId(), lessonId, totalScore);

        return AjaxResult.success("提交成功").put("totalScore", totalScore);
    }

    /**
     * 根据年级名称判断打字基准速度
     * @param gradeName 年级名称（如"一年级"、"七年级"、"高一"）
     * @return 基准速度（小学20字/分，初高中40字/分）
     */
    private int determineBaseSpeed(String gradeName) {
        if (gradeName == null) {
            return 40; // 默认初高中标准
        }
        
        // 小学：一年级~六年级
        if (gradeName.matches(".*[一二三四五六]年级.*") || 
            gradeName.matches(".*[1-6]年级.*")) {
            return 20;
        }
        
        // 初中、高中（七年级及以上）
        return 40;
    }

    /**
     * 将判断题的中文答案转换为 T/F 格式
     * 支持: 对/正确/T/true -> T,  错/错误/F/false -> F
     */
    private String normalizeJudgmentAnswer(String answer) {
        if (answer == null) return null;
        String trimmed = answer.trim();
        if ("对".equals(trimmed) || "正确".equals(trimmed) || "T".equalsIgnoreCase(trimmed) || "true".equalsIgnoreCase(trimmed)) {
            return "T";
        }
        if ("错".equals(trimmed) || "错误".equals(trimmed) || "F".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
            return "F";
        }
        return trimmed; // 其他情况返回原值
    }

    /**
     * 获取学生历史成绩单
     * 默认返回今年的所有课程成绩
     */
    @GetMapping("/history-scores")
    public AjaxResult getHistoryScores(@RequestParam(required = false) Integer year)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return AjaxResult.error("用户未登录");
        }

        Long userId = loginUser.getUserId();
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(userId);
        if (student == null) {
            return AjaxResult.error("未找到学生信息");
        }

        // 默认今年
        if (year == null) {
            year = java.time.Year.now().getValue();
        }

        log.info("【历史成绩】学生 {} 查询 {} 年成绩", student.getStudentId(), year);

        // 查询该学生所有已提交的课程成绩
        List<Map<String, Object>> historyList = new java.util.ArrayList<>();
        
        // 查询学生有答题记录的所有课程ID
        List<Long> lessonIds = studentAnswerMapper.selectDistinctLessonIdsByStudent(student.getStudentId(), year);
        
        for (Long lessonId : lessonIds) {
            BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
            if (lesson == null) continue;
            
            // 查询该课程的总分
            List<BizLessonQuestionDetailVo> questions = lessonQuestionMapper.selectDetailsByLessonId(lessonId);
            int totalScore = questions.stream().mapToInt(q -> q.getQuestionScore() != null ? q.getQuestionScore().intValue() : 0).sum();
            
            // 查询学生在该课程的得分
            List<BizStudentAnswer> answers = studentAnswerMapper.selectByStudentAndLesson(student.getStudentId(), lessonId);
            int myScore = answers.stream().mapToInt(a -> a.getScore() != null ? a.getScore() : 0).sum();
            
            // 获取最后提交时间
            Date lastSubmitTime = answers.stream()
                .map(BizStudentAnswer::getSubmitTime)
                .filter(t -> t != null)
                .max(Date::compareTo)
                .orElse(null);
            
            // 统计各题型得分
            int typingScore = 0, theoryScore = 0, practicalScore = 0;
            int typingTotal = 0, theoryTotal = 0, practicalTotal = 0;
            
            Map<Long, BizLessonQuestionDetailVo> questionMap = questions.stream()
                .collect(java.util.stream.Collectors.toMap(BizLessonQuestionDetailVo::getQuestionId, q -> q));
            
            for (BizStudentAnswer a : answers) {
                BizLessonQuestionDetailVo q = questionMap.get(a.getQuestionId());
                if (q == null) continue;
                int score = a.getScore() != null ? a.getScore() : 0;
                int qScore = q.getQuestionScore() != null ? q.getQuestionScore().intValue() : 0;
                
                if ("typing".equals(q.getQuestionType())) {
                    typingScore += score;
                    typingTotal += qScore;
                } else if ("choice".equals(q.getQuestionType()) || "judgment".equals(q.getQuestionType())) {
                    theoryScore += score;
                    theoryTotal += qScore;
                } else if ("practical".equals(q.getQuestionType())) {
                    practicalScore += score;
                    practicalTotal += qScore;
                }
            }
            
            Map<String, Object> record = new java.util.HashMap<>();
            record.put("lessonId", lessonId);
            record.put("lessonTitle", lesson.getLessonTitle());
            record.put("totalScore", totalScore);
            record.put("myScore", myScore);
            record.put("submitTime", lastSubmitTime);
            record.put("typingScore", typingScore + "/" + typingTotal);
            record.put("theoryScore", theoryScore + "/" + theoryTotal);
            record.put("practicalScore", practicalScore + "/" + practicalTotal);
            
            historyList.add(record);
        }
        
        // 按提交时间倒序
        historyList.sort((a, b) -> {
            Date t1 = (Date) a.get("submitTime");
            Date t2 = (Date) b.get("submitTime");
            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t2.compareTo(t1);
        });

        return AjaxResult.success(historyList);
    }

    /**
     * 获取错题列表
     */
    @GetMapping("/wrong-questions")
    public AjaxResult getWrongQuestions(@RequestParam(required = false) Long lessonId) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return AjaxResult.error("用户未登录");
        }
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (student == null) {
            return AjaxResult.error("未找到学生信息");
        }
        
        List<BizLessonQuestionDetailVo> list = studentAnswerMapper.selectWrongQuestions(student.getStudentId(), lessonId);
        return AjaxResult.success(list);
    }

    /**
     * 提交答案请求体
     */
    public static class SubmitAnswerRequest {
        private Long lessonId;
        private Map<Long, String> answers;
        private Map<Long, Integer> answerTimes; // 新增：题目ID -> 答题用时(秒)

        public Long getLessonId() { return lessonId; }
        public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

        public Map<Long, String> getAnswers() { return answers; }
        public void setAnswers(Map<Long, String> answers) { this.answers = answers; }
        
        public Map<Long, Integer> getAnswerTimes() { return answerTimes; }
        public void setAnswerTimes(Map<Long, Integer> answerTimes) { this.answerTimes = answerTimes; }
    }
}
