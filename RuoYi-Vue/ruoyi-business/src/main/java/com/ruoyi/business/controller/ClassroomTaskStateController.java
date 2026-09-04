package com.ruoyi.business.controller;

import java.util.List;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizStudentTaskState;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.ClassroomStudentTaskSummaryVo;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.service.ClassroomTaskStateService;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 学生作业状态上报、教师状态校准与退回入口。 */
@RestController
@RequestMapping("/business/classroom-state")
public class ClassroomTaskStateController extends BaseController
{
    @Autowired private ClassroomTaskStateService taskStateService;
    @Autowired private BizStudentMapper studentMapper;
    @Autowired private BizLessonAssignmentMapper assignmentMapper;
    @Autowired private BizLessonQuestionMapper lessonQuestionMapper;
    @Autowired private GuideSheetAccessService guideSheetAccessService;

    @PreAuthorize("@studentSs.isStudent()")
    @PostMapping("/student")
    public AjaxResult markStudentState(@RequestBody StateRequest request)
    {
        BizStudent student = studentMapper.selectBizStudentByUserId(SecurityUtils.getUserId());
        if (student == null) throw new ServiceException("未找到学生信息");
        if (!ClassroomTaskStateService.ENTERED.equals(request.getTaskState())
                && !ClassroomTaskStateService.WORKING.equals(request.getTaskState()))
        {
            throw new ServiceException("学生端只能上报进入或作答中状态");
        }
        Long currentLessonId = assignmentMapper.selectCurrentLessonByClass(
                student.getEntryYear(), normalizeClassCode(student.getClassCode()), student.getDeptId());
        if (request.getLessonId() == null || !request.getLessonId().equals(currentLessonId))
        {
            throw new ServiceException("当前课程已发生变化，请刷新页面");
        }
        assertQuestionBelongsToLesson(request.getLessonId(), request.getQuestionId());
        return success(taskStateService.mark(student, student.getDeptId(), request.getLessonId(),
                request.getQuestionId(), request.getTaskState()));
    }

    @PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('admin')")
    @GetMapping("/class")
    public AjaxResult listClassStates(@RequestParam Long lessonId,
                                      @RequestParam Long questionId,
                                      @RequestParam String entryYear,
                                      @RequestParam String classCode)
    {
        Long lessonDeptId = guideSheetAccessService.requireViewableLessonClassDept(lessonId, entryYear, classCode);
        assertQuestionBelongsToLesson(lessonId, questionId);
        List<BizStudentTaskState> states = taskStateService.listClassStates(
                lessonDeptId, lessonId, questionId, entryYear, classCode);
        return success(states);
    }

    /** 教师课堂大屏的全班课程状态汇总，终端在线状态由学生桌面接口独立提供。 */
    @PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('admin')")
    @GetMapping("/summary")
    public AjaxResult listClassSummary(@RequestParam Long lessonId,
                                       @RequestParam String entryYear,
                                       @RequestParam String classCode)
    {
        Long lessonDeptId = guideSheetAccessService.requireViewableLessonClassDept(lessonId, entryYear, classCode);
        List<ClassroomStudentTaskSummaryVo> summaries = taskStateService.listClassSummary(
                lessonDeptId, lessonId, entryYear, classCode);
        return success(summaries);
    }

    @PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('admin')")
    @PostMapping("/return")
    public AjaxResult returnForResubmission(@RequestBody StateRequest request)
    {
        if (StringUtils.isBlank(request.getEntryYear()) || StringUtils.isBlank(request.getClassCode())
                || request.getStudentId() == null)
        {
            throw new ServiceException("退回参数不完整");
        }
        Long lessonDeptId = guideSheetAccessService.requireViewableLessonClassDept(
                request.getLessonId(), request.getEntryYear(), request.getClassCode());
        assertQuestionBelongsToLesson(request.getLessonId(), request.getQuestionId());
        BizStudent student = studentMapper.selectBizStudentByStudentId(request.getStudentId());
        if (student == null || !lessonDeptId.equals(student.getDeptId())
                || !request.getEntryYear().trim().equals(student.getEntryYear())
                || !normalizeClassCode(request.getClassCode()).equals(normalizeClassCode(student.getClassCode())))
        {
            throw new ServiceException("学生不属于当前班级");
        }
        taskStateService.mark(student, student.getDeptId(), request.getLessonId(),
                request.getQuestionId(), ClassroomTaskStateService.RETURNED);
        return success("已退回学生重交");
    }

    private void assertQuestionBelongsToLesson(Long lessonId, Long questionId)
    {
        if (lessonId == null || questionId == null) throw new ServiceException("课程和题目不能为空");
        List<BizLessonQuestionDetailVo> questions = lessonQuestionMapper.selectDetailsByLessonId(lessonId);
        boolean exists = questions != null && questions.stream()
                .anyMatch(item -> questionId.equals(item.getQuestionId()));
        if (!exists) throw new ServiceException("题目不属于当前课程");
    }

    private String normalizeClassCode(String classCode)
    {
        if (classCode == null) return "";
        String normalized = classCode.trim();
        return normalized.endsWith("班") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    public static class StateRequest
    {
        private Long lessonId;
        private Long questionId;
        private Long studentId;
        private String taskState;
        private String entryYear;
        private String classCode;
        public Long getLessonId() { return lessonId; }
        public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getTaskState() { return taskState; }
        public void setTaskState(String taskState) { this.taskState = taskState; }
        public String getEntryYear() { return entryYear; }
        public void setEntryYear(String entryYear) { this.entryYear = entryYear; }
        public String getClassCode() { return classCode; }
        public void setClassCode(String classCode) { this.classCode = classCode; }
    }
}
