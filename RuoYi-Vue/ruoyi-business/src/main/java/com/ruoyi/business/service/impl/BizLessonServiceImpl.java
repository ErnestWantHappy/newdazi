package com.ruoyi.business.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.domain.BizLessonGuideSheetBinding;
import com.ruoyi.business.domain.BizLessonQuestion;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.GradeGroupVo;
import com.ruoyi.business.domain.vo.LessonDetailVo;
import com.ruoyi.business.domain.vo.LessonInfoVo;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.BizTeacherClassMapper;
import com.ruoyi.business.mapper.GuideSheetBindingMapper;
import com.ruoyi.business.service.LessonGuideSheetBindingService;
import com.ruoyi.business.util.AcademicYearUtils;
import com.ruoyi.business.domain.BizTeacherClass;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysDeptMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.service.IBizLessonService;
import org.apache.commons.lang3.StringUtils; // 新增：引入字符串工具类，便于班级编码清洗
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 课程管理Service业务层处理 (最终修正版)
 */
@Service
public class BizLessonServiceImpl implements IBizLessonService
{
    private static final Logger log = LoggerFactory.getLogger(BizLessonServiceImpl.class);

    @Autowired
    private BizLessonMapper bizLessonMapper;

    @Autowired
    private BizStudentMapper bizStudentMapper;

    @Autowired
    private SysDeptMapper deptMapper;

    @Autowired
    private BizLessonQuestionMapper lessonQuestionMapper;

    @Autowired
    private BizLessonAssignmentMapper lessonAssignmentMapper;

    @Autowired
    private BizTeacherClassMapper teacherClassMapper;

    @Autowired
    private com.ruoyi.business.mapper.BizQuestionMapper bizQuestionMapper;

    @Autowired
    private GuideSheetBindingMapper guideSheetBindingMapper;

    @Autowired
    private LessonGuideSheetBindingService lessonGuideSheetBindingService;

    @Override
    public BizLesson selectBizLessonByLessonId(Long lessonId)
    {
        BizLesson lesson = bizLessonMapper.selectBizLessonByLessonId(lessonId);
        assertCanManageLesson(lesson);
        return lesson;
    }

    @Override
    public List<BizLesson> selectBizLessonList(BizLesson bizLesson)
    {
        bizLesson.setDeptId(SecurityUtils.getDeptId());
        return bizLessonMapper.selectBizLessonList(bizLesson);
    }

    @Override
    public int insertBizLesson(BizLesson bizLesson)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isBlank(bizLesson.getEntryYear()))
        {
            bizLesson.setEntryYear(calculateEntryYearFromGrade(bizLesson.getGrade()));
        }
        bizLesson.setCreatorId(loginUser.getUserId());
        bizLesson.setDeptId(loginUser.getDeptId());
        bizLesson.setCreateBy(loginUser.getUsername());
        if (bizLesson.getCreateTime() == null) {
            bizLesson.setCreateTime(new Date());
        }
        return bizLessonMapper.insertBizLesson(bizLesson);
    }

    @Override
    public int updateBizLesson(BizLesson bizLesson)
    {
        BizLesson existing = bizLessonMapper.selectBizLessonByLessonId(bizLesson.getLessonId());
        assertCanManageLesson(existing);
        preserveLessonEntryYear(existing, bizLesson.getEntryYear());
        bizLesson.setEntryYear(existing.getEntryYear());
        bizLesson.setCreatorId(existing.getCreatorId());
        bizLesson.setDeptId(existing.getDeptId());
        bizLesson.setUpdateBy(SecurityUtils.getUsername());
        bizLesson.setUpdateTime(new Date());
        int affected = bizLessonMapper.updateBizLesson(bizLesson);
        if (affected != 1)
        {
            throw new ServiceException("课程已被其他操作修改或删除，请刷新后重试");
        }
        return affected;
    }

    @Override
    @Transactional
    public int deleteBizLessonByLessonIds(Long[] lessonIds)
    {
        for (Long lessonId : lessonIds) {
            assertCanManageLesson(bizLessonMapper.selectBizLessonByLessonId(lessonId));
            assertLessonHasNoGuideSheetHistory(lessonId);
            // 级联删除关联数据
            lessonQuestionMapper.deleteByLessonId(lessonId);
            lessonAssignmentMapper.deleteByLessonId(lessonId);
        }
        int affected = bizLessonMapper.deleteBizLessonByLessonIds(lessonIds);
        if (affected != lessonIds.length)
        {
            throw new ServiceException("部分课程已发生变化，删除已取消，请刷新后重试");
        }
        return affected;
    }

    @Override
    @Transactional
    public int deleteBizLessonByLessonId(Long lessonId)
    {
        assertCanManageLesson(bizLessonMapper.selectBizLessonByLessonId(lessonId));
        assertLessonHasNoGuideSheetHistory(lessonId);
        int affected = bizLessonMapper.deleteBizLessonByLessonId(lessonId);
        if (affected != 1)
        {
            throw new ServiceException("课程已发生变化，删除已取消，请刷新后重试");
        }
        return affected;
    }

    @Override
    public List<GradeGroupVo> getTeacherDashboardData() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long deptId = null;
        if (loginUser != null && loginUser.getUser() != null)
        {
            deptId = loginUser.getUser().getDeptId();
        }
        if (deptId == null)
        {
            log.warn("【教师首页数据】当前教师未绑定校区，无法加载首页数据");
            return new ArrayList<>();
        }
        String username = loginUser != null ? loginUser.getUsername() : "unknown";
        log.info("【教师首页数据】开始获取数据，教师: {}, 学校ID: {}", username, deptId);

        SysDept school = deptMapper.selectDeptById(deptId);
        if (school == null) {
            log.warn("【教师首页数据】无法找到教师关联的学校信息，DeptId: {}", deptId);
            return new ArrayList<>();
        }
        String schoolType = school.getSchoolType();
        log.info("【教师首页数据】学校类型: {}", schoolType);

        // 改为从教师管理的班级表（biz_teacher_class）获取年级分组，而不是全校学生
        BizTeacherClass tcQuery = new BizTeacherClass();
        tcQuery.setUserId(loginUser.getUserId());
        tcQuery.setDeptId(deptId);
        List<BizTeacherClass> managedClasses = teacherClassMapper.selectBizTeacherClassList(tcQuery);
        log.info("【教师首页数据】步骤1: 从 biz_teacher_class 查询到 {} 条该教师管理的班级记录。", managedClasses.size());

        Map<String, List<String>> yearClassMap = managedClasses.stream()
                .filter(Objects::nonNull)
                .filter(tc -> tc.getEntryYear() != null && !tc.getEntryYear().isEmpty())
                .collect(Collectors.groupingBy(
                        BizTeacherClass::getEntryYear,
                        Collectors.mapping(tc -> tc.getClassCode() + "班", 
                            Collectors.collectingAndThen(Collectors.toList(), 
                                list -> list.stream().distinct().collect(Collectors.toList())))
                ));
        log.info("【教师首页数据】步骤2: 成功按入学年份分组，共 {} 个年份组。", yearClassMap.size());

        List<GradeGroupVo> result = new ArrayList<>();
        for (String entryYear : yearClassMap.keySet()) {
            log.info("【教师首页数据】步骤3: 正在处理入学年份: {}", entryYear);
            GradeGroupVo gradeGroup = new GradeGroupVo();
            gradeGroup.setEntryYear(entryYear);

            Map<String, Long> gradeInfo = calculateGradeInfo(entryYear, schoolType);
            gradeGroup.setGradeName(gradeInfo.keySet().iterator().next());
            Long currentGradeId = gradeInfo.values().iterator().next();
            gradeGroup.setGradeId(currentGradeId);
            log.info("【教师首页数据】计算得出年级为: {}, 年级ID: {}", gradeGroup.getGradeName(), currentGradeId);

            gradeGroup.setAllClassesInGrade(yearClassMap.get(entryYear));

            // 按稳定 entry_year 装课：已毕业(gradeId=-1)/新生(0) 也必须查课，
            // 禁止用「当前年级号 > 0」误杀历史届课程卡片。
            List<LessonInfoVo> selfLessons = bizLessonMapper.selectLessonsByEntryYearAndCreator(entryYear, username, deptId);
            log.info("【教师首页数据】届别 {}（{}）自建课程 {} 门。", entryYear, gradeGroup.getGradeName(),
                    selfLessons == null ? 0 : selfLessons.size());

            List<LessonInfoVo> sharedLessons = bizLessonMapper.selectSharedLessonsByEntryYearAndUser(
                    entryYear, loginUser.getUserId(), deptId, username);
            log.info("【教师首页数据】届别 {} 共享课程 {} 门。", entryYear,
                    sharedLessons == null ? 0 : sharedLessons.size());

            List<LessonInfoVo> allLessons = new ArrayList<>();
            if (selfLessons != null) {
                allLessons.addAll(selfLessons);
            }
            if (sharedLessons != null) {
                allLessons.addAll(sharedLessons);
            }
            // 自建和共享课程合并后统一排序，确保最新课程固定在左上角。
            allLessons.sort((a, b) -> {
                long timeA = a.getCreateTime() == null ? 0L : a.getCreateTime().getTime();
                long timeB = b.getCreateTime() == null ? 0L : b.getCreateTime().getTime();
                int byCreateTime = Long.compare(timeB, timeA);
                if (byCreateTime != 0) {
                    return byCreateTime;
                }
                long idA = a.getLessonId() == null ? 0L : a.getLessonId();
                long idB = b.getLessonId() == null ? 0L : b.getLessonId();
                return Long.compare(idB, idA);
            });

            for (LessonInfoVo lesson : allLessons) {
                List<String> classCodes = lessonAssignmentMapper.selectClassCodesByLessonIdAndEntryYear(
                        lesson.getLessonId(), entryYear);
                if (classCodes != null && !classCodes.isEmpty()) {
                    List<String> formattedCodes = classCodes.stream()
                        .filter(StringUtils::isNotBlank)
                        .map(code -> code.endsWith("班") ? code : code + "班")
                        .collect(Collectors.toList());
                    lesson.setAssignedClasses(formattedCodes);
                }
            }

            gradeGroup.setLessons(allLessons);

            result.add(gradeGroup);
        }

        result.sort((a, b) -> b.getEntryYear().compareTo(a.getEntryYear()));
        log.info("【教师首页数据】数据组装完成，共返回 {} 个年级组的数据。", result.size());

        return result;
    }

    @Override
    public LessonDetailVo selectLessonDetailsByLessonId(Long lessonId) {
        LessonDetailVo detailVo = new LessonDetailVo();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long deptId = null;
        if (loginUser != null && loginUser.getUser() != null)
        {
            deptId = loginUser.getUser().getDeptId();
        }
        if (deptId == null)
        {
            log.warn("【课程详情】当前教师未绑定校区，无法查询课程详情");
            return null;
        }

        BizLesson lesson = bizLessonMapper.selectBizLessonByLessonId(lessonId);
        if (lesson == null) return null;
        assertCanManageLesson(lesson);

        List<BizLessonQuestionDetailVo> questions = lessonQuestionMapper.selectDetailsByLessonId(lessonId);
        List<String> assignedClassCodes = lessonAssignmentMapper.selectClassCodesByLessonIdAndEntryYear(
                lessonId, lesson.getEntryYear());
        if (CollectionUtils.isEmpty(assignedClassCodes)) {
            assignedClassCodes = new ArrayList<>();
        } else {
            // 新增：为班级编码补充“班”字，保持与前端复选框一致
            assignedClassCodes = assignedClassCodes.stream()
                    .filter(StringUtils::isNotBlank)
                    .map(code -> code.endsWith("班") ? code : code + "班")
                    .distinct()
                    .collect(Collectors.toList());
        }

        BizTeacherClass classQuery = new BizTeacherClass();
        classQuery.setUserId(loginUser.getUserId());
        classQuery.setDeptId(deptId);
        classQuery.setEntryYear(lesson.getEntryYear());
        List<String> allClassesInGrade = teacherClassMapper.selectBizTeacherClassList(classQuery).stream()
                .map(BizTeacherClass::getClassCode)
                .filter(StringUtils::isNotBlank)
                .map(code -> code.endsWith("班") ? code : code + "班")
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        detailVo.setLessonId(lesson.getLessonId());
        detailVo.setLessonTitle(lesson.getLessonTitle());
        detailVo.setGrade(lesson.getGrade());
        detailVo.setEntryYear(lesson.getEntryYear());
        detailVo.setSemester(lesson.getSemester());
        detailVo.setLessonNum(lesson.getLessonNum());
        detailVo.setShuffleMode(lesson.getShuffleMode() != null ? lesson.getShuffleMode() : 0);
        detailVo.setRandomChoiceCount(lesson.getRandomChoiceCount() != null ? lesson.getRandomChoiceCount() : 0);
        detailVo.setRandomJudgmentCount(lesson.getRandomJudgmentCount() != null ? lesson.getRandomJudgmentCount() : 0);
        // 历史课无 lesson_mode 时按测评课处理
        detailVo.setLessonMode(normalizeLessonMode(lesson.getLessonMode()));
        detailVo.setTeacherNote(lesson.getTeacherNote());
        // 考勤课不暴露自动推进配置；测评课返回库内配置（默认关）
        boolean attendanceDetail = "attendance".equals(normalizeLessonMode(lesson.getLessonMode()));
        detailVo.setAutoAdvanceEnabled(attendanceDetail ? Boolean.FALSE : Boolean.TRUE.equals(lesson.getAutoAdvanceEnabled()));
        detailVo.setAutoAdvanceThresholdPct(lesson.getAutoAdvanceThresholdPct() != null ? lesson.getAutoAdvanceThresholdPct() : 50);
        detailVo.setAutoAdvanceDelayHours(lesson.getAutoAdvanceDelayHours() != null
                ? lesson.getAutoAdvanceDelayHours() : new java.math.BigDecimal("2.0"));
        detailVo.setQuestions(questions);
        detailVo.setAssignedClassCodes(assignedClassCodes);
        detailVo.setAllClassesInGrade(allClassesInGrade);
        BizLessonGuideSheetBinding binding = guideSheetBindingMapper.selectCurrentByLessonId(lessonId);
        detailVo.setGuideSheetBinding(binding);
        detailVo.setGuideSheetEnabled(binding != null && "Y".equals(binding.getEnabled()));
        detailVo.setSourceSheetId(binding == null ? null : binding.getSourceSheetId());

        return detailVo;
    }

    @Override
    @Transactional
    public LessonDetailVo saveLessonDetails(LessonDetailVo lessonDetailVo) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        String username = loginUser.getUsername();
        Long userId = loginUser.getUserId();
        Long deptId = loginUser.getUser().getDeptId();

        BizLesson existingLesson = null;
        if (lessonDetailVo.getLessonId() != null)
        {
            existingLesson = bizLessonMapper.selectBizLessonByLessonId(lessonDetailVo.getLessonId());
            assertCanManageLesson(existingLesson);
        }
        validateLessonContent(lessonDetailVo);

        String entryYear = StringUtils.trimToNull(lessonDetailVo.getEntryYear());
        if (existingLesson != null)
        {
            preserveLessonEntryYear(existingLesson, entryYear);
            entryYear = existingLesson.getEntryYear();
        }
        if (StringUtils.isBlank(entryYear))
        {
            entryYear = calculateEntryYearFromGrade(lessonDetailVo.getGrade());
        }
        validateEntryYear(entryYear);
        lessonDetailVo.setEntryYear(entryYear);

        BizLesson lessonToSave = new BizLesson();
        lessonToSave.setLessonId(lessonDetailVo.getLessonId());
        lessonToSave.setDeptId(deptId);
        lessonToSave.setLessonTitle(lessonDetailVo.getLessonTitle());
        lessonToSave.setGrade(lessonDetailVo.getGrade());
        lessonToSave.setEntryYear(entryYear);
        lessonToSave.setSemester(lessonDetailVo.getSemester());
        Integer lessonNum = lessonDetailVo.getLessonNum();
        if (lessonToSave.getLessonId() == null && (lessonNum == null || lessonNum <= 0)) {
            Integer maxLessonNum = bizLessonMapper.selectMaxLessonNumByEntryYearAndCreator(
                    entryYear, username, deptId);
            lessonNum = (maxLessonNum == null ? 0 : maxLessonNum) + 1;
        }
        lessonToSave.setLessonNum(lessonNum);
        lessonToSave.setShuffleMode(lessonDetailVo.getShuffleMode() != null ? lessonDetailVo.getShuffleMode() : 0);
        lessonToSave.setRandomChoiceCount(lessonDetailVo.getRandomChoiceCount() != null ? lessonDetailVo.getRandomChoiceCount() : 0);
        lessonToSave.setRandomJudgmentCount(lessonDetailVo.getRandomJudgmentCount() != null ? lessonDetailVo.getRandomJudgmentCount() : 0);
        String normalizedMode = normalizeLessonMode(lessonDetailVo.getLessonMode());
        lessonToSave.setLessonMode(normalizedMode);
        // 允许清空教师说明
        lessonToSave.setTeacherNote(lessonDetailVo.getTeacherNote() == null ? "" : lessonDetailVo.getTeacherNote().trim());
        // 推进策略：考勤强制关；常规课一律采用教师统一策略（不在设计器逐课配置）
        applyTeacherAdvancePolicyToLesson(lessonToSave, userId, deptId, normalizedMode);

        if (lessonToSave.getLessonId() == null) {
            lessonToSave.setCreatorId(userId);
            lessonToSave.setCreateBy(username);
            lessonToSave.setCreateTime(new Date());
            bizLessonMapper.insertBizLesson(lessonToSave);
        } else {
            lessonToSave.setUpdateBy(username);
            lessonToSave.setUpdateTime(new Date());
            if (bizLessonMapper.updateBizLesson(lessonToSave) != 1)
            {
                throw new ServiceException("课程已被其他操作修改或删除，请刷新后重试");
            }
        }
        Long lessonId = lessonToSave.getLessonId();
        lessonDetailVo.setLessonId(lessonId);

        lessonQuestionMapper.deleteByLessonId(lessonId);
        List<BizLessonQuestionDetailVo> questions = lessonDetailVo.getQuestions();
        if (!CollectionUtils.isEmpty(questions)) {
            List<BizLessonQuestion> questionToInsert = questions.stream().map(q -> {
                BizLessonQuestion item = new BizLessonQuestion();
                item.setLessonId(lessonId);
                item.setQuestionId(q.getQuestionId());
                item.setQuestionScore(q.getQuestionScore());
                item.setOrderNum(q.getOrderNum());
                return item;
            }).collect(Collectors.toList());
            lessonQuestionMapper.batchInsert(questionToInsert);
            
            // 同步更新打字题的自定义时长到 biz_question 表
            for (BizLessonQuestionDetailVo q : questions) {
                if ("typing".equals(q.getQuestionType()) && q.getTypingDuration() != null) {
                    com.ruoyi.business.domain.BizQuestion updateQ = new com.ruoyi.business.domain.BizQuestion();
                    updateQ.setQuestionId(q.getQuestionId());
                    updateQ.setTypingDuration(q.getTypingDuration());
                    bizQuestionMapper.updateBizQuestion(updateQ);
                }
            }
        }

        lessonAssignmentMapper.deleteByLessonId(lessonId);
        List<String> classCodes = lessonDetailVo.getAssignedClassCodes();
        if (!CollectionUtils.isEmpty(classCodes)) {
            validateAssignedClasses(userId, deptId, entryYear, classCodes);
            List<BizLessonAssignment> assignments = new ArrayList<>();
            for (String classCode : classCodes) {
                if (StringUtils.isBlank(classCode)) {
                    continue;
                }
                String pureClassCode = classCode.replace("班", "").trim();
                
                // 【核心】班级互斥：删除该班级在其他课程的指派
                lessonAssignmentMapper.deleteOtherAssignmentsByClass(entryYear, pureClassCode, deptId, lessonId);
                
                BizLessonAssignment assignment = new BizLessonAssignment();
                assignment.setLessonId(lessonId);
                assignment.setDeptId(deptId);
                assignment.setClassCode(pureClassCode);
                assignment.setEntryYear(entryYear);
                assignment.setAssignerId(userId);
                assignment.setAssignTime(new Date());
                assignments.add(assignment);
            }
            if (!assignments.isEmpty()) {
                lessonAssignmentMapper.batchInsert(assignments);
            }
        }
        BizLessonGuideSheetBinding binding = lessonGuideSheetBindingService.synchronize(
                lessonDetailVo, lessonId, userId, username);
        lessonDetailVo.setGuideSheetBinding(binding);
        lessonDetailVo.setSourceSheetId(binding == null ? null : binding.getSourceSheetId());
        lessonDetailVo.setGuideSheetEnabled(binding != null && "Y".equals(binding.getEnabled()));
        return lessonDetailVo;
    }

    private void validateLessonContent(LessonDetailVo detailVo)
    {
        String lessonMode = normalizeLessonMode(detailVo.getLessonMode());
        detailVo.setLessonMode(lessonMode);

        List<BizLessonQuestionDetailVo> questions = detailVo.getQuestions();
        boolean hasOrdinaryQuestions = !CollectionUtils.isEmpty(questions);
        if (hasOrdinaryQuestions)
        {
            long totalScore = 0L;
            for (BizLessonQuestionDetailVo question : questions)
            {
                if (question == null || question.getQuestionId() == null || question.getQuestionScore() == null
                        || question.getQuestionScore() < 0)
                {
                    throw new ServiceException("课程题目或分值配置不完整");
                }
                totalScore += question.getQuestionScore();
            }
            if (totalScore != 100L)
            {
                throw new ServiceException("普通题目总分必须为100分");
            }
        }

        BizLessonGuideSheetBinding current = detailVo.getLessonId() == null
                ? null : guideSheetBindingMapper.selectCurrentByLessonId(detailVo.getLessonId());

        // 考勤课：允许 0 题、不绑导学单；若已加题或启用导学单则自动按测评规则校验（升级路径）
        if ("attendance".equals(lessonMode))
        {
            if (hasOrdinaryQuestions || Boolean.TRUE.equals(detailVo.getGuideSheetEnabled()) || current != null)
            {
                // 已具备测评内容时，仍允许保持 attendance 标记，但内容规则与测评课一致
                if (Boolean.TRUE.equals(detailVo.getGuideSheetEnabled())
                        && detailVo.getSourceSheetId() == null && current == null)
                {
                    throw new ServiceException("开启电子导学单时必须选择一份导学单");
                }
            }
            return;
        }

        if (!hasOrdinaryQuestions && !Boolean.TRUE.equals(detailVo.getGuideSheetEnabled()) && current == null)
        {
            throw new ServiceException("请至少添加普通题目或启用电子导学单（考勤课请将课程用途设为「课堂考勤」）");
        }

        if (Boolean.TRUE.equals(detailVo.getGuideSheetEnabled()))
        {
            if (detailVo.getSourceSheetId() == null && current == null)
            {
                throw new ServiceException("开启电子导学单时必须选择一份导学单");
            }
        }
    }

    /** 归一化课程用途；非法值回退为测评课，避免脏数据阻断保存 */
    private String normalizeLessonMode(String lessonMode)
    {
        if ("attendance".equalsIgnoreCase(lessonMode))
        {
            return "attendance";
        }
        return "assessment";
    }

    /**
     * 写入自动推进配置：考勤课强制关闭；阈值 30～100；延迟 0.5～24 小时。
     */
    private void applyAutoAdvanceConfig(BizLesson lessonToSave, LessonDetailVo detailVo, String lessonMode)
    {
        if ("attendance".equals(lessonMode))
        {
            lessonToSave.setAutoAdvanceEnabled(Boolean.FALSE);
            lessonToSave.setAutoAdvanceThresholdPct(50);
            lessonToSave.setAutoAdvanceDelayHours(new java.math.BigDecimal("2.0"));
            return;
        }
        boolean enabled = Boolean.TRUE.equals(detailVo.getAutoAdvanceEnabled());
        lessonToSave.setAutoAdvanceEnabled(enabled);
        int pct = detailVo.getAutoAdvanceThresholdPct() != null ? detailVo.getAutoAdvanceThresholdPct() : 50;
        if (pct < 30)
        {
            pct = 30;
        }
        if (pct > 100)
        {
            pct = 100;
        }
        lessonToSave.setAutoAdvanceThresholdPct(pct);
        java.math.BigDecimal delay = detailVo.getAutoAdvanceDelayHours() != null
                ? detailVo.getAutoAdvanceDelayHours()
                : new java.math.BigDecimal("2.0");
        if (delay.compareTo(new java.math.BigDecimal("0.5")) < 0)
        {
            delay = new java.math.BigDecimal("0.5");
        }
        if (delay.compareTo(new java.math.BigDecimal("24")) > 0)
        {
            delay = new java.math.BigDecimal("24");
        }
        lessonToSave.setAutoAdvanceDelayHours(delay);
    }

    /**
     * 常规课跟随教师统一推进策略；考勤强制关闭。
     */
    private void applyTeacherAdvancePolicyToLesson(BizLesson lessonToSave, Long teacherId, Long deptId, String lessonMode)
    {
        if ("attendance".equals(lessonMode))
        {
            lessonToSave.setAutoAdvanceEnabled(Boolean.FALSE);
            lessonToSave.setAutoAdvanceThresholdPct(50);
            lessonToSave.setAutoAdvanceDelayHours(new java.math.BigDecimal("2.0"));
            return;
        }
        BizLesson policy = bizLessonMapper.selectAdvancePolicyByTeacher(teacherId, deptId);
        if (policy == null)
        {
            lessonToSave.setAutoAdvanceEnabled(Boolean.FALSE);
            lessonToSave.setAutoAdvanceThresholdPct(50);
            lessonToSave.setAutoAdvanceDelayHours(new java.math.BigDecimal("2.0"));
            return;
        }
        lessonToSave.setAutoAdvanceEnabled(Boolean.TRUE.equals(policy.getAutoAdvanceEnabled()));
        lessonToSave.setAutoAdvanceThresholdPct(
                policy.getAutoAdvanceThresholdPct() != null ? policy.getAutoAdvanceThresholdPct() : 50);
        lessonToSave.setAutoAdvanceDelayHours(
                policy.getAutoAdvanceDelayHours() != null ? policy.getAutoAdvanceDelayHours() : new java.math.BigDecimal("2.0"));
    }

    @Override
    public Map<String, Object> getTeacherAdvancePolicy()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long teacherId = loginUser.getUserId();
        Long deptId = loginUser.getDeptId();
        BizLesson sample = bizLessonMapper.selectAdvancePolicyByTeacher(teacherId, deptId);
        Map<String, Object> policy = new HashMap<>();
        if (sample == null)
        {
            policy.put("autoAdvanceEnabled", Boolean.FALSE);
            policy.put("autoAdvanceThresholdPct", 50);
            policy.put("autoAdvanceDelayHours", new java.math.BigDecimal("2.0"));
            policy.put("hasPolicy", Boolean.FALSE);
            return policy;
        }
        policy.put("autoAdvanceEnabled", Boolean.TRUE.equals(sample.getAutoAdvanceEnabled()));
        policy.put("autoAdvanceThresholdPct",
                sample.getAutoAdvanceThresholdPct() != null ? sample.getAutoAdvanceThresholdPct() : 50);
        policy.put("autoAdvanceDelayHours",
                sample.getAutoAdvanceDelayHours() != null ? sample.getAutoAdvanceDelayHours() : new java.math.BigDecimal("2.0"));
        policy.put("hasPolicy", Boolean.TRUE);
        return policy;
    }

    @Override
    public Map<String, Object> updateTeacherAdvancePolicy(LessonDetailVo config)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        String username = loginUser.getUsername();
        Long teacherId = loginUser.getUserId();
        Long deptId = loginUser.getDeptId();
        LessonDetailVo safe = config != null ? config : new LessonDetailVo();
        BizLesson probe = new BizLesson();
        applyAutoAdvanceConfig(probe, safe, "assessment");
        bizLessonMapper.upsertAdvancePolicy(
                teacherId,
                deptId,
                Boolean.TRUE.equals(probe.getAutoAdvanceEnabled()),
                probe.getAutoAdvanceThresholdPct(),
                probe.getAutoAdvanceDelayHours(),
                username);
        int rows = bizLessonMapper.updateAdvancePolicyByCreator(
                teacherId,
                username,
                deptId,
                Boolean.TRUE.equals(probe.getAutoAdvanceEnabled()),
                probe.getAutoAdvanceThresholdPct(),
                probe.getAutoAdvanceDelayHours(),
                username);
        lessonAssignmentMapper.clearReadyTimesByTeacher(teacherId, username, deptId);
        Map<String, Object> policy = getTeacherAdvancePolicy();
        policy.put("updatedLessons", rows);
        return policy;
    }

    private void validateAssignedClasses(Long userId, Long deptId, String entryYear, List<String> classCodes)
    {
        if (StringUtils.isBlank(entryYear))
        {
            throw new ServiceException("无法根据课程年级确定入学年份");
        }
        java.util.Set<String> available = bizStudentMapper.selectDistinctYearAndClassByDeptId(deptId).stream()
                .filter(Objects::nonNull)
                .filter(student -> entryYear.equals(student.getEntryYear()))
                .map(BizStudent::getClassCode)
                .filter(StringUtils::isNotBlank)
                .collect(java.util.stream.Collectors.toSet());
        for (String classCode : classCodes)
        {
            String normalized = StringUtils.trimToEmpty(classCode).replace("班", "");
            if (!available.contains(normalized))
            {
                throw new ServiceException("班级不存在或不属于当前学校：" + classCode);
            }
            if (!SecurityUtils.isAdmin(userId))
            {
                BizTeacherClass managed = new BizTeacherClass();
                managed.setUserId(userId);
                managed.setDeptId(deptId);
                managed.setEntryYear(entryYear);
                managed.setClassCode(normalized);
                if (teacherClassMapper.checkTeacherClassExists(managed) <= 0)
                {
                    throw new ServiceException("只能指派自己管理的班级：" + classCode);
                }
            }
        }
    }

    private void assertCanManageLesson(BizLesson lesson)
    {
        if (lesson == null)
        {
            throw new ServiceException("课程不存在");
        }
        Long userId = SecurityUtils.getUserId();
        if (SecurityUtils.isAdmin(userId))
        {
            return;
        }
        boolean sameDept = lesson.getDeptId() != null && lesson.getDeptId().equals(SecurityUtils.getDeptId());
        boolean creator = userId.equals(lesson.getCreatorId())
                || (lesson.getCreatorId() == null && SecurityUtils.getUsername().equals(lesson.getCreateBy()));
        if (!sameDept || !creator)
        {
            throw new ServiceException("无权管理该课程");
        }
    }

    private void assertLessonHasNoGuideSheetHistory(Long lessonId)
    {
        lessonGuideSheetBindingService.assertLessonHasNoHistory(lessonId);
    }

    private Map<String, Long> calculateGradeInfo(String entryYear, String schoolType) {
        Map<String, Long> gradeInfo = new HashMap<>();
        if (entryYear == null || schoolType == null) {
            gradeInfo.put("未知年级", 0L);
            return gradeInfo;
        }
        try {
            int entryYearInt = Integer.parseInt(entryYear);
            int gradeNum = AcademicYearUtils.resolveAcademicStartYear(java.time.LocalDate.now())
                    - entryYearInt + 1;

            if (gradeNum <= 0) {
                gradeInfo.put("新生", 0L);
                return gradeInfo;
            }

            String[] chineseNums = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"};

            switch (schoolType) {
                case "1":
                    if (gradeNum <= 6) {
                        gradeInfo.put(chineseNums[gradeNum] + "年级", (long) gradeNum);
                    } else {
                        gradeInfo.put("已毕业", -1L);
                    }
                    break;
                case "2":
                    long juniorGrade = gradeNum + 6;
                    if (juniorGrade <= 9) {
                        gradeInfo.put(chineseNums[(int)juniorGrade] + "年级", juniorGrade);
                    } else {
                        gradeInfo.put("已毕业", -1L);
                    }
                    break;
                case "3":
                    if (gradeNum <= 3) {
                        gradeInfo.put("高" + chineseNums[gradeNum], (long) gradeNum + 9);
                    } else {
                        gradeInfo.put("已毕业", -1L);
                    }
                    break;
                default:
                    gradeInfo.put("未知学段", 0L);
                    break;
            }
        } catch (NumberFormatException e) {
            gradeInfo.put("未知年级", 0L);
        }
        return gradeInfo;
    }

    private String calculateEntryYearFromGrade(Long grade) {
        if (grade == null) {
            throw new ServiceException("课程年级不能为空");
        }
        try {
            return AcademicYearUtils.resolveEntryYear(grade.intValue(), java.time.LocalDate.now());
        } catch (IllegalArgumentException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    private void preserveLessonEntryYear(BizLesson existing, String requestedEntryYear)
    {
        if (existing == null || StringUtils.isBlank(existing.getEntryYear()))
        {
            throw new ServiceException("课程所属入学年份缺失，请先执行学年迁移");
        }
        if (StringUtils.isNotBlank(requestedEntryYear)
                && !existing.getEntryYear().equals(requestedEntryYear.trim()))
        {
            throw new ServiceException("课程所属入学年份不可修改，如需跨届使用请复制为新课程");
        }
    }

    private void validateEntryYear(String entryYear)
    {
        if (StringUtils.isBlank(entryYear) || !entryYear.matches("\\d{4}"))
        {
            throw new ServiceException("课程所属入学年份格式错误");
        }
    }
}
