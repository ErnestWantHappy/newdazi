package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizScoringItem;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.CountyExam;
import com.ruoyi.business.domain.CountyExamAnswer;
import com.ruoyi.business.domain.CountyExamClass;
import com.ruoyi.business.domain.CountyExamGrader;
import com.ruoyi.business.domain.CountyExamPaperQuestion;
import com.ruoyi.business.domain.CountyExamQuestion;
import com.ruoyi.business.domain.CountyExamStudent;
import com.ruoyi.business.domain.dto.CountyExamGradeRequest;
import com.ruoyi.business.domain.dto.CountyExamGraderAllocateRequest;
import com.ruoyi.business.domain.dto.CountyExamSubmitRequest;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.CountyExamScoringItemVo;
import com.ruoyi.business.mapper.BizScoringItemMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.CountyExamAnswerMapper;
import com.ruoyi.business.mapper.CountyExamClassMapper;
import com.ruoyi.business.mapper.CountyExamGraderMapper;
import com.ruoyi.business.mapper.CountyExamMapper;
import com.ruoyi.business.mapper.CountyExamPaperQuestionMapper;
import com.ruoyi.business.mapper.CountyExamQuestionMapper;
import com.ruoyi.business.mapper.CountyExamStudentMapper;
import com.ruoyi.business.service.AsyncConversionService;
import com.ruoyi.business.service.ICountyExamService;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.mapper.SysDeptMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 区域抽测管理 Service实现类。
 */
@Service
public class CountyExamServiceImpl implements ICountyExamService {
    private static final String STATUS_DRAFT = "0";
    private static final String STATUS_OPEN = "1";
    private static final String STATUS_CLOSED = "2";
    private static final String STATUS_PUBLISHED = "3";
    private static final String GRADING_ENABLED = "1";
    private static final String GRADING_DISABLED = "0";
    private static final String STUDENT_SUBMITTED = "1";
    private static final String AUTO_SUBMIT_YES = "1";
    private static final String AUTO_SUBMIT_NO = "0";
    private static final int DEFAULT_DURATION_MINUTES = 40;
    private static final int REQUIRED_FULL_SCORE = 100;

    @Autowired
    private CountyExamMapper countyExamMapper;

    @Autowired
    private CountyExamQuestionMapper questionMapper;

    @Autowired
    private CountyExamClassMapper classMapper;

    @Autowired
    private CountyExamStudentMapper studentMapper;

    @Autowired
    private CountyExamAnswerMapper answerMapper;

    @Autowired
    private CountyExamGraderMapper graderMapper;

    @Autowired
    private CountyExamPaperQuestionMapper paperQuestionMapper;

    @Autowired
    private BizStudentMapper bizStudentMapper;

    @Autowired
    private SysDeptMapper deptMapper;

    @Autowired
    private BizScoringItemMapper scoringItemMapper;

    @Autowired
    private AsyncConversionService asyncConversionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisCache redisCache;

    @Override
    public CountyExam selectCountyExamById(Long examId) {
        return countyExamMapper.selectCountyExamById(examId);
    }

    @Override
    public List<CountyExam> selectCountyExamList(CountyExam countyExam) {
        return countyExamMapper.selectCountyExamList(countyExam);
    }

    @Override
    public int insertCountyExam(CountyExam countyExam) {
        requireManager();
        normalizeEditableExam(countyExam, null);
        // 状态、总分和时间只能由专用流程产生，不能信任新增请求中的同名字段。
        countyExam.setStatus(STATUS_DRAFT);
        countyExam.setGradingEnabled(GRADING_DISABLED);
        countyExam.setTotalScore(0);
        countyExam.setOpenTime(null);
        countyExam.setCloseTime(null);
        countyExam.setPublishTime(null);
        countyExam.setCreatorId(SecurityUtils.getUserId());
        countyExam.setCreateBy(SecurityUtils.getUsername());
        return countyExamMapper.insertCountyExam(countyExam);
    }

    @Override
    public int updateCountyExam(CountyExam countyExam) {
        requireManager();
        if (countyExam == null || countyExam.getExamId() == null) {
            throw new ServiceException("区域抽测ID不能为空");
        }
        CountyExam saved = requireExam(countyExam.getExamId());
        requireDraft(saved);
        CountyExam editable = new CountyExam();
        editable.setExamId(saved.getExamId());
        editable.setExamName(countyExam.getExamName());
        editable.setSchoolType(countyExam.getSchoolType());
        editable.setExamGrade(countyExam.getExamGrade());
        editable.setShuffleMode(countyExam.getShuffleMode());
        editable.setRandomChoiceCount(countyExam.getRandomChoiceCount());
        editable.setRandomJudgmentCount(countyExam.getRandomJudgmentCount());
        editable.setDurationMinutes(countyExam.getDurationMinutes());
        normalizeEditableExam(editable, saved);
        editable.setUpdateBy(SecurityUtils.getUsername());
        int rows = countyExamMapper.updateDraftFields(editable);
        if (rows != 1) {
            throw new ServiceException("区域抽测状态已变化，请刷新后重试");
        }
        return rows;
    }

    @Override
    public int deleteCountyExamById(Long examId) {
        requireManager();
        CountyExam saved = requireExam(examId);
        requireDraft(saved);
        return countyExamMapper.deleteCountyExamById(examId);
    }

    @Override
    public int deleteCountyExamByIds(Long[] examIds) {
        requireManager();
        if (examIds != null) {
            for (Long examId : examIds) {
                requireDraft(requireExam(examId));
            }
        }
        return countyExamMapper.deleteCountyExamByIds(examIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveQuestions(Long examId, List<CountyExamQuestion> questions) {
        requireManager();
        CountyExam exam = requireExamForUpdate(examId);
        requireDraft(exam);
        if (questions == null || questions.isEmpty()) {
            questionMapper.deleteByExamId(examId);
            updateTotalScore(examId, 0);
            return 0;
        }
        validateQuestionPayload(questions);
        int order = 1;
        for (CountyExamQuestion question : questions) {
            question.setExamId(examId);
            question.setOrderNum(order);
            order++;
        }
        questionMapper.deleteByExamId(examId);
        int rows = questionMapper.batchInsert(questions);
        List<BizLessonQuestionDetailVo> details = questionMapper.selectDetailsByExamId(examId);
        int totalScore = calculateEffectiveTotalScore(exam, details);
        if (totalScore != REQUIRED_FULL_SCORE) {
            throw new ServiceException("区域抽测试卷总分必须为 100 分，当前为 " + totalScore + " 分");
        }
        updateTotalScore(examId, totalScore);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveClasses(Long examId, List<CountyExamClass> classes) {
        requireManager();
        CountyExam exam = requireExamForUpdate(examId);
        requireDraft(exam);
        if (classes == null || classes.isEmpty()) {
            classMapper.deleteByExamId(examId);
            return 0;
        }
        validateClassesForExam(exam, classes);
        for (CountyExamClass examClass : classes) {
            examClass.setExamId(examId);
            examClass.setType("1");
            examClass.setEntryYear(examClass.getEntryYear().trim());
            examClass.setClassCode(examClass.getClassCode().trim());
        }
        classMapper.deleteByExamId(examId);
        return classMapper.batchInsert(classes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> openExam(Long examId, Integer durationMinutes) {
        requireManager();
        CountyExam exam = requireExamForUpdate(examId);
        requireDraft(exam);
        int normalizedDuration = normalizeDurationMinutes(durationMinutes == null ? exam.getDurationMinutes() : durationMinutes);
        CountyExam durationUpdate = new CountyExam();
        durationUpdate.setExamId(examId);
        durationUpdate.setDurationMinutes(normalizedDuration);
        durationUpdate.setUpdateBy(SecurityUtils.getUsername());
        countyExamMapper.updateCountyExam(durationUpdate);
        exam.setDurationMinutes(normalizedDuration);
        List<BizLessonQuestionDetailVo> questions = questionMapper.selectDetailsByExamId(examId);
        if (questions == null || questions.isEmpty()) {
            throw new ServiceException("请先完成区域抽测组卷");
        }
        int totalScore = calculateEffectiveTotalScore(exam, questions);
        if (totalScore != REQUIRED_FULL_SCORE) {
            throw new ServiceException("区域抽测试卷总分必须为 100 分，当前为 " + totalScore + " 分");
        }
        if (!Integer.valueOf(totalScore).equals(exam.getTotalScore())) {
            updateTotalScore(examId, totalScore);
            exam.setTotalScore(totalScore);
        }
        validatePracticalGraderCoverage(examId, questions, graderMapper.selectByExamId(examId));
        List<CountyExamClass> classes = classMapper.selectByExamId(examId);
        if (classes == null || classes.isEmpty()) {
            throw new ServiceException("请先指派参考班级");
        }
        validateClassesForExam(exam, classes);
        validateNoActiveClassConflict(examId, classes);
        snapshotScoringItems(examId, questions);
        int participantCount = createParticipants(examId, classes);
        if (participantCount <= 0) {
            throw new ServiceException("参考班级没有有效学生，不能开启区域抽测");
        }
        List<CountyExamStudent> participants = studentMapper.selectParticipants(examId);
        for (CountyExamStudent participant : participants) {
            ensurePaperForStudent(exam, participant.getStudentId());
        }
        if (countyExamMapper.updateStatus(examId, STATUS_DRAFT, STATUS_OPEN) != 1) {
            throw new ServiceException("区域抽测状态已变化，请刷新后重试");
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("participantCount", participantCount);
        result.put("durationMinutes", normalizedDuration);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> closeExam(Long examId) {
        requireManager();
        CountyExam exam = requireExamForUpdate(examId);
        if (!STATUS_OPEN.equals(exam.getStatus())) {
            throw new ServiceException("只有已开启的区域抽测可以关闭");
        }
        List<BizLessonQuestionDetailVo> questions = questionMapper.selectDetailsByExamId(examId);
        List<CountyExamGrader> configs = graderMapper.selectByExamId(examId);
        validatePracticalGraderCoverage(examId, questions, configs);
        int autoSubmitCount = autoSubmitOpenParticipants(exam);
        if (countyExamMapper.updateStatus(examId, STATUS_OPEN, STATUS_CLOSED) != 1) {
            throw new ServiceException("区域抽测状态已变化，请刷新后重试");
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", STATUS_CLOSED);
        result.put("autoSubmitCount", autoSubmitCount);
        if (configs != null && !configs.isEmpty()) {
            result.putAll(generateGradingTasks(examId, configs));
        }
        result.put("gradingProgress", buildGradingProgress(examId));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> allocateGraders(Long examId, CountyExamGraderAllocateRequest request) {
        requireManager();
        CountyExam exam = requireExamForUpdate(examId);
        if (STATUS_PUBLISHED.equals(exam.getStatus())) {
            throw new ServiceException("成绩已发布，不能修改评卷配置");
        }
        List<CountyExamGrader> configs = buildGraderConfigs(examId, request);
        if (configs.isEmpty()) {
            throw new ServiceException("请选择评卷教师");
        }
        validatePracticalGraderCoverage(examId, questionMapper.selectDetailsByExamId(examId), configs);
        graderMapper.deleteByExamId(examId);
        for (CountyExamGrader config : configs) {
            graderMapper.insert(config);
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("configured", true);
        result.put("configCount", configs.size());
        if (STATUS_CLOSED.equals(exam.getStatus())) {
            result.putAll(generateGradingTasks(examId, configs));
        } else {
            result.put("allocated", false);
        }
        result.put("gradingProgress", buildGradingProgress(examId));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> resetGraders(Long examId) {
        requireManager();
        CountyExam exam = requireExamForUpdate(examId);
        if (!STATUS_CLOSED.equals(exam.getStatus())) {
            throw new ServiceException("只有关闭后且未发布的区域抽测可以重置评卷任务");
        }
        List<CountyExamGrader> configs = graderMapper.selectByExamId(examId);
        if (configs == null || configs.isEmpty()) {
            throw new ServiceException("请先配置评卷教师");
        }
        jdbcTemplate.update(
                "delete d from biz_county_exam_grading_detail d " +
                        "inner join biz_county_exam_answer a on d.answer_id = a.answer_id " +
                        "inner join biz_county_exam_paper_question pq on pq.exam_id = a.exam_id and pq.student_id = a.student_id and pq.question_id = a.question_id " +
                        "where a.exam_id = ? and pq.question_type = 'practical'",
                examId);
        int resetCount = answerMapper.resetPracticalGrading(examId);
        for (CountyExamGrader config : configs) {
            config.setGradedCount(0);
            graderMapper.update(config);
        }
        Map<String, Object> result = generateGradingTasks(examId, configs);
        result.put("resetCount", resetCount);
        result.put("gradingProgress", buildGradingProgress(examId));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateGradingEnabled(Long examId, boolean enabled) {
        requireManager();
        CountyExam exam = requireExamForUpdate(examId);
        if (!STATUS_CLOSED.equals(exam.getStatus())) {
            throw new ServiceException("只有已关闭且未发布的区域抽测可以调整评卷入口");
        }
        if (enabled) {
            validatePracticalGraderCoverage(examId, questionMapper.selectDetailsByExamId(examId),
                    graderMapper.selectByExamId(examId));
            int assignedCount = countAssignedPracticalAnswers(examId);
            if (assignedCount <= 0) {
                throw new ServiceException("请先配置评卷教师并生成匿名评卷任务");
            }
        }
        String gradingEnabled = enabled ? GRADING_ENABLED : GRADING_DISABLED;
        if (countyExamMapper.updateGradingEnabled(
                examId, gradingEnabled, SecurityUtils.getUsername()) != 1) {
            throw new ServiceException("区域抽测状态已变化，请刷新后重试");
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("examId", examId);
        result.put("gradingEnabled", gradingEnabled);
        result.put("gradingProgress", buildGradingProgress(examId));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> publishExam(Long examId) {
        requireManager();
        CountyExam exam = requireExamForUpdate(examId);
        if (!STATUS_CLOSED.equals(exam.getStatus())) {
            throw new ServiceException("请先关闭区域抽测，再发布成绩");
        }
        autoSubmitOpenParticipants(exam);
        int ungradedCount = answerMapper.countUngradedPracticalAnswers(examId);
        if (ungradedCount > 0) {
            throw new ServiceException("还有 " + ungradedCount + " 份操作题未完成评卷");
        }
        for (CountyExamStudent student : studentMapper.selectParticipants(examId)) {
            recomputeStudentScore(examId, student.getStudentId());
        }
        if (countyExamMapper.updateStatus(examId, STATUS_CLOSED, STATUS_PUBLISHED) != 1) {
            throw new ServiceException("区域抽测状态已变化，请刷新后重试");
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", STATUS_PUBLISHED);
        return result;
    }

    @Override
    public Map<String, Object> getExamDetail(Long examId) {
        requireManager();
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("exam", requireExam(examId));
        result.put("questions", questionMapper.selectDetailsByExamId(examId));
        result.put("classes", classMapper.selectByExamId(examId));
        result.put("graders", graderMapper.selectByExamId(examId));
        result.put("gradingProgress", buildGradingProgress(examId));
        return result;
    }

    @Override
    public Map<String, Object> getSummary(Long examId) {
        requireManager();
        CountyExam exam = requireExam(examId);
        List<Map<String, Object>> schools = studentMapper.selectSummaryRows(examId);
        List<CountyExamStudent> participants = studentMapper.selectParticipants(examId);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("schools", schools);
        result.put("overview", buildAnalysisOverview(
                participants, schools == null ? 0 : schools.size(), exam.getTotalScore()));
        result.put("distribution", buildScoreDistribution(participants, exam.getTotalScore()));
        result.put("questions", answerMapper.selectQuestionPerformance(examId));
        result.put("official", STATUS_PUBLISHED.equals(exam.getStatus()));
        return result;
    }

    @Override
    public List<Map<String, Object>> getStudents(Long examId, String keyword) {
        requireManager();
        requireExam(examId);
        return studentMapper.selectStudentRows(examId, keyword);
    }

    @Override
    public void exportStudents(Long examId, HttpServletResponse response) {
        requireManager();
        CountyExam exam = requireExam(examId);
        List<Map<String, Object>> rows = studentMapper.selectStudentRows(examId, null);
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("区域抽测成绩");
            String[] headers = {"学校", "班级", "账号", "学生姓名", "学号", "理论分", "打字分", "操作分", "总分", "状态", "提交时间"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
                sheet.setColumnWidth(i, 16 * 256);
            }
            int rowIndex = 1;
            for (Map<String, Object> item : rows) {
                Row row = sheet.createRow(rowIndex++);
                writeCell(row, 0, item.get("deptName"));
                writeCell(row, 1, item.get("classInfo"));
                writeCell(row, 2, item.get("userName"));
                writeCell(row, 3, item.get("studentName"));
                writeCell(row, 4, item.get("studentNo"));
                writeCell(row, 5, item.get("theoryScore"));
                writeCell(row, 6, item.get("typingScore"));
                writeCell(row, 7, item.get("practicalScore"));
                writeCell(row, 8, item.get("totalScore"));
                writeCell(row, 9, STUDENT_SUBMITTED.equals(String.valueOf(item.get("status"))) ? "已提交" : "未提交");
                writeCell(row, 10, item.get("submitTime"));
            }
            String fileName = URLEncoder.encode(exam.getExamName() + "-成绩.xlsx", StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new ServiceException("导出区域抽测成绩失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> checkCurrentStudentExam() {
        return getStudentExam(false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> getCurrentStudentExam() {
        return getStudentExam(true);
    }

    private Map<String, Object> getStudentExam(boolean startTiming) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (student == null) {
            throw new ServiceException("未找到学生信息");
        }
        List<CountyExamClass> activeClasses = classMapper.selectActiveByStudentInfo(
                loginUser.getDeptId(), student.getEntryYear(), student.getClassCode());
        if (activeClasses == null || activeClasses.isEmpty()) {
            return noStudentExam();
        }
        CountyExam exam = requireExam(activeClasses.get(0).getExamId());
        ensureParticipant(exam, student, loginUser.getDeptId());
        if (startTiming) {
            ensurePaperForStudent(exam, student.getStudentId());
        }
        CountyExamStudent examStudent = startTiming
                ? ensureStudentTiming(exam, lockStudentAttempt(exam.getExamId(), student.getStudentId()))
                : studentMapper.selectByExamAndStudent(exam.getExamId(), student.getStudentId());
        Date now = new Date();
        if (STUDENT_SUBMITTED.equals(examStudent.getStatus())) {
            return endedStudentExamPayload(exam, examStudent, "区域抽测已提交");
        }
        if (isTimedOut(examStudent, now)) {
            autoSubmitStudentExam(exam, student);
            CountyExamStudent submitted = studentMapper.selectByExamAndStudent(exam.getExamId(), student.getStudentId());
            return endedStudentExamPayload(exam, submitted, "区域抽测作答时间已结束");
        }
        if (!startTiming) {
            return buildStudentExamStatusPayload(exam, examStudent);
        }
        Map<String, Object> result = buildStudentExamPayload(exam, student, loginUser.getDeptId());
        result.put("hasExam", true);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> saveStudentDraft(CountyExamSubmitRequest request) {
        return saveStudentAnswers(request, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitStudentExam(CountyExamSubmitRequest request) {
        return saveStudentAnswers(request, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long validateStudentWorkUpload(Long examId, Long questionId) {
        if (examId == null || questionId == null) {
            throw new ServiceException("操作题上传参数不完整");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (student == null) {
            throw new ServiceException("未找到学生信息");
        }
        CountyExam exam = requireExam(examId);
        if (!STATUS_OPEN.equals(exam.getStatus())) {
            throw new ServiceException("区域抽测未开启或已关闭");
        }
        ensureParticipant(exam, student, loginUser.getDeptId());
        CountyExamStudent examStudent = lockStudentAttempt(exam.getExamId(), student.getStudentId());
        examStudent = ensureStudentTiming(exam, examStudent);
        if (STUDENT_SUBMITTED.equals(examStudent.getStatus())) {
            throw new ServiceException("区域抽测已最终提交，不能继续上传");
        }
        if (isTimedOut(examStudent, new Date())) {
            autoSubmitStudentExam(exam, student);
            throw new ServiceException("区域抽测作答时间已结束");
        }
        ensurePaperForStudent(exam, student.getStudentId());
        if (paperQuestionMapper.countPracticalByExamStudentQuestion(
                examId, student.getStudentId(), questionId) <= 0) {
            throw new ServiceException("只能上传本人区域抽测试卷中的操作题作品");
        }
        return student.getStudentId();
    }

    @Override
    public Map<String, Object> getGradingEntry() {
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> result = new HashMap<String, Object>();
        int pendingCount = answerMapper.countTasksByGrader(userId, "0");
        int totalCount = answerMapper.countTasksByGrader(userId, null);
        result.put("hasTask", pendingCount > 0);
        result.put("taskCount", pendingCount);
        result.put("pendingTaskCount", pendingCount);
        result.put("totalTaskCount", totalCount);
        return result;
    }

    @Override
    public List<Map<String, Object>> getGradingTasks(String gradingStatus) {
        Long userId = SecurityUtils.getUserId();
        List<Map<String, Object>> tasks = answerMapper.selectGradingTasks(userId, gradingStatus);
        for (Map<String, Object> task : tasks) {
            Long examId = longValue(task.get("examId"));
            Long questionId = longValue(task.get("questionId"));
            task.put("scoringItems", selectCountyScoringItems(
                    examId, questionId, intValue(task.get("questionScore"))));
        }
        return tasks;
    }

    @Override
    public Map<String, Object> getGradingAnswer(Long answerId) {
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> detail = answerMapper.selectGradingAnswerDetail(answerId, userId);
        if (detail == null) {
            throw new ServiceException("未找到可评阅的匿名答卷");
        }
        detail.put("scoringItems", selectCountyScoringItems(
                longValue(detail.get("examId")), longValue(detail.get("questionId")),
                intValue(detail.get("questionScore"))));
        detail.put("scoringDetails", selectCountyGradingDetails(answerId));
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> gradeAnswer(CountyExamGradeRequest request) {
        if (request == null || request.getAnswerId() == null || request.getScore() == null) {
            throw new ServiceException("评卷参数不完整");
        }
        if (request.getScore() < 0) {
            throw new ServiceException("分数不能为负数");
        }
        Long userId = SecurityUtils.getUserId();
        CountyExamAnswer answer = answerMapper.selectById(request.getAnswerId());
        if (answer == null || answer.getGraderId() == null || !answer.getGraderId().equals(userId)) {
            throw new ServiceException("只能评阅分配给自己的匿名答卷");
        }
        CountyExam exam = requireExamForUpdate(answer.getExamId());
        if (!STATUS_CLOSED.equals(exam.getStatus())
                || !GRADING_ENABLED.equals(exam.getGradingEnabled())) {
            throw new ServiceException("当前区域抽测未开放评卷");
        }
        Map<String, Object> detail = answerMapper.selectGradingAnswerDetail(request.getAnswerId(), userId);
        if (detail == null) {
            throw new ServiceException("未找到可评阅的匿名答卷");
        }
        Object questionScore = detail.get("questionScore");
        if (questionScore != null && BigDecimal.valueOf(request.getScore()).compareTo(new BigDecimal(String.valueOf(questionScore))) > 0) {
            throw new ServiceException("得分不能超过题目满分");
        }
        List<CountyExamScoringItemVo> scoringItems = selectCountyScoringItems(
                longValue(detail.get("examId")), longValue(detail.get("questionId")),
                intValue(questionScore));
        validateCountyScoringDetails(request, scoringItems);
        if (answerMapper.updateGrade(request.getAnswerId(), request.getScore(), userId) != 1) {
            throw new ServiceException("评卷状态已变化，请刷新后重试");
        }
        saveCountyGradingDetails(request);
        refreshGraderCount(answer.getExamId(), answer.getQuestionId(), userId);
        recomputeStudentScore(answer.getExamId(), answer.getStudentId());
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("answerId", request.getAnswerId());
        result.put("score", request.getScore());
        return result;
    }

    @Override
    public List<Map<String, Object>> getAssignableClasses(String schoolType) {
        requireManager();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select d.dept_id as deptId, d.dept_name as deptName, s.entry_year as entryYear, s.class_code as classCode, count(1) as studentCount " +
                        "from biz_student s " +
                        "inner join sys_user u on s.user_id = u.user_id " +
                        "inner join sys_dept d on u.dept_id = d.dept_id " +
                        "where d.del_flag = '0' and d.status = '0' and (? is null or d.school_type = ?) " +
                        "group by d.dept_id, d.dept_name, s.entry_year, s.class_code " +
                        "order by d.dept_name asc, s.entry_year desc, cast(s.class_code as unsigned) asc, s.class_code asc",
                schoolType, schoolType);
        return rows;
    }

    @Override
    public List<Map<String, Object>> getAssignableGraders(String keyword) {
        requireManager();
        // 全平台有效教师均可评卷；有关键字时提高命中上限，避免 limit 过小静默截断导致「有账号选不到」。
        String likeKeyword = StringUtils.isEmpty(keyword) ? null : "%" + keyword.trim() + "%";
        int limit = likeKeyword == null ? 200 : 500;
        return jdbcTemplate.queryForList(
                "select distinct u.user_id as userId, u.user_name as userName, u.nick_name as nickName, " +
                        "d.dept_name as deptName " +
                        "from sys_user u " +
                        "inner join sys_user_role ur on u.user_id = ur.user_id " +
                        "inner join sys_role r on ur.role_id = r.role_id " +
                        "left join sys_dept d on u.dept_id = d.dept_id " +
                        "where u.del_flag = '0' and u.status = '0' " +
                        "and r.del_flag = '0' and r.status = '0' and r.role_key = 'teacher' " +
                        "and (? is null or u.nick_name like ? or u.user_name like ? or d.dept_name like ?) " +
                        "order by d.dept_name asc, u.nick_name asc, u.user_id asc " +
                        "limit " + limit,
                likeKeyword, likeKeyword, likeKeyword, likeKeyword);
    }

    private List<CountyExamGrader> buildGraderConfigs(Long examId, CountyExamGraderAllocateRequest request) {
        List<BizLessonQuestionDetailVo> questions = questionMapper.selectDetailsByExamId(examId);
        Set<Long> practicalQuestionIds = new HashSet<Long>();
        if (questions != null) {
            for (BizLessonQuestionDetailVo question : questions) {
                if (question != null && "practical".equals(question.getQuestionType())) {
                    practicalQuestionIds.add(question.getQuestionId());
                }
            }
        }
        if (practicalQuestionIds.isEmpty()) {
            throw new ServiceException("本场区域抽测没有操作题，无需配置评卷教师");
        }

        List<CountyExamGrader> configs = new ArrayList<CountyExamGrader>();
        Set<String> uniqueKeys = new HashSet<String>();
        if (request != null && request.getAssignments() != null && !request.getAssignments().isEmpty()) {
            for (CountyExamGraderAllocateRequest.Assignment item : request.getAssignments()) {
                if (item == null || item.getQuestionId() == null || item.getGraderId() == null) {
                    throw new ServiceException("评卷教师配置不完整");
                }
                if (!practicalQuestionIds.contains(item.getQuestionId())) {
                    throw new ServiceException("只能为操作题配置评卷教师");
                }
                String key = item.getQuestionId() + "-" + item.getGraderId();
                if (!uniqueKeys.add(key)) {
                    throw new ServiceException("同一道操作题不能重复配置同一位评卷教师");
                }
                CountyExamGrader config = new CountyExamGrader();
                config.setExamId(examId);
                config.setQuestionId(item.getQuestionId());
                config.setGraderId(item.getGraderId());
                config.setTargetCount(item.getTargetCount() == null || item.getTargetCount() < 0 ? 0 : item.getTargetCount());
                config.setGradedCount(0);
                configs.add(config);
            }
            validateActiveGraderIds(configs);
            return configs;
        }

        if (request != null && request.getGraderIds() != null) {
            for (Long questionId : practicalQuestionIds) {
                for (Long graderId : request.getGraderIds()) {
                    if (graderId == null || graderId <= 0) {
                        throw new ServiceException("评卷教师ID无效");
                    }
                    String key = questionId + "-" + graderId;
                    if (!uniqueKeys.add(key)) {
                        continue;
                    }
                    CountyExamGrader config = new CountyExamGrader();
                    config.setExamId(examId);
                    config.setQuestionId(questionId);
                    config.setGraderId(graderId);
                    config.setTargetCount(0);
                    config.setGradedCount(0);
                    configs.add(config);
                }
            }
        }
        validateActiveGraderIds(configs);
        return configs;
    }

    private void validateActiveGraderIds(List<CountyExamGrader> configs) {
        Set<Long> graderIds = new HashSet<Long>();
        for (CountyExamGrader config : configs) {
            graderIds.add(config.getGraderId());
        }
        for (Long graderId : graderIds) {
            Integer count = jdbcTemplate.queryForObject(
                    "select count(distinct u.user_id) " +
                            "from sys_user u " +
                            "inner join sys_user_role ur on ur.user_id = u.user_id " +
                            "inner join sys_role r on r.role_id = ur.role_id " +
                            "where u.user_id = ? and u.del_flag = '0' and u.status = '0' " +
                            "and r.del_flag = '0' and r.status = '0' and r.role_key = 'teacher'",
                    Integer.class, graderId);
            if (count == null || count <= 0) {
                throw new ServiceException("评卷教师不存在、已停用或不是教师：" + graderId);
            }
        }
    }

    void validatePracticalGraderCoverage(Long examId, List<BizLessonQuestionDetailVo> questions,
                                         List<CountyExamGrader> configs) {
        Set<Long> practicalQuestionIds = new HashSet<Long>();
        if (questions != null) {
            for (BizLessonQuestionDetailVo question : questions) {
                if (question != null && "practical".equals(question.getQuestionType())
                        && question.getQuestionId() != null) {
                    practicalQuestionIds.add(question.getQuestionId());
                }
            }
        }
        if (practicalQuestionIds.isEmpty()) {
            return;
        }

        Set<Long> configuredQuestionIds = new HashSet<Long>();
        if (configs != null) {
            for (CountyExamGrader config : configs) {
                if (config != null && config.getQuestionId() != null && config.getGraderId() != null
                        && config.getGraderId() > 0 && practicalQuestionIds.contains(config.getQuestionId())) {
                    configuredQuestionIds.add(config.getQuestionId());
                }
            }
        }
        practicalQuestionIds.removeAll(configuredQuestionIds);
        if (!practicalQuestionIds.isEmpty()) {
            throw new ServiceException("请先为每一道操作题配置评卷教师，缺少题目：" + practicalQuestionIds
                    + "（区域抽测ID：" + examId + "）");
        }
    }

    private Map<String, Object> generateGradingTasks(Long examId, List<CountyExamGrader> configs) {
        validatePracticalGraderCoverage(examId, questionMapper.selectDetailsByExamId(examId), configs);
        if (answerMapper.countGradedPracticalAnswers(examId) > 0) {
            throw new ServiceException("已有操作题完成评卷，不能重新生成评卷任务");
        }
        answerMapper.clearPracticalGraders(examId);
        Map<Long, List<CountyExamGrader>> configsByQuestion = new LinkedHashMap<Long, List<CountyExamGrader>>();
        for (CountyExamGrader config : configs) {
            if (config.getQuestionId() == null || config.getGraderId() == null) {
                continue;
            }
            if (!configsByQuestion.containsKey(config.getQuestionId())) {
                configsByQuestion.put(config.getQuestionId(), new ArrayList<CountyExamGrader>());
            }
            configsByQuestion.get(config.getQuestionId()).add(config);
        }

        int answerCount = 0;
        int assignedCount = 0;
        Set<Long> graderIds = new HashSet<Long>();
        for (Map.Entry<Long, List<CountyExamGrader>> entry : configsByQuestion.entrySet()) {
            Long questionId = entry.getKey();
            List<CountyExamGrader> questionConfigs = entry.getValue();
            List<CountyExamAnswer> answers = answerMapper.selectPracticalAnswersForAllocationByQuestion(examId, questionId);
            Collections.shuffle(answers, new java.util.Random(examId * 1009L + questionId));
            answerCount += answers.size();

            Map<Long, List<Long>> answerIdsByGrader = new LinkedHashMap<Long, List<Long>>();
            for (CountyExamGrader config : questionConfigs) {
                graderIds.add(config.getGraderId());
                answerIdsByGrader.put(config.getGraderId(), new ArrayList<Long>());
            }

            int cursor = 0;
            for (CountyExamGrader config : questionConfigs) {
                int targetCount = config.getTargetCount() == null ? 0 : config.getTargetCount();
                for (int i = 0; i < targetCount && cursor < answers.size(); i++) {
                    answerIdsByGrader.get(config.getGraderId()).add(answers.get(cursor++).getAnswerId());
                }
            }

            int roundRobinIndex = 0;
            while (cursor < answers.size() && !questionConfigs.isEmpty()) {
                CountyExamGrader config = questionConfigs.get(roundRobinIndex % questionConfigs.size());
                answerIdsByGrader.get(config.getGraderId()).add(answers.get(cursor++).getAnswerId());
                roundRobinIndex++;
            }

            for (CountyExamGrader config : questionConfigs) {
                List<Long> answerIds = answerIdsByGrader.get(config.getGraderId());
                int actualCount = answerIds == null ? 0 : answerIds.size();
                config.setTargetCount(actualCount);
                config.setGradedCount(0);
                if (config.getId() != null) {
                    graderMapper.update(config);
                }
                if (answerIds != null && !answerIds.isEmpty()) {
                    answerMapper.batchUpdateGrader(answerIds, config.getGraderId());
                    assignedCount += answerIds.size();
                }
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("allocated", true);
        result.put("answerCount", answerCount);
        result.put("assignedCount", assignedCount);
        result.put("graderCount", graderIds.size());
        return result;
    }

    private CountyExamStudent ensureStudentTiming(CountyExam exam, CountyExamStudent examStudent) {
        if (examStudent == null) {
            throw new ServiceException("未找到区域抽测参考学生记录");
        }
        if (examStudent.getStartTime() == null || examStudent.getDeadlineTime() == null) {
            Date startTime = new Date();
            examStudent.setStartTime(startTime);
            examStudent.setDeadlineTime(addMinutes(startTime, normalizeDurationMinutes(exam.getDurationMinutes())));
            examStudent.setAutoSubmit(AUTO_SUBMIT_NO);
            studentMapper.update(examStudent);
        }
        return examStudent;
    }

    private CountyExamStudent lockStudentAttempt(Long examId, Long studentId) {
        CountyExamStudent examStudent = studentMapper.selectByExamAndStudentForUpdate(examId, studentId);
        if (examStudent == null) {
            throw new ServiceException("未找到区域抽测参考学生记录");
        }
        return examStudent;
    }

    private boolean isTimedOut(CountyExamStudent examStudent, Date now) {
        return examStudent != null
                && examStudent.getDeadlineTime() != null
                && !examStudent.getDeadlineTime().after(now);
    }

    private long remainingSeconds(CountyExamStudent examStudent, Date now) {
        if (examStudent == null || examStudent.getDeadlineTime() == null) {
            return 0L;
        }
        return Math.max(0L, (examStudent.getDeadlineTime().getTime() - now.getTime()) / 1000L);
    }

    private Date addMinutes(Date startTime, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    private int normalizeDurationMinutes(Integer durationMinutes) {
        return durationMinutes == null || durationMinutes <= 0 ? DEFAULT_DURATION_MINUTES : durationMinutes;
    }

    private Map<String, Object> endedStudentExamPayload(CountyExam exam, CountyExamStudent examStudent, String message) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("hasExam", true);
        result.put("ended", true);
        result.put("submitted", examStudent != null && STUDENT_SUBMITTED.equals(examStudent.getStatus()));
        result.put("autoSubmitted", examStudent != null && AUTO_SUBMIT_YES.equals(examStudent.getAutoSubmit()));
        result.put("examId", exam.getExamId());
        result.put("examName", exam.getExamName());
        result.put("status", exam.getStatus());
        result.put("durationMinutes", normalizeDurationMinutes(exam.getDurationMinutes()));
        result.put("startTime", examStudent == null ? null : examStudent.getStartTime());
        result.put("deadlineTime", examStudent == null ? null : examStudent.getDeadlineTime());
        result.put("submitTime", examStudent == null ? null : examStudent.getSubmitTime());
        result.put("serverTime", new Date());
        result.put("message", message);
        return result;
    }

    private Map<String, Object> buildStudentExamStatusPayload(CountyExam exam, CountyExamStudent examStudent) {
        Date now = new Date();
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("hasExam", true);
        result.put("ended", false);
        result.put("submitted", examStudent != null && STUDENT_SUBMITTED.equals(examStudent.getStatus()));
        result.put("examId", exam.getExamId());
        result.put("examName", exam.getExamName());
        result.put("status", exam.getStatus());
        result.put("durationMinutes", normalizeDurationMinutes(exam.getDurationMinutes()));
        result.put("startTime", examStudent == null ? null : examStudent.getStartTime());
        result.put("deadlineTime", examStudent == null ? null : examStudent.getDeadlineTime());
        result.put("serverTime", now);
        result.put("remainingSeconds", examStudent == null ? null : remainingSeconds(examStudent, now));
        return result;
    }

    private boolean autoSubmitStudentExam(CountyExam exam, BizStudent student) {
        CountyExamStudent examStudent = lockStudentAttempt(exam.getExamId(), student.getStudentId());
        if (STUDENT_SUBMITTED.equals(examStudent.getStatus())) {
            return false;
        }
        ensurePaperForStudent(exam, student.getStudentId());
        List<BizLessonQuestionDetailVo> questions = paperQuestionMapper.selectDetailsByExamAndStudent(exam.getExamId(), student.getStudentId());
        List<CountyExamAnswer> answers = answerMapper.selectByExamAndStudent(exam.getExamId(), student.getStudentId());
        Map<Long, CountyExamAnswer> answerMap = new HashMap<Long, CountyExamAnswer>();
        for (CountyExamAnswer answer : answers) {
            answerMap.put(answer.getQuestionId(), answer);
        }

        Date now = new Date();
        CountyExamSubmitRequest emptyRequest = new CountyExamSubmitRequest();
        emptyRequest.setExamId(exam.getExamId());
        emptyRequest.setAnswers(new HashMap<Long, String>());
        List<Long> pendingPreviewAnswerIds = new ArrayList<Long>();
        for (BizLessonQuestionDetailVo question : questions) {
            CountyExamAnswer existing = answerMap.get(question.getQuestionId());
            if (existing == null) {
                CountyExamAnswer answer = buildAnswer(exam, student, question, null, emptyRequest, now, true);
                answerMapper.insert(answer);
                if ("pending".equals(answer.getPreviewStatus()) && answer.getAnswerId() != null) {
                    pendingPreviewAnswerIds.add(answer.getAnswerId());
                }
            } else if ("practical".equals(question.getQuestionType()) && StringUtils.isEmpty(existing.getStudentAnswer())) {
                CountyExamAnswer answer = buildAnswer(exam, student, question, null, emptyRequest, now, true);
                answer.setAnswerId(existing.getAnswerId());
                answerMapper.updateStudentAnswer(answer);
            }
        }
        recomputeStudentScore(exam.getExamId(), student.getStudentId());
        if (studentMapper.markSubmittedIfOpen(
                examStudent.getId(), now, AUTO_SUBMIT_YES) != 1) {
            throw new ServiceException("区域抽测提交状态已变化，请刷新后重试");
        }
        triggerCountyPreviewConversionsAfterCommit(pendingPreviewAnswerIds);
        return true;
    }

    private int autoSubmitOpenParticipants(CountyExam exam) {
        int count = 0;
        for (CountyExamStudent participant : studentMapper.selectParticipants(exam.getExamId())) {
            if (participant == null || STUDENT_SUBMITTED.equals(participant.getStatus())) {
                continue;
            }
            BizStudent student = bizStudentMapper.selectBizStudentByStudentId(participant.getStudentId());
            if (student == null) {
                continue;
            }
            if (autoSubmitStudentExam(exam, student)) {
                count++;
            }
        }
        return count;
    }

    private Map<String, Object> saveStudentAnswers(CountyExamSubmitRequest request, boolean finalSubmit) {
        if (request == null || request.getExamId() == null) {
            throw new ServiceException("区域抽测提交参数不完整");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (student == null) {
            throw new ServiceException("未找到学生信息");
        }
        CountyExam exam = requireExam(request.getExamId());
        if (!STATUS_OPEN.equals(exam.getStatus())) {
            throw new ServiceException("区域抽测未开启或已关闭");
        }
        ensureParticipant(exam, student, loginUser.getDeptId());
        CountyExamStudent examStudent = lockStudentAttempt(exam.getExamId(), student.getStudentId());
        if (STUDENT_SUBMITTED.equals(examStudent.getStatus())) {
            throw new ServiceException("区域抽测已最终提交，不能再次修改");
        }
        examStudent = ensureStudentTiming(exam, examStudent);
        if (isTimedOut(examStudent, new Date())) {
            autoSubmitStudentExam(exam, student);
            return endedStudentExamPayload(exam, studentMapper.selectByExamAndStudent(exam.getExamId(), student.getStudentId()), "区域抽测作答时间已结束");
        }
        ensurePaperForStudent(exam, student.getStudentId());
        List<BizLessonQuestionDetailVo> paperQuestions = paperQuestionMapper.selectDetailsByExamAndStudent(exam.getExamId(), student.getStudentId());
        Map<Long, BizLessonQuestionDetailVo> questionMap = new HashMap<Long, BizLessonQuestionDetailVo>();
        for (BizLessonQuestionDetailVo question : paperQuestions) {
            questionMap.put(question.getQuestionId(), question);
        }
        Map<Long, String> requestAnswers = request.getAnswers() == null ? new HashMap<Long, String>() : request.getAnswers();
        List<Long> pendingPreviewAnswerIds = new ArrayList<Long>();
        Date now = new Date();
        for (BizLessonQuestionDetailVo question : paperQuestions) {
            boolean hasRequestAnswer = requestAnswers.containsKey(question.getQuestionId());
            if (!finalSubmit && !hasRequestAnswer) {
                continue;
            }
            CountyExamAnswer existing = answerMapper.selectLatestByExamStudentQuestion(exam.getExamId(), student.getStudentId(), question.getQuestionId());
            if (finalSubmit && !hasRequestAnswer && existing != null) {
                continue;
            }
            String studentAnswer = requestAnswers.get(question.getQuestionId());
            if ("practical".equals(question.getQuestionType()) && hasRequestAnswer
                    && StringUtils.isNotEmpty(studentAnswer)
                    && (existing == null || !Objects.equals(existing.getStudentAnswer(), studentAnswer))
                    && !isStudentWorkPath(studentAnswer, exam.getExamId(), student.getStudentId(), question.getQuestionId())) {
                throw new ServiceException("操作题作品路径无效，请重新上传");
            }
            if ("typing".equals(question.getQuestionType()) && existing != null && existing.getAnswerTime() != null) {
                if (hasRequestAnswer && !Objects.equals(existing.getStudentAnswer(), studentAnswer)) {
                    throw new ServiceException("打字题已提交，不能重新打字");
                }
                continue;
            }
            CountyExamAnswer answer = buildAnswer(exam, student, question, studentAnswer, request, now, finalSubmit);
            if (existing == null) {
                answerMapper.insert(answer);
            } else {
                answer.setAnswerId(existing.getAnswerId());
                answerMapper.updateStudentAnswer(answer);
            }
            if ("pending".equals(answer.getPreviewStatus()) && answer.getAnswerId() != null) {
                pendingPreviewAnswerIds.add(answer.getAnswerId());
            }
        }
        recomputeStudentScore(exam.getExamId(), student.getStudentId());
        if (finalSubmit && studentMapper.markSubmittedIfOpen(
                examStudent.getId(), now, AUTO_SUBMIT_NO) != 1) {
            throw new ServiceException("区域抽测提交状态已变化，请刷新后重试");
        }
        triggerCountyPreviewConversionsAfterCommit(pendingPreviewAnswerIds);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("submitted", finalSubmit);
        result.put("examId", exam.getExamId());
        return result;
    }

    private CountyExamAnswer buildAnswer(CountyExam exam, BizStudent student, BizLessonQuestionDetailVo question,
                                         String studentAnswer, CountyExamSubmitRequest request, Date now, boolean finalSubmit) {
        CountyExamAnswer answer = new CountyExamAnswer();
        answer.setExamId(exam.getExamId());
        answer.setStudentId(student.getStudentId());
        answer.setQuestionId(question.getQuestionId());
        answer.setStudentAnswer(studentAnswer);
        answer.setSubmitTime(now);
        answer.setAnswerTime(request.getAnswerTimes() == null ? null : request.getAnswerTimes().get(question.getQuestionId()));
        String questionType = question.getQuestionType();
        int score = 0;
        Boolean correct = Boolean.FALSE;
        if ("choice".equals(questionType)) {
            String normalized = studentAnswer == null ? null : studentAnswer.trim();
            correct = normalized != null && normalized.equalsIgnoreCase(question.getAnswer());
            score = correct ? scoreValue(question) : 0;
            answer.setScore(score);
        } else if ("judgment".equals(questionType)) {
            String normalized = normalizeJudgmentAnswer(studentAnswer);
            correct = normalized != null && normalized.equalsIgnoreCase(question.getAnswer());
            score = correct ? scoreValue(question) : 0;
            answer.setScore(score);
            answer.setStudentAnswer(normalized);
        } else if ("typing".equals(questionType)) {
            score = calculateTypingScore(question, studentAnswer, request);
            correct = score >= scoreValue(question) * 0.6;
            answer.setScore(score);
            applyTypingStats(answer, request, question.getQuestionId());
        } else if ("practical".equals(questionType)) {
            applyPracticalPreview(answer, studentAnswer);
            answer.setFilePath(studentAnswer);
            answer.setScore(StringUtils.isEmpty(studentAnswer) && finalSubmit ? 0 : null);
            answer.setGradingStatus(StringUtils.isEmpty(studentAnswer) ? "1" : "0");
        }
        answer.setIsCorrect(Boolean.TRUE.equals(correct) ? 1 : 0);
        return answer;
    }

    private void applyTypingStats(CountyExamAnswer answer, CountyExamSubmitRequest request, Long questionId) {
        if (request.getTypingStats() == null || request.getTypingStats().get(questionId) == null) {
            return;
        }
        CountyExamSubmitRequest.TypingStatItem stat = request.getTypingStats().get(questionId);
        answer.setTypingSpeed(stat.getTypingSpeed());
        answer.setAccuracyRate(normalizePercentage(stat.getAccuracyRate()));
        answer.setCompletionRate(normalizePercentage(stat.getCompletionRate()));
    }

    static Double normalizePercentage(Double value) {
        if (value == null || value.isNaN() || value.isInfinite()) {
            return null;
        }
        return Math.max(0D, Math.min(100D, value));
    }

    boolean isStudentWorkPath(String path, Long examId, Long studentId, Long questionId) {
        if (StringUtils.isEmpty(path) || examId == null || studentId == null || questionId == null
                || path.contains("..") || path.contains("\\") || path.contains("?") || path.contains("#")) {
            return false;
        }
        // 兼容升级前已上传且路径中带学生ID的作品；新路径使用随机标识并用 Redis 绑定上传人。
        String legacyPrefix = Constants.RESOURCE_PREFIX + "/upload/county-exam/" + examId + "/"
                + studentId + "/" + questionId + "/";
        if (path.startsWith(legacyPrefix) && path.length() > legacyPrefix.length()) {
            return true;
        }
        String randomPrefix = Constants.RESOURCE_PREFIX + "/upload/county-exam/" + examId + "/"
                + questionId + "/";
        if (!path.startsWith(randomPrefix) || path.length() <= randomPrefix.length()) {
            return false;
        }
        Object owner = redisCache.getCacheObject("student:county-exam-upload-owner:" + path);
        return owner != null && String.valueOf(studentId).equals(String.valueOf(owner));
    }

    private void applyPracticalPreview(CountyExamAnswer answer, String studentAnswer) {
        answer.setPreviewRetryCount(0);
        answer.setPreviewLastRetryTime(null);
        if (StringUtils.isEmpty(studentAnswer)) {
            answer.setPreviewStatus(null);
            answer.setPreviewPath(null);
            answer.setPreviewErrorMessage(null);
            return;
        }
        String lower = studentAnswer.toLowerCase();
        if (lower.endsWith(".docx") || lower.endsWith(".doc")) {
            answer.setPreviewStatus("pending");
            answer.setPreviewPath(null);
            answer.setPreviewErrorMessage(null);
        } else if (lower.endsWith(".pdf")) {
            answer.setPreviewStatus("success");
            answer.setPreviewPath(studentAnswer);
            answer.setPreviewErrorMessage(null);
        } else {
            answer.setPreviewStatus("failed");
            answer.setPreviewPath(null);
            answer.setPreviewErrorMessage("不支持在线预览的文件类型");
        }
    }

    private int calculateTypingScore(BizLessonQuestionDetailVo question, String studentAnswer, CountyExamSubmitRequest request) {
        String original = question.getQuestionContent();
        if (StringUtils.isEmpty(original) || studentAnswer == null) {
            return 0;
        }
        int correctCount = 0;
        int compareLength = Math.min(studentAnswer.length(), original.length());
        for (int i = 0; i < compareLength; i++) {
            if (original.charAt(i) == studentAnswer.charAt(i)) {
                correctCount++;
            }
        }
        double accuracyRate = studentAnswer.length() > 0 ? (double) correctCount / studentAnswer.length() : 0;
        int duration = question.getTypingDuration() == null || question.getTypingDuration() <= 0 ? 5 : question.getTypingDuration();
        int baseSpeed = 40;
        int targetCount = Math.min(baseSpeed * duration, original.length());
        if (targetCount <= 0) {
            targetCount = original.length();
        }
        double speedFactor = Math.min((double) correctCount / targetCount, 1.0);
        return Math.min((int) Math.round(scoreValue(question) * speedFactor * accuracyRate), scoreValue(question));
    }

    private void recomputeStudentScore(Long examId, Long studentId) {
        List<BizLessonQuestionDetailVo> questions = paperQuestionMapper.selectDetailsByExamAndStudent(examId, studentId);
        List<CountyExamAnswer> answers = answerMapper.selectByExamAndStudent(examId, studentId);
        Map<Long, CountyExamAnswer> answerMap = new HashMap<Long, CountyExamAnswer>();
        for (CountyExamAnswer answer : answers) {
            answerMap.put(answer.getQuestionId(), answer);
        }
        int theoryScore = 0;
        int typingScore = 0;
        int practicalScore = 0;
        for (BizLessonQuestionDetailVo question : questions) {
            CountyExamAnswer answer = answerMap.get(question.getQuestionId());
            int score = answer != null && answer.getScore() != null ? answer.getScore() : 0;
            if ("choice".equals(question.getQuestionType()) || "judgment".equals(question.getQuestionType())) {
                theoryScore += score;
            } else if ("typing".equals(question.getQuestionType())) {
                typingScore += score;
            } else if ("practical".equals(question.getQuestionType())) {
                practicalScore += score;
            }
        }
        CountyExamStudent student = studentMapper.selectByExamAndStudent(examId, studentId);
        if (student == null) {
            return;
        }
        studentMapper.updateScores(
                student.getId(),
                new BigDecimal(theoryScore + typingScore + practicalScore),
                new BigDecimal(theoryScore),
                new BigDecimal(practicalScore));
    }

    private void ensureParticipant(CountyExam exam, BizStudent student, Long deptId) {
        CountyExamClass examClass = null;
        List<CountyExamClass> activeClasses = classMapper.selectActiveByStudentInfo(deptId, student.getEntryYear(), student.getClassCode());
        for (CountyExamClass activeClass : activeClasses) {
            if (exam.getExamId().equals(activeClass.getExamId())) {
                examClass = activeClass;
                break;
            }
        }
        if (examClass == null || !exam.getExamId().equals(examClass.getExamId())) {
            throw new ServiceException("您不在本场区域抽测参考班级中");
        }
        CountyExamStudent existing = studentMapper.selectByExamAndStudent(exam.getExamId(), student.getStudentId());
        if (existing == null) {
            CountyExamStudent countyStudent = new CountyExamStudent();
            countyStudent.setExamId(exam.getExamId());
            countyStudent.setStudentId(student.getStudentId());
            countyStudent.setDeptId(deptId);
            countyStudent.setClassInfo(student.getEntryYear() + "级" + student.getClassCode() + "班");
            countyStudent.setTotalScore(BigDecimal.ZERO);
            countyStudent.setTheoryScore(BigDecimal.ZERO);
            countyStudent.setTechScore(BigDecimal.ZERO);
            countyStudent.setStatus("0");
            countyStudent.setAutoSubmit(AUTO_SUBMIT_NO);
            studentMapper.insertOrIgnore(countyStudent);
        }
    }

    private void validateNoActiveClassConflict(Long examId, List<CountyExamClass> classes) {
        for (CountyExamClass examClass : classes) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "select e.exam_name as examName " +
                            "from biz_county_exam_class c " +
                            "inner join biz_county_exam e on c.exam_id = e.exam_id " +
                            "where c.exam_id <> ? and c.dept_id = ? and c.entry_year = ? and c.class_code = ? " +
                            "and e.status = '1' and e.del_flag = '0' limit 1",
                    examId, examClass.getDeptId(), examClass.getEntryYear(), examClass.getClassCode());
            if (!rows.isEmpty()) {
                throw new ServiceException("参考班级已存在开启中的区域抽测：" + rows.get(0).get("examName"));
            }
        }
    }

    private int createParticipants(Long examId, List<CountyExamClass> classes) {
        int count = 0;
        for (CountyExamClass examClass : classes) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "select s.student_id as studentId " +
                            "from biz_student s inner join sys_user u on s.user_id = u.user_id " +
                            "where u.dept_id = ? and s.entry_year = ? and s.class_code = ? " +
                            "and u.del_flag = '0' and u.status = '0'",
                    examClass.getDeptId(), examClass.getEntryYear(), examClass.getClassCode());
            for (Map<String, Object> row : rows) {
                CountyExamStudent student = new CountyExamStudent();
                student.setExamId(examId);
                student.setStudentId(longValue(row.get("studentId")));
                student.setDeptId(examClass.getDeptId());
                student.setClassInfo(examClass.getEntryYear() + "级" + examClass.getClassCode() + "班");
                student.setTotalScore(BigDecimal.ZERO);
                student.setTheoryScore(BigDecimal.ZERO);
                student.setTechScore(BigDecimal.ZERO);
                student.setStatus("0");
                student.setAutoSubmit(AUTO_SUBMIT_NO);
                count += studentMapper.insertOrIgnore(student);
            }
        }
        return count;
    }

    private void ensurePaperForStudent(CountyExam exam, Long studentId) {
        if (paperQuestionMapper.countByExamAndStudent(exam.getExamId(), studentId) > 0) {
            return;
        }
        List<BizLessonQuestionDetailVo> questions = questionMapper.selectDetailsByExamId(exam.getExamId());
        List<BizLessonQuestionDetailVo> selected = selectQuestionsForStudent(questions, exam, studentId);
        List<CountyExamPaperQuestion> snapshots = new ArrayList<CountyExamPaperQuestion>();
        int order = 1;
        for (BizLessonQuestionDetailVo question : selected) {
            CountyExamPaperQuestion snapshot = new CountyExamPaperQuestion();
            snapshot.setExamId(exam.getExamId());
            snapshot.setStudentId(studentId);
            snapshot.setQuestionId(question.getQuestionId());
            snapshot.setQuestionType(question.getQuestionType());
            snapshot.setQuestionContent(question.getQuestionContent());
            snapshot.setOptionA(question.getOptionA());
            snapshot.setOptionB(question.getOptionB());
            snapshot.setOptionC(question.getOptionC());
            snapshot.setOptionD(question.getOptionD());
            snapshot.setAnswer(question.getAnswer());
            snapshot.setAnalysis(question.getAnalysis());
            snapshot.setQuestionScore(scoreValue(question));
            snapshot.setOrderNum(order++);
            snapshot.setTypingDuration(question.getTypingDuration());
            snapshot.setWordCount(question.getWordCount());
            snapshot.setFilePath(question.getFilePath());
            snapshot.setPreviewPath(question.getPreviewPath());
            snapshots.add(snapshot);
        }
        if (!snapshots.isEmpty()) {
            paperQuestionMapper.batchInsert(snapshots);
        }
    }

    private List<BizLessonQuestionDetailVo> selectQuestionsForStudent(List<BizLessonQuestionDetailVo> questions, CountyExam exam, Long studentId) {
        List<BizLessonQuestionDetailVo> typing = new ArrayList<BizLessonQuestionDetailVo>();
        List<BizLessonQuestionDetailVo> practical = new ArrayList<BizLessonQuestionDetailVo>();
        List<BizLessonQuestionDetailVo> choice = new ArrayList<BizLessonQuestionDetailVo>();
        List<BizLessonQuestionDetailVo> judgment = new ArrayList<BizLessonQuestionDetailVo>();
        for (BizLessonQuestionDetailVo question : questions) {
            if ("typing".equals(question.getQuestionType())) {
                typing.add(question);
            } else if ("practical".equals(question.getQuestionType())) {
                practical.add(question);
            } else if ("choice".equals(question.getQuestionType())) {
                choice.add(question);
            } else if ("judgment".equals(question.getQuestionType())) {
                judgment.add(question);
            }
        }
        long seed = studentId * 10000L + exam.getExamId();
        if (exam.getShuffleMode() != null && exam.getShuffleMode() > 0) {
            Collections.shuffle(choice, new java.util.Random(seed));
            Collections.shuffle(judgment, new java.util.Random(seed + 1));
        }
        if (exam.getShuffleMode() != null && exam.getShuffleMode() == 2) {
            choice = takeFirst(choice, exam.getRandomChoiceCount());
            judgment = takeFirst(judgment, exam.getRandomJudgmentCount());
        }
        List<BizLessonQuestionDetailVo> result = new ArrayList<BizLessonQuestionDetailVo>();
        result.addAll(typing);
        result.addAll(practical);
        result.addAll(choice);
        result.addAll(judgment);
        return result;
    }

    private List<BizLessonQuestionDetailVo> takeFirst(List<BizLessonQuestionDetailVo> questions, Integer count) {
        if (count == null || count <= 0 || count >= questions.size()) {
            return questions;
        }
        return new ArrayList<BizLessonQuestionDetailVo>(questions.subList(0, count));
    }

    private Map<String, Object> buildStudentExamPayload(CountyExam exam, BizStudent student, Long deptId) {
        List<BizLessonQuestionDetailVo> questions = paperQuestionMapper.selectDetailsByExamAndStudent(exam.getExamId(), student.getStudentId());
        Map<Long, String> questionTypes = new HashMap<Long, String>();
        for (BizLessonQuestionDetailVo question : questions) {
            questionTypes.put(question.getQuestionId(), question.getQuestionType());
            question.setAnswer(null);
            question.setAnalysis(null);
            question.setScoringItems(null);
        }
        List<CountyExamAnswer> answers = answerMapper.selectByExamAndStudent(exam.getExamId(), student.getStudentId());
        Map<Long, Map<String, Object>> answerMap = new HashMap<Long, Map<String, Object>>();
        for (CountyExamAnswer answer : answers) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("answer", answer.getStudentAnswer());
            item.put("previewStatus", answer.getPreviewStatus());
            item.put("previewPath", answer.getPreviewPath());
            item.put("previewErrorMessage", answer.getPreviewErrorMessage());
            item.put("typingSubmitted", "typing".equals(questionTypes.get(answer.getQuestionId())) && answer.getAnswerTime() != null);
            item.put("answerTime", answer.getAnswerTime());
            item.put("submitTime", answer.getSubmitTime());
            answerMap.put(answer.getQuestionId(), item);
        }
        SysDept dept = deptMapper.selectDeptById(deptId);
        Map<String, Object> studentInfo = new HashMap<String, Object>();
        studentInfo.put("studentId", student.getStudentId());
        studentInfo.put("studentName", student.getStudentName());
        studentInfo.put("entryYear", student.getEntryYear());
        studentInfo.put("classCode", student.getClassCode());
        studentInfo.put("deptName", dept == null ? "" : dept.getDeptName());
        studentInfo.put("gradeName", calculateGradeName(student.getEntryYear(), dept == null ? "1" : dept.getSchoolType()));
        CountyExamStudent examStudent = studentMapper.selectByExamAndStudent(exam.getExamId(), student.getStudentId());
        Date now = new Date();
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("examId", exam.getExamId());
        result.put("examName", exam.getExamName());
        result.put("status", exam.getStatus());
        result.put("durationMinutes", normalizeDurationMinutes(exam.getDurationMinutes()));
        result.put("startTime", examStudent == null ? null : examStudent.getStartTime());
        result.put("deadlineTime", examStudent == null ? null : examStudent.getDeadlineTime());
        result.put("serverTime", now);
        result.put("remainingSeconds", examStudent == null ? null : remainingSeconds(examStudent, now));
        result.put("submitted", examStudent != null && STUDENT_SUBMITTED.equals(examStudent.getStatus()));
        result.put("ended", false);
        result.put("questions", questions);
        result.put("submittedAnswers", answerMap);
        result.put("studentInfo", studentInfo);
        return result;
    }

    private void snapshotScoringItems(Long examId, List<BizLessonQuestionDetailVo> questions) {
        jdbcTemplate.update("delete from biz_county_exam_scoring_item where exam_id = ?", examId);
        for (BizLessonQuestionDetailVo question : questions) {
            if (!"practical".equals(question.getQuestionType())) {
                continue;
            }
            List<BizScoringItem> items = scoringItemMapper.selectItemsByQuestion(question.getQuestionId());
            for (BizScoringItem item : items) {
                jdbcTemplate.update(
                        "insert into biz_county_exam_scoring_item (exam_id, question_id, source_item_id, item_name, item_score, order_num) values (?, ?, ?, ?, ?, ?)",
                        examId, question.getQuestionId(), item.getItemId(), item.getItemName(), item.getItemScore(), item.getOrderNum());
            }
        }
    }

    private List<CountyExamScoringItemVo> selectCountyScoringItems(
            Long examId, Long questionId, int questionScore) {
        if (examId == null || questionId == null) {
            return new ArrayList<CountyExamScoringItemVo>();
        }
        List<BizScoringItem> items = jdbcTemplate.query(
                "select item_id, question_id, item_name, item_score, order_num from biz_county_exam_scoring_item where exam_id = ? and question_id = ? order by order_num asc",
                new Object[]{examId, questionId},
                (rs, rowNum) -> {
                    BizScoringItem item = new BizScoringItem();
                    item.setItemId(rs.getLong("item_id"));
                    item.setQuestionId(rs.getLong("question_id"));
                    item.setItemName(rs.getString("item_name"));
                    item.setItemScore(rs.getInt("item_score"));
                    item.setOrderNum(rs.getInt("order_num"));
                    return item;
                });
        return buildCountyScoringItems(items, questionScore);
    }

    static List<CountyExamScoringItemVo> buildCountyScoringItems(
            List<BizScoringItem> items, int questionScore) {
        List<CountyExamScoringItemVo> result = new ArrayList<CountyExamScoringItemVo>();
        if (items == null || items.isEmpty() || questionScore < 0) {
            return result;
        }
        int totalWeight = 0;
        for (BizScoringItem item : items) {
            if (item == null || item.getItemScore() == null || item.getItemScore() < 0) {
                return result;
            }
            totalWeight += item.getItemScore();
        }
        // 历史异常权重不参与分项评分，教师仍可切换为直接打分。
        if (totalWeight != 100) {
            return result;
        }

        List<Integer> maxScores = new ArrayList<Integer>();
        List<Integer> remainders = new ArrayList<Integer>();
        List<Integer> indexes = new ArrayList<Integer>();
        int allocated = 0;
        for (int i = 0; i < items.size(); i++) {
            long weightedScore = (long) items.get(i).getItemScore() * questionScore;
            int maxScore = (int) (weightedScore / 100L);
            maxScores.add(maxScore);
            remainders.add((int) (weightedScore % 100L));
            indexes.add(i);
            allocated += maxScore;
        }
        Collections.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(Integer left, Integer right) {
                int remainderCompare = Integer.compare(remainders.get(right), remainders.get(left));
                return remainderCompare != 0 ? remainderCompare : Integer.compare(left, right);
            }
        });
        for (int i = 0; i < questionScore - allocated; i++) {
            int index = indexes.get(i);
            maxScores.set(index, maxScores.get(index) + 1);
        }

        for (int i = 0; i < items.size(); i++) {
            BizScoringItem item = items.get(i);
            CountyExamScoringItemVo vo = new CountyExamScoringItemVo();
            vo.setItemId(item.getItemId());
            vo.setQuestionId(item.getQuestionId());
            vo.setItemName(item.getItemName());
            vo.setWeightPercent(item.getItemScore());
            vo.setMaxScore(maxScores.get(i));
            vo.setOrderNum(item.getOrderNum());
            result.add(vo);
        }
        return result;
    }

    static void validateCountyScoringDetails(
            CountyExamGradeRequest request, List<CountyExamScoringItemVo> scoringItems) {
        if (request.getScoringDetails() == null) {
            return;
        }
        if (scoringItems == null || scoringItems.isEmpty()) {
            throw new ServiceException("当前题目没有有效评分项，请使用直接打分");
        }
        if (request.getScoringDetails().size() != scoringItems.size()) {
            throw new ServiceException("请完成全部评分项");
        }
        Map<Long, Integer> maxScores = new HashMap<Long, Integer>();
        for (CountyExamScoringItemVo item : scoringItems) {
            maxScores.put(item.getItemId(), item.getMaxScore());
        }
        Set<Long> submittedItemIds = new HashSet<Long>();
        int detailTotal = 0;
        for (CountyExamGradeRequest.ScoringDetailRequest detail : request.getScoringDetails()) {
            if (detail == null || detail.getItemId() == null || detail.getScore() == null) {
                throw new ServiceException("评分项参数不完整");
            }
            Integer maxScore = maxScores.get(detail.getItemId());
            if (maxScore == null || !submittedItemIds.add(detail.getItemId())) {
                throw new ServiceException("评分项不属于当前抽测题目或存在重复");
            }
            if (detail.getScore() < 0 || detail.getScore() > maxScore) {
                throw new ServiceException("分项得分超出允许范围");
            }
            detailTotal += detail.getScore();
        }
        if (detailTotal != request.getScore()) {
            throw new ServiceException("分项得分合计必须与总分一致");
        }
    }

    private List<Map<String, Object>> selectCountyGradingDetails(Long answerId) {
        return jdbcTemplate.queryForList(
                "select detail_id as detailId, answer_id as answerId, item_id as itemId, score from biz_county_exam_grading_detail where answer_id = ?",
                answerId);
    }

    private void saveCountyGradingDetails(CountyExamGradeRequest request) {
        jdbcTemplate.update("delete from biz_county_exam_grading_detail where answer_id = ?", request.getAnswerId());
        if (request.getScoringDetails() == null) {
            return;
        }
        for (CountyExamGradeRequest.ScoringDetailRequest detail : request.getScoringDetails()) {
            jdbcTemplate.update(
                    "insert into biz_county_exam_grading_detail (answer_id, item_id, score) values (?, ?, ?)",
                    request.getAnswerId(), detail.getItemId(), detail.getScore());
        }
    }

    private void refreshGraderCount(Long examId, Long questionId, Long graderId) {
        Integer gradedCount = jdbcTemplate.queryForObject(
                "select count(1) from biz_county_exam_answer where exam_id = ? and question_id = ? and grader_id = ? and grading_status = '1'",
                Integer.class, examId, questionId, graderId);
        CountyExamGrader grader = graderMapper.selectByExamQuestionAndGrader(examId, questionId, graderId);
        if (grader != null) {
            grader.setGradedCount(gradedCount == null ? 0 : gradedCount);
            graderMapper.update(grader);
        }
    }

    private int countAssignedPracticalAnswers(Long examId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) " +
                        "from biz_county_exam_answer a " +
                        "inner join biz_county_exam_paper_question pq " +
                        "on pq.exam_id = a.exam_id and pq.student_id = a.student_id and pq.question_id = a.question_id " +
                        "where a.exam_id = ? and pq.question_type = 'practical' " +
                        "and a.student_answer is not null and a.student_answer != '' " +
                        "and a.grader_id is not null",
                Integer.class, examId);
        return count == null ? 0 : count;
    }

    private Map<String, Object> buildGradingProgress(Long examId) {
        Map<String, Object> progress = new HashMap<String, Object>();
        Integer participantCount = jdbcTemplate.queryForObject(
                "select count(1) from biz_county_exam_student where exam_id = ?",
                Integer.class, examId);
        List<Map<String, Object>> questionRows = jdbcTemplate.queryForList(
                "select pq.question_id as questionId, " +
                        "count(distinct pq.student_id) as participantCount, " +
                        "count(distinct case when a.student_answer is not null and a.student_answer != '' then a.answer_id end) as submittedCount, " +
                        "count(distinct case when a.student_answer is not null and a.student_answer != '' and a.grader_id is not null then a.answer_id end) as assignedCount, " +
                        "count(distinct case when a.student_answer is not null and a.student_answer != '' and a.grading_status = '1' and a.score is not null then a.answer_id end) as gradedCount " +
                        "from biz_county_exam_paper_question pq " +
                        "left join biz_county_exam_answer a " +
                        "on a.exam_id = pq.exam_id and a.student_id = pq.student_id and a.question_id = pq.question_id " +
                        "where pq.exam_id = ? and pq.question_type = 'practical' " +
                        "group by pq.question_id order by pq.question_id asc",
                examId);
        Map<Long, Map<String, Object>> byQuestion = new LinkedHashMap<Long, Map<String, Object>>();
        int submittedTotal = 0;
        int assignedTotal = 0;
        int gradedTotal = 0;
        for (Map<String, Object> row : questionRows) {
            Long questionId = longValue(row.get("questionId"));
            int submittedCount = intValue(row.get("submittedCount"));
            int assignedCount = intValue(row.get("assignedCount"));
            int gradedCount = intValue(row.get("gradedCount"));
            row.put("pendingCount", Math.max(submittedCount - gradedCount, 0));
            byQuestion.put(questionId, row);
            submittedTotal += submittedCount;
            assignedTotal += assignedCount;
            gradedTotal += gradedCount;
        }
        progress.put("participantCount", participantCount == null ? 0 : participantCount);
        progress.put("questionProgress", questionRows);
        progress.put("questionProgressMap", byQuestion);
        progress.put("submittedCount", submittedTotal);
        progress.put("assignedCount", assignedTotal);
        progress.put("gradedCount", gradedTotal);
        progress.put("pendingCount", Math.max(submittedTotal - gradedTotal, 0));
        return progress;
    }

    private void triggerCountyPreviewConversionsAfterCommit(List<Long> answerIds) {
        if (answerIds == null || answerIds.isEmpty()) {
            return;
        }
        // afterCommit 仅调度领取，不在交卷线程上同步 claim / LibreOffice
        final List<Long> finalAnswerIds = new ArrayList<Long>(answerIds);
        Runnable runnable = () -> {
            for (Long answerId : finalAnswerIds) {
                asyncConversionService.scheduleCountyExamPreviewConversion(answerId);
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runnable.run();
                }
            });
            return;
        }
        runnable.run();
    }

    private void updateTotalScore(Long examId, int totalScore) {
        CountyExam exam = new CountyExam();
        exam.setExamId(examId);
        exam.setTotalScore(totalScore);
        exam.setUpdateBy(SecurityUtils.getUsername());
        countyExamMapper.updateCountyExam(exam);
    }

    private void normalizeEditableExam(CountyExam target, CountyExam fallback) {
        if (target == null) {
            throw new ServiceException("区域抽测参数不能为空");
        }
        if (target.getExamName() == null && fallback != null) {
            target.setExamName(fallback.getExamName());
        }
        if (StringUtils.isEmpty(target.getExamName())) {
            throw new ServiceException("区域抽测名称不能为空");
        }
        target.setExamName(target.getExamName().trim());
        if (StringUtils.isEmpty(target.getExamName())) {
            throw new ServiceException("区域抽测名称不能为空");
        }

        if (target.getSchoolType() == null && fallback != null) {
            target.setSchoolType(fallback.getSchoolType());
        }
        if (target.getExamGrade() == null && fallback != null) {
            target.setExamGrade(fallback.getExamGrade());
        }
        gradeInSection(target.getSchoolType(), target.getExamGrade());

        if (target.getShuffleMode() == null) {
            target.setShuffleMode(fallback == null || fallback.getShuffleMode() == null
                    ? 0 : fallback.getShuffleMode());
        }
        if (target.getShuffleMode() < 0 || target.getShuffleMode() > 2) {
            throw new ServiceException("出题模式无效");
        }
        if (target.getRandomChoiceCount() == null) {
            target.setRandomChoiceCount(fallback == null || fallback.getRandomChoiceCount() == null
                    ? 0 : fallback.getRandomChoiceCount());
        }
        if (target.getRandomJudgmentCount() == null) {
            target.setRandomJudgmentCount(fallback == null || fallback.getRandomJudgmentCount() == null
                    ? 0 : fallback.getRandomJudgmentCount());
        }
        if (target.getRandomChoiceCount() < 0 || target.getRandomJudgmentCount() < 0) {
            throw new ServiceException("随机抽题数量不能为负数");
        }
        if (target.getShuffleMode() != 2) {
            target.setRandomChoiceCount(0);
            target.setRandomJudgmentCount(0);
        }

        if (target.getDurationMinutes() == null) {
            target.setDurationMinutes(fallback == null || fallback.getDurationMinutes() == null
                    ? DEFAULT_DURATION_MINUTES : fallback.getDurationMinutes());
        }
        if (target.getDurationMinutes() <= 0) {
            throw new ServiceException("作答时长必须大于 0 分钟");
        }
    }

    private void validateQuestionPayload(List<CountyExamQuestion> questions) {
        Set<Long> questionIds = new HashSet<Long>();
        for (CountyExamQuestion question : questions) {
            if (question == null || question.getQuestionId() == null || question.getQuestionId() <= 0) {
                throw new ServiceException("组卷中存在无效题目");
            }
            if (!questionIds.add(question.getQuestionId())) {
                throw new ServiceException("同一道题不能重复加入区域抽测试卷");
            }
            if (question.getQuestionScore() == null || question.getQuestionScore() <= 0) {
                throw new ServiceException("题目分值必须大于 0");
            }
        }
    }

    static int calculateEffectiveTotalScore(CountyExam exam, List<BizLessonQuestionDetailVo> questions) {
        if (exam == null || questions == null || questions.isEmpty()) {
            return 0;
        }
        List<Integer> choiceScores = new ArrayList<Integer>();
        List<Integer> judgmentScores = new ArrayList<Integer>();
        long fixedScore = 0L;
        for (BizLessonQuestionDetailVo question : questions) {
            if (question == null || question.getQuestionId() == null
                    || StringUtils.isEmpty(question.getQuestionType())
                    || question.getQuestionScore() == null
                    || question.getQuestionScore().longValue() <= 0L
                    || question.getQuestionScore().longValue() > Integer.MAX_VALUE) {
                throw new ServiceException("组卷包含不存在的题目或无效分值");
            }
            int score = question.getQuestionScore().intValue();
            if ("choice".equals(question.getQuestionType())) {
                choiceScores.add(score);
            } else if ("judgment".equals(question.getQuestionType())) {
                judgmentScores.add(score);
            } else if ("typing".equals(question.getQuestionType())
                    || "practical".equals(question.getQuestionType())) {
                fixedScore += score;
            } else {
                throw new ServiceException("组卷包含不支持的题型：" + question.getQuestionType());
            }
        }

        int shuffleMode = exam.getShuffleMode() == null ? 0 : exam.getShuffleMode();
        if (shuffleMode < 0 || shuffleMode > 2) {
            throw new ServiceException("出题模式无效");
        }
        long total = fixedScore;
        if (shuffleMode == 2) {
            total += calculateRandomTypeScore(
                    choiceScores, exam.getRandomChoiceCount(), "选择题");
            total += calculateRandomTypeScore(
                    judgmentScores, exam.getRandomJudgmentCount(), "判断题");
        } else {
            total += sumScores(choiceScores);
            total += sumScores(judgmentScores);
        }
        if (total > Integer.MAX_VALUE) {
            throw new ServiceException("区域抽测试卷总分超出允许范围");
        }
        return (int) total;
    }

    private static long calculateRandomTypeScore(List<Integer> scores, Integer configuredCount,
                                                 String typeName) {
        int count = configuredCount == null ? 0 : configuredCount;
        if (count < 0 || count > scores.size()) {
            throw new ServiceException(typeName + "随机抽取数量超过可用题数");
        }
        if (count == 0) {
            return sumScores(scores);
        }
        int firstScore = scores.get(0);
        for (Integer score : scores) {
            if (score == null || score.intValue() != firstScore) {
                throw new ServiceException("随机抽题模式下，同一题型的分值必须一致：" + typeName);
            }
        }
        return (long) count * firstScore;
    }

    private static long sumScores(List<Integer> scores) {
        long total = 0L;
        for (Integer score : scores) {
            total += score;
        }
        return total;
    }

    private void validateClassesForExam(CountyExam exam, List<CountyExamClass> classes) {
        String expectedEntryYear = expectedEntryYear(exam, Calendar.getInstance());
        Set<Long> schoolIds = new HashSet<Long>();
        for (CountyExamClass examClass : classes) {
            if (examClass == null || examClass.getDeptId() == null || examClass.getDeptId() <= 0
                    || StringUtils.isEmpty(examClass.getEntryYear())
                    || StringUtils.isEmpty(examClass.getClassCode())) {
                throw new ServiceException("参考班级参数不完整");
            }
            if (StringUtils.isNotEmpty(examClass.getType()) && !"1".equals(examClass.getType())) {
                throw new ServiceException("区域抽测目前只支持行政班");
            }
            if (!schoolIds.add(examClass.getDeptId())) {
                throw new ServiceException("每所学校只能选择一个参考班级");
            }
            String entryYear = examClass.getEntryYear().trim();
            String classCode = examClass.getClassCode().trim();
            if (!expectedEntryYear.equals(entryYear)) {
                throw new ServiceException("参考班级入学年份与抽测年级不匹配，应为 " + expectedEntryYear + " 级");
            }
            Integer count = jdbcTemplate.queryForObject(
                    "select count(1) " +
                            "from biz_student s " +
                            "inner join sys_user u on u.user_id = s.user_id " +
                            "inner join sys_dept d on d.dept_id = u.dept_id " +
                            "where d.dept_id = ? and d.school_type = ? " +
                            "and d.del_flag = '0' and d.status = '0' " +
                            "and u.del_flag = '0' and u.status = '0' " +
                            "and s.entry_year = ? and s.class_code = ?",
                    Integer.class, examClass.getDeptId(), exam.getSchoolType(), entryYear, classCode);
            if (count == null || count <= 0) {
                throw new ServiceException("参考班级不存在、学段不匹配或没有有效学生");
            }
        }
    }

    static String expectedEntryYear(CountyExam exam, Calendar now) {
        int gradeInSection = gradeInSection(exam.getSchoolType(), exam.getExamGrade());
        return String.valueOf(resolveAcademicStartYear(now) - gradeInSection + 1);
    }

    private static int gradeInSection(String schoolType, Integer examGrade) {
        if (examGrade == null) {
            throw new ServiceException("抽测年级不能为空");
        }
        if ("1".equals(schoolType) && examGrade >= 1 && examGrade <= 6) {
            return examGrade;
        }
        if ("2".equals(schoolType) && examGrade >= 7 && examGrade <= 9) {
            return examGrade - 6;
        }
        if ("3".equals(schoolType) && examGrade >= 10 && examGrade <= 12) {
            return examGrade - 9;
        }
        throw new ServiceException("学段与抽测年级不匹配");
    }

    static int resolveAcademicStartYear(Calendar now) {
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1;
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        return currentMonth > 7 || (currentMonth == 7 && currentDay >= 20)
                ? currentYear : currentYear - 1;
    }

    private void requireManager() {
        if (!SecurityUtils.hasRole("admin") && !SecurityUtils.hasRole("researcher")) {
            throw new ServiceException("只有管理员或教研员可以操作区域抽测");
        }
    }

    private CountyExam requireExam(Long examId) {
        if (examId == null) {
            throw new ServiceException("区域抽测ID不能为空");
        }
        CountyExam exam = countyExamMapper.selectCountyExamById(examId);
        if (exam == null) {
            throw new ServiceException("区域抽测不存在");
        }
        return exam;
    }

    private CountyExam requireExamForUpdate(Long examId) {
        if (examId == null) {
            throw new ServiceException("区域抽测ID不能为空");
        }
        CountyExam exam = countyExamMapper.selectCountyExamByIdForUpdate(examId);
        if (exam == null) {
            throw new ServiceException("区域抽测不存在");
        }
        return exam;
    }

    private void requireDraft(CountyExam exam) {
        if (!STATUS_DRAFT.equals(exam.getStatus())) {
            throw new ServiceException("只有草稿状态的区域抽测可以修改");
        }
    }

    private Map<String, Object> noStudentExam() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("hasExam", false);
        return result;
    }

    private int scoreValue(BizLessonQuestionDetailVo question) {
        return question.getQuestionScore() == null ? 0 : question.getQuestionScore().intValue();
    }

    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private int intValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private String normalizeJudgmentAnswer(String answer) {
        if (answer == null) {
            return null;
        }
        String trimmed = answer.trim();
        if ("对".equals(trimmed) || "正确".equals(trimmed) || "T".equalsIgnoreCase(trimmed) || "true".equalsIgnoreCase(trimmed)) {
            return "T";
        }
        if ("错".equals(trimmed) || "错误".equals(trimmed) || "F".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
            return "F";
        }
        return trimmed;
    }

    private String calculateGradeName(String entryYear, String schoolType) {
        if (entryYear == null) {
            return "未知年级";
        }
        Calendar now = Calendar.getInstance();
        int yearsInSchool;
        try {
            yearsInSchool = resolveAcademicStartYear(now) - Integer.parseInt(entryYear) + 1;
        } catch (NumberFormatException e) {
            return "未知年级";
        }
        String[] gradeNames;
        if ("1".equals(schoolType)) {
            gradeNames = new String[]{"一年级", "二年级", "三年级", "四年级", "五年级", "六年级"};
        } else if ("2".equals(schoolType)) {
            gradeNames = new String[]{"七年级", "八年级", "九年级"};
        } else {
            gradeNames = new String[]{"高一", "高二", "高三"};
        }
        if (yearsInSchool >= 1 && yearsInSchool <= gradeNames.length) {
            return gradeNames[yearsInSchool - 1];
        }
        return "未知年级";
    }

    static Map<String, Object> buildAnalysisOverview(List<CountyExamStudent> participants,
                                                      int schoolCount, Integer configuredFullScore) {
        int fullScore = normalizeAnalysisFullScore(configuredFullScore);
        int participantCount = 0;
        int submittedCount = 0;
        int passCount = 0;
        double totalScore = 0D;
        Double maxScore = null;
        Double minScore = null;
        if (participants != null) {
            for (CountyExamStudent participant : participants) {
                if (participant == null) {
                    continue;
                }
                participantCount++;
                if (STUDENT_SUBMITTED.equals(participant.getStatus())) {
                    submittedCount++;
                }
                double score = participant.getTotalScore() == null
                        ? 0D : participant.getTotalScore().doubleValue();
                totalScore += score;
                maxScore = maxScore == null ? score : Math.max(maxScore, score);
                minScore = minScore == null ? score : Math.min(minScore, score);
                if (score * 100D >= fullScore * 60D) {
                    passCount++;
                }
            }
        }

        Map<String, Object> overview = new LinkedHashMap<String, Object>();
        overview.put("schoolCount", Math.max(schoolCount, 0));
        overview.put("participantCount", participantCount);
        overview.put("submittedCount", submittedCount);
        overview.put("averageScore", participantCount == 0 ? 0D : roundOneDecimal(totalScore / participantCount));
        overview.put("maxScore", maxScore == null ? 0D : roundOneDecimal(maxScore));
        overview.put("minScore", minScore == null ? 0D : roundOneDecimal(minScore));
        overview.put("passRate", participantCount == 0 ? 0D
                : roundOneDecimal(passCount * 100D / participantCount));
        overview.put("fullScore", fullScore);
        return overview;
    }

    static List<Map<String, Object>> buildScoreDistribution(List<CountyExamStudent> participants,
                                                             Integer configuredFullScore) {
        int fullScore = normalizeAnalysisFullScore(configuredFullScore);
        int[] counts = new int[10];
        if (participants != null) {
            for (CountyExamStudent participant : participants) {
                if (participant == null) {
                    continue;
                }
                double score = participant.getTotalScore() == null
                        ? 0D : participant.getTotalScore().doubleValue();
                double percentageScore = Math.max(0D, Math.min(100D, score * 100D / fullScore));
                int bucket = percentageScore >= 100D ? 9 : (int) (percentageScore / 10D);
                counts[bucket]++;
            }
        }

        List<Map<String, Object>> distribution = new ArrayList<Map<String, Object>>(10);
        for (int i = 0; i < counts.length; i++) {
            int lowerBound = i * 10;
            int upperBound = i == counts.length - 1 ? 100 : lowerBound + 9;
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("label", lowerBound + "-" + upperBound);
            item.put("lowerBound", lowerBound);
            item.put("upperBound", upperBound);
            item.put("count", counts[i]);
            distribution.add(item);
        }
        return distribution;
    }

    private static int normalizeAnalysisFullScore(Integer configuredFullScore) {
        return configuredFullScore == null || configuredFullScore <= 0 ? 100 : configuredFullScore;
    }

    private static double roundOneDecimal(double value) {
        return Math.round(value * 10D) / 10D;
    }

    private void writeCell(Row row, int index, Object value) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value == null ? "" : String.valueOf(value));
    }
}
