package com.ruoyi.business.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.domain.BizLessonQuestion;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.GradeGroupVo;
import com.ruoyi.business.domain.vo.LessonDetailVo;
import com.ruoyi.business.domain.vo.LessonInfoVo;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.model.LoginUser;
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

    @Override
    public BizLesson selectBizLessonByLessonId(Long lessonId)
    {
        return bizLessonMapper.selectBizLessonByLessonId(lessonId);
    }

    @Override
    public List<BizLesson> selectBizLessonList(BizLesson bizLesson)
    {
        return bizLessonMapper.selectBizLessonList(bizLesson);
    }

    @Override
    public int insertBizLesson(BizLesson bizLesson)
    {
        return bizLessonMapper.insertBizLesson(bizLesson);
    }

    @Override
    public int updateBizLesson(BizLesson bizLesson)
    {
        return bizLessonMapper.updateBizLesson(bizLesson);
    }

    @Override
    @Transactional
    public int deleteBizLessonByLessonIds(Long[] lessonIds)
    {
        for (Long lessonId : lessonIds) {
            // 级联删除关联数据
            lessonQuestionMapper.deleteByLessonId(lessonId);
            lessonAssignmentMapper.deleteByLessonId(lessonId);
        }
        // 删除课程本身
        return bizLessonMapper.deleteBizLessonByLessonIds(lessonIds);
    }

    @Override
    public int deleteBizLessonByLessonId(Long lessonId)
    {
        return bizLessonMapper.deleteBizLessonByLessonId(lessonId);
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

        List<BizStudent> students = bizStudentMapper.selectDistinctYearAndClassByDeptId(deptId);
        log.info("【教师首页数据】步骤1: 从数据库查询到 {} 条学生(年级-班级)记录。", students.size());

        Map<String, List<String>> yearClassMap = students.stream()
                .filter(Objects::nonNull)
                .filter(s -> Objects.nonNull(s.getEntryYear()))
                .collect(Collectors.groupingBy(
                        BizStudent::getEntryYear,
                        Collectors.mapping(s -> s.getClassCode() + "班", Collectors.toList())
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

            if (currentGradeId > 0) {
                // 查询我创建的课程
                List<LessonInfoVo> selfLessons = bizLessonMapper.selectLessonsByGradeAndCreator(currentGradeId, username);
                log.info("【教师首页数据】为该年级查询到 {} 门自建课程。", selfLessons.size());

                // 查询共享给我的课程
                List<LessonInfoVo> sharedLessons = bizLessonMapper.selectSharedLessonsByGradeAndUser(
                        currentGradeId, loginUser.getUserId(), deptId, username);
                log.info("【教师首页数据】为该年级查询到 {} 门共享课程。", sharedLessons.size());

                // 合并并按创建时间降序排列（最新创建的在前）
                List<LessonInfoVo> allLessons = new ArrayList<>();
                allLessons.addAll(selfLessons);
                allLessons.addAll(sharedLessons);
                // 注意：由于LessonInfoVo没有createTime，需要依赖查询顺序
                // 或者在查询时已经按创建时间降序返回
                
                // 填充每个课程的已指派班级
                for (LessonInfoVo lesson : allLessons) {
                    List<String> classCodes = lessonAssignmentMapper.selectClassCodesByLessonId(lesson.getLessonId());
                    if (classCodes != null && !classCodes.isEmpty()) {
                        List<String> formattedCodes = classCodes.stream()
                            .filter(StringUtils::isNotBlank)
                            .map(code -> code.endsWith("班") ? code : code + "班")
                            .collect(Collectors.toList());
                        lesson.setAssignedClasses(formattedCodes);
                    }
                }

                gradeGroup.setLessons(allLessons);
            } else {
                gradeGroup.setLessons(new ArrayList<>());
            }

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

        List<BizLessonQuestionDetailVo> questions = lessonQuestionMapper.selectDetailsByLessonId(lessonId);
        List<String> assignedClassCodes = lessonAssignmentMapper.selectClassCodesByLessonId(lessonId);
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

        List<String> allClassesInGrade = new ArrayList<>();
        if (lesson.getGrade() != null) {
            SysDept school = deptMapper.selectDeptById(deptId);
            List<BizStudent> students = bizStudentMapper.selectDistinctYearAndClassByDeptId(deptId);
            for (BizStudent student : students) {
                Map<String, Long> gradeInfo = calculateGradeInfo(student.getEntryYear(), school.getSchoolType());
                Long currentGradeId = gradeInfo.values().stream().findFirst().orElse(null);
                if (currentGradeId != null && currentGradeId >= 0 && lesson.getGrade().equals(currentGradeId)) {
                    allClassesInGrade.add(student.getClassCode() + "班");
                }
            }
            allClassesInGrade = allClassesInGrade.stream()
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList()); // 新增：班级列表去重排序，保证展示稳定
        }

        detailVo.setLessonId(lesson.getLessonId());
        detailVo.setLessonTitle(lesson.getLessonTitle());
        detailVo.setGrade(lesson.getGrade());
        detailVo.setSemester(lesson.getSemester());
        detailVo.setLessonNum(lesson.getLessonNum());
        detailVo.setQuestions(questions);
        detailVo.setAssignedClassCodes(assignedClassCodes);
        detailVo.setAllClassesInGrade(allClassesInGrade);

        return detailVo;
    }

    @Override
    @Transactional
    public LessonDetailVo saveLessonDetails(LessonDetailVo lessonDetailVo) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        String username = loginUser.getUsername();
        Long userId = loginUser.getUserId();

        BizLesson lessonToSave = new BizLesson();
        lessonToSave.setLessonId(lessonDetailVo.getLessonId());
        lessonToSave.setLessonTitle(lessonDetailVo.getLessonTitle());
        lessonToSave.setGrade(lessonDetailVo.getGrade());
        lessonToSave.setSemester(lessonDetailVo.getSemester());
        lessonToSave.setLessonNum(lessonDetailVo.getLessonNum());

        if (lessonToSave.getLessonId() == null) {
            lessonToSave.setCreateBy(username);
            bizLessonMapper.insertBizLesson(lessonToSave);
        } else {
            lessonToSave.setUpdateBy(username);
            bizLessonMapper.updateBizLesson(lessonToSave);
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
        }

        lessonAssignmentMapper.deleteByLessonId(lessonId);
        List<String> classCodes = lessonDetailVo.getAssignedClassCodes();
        if (!CollectionUtils.isEmpty(classCodes)) {
            String entryYear = calculateEntryYearFromGrade(lessonDetailVo.getGrade());
            List<BizLessonAssignment> assignments = new ArrayList<>();
            for (String classCode : classCodes) {
                if (StringUtils.isBlank(classCode)) {
                    continue;
                }
                String pureClassCode = classCode.replace("班", "").trim();
                
                // 【核心】班级互斥：删除该班级在其他课程的指派
                lessonAssignmentMapper.deleteOtherAssignmentsByClass(entryYear, pureClassCode, lessonId);
                
                BizLessonAssignment assignment = new BizLessonAssignment();
                assignment.setLessonId(lessonId);
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
        return lessonDetailVo;
    }

    private Map<String, Long> calculateGradeInfo(String entryYear, String schoolType) {
        Map<String, Long> gradeInfo = new HashMap<>();
        if (entryYear == null || schoolType == null) {
            gradeInfo.put("未知年级", 0L);
            return gradeInfo;
        }
        try {
            int entryYearInt = Integer.parseInt(entryYear);
            Calendar now = Calendar.getInstance();
            int currentYear = now.get(Calendar.YEAR);
            int currentMonth = now.get(Calendar.MONTH) + 1;
            int currentDay = now.get(Calendar.DAY_OF_MONTH);

            int gradeNum = (currentYear - entryYearInt);

            if (currentMonth > 7 || (currentMonth == 7 && currentDay >= 20)) {
                gradeNum += 1;
            }

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
        if (grade == null) return null;
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1;
        int currentDay = now.get(Calendar.DAY_OF_MONTH);

        int gradeNum = grade.intValue();
        if (grade > 9) {
            gradeNum = grade.intValue() - 9;
        } else if (grade > 6) {
            gradeNum = grade.intValue() - 6;
        }

        // 入学年份 = 当前学年起始年 - (学段内年级序号 - 1)
        // 例如：2025学年，七年级(gradeNum=1)的入学年份是 2025 - (1-1) = 2025
        int academicStartYear = currentYear;
        if (currentMonth < 7 || (currentMonth == 7 && currentDay < 20)) {
            academicStartYear = currentYear - 1;
        }
        int entryYear = academicStartYear - gradeNum + 1;
        return String.valueOf(entryYear);
    }
}
