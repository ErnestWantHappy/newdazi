package com.ruoyi.business.service;

import java.util.List;
import java.util.Map;

import com.ruoyi.business.domain.BizGuideSheet;
import com.ruoyi.business.domain.BizGuideSheetProgress;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.domain.BizLessonGuideSheetBinding;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizTeacherClass;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizTeacherClassMapper;
import com.ruoyi.business.mapper.GuideSheetBindingMapper;
import com.ruoyi.business.mapper.GuideSheetMapper;
import com.ruoyi.business.mapper.GuideSheetProgressMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 导学单统一访问控制，确保所有读写入口使用同一组课程与班级边界。
 */
@Service
public class GuideSheetAccessService
{
    @Autowired
    private GuideSheetMapper guideSheetMapper;

    @Autowired
    private GuideSheetBindingMapper bindingMapper;

    @Autowired
    private GuideSheetProgressMapper progressMapper;

    @Autowired
    private BizLessonMapper lessonMapper;

    @Autowired
    private BizLessonAssignmentMapper lessonAssignmentMapper;

    @Autowired
    private BizTeacherClassMapper teacherClassMapper;

    @Autowired
    private BizStudentMapper studentMapper;

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    @Autowired
    private OrganizationBoundaryService organizationBoundaryService;

    @Autowired
    private ICountyExamService countyExamService;

    public BizGuideSheet requireVisibleTemplate(Long sheetId)
    {
        BizGuideSheet sheet = guideSheetMapper.selectBizGuideSheetBySheetId(sheetId);
        if (sheet == null || !isTemplateVisibleTo(sheet, SecurityUtils.getUserId(),
                organizationBoundaryService.resolveCountyDeptId(SecurityUtils.getDeptId()),
                SecurityUtils.isAdmin(SecurityUtils.getUserId())))
        {
            throw new ServiceException("导学单不存在或无权访问");
        }
        return sheet;
    }

    public BizGuideSheet requireSelectableTemplate(Long sheetId)
    {
        BizGuideSheet sheet = requireVisibleTemplate(sheetId);
        if (!"0".equals(sheet.getDelFlag()))
        {
            throw new ServiceException("该导学单已归档，不能用于新课程");
        }
        return sheet;
    }

    public void assertCanManageTemplate(Long sheetId)
    {
        BizGuideSheet sheet = guideSheetMapper.selectBizGuideSheetBySheetId(sheetId);
        if (sheet == null || !canManageTemplate(sheet, SecurityUtils.getUserId(),
                SecurityUtils.isAdmin(SecurityUtils.getUserId())))
        {
            throw new ServiceException("无权编辑或归档该导学单");
        }
    }

    public boolean isTemplateVisibleTo(BizGuideSheet sheet, Long viewerId, Long viewerCountyDeptId,
                                       boolean administrator)
    {
        if (sheet == null || viewerId == null)
        {
            return false;
        }
        if (administrator || viewerId.equals(sheet.getCreatorId()))
        {
            return true;
        }
        if (!"0".equals(sheet.getDelFlag()))
        {
            return false;
        }
        return "Y".equals(sheet.getIsPublic()) && viewerCountyDeptId != null
                && viewerCountyDeptId.equals(sheet.getCountyDeptId());
    }

    public boolean canManageTemplate(BizGuideSheet sheet, Long viewerId, boolean administrator)
    {
        return sheet != null && viewerId != null
                && (administrator || viewerId.equals(sheet.getCreatorId()));
    }

    public BizLessonGuideSheetBinding requireCurrentStudentBinding(BizStudent student)
    {
        assertStudent(student);
        assertNoPendingCountyExam();
        Long deptId = student.getDeptId() != null ? student.getDeptId() : SecurityUtils.getDeptId();
        Long lessonId = lessonAssignmentMapper.selectCurrentLessonByClass(
                student.getEntryYear(), student.getClassCode(), deptId);
        if (lessonId == null)
        {
            return null;
        }
        BizLessonGuideSheetBinding binding = bindingMapper.selectEnabledByLessonId(lessonId);
        if (binding == null)
        {
            return null;
        }
        validateStudentBinding(student, binding, deptId);
        return binding;
    }

    public BizLessonGuideSheetBinding requireStudentBinding(BizStudent student, Long bindingId)
    {
        assertStudent(student);
        assertNoPendingCountyExam();
        BizLessonGuideSheetBinding binding = bindingMapper.selectByBindingId(bindingId);
        if (binding == null)
        {
            throw new ServiceException("课程导学单不存在");
        }
        Long deptId = student.getDeptId() != null ? student.getDeptId() : SecurityUtils.getDeptId();
        validateStudentBinding(student, binding, deptId);
        return binding;
    }

    public BizLessonGuideSheetBinding requireBindingClassAccess(Long bindingId, String entryYear,
                                                                 String classCode)
    {
        BizLessonGuideSheetBinding binding = bindingMapper.selectByBindingId(bindingId);
        if (binding == null)
        {
            throw new ServiceException("课程导学单绑定不存在");
        }
        if (StringUtils.isBlank(entryYear) || StringUtils.isBlank(classCode))
        {
            throw new ServiceException("入学年份和班级编号必须同时提供");
        }
        String normalizedClassCode = normalizeClassCode(classCode);
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(binding.getLessonId());
        Long currentDeptId = SecurityUtils.getDeptId();
        if (lesson == null || lesson.getDeptId() == null || !lesson.getDeptId().equals(currentDeptId))
        {
            throw new ServiceException("课程不存在或不属于当前学校");
        }
        assertLessonEntryYear(lesson, entryYear);
        boolean classEvidence = isLessonAssignedToClass(
                lesson.getLessonId(), currentDeptId, entryYear, normalizedClassCode);
        boolean historicalResult = !progressMapper.selectByBindingAndClass(
                bindingId, currentDeptId, entryYear, normalizedClassCode).isEmpty();
        if (!classEvidence && !historicalResult)
        {
            throw new ServiceException("该课程没有当前或历史班级数据");
        }
        assertTeacherClassScope(lesson, currentDeptId, entryYear, normalizedClassCode);
        return binding;
    }

    public BizLessonGuideSheetBinding requireBindingManagementAccess(Long bindingId)
    {
        BizLessonGuideSheetBinding binding = bindingMapper.selectByBindingId(bindingId);
        if (binding == null)
        {
            throw new ServiceException("课程导学单绑定不存在");
        }
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(binding.getLessonId());
        Long userId = SecurityUtils.getUserId();
        boolean creator = isLessonCreator(lesson, userId);
        if (lesson == null || lesson.getDeptId() == null || !lesson.getDeptId().equals(SecurityUtils.getDeptId()))
        {
            throw new ServiceException("无权查看该课程导学单快照");
        }
        if (SecurityUtils.isAdmin(userId) || creator)
        {
            return binding;
        }
        BizLessonAssignment query = new BizLessonAssignment();
        query.setLessonId(lesson.getLessonId());
        List<BizLessonAssignment> assignments = lessonAssignmentMapper.selectBizLessonAssignmentList(query);
        if (assignments != null)
        {
            for (BizLessonAssignment assignment : assignments)
            {
                if (!SecurityUtils.getDeptId().equals(assignment.getDeptId()))
                {
                    continue;
                }
                BizTeacherClass teacherClass = new BizTeacherClass();
                teacherClass.setUserId(userId);
                teacherClass.setDeptId(assignment.getDeptId());
                teacherClass.setEntryYear(assignment.getEntryYear());
                teacherClass.setClassCode(assignment.getClassCode());
                if (teacherClassMapper.checkTeacherClassExists(teacherClass) > 0)
                {
                    return binding;
                }
            }
        }
        throw new ServiceException("无权查看该课程导学单快照");
    }

    public BizLessonGuideSheetBinding requireLessonClassBindingContext(Long lessonId, String entryYear,
                                                                        String classCode)
    {
        // 成绩页对历史课程也要能打开（历史班级查看历史成绩）：这里只校验课程属于本校且届别一致，
        // 不再要求“当前指派”。无当前指派时 return null 由调用方按“无导学单上下文”静默处理，
        // 避免历史课查询被“该课程未指派给当前班级”拦截。
        if (lessonId == null || StringUtils.isBlank(entryYear) || StringUtils.isBlank(classCode))
        {
            throw new ServiceException("课程、入学年份和班级编号必须同时提供");
        }
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        Long currentDeptId = SecurityUtils.getDeptId();
        if (lesson == null || lesson.getDeptId() == null || !lesson.getDeptId().equals(currentDeptId))
        {
            throw new ServiceException("课程不存在或不属于当前学校");
        }
        assertLessonEntryYear(lesson, entryYear);
        // 仅校验教师班级范围（历史课也受管理班级约束）；指派与否不再作为读开关/上下文的硬门槛。
        assertTeacherClassScope(lesson, currentDeptId, entryYear, normalizeClassCode(classCode));
        return bindingMapper.selectCurrentByLessonId(lessonId);
    }

    public void assertCanViewLessonClass(Long lessonId, String entryYear, String classCode)
    {
        if (lessonId == null || StringUtils.isBlank(entryYear) || StringUtils.isBlank(classCode))
        {
            throw new ServiceException("课程、入学年份和班级编号必须同时提供");
        }
        String normalizedClassCode = normalizeClassCode(classCode);
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        Long currentDeptId = SecurityUtils.getDeptId();
        if (lesson == null || lesson.getDeptId() == null || !lesson.getDeptId().equals(currentDeptId))
        {
            throw new ServiceException("课程不存在或不属于当前学校");
        }
        assertLessonEntryYear(lesson, entryYear);
        if (!isLessonAssignedToClass(lessonId, currentDeptId, entryYear, normalizedClassCode))
        {
            throw new ServiceException("该课程未指派给当前班级");
        }

        assertTeacherClassScope(lesson, currentDeptId, entryYear, normalizedClassCode);
    }

    /**
     * 校验教师可查看指定课程班级，并返回课程实际所属学校。
     * 课程创建者的账号主部门可能因历史任教关系与课程学校不同，此时仍须以课程、指派和班级管理事实校验。
     */
    public Long requireViewableLessonClassDept(Long lessonId, String entryYear, String classCode)
    {
        if (lessonId == null || StringUtils.isBlank(entryYear) || StringUtils.isBlank(classCode))
        {
            throw new ServiceException("课程、入学年份和班级编号必须同时提供");
        }
        String normalizedClassCode = normalizeClassCode(classCode);
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        Long userId = SecurityUtils.getUserId();
        Long lessonDeptId = lesson == null ? null : lesson.getDeptId();
        boolean creator = isLessonCreator(lesson, userId);
        if (lessonDeptId == null || (!SecurityUtils.isAdmin(userId) && !creator
                && !lessonDeptId.equals(SecurityUtils.getDeptId())))
        {
            throw new ServiceException("课程不存在或不属于当前学校");
        }
        assertLessonEntryYear(lesson, entryYear);
        if (!isLessonAssignedToClass(lessonId, lessonDeptId, entryYear, normalizedClassCode))
        {
            throw new ServiceException("该课程未指派给当前班级");
        }
        assertTeacherClassScope(lesson, lessonDeptId, entryYear, normalizedClassCode);
        return lessonDeptId;
    }

    public BizStudent requireCurrentStudent()
    {
        BizStudent student = studentMapper.selectBizStudentByUserId(SecurityUtils.getUserId());
        assertStudent(student);
        return student;
    }

    public void assertStudentInBindingClass(Long bindingId, Long studentId, Long deptId,
                                            String entryYear, String classCode)
    {
        BizGuideSheetProgress progress = progressMapper.selectByBindingAndStudent(bindingId, studentId);
        String normalizedClassCode = normalizeClassCode(classCode);
        if (progress == null || !deptId.equals(progress.getDeptId())
                || !entryYear.equals(progress.getEntryYear())
                || !normalizedClassCode.equals(progress.getClassCode()))
        {
            throw new ServiceException("学生不属于该导学单的历史班级范围");
        }
    }

    private void validateStudentBinding(BizStudent student, BizLessonGuideSheetBinding binding, Long deptId)
    {
        if (!"Y".equals(binding.getIsCurrent()) || !"Y".equals(binding.getEnabled()))
        {
            throw new ServiceException("该课程未开启电子导学单");
        }
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(binding.getLessonId());
        if (lesson == null || lesson.getDeptId() == null || !lesson.getDeptId().equals(deptId))
        {
            throw new ServiceException("课程不存在或学校归属不匹配");
        }
        Long currentLessonId = lessonAssignmentMapper.selectCurrentLessonByClass(
                student.getEntryYear(), student.getClassCode(), deptId);
        if (!lesson.getLessonId().equals(currentLessonId))
        {
            throw new ServiceException("该导学单不属于学生当前课程");
        }
        BizLessonGuideSheetBinding current = bindingMapper.selectEnabledByLessonId(lesson.getLessonId());
        if (current == null || !binding.getBindingId().equals(current.getBindingId()))
        {
            throw new ServiceException("该导学单已不是课程当前绑定");
        }
    }

    private boolean isLessonAssignedToClass(Long lessonId, Long deptId, String entryYear, String classCode)
    {
        BizLessonAssignment query = new BizLessonAssignment();
        query.setLessonId(lessonId);
        List<BizLessonAssignment> assignments = lessonAssignmentMapper.selectBizLessonAssignmentList(query);
        if (assignments != null)
        {
            for (BizLessonAssignment assignment : assignments)
            {
                if (deptId.equals(assignment.getDeptId()) && entryYear.equals(assignment.getEntryYear())
                        && classCode.equals(assignment.getClassCode()))
                {
                    return true;
                }
            }
        }
        // 推进后当前指派会移动到下一课；历史记录或真实答卷都能证明原课程班级范围。
        return lessonAssignmentMapper.countHistoricalAssignment(lessonId, entryYear, classCode, deptId) > 0
                || studentAnswerMapper.existsLessonClassAnswer(lessonId, classCode, entryYear, deptId) > 0;
    }

    private void assertLessonEntryYear(BizLesson lesson, String entryYear)
    {
        // 先核对课程自身的稳定届别，避免异常指派或历史数据扩大跨届访问范围。
        if (StringUtils.isBlank(lesson.getEntryYear()) || !entryYear.equals(lesson.getEntryYear()))
        {
            throw new ServiceException("课程届别与请求入学年份不一致");
        }
    }

    private void assertTeacherClassScope(BizLesson lesson, Long deptId, String entryYear,
                                         String classCode)
    {
        Long userId = SecurityUtils.getUserId();
        if (SecurityUtils.isAdmin(userId) || isLessonCreator(lesson, userId))
        {
            return;
        }
        BizTeacherClass teacherClass = new BizTeacherClass();
        teacherClass.setUserId(userId);
        teacherClass.setDeptId(deptId);
        teacherClass.setEntryYear(entryYear);
        teacherClass.setClassCode(classCode);
        if (teacherClassMapper.checkTeacherClassExists(teacherClass) <= 0)
        {
            throw new ServiceException("只能查看自己管理班级的数据");
        }
    }

    private boolean isLessonCreator(BizLesson lesson, Long userId)
    {
        return lesson != null && userId != null && (userId.equals(lesson.getCreatorId())
                || (lesson.getCreatorId() == null
                && StringUtils.equals(SecurityUtils.getUsername(), lesson.getCreateBy())));
    }

    private void assertStudent(BizStudent student)
    {
        if (student == null)
        {
            throw new ServiceException("未找到当前学生信息");
        }
        if (StringUtils.isBlank(student.getEntryYear()) || StringUtils.isBlank(student.getClassCode()))
        {
            throw new ServiceException("学生入学年份或班级信息不完整");
        }
    }

    public void assertNoPendingCountyExam()
    {
        Map<String, Object> currentExam = countyExamService.checkCurrentStudentExam();
        if (Boolean.TRUE.equals(currentExam.get("hasExam"))
                && !Boolean.TRUE.equals(currentExam.get("ended")))
        {
            throw new ServiceException("请先完成区域抽测");
        }
    }

    private String normalizeClassCode(String classCode)
    {
        String normalized = StringUtils.trimToEmpty(classCode);
        return normalized.endsWith("班") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }
}
