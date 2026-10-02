package com.ruoyi.business.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.DataLabSubmission;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.DataLabMapper;
import com.ruoyi.business.mapper.PythonPracticeMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/** 实验配置、版本和作品统一归属校验，账号ID与学生档案ID不混用。 */
@Service
public class DataLabAccessService {
    @Autowired private BizQuestionMapper questionMapper;
    @Autowired private BizStudentMapper studentMapper;
    @Autowired private PythonPracticeMapper practiceMapper;
    @Autowired private DataLabMapper mapper;

    public BizQuestion question(Long questionId, boolean edit) {
        if (edit ? !SecurityUtils.hasPermi("business:pythonPractice:edit")
                 : (!SecurityUtils.hasPermi("business:pythonPractice:query") && !SecurityUtils.hasPermi("business:pythonPractice:edit"))) deny();
        BizQuestion question = questionMapper.selectBizQuestionByQuestionId(questionId);
        boolean owner = question != null && Objects.equals(question.getCreatorId(), SecurityUtils.getUserId());
        boolean shared = question != null && ("Y".equals(question.getIsPublic()) || "1".equals(question.getIsPublic()));
        if (question == null || !"practical".equals(question.getQuestionType()) || !"DATA_LAB".equals(question.getPracticalMode())
                || (!SecurityUtils.isAdmin(SecurityUtils.getUserId()) && !owner && (edit || !shared))) deny();
        return question;
    }

    public Map<String, Object> student(Long userId, Long versionId, Long questionId) {
        if (!Objects.equals(userId, SecurityUtils.getUserId())) deny();
        BizStudent student = studentMapper.selectBizStudentByUserId(userId);
        if (student == null || versionId == null || questionId == null) deny();
        List<Map<String, Object>> available = practiceMapper.selectStudentQuestions(student.getDeptId(), student.getEntryYear(), student.getClassCode());
        for (Map<String, Object> row : available) {
            if (same(row.get("source_id"), versionId) && same(row.get("question_id"), questionId)
                    && "DATA_LAB".equals(row.get("practical_mode"))) return row;
        }
        deny(); return null;
    }

    public void teacher(Long versionId, Long questionId) {
        if (!SecurityUtils.hasPermi("business:pythonPractice:query") && !SecurityUtils.hasPermi("business:pythonPractice:edit")) deny();
        Map<String, Object> plan = practiceMapper.selectPlanByVersion(versionId);
        if (plan == null || !same(plan.get("dept_id"), SecurityUtils.getDeptId())) deny();
        if (mapper.countVersionQuestion(versionId, questionId) != 1) deny();
    }

    public boolean managed(Long studentUserId) {
        BizStudent student = studentMapper.selectBizStudentByUserId(studentUserId);
        if (student == null || !Objects.equals(student.getDeptId(), SecurityUtils.getDeptId())) return false;
        boolean privileged = SecurityUtils.isAdmin(SecurityUtils.getUserId()) || SecurityUtils.hasRole("researcher");
        for (Map<String, Object> row : practiceMapper.selectManagedClasses(SecurityUtils.getUserId(), SecurityUtils.getDeptId(), privileged)) {
            if (Objects.equals(String.valueOf(row.get("entry_year")), student.getEntryYear())
                    && Objects.equals(String.valueOf(row.get("class_code")), student.getClassCode())) return true;
        }
        return false;
    }

    public void submission(DataLabSubmission submission, boolean grading) {
        if (submission == null) deny();
        if (!grading && Objects.equals(submission.getStudentId(), SecurityUtils.getUserId())) return;
        teacher(submission.getSourceId(), submission.getQuestionId());
        if (!managed(submission.getStudentId())) deny();
    }

    public void chart(String resource) {
        if (!resource.startsWith("/profile/datalab/charts/")) deny();
        submission(mapper.selectSubmissionByChart(resource), false);
    }

    private static boolean same(Object value, Long expected) { return value != null && expected != null && value.toString().equals(expected.toString()); }
    private static void deny() { throw new ServiceException("无权访问该实验或作品", 403); }
}
