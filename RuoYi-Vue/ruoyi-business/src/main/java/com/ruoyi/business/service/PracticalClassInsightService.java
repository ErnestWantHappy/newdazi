package com.ruoyi.business.service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.*;
import com.ruoyi.business.domain.dto.PaperImportRequest;
import com.ruoyi.business.domain.vo.*;
import com.ruoyi.business.mapper.*;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** 学情只读取正式成绩；每次读取按实际内容计算指纹，改分后旧报告自动失效。 */
@Service
public class PracticalClassInsightService
{
    @Autowired private TeacherPaperSubmissionService scope;
    @Autowired private BizStudentAnswerMapper answers;
    @Autowired private BizScoringDetailMapper details;
    @Autowired private PracticalArtifactMapper artifacts;
    @Autowired private PracticalRubricSnapshotMapper snapshots;
    @Autowired private PracticalRubricSnapshotService rubrics;
    @Autowired private PaperScanVisionGateway vision;
    @Autowired private RedisCache cache;
    @Autowired private ObjectMapper json;

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Map<String, Object> snapshot(PaperImportRequest request) throws Exception
    {
        BizLessonQuestionDetailVo question = scope.requireQuestion(request);
        Long deptId = scope.requireScope(request);
        List<PracticalSubmissionVo> rows = answers.selectPracticalSubmissions(request.lessonId,
                request.questionId, request.classCode, request.entryYear, deptId);
        Set<Long> activeStudents = new HashSet<>();
        scope.roster(request).forEach(student -> activeStudents.add(student.getStudentId()));
        rows = new ArrayList<>(rows);
        rows.removeIf(row -> !activeStudents.contains(row.getStudentId()));
        List<Map<String, Object>> confirmed = new ArrayList<>();
        List<Map<String, Object>> studentProfiles = new ArrayList<>();
        Map<String, Map<String, Object>> points = new TreeMap<>();
        Map<String, Integer> groups = new LinkedHashMap<>();
        groups.put("待加强（低于60%）", 0);
        groups.put("基本掌握（60%至不足85%）", 0);
        groups.put("掌握较好（85%及以上）", 0);
        int submitted = 0;
        int sum = 0;
        int missingDetails = 0;
        for (PracticalSubmissionVo row : rows)
        {
            if (Boolean.TRUE.equals(row.getSubmitted())) submitted++;
            if (!isConfirmed(row)) continue;
            PracticalRubricSnapshot rubric = row.getPracticalVersionId() == null ? null
                    : snapshots.selectByVersionId(row.getPracticalVersionId());
            if (row.getPracticalVersionId() != null)
            {
                PracticalArtifact artifact = artifacts.selectByContext("LESSON", request.lessonId, row.getStudentId(), request.questionId);
                if (artifact == null || !Objects.equals(artifact.getCurrentVersionId(), row.getPracticalVersionId())) continue;
            }
            List<BizScoringDetail> scoreDetails = details.selectDetailsByAnswerId(row.getAnswerId());
            scoreDetails.sort(Comparator.comparing(BizScoringDetail::getItemId));
            Map<String, Object> fact = new LinkedHashMap<>();
            fact.put("answerId", row.getAnswerId());
            fact.put("versionId", row.getPracticalVersionId());
            fact.put("score", row.getScore());
            fact.put("rubric", rubric);
            fact.put("details", scoreDetails);
            confirmed.add(fact);
            Map<String, Object> student = new LinkedHashMap<>();
            student.put("studentId", row.getStudentId());
            student.put("studentName", row.getStudentName());
            student.put("studentNo", row.getStudentNo());
            student.put("score", row.getScore());
            student.put("maxScore", maxScore(row, rubric));
            student.put("evidenceStatus", rubric == null || scoreDetails.isEmpty() ? "证据不足" : "已有评分证据");
            student.put("missingDetails", rubric == null || scoreDetails.isEmpty());
            studentProfiles.add(student);
            sum += row.getScore();
            int max = maxScore(row, rubric);
            double ratio = max > 0 ? row.getScore() * 1.0 / max : -1;
            if (ratio >= 0)
            {
                String group = ratio < 0.6 ? "待加强（低于60%）" : ratio < 0.85 ? "基本掌握（60%至不足85%）" : "掌握较好（85%及以上）";
                groups.put(group, groups.get(group) + 1);
            }
            if (rubric == null || scoreDetails.isEmpty()) { missingDetails++; continue; }
            for (PracticalScoringItemVo item : rubrics.buildScoringItems(rubric))
            {
                BizScoringDetail scored = scoreDetails.stream().filter(d -> Objects.equals(d.getItemId(), item.getItemId())).findFirst().orElse(null);
                if (scored == null || scored.getScore() == null) continue;
                String key = rubric.getSnapshotId() + ":" + item.getItemId();
                Map<String, Object> point = points.computeIfAbsent(key, k -> {
                    Map<String, Object> p = new LinkedHashMap<>();
                    p.put("name", item.getItemName()); p.put("rubricVersion", rubric.getSnapshotVersion());
                    p.put("maxScore", item.getMaxScore()); p.put("count", 0); p.put("sum", 0); p.put("lostCount", 0);
                    return p;
                });
                point.put("count", (Integer) point.get("count") + 1);
                point.put("sum", (Integer) point.get("sum") + scored.getScore());
                if (item.getMaxScore() != null && scored.getScore() < item.getMaxScore()) point.put("lostCount", (Integer) point.get("lostCount") + 1);
            }
        }
        for (Map<String, Object> point : points.values())
        {
            int count = (Integer) point.get("count");
            int lost = (Integer) point.get("lostCount");
            point.put("average", count == 0 ? null : Math.round((Integer) point.get("sum") * 100.0 / count) / 100.0);
            point.put("issueRate", count == 0 ? null : Math.round(lost * 10000.0 / count) / 100.0);
            point.put("evidenceStatus", count == 0 ? "证据不足" : "已有评分证据");
        }
        List<Map<String, Object>> focusStudents = new ArrayList<>();
        for (Map<String, Object> student : studentProfiles)
        {
            Number score = (Number) student.get("score");
            Number max = (Number) student.get("maxScore");
            if (Boolean.TRUE.equals(student.get("missingDetails")) || (score != null && max != null && max.intValue() > 0
                    && score.doubleValue() / max.doubleValue() < 0.6))
            {
                Map<String, Object> focus = new LinkedHashMap<>(student);
                focus.put("reason", Boolean.TRUE.equals(student.get("missingDetails")) ? "缺少可追溯分项证据，需教师核对" : "本题得分低于60%，建议优先面谈");
                focusStudents.add(focus);
            }
        }
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", rows.size()); stats.put("submitted", submitted);
        stats.put("confirmed", confirmed.size()); stats.put("unconfirmed", submitted - confirmed.size());
        stats.put("average", confirmed.isEmpty() ? null : Math.round(sum * 100.0 / confirmed.size()) / 100.0);
        stats.put("missingDetails", missingDetails); stats.put("points", points.values()); stats.put("groups", groups);
        stats.put("question", question.getQuestionContent());
        stats.put("scope", "当前课程/班级/题目（操作题）");
        stats.put("gradeLabel", request.entryYear == null ? "年级待确认" : request.entryYear + "级");
        stats.put("questionContent", question.getQuestionContent());
        stats.put("examSummary", Collections.singletonMap("coverage", "当前题目，未自动混入其他题目"));
        stats.put("studentProfiles", studentProfiles);
        stats.put("studentFocus", focusStudents);
        stats.put("knowledgePointCandidates", buildKnowledgeCandidates(question, points.values()));
        stats.put("evidenceCoverage", missingDetails == 0 ? "评分证据完整" : "部分样本缺少分项依据，结论需核对");
        String fingerprint = DigestUtils.sha256Hex(json.writeValueAsString(Arrays.asList(request.lessonId,
                request.questionId, deptId, request.entryYear, request.classCode, stats, confirmed)));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", stats); result.put("fingerprint", fingerprint);
        String cached = cache.getCacheObject(key(fingerprint));
        result.put("analysis", cached == null ? null : json.readValue(cached, Map.class));
        return result;
    }

    private int maxScore(PracticalSubmissionVo row, PracticalRubricSnapshot rubric)
    {
        return rubric == null ? (row.getMaxScore() == null ? 0 : row.getMaxScore()) : (rubric.getQuestionScore() == null ? 0 : rubric.getQuestionScore());
    }

    private List<Map<String, Object>> buildKnowledgeCandidates(BizLessonQuestionDetailVo question, Collection<Map<String, Object>> pointValues)
    {
        List<Map<String, Object>> candidates = new ArrayList<>();
        // 评分项不能作为知识点；真实映射由 V3 读取题干和参考材料生成。
        return candidates;
    }

    static boolean isConfirmed(PracticalSubmissionVo row)
    {
        return Boolean.TRUE.equals(row.getSubmitted()) && row.getScore() != null
                && !"RETURNED".equals(row.getTaskState());
    }

    public Map<String, Object> generate(Map<String, Object> snapshot) throws Exception
    {
        throw new ServiceException("请使用V3证据诊断入口，旧统计不再生成知识结论");
    }

    private String key(String fingerprint) { return "teacher:practical-insight:" + SecurityUtils.getUserId() + ":" + fingerprint; }
}
