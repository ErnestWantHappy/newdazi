package com.ruoyi.business.service;

import java.util.*;
import java.util.stream.Collectors;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.business.config.ClassroomRoomKey;
import com.ruoyi.business.domain.*;
import com.ruoyi.business.domain.dto.TheoryRedoRequest;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.mapper.*;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TheoryRedoService
{
    @Autowired private TheoryRedoMapper mapper;
    @Autowired private BizStudentMapper studentMapper;
    @Autowired private BizLessonMapper lessonMapper;
    @Autowired private BizLessonAssignmentMapper assignmentMapper;
    @Autowired private BizLessonQuestionMapper questionMapper;
    @Autowired private BizStudentAnswerMapper answerMapper;
    @Autowired private BizScoreAdjustmentMapper adjustmentMapper;
    @Autowired private GuideSheetAccessService accessService;
    @Autowired private ClassroomTaskStateWriter stateWriter;

    public long revision(Long studentId, Long lessonId)
    {
        return mapper.revision(studentId, lessonId);
    }

    public long typingRevision(Long studentId, Long lessonId)
    {
        return mapper.typingRevision(studentId, lessonId);
    }

    public Map<String, Object> context(Long lessonId, String entryYear, String classCode)
    {
        classCode = ClassroomRoomKey.normalizeClassCode(classCode);
        assertScope(lessonId, entryYear, classCode);
        Map<String, Object> result = new HashMap<>();
        result.put("hasTyping", redoQuestions(lessonId, true).size() > 0);
        result.put("current", lessonId.equals(assignmentMapper.selectCurrentLessonByClass(
                entryYear, classCode, SecurityUtils.getDeptId())));
        result.put("students", mapper.classStatus(SecurityUtils.getDeptId(), entryYear, classCode, lessonId));
        return result;
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public int reset(TheoryRedoRequest request)
    {
        return reset(request, false);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public int resetTyping(TheoryRedoRequest request)
    {
        return reset(request, true);
    }

    private int reset(TheoryRedoRequest request, boolean typing)
    {
        if (request == null || request.getStudentRevisions() == null || request.getStudentRevisions().isEmpty()
                || request.getStudentRevisions().size() > 200
                || request.getStudentRevisions().entrySet().stream().anyMatch(e -> e.getKey() == null
                    || e.getKey() <= 0 || e.getValue() == null || e.getValue() < 0))
            throw new ServiceException("请选择有效学生，单次最多200人");
        Long lessonId = request.getLessonId();
        String year = request.getEntryYear();
        String classCode = ClassroomRoomKey.normalizeClassCode(request.getClassCode());
        Long deptId = SecurityUtils.getDeptId();
        Long teacherId = SecurityUtils.getUserId();
        assertScope(lessonId, year, classCode);
        List<Long> ids = new ArrayList<>(request.getStudentRevisions().keySet());
        Collections.sort(ids);
        List<BizStudent> students = new ArrayList<>();
        for (Long id : ids)
        {
            if (mapper.lockStudent(id) == null) throw new ServiceException("学生不存在，请刷新名单");
            BizStudent student = studentMapper.selectBizStudentByStudentId(id);
            if (student == null || !deptId.equals(student.getDeptId()) || !year.equals(student.getEntryYear())
                    || !classCode.equals(ClassroomRoomKey.normalizeClassCode(student.getClassCode()))
                    || studentMapper.countManagedScoreStudent(teacherId, deptId, id) <= 0)
                throw new ServiceException("只能重置当前所选班级内有管理权限的学生");
            students.add(student);
        }
        assertCurrent(mapper.lockCurrentAssignment(deptId, year, classCode), lessonId, false);
        List<BizLessonQuestionDetailVo> questions = redoQuestions(lessonId, typing);
        if (questions.isEmpty()) throw new ServiceException(typing ? "本课没有打字题" : "本课没有理论题");
        List<Long> questionIds = questions.stream().map(BizLessonQuestionDetailVo::getQuestionId).collect(Collectors.toList());
        int changed = 0;
        for (BizStudent student : students)
        {
            Long id = student.getStudentId();
            long revision = typing ? typingRevision(id, lessonId) : revision(id, lessonId);
            if (revision != request.getStudentRevisions().get(id))
                throw new ServiceException("已有老师安排重做，请刷新名单后重试");
            BizScoreAdjustment adjustment = adjustmentMapper.selectLatestByStudentAndLesson(id, lessonId, deptId);
            if (adjustment != null && "ADJUST".equals(adjustment.getActionType()) && adjustment.getAdjustedHomeworkScore() != null)
                throw new ServiceException("所选学生存在人工改分，请先取消人工改分再重做");
            List<BizStudentAnswer> oldAnswers = answerMapper.selectByStudentAndLesson(id, lessonId).stream()
                    .filter(a -> questionIds.contains(a.getQuestionId())).collect(Collectors.toList());
            // 未提交和已待重做的学生不重复建轮次；全选时可以自然跳过。
            if (oldAnswers.isEmpty()) continue;
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("answers", oldAnswers);
            snapshot.put("questions", questions);
            int archived = typing
                    ? mapper.archiveTyping(id, lessonId, deptId, year, classCode, teacherId, revision, JSON.toJSONString(snapshot))
                    : mapper.archive(id, lessonId, deptId, year, classCode, teacherId, revision, JSON.toJSONString(snapshot));
            if (archived != 1)
                throw new ServiceException("原答卷留存失败，本次重做已取消");
            answerMapper.deleteByStudentLessonAndQuestions(id, lessonId, questionIds);
            for (BizStudentAnswer oldAnswer : oldAnswers)
                stateWriter.markInTransaction(student, deptId, lessonId, oldAnswer.getQuestionId(), ClassroomTaskStateService.RETURNED);
            changed++;
        }
        return changed;
    }

    /** 必须在交卷事务内调用，并持锁至答案和任务状态全部写入完成。 */
    public BizStudent guardSubmission(Long studentId, Long lessonId, Long expectedRevision)
    {
        if (mapper.lockStudent(studentId) == null) throw new ServiceException("学生不存在");
        BizStudent student = studentMapper.selectBizStudentByStudentId(studentId);
        if (student == null) throw new ServiceException("学生不存在");
        assertCurrent(mapper.lockCurrentAssignment(student.getDeptId(), student.getEntryYear(), student.getClassCode()), lessonId, true);
        long currentRevision = revision(studentId, lessonId);
        // 首轮兼容已有页面；一旦发生重做，缺版本号和旧版本一律拒绝。
        if (currentRevision != (expectedRevision == null ? 0L : expectedRevision))
            throw new ServiceException("老师已安排理论重做，请刷新页面后重新作答");
        Set<Long> ids = theoryQuestions(lessonId).stream().map(BizLessonQuestionDetailVo::getQuestionId).collect(Collectors.toSet());
        if (answerMapper.selectByStudentAndLesson(studentId, lessonId).stream().anyMatch(a -> ids.contains(a.getQuestionId())))
            throw new ServiceException("理论测试已提交，不能重复提交");
        return student;
    }

    private List<BizLessonQuestionDetailVo> theoryQuestions(Long lessonId)
    {
        return redoQuestions(lessonId, false);
    }

    public BizStudent guardTypingSubmission(Long studentId, Long lessonId, Long expectedRevision)
    {
        if (mapper.lockStudent(studentId) == null) throw new ServiceException("学生不存在");
        BizStudent student = studentMapper.selectBizStudentByStudentId(studentId);
        if (student == null) throw new ServiceException("学生不存在");
        assertCurrent(mapper.lockCurrentAssignment(student.getDeptId(), student.getEntryYear(), student.getClassCode()), lessonId, false);
        if (typingRevision(studentId, lessonId) != (expectedRevision == null ? 0L : expectedRevision))
            throw new ServiceException("老师已安排打字重做，请刷新页面后重新打字");
        return student;
    }

    private List<BizLessonQuestionDetailVo> redoQuestions(Long lessonId, boolean typing)
    {
        return questionMapper.selectDetailsByLessonId(lessonId).stream()
                .filter(q -> typing ? "typing".equals(q.getQuestionType())
                        : "choice".equals(q.getQuestionType()) || "judgment".equals(q.getQuestionType()))
                .collect(Collectors.toList());
    }

    private void assertScope(Long lessonId, String year, String classCode)
    {
        if (lessonId == null || year == null || year.trim().isEmpty() || classCode == null || classCode.isEmpty())
            throw new ServiceException("请选择一个班级和一门课程");
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        if (lesson == null || !SecurityUtils.getDeptId().equals(lesson.getDeptId())
                || "attendance".equalsIgnoreCase(lesson.getLessonMode()))
            throw new ServiceException("仅支持本校普通课程重做");
        accessService.assertCanViewLessonClass(lessonId, year, classCode);
    }

    private void assertCurrent(BizLessonAssignment assignment, Long lessonId, boolean requireOpen)
    {
        if (assignment == null || !lessonId.equals(assignment.getLessonId()))
            throw new ServiceException("仅支持该班当前课程，请刷新页面");
        if (requireOpen && !Integer.valueOf(1).equals(assignment.getTheoryOpen()))
            throw new ServiceException("老师尚未开放理论测试");
    }
}
