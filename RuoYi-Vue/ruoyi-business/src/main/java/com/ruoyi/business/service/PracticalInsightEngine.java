package com.ruoyi.business.service;

import java.util.*;
import java.util.stream.Collectors;
import com.ruoyi.business.domain.dto.PracticalInsightV3.*;
import com.ruoyi.common.exception.ServiceException;

/** 模型只提供带证据的诊断，人数、分母、门槛和观察名单全部由服务端计算。 */
public final class PracticalInsightEngine
{
    private PracticalInsightEngine() { }
    public static final Set<String> ERROR_CODES = new HashSet<>(Arrays.asList(
            "CONCEPT", "CONDITION", "SYNTAX", "ORDER", "AGGREGATION", "DATA_SOURCE", "OTHER"));
    public static void validatePaper(Paper paper, int pages)
    {
        require(paper != null && paper.questions != null && paper.knowledgePoints != null
                && !paper.questions.isEmpty() && !paper.knowledgePoints.isEmpty()
                && paper.questions.size() <= 100 && paper.knowledgePoints.size() <= 40, "试卷知识点结构不完整");
        Set<String> questions = new HashSet<>(), knowledge = new HashSet<>();
        for (Question q : paper.questions)
        {
            require(q != null && text(q.id) && questions.add(q.id) && text(q.stem)
                    && text(q.referenceAnswer), "试卷题号、题干或参考答案缺失");
            require(q.sourcePages != null && q.sourcePages.stream().allMatch(p -> p != null && p > 0 && p <= pages), "试卷页码越界");
            require(q.parts != null && q.options != null, "题目作答位或选项缺失");
            Set<String> parts = new HashSet<>();
            for (Part part : q.parts) require(part != null && text(part.id) && parts.add(part.id)
                    && text(part.referenceAnswer), "独立作答位重复或缺少参考答案");
            if (q.contextComplete) require(!q.parts.isEmpty(), "完整题目必须列出全部作答位");
            if ("CHOICE".equals(q.type) && q.contextComplete)
                require(q.optionCount >= 2 && q.options.size() == q.optionCount && q.options.stream().allMatch(PracticalInsightEngine::text), "选择题完整选项缺失");
        }
        for (Knowledge k : paper.knowledgePoints)
        {
            require(k != null && text(k.id) && knowledge.add(k.id) && text(k.name) && text(k.rationale)
                    && !k.name.matches(".*(是否正确|答案合理|得分|评分点).*|第[一二三四五六七八九十0-9]+题")
                    && k.questionIds != null && !k.questionIds.isEmpty() && questions.containsAll(k.questionIds), "知识点必须是有题干依据的可教学概念");
            Set<String> patterns = new HashSet<>();
            require(k.errorPatterns != null && k.errorPatterns.size() <= 6, "错误模式目录无效");
            for (ErrorPattern p : k.errorPatterns) require(p != null && text(p.id) && patterns.add(p.id) && text(p.name)
                    && p.questionIds != null && !p.questionIds.isEmpty() && k.questionIds.containsAll(p.questionIds), "错误模式引用无效");
        }
    }
    public static void validateRecognition(Recognition r, Paper paper, int pageCount)
    {
        require(r != null && r.evidence != null && !r.evidence.isEmpty() && r.evidence.size() <= 500, "缺少作答识别证据");
        Set<String> ids = new HashSet<>(), slots = new HashSet<>();
        Set<String> questions = paper.questions.stream().map(q -> q.id).collect(Collectors.toSet());
        for (Evidence e : r.evidence)
        {
            require(e != null && text(e.id) && ids.add(e.id) && questions.contains(e.questionId)
                    && e.page > 0 && e.page <= pageCount && e.text != null
                    && Arrays.asList("written", "blank", "uncertain").contains(e.status)
                    && (!"blank".equals(e.status) || e.text.trim().isEmpty())
                    && (!"written".equals(e.status) || text(e.text)), "作答证据引用或空白状态不合法");
            require(slots.add(e.questionId + ":" + e.partId), "同一作答位被重复识别");
            Question q = paper.questions.stream().filter(x -> x.id.equals(e.questionId)).findFirst().get();
            require(q.parts.isEmpty() || q.parts.stream().anyMatch(p -> p.id.equals(e.partId)), "识别引用了未知作答位");
        }
        for (Question q : paper.questions) for (Part p : q.parts)
            require(slots.contains(q.id + ":" + p.id), "识别未覆盖全部作答位，不能据此统计空白");
    }
    public static void validateResult(Result r, Recognition recognition, Paper paper)
    {
        require(r != null && r.diagnoses != null, "缺少知识诊断");
        Map<String, Evidence> evidence = recognition.evidence.stream().collect(Collectors.toMap(e -> e.id, e -> e));
        Map<String, Knowledge> knowledge = paper.knowledgePoints.stream().collect(Collectors.toMap(k -> k.id, k -> k));
        Set<String> seen = new HashSet<>();
        for (Diagnosis d : r.diagnoses)
        {
            require(d != null && knowledge.containsKey(d.knowledgeId) && seen.add(d.knowledgeId)
                    && d.state != null && d.evidenceIds != null && evidence.keySet().containsAll(d.evidenceIds), "诊断知识点或证据引用无效");
            List<Evidence> refs = d.evidenceIds.stream().map(evidence::get).collect(Collectors.toList());
            require(refs.stream().allMatch(e -> knowledge.get(d.knowledgeId).questionIds.contains(e.questionId)), "诊断引用了无关题目");
            require(d.correctEvidenceIds != null && d.evidenceIds.containsAll(d.correctEvidenceIds)
                    && d.correctEvidenceIds.stream().allMatch(id -> "written".equals(evidence.get(id).status)
                    && !referenceDisputed(evidence.get(id), paper)), "正确证据必须来自本知识点清晰且无参考争议的作答");
            if (judged(d.state)) require(!refs.isEmpty() && refs.stream().anyMatch(e -> "written".equals(e.status)) && text(d.observation), "不能用空白或缺失证据判断掌握");
            if (judged(d.state) || d.state == State.CONFLICT)
                require(refs.stream().filter(e -> "written".equals(e.status)).allMatch(e -> paper.questions.stream()
                        .anyMatch(q -> q.id.equals(e.questionId) && q.contextComplete)), "题目上下文不足，不能判定掌握或矛盾");
            if (d.state == State.MASTERED) require(refs.stream().allMatch(e -> "written".equals(e.status) && !referenceDisputed(e, paper)), "掌握判断不能包含空白、不确定或参考争议证据");
            if (judged(d.state)) require(refs.stream().anyMatch(e -> "written".equals(e.status) && !referenceDisputed(e, paper)), "缺少无参考争议的有效作答，不能判断掌握或错误");
            if (d.state == State.CONFLICT) require(refs.stream().filter(e -> "written".equals(e.status) && !referenceDisputed(e, paper)).map(e -> e.questionId).distinct().count() >= 2
                    && text(d.observation) && text(d.comparability), "矛盾诊断需要不同题清晰证据及同技能可比依据");
            if (d.state == State.BLANK) require(!refs.isEmpty() && refs.stream().allMatch(e -> "blank".equals(e.status)), "空白诊断与原文冲突");
            if (d.state == State.PARTIAL || d.state == State.MISCONCEPTION)
            {
                require(ERROR_CODES.contains(d.errorCode), "错误类别无效");
                require(d.errors != null && !d.errors.isEmpty(), "错误诊断缺少逐题差异及验证问题");
            }
            require(d.errors != null, "错误明细缺失");
            require(!unsupportedClaim(d.observation) && !unsupportedClaim(d.causeHypothesis), "诊断包含无依据的行为推测或班级外推");
            Set<String> occurrences = new HashSet<>();
            for (ErrorDetail error : d.errors)
            {
                require(d.state == State.PARTIAL || d.state == State.MISCONCEPTION || d.state == State.CONFLICT, "非错误状态不能附加错误");
                require(error != null && error.evidenceIds != null && !error.evidenceIds.isEmpty()
                        && d.evidenceIds.containsAll(error.evidenceIds) && occurrences.add(error.questionId + ":" +
                        (text(error.patternId) ? error.patternId : new TreeSet<>(error.evidenceIds) + ":" + error.observedError)), "错误证据重复或引用无效");
                require(error.evidenceIds.stream().map(evidence::get).allMatch(e -> "written".equals(e.status) && e.questionId.equals(error.questionId)), "不能把空白或其他题作为错误证据");
                require(Collections.disjoint(error.evidenceIds, d.correctEvidenceIds), "同一作答不能同时作为正确和错误证据");
                require(error.evidenceIds.stream().map(evidence::get).noneMatch(e -> referenceDisputed(e, paper)), "参考争议作答不能计入错误");
                require(paper.questions.stream().anyMatch(q -> q.id.equals(error.questionId) && q.contextComplete), "题目上下文不足，不能推断错因");
                require(!text(error.patternId) || knowledge.get(d.knowledgeId).errorPatterns.stream().anyMatch(p -> p.id.equals(error.patternId) && p.questionIds.contains(error.questionId)), "错误模式不适用当前题目");
                require(Arrays.asList("CONCEPT", "SKILL", "METHOD", "READING", "OTHER").contains(error.category), "错因category不在约定分类中");
                require(text(error.observedError), "具体差异observedError缺失");
                require(text(error.expectedApproach), "正确做法expectedApproach缺失");
                require(text(error.causeHypothesis), "待验证原因causeHypothesis缺失");
                require(text(error.verificationQuestion), "教师验证问题verificationQuestion缺失");
                require(!unsupportedClaim(error.observedError) && !unsupportedClaim(error.causeHypothesis), "错因包含无依据的行为推测或班级外推");
                if (text(error.actualFragment) || text(error.expectedFragment))
                    require(fragmentGrounded(error, refs, paper), "错误归并片段必须有学生原答或所选选项及正确做法依据");
                if (!text(error.patternId) && text(error.actualFragment))
                    require(!choiceLabel(error.actualFragment) && !choiceLabel(error.expectedFragment), "目录外错误必须使用选项内容，不能仅按选项字母归并");
            }
            // 正确与错误并存是部分掌握；同等任务矛盾仍保留独立状态及可比性校验。
            if (!d.correctEvidenceIds.isEmpty() && !d.errors.isEmpty() && d.state != State.CONFLICT) d.state = State.PARTIAL;
        }
        require(seen.equals(knowledge.keySet()), "诊断未覆盖试卷全部知识点");
        // 原文以识别阶段为准，禁止第二阶段改写学生答案。
        r.evidence = recognition.evidence;
    }
    public static Result keepValidDiagnoses(Result candidate, Recognition recognition, Paper paper)
    {
        Result result = new Result(); result.evidence = recognition.evidence;
        for (Knowledge k : paper.knowledgePoints)
        {
            List<Diagnosis> matches = candidate == null || candidate.diagnoses == null ? Collections.emptyList()
                    : candidate.diagnoses.stream().filter(d -> d != null && k.id.equals(d.knowledgeId)).collect(Collectors.toList());
            try
            {
                require(matches.size() == 1, "知识点诊断缺失或重复");
                Diagnosis diagnosis = matches.get(0);
                // 归并短语只决定能否合并同类错误；删除不可信短语后仍须通过全部主体证据校验。
                if (diagnosis.errors != null) for (ErrorDetail error : diagnosis.errors)
                {
                    if (error == null || (!text(error.actualFragment) && !text(error.expectedFragment))) continue;
                    List<Evidence> refs = recognition.evidence.stream()
                            .filter(e -> error.evidenceIds != null && error.evidenceIds.contains(e.id)).collect(Collectors.toList());
                    if (!fragmentGrounded(error, refs, paper) || (!text(error.patternId)
                            && (choiceLabel(error.actualFragment) || choiceLabel(error.expectedFragment))))
                    {
                        error.actualFragment = ""; error.expectedFragment = "";
                        result.reviewReasons.add(k.name + "：可选归并片段无可靠依据，已排除自动归并");
                    }
                }
                Paper subset = new Paper(); subset.questions = paper.questions; subset.knowledgePoints = Collections.singletonList(k);
                Result single = new Result(); single.diagnoses.add(matches.get(0)); validateResult(single, recognition, subset);
                result.diagnoses.add(matches.get(0));
            }
            catch (ServiceException e)
            {
                // 局部证据不可信只隔离当前知识点，不能把整份学生已验证的证据丢弃。
                Diagnosis pending = new Diagnosis(); pending.knowledgeId = k.id; pending.state = State.INSUFFICIENT;
                pending.observation = "待核对：" + e.getMessage(); result.diagnoses.add(pending);
                result.reviewReasons.add(k.name + "：" + e.getMessage());
            }
        }
        return result;
    }
    public static void validateTeaching(Teaching t, Paper p, int minutes)
    {
        require(t != null && t.activities != null && !t.activities.isEmpty() && t.activities.size() <= 5, "教学安排结构无效");
        Set<String> ids = p.knowledgePoints.stream().map(k -> k.id).collect(Collectors.toSet());
        int total = 0;
        for (Activity a : t.activities)
        {
            require(a != null && ids.contains(a.knowledgeId) && a.minutes > 0 && text(a.objective)
                    && text(a.explanation) && text(a.foundationPractice) && text(a.extensionPractice)
                    && text(a.referenceAnswer) && text(a.exitQuestion) && text(a.exitAnswer)
                    && text(a.successCriteria), "教学活动缺少练习、答案或达标检测");
            total += a.minutes;
        }
        require(total <= minutes, "教学安排超过本课时长");
    }
    public static Map<String, Object> aggregate(Input input, Paper paper, Map<Long, Result> results,
                                               int minSamples, double minCoverage)
    {
        List<Map<String, Object>> stats = new ArrayList<>(), matrix = new ArrayList<>(), focus = new ArrayList<>();
        minSamples = Math.max(10, minSamples);
        minCoverage = Double.isFinite(minCoverage) ? Math.max(.8, minCoverage) : 1;
        Map<String, Map<String, Object>> errors = new LinkedHashMap<>();
        int validStudents = 0;
        for (Student s : input.students)
        {
            Result result = results.get(s.studentId);
            Map<String, Diagnosis> byKnowledge = new LinkedHashMap<>();
            if (result != null) for (Diagnosis d : result.diagnoses) byKnowledge.put(d.knowledgeId, d);
            if (byKnowledge.values().stream().anyMatch(d -> judged(d.state))) validStudents++;
            List<Map<String, Object>> cells = new ArrayList<>();
            List<String> reasons = new ArrayList<>();
            int unknown = 0;
            Map<String, Set<String>> repeated = new LinkedHashMap<>();
            for (Knowledge k : paper.knowledgePoints)
            {
                Diagnosis d = byKnowledge.get(k.id);
                State state = d == null ? State.INSUFFICIENT : d.state;
                cells.add(map("knowledgeId", k.id, "state", state, "diagnosis", d));
                if (state == State.INSUFFICIENT || state == State.NOT_COVERED) unknown++;
                if (state == State.CONFLICT) reasons.add(k.name + "：跨题表现矛盾，建议口头追问");
                if (k.core && (state == State.PARTIAL || state == State.MISCONCEPTION)
                        && s.maxScore != null && s.maxScore > 0 && s.score != null && s.score.doubleValue() / s.maxScore >= .85)
                    reasons.add("总分较好但核心知识薄弱：" + k.name);
                if (d != null) for (ErrorDetail detail : d.errors)
                {
                    ErrorPattern pattern = k.errorPatterns.stream().filter(p -> p.id.equals(detail.patternId)).findFirst().orElse(null);
                    // 目录外错误仅在具体原答与正确片段一致时归并，不按宽泛类别拼成共性。
                    boolean grounded = pattern == null && !choiceLabel(detail.actualFragment) && !choiceLabel(detail.expectedFragment)
                            && fragmentGrounded(detail, result.evidence, paper);
                    String derived = grounded ? "literal:" + detail.category + ":" + detail.actualFragment.length() + ":" + detail.actualFragment + ":" + detail.expectedFragment : null;
                    String key = k.id + ":" + (pattern != null ? pattern.id : grounded ? derived : s.studentId + ":" + detail.questionId);
                    Map<String, Object> error = errors.computeIfAbsent(key, x -> map("id", key, "knowledgeId", k.id,
                            "name", pattern != null ? pattern.name : grounded ? "将 " + detail.expectedFragment + " 写成 " + detail.actualFragment : detail.observedError, "patternId", detail.patternId,
                            "derivedPattern", grounded, "questionIds", new ArrayList<>(pattern == null ? Collections.singletonList(detail.questionId) : pattern.questionIds),
                            "studentIds", new ArrayList<Long>(), "examples", new ArrayList<Object>()));
                    List<String> applicable = (List<String>) error.get("questionIds");
                    if (!applicable.contains(detail.questionId)) applicable.add(detail.questionId);
                    List<Long> ids = (List<Long>) error.get("studentIds"); if (!ids.contains(s.studentId)) ids.add(s.studentId);
                    ((List<Object>) error.get("examples")).add(map("studentId", s.studentId, "questionId", detail.questionId,
                            "observation", detail.observedError, "expectedApproach", detail.expectedApproach, "category", detail.category,
                            "causeHypothesis", detail.causeHypothesis, "verificationQuestion", detail.verificationQuestion,
                            "evidenceIds", detail.evidenceIds, "originalAnswers", result.evidence.stream().filter(e -> detail.evidenceIds.contains(e.id)).collect(Collectors.toList())));
                    if (pattern != null || grounded) repeated.computeIfAbsent(key, x -> new HashSet<>()).add(detail.questionId);
                }
            }
            for (Map.Entry<String, Set<String>> entry : repeated.entrySet()) if (entry.getValue().size() >= 2)
                reasons.add("跨" + entry.getValue().size() + "题重复出现：" + errors.get(entry.getKey()).get("name"));
            int expectedSlots = paper.questions.stream().mapToInt(q -> q.parts.size()).sum();
            long blankSlots = result == null ? 0 : result.evidence.stream().filter(e -> "blank".equals(e.status))
                    .map(e -> e.questionId + ":" + e.partId).distinct().count();
            if (expectedSlots > 0 && blankSlots * 2 >= expectedSlots) reasons.add("大量空白：" + blankSlots + "/" + expectedSlots + "个作答位，先查看原卷核实");
            if (unknown > 0 && result != null) reasons.add("部分知识点证据不足，补充原卷或口头检测");
            if (s.maxScore != null && s.maxScore > 0 && s.score != null && s.score.doubleValue() / s.maxScore < .6) reasons.add("正式成绩低于60%，结合具体知识点补练");
            Map<String, Object> row = map("studentId", s.studentId, "name", s.name, "number", s.number,
                    "score", s.score, "maxScore", s.maxScore, "cells", cells);
            matrix.add(row);
            if (!reasons.isEmpty()) focus.add(map("studentId", s.studentId, "name", s.name, "score", s.score, "reasons", reasons,
                    "priority", reasons.stream().anyMatch(x -> x.contains("核心知识薄弱") || x.contains("表现矛盾") || x.contains("跨") && x.contains("重复")) ? 0
                            : reasons.stream().anyMatch(x -> x.contains("空白") || x.contains("证据不足")) ? 1 : 2));
        }
        for (Knowledge k : paper.knowledgePoints)
        {
            Map<State, List<Long>> members = new EnumMap<>(State.class);
            for (State state : State.values()) members.put(state, new ArrayList<>());
            for (Student s : input.students)
            {
                Result r = results.get(s.studentId);
                Diagnosis d = r == null ? null : r.diagnoses.stream().filter(x -> k.id.equals(x.knowledgeId)).findFirst().orElse(null);
                members.get(d == null ? State.INSUFFICIENT : d.state).add(s.studentId);
            }
            int mastered = members.get(State.MASTERED).size(), partial = members.get(State.PARTIAL).size(), wrong = members.get(State.MISCONCEPTION).size();
            int valid = mastered + partial + wrong;
            boolean sufficient = sufficient(valid, input.total, minSamples, minCoverage);
            stats.add(map("id", k.id, "name", k.name, "core", k.core, "questionIds", k.questionIds,
                    "mastered", mastered, "partial", partial, "misconception", wrong, "valid", valid,
                    "unknown", input.total - valid, "members", members, "sufficient", sufficient,
                    "masteryRate", percent(mastered, valid), "issueRate", percent(partial + wrong, valid),
                    "coverage", percent(valid, input.total)));
        }
        List<Map<String, Object>> ranked = new ArrayList<>(stats);
        ranked.sort(Comparator.<Map<String, Object>>comparingInt(k -> ((Number) k.get("partial")).intValue() + ((Number) k.get("misconception")).intValue()).reversed()
                .thenComparing(k -> !Boolean.TRUE.equals(k.get("core"))));
        List<Map<String, Object>> strengths = stats.stream().filter(k -> ((Number) k.get("valid")).intValue() > 0
                && ((Number) k.get("masteryRate")).doubleValue() >= 80).sorted(Comparator.comparingDouble(k -> -((Number) k.get("masteryRate")).doubleValue())).limit(3).collect(Collectors.toList());
        List<Map<String, Object>> weaknesses = ranked.stream().filter(k -> ((Number) k.get("partial")).intValue() + ((Number) k.get("misconception")).intValue() > 0).limit(3).collect(Collectors.toList());
        for (Map<String, Object> error : errors.values())
        {
            int count = ((List<?>) error.get("studentIds")).size();
            List<String> questionIds = (List<String>) error.get("questionIds");
            // 已明确犯错者也属于模式可判定样本，不能因另一适用题空白把分子排除在分母外。
            Set<Long> eligible = new HashSet<>((List<Long>) error.get("studentIds"));
            for (Map.Entry<Long, Result> entry : results.entrySet()) {
                Result r = entry.getValue();
                if (r.diagnoses.stream().anyMatch(d -> d.knowledgeId.equals(error.get("knowledgeId")) && judged(d.state))
                        && questionIds.stream().allMatch(id -> paper.questions.stream().filter(q -> q.id.equals(id)).allMatch(q -> !q.parts.isEmpty()
                        && q.parts.stream().allMatch(p -> !disputed(p) && r.evidence.stream().anyMatch(e -> id.equals(e.questionId) && p.id.equals(e.partId) && "written".equals(e.status)))))) eligible.add(entry.getKey());
            }
            int denominator = eligible.size();
            boolean enoughForPattern = (text((String) error.get("patternId")) || Boolean.TRUE.equals(error.get("derivedPattern"))) && sufficient(denominator, input.total, minSamples, minCoverage);
            error.put("count", count); error.put("occurrences", ((List<?>) error.get("examples")).size());
            error.put("denominator", denominator); error.put("rate", percent(count, denominator));
            error.put("sufficient", enoughForPattern); error.put("common", enoughForPattern && count >= 2);
        }
        List<Map<String, Object>> errorStats = new ArrayList<>(errors.values());
        errorStats.sort(Comparator.comparingInt(e -> -((Number) e.get("count")).intValue()));
        focus.sort(Comparator.comparingInt(s -> ((Number) s.get("priority")).intValue()));
        boolean enough = sufficient(validStudents, input.total, minSamples, minCoverage);
        return map("schemaVersion", "3", "paper", paper, "coverage", map("total", input.total,
                "confirmed", input.students.size(), "valid", validStudents, "rate", percent(validStudents, input.total),
                "sufficient", enough, "minSamples", minSamples, "minCoverage", minCoverage,
                "message", enough ? "按各知识点有效样本解释本次表现" : "有效样本不足，仅反映已分析样本，不能代表全班"),
                "knowledgeStats", stats, "strengths", strengths, "weaknesses", weaknesses, "errorStats", errorStats,
                "studentMatrix", matrix, "focusStudents", focus, "scoringStats", input.scoringStats,
                "scope", input.scope, "gradeLabel", input.gradeLabel, "limitations", paper.limitations);
    }
    public static boolean sufficient(int valid, int total, int min, double coverage)
    { return total > 0 && valid >= Math.max(10, min) && valid * 1.0 / total >= Math.max(.8, coverage); }
    public static Double percent(int numerator, int denominator)
    { return denominator <= 0 ? null : Math.round(numerator * 10000.0 / denominator) / 100.0; }
    public static boolean judged(State state)
    { return state == State.MASTERED || state == State.PARTIAL || state == State.MISCONCEPTION; }
    private static boolean referenceDisputed(Evidence e, Paper paper)
    { return paper.questions.stream().filter(q -> q.id.equals(e.questionId)).flatMap(q -> q.parts.stream())
            .anyMatch(p -> p.id.equals(e.partId) && disputed(p)); }
    private static boolean disputed(Part p)
    { return p.referenceStatus == null ? text(p.referenceIssue) : !"CLEAR".equals(p.referenceStatus); }

    private static boolean choiceLabel(String value) { return value != null && value.trim().matches("[A-Za-z]"); }

    private static boolean fragmentGrounded(ErrorDetail error, List<Evidence> evidence, Paper paper)
    {
        if (!text(error.actualFragment) || !text(error.expectedFragment) || error.actualFragment.length() > 120
                || error.expectedFragment.length() > 120 || error.actualFragment.equals(error.expectedFragment)
                || !text(error.expectedApproach) || !error.expectedApproach.contains(error.expectedFragment)) return false;
        // 只合并原文可核对的具体片段，绝不按概念/技能等宽泛类别合并。
        for (Evidence e : evidence) if (error.evidenceIds.contains(e.id) && "written".equals(e.status)) {
            if (e.text.contains(error.actualFragment)) return true;
            if (e.text.trim().matches("[A-Z]")) for (Question q : paper.questions) if (q.id.equals(e.questionId))
                for (String option : q.options) if (option.matches("(?s)^\\s*" + e.text.trim() + "[.、:：)）\\s].*")
                        && option.contains(error.actualFragment)) return true;
        }
        return false;
    }
    private static String errorName(String code)
    {
        switch (code) {
            case "CONCEPT": return "概念理解"; case "CONDITION": return "条件判断"; case "SYNTAX": return "语法使用";
            case "ORDER": return "步骤顺序"; case "AGGREGATION": return "聚合方式"; case "DATA_SOURCE": return "数据来源";
            default: return "其他错误（需核对）";
        }
    }
    public static Map<String, Object> map(Object... pairs)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) result.put((String) pairs[i], pairs[i + 1]);
        return result;
    }
    private static boolean text(String v) { return v != null && !v.trim().isEmpty() && v.length() <= 12000; }
    private static boolean unsupportedClaim(String v)
    { return v != null && v.matches("(?s).*(粗心|学习习惯|态度不端正|全班普遍|全班都|所有学生都|普遍存在|大多数学生|全体学生均).*"); }
    private static void require(boolean condition, String message) { if (!condition) throw new ServiceException(message); }
}
