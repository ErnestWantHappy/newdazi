package com.ruoyi.business.service;

import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.*;
import com.ruoyi.business.domain.dto.PracticalInsightV3.*;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.mapper.*;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 个人反馈只取当前登录学生；发布冻结快照，不复用教师整班报告接口。 */
@Service
public class StudentFeedbackService {
    @Autowired private StudentFeedbackMapper feedback;
    @Autowired private PracticalInsightMapper insights;
    @Autowired private PracticalInsightReportService reports;
    @Autowired private BizStudentMapper students;
    @Autowired private BizStudentAnswerMapper answers;
    @Autowired private BizLessonQuestionMapper questions;
    @Autowired private BizScoringDetailMapper details;
    @Autowired private PracticalRubricSnapshotMapper rubricMapper;
    @Autowired private PracticalRubricSnapshotService rubrics;
    @Autowired private PracticalArtifactService artifacts;
    @Autowired private FlowchartMapper flowcharts;
    @Autowired private GuideSheetAccessService access;
    @Autowired private ClassroomTaskStateService states;
    @Autowired private ObjectMapper json;

    public BizStudent currentStudent() {
        BizStudent student = students.selectBizStudentByUserId(SecurityUtils.getUserId());
        if (student == null) throw new ServiceException("学生信息不存在");
        access.assertNoPendingCountyExam();
        return student;
    }
    public List<Map<String,Object>> lessons() { return feedback.lessons(currentStudent().getStudentId()); }

    public List<Map<String,Object>> mine(Long lessonId) throws Exception {
        BizStudent student = currentStudent();
        List<BizStudentAnswer> own = answers.selectByStudentAndLesson(student.getStudentId(), lessonId);
        List<Map<String,Object>> result = new ArrayList<>();
        if (own == null || own.isEmpty()) return result;
        Map<Long,BizLessonQuestionDetailVo> byId = questions.selectDetailsByLessonId(lessonId).stream()
                .collect(Collectors.toMap(BizLessonQuestionDetailVo::getQuestionId, q -> q, (a,b) -> a));
        for (BizStudentAnswer answer : own) {
            BizLessonQuestionDetailVo question = byId.get(answer.getQuestionId());
            if (question == null) continue;
            Map<String,Object> row = new LinkedHashMap<>();
            row.put("questionId", question.getQuestionId()); row.put("question", question.getQuestionContent());
            row.put("questionType", question.getQuestionType()); row.put("practicalMode", question.getPracticalMode());
            row.put("score", answer.getScore()); row.put("maxScore", question.getQuestionScore()); row.put("submittedAt", answer.getSubmitTime());
            boolean submitted = answer.getScore() != null || answer.getPracticalVersionId() != null
                    || com.ruoyi.common.utils.StringUtils.isNotBlank(answer.getStudentAnswer());
            row.put("submitted", submitted);
            row.put("taskState", !submitted ? "WORKING" : states.isReturned(student.getStudentId(),lessonId,question.getQuestionId()) ? "RETURNED" : answer.getScore() == null ? "SUBMITTED" : "GRADED");
            row.put("isCorrect", answer.getIsCorrect());
            if (!"practical".equalsIgnoreCase(question.getQuestionType())) {
                row.put("answer", answer.getStudentAnswer());
                // 未出正式成绩前不额外泄露标准答案，沿用原答题边界。
                if (answer.getScore() != null) { row.put("reference", question.getAnswer()); row.put("analysis", question.getAnalysis()); }
            } else if (answer.getPracticalVersionId() != null) {
                row.put("artifact", artifacts.getCurrentLessonArtifact(student.getStudentId(),lessonId,question.getQuestionId()));
                PracticalRubricSnapshot rubric = rubricMapper.selectByVersionId(answer.getPracticalVersionId());
                if (rubric != null) { row.put("criteria", rubrics.buildScoringItems(rubric)); row.put("maxScore", rubric.getQuestionScore()); }
                if (answer.getScore() != null) row.put("scoringDetails", details.selectDetailsByAnswerId(answer.getAnswerId()));
            } else if (answer.getStudentAnswer() != null && answer.getStudentAnswer().startsWith("FLOWCHART:")) {
                Long id = Long.valueOf(answer.getStudentAnswer().substring("FLOWCHART:".length()));
                FlowchartSubmission submission = flowcharts.selectSubmissionById(id);
                if (submission != null && student.getStudentId().equals(submission.getStudentId())) row.put("flowchartDocument",submission.getDocumentJson());
            }
            Map<String,Object> publication = feedback.latest(student.getStudentId(),student.getDeptId(),lessonId,question.getQuestionId());
            if (publication != null) {
                Map<String,Object> personal = json.readValue(String.valueOf(publication.get("payloadJson")),Map.class);
                personal.put("publishedAt",publication.get("publishedAt"));
                personal.put("oldVersion",!Objects.equals(asLong(publication.get("sourceVersionId")), answer.getPracticalVersionId()));
                row.put("aiFeedback",personal);
            }
            result.add(row);
        }
        return result;
    }

    @Transactional(rollbackFor=Exception.class)
    public Map<String,Object> publish(Long reportId) throws Exception {
        Map<String,Object> view = reports.get(reportId);
        if (Boolean.TRUE.equals(view.get("stale"))) throw new ServiceException("报告材料或成绩已变化，请生成最新诊断后发布");
        Job job = insights.getForUpdate(reportId);
        if (job == null || !Objects.equals(job.teacherId,SecurityUtils.getUserId())) throw new ServiceException("无权发布该报告");
        if (!Arrays.asList("COMPLETED","PARTIAL").contains(job.status) || job.reportJson == null)
            throw new ServiceException("请等待诊断完成后发布");
        List<EvidenceRow> evidenceRows = insights.evidence(reportId);
        String publicationHash = publicationHash(job,evidenceRows);
        Long existing = feedback.publishedReport(reportId,publicationHash);
        if (existing != null) return PracticalInsightEngine.map("publicationId",existing,"alreadyPublished",true);
        Input input = json.readValue(job.inputJson,Input.class);
        Map<Long,EvidenceRow> evidence = evidenceRows.stream().collect(Collectors.toMap(e -> e.studentId,e -> e,(a,b) -> a));
        Map<String,Object> report = json.readValue(job.reportJson,Map.class);
        Paper paper = json.convertValue(report.get("paper"),Paper.class);
        feedback.publish(job,publicationHash);
        Long publication = feedback.publishedReport(reportId,publicationHash);
        for (Student student : input.students) {
            EvidenceRow source = evidence.get(student.studentId);
            Result diagnosis = source != null && "SUCCESS".equals(source.status) && source.resultJson != null ? json.readValue(source.resultJson,Result.class) : null;
            Map<String,Object> personal = personalSnapshot(student,diagnosis,paper);
            feedback.snapshot(publication,student.studentId,student.versionId,json.writeValueAsString(personal));
        }
        return PracticalInsightEngine.map("publicationId",publication,"studentCount",input.students.size());
    }

    public static String publicationHash(Job job,List<EvidenceRow> evidence) throws Exception {
        List<EvidenceRow> ordered = new ArrayList<>(evidence);
        ordered.sort(Comparator.comparing(e -> e.studentId));
        // 重试会复用报告编号；按内容识别发布版本，保留学生已看到的旧快照。
        List<Object> content = new ArrayList<>();
        content.add(job.inputJson); content.add(job.reportJson);
        for (EvidenceRow row : ordered) content.add(Arrays.asList(row.studentId,row.status,row.resultJson));
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(new ObjectMapper().writeValueAsString(content));
    }
    public static Map<String,Object> personalSnapshot(Student student, Result result, Paper paper) {
        Map<String,Object> personal = new LinkedHashMap<>();
        personal.put("scoreAtAnalysis",student.score); personal.put("maxScoreAtAnalysis",student.maxScore);
        List<Map<String,Object>> diagnoses = new ArrayList<>();
        if (result != null) {
            Set<String> ids = result.evidence.stream().map(e -> e.id).collect(Collectors.toSet());
            for (Diagnosis diagnosis : result.diagnoses) {
                List<ErrorDetail> errors = diagnosis.errors.stream().filter(e -> e.evidenceIds.stream().anyMatch(ids::contains)).collect(Collectors.toList());
                if (!diagnosis.evidenceIds.stream().anyMatch(ids::contains) && errors.isEmpty()) continue;
                String name = paper == null ? "知识点" : paper.knowledgePoints.stream().filter(k -> Objects.equals(k.id,diagnosis.knowledgeId)).map(k -> k.name).findFirst().orElse("知识点");
                diagnoses.add(PracticalInsightEngine.map("knowledge",name,"state",diagnosis.state,"observation",diagnosis.observation,"errors",errors));
            }
            personal.put("evidence",result.evidence); personal.put("reviewReasons",result.reviewReasons);
        }
        personal.put("diagnoses",diagnoses);
        return personal;
    }
    private Long asLong(Object value) { return value == null ? null : Long.valueOf(value.toString()); }
}
