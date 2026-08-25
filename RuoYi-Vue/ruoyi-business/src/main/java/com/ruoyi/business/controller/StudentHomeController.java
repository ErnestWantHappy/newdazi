package com.ruoyi.business.controller;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DeadlockLoserDataAccessException;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.StudentLessonQuestionVo;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizLessonCheckin;
import com.ruoyi.business.domain.BizLessonGuideSheetBinding;
import com.ruoyi.business.mapper.BizLessonCheckinMapper;
import com.ruoyi.business.service.ICountyExamService;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.business.service.GuideSheetStudentViewService;
import com.ruoyi.business.service.PracticalGradingDeadlineService;
import com.ruoyi.business.service.StudentAnswerSubmissionService;
import com.ruoyi.business.service.PracticalArtifactService;
import com.ruoyi.business.service.StudentToolService;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.domain.dto.PracticalArtifactSubmitRequest;
import com.ruoyi.business.domain.dto.PracticalArtifactDeleteRequest;
import com.ruoyi.business.domain.dto.PracticalUploadTicket;
import com.ruoyi.business.domain.vo.PracticalArtifactVo;
import com.ruoyi.business.domain.ProgrammingQuestionConfig;
import com.ruoyi.business.mapper.ProgrammingJudgeMapper;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.common.core.domain.entity.SysDept;
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
@PreAuthorize("@studentSs.isStudent()")
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

    @Autowired
    private com.ruoyi.business.service.AsyncConversionService asyncConversionService;

    @Autowired
    private ICountyExamService countyExamService;

    @Autowired
    private GuideSheetAccessService guideSheetAccessService;

    @Autowired
    private GuideSheetStudentViewService studentViewService;

    @Autowired
    private PracticalGradingDeadlineService practicalGradingDeadlineService;

    @Autowired
    private StudentAnswerSubmissionService studentAnswerSubmissionService;

    @Autowired
    private PracticalArtifactService practicalArtifactService;

    @Autowired
    private BizLessonCheckinMapper lessonCheckinMapper;

    @Autowired
    private StudentToolService studentToolService;

    @Value("${student.submission-grace-minutes:15}")
    private long submissionGraceMinutes;

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
        log.debug("【学生首页】用户ID: {} 请求当前课程", userId);

        // 1. 查询学生信息（入学年份、班级编号）
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(userId);
        if (student == null) {
            log.warn("【学生首页】用户 {} 不是学生", userId);
            return AjaxResult.error("您不是学生用户");
        }
        if (hasPendingCountyExam()) {
            return AjaxResult.success()
                    .put("hasLesson", false)
                    .put("blockedByCountyExam", true)
                    .put("message", "请先完成区域抽测");
        }

        String entryYear = student.getEntryYear();
        String classCode = student.getClassCode();
        log.debug("【学生首页】学生入学年份: {}, 班级: {}", entryYear, classCode);

        // 获取学校信息
        SysDept dept = deptMapper.selectDeptById(deptId);
        String deptName = dept != null ? dept.getDeptName() : "";
        String schoolType = dept != null ? dept.getSchoolType() : "1";

        // 计算年级名称
        String gradeName = calculateGradeName(entryYear, schoolType);

        // 构建学生信息
        Map<String, Object> studentInfo = new HashMap<>();
        studentInfo.put("studentId", student.getStudentId());  // 用于随机种子
        studentInfo.put("studentName", student.getStudentName());
        studentInfo.put("entryYear", entryYear);
        studentInfo.put("classCode", classCode);
        studentInfo.put("deptName", deptName);
        studentInfo.put("gradeName", gradeName);

        // 2. 查询当前被指派的课程ID
        Long lessonId = lessonAssignmentMapper.selectCurrentLessonByClass(entryYear, classCode, deptId);
        if (lessonId == null) {
            log.debug("【学生首页】学生 {} 当前没有被指派课程", userId);
            return AjaxResult.success()
                    .put("hasLesson", false)
                    .put("message", "暂无课程")
                    .put("studentInfo", studentInfo);
        }

        log.debug("【学生首页】当前课程ID: {}", lessonId);

        // 3. 查询课程基本信息
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        BizLessonGuideSheetBinding guideSheetBinding = guideSheetAccessService.requireCurrentStudentBinding(student);

        // 4. 查询课程题目列表
        List<StudentLessonQuestionVo> questions = studentViewService.toStudentLessonQuestions(
                lessonQuestionMapper.selectDetailsByLessonId(lessonId));
        for (StudentLessonQuestionVo question : questions)
        {
            if ("practical".equalsIgnoreCase(question.getQuestionType()))
            {
                // 旧数据或旧缓存可能没有返回作答方式；启用 Python 配置时必须明确告诉学生端使用编辑器。
                if (question.getPracticalMode() == null || question.getPracticalMode().trim().isEmpty())
                {
                    ProgrammingQuestionConfig programmingConfig = programmingJudgeMapper.selectConfig(question.getQuestionId());
                    if (programmingConfig != null && "1".equals(programmingConfig.getEnabled()))
                    {
                        question.setPracticalMode("PYTHON");
                    }
                }
                question.setPracticalMaterials(
                        practicalArtifactService.getStudentMaterials(question.getQuestionId()));
            }
        }

        log.debug("【学生首页】课程 {} 包含 {} 道题目", lessonId, questions.size());
        
        // 5. 查询学生已提交的答题记录
        List<BizStudentAnswer> submittedAnswers = studentAnswerMapper.selectByStudentAndLesson(student.getStudentId(), lessonId);
        // 转换为Map: { questionId: { answer, score } }
        java.util.Map<Long, java.util.Map<String, Object>> answersMap = new java.util.HashMap<>();
        for (BizStudentAnswer sa : submittedAnswers) {
            java.util.Map<String, Object> info = new java.util.HashMap<>();
            info.put("answer", sa.getStudentAnswer());
            info.put("score", sa.getScore());
            info.put("submitTime", sa.getSubmitTime());
            info.put("previewStatus", sa.getPreviewStatus());
            info.put("previewPath", sa.getPreviewPath());
            info.put("practicalVersionId", sa.getPracticalVersionId());
            if (sa.getPracticalVersionId() != null) {
                info.put("artifact", practicalArtifactService.getCurrentLessonArtifact(
                        student.getStudentId(), lessonId, sa.getQuestionId()));
            }
            answersMap.put(sa.getQuestionId(), info);
        }

        String lessonMode = lesson != null && "attendance".equalsIgnoreCase(lesson.getLessonMode())
                ? "attendance" : "assessment";
        BizLessonCheckin checkin = lessonCheckinMapper.selectByLessonAndStudent(lessonId, student.getStudentId());
        boolean checkedIn = checkin != null;

        // 学生实验工具：本节课工具 + 按 学校+年级+班级 匹配的常驻工具
        java.util.Map<String, Object> studentTools = studentToolService.getToolsForStudent(
                deptId, entryYear, classCode, lessonId);

        // 题目开放开关：当前指派行的 班级 x 课程 双开关；推进课程会自动复位
        BizLessonAssignment assignmentQuery = new BizLessonAssignment();
        assignmentQuery.setEntryYear(student.getEntryYear());
        assignmentQuery.setClassCode(student.getClassCode());
        assignmentQuery.setDeptId(deptId);
        java.util.List<BizLessonAssignment> assignmentRows = lessonAssignmentMapper.selectBizLessonAssignmentList(assignmentQuery);
        BizLessonAssignment currentAssignment = assignmentRows == null || assignmentRows.isEmpty() ? null : assignmentRows.get(0);
        boolean theoryOpen = currentAssignment != null && Integer.valueOf(1).equals(currentAssignment.getTheoryOpen());
        boolean practicalOpen = currentAssignment != null && Integer.valueOf(1).equals(currentAssignment.getPracticalOpen());

        // 该课程是否含对应题型（未开放时前端给提示，而不是把区域藏到找不到）
        boolean hasTheory = false;
        boolean hasPractical = false;
        for (StudentLessonQuestionVo q : questions)
        {
            if ("choice".equalsIgnoreCase(q.getQuestionType()) || "judgment".equalsIgnoreCase(q.getQuestionType()))
            {
                hasTheory = true;
            }
            else if ("practical".equalsIgnoreCase(q.getQuestionType()))
            {
                hasPractical = true;
            }
        }

        return AjaxResult.success()
                .put("hasLesson", true)
                .put("lessonId", lessonId)
                .put("lessonTitle", lesson != null ? lesson.getLessonTitle() : "")
                .put("lessonMode", lessonMode)
                // 课程级物联网开关：学生首页据此决定是否显示「物联实验」入口。
                .put("iotEnabled", lesson != null && Boolean.TRUE.equals(lesson.getIotEnabled()))
                .put("teacherNote", lesson != null ? lesson.getTeacherNote() : null)
                .put("checkedIn", checkedIn)
                .put("checkinTime", checkin == null ? null : checkin.getCheckinTime())
                // 考勤课永不触发自动推进；前端/后续任务可据此跳过
                .put("autoAdvanceDisabled", "attendance".equals(lessonMode))
                .put("shuffleMode", lesson != null ? lesson.getShuffleMode() : 0)
                .put("randomChoiceCount", lesson != null ? lesson.getRandomChoiceCount() : 0)
                .put("randomJudgmentCount", lesson != null ? lesson.getRandomJudgmentCount() : 0)
                .put("guideSheetEnabled", guideSheetBinding != null)
                .put("guideSheetBindingId", guideSheetBinding == null ? null : guideSheetBinding.getBindingId())
                .put("guideSheetTitle", guideSheetBinding == null ? null : guideSheetBinding.getSnapshotTitle())
                .put("questions", questions)
                .put("submittedAnswers", answersMap)  // 新增：学生已提交的答案
                .put("studentInfo", studentInfo)
                .put("studentTools", studentTools)
                .put("theoryOpen", theoryOpen)
                .put("practicalOpen", practicalOpen)
                .put("hasTheory", hasTheory)
                .put("hasPractical", hasPractical);

    }

    /**
     * 学生课堂签到（考勤课主路径；测评课也可点签到但不计作业分）
     */
    @PostMapping("/checkin")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult checkin(@RequestBody Map<String, Object> body)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return AjaxResult.error("用户未登录");
        }
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (student == null) {
            return AjaxResult.error("您不是学生用户");
        }
        if (hasPendingCountyExam()) {
            return AjaxResult.error("请先完成区域抽测");
        }
        Long lessonId = null;
        if (body != null && body.get("lessonId") != null) {
            lessonId = Long.valueOf(String.valueOf(body.get("lessonId")));
        }
        if (lessonId == null) {
            return AjaxResult.error("缺少课程ID");
        }

        Long deptId = loginUser.getDeptId();
        Long currentLessonId = lessonAssignmentMapper.selectCurrentLessonByClass(
                student.getEntryYear(), student.getClassCode(), deptId);
        if (!lessonId.equals(currentLessonId)) {
            return AjaxResult.error("只能签到当前指派课程");
        }
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        if (lesson == null || lesson.getDeptId() == null || !lesson.getDeptId().equals(deptId)) {
            return AjaxResult.error("课程不存在或无权签到");
        }

        BizLessonCheckin existing = lessonCheckinMapper.selectByLessonAndStudent(lessonId, student.getStudentId());
        if (existing != null) {
            return AjaxResult.success("已签到")
                    .put("checkedIn", true)
                    .put("checkinTime", existing.getCheckinTime())
                    .put("lessonMode", "attendance".equalsIgnoreCase(lesson.getLessonMode()) ? "attendance" : "assessment");
        }

        Date now = new Date();
        BizLessonCheckin row = new BizLessonCheckin();
        row.setLessonId(lessonId);
        row.setStudentId(student.getStudentId());
        row.setDeptId(deptId);
        row.setCheckinTime(now);
        row.setCreateBy(loginUser.getUsername());
        row.setCreateTime(now);
        lessonCheckinMapper.insertIgnore(row);

        BizLessonCheckin saved = lessonCheckinMapper.selectByLessonAndStudent(lessonId, student.getStudentId());
        return AjaxResult.success("签到成功")
                .put("checkedIn", true)
                .put("checkinTime", saved != null ? saved.getCheckinTime() : now)
                .put("lessonMode", "attendance".equalsIgnoreCase(lesson.getLessonMode()) ? "attendance" : "assessment");
    }

    /**
     * 根据入学年份和学校类型计算年级名称
     */
    private String calculateGradeName(String entryYear, String schoolType) {
        return com.ruoyi.business.util.AcademicYearUtils.resolveGradeName(
                entryYear, schoolType, java.time.LocalDate.now());
    }

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    @Autowired
    private BizQuestionMapper questionMapper;

    @Autowired
    private ProgrammingJudgeMapper programmingJudgeMapper;

    @Autowired
    private RedisCache redisCache;

    private static final String[] PRACTICAL_ALLOWED_EXTENSIONS = {
            "doc", "docx", "pdf", "ppt", "pptx", "xls", "xlsx"
    };

    /**
     * 学生操作题专用上传：上传前同时校验课程归属、补交窗口和题目类型。
     */
    @PreAuthorize("@studentSs.isStudent()")
    @PostMapping("/practical-upload")
    public AjaxResult uploadPracticalWork(@RequestParam("lessonId") Long lessonId,
                                          @RequestParam("questionId") Long questionId,
                                          @RequestParam("file") MultipartFile file) throws Exception
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("上传文件不能为空，请重新选择文件");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (student == null)
        {
            throw new ServiceException("未找到学生信息");
        }
        if (hasPendingCountyExam())
        {
            throw new ServiceException("请先完成区域抽测");
        }
        String accessError = validateSubmissionAccess(student, loginUser.getDeptId(), lessonId);
        if (accessError != null)
        {
            throw new ServiceException(accessError);
        }
        BizLessonQuestionDetailVo question = lessonQuestionMapper.selectDetailsByLessonId(lessonId).stream()
                .filter(item -> questionId != null && questionId.equals(item.getQuestionId()))
                .findFirst()
                .orElse(null);
        if (question == null || !"practical".equalsIgnoreCase(question.getQuestionType()))
        {
            throw new ServiceException("题目不存在或不是操作题");
        }

        PracticalUploadTicket ticket = practicalArtifactService.stageStudentFile(
                student.getStudentId(), lessonId, question, file);
        return AjaxResult.success()
                .put("fileName", ticket.getResourcePath())
                .put("newFileName", ticket.getOriginalFileName())
                .put("uploadToken", ticket.getToken())
                .put("fileKind", ticket.getFileKind())
                .put("fileExtension", ticket.getFileExtension())
                .put("fileSize", ticket.getFileSize());
    }

    /**
     * 将一个Office/PDF文件或一组有序图片提交为新的不可变作品版本。
     */
    @PreAuthorize("@studentSs.isStudent()")
    @PostMapping("/practical-artifact/submit")
    public AjaxResult submitPracticalArtifact(@RequestBody PracticalArtifactSubmitRequest request)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (student == null)
        {
            throw new ServiceException("未找到学生信息");
        }
        if (hasPendingCountyExam())
        {
            throw new ServiceException("请先完成区域抽测");
        }
        String accessError = validateSubmissionAccess(student, loginUser.getDeptId(), request.getLessonId());
        if (accessError != null)
        {
            throw new ServiceException(accessError);
        }
        BizLessonQuestionDetailVo question = findPracticalQuestion(
                request.getLessonId(), request.getQuestionId());
        PracticalArtifactVo result = practicalArtifactService.submitLessonArtifact(
                student.getStudentId(), loginUser.getUserId(), question,
                request.getLessonId(), request.getExpectedVersionId(), request.getUploadTokens());
        triggerPracticalDeadlineCheck(
                request.getLessonId(), loginUser.getDeptId(), student.getEntryYear(), student.getClassCode());
        return AjaxResult.success("作品提交成功", result);
    }

    /**
     * 删除当前作品只会取消当前版本，历史版本和旧成绩快照继续保留。
     */
    @PreAuthorize("@studentSs.isStudent()")
    @PostMapping("/practical-artifact/delete")
    public AjaxResult deletePracticalArtifact(@RequestBody PracticalArtifactDeleteRequest request)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (student == null)
        {
            throw new ServiceException("未找到学生信息");
        }
        String accessError = validateSubmissionAccess(student, loginUser.getDeptId(), request.getLessonId());
        if (accessError != null)
        {
            throw new ServiceException(accessError);
        }
        findPracticalQuestion(request.getLessonId(), request.getQuestionId());
        practicalArtifactService.deleteCurrentLessonArtifact(
                student.getStudentId(), request.getLessonId(), request.getQuestionId(),
                request.getExpectedVersionId());
        return AjaxResult.success("当前作品已删除，历史版本仍保留");
    }

    private BizLessonQuestionDetailVo findPracticalQuestion(Long lessonId, Long questionId)
    {
        BizLessonQuestionDetailVo question = lessonQuestionMapper.selectDetailsByLessonId(lessonId).stream()
                .filter(item -> questionId != null && questionId.equals(item.getQuestionId()))
                .findFirst()
                .orElse(null);
        if (question == null || !"practical".equalsIgnoreCase(question.getQuestionType()))
        {
            throw new ServiceException("题目不存在或不是操作题");
        }
        return question;
    }

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
        if (hasPendingCountyExam()) {
            return AjaxResult.error("请先完成区域抽测");
        }

        Long lessonId = request.getLessonId();
        Map<Long, String> answers = request.getAnswers();
        Long studentId = student.getStudentId();
        Long deptId = loginUser.getDeptId();
        if (lessonId == null || answers == null || answers.isEmpty()) {
            return AjaxResult.error("参数错误");
        }

        String accessError = validateSubmissionAccess(student, deptId, lessonId);
        if (accessError != null) {
            return AjaxResult.error(accessError);
        }
        BizLesson requestedLesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        // 纯考勤课无题可答；若已升级加题则允许提交
        if ("attendance".equalsIgnoreCase(requestedLesson.getLessonMode())) {
            List<BizLessonQuestionDetailVo> attendanceQs = lessonQuestionMapper.selectDetailsByLessonId(lessonId);
            if (attendanceQs == null || attendanceQs.isEmpty()) {
                return AjaxResult.error("当前为考勤课，请使用签到，无需提交作业答案");
            }
        }

        log.info("【学生答题】学生 {} 开始提交课程 {} 的答案，共 {} 道题", studentId, lessonId, answers.size());

        // 获取学校信息，计算学生年级
        SysDept dept = deptMapper.selectDeptById(deptId);
        String schoolType = dept != null ? dept.getSchoolType() : "1";
        String gradeName = calculateGradeName(student.getEntryYear(), schoolType);
        
        // 根据年级确定打字基准速度
        int baseSpeed = determineBaseSpeed(gradeName);
        log.info("【学生答题】学生年级: {}, 打字基准速度: {} 字/分", gradeName, baseSpeed);

        // 获取题目列表用于判断正确答案
        List<BizLessonQuestionDetailVo> questions = lessonQuestionMapper.selectDetailsByLessonId(lessonId);
        Map<Long, BizLessonQuestionDetailVo> questionMap = questions.stream()
                .collect(java.util.stream.Collectors.toMap(BizLessonQuestionDetailVo::getQuestionId, q -> q));

        Date now = new Date();
        int totalScore = 0;
        java.util.List<BizStudentAnswer> answersToSave = new java.util.ArrayList<>();

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
                        if (duration == null || duration <= 0) duration = 5; // 默认5分钟

                        // 获取实际答题用时（秒），如果前端未传则使用题目设定时长
                        Integer timeSpent = request.getAnswerTimes() != null ? request.getAnswerTimes().get(questionId) : null;
                        if (timeSpent == null || timeSpent <= 0) {
                            timeSpent = duration * 60; // 兜底：使用设定时长
                        }
                        
                        // 记录答题时间到数据库
                        answer.setAnswerTime(timeSpent);
                        
                        // === 新公式 v5 ===
                        // 目标字数 = min(baseSpeed × 自定义时长, 原文字数)
                        // 速度系数 = 正确字数 / 目标字数（封顶1.0）
                        // 得分 = 满分 × 速度系数 × 正确率
                        if (question.getQuestionScore() != null && question.getQuestionScore() > 0) {
                            int targetCount = Math.min(baseSpeed * duration, originalLength);
                            if (targetCount <= 0) targetCount = originalLength; // 兜底
                            
                            double speedFactor = (double) correctCount / targetCount;
                            speedFactor = Math.min(speedFactor, 1.0); // 封顶，不超过满分
                            
                            double rawScore = question.getQuestionScore() * speedFactor * accuracyRate;
                            score = (int) Math.round(rawScore);
                            
                            // 确保不超过满分
                            score = Math.min(score, question.getQuestionScore().intValue());
                        } else {
                            log.warn("【学生答题】打字题缺少有效分值配置，lessonId={}, questionId={}, questionScore={}",
                                    lessonId, questionId, question.getQuestionScore());
                        }
                    
                    // isCorrect 标记（60%及格线）
                    isCorrect = question.getQuestionScore() != null 
                             && score >= question.getQuestionScore() * 0.6;
                    
                    // 打字题重复提交（含弱网重发）保留历史最高分，防止慢的一次覆盖快的一次
                    answer.setKeepBestScore(true);

                    // 存储前端传来的打字统计数据
                    if (request.getTypingStats() != null && request.getTypingStats().containsKey(questionId)) {
                        TypingStatItem stat = request.getTypingStats().get(questionId);
                        if (stat != null) {
                            answer.setTypingSpeed(stat.getTypingSpeed());
                            answer.setAccuracyRate(stat.getAccuracyRate());
                            answer.setCompletionRate(stat.getCompletionRate());
                        }
                    }
                }
            }
            
            if ("practical".equals(question.getQuestionType())) {
                // 操作题：保存文件路径，异步转换为PDF
                if (studentAnswer != null && !studentAnswer.trim().isEmpty()) {
                    validatePracticalAnswerPath(studentId, lessonId, questionId, studentAnswer);
                    String lowerCaseAnswer = studentAnswer.toLowerCase();
                    answer.setPreviewRetryCount(0);
                    answer.setPreviewLastRetryTime(null);
                    // 检查是否为docx文件，标记为待转换
                    if (lowerCaseAnswer.endsWith(".docx") || lowerCaseAnswer.endsWith(".doc")) {
                        answer.setPreviewStatus("pending");
                        answer.setPreviewPath(null);
                        answer.setPreviewErrorMessage(null);
                    } else if (lowerCaseAnswer.endsWith(".pdf")) {
                        // PDF 可直接预览，无需转换
                        answer.setPreviewStatus("success");
                        answer.setPreviewPath(studentAnswer);
                        answer.setPreviewErrorMessage(null);
                    } else {
                        // 其他文件暂不支持在线预览，保留原文件下载即可
                        answer.setPreviewStatus("failed");
                        answer.setPreviewPath(null);
                        answer.setPreviewErrorMessage("不支持在线预览的文件类型");
                        log.warn("【操作题】暂不支持在线预览的文件类型，lessonId={}, questionId={}, answer={}",
                                lessonId, questionId, studentAnswer);
                    }
                } else {
                    answer.setPreviewStatus(null);
                    answer.setPreviewPath(null);
                    answer.setPreviewRetryCount(0);
                    answer.setPreviewLastRetryTime(null);
                    answer.setPreviewErrorMessage(null);
                }
                // 操作题不自动评分，score必须为null
                answer.setIsCorrect(false); // 均视为未判
                answer.setScore(null);
            } else {
                answer.setIsCorrect(isCorrect);
                answer.setScore(score);
                totalScore += score;
            }

            answersToSave.add(answer);
        }

        // 固定写入顺序可缩小交叉锁概率；死锁重试必须在独立事务代理之外执行。
        answersToSave.sort(java.util.Comparator.comparing(BizStudentAnswer::getQuestionId));
        java.util.List<Long> pendingConversionAnswerIds = persistAnswersWithDeadlockRetry(
                studentId, lessonId, answersToSave);

        triggerPendingPracticalConversions(pendingConversionAnswerIds);
        triggerPracticalDeadlineCheck(
                lessonId, loginUser.getDeptId(), student.getEntryYear(), student.getClassCode());
        log.info("【学生答题】学生 {} 提交课程 {} 答案，得分: {}", student.getStudentId(), lessonId, totalScore);

        return AjaxResult.success("提交成功").put("totalScore", totalScore);
    }

    /**
     * 每次重试都通过 Spring 代理开启新事务，确保死锁事务已完整回滚后才重放整单答案。
     */
    private java.util.List<Long> persistAnswersWithDeadlockRetry(
            Long studentId, Long lessonId, java.util.List<BizStudentAnswer> answersToSave) {
        final int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return studentAnswerSubmissionService.persistAnswers(studentId, lessonId, answersToSave);
            } catch (DeadlockLoserDataAccessException e) {
                if (attempt == maxAttempts) {
                    throw e;
                }
                long delayMillis = 20L * attempt
                        + java.util.concurrent.ThreadLocalRandom.current().nextLong(20L);
                log.warn("【学生答题】检测到数据库死锁，准备重试，studentId={}，lessonId={}，attempt={}",
                        studentId, lessonId, attempt);
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new ServiceException("提交被中断，请重试");
                }
            }
        }
        throw new IllegalStateException("答案提交重试状态异常");
    }

    /**
     * 独立答案事务返回时已提交，此处再调度转换，异步线程可以稳定读取答题记录。
     */
    private void triggerPendingPracticalConversions(java.util.List<Long> answerIds) {
        if (answerIds == null || answerIds.isEmpty()) {
            return;
        }

        // afterCommit 只负责「调度」领取，不在 HTTP 线程上同步 claim/投递 LibreOffice
        java.util.LinkedHashSet<Long> uniqueAnswerIds = new java.util.LinkedHashSet<>(answerIds);
        for (Long answerId : uniqueAnswerIds) {
            asyncConversionService.scheduleSubmitPreviewConversion(answerId);
            log.info("【操作题】answerId={}，答案事务提交后已调度首次转换领取", answerId);
        }
    }

    /**
     * 答案提交成功后再统计50%阈值，避免当前事务内看不到本次提交。
     * 期限补偿任务会兜底，因此这里失败不能回滚学生答案。
     */
    private void triggerPracticalDeadlineCheck(Long lessonId, Long deptId,
                                               String entryYear, String classCode) {
        try {
            practicalGradingDeadlineService.checkAndCreateDeadline(
                    lessonId, deptId, entryYear, classCode);
        } catch (Exception e) {
            log.error("【操作题期限】提交后触发检查失败，lessonId={}，entryYear={}，classCode={}",
                    lessonId, entryYear, classCode, e);
        }
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

    private boolean hasPendingCountyExam()
    {
        Map<String, Object> current = countyExamService.checkCurrentStudentExam();
        return Boolean.TRUE.equals(current.get("hasExam")) && !Boolean.TRUE.equals(current.get("ended"));
    }

    private String validateSubmissionAccess(BizStudent student, Long deptId, Long lessonId)
    {
        if (student == null || deptId == null || lessonId == null)
        {
            return "课程参数错误";
        }
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        if (lesson == null || lesson.getDeptId() == null || !lesson.getDeptId().equals(deptId))
        {
            return "课程不存在或不属于当前学校";
        }
        Long currentLessonId = lessonAssignmentMapper.selectCurrentLessonByClass(
                student.getEntryYear(), student.getClassCode(), deptId);
        if (lessonId.equals(currentLessonId))
        {
            return null;
        }
        Date graceStart = new Date(System.currentTimeMillis()
                - java.util.concurrent.TimeUnit.MINUTES.toMillis(Math.max(submissionGraceMinutes, 0L)));
        int recentAdvance = lessonAssignmentMapper.countRecentAdvanceHistory(
                lessonId, student.getEntryYear(), student.getClassCode(), deptId, graceStart);
        return recentAdvance > 0 ? null : "该课程已结束，补交时间已超过" + submissionGraceMinutes + "分钟";
    }

    private void validatePracticalAnswerPath(Long studentId, Long lessonId, Long questionId, String resource)
    {
        String normalized = resource == null ? "" : resource.trim().replace('\\', '/');
        String lower = normalized.toLowerCase(java.util.Locale.ROOT);
        // 已保存的本人历史答案允许幂等重试，兼容升级前由通用上传生成的旧路径。
        BizStudentAnswer existing = studentAnswerMapper.selectLatestByStudentLessonQuestion(
                studentId, lessonId, questionId);
        if (existing != null && normalized.equals(existing.getStudentAnswer()))
        {
            return;
        }
        if (!lower.startsWith("/profile/upload/student-answer/") || lower.contains("../"))
        {
            throw new ServiceException("操作题作品路径非法，请重新上传");
        }
        boolean allowedExtension = false;
        for (String extension : PRACTICAL_ALLOWED_EXTENSIONS)
        {
            if (lower.endsWith("." + extension))
            {
                allowedExtension = true;
                break;
            }
        }
        if (!allowedExtension)
        {
            throw new ServiceException("操作题作品格式不支持，请重新上传");
        }

        Object owner = redisCache.getCacheObject(practicalUploadOwnerKey(normalized));
        if (owner == null || !String.valueOf(studentId).equals(String.valueOf(owner)))
        {
            throw new ServiceException("操作题作品不属于当前学生，请重新上传");
        }
    }

    private String practicalUploadOwnerKey(String resource)
    {
        return "student:practical-upload-owner:" + resource;
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
        guideSheetAccessService.assertNoPendingCountyExam();

        // 默认今年
        if (year == null) {
            year = java.time.Year.now().getValue();
        }

        log.debug("【历史成绩】学生 {} 查询 {} 年成绩", student.getStudentId(), year);

        java.time.ZoneId zone = java.time.ZoneId.systemDefault();
        Date startTime = Date.from(java.time.LocalDate.of(year, 1, 1)
                .atStartOfDay(zone).toInstant());
        Date endTime = Date.from(java.time.LocalDate.of(year + 1, 1, 1)
                .atStartOfDay(zone).toInstant());
        // 唯一约束保证答案无需再全表取最新；一条 SQL 返回完整成绩单，避免课程和评分项 N+1。
        List<Map<String, Object>> historyList = studentAnswerMapper.selectHistoryScores(
                student.getStudentId(), startTime, endTime);

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
        guideSheetAccessService.assertNoPendingCountyExam();
        
        List<BizLessonQuestionDetailVo> list = studentAnswerMapper.selectWrongQuestions(student.getStudentId(), lessonId);
        return AjaxResult.success(list);
    }

    /**
     * 提交答案请求体
     */
    public static class SubmitAnswerRequest {
        private Long lessonId;
        private Map<Long, String> answers;
        private Map<Long, Integer> answerTimes; // 题目ID -> 答题用时(秒)
        private Map<Long, TypingStatItem> typingStats; // 打字题统计数据

        public Long getLessonId() { return lessonId; }
        public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

        public Map<Long, String> getAnswers() { return answers; }
        public void setAnswers(Map<Long, String> answers) { this.answers = answers; }
        
        public Map<Long, Integer> getAnswerTimes() { return answerTimes; }
        public void setAnswerTimes(Map<Long, Integer> answerTimes) { this.answerTimes = answerTimes; }
        
        public Map<Long, TypingStatItem> getTypingStats() { return typingStats; }
        public void setTypingStats(Map<Long, TypingStatItem> typingStats) { this.typingStats = typingStats; }
    }
    
    /**
     * 打字统计项
     */
    public static class TypingStatItem {
        private Integer typingSpeed;    // 字符/分钟
        private Double accuracyRate;    // 正确率 %
        private Double completionRate;  // 完成率 %
        
        public Integer getTypingSpeed() { return typingSpeed; }
        public void setTypingSpeed(Integer typingSpeed) { this.typingSpeed = typingSpeed; }
        
        public Double getAccuracyRate() { return accuracyRate; }
        public void setAccuracyRate(Double accuracyRate) { this.accuracyRate = accuracyRate; }
        
        public Double getCompletionRate() { return completionRate; }
        public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
    }
}
