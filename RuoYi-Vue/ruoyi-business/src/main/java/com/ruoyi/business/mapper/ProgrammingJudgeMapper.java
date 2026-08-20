package com.ruoyi.business.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.ProgrammingDraft;
import com.ruoyi.business.domain.ProgrammingQuestionConfig;
import com.ruoyi.business.domain.ProgrammingSubmission;
import com.ruoyi.business.domain.ProgrammingSubmissionCase;
import com.ruoyi.business.domain.ProgrammingTestCase;

public interface ProgrammingJudgeMapper {
    ProgrammingQuestionConfig selectConfig(@Param("questionId") Long questionId);
    List<ProgrammingTestCase> selectTestCases(@Param("questionId") Long questionId);
    List<ProgrammingTestCase> selectPublicTestCases(@Param("questionId") Long questionId);
    int upsertConfig(ProgrammingQuestionConfig config);
    int deleteTestCases(@Param("questionId") Long questionId);
    int insertTestCase(ProgrammingTestCase testCase);
    ProgrammingDraft selectDraft(@Param("studentId") Long studentId, @Param("lessonId") Long lessonId, @Param("questionId") Long questionId);
    int upsertDraft(ProgrammingDraft draft);
    ProgrammingSubmission selectSubmissionByKey(@Param("studentId") Long studentId, @Param("lessonId") Long lessonId, @Param("questionId") Long questionId, @Param("submissionKey") String submissionKey);
    ProgrammingSubmission selectSubmissionById(@Param("submissionId") Long submissionId);
    List<ProgrammingSubmission> selectStudentSubmissions(@Param("studentId") Long studentId, @Param("lessonId") Long lessonId, @Param("questionId") Long questionId, @Param("limit") int limit);
    int insertSubmission(ProgrammingSubmission submission);
    int markJudging(@Param("submissionId") Long submissionId, @Param("judgingAt") Date judgingAt);
    int updateSubmissionResult(ProgrammingSubmission submission);
    int insertSubmissionCase(ProgrammingSubmissionCase submissionCase);
    List<ProgrammingSubmissionCase> selectSubmissionCases(@Param("submissionId") Long submissionId, @Param("publicOnly") boolean publicOnly);
    int cancelSubmission(@Param("submissionId") Long submissionId, @Param("studentId") Long studentId, @Param("cancelledAt") Date cancelledAt);
    int countActiveClassSubmissions(@Param("lessonId") Long lessonId, @Param("questionId") Long questionId, @Param("deptId") Long deptId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);
    List<ProgrammingSubmission> selectStuckSubmissions(@Param("before") Date before, @Param("limit") int limit);
}
